package com.shampaggon.crackshot;

import com.shampaggon.crackshot.CSMessages.Message;
import com.shampaggon.crackshot.compatibility.MaterialManager;
import com.shampaggon.crackshot.compatibility.SoundManager;
import com.shampaggon.crackshot.events.WeaponExplodeEvent;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import com.shampaggon.crackshot.headshots.Aabb;
import com.shampaggon.crackshot.headshots.Vector3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fun.cactus.utils.firework.FireworkData;
import fun.cactus.utils.firework.FireworkParser;
import fun.cactus.utils.firework.FireworkService;
import fun.cactus.utils.particle.ParticleEffectData;
import fun.cactus.utils.particle.ParticleEffectExecutor;
import fun.cactus.utils.particle.ParticleEffectParser;
import fun.cactus.utils.potion.PotionActivation;
import fun.cactus.utils.potion.PotionEffectData;
import fun.cactus.utils.potion.PotionEffectParser;
import fun.cactus.utils.potion.PotionEffectService;
import fun.cactus.utils.region.CuboidRegion;
import fun.cactus.utils.region.RegionChecker;
import fun.cactus.utils.region.RegionParser;
import fun.cactus.utils.supereffective.SuperEffectiveData;
import fun.cactus.utils.supereffective.SuperEffectiveParser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Вспомогательный сервис CrackShot.
 * Здесь сосредоточены загрузка конфигов, создание предметов оружия,
 * визуальные эффекты, логика мин/ловушек и разные utility-операции.
 */
public class CSMinion {
    private static final char BLACK_LEFT_TRIANGLE = 9664;
    private static final char WHITE_RIGHT_TRIANGLE = 9655;
    private static final char INFINITY_SYMBOL = 215;
    private static final String WEAPONS_DIRECTORY = "weapons";
    private static final String[] DEFAULT_WEAPON_FILES = {
            "defaultWeapons.yml",
            "defaultExplosives.yml",
            "defaultAttachments.yml",
            "melee.yml",
            "arms.yml",
            "pistols.yml"
    };

    private final CSDirector plugin;
    public String heading = "§7░ §c[-§l¬§cº§lc§7§ls§7] §c- §7";

    public CSMinion(CSDirector plugin) {
        this.plugin = plugin;
    }

    // Удаляет зарегистрированные рецепты CrackShot перед перезагрузкой или выключением плагина.
    public void clearRecipes() {
        try {
            for (String parentNode : this.plugin.parentlist.values()) {
                if (!this.plugin.getBoolean(parentNode + ".Crafting.Enable")) {
                    continue;
                }

                ItemStack weapon = this.vendingMachine(parentNode);
                if (weapon != null) {
                    this.removeRecipe(weapon);
                }
            }
        } catch (UnsupportedOperationException ignored) {
        }
    }

    public void customRecipes() {
        for (String parentNode : this.plugin.parentlist.values()) {
            if (this.plugin.getBoolean(parentNode + ".Crafting.Enable")) {
                this.registerCustomRecipe(parentNode);
            }
        }
    }

    // Собирает ItemStack оружия из конфига, включая имя, CMD и отображаемый боезапас.
    public ItemStack vendingMachine(String parentNode) {
        String itemInfo = this.plugin.getString(parentNode + ".Item_Information.Item_Type");
        if (itemInfo == null) {
            this.log(" The weapon '" + parentNode + "' has no value provided for Item_Type!");
            return null;
        }

        ItemStack weapon = this.parseItemStack(itemInfo);
        if (weapon == null) {
            this.log(" The weapon '" + parentNode + "' has an invalid value for Item_Type!");
            return null;
        }

        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) {
            return weapon;
        }

        Integer customModelData = this.plugin.getCustomModelData(parentNode);
        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }
        meta.setDisplayName(this.buildWeaponDisplayName(parentNode));
        this.applyLore(meta, this.plugin.getString(parentNode + ".Item_Information.Item_Lore"));
        weapon.setItemMeta(meta);
        return weapon;
    }

    public String identifyWeapon(String weapon) {
        String closestParent = null;

        for (String parentNode : this.plugin.parentlist.values()) {
            if (weapon.equalsIgnoreCase(parentNode)) {
                return parentNode;
            }

            if (closestParent == null && parentNode.toUpperCase().startsWith(weapon.toUpperCase())) {
                closestParent = parentNode;
            }
        }

        return closestParent;
    }

    public void oneTime(Player player) {
        if (player.getItemInHand().getAmount() == 1) {
            player.getInventory().clear(player.getInventory().getHeldItemSlot());
        } else {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        }

        this.plugin.unscopePlayer(player);
        player.updateInventory();
    }

    public void getWeaponCommand(Player player, String weapon, boolean spawned, String amount, boolean given, boolean byAPI) {
        String parent_node = this.identifyWeapon(weapon);
        if (parent_node != null) {
            String attachType = this.plugin.getString(parent_node + ".Item_Information.Attachments.Type");
            if (attachType == null || !attachType.equalsIgnoreCase("accessory")) {
                this.getWeaponHelper(player, parent_node, spawned, amount, given, byAPI);
                return;
            }
        }

        player.sendMessage(this.heading + "No weapon matches '" + weapon + "'.");
    }

    public void getWeaponHelper(Player player, String parentNode, boolean spawned, String amount, boolean given, boolean byAPI) {
        // Вся выдача проходит через единый метод, чтобы одинаково обрабатывались права,
        // лимиты стака, сообщения и звуки для команды, API и внутренних вызовов.
        if (spawned && !player.hasPermission("crackshot.get." + parentNode) && !player.hasPermission("crackshot.get.all")) {
            player.sendMessage(this.heading + "You do not have permission to get this item.");
        } else {
            ItemStack sniperID = this.vendingMachine(parentNode);
            if (sniperID == null) {
                player.sendMessage(this.heading + "You have failed to provide a value for 'Item_Type'.");
            } else {
                int intAmount = 1;
                if (amount != null) {
                    try {
                        intAmount = Integer.valueOf(amount);
                    } catch (NumberFormatException var12) {
                    }
                }

                if (intAmount > 64) {
                    intAmount = 64;
                }

                if (intAmount < 1) {
                    player.sendMessage(this.heading + "'" + intAmount + "' is not a valid amount.");
                } else if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(this.heading + "Your inventory is full.");
                } else {
                    String multiplier = "";
                    if (intAmount > 1) {
                        multiplier = " ✕" + intAmount;
                    }

                    for (int count = 0; count < intAmount; ++count) {
                        player.getInventory().addItem(sniperID);
                    }

                    String publicName = parentNode;
                    if (parentNode.length() > 19) {
                        publicName = parentNode.substring(0, 19) + "...";
                    }

                    if (spawned) {
                        player.sendMessage(this.heading + "Successfully grabbed - " + publicName + multiplier);
                    } else if (given && !byAPI) {
                        String itemName = this.plugin.getString(parentNode + ".Item_Information.Item_Name");
                        CSMessages.sendMessage(player, this.heading, Message.WEAPON_RECEIVED.getMessage(itemName, String.valueOf('✕'), intAmount));
                    }

                    if (!byAPI) {
                        this.plugin.playSoundEffects(player, parentNode, ".Item_Information.Sounds_Acquired", false, null);
                    }

                }
            }
        }
    }

    public Vector getAlignedDirection(Location locA, Location locB) {
        return locB.toVector().subtract(locA.toVector()).normalize();
    }

    // Загружает general.yml и раскладывает его значения в быстрые кэши плагина.
    public void loadGeneralConfig() {
        File tag = this.ensureBundledConfig("general.yml", "General configuration added!");

        try {
            this.plugin.weaponConfig = YamlConfiguration.loadConfiguration(tag);
            if (this.plugin.weaponConfig.getList("Disabled_Worlds") != null) {
                this.plugin.disWorlds = this.plugin.weaponConfig.getList("Disabled_Worlds").toArray(new String[]{"0"});
            }

            ConfigurationSection invCtrl = this.plugin.weaponConfig.getConfigurationSection("Inventory_Control");
            if (invCtrl != null) {
                for (String group : invCtrl.getKeys(false)) {
                    CSDirector.ints.put(group + ".Limit", this.plugin.weaponConfig.getInt("Inventory_Control." + group + ".Limit"));
                    CSDirector.strings.put(group + ".Message_Exceeded", this.plugin.weaponConfig.getString("Inventory_Control." + group + ".Message_Exceeded").replace("&", "§"));
                    CSDirector.strings.put(group + ".Sounds_Exceeded", this.plugin.weaponConfig.getString("Inventory_Control." + group + ".Sounds_Exceeded"));
                }
            }

            CSDirector.bools.put("Merged_Reload.Disable", this.plugin.weaponConfig.getBoolean("Merged_Reload.Disable"));
            CSDirector.strings.put("Merged_Reload.Message_Denied", this.plugin.weaponConfig.getString("Merged_Reload.Message_Denied").replace("&", "§"));
            CSDirector.strings.put("Merged_Reload.Sounds_Denied", this.plugin.weaponConfig.getString("Merged_Reload.Sounds_Denied"));
        } catch (Exception exception) {
            this.log(tag.getName() + " could not be loaded.");
        }
    }

    public void loadMessagesConfig() {
        File tag = this.ensureBundledConfig("messages.yml", "Message configuration added!");

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(tag);
            for (String key : config.getKeys(true)) {
                CSMessages.messages.put(key, config.getString(key));
            }
        } catch (Exception exception) {
            this.log(tag.getName() + " could not be loaded.");
        }
    }

    public File getDefaultConfig(String fileName) {
        File file = new File(this.plugin.getDataFolder(), fileName);
        return this.copyBundledResource("/" + fileName, file) ? file : null;
    }

    // Загружает все оружейные yaml-файлы, а если папка пуста — ставит дефолтные наборы.
    public void loadWeapons(Player player) {
        File weaponsDirectory = this.ensureWeaponsDirectory();
        List<File> fileList = this.loadWeaponFiles(weaponsDirectory.toPath());
        if (fileList.isEmpty()) {
            // Если папка пуста, раскладываем стартовые наборы, чтобы сервер мог подняться без ручной подготовки файлов.
            this.installDefaultWeaponFiles();
            fileList = this.loadWeaponFiles(weaponsDirectory.toPath());
            this.log("Default weapons added!");
        }

        if (fileList.isEmpty()) {
            this.log("No weapons were loaded!");
            return;
        }

        for (File file : fileList) {
            this.plugin.weaponConfig = this.loadConfig(file, player);
            this.plugin.fillHashMaps(this.plugin.weaponConfig);
        }

        this.completeList();
    }

    public List<File> getAllYmlFiles(Path startPath) throws IOException {
        try (Stream<Path> stream = Files.walk(startPath)) {
            return stream
                    .filter(p -> p.toString().endsWith(".yml"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    public File grabDefaults(String defaultWeap) {
        File file = new File(this.ensureWeaponsDirectory(), defaultWeap);
        return this.copyBundledResource("/resources/" + defaultWeap, file) ? file : null;
    }

    public YamlConfiguration loadConfig(File file, Player player) {
        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException var5) {
        } catch (IOException ex) {
            if (player != null) {
                player.sendMessage(this.heading + "The file '" + file.getName() + "' could not be loaded.");
            }

            ex.printStackTrace();
        } catch (InvalidConfigurationException ex) {
            if (player != null) {
                player.getWorld().playSound(player.getLocation(), SoundManager.get("VILLAGER_HAGGLE"), 1.0F, 1.0F);
                player.sendMessage(this.heading + "The file '" + file.getName() + "' is incorrectly configured. View the error report in the console and fix it!");
            }

            ex.printStackTrace();
        }

        return config;
    }

    public void completeList() {
        // Формируем список оружия для /shot list только из доступных и не скрытых записей.
        int counter = 1;

        for (String parent_node : this.plugin.parentlist.values()) {
            String attachType = this.plugin.getString(parent_node + ".Item_Information.Attachments.Type");
            if (!this.plugin.getBoolean(parent_node + ".Item_Information.Hidden_From_List") && (attachType == null || !attachType.equalsIgnoreCase("accessory"))) {
                this.plugin.wlist.put(counter, parent_node);
                ++counter;
            }
        }

        CSDirector.ints.put("totalPages", (int) Math.ceil((double) (counter - 1) / (double) 18.0F));
    }

    private void removeRecipe(ItemStack weapon) {
        for (Recipe recipe : this.plugin.getServer().getRecipesFor(weapon)) {
            Iterator<Recipe> iterator = this.plugin.getServer().recipeIterator();
            while (iterator.hasNext()) {
                if (iterator.next().getResult().isSimilar(recipe.getResult())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private void registerCustomRecipe(String parentNode) {
        boolean shaped = this.plugin.getBoolean(parentNode + ".Crafting.Shaped");
        int quantity = this.plugin.getInt(parentNode + ".Crafting.Quantity");
        String ingredients = this.plugin.getString(parentNode + ".Crafting.Ingredients");
        if (ingredients == null) {
            this.log("The weapon '" + parentNode + "' does not have a value for Crafting.Ingredients.");
            return;
        }

        ItemStack weapon = this.vendingMachine(parentNode);
        if (weapon == null) {
            return;
        }

        if (quantity > 1) {
            weapon.setAmount(quantity);
        }

        String[] ingredientDefinitions = ingredients.split(",");
        if (shaped) {
            this.registerShapedRecipe(parentNode, weapon, ingredients, ingredientDefinitions);
            return;
        }

        this.registerShapelessRecipe(weapon, ingredientDefinitions);
    }

    private void registerShapedRecipe(String parentNode, ItemStack weapon, String ingredients, String[] ingredientDefinitions) {
        if (ingredientDefinitions.length != 9) {
            this.log("The crafting recipe (" + ingredients + ") of weapon '" + parentNode + "' has " + ingredientDefinitions.length + " value(s) instead of 9.");
            return;
        }

        ShapedRecipe recipe = new ShapedRecipe(weapon);
        recipe.shape("ABC", "DEF", "GHI");
        for (int index = 0; index < ingredientDefinitions.length; index++) {
            this.applyIngredient(recipe, (char) ('A' + index), ingredientDefinitions[index]);
        }

        this.plugin.getServer().addRecipe(recipe);
    }

    private void registerShapelessRecipe(ItemStack weapon, String[] ingredientDefinitions) {
        ShapelessRecipe recipe = new ShapelessRecipe(weapon);
        for (String ingredientDefinition : ingredientDefinitions) {
            ItemStack ingredient = this.parseItemStack(ingredientDefinition);
            if (ingredient != null && ingredient.getType() != Material.AIR) {
                recipe.addIngredient(1, ingredient.getType(), ingredient.getDurability());
            }
        }

        this.plugin.getServer().addRecipe(recipe);
    }

    private void applyIngredient(ShapedRecipe recipe, char slot, String ingredientDefinition) {
        ItemStack ingredient = this.parseItemStack(ingredientDefinition);
        if (ingredient != null && ingredient.getType() != Material.AIR) {
            recipe.setIngredient(slot, ingredient.getType(), ingredient.getDurability());
        }
    }

    // Вся логика отображения боезапаса собрана здесь, чтобы править формат имени можно было в одном месте.
    private String buildWeaponDisplayName(String parentNode) {
        String displayName = this.plugin.getString(parentNode + ".Item_Information.Item_Name");
        String firearmActionType = this.plugin.getString(parentNode + ".Firearm_Action.Type");
        boolean dualWield = this.plugin.getBoolean(parentNode + ".Shooting.Dual_Wield");
        boolean keepUnusedTag = !this.plugin.getBoolean(parentNode + ".Item_Information.Remove_Unused_Tag");
        boolean explosiveDeviceEnabled = this.plugin.getBoolean(parentNode + ".Explosive_Devices.Enable");
        String explosiveDeviceType = this.plugin.getString(parentNode + ".Explosive_Devices.Device_Type");
        String attachmentType = this.plugin.getString(parentNode + ".Item_Information.Attachments.Type");
        String attachmentInfo = this.plugin.getString(parentNode + ".Item_Information.Attachments.Info");
        Integer startAmount = this.getStartingReloadAmount(parentNode);
        boolean startGiven = this.hasConfiguredStartingAmount(parentNode, startAmount);
        int reloadAmount = this.resolveDisplayedReloadAmount(parentNode, firearmActionType, startAmount);

        displayName += this.buildWeaponAmmoTag(parentNode, reloadAmount, dualWield, keepUnusedTag, explosiveDeviceEnabled, explosiveDeviceType, firearmActionType, attachmentType, attachmentInfo);
        return this.applyFirearmActionMarker(displayName, firearmActionType, dualWield, startGiven, startAmount);
    }

    private String buildWeaponAmmoTag(String parentNode, int reloadAmount, boolean dualWield, boolean keepUnusedTag, boolean explosiveDeviceEnabled, String explosiveDeviceType, String firearmActionType, String attachmentType, String attachmentInfo) {
        if (this.plugin.getBoolean(parentNode + ".Reload.Enable") && !explosiveDeviceEnabled) {
            if (dualWield) {
                return this.buildDualAmmoTag(String.valueOf(reloadAmount), String.valueOf(reloadAmount));
            }

            if (this.isMainAttachment(attachmentType)) {
                return this.buildAttachmentAmmoTag(attachmentInfo, String.valueOf(reloadAmount));
            }

            return this.wrapAmmoTag(String.valueOf(reloadAmount));
        }

        if (this.isRemoteExplosive(explosiveDeviceType)) {
            return this.wrapAmmoTag(this.resolveExplosiveCapacity(parentNode, explosiveDeviceType));
        }

        if (this.isTrapExplosive(explosiveDeviceType)) {
            return this.wrapAmmoTag("?");
        }

        if (dualWield) {
            String infinity = String.valueOf(INFINITY_SYMBOL);
            return this.buildDualAmmoTag(infinity, infinity);
        }

        if (this.isMainAttachment(attachmentType)) {
            return this.buildAttachmentAmmoTag(attachmentInfo, String.valueOf(INFINITY_SYMBOL));
        }

        if (keepUnusedTag || firearmActionType != null) {
            return this.wrapAmmoTag(String.valueOf(INFINITY_SYMBOL));
        }

        return "";
    }

    private String buildAttachmentAmmoTag(String attachmentNode, String primaryAmmoDisplay) {
        boolean attachmentReloadEnabled = this.plugin.getBoolean(attachmentNode + ".Reload.Enable");
        String secondaryAmmoDisplay = attachmentReloadEnabled
                ? String.valueOf(this.resolveAttachmentReloadAmount(attachmentNode))
                : String.valueOf(INFINITY_SYMBOL);
        return this.wrapAmmoTag(primaryAmmoDisplay + " " + BLACK_LEFT_TRIANGLE + WHITE_RIGHT_TRIANGLE + " " + secondaryAmmoDisplay);
    }

    private String buildDualAmmoTag(String leftAmmoDisplay, String rightAmmoDisplay) {
        return this.wrapAmmoTag(leftAmmoDisplay + " | " + rightAmmoDisplay);
    }

    private String wrapAmmoTag(String ammoDisplay) {
        return " «" + ammoDisplay + "»";
    }

    private String resolveExplosiveCapacity(String parentNode, String explosiveDeviceType) {
        String capacity = "N/A";
        String deviceInfo = this.plugin.getString(parentNode + ".Explosive_Devices.Device_Info");
        String[] refinedOre = "itembomb".equalsIgnoreCase(explosiveDeviceType) && deviceInfo != null
                ? deviceInfo.split(",")
                : this.returnRefinedOre(null, parentNode);
        if (refinedOre != null) {
            capacity = refinedOre[0];
        }

        return capacity;
    }

    private int resolveDisplayedReloadAmount(String weaponNode, String firearmActionType, Integer startAmount) {
        // Для bolt/lever отображаемая ёмкость уменьшается на патрон,
        // который условно "уходит в патронник" до следующего цикла.
        int reloadAmount = this.plugin.getInt(weaponNode + ".Reload.Reload_Amount");
        if (this.hasConfiguredStartingAmount(weaponNode, startAmount)) {
            reloadAmount = Math.max(startAmount, 0);
        }

        if (this.usesReservedChamber(firearmActionType)) {
            reloadAmount = Math.max(reloadAmount - 1, 0);
        }

        return reloadAmount;
    }

    private int resolveAttachmentReloadAmount(String attachmentNode) {
        return this.resolveDisplayedReloadAmount(attachmentNode, this.plugin.getString(attachmentNode + ".Firearm_Action.Type"), this.getStartingReloadAmount(attachmentNode));
    }

    private Integer getStartingReloadAmount(String weaponNode) {
        return CSDirector.ints.get(weaponNode + ".Reload.Starting_Amount");
    }

    private boolean hasConfiguredStartingAmount(String weaponNode, Integer startAmount) {
        return startAmount != null && startAmount <= this.plugin.getInt(weaponNode + ".Reload.Reload_Amount");
    }

    private boolean usesReservedChamber(String firearmActionType) {
        return firearmActionType != null && ("bolt".equalsIgnoreCase(firearmActionType) || "lever".equalsIgnoreCase(firearmActionType));
    }

    private boolean isMainAttachment(String attachmentType) {
        return attachmentType != null && attachmentType.equalsIgnoreCase("main");
    }

    private boolean isRemoteExplosive(String explosiveDeviceType) {
        return explosiveDeviceType != null && (explosiveDeviceType.equalsIgnoreCase("remote") || explosiveDeviceType.equalsIgnoreCase("itembomb"));
    }

    private boolean isTrapExplosive(String explosiveDeviceType) {
        return explosiveDeviceType != null && explosiveDeviceType.equalsIgnoreCase("trap");
    }

    private String applyFirearmActionMarker(String displayName, String firearmActionType, boolean dualWield, boolean startGiven, Integer startAmount) {
        if (firearmActionType == null || dualWield) {
            return displayName;
        }

        if (this.isClosedAction(firearmActionType)) {
            return displayName.replace("«", startGiven && startAmount != null && startAmount < 1 ? "▫ «" : "▪ «");
        }

        if ("revolver".equalsIgnoreCase(firearmActionType) || "break".equalsIgnoreCase(firearmActionType)) {
            return displayName.replace("«", "▪ «");
        }

        return displayName;
    }

    private boolean isClosedAction(String firearmActionType) {
        return "bolt".equalsIgnoreCase(firearmActionType)
                || "lever".equalsIgnoreCase(firearmActionType)
                || "pump".equalsIgnoreCase(firearmActionType)
                || "slide".equalsIgnoreCase(firearmActionType);
    }

    private void applyLore(ItemMeta meta, String loreText) {
        if (loreText == null) {
            return;
        }

        List<String> lore = new ArrayList<>();
        Collections.addAll(lore, loreText.split("\\|"));
        meta.setLore(lore);
    }

    private File ensureBundledConfig(String fileName, String createdMessage) {
        File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists() && this.copyBundledResource("/" + fileName, file)) {
            this.log(createdMessage);
        }

        return file;
    }

    private File ensureWeaponsDirectory() {
        File directory = new File(this.plugin.getDataFolder(), WEAPONS_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory;
    }

    private List<File> loadWeaponFiles(Path weaponsPath) {
        try {
            return this.getAllYmlFiles(weaponsPath);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void installDefaultWeaponFiles() {
        for (String fileName : DEFAULT_WEAPON_FILES) {
            this.grabDefaults(fileName);
        }
    }

    private boolean copyBundledResource(String resourcePath, File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (InputStream inputStream = CSDirector.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return false;
            }

            try (FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) > 0) {
                    output.write(buffer, 0, read);
                }
            }

            return true;
        } catch (IOException exception) {
            return false;
        }
    }

//    public void listWeapons(Player sender, String[] args) {
//        int start = 1;
//        int page = 1;
//        int finalChapter = this.plugin.getInt("totalPages");
//        if (finalChapter == 0) {
//            finalChapter = 1;
//        }
//
//        if (args.length == 2 && !args[1].equalsIgnoreCase("all")) {
//            int pageNumber;
//            try {
//                pageNumber = Integer.valueOf(args[1]);
//            } catch (NumberFormatException var10) {
//                sender.sendMessage(this.heading + "You have provided an invalid page number.");
//                return;
//            }
//
//            if (pageNumber < 1) {
//                return;
//            }
//
//            start = 1 + (pageNumber - 1) * 18;
//            page = pageNumber;
//            if (start >= finalChapter * 18) {
//                start = 1 + (finalChapter - 1) * 18;
//            }
//
//            if (pageNumber < 1) {
//                page = 1;
//            } else if (pageNumber > finalChapter) {
//                page = finalChapter;
//            }
//        }
//
//        int finish = start + 18;
//        if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
//            finish = finalChapter * 18;
//            sender.sendMessage("§7░ §cWeapons [All pages]:");
//        } else {
//            sender.sendMessage("§7░ §cWeapons [Page " + page + "/" + finalChapter + "]:");
//        }
//
//        for(int i = start; i < finish; i += 2) {
//            String weapon = this.plugin.wlist.get(i);
//            if (weapon == null) {
//                break;
//            }
//
//            String weapon2 = this.plugin.wlist.get(i + 1);
//            sender.sendMessage(this.makePretty(weapon, weapon2));
//        }
//
//    }

    public void listWeapons(Player sender, String[] args) {
        // Команда строится страницами по 18 элементов, но поддерживает и вывод всех страниц сразу.
        int start = 1;
        int page = 1;
        int finalChapter = this.plugin.getInt("totalPages");
        if (finalChapter == 0) {
            finalChapter = 1;
        }

        if (args.length == 2 && !args[1].equalsIgnoreCase("all")) {
            int pageNumber;
            try {
                pageNumber = Integer.valueOf(args[1]);
            } catch (NumberFormatException var10) {
                sender.sendMessage(this.heading + "You have provided an invalid page number.");
                return;
            }

            if (pageNumber < 1) {
                return;
            }

            start = 1 + (pageNumber - 1) * 18;
            page = pageNumber;
            if (start >= finalChapter * 18) {
                start = 1 + (finalChapter - 1) * 18;
            }

            if (pageNumber < 1) {
                page = 1;
            } else if (pageNumber > finalChapter) {
                page = finalChapter;
            }
        }

        int finish = start + 18;
        if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
            finish = finalChapter * 18;
            sender.sendMessage("§7░ §cWeapons [All pages]:");
        } else {
            sender.sendMessage("§7░ §cWeapons [Page " + page + "/" + finalChapter + "]:");
        }

        for (int i = start; i < finish; i += 2) {
            if (i >= this.plugin.wlist.size()) break;
            String weapon = this.plugin.wlist.get(i);
            if (weapon == null) break;

            String weapon2 = null;
            if (i + 1 < this.plugin.wlist.size()) {
                weapon2 = this.plugin.wlist.get(i + 1);
            }

            // build display name exactly as makePretty does for the first weapon piece
            String dispWeapon = weapon.length() > 18 ? weapon.toUpperCase().substring(0, 18) + "..." : weapon.toUpperCase();

            // get full formatted line from makePretty
            String fullLine = this.makePretty(weapon, weapon2);

            // replace only first occurrence of dispWeapon (case-sensitive) with marker
            String marker = "<<<CLICKABLE_WEAPON>>>";
            int idx = fullLine.indexOf(dispWeapon);
            if (idx == -1) {
                // fallback: send fullLine as plain text if matching failed
                sender.sendMessage(fullLine);
                continue;
            }
            String before = fullLine.substring(0, idx);
            String after = fullLine.substring(idx + dispWeapon.length());

            // Build TextComponents preserving colors/formatting
            TextComponent compBefore = new TextComponent(before);
            TextComponent compWeapon = new TextComponent(dispWeapon);
            compWeapon.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shot get " + weapon));
            compWeapon.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Нажмите, чтобы получить " + dispWeapon).create()));
            TextComponent compAfter = new TextComponent(after);

            TextComponent line = new TextComponent();
            line.addExtra(compBefore);
            line.addExtra(compWeapon);
            line.addExtra(compAfter);

            sender.spigot().sendMessage(line);
        }
    }

    public String makePretty(String weapon, String weapon2) {
        // Формирует старый "двухколоночный" вывод списка оружия с ручным выравниванием по символам.
        weapon = weapon.length() > 18 ? weapon.toUpperCase().substring(0, 18) + "..." : weapon.toUpperCase();
        String tripleDot = weapon.replace("...", "O").replace("I", "");
        int officialLength = weapon.replace("...", "O").length();
        int count = officialLength - tripleDot.length();
        String padding = officialLength % 2 == 0 ? "||" : " |";
        int spaceLimit = 34 - (officialLength + 1) / 2;
        if (count != 0 && count % 2 != 0) {
            padding = officialLength % 2 != 0 ? " ||" : " |";
        }

        for (int a = officialLength + 1; a < spaceLimit + count / 2; ++a) {
            padding = " " + padding;
        }

        if (weapon2 != null) {
            if (weapon2.length() > 18) {
                weapon2 = weapon2.substring(0, 18) + "...";
            }

            weapon = "§7░ §c - §7" + weapon + padding + "§7░ §c - §7" + weapon2.toUpperCase();
        } else {
            weapon = "§7░ §c - §7" + weapon + padding + "§7░";
        }

        return weapon;
    }

    public void removeEnchantments(ItemStack item) {
        for (Enchantment e : item.getEnchantments().keySet()) {
            item.removeEnchantment(e);
        }

    }

    public String extractReading(String name) {
        if (!name.contains("«")) {
            return String.valueOf('×');
        } else {
            String[] nameDigger = name.split("«");
            return nameDigger[1].split("»")[0];
        }
    }

    public void replaceBrackets(ItemStack item, String gapFiller, String parent_node) {
        // Метод меняет только содержимое между «» и старается сохранить формат
        // для основного оружия и прикреплённого модуля в одном display name.
        String attachType = this.plugin.getAttachment(parent_node, item)[0];

        try {
            if (attachType != null) {
                String[] ammoReading = this.extractReading(item.getItemMeta().getDisplayName()).split(" ");
                if (attachType.equalsIgnoreCase("main")) {
                    gapFiller = gapFiller + " " + ammoReading[1] + " " + ammoReading[2];
                } else if (attachType.equalsIgnoreCase("accessory")) {
                    gapFiller = ammoReading[0] + " " + ammoReading[1] + " " + gapFiller;
                }
            }
        } catch (IndexOutOfBoundsException var6) {
            this.resetItemName(item, parent_node);
            return;
        }

        String refinedOre = item.getItemMeta().getDisplayName().replaceAll("(?<=«).*?(?=»)", gapFiller);
        this.setItemName(item, refinedOre);
    }

    public void resetItemName(ItemStack item, String parentNode) {
        ItemStack correctItem = this.vendingMachine(parentNode);
        this.setItemName(item, correctItem.getItemMeta().getDisplayName());
    }

    public void setItemName(ItemStack item, String name) {
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        item.setItemMeta(m);
    }

    public boolean isHesh(Projectile proj, LivingEntity victim) {
        double headHalfSize = 0.5F;
        Location victimEyeLoc = victim.getEyeLocation();
        Location projLoc = proj.getLocation();
        Vector projDir = proj.getVelocity();
        Aabb hitbox = new Aabb(new Vector3(victimEyeLoc.getX(), victimEyeLoc.getY(), victimEyeLoc.getZ()), new Vector3(headHalfSize, headHalfSize, headHalfSize));
        Vector3 point = new Vector3(projLoc.getX(), projLoc.getY(), projLoc.getZ());
        Vector3 dir = new Vector3(projDir.getX(), projDir.getY(), projDir.getZ());
        return Aabb.intersectsLine(point, dir, hitbox);
    }

    public boolean durabilityCheck(String item) {
        // Проверка legacy-id предметов с прочностью, где нельзя бездумно переиспользовать durability.
        String[] list = new String[]{"346", "398", "359"};

        for (int i = 256; i <= 259; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }

        for (int i = 267; i <= 279; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }

        for (int i = 283; i <= 286; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }

        for (int i = 290; i <= 294; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }

        for (int i = 298; i <= 317; ++i) {
            if (item.contains(String.valueOf(i))) {
                return true;
            }
        }

        for (String it : list) {
            if (item.contains(it)) {
                return true;
            }
        }

        return false;
    }

    public void projectileLightning(Location loc, boolean zapNoDam) {
        if (zapNoDam) {
            loc.getWorld().strikeLightningEffect(loc);
        } else {
            loc.getWorld().strikeLightning(loc);
        }

    }

    public void explosionPackage(LivingEntity victim, String parent_node, Player player) {
        // Единая точка пост-обработки взрыва: сообщения, огонь, спавн сущностей, звуки и эффекты.
        if (parent_node != null) {
            String vicName = victim.getType().getName();
            String shooterName = player == null ? "<shooter>" : player.getName();
            boolean spawnedEnts = this.plugin.spawnEntities(victim, parent_node, ".Spawn_Entity_On_Hit.EntityType_Baby_Explode_Amount", player);
            this.givePotionEffects(victim, parent_node, ".Explosions.Explosion_Potion_Effect", /*"explosion"*/PotionActivation.EXPLOSION);
            int inc = this.plugin.getInt(parent_node + ".Explosions.Ignite_Victims");
            if (inc != 0) {
                victim.setFireTicks(inc);
            }

            this.plugin.playSoundEffects(victim, parent_node, ".Explosions.Sounds_Victim", false, (Location) null, new String[0]);
            if (victim == player) {
                return;
            }

            if (victim instanceof Player) {
                if (spawnedEnts) {
                    this.plugin.sendPlayerMessage(victim, parent_node, ".Spawn_Entity_On_Hit.Message_Victim", shooterName, vicName, "<flight>", "<damage>");
                }

                vicName = victim.getName();
                this.plugin.sendPlayerMessage(victim, parent_node, ".Explosions.Message_Victim", shooterName, vicName, "<flight>", "<damage>");
            }

            if (player != null) {
                if (spawnedEnts) {
                    this.plugin.sendPlayerMessage(player, parent_node, ".Spawn_Entity_On_Hit.Message_Shooter", shooterName, vicName, "<flight>", "<damage>");
                }

                this.plugin.sendPlayerMessage(player, parent_node, ".Explosions.Message_Shooter", shooterName, vicName, "<flight>", "<damage>");
                this.plugin.playSoundEffects(player, parent_node, ".Explosions.Sounds_Shooter", false, (Location) null, new String[0]);
            }
        }

    }

    public void callAndResponse(final Player victim, final Player fisherman, final Vehicle vehicle, final String[] mineInfo, final boolean shot) {
        // Защищает от многократного срабатывания мины по одному и тому же игроку за короткий интервал.
        if (!victim.hasMetadata("CS_trigDelay")) {
            if (fisherman == null) {
                if (vehicle == null) {
                    this.detonateRDE(fisherman, victim, mineInfo, false);
                } else {
                    this.mineAction(vehicle, mineInfo, fisherman, shot, null, victim);
                }

            } else {
                victim.setMetadata("CS_trigDelay", new FixedMetadataValue(this.plugin, false));
                this.tempVars(victim, "CS_trigDelay", 200L);
                victim.setMetadata("CS_singed", new FixedMetadataValue(this.plugin, false));
                this.illegalSlap(fisherman, victim, 0);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                    if (victim.hasMetadata("CS_singed") && victim.getMetadata("CS_singed").get(0).asBoolean()) {
                        victim.removeMetadata("CS_singed", CSMinion.this.plugin);
                        victim.removeMetadata("CS_trigDelay", CSMinion.this.plugin);
                        if (vehicle == null) {
                            CSMinion.this.detonateRDE(fisherman, victim, mineInfo, false);
                        } else {
                            CSMinion.this.mineAction(vehicle, mineInfo, fisherman, shot, victim.getName(), victim);
                        }
                    }

                }, 1L);
            }
        }
    }

    public void reseatTag(Item item) {
        if (!(item.getVehicle() instanceof Entity)) {
            for (Entity veh : item.getNearbyEntities(1.0F, 10.0F, 1.0F)) {
                if (veh instanceof Minecart && !(veh.getPassenger() instanceof Entity)) {
                    veh.setPassenger(item);
                    break;
                }
            }

        }
    }

    public void reseatTag(Vehicle vehicle) {
        if (!(vehicle.getPassenger() instanceof Entity)) {
            for (Entity ent : vehicle.getNearbyEntities((double) 1.0F, (double) 10.0F, (double) 1.0F)) {
                if (ent instanceof Item && !(ent.getVehicle() instanceof Entity)) {
                    ItemStack itemFuse = ((Item) ent).getItemStack();
                    if (this.plugin.itemIsSafe(itemFuse) && itemFuse.getItemMeta().getDisplayName().startsWith("§cS3AGULLL~")) {
                        vehicle.setPassenger(ent);
                        break;
                    }
                }
            }

        }
    }

    public void mineAction(Vehicle vehicle, String[] mineInfo, Player fisherman, boolean shot, String vicName, Entity victim) {
        if (fisherman != null && vicName != null) {
            this.plugin.sendPlayerMessage(fisherman, mineInfo[2], ".Explosive_Devices.Message_Trigger_Placer", mineInfo[1], vicName, "<flight>", "<damage>");
            this.plugin.playSoundEffects(fisherman, mineInfo[2], ".Explosive_Devices.Sounds_Alert_Placer", false, (Location) null, new String[0]);
        }

        if (victim instanceof Player && !mineInfo[1].equals(((Player) victim).getName())) {
            this.plugin.sendPlayerMessage((Player) victim, mineInfo[2], ".Explosive_Devices.Message_Trigger_Victim", mineInfo[1], vicName, "<flight>", "<damage>");
        }

        this.plugin.projectileExplosion(vehicle, mineInfo[2], shot, fisherman, true, false, (Location) null, (Block) null, false, 0);
        if (!shot) {
            this.plugin.playSoundEffects(vehicle, mineInfo[2], ".Explosive_Devices.Sounds_Trigger", false, (Location) null, new String[0]);
        }

        vehicle.getPassenger().remove();
    }

    public void tempVars(final Player player, final String metaData, long delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> player.removeMetadata(metaData, CSMinion.this.plugin), delay);
    }

    public void illegalSlap(Player player, LivingEntity victim, int dmg) {
        PermissionAttachment attachment = player.addAttachment(this.plugin);
        attachment.setPermission("nocheatplus", true);
        attachment.setPermission("anticheat.check.exempt", true);
        victim.damage(dmg, player);
        player.removeAttachment(attachment);
    }

    public String[] fastenSeatbelts(Item psngr) {
        if (this.plugin.itemIsSafe(psngr.getItemStack())) {
            String itemName = psngr.getItemStack().getItemMeta().getDisplayName();
            if (itemName.contains("§cS3AGULLL")) {
                return itemName.split("~");
            }
        }

        return null;
    }

    public boolean bombIsInvalid(Player player, String[] deviceInfo, String parentNode) {
        // Валидация Device_Info вынесена отдельно, чтобы все ошибки формата ловить до спавна бомбы.
        boolean retVal = false;
        String debugMsg = this.heading + "The 'Device_Info' node of the weapon " + parentNode;
        String debugEnd = null;
        if (deviceInfo != null && deviceInfo.length == 4) {
            if (this.parseItemStack(deviceInfo[2]) == null) {
                debugEnd = " contains the value '" + deviceInfo[2] + "', which is not a valid item ID.";
                retVal = true;
            } else if (this.parseItemStack(deviceInfo[3]) == null) {
                debugEnd = " contains the value '" + deviceInfo[3] + "', which is not a valid item ID.";
                retVal = true;
            } else {
                try {
                    if (Integer.valueOf(deviceInfo[0]) <= 0) {
                        debugEnd = " contains the value '" + deviceInfo[0] + "', which is not a number greater than 0.";
                        retVal = true;
                    }

                    Double.valueOf(deviceInfo[1]);
                } catch (NumberFormatException var8) {
                    debugEnd = " contains an invalid number.";
                    retVal = true;
                }
            }
        } else {
            debugEnd = " is incorrectly formatted.";
            retVal = true;
        }

        if (retVal) {
            player.sendMessage(debugMsg + debugEnd);
        }

        return retVal;
    }

    public String[] returnRefinedOre(Player player, String parent_node) {
        String rdeOre = this.plugin.getString(parent_node + ".Explosive_Devices.Device_Info");
        boolean playerExists = player != null;
        String msgToSend = null;
        if (rdeOre != null) {
            String[] rdeRefined = rdeOre.split("-");
            if (rdeRefined.length == 3) {
                try {
                    if (Integer.parseInt(rdeRefined[0]) < 1) {
                        msgToSend = "'" + rdeRefined[0] + "' in '" + rdeOre + "' of weapon '" + parent_node + "' must be a positive number.";
                    } else {
                        if (rdeRefined[1].length() == 2) {
                            return rdeRefined;
                        }

                        msgToSend = "'" + rdeRefined[1] + "' in '" + rdeOre + "' of weapon '" + parent_node + "' must be 2 characters long, not " + rdeRefined[1].length() + ".";
                    }
                } catch (NumberFormatException var8) {
                    msgToSend = "'" + rdeRefined[0] + "' in '" + rdeOre + "' of weapon '" + parent_node + "' is not a valid number.";
                }
            } else {
                msgToSend = "'" + rdeOre + "' of weapon '" + parent_node + "' has an incorrect format! The correct format is: Amount-UniqueID-Headname!";
            }
        }

        if (playerExists && msgToSend != null) {
            player.sendMessage(this.heading + msgToSend);
        }

        return null;
    }

    public void removeNamedItem(Player player, String itemInfo, int totalAmt, String weaponTitle, boolean shop) {
        // Удаляет ammo/item stacks по типу, durability и при необходимости по проверке имени.
        int removed = 0;
        ItemStack item = this.parseItemStack(itemInfo);
        if (item != null) {
            ItemStack[] inv = player.getInventory().getContents();
            String ammoName = this.plugin.getString(weaponTitle + ".Ammo.Ammo_Name_Check");
            boolean checkName = ammoName != null;

            for (int i = 0; removed <= totalAmt && i < inv.length; ++i) {
                if (inv[i] != null && inv[i].getType() == item.getType() && inv[i].getDurability() == item.getDurability() && (!checkName || this.plugin.itemIsSafe(inv[i]) && inv[i].getItemMeta().getDisplayName().contains(ammoName))) {
                    if (inv[i].getAmount() > totalAmt - removed) {
                        inv[i].setAmount(inv[i].getAmount() - (totalAmt - removed));
                        removed = totalAmt;
                    } else {
                        removed += inv[i].getAmount();
                        inv[i] = null;
                    }
                }
            }

            player.getInventory().setContents(inv);
            player.updateInventory();
            if (!this.containsItemStack(player, itemInfo, 1, weaponTitle) && !shop) {
                this.plugin.playSoundEffects(player, weaponTitle, ".Ammo.Sounds_Out_Of_Ammo", false, (Location) null, new String[0]);
            }

        }
    }

    public int countItemStacks(Player player, String itemInfo, String weaponTitle) {
        int count = 0;
        ItemStack item = this.parseItemStack(itemInfo);
        if (item == null) {
            count = 0;
        } else {
            String ammoName = this.plugin.getString(weaponTitle + ".Ammo.Ammo_Name_Check");
            boolean checkName = ammoName != null;

            for (ItemStack itemSlot : player.getInventory().getContents()) {
                if (itemSlot != null && itemSlot.getType() == item.getType() && itemSlot.getDurability() == item.getDurability() && (!checkName || this.plugin.itemIsSafe(itemSlot) && itemSlot.getItemMeta().getDisplayName().contains(ammoName))) {
                    count += itemSlot.getAmount();
                }
            }
        }

        return count;
    }

    public boolean containsItemStack(Player player, String itemInfo, int minAmount, String weaponTitle) {
        ItemStack item = this.parseItemStack(itemInfo);
        return item != null && this.countItemStacks(player, itemInfo, weaponTitle) >= minAmount;
    }

    /* public double getSuperDamage(EntityType victimType, String parent_node, double totalDmg) {
         String superEffect = this.plugin.getString(parent_node + ".Abilities.Super_Effective");
         if (superEffect != null) {
             String[] mobList = superEffect.split(",");

             for (String mob : mobList) {
                 mob = mob.replace(" ", "");
                 String[] args = mob.split("-");

                 try {
                     if (args.length == 2 && victimType == EntityType.valueOf(args[0])) {
                         totalDmg = (double) Math.round(totalDmg * Double.valueOf(args[1]));
                     }
                 } catch (IllegalArgumentException var13) {
                     this.plugin.printM("The value provided for the Super_Effective node of the weapon '" + parent_node + "' is incorrect.");
                 }
             }
         }

         return totalDmg;
     }*/
    public double getSuperDamage(EntityType victimType, String parentNode, double totalDamage) {

        String configValue = plugin.getString(parentNode + ".Abilities.Super_Effective");

        if (configValue == null) return totalDamage;


        for (String rawEffect : configValue.split(",")) {

            try {

                SuperEffectiveData effect = SuperEffectiveParser.parse(rawEffect);

                if (effect.getEntityType() != victimType) continue;

                return Math.round(totalDamage * effect.getMultiplier());

            } catch (Exception ex) {

                plugin.printM("The value provided for the " + "Super_Effective node of weapon '" + parentNode + "' is incorrect.");
            }
        }

        return totalDamage;
    }

    public int getInstantKillChance(EntityType victimType, String parent_node) {
        String instantKill = this.plugin.getString(parent_node + ".Abilities.Instant_Kill");
        if (instantKill == null) {
            return 0;
        }

        String[] victimList = instantKill.split(",");

        for (String victimInfo : victimList) {
            victimInfo = victimInfo.replace(" ", "");
            String[] args = victimInfo.split("-");

            try {
                if (args.length == 2 && ("ALL".equalsIgnoreCase(args[0]) || victimType == EntityType.valueOf(args[0]))) {
                    int chance = Integer.parseInt(args[1]);
                    if (chance < 0) {
                        return 0;
                    }
                    return Math.min(chance, 100);
                }
            } catch (IllegalArgumentException exception) {
                this.plugin.printM("The value provided for the Instant_Kill node of the weapon '" + parent_node + "' is incorrect.");
            }
        }

        return 0;
    }

    public boolean shouldInstantKill(EntityType victimType, String parent_node) {
        int chance = this.getInstantKillChance(victimType, parent_node);
        return chance > 0 && new Random().nextInt(100) < chance;
    }

 /*   public void displayFireworks(Entity entity, String parentNode, String child_node) {
        // Firework-эффекты читаются из строки конфига, поэтому здесь много валидации формата.
        if (this.plugin.getBoolean(parentNode + ".Fireworks.Enable") && this.plugin.getString(parentNode + child_node) != null) {
            String[] fwList = this.plugin.getString(parentNode + child_node).split(",");

            for (String fwInfo : fwList) {
                fwInfo = fwInfo.replace(" ", "");
                String[] args = fwInfo.split("-");
                if (args.length == 6) {
                    try {
                        Firework fireWork;
                        if (entity instanceof LivingEntity) {
                            fireWork = entity.getWorld().spawn(((LivingEntity) entity).getEyeLocation(), Firework.class);
                        } else {
                            fireWork = entity.getWorld().spawn(entity.getLocation(), Firework.class);
                        }

                        FireworkMeta fireWorkMeta = fireWork.getFireworkMeta();
                        FireworkEffect effect = FireworkEffect.builder().trail(Boolean.parseBoolean(args[1])).flicker(Boolean.parseBoolean(args[2])).withColor(Color.fromRGB(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]))).with(Type.valueOf(args[0].toUpperCase())).build();
                        fireWorkMeta.addEffects(effect);
                        fireWork.setFireworkMeta(fireWorkMeta);
                    } catch (IllegalArgumentException var13) {
                        this.log(fwInfo + "' of weapon '" + parentNode + "' has an incorrect value for firework type, flicker, trail, or colour!");
                    }
                } else {
                    this.log(fwInfo + "' of weapon '" + parentNode + "' has an invalid format! The correct format is: Type-Trail-Flicker-Red-Blue-Green!");
                }
            }

        }
    }*/

    public void displayFireworks(
            Entity entity,
            String parentNode,
            String childNode
    ) {

        if (!plugin.getBoolean(
                parentNode +
                        ".Fireworks.Enable"
        )) {
            return;
        }

        String fireworkString =
                plugin.getString(
                        parentNode +
                                childNode
                );

        if (fireworkString == null) {
            return;
        }

        for (String rawFirework :
                fireworkString.split(",")) {

            try {

                FireworkData firework =
                        FireworkParser.parse(
                                rawFirework
                        );

                FireworkService.spawn(
                        entity,
                        firework
                );

            } catch (Exception ex) {

                log(
                        "'" +
                                rawFirework +
                                "' of weapon '" +
                                parentNode +
                                "' has an invalid firework format!"
                );
            }
        }
    }

    /*  public void givePotionEffects(LivingEntity player, String parentNode, String childNode, String event) {
          if (!event.equals("explosion")) {
              String eventInfo = this.plugin.getString(parentNode + ".Potion_Effects.Activation");
              if (eventInfo == null || !eventInfo.toLowerCase().contains(event)) {
                  return;
              }
          }

          if (this.plugin.getString(parentNode + childNode) != null) {
              String[] effectList = this.plugin.getString(parentNode + childNode).split(",");

              for (String potFX : effectList) {
                  potFX = potFX.replace(" ", "");
                  String[] args = potFX.split("-");
                  if (args.length == 3) {
                      try {
                          PotionEffectType potionType = PotionEffectType.getByName(args[0].toUpperCase());
                          int duration = Integer.parseInt(args[1]);
                          if (potionType.getDurationModifier() != (double) 1.0F) {
                              double maths = (double) duration * ((double) 1.0F / potionType.getDurationModifier());
                              duration = (int) maths;
                          }

                          player.removePotionEffect(potionType);
                          player.addPotionEffect(potionType.createEffect(duration, Integer.parseInt(args[2]) - 1));
                      } catch (Exception e) {
                          this.log(potFX + "' of weapon '" + parentNode + "' has an incorrect potion type, duration or level!");
                      }
                  } else {
                      this.log(potFX + "' of weapon '" + parentNode + "' has an invalid format! The correct format is: Potion-Duration-Level!");
                  }
              }

          }
      }*/
    public void givePotionEffects(
            LivingEntity entity,
            String parentNode,
            String childNode,
//            String event
            PotionActivation activation
    ) {
        if (!isActivated(parentNode, activation)) {
            return;
        }

//        if (!"explosion".equals(event)) {
//
//            String activation =
//                    plugin.getString(
//                            parentNode +
//                                    ".Potion_Effects.Activation"
//                    );
//
//            if (activation == null
//                    || !activation.toLowerCase()
//                    .contains(event.toLowerCase())) {
//
//                return;
//            }
//        }

        String potionString =
                plugin.getString(
                        parentNode + childNode
                );

        if (potionString == null) {
            return;
        }

        for (String rawPotion :
                potionString.split(",")) {

            try {

                PotionEffectData effect =
                        PotionEffectParser.parse(
                                rawPotion
                        );

                PotionEffectService.apply(
                        entity,
                        effect
                );

            } catch (Exception ex) {

                log(
                        "'" + rawPotion +
                                "' of weapon '" +
                                parentNode +
                                "' has an invalid format!"
                );
            }
        }
    }

    private boolean isActivated(
            String parentNode,
            PotionActivation activation
    ) {

        if (activation == PotionActivation.EXPLOSION) {
            return true;
        }

        String configValue =
                plugin.getString(
                        parentNode +
                                ".Potion_Effects.Activation"
                );

        if (configValue == null) {
            return false;
        }

        return Arrays.stream(configValue.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(
                        value -> value.equals(
                                activation.name()
                        )
                );
    }

    /*  public void giveParticleEffects(Entity player, String parentNode, String childNode, boolean muzzleFlash, Location givenCoord) {
          // Часть эффектов завязана на старый Bukkit Effect API, поэтому поддерживаются только legacy-форматы.
          if ((this.plugin.getBoolean(parentNode + ".Particles.Enable") || givenCoord != null) && this.plugin.getString(parentNode + childNode) != null) {
              Location loc = player != null ? player.getLocation() : givenCoord;
              World world = loc.getWorld();
              if (muzzleFlash) {
                  Location eyeLoc = ((LivingEntity) player).getEyeLocation();
                  loc = eyeLoc.toVector().add(eyeLoc.getDirection().multiply((double) 1.5F)).toLocation(world);
              }

              String[] partList = this.plugin.getString(parentNode + childNode).split(",");

              for (String partFX : partList) {
                  partFX = partFX.replace(" ", "");
                  String[] args = partFX.split("-");
                  if (args.length == 1) {
                      if (args[0].equalsIgnoreCase("smoke")) {
                          for (int i = 0; i < 8; ++i) {
                              world.playEffect(loc, Effect.SMOKE, i);
                          }
                      } else if (args[0].equalsIgnoreCase("lightning")) {
                          world.strikeLightningEffect(loc);
                      } else if (args[0].equalsIgnoreCase("explosion")) {
                          world.createExplosion(loc, 0.0F);
                      }
                  } else if (args.length == 2) {
                      try {
                          if (args[0].equalsIgnoreCase("potion_splash")) {
                              world.playEffect(loc, Effect.POTION_BREAK, Integer.parseInt(args[1]));
                          } else if (args[0].equalsIgnoreCase("block_break")) {
                              int blockID = Integer.parseInt(args[1]);
                              if (blockID < 256) {
                                  world.playEffect(loc, Effect.STEP_SOUND, blockID);
                              } else {
                                  this.plugin.printM("'" + partFX + "' was provided as a particle effect for the weapon '" + parentNode + "'. It contains '" + blockID + "', which is not a valid block ID.");
                              }
                          } else if (args[0].equalsIgnoreCase("flames")) {
                              world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, Integer.parseInt(args[1]));
                          }
                      } catch (NumberFormatException var15) {
                          this.plugin.printM("'" + partFX + "' was provided as a particle effect for the weapon '" + parentNode + "'. It contains '" + args[1] + "', which is not a valid number.");
                      }
                  }
              }

          }
      }
  */
    public void giveParticleEffects(
            Entity entity,
            String parentNode,
            String childNode,
            boolean muzzleFlash,
            Location givenCoord
    ) {

        if (!plugin.getBoolean(parentNode + ".Particles.Enable")
                && givenCoord == null) {
            return;
        }

        String effects =
                plugin.getString(parentNode + childNode);

        if (effects == null) {
            return;
        }

        Location location =
                resolveLocation(entity, givenCoord, muzzleFlash);

        World world = location.getWorld();

        for (String rawEffect : effects.split(",")) {

            try {

                ParticleEffectData effect =
                        ParticleEffectParser.parse(rawEffect);

                ParticleEffectExecutor.play(
                        world,
                        location,
                        effect,
                        parentNode
                );

            } catch (Exception ex) {

                plugin.printM(
                        "Invalid particle effect '" +
                                rawEffect +
                                "' for weapon '" +
                                parentNode +
                                "'"
                );
            }
        }
    }

    private Location resolveLocation(
            Entity entity,
            Location givenCoord,
            boolean muzzleFlash
    ) {

        if (givenCoord != null) {
            return givenCoord;
        }

        Location location = entity.getLocation();

        if (!muzzleFlash) {
            return location;
        }

        Location eyeLocation =
                ((LivingEntity) entity).getEyeLocation();

        return eyeLocation.toVector()
                .add(
                        eyeLocation.getDirection()
                                .multiply(1.5D)
                )
                .toLocation(eyeLocation.getWorld());
    }

    public boolean isInsideCuboid(Location locPoint, Location loc1, Location loc2, World world) {
        double[] dim = new double[2];
        if (!locPoint.getWorld().equals(world)) {
            return false;
        } else {
            dim[0] = loc1.getX();
            dim[1] = loc2.getX();
            Arrays.sort(dim);
            if (!(locPoint.getX() > dim[1]) && !(locPoint.getX() < dim[0])) {
                dim[0] = loc1.getY();
                dim[1] = loc2.getY();
                Arrays.sort(dim);
                if (!(locPoint.getY() > dim[1]) && !(locPoint.getY() < dim[0])) {
                    dim[0] = loc1.getZ();
                    dim[1] = loc2.getZ();
                    Arrays.sort(dim);
                    return !(locPoint.getZ() > dim[1]) && !(locPoint.getZ() < dim[0]);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public boolean regionCheck(Entity player, String parentNode) {
        // Региональная проверка работает по кубоидам из конфига без сторонних зависимостей типа WorldGuard.
       /* if (!this.plugin.getBoolean(parent_node + ".Region_Check.Enable")) {
            return true;
        } else {
            String region_info = this.plugin.getString(parent_node + ".Region_Check.World_And_Coordinates");
            String[] regions = region_info.split("\\|");
            boolean retVal = false;
            boolean relevance = false;

            for(String region : regions) {
                region = region.replace(" ", "");
                String[] args = region.split(",");
                if (args != null && (args.length == 7 || args.length == 8)) {
                    boolean blackList = args.length == 8 && Boolean.parseBoolean(args[7]);

                    try {
                        World regionWorld = Bukkit.getWorld(args[0]);
                        Location locPoint = player.getLocation();
                        Location locOne = new Location(regionWorld, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.valueOf(args[3]));
                        Location locTwo = new Location(regionWorld, Double.parseDouble(args[4]), Double.parseDouble(args[5]), Double.valueOf(args[6]));
                        if (player.getWorld().equals(regionWorld)) {
                            relevance = true;
                            if (this.isInsideCuboid(locPoint, locOne, locTwo, regionWorld)) {
                                if (blackList) {
                                    return false;
                                }

                                retVal = true;
                            } else if (blackList) {
                                retVal = true;
                            }
                        }
                    } catch (NumberFormatException var17) {
                        if (player instanceof Player) {
                            player.sendMessage(this.heading + "The value provided for the 'World_And_Coordinates' node of the weapon '" + parent_node + "' is incorrect. Double check the coordinates.");
                        }
                    }
                } else if (player instanceof Player) {
                    player.sendMessage(this.heading + "The 'World_And_Coordinates' node of the weapon '" + parent_node + "' has an incorrect number of arguments.");
                }
            }

            return relevance ? retVal : true;
        }*/
        if (!plugin.getBoolean(parentNode + ".Region_Check.Enable")) {
            return true;
        }

        String regionInfo =
                plugin.getString(parentNode + ".Region_Check.World_And_Coordinates");

        List<CuboidRegion> regions =
                RegionParser.parse(regionInfo);

        return new RegionChecker().check(player, regions);
    }

    public void weaponInteraction(Player shooter, String parent_node, boolean leftClick) {
        // Перед фактическим выстрелом проверяем тип снаряда, воду и вызываем prepare-событие для внешних плагинов.
        String projType = this.plugin.getString(parent_node + ".Shooting.Projectile_Type");
        boolean underwater = this.plugin.getBoolean(parent_node + ".Extras.Disable_Underwater");
        String[] validTypes = new String[]{"arrow", "snowball", "egg", "grenade", "flare", "fireball", "witherskull", "energy", "splash"};
        if (underwater) {
            Location loc = shooter.getEyeLocation();
            if (loc.getBlock().getType().toString().toUpperCase().endsWith("WATER")) {
                return;
            }
        }

        if (projType != null) {
            for (String type : validTypes) {
                if (projType.equalsIgnoreCase(type)) {
                    WeaponPrepareShootEvent prepareEvent = new WeaponPrepareShootEvent(shooter, parent_node);
                    this.plugin.getServer().getPluginManager().callEvent(prepareEvent);
                    if (!prepareEvent.isCancelled()) {
                        this.plugin.fireProjectile(shooter, parent_node, leftClick);
                    }

                    return;
                }
            }

            shooter.sendMessage(this.heading + "'" + projType + "' is not a valid type of projectile!");
        }

    }

    public void callAirstrike(final Entity mark, final String parent_node, final Player player) {
        // Airstrike раскладывает сетку падающих блоков с вариацией по координатам и несколькими волнами.
        final int height = this.plugin.getInt(parent_node + ".Airstrikes.Height_Dropped");
        final int area = this.plugin.getInt(parent_node + ".Airstrikes.Area");
        final int spacing = this.plugin.getInt(parent_node + ".Airstrikes.Distance_Between_Bombs");
        int strikeNo = this.plugin.getInt(parent_node + ".Airstrikes.Multiple_Strikes.Number_Of_Strikes");
        int strikeDelay = this.plugin.getInt(parent_node + ".Airstrikes.Multiple_Strikes.Delay_Between_Strikes");
        boolean multiStrike = this.plugin.getBoolean(parent_node + ".Airstrikes.Multiple_Strikes.Enable");
        final double coordinator = (double) (area - 1) * ((double) spacing / (double) 2.0F);
        final Location loc = mark.getLocation();
        final int y = loc.getBlockY();
        if (!multiStrike) {
            strikeNo = 1;
            strikeDelay = 1;
        }

        final Random r = new Random();
        final int vVar = this.plugin.getInt(parent_node + ".Airstrikes.Vertical_Variation");
        final int hVar = this.plugin.getInt(parent_node + ".Airstrikes.Horizontal_Variation");
        String block = this.plugin.getString(parent_node + ".Airstrikes.Block_Type");
        if (block != null) {
            String[] blockInfo = block.split("~");
            if (blockInfo.length < 2) {
                blockInfo = new String[]{blockInfo[0], "0"};
            }

            try {
                final Material blockMat = MaterialManager.getMaterial(block);
                final Byte secondaryData = Byte.valueOf(blockInfo[1]);
                this.plugin.sendPlayerMessage(player, parent_node, ".Airstrikes.Message_Call_Airstrike", player.getName(), "<victim>", "<flight>", "<damage>");
                this.giveParticleEffects(null, parent_node, ".Airstrikes.Particle_Call_Airstrike", false, loc);
                WeaponExplodeEvent explodeEvent = new WeaponExplodeEvent(player, loc, parent_node, false, true);
                this.plugin.getServer().getPluginManager().callEvent(explodeEvent);

                for (int delay = 0; delay < strikeDelay * strikeNo; delay += strikeDelay) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            CSMinion.this.plugin.playSoundEffects(mark, parent_node, ".Airstrikes.Sounds_Airstrike", false, (Location) null, new String[0]);

                            for (int iOne = 0; iOne < area; ++iOne) {
                                double x = (double) (loc.getBlockX() + iOne * spacing) - coordinator;

                                for (int iTwo = 0; iTwo < area; ++iTwo) {
                                    double z = (double) (loc.getBlockZ() + iTwo * spacing) - coordinator;
                                    int hD = y + height;
                                    if (vVar != 0) {
                                        hD += r.nextInt(vVar);
                                    }

                                    if (hVar != 0) {
                                        x += (r.nextInt(hVar) - r.nextInt(hVar));
                                        z += (r.nextInt(hVar) - r.nextInt(hVar));
                                    }

                                    FallingBlock bomb = loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x, (double) hD, z), blockMat, secondaryData);
                                    bomb.setDropItem(false);
                                    bomb.setMetadata("CS_strike", new FixedMetadataValue(CSMinion.this.plugin, parent_node + "~" + player.getName()));
                                }
                            }

                        }
                    }, delay);
                    if (!multiStrike) {
                        break;
                    }
                }

            } catch (IllegalArgumentException var23) {
                player.sendMessage(this.heading + "'" + block + "' in the 'Airstrikes' module of weapon '" + parent_node + "' is not a valid block-type.");
            }
        }
    }

    public void detonateRDE(Player player, Player victim, String[] itemInfo, boolean clacker) {
        // Для C4 и прочих RDE восстанавливаем parent_node по сохранённому uniqueID на skull-блоке.
        World world = Bukkit.getServer().getWorld(itemInfo[1]);
        Location loc = new Location(world, (double) Integer.valueOf(itemInfo[2]) + (double) 0.5F, (double) Integer.valueOf(itemInfo[3]) + (double) 0.5F, (double) Integer.valueOf(itemInfo[4]) + (double) 0.5F);
        Block c4 = world.getBlockAt(loc);
        if (MaterialManager.isSkullBlock(c4) && c4.getState() instanceof Skull) {
            String uniqueID = null;
            String storedPlayerName = clacker ? player.getName() : "Anonymous";

            Skull c4Block;
            try {
                c4Block = (Skull) c4.getState();
            } catch (ClassCastException var16) {
                return;
            }

            boolean hasOwner = c4Block.hasOwner();
            if (clacker) {
                String playerName = player.getName();
                Map<String, String> placedHeads = this.plugin.c4_backup.get(playerName);
                if (placedHeads != null) {
                    String key = c4.getWorld().getName() + "," + c4.getX() + "," + c4.getY() + "," + c4.getZ();
                    if (placedHeads.containsKey(key)) {
                        uniqueID = placedHeads.get(key);
                        placedHeads.remove(key);
                    }
                }
            }

            if (hasOwner || uniqueID != null) {
                if (hasOwner) {
                    String grabInfo = c4Block.getOwner();
                    String[] blockInfo = grabInfo.split("،");
                    if (blockInfo.length < 1) {
                        return;
                    }

                    uniqueID = blockInfo[0];
                    storedPlayerName = blockInfo[1];
                }

                for (String ids : this.plugin.rdelist.keySet()) {
                    if (ids.equalsIgnoreCase(uniqueID)) {
                        String parent_node = this.plugin.rdelist.get(ids);
                        String[] refinedOre = this.returnRefinedOre(player, parent_node);
                        if (refinedOre != null) {
                            c4Block.setOwner(refinedOre[2]);
                            c4Block.update(false);
                        }

                        if (!clacker) {
                            if (player != null) {
                                this.plugin.sendPlayerMessage(player, parent_node, ".Explosive_Devices.Message_Trigger_Placer", storedPlayerName.replace(String.valueOf('ظ'), "..."), victim.getName(), "<flight>", "<damage>");
                                this.plugin.playSoundEffects(player, parent_node, ".Explosive_Devices.Sounds_Alert_Placer", false, (Location) null, new String[0]);
                            }

                            this.plugin.sendPlayerMessage(victim, parent_node, ".Explosive_Devices.Message_Trigger_Victim", storedPlayerName.replace(String.valueOf('ظ'), "..."), victim.getName(), "<flight>", "<damage>");
                        }

                        c4Block.setMetadata("CS_transformers", new FixedMetadataValue(this.plugin, true));
                        this.plugin.playSoundEffects(null, parent_node, ".Explosive_Devices.Sounds_Trigger", false, loc, new String[0]);
                        this.plugin.projectileExplosion(null, parent_node, false, player, false, true, loc, c4, false, 0);
                        break;
                    }
                }
            }
        }

    }

    public boolean boobyAction(Block block, Entity victim, ItemStack item) {
        // Ловушка на ItemFrame/pressure plate определяет владельца по строке в display name предмета.
        if (item != null && this.plugin.itemIsSafe(item)) {
            String itemName = item.getItemMeta().getDisplayName();
            String actualName = this.plugin.getPureName(itemName);
            String parent_node = this.plugin.boobs.get(actualName);
            if (parent_node == null) {
                return false;
            } else {
                String vicName = "Santa Claus";
                if (victim != null) {
                    vicName = victim instanceof Player ? victim.getName() : victim.getType().getName();
                }

                if (!this.getBoobean(3, parent_node)) {
                    return false;
                } else {
                    String detectedName = this.extractReading(item.getItemMeta().getDisplayName());
                    if (detectedName.equals("?")) {
                        return false;
                    } else {
                        Player planter = Bukkit.getServer().getPlayer(detectedName);
                        if (victim != null) {
                            if (planter != null) {
                                if (planter == victim) {
                                    return false;
                                }

                                this.plugin.sendPlayerMessage(planter, parent_node, ".Explosive_Devices.Message_Trigger_Placer", detectedName, vicName, "<flight>", "<damage>");
                                this.plugin.playSoundEffects(planter, parent_node, ".Explosive_Devices.Sounds_Alert_Placer", false, (Location) null, new String[0]);
                            }

                            if (victim instanceof Player) {
                                this.plugin.sendPlayerMessage((LivingEntity) victim, parent_node, ".Explosive_Devices.Message_Trigger_Victim", detectedName, vicName, "<flight>", "<damage>");
                            }
                        }

                        this.plugin.playSoundEffects(null, parent_node, ".Explosive_Devices.Sounds_Trigger", false, block.getLocation().add((double) 0.5F, (double) 0.5F, (double) 0.5F), new String[0]);
                        this.plugin.projectileExplosion(null, parent_node, false, planter, false, true, (Location) null, block, true, 0);
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
    }

    public boolean getBoobean(int entry, String parent_node) {
        String ore = this.plugin.getString(parent_node + ".Explosive_Devices.Device_Info");
        if (ore == null) {
            return false;
        } else {
            String[] refinedOre = ore.split("-");
            return refinedOre.length != 5 ? false : Boolean.parseBoolean(refinedOre[entry - 1]);
        }
    }

    //    public ItemStack parseItemStack(String ore) {
//        ItemStack item = null;
//        if (ore != null) {
//            String[] refinedOre = ore.split("~");
//            if (refinedOre.length == 1) {
//                refinedOre = new String[]{refinedOre[0], "0"};
//            }
//
//            try {
//                item = new ItemStack(MaterialManager.getMaterial(ore), 1, Short.parseShort(refinedOre[1]));
//            } catch (Exception var5) {
//            }
//        }
//
//        return item;
//    }
    public ItemStack parseItemStack(String ore) {
        if (ore == null || ore.trim().isEmpty()) return null;

        // Поддерживаем несколько форматов:
        // "MATERIAL"
        // "MATERIAL~durability"
        // "minecraft:material"
        // legacy id или "id~durability"
        String input = ore.trim();
        String materialPart = input;
        short durability = 0;

        // Отделяем durability/data, если оно было передано вместе с материалом.
        if (input.contains("~")) {
            String[] parts = input.split("~", 2);
            materialPart = parts[0].trim();
            try {
                durability = Short.parseShort(parts[1].trim());
            } catch (NumberFormatException ignored) {
                durability = 0;
            }
        }

        Material mat = null;

        // Сначала пробуем современное имя материала, включая namespaced-формат.
        mat = Material.matchMaterial(materialPart);

        // Если имя namespaced, пробуем искать и без префикса namespace.
        if (mat == null && materialPart.contains(":")) {
            String afterColon = materialPart.substring(materialPart.indexOf(':') + 1);
            mat = Material.matchMaterial(afterColon);
        }

        // Последний шанс — legacy id/data через MaterialManager.
        if (mat == null) {
            try {
                mat = MaterialManager.getMaterial(materialPart); // legacy; may return null on modern APIs
            } catch (NumberFormatException ignored) {
            }
        }

        if (mat == null) return null;

        ItemStack item;
        try {
            item = new ItemStack(mat, 1, durability);
        } catch (NoSuchMethodError | IllegalArgumentException e) {
            // На новых API legacy-конструктор может отсутствовать, поэтому делаем fallback.
            item = new ItemStack(mat, 1);
            try {
                item.setDurability(durability);
            } catch (NoSuchMethodError ignored) {
            }
        }

        return item;
    }

//    public void runCommand(Player player, String weaponTitle) {
//        // Команды из конфига разделены специальным разделителем и могут исполняться как от игрока, так и от консоли.
//        String commands = this.plugin.getString(weaponTitle + ".Extras.Run_Command");
//        if (commands != null) {
//            commands = commands.replaceAll("<shooter>", player.getName());
//            Server server = this.plugin.getServer();
//            String delimiter = "่๋້";
//
//
//            for (String command : commands.split(delimiter)) {
//                if (command.startsWith("@")) {
//                    server.dispatchCommand(server.getConsoleSender(), command.substring(1).trim());
//                } else {
//                    server.dispatchCommand(player, command.trim());
//                }
//            }
//        }
//
//
//    }

    private void log(String msg) {
        System.out.println("[CrackShot] '" + msg);
    }
}
