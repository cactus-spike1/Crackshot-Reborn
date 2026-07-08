package com.shampaggon.crackshot;

import com.shampaggon.crackshot.CSMessages.Message;
import com.shampaggon.crackshot.cmd.CrackshotCMD;
import com.shampaggon.crackshot.compatibility.EntityExplodeEventFactory;
import com.shampaggon.crackshot.compatibility.MaterialManager;
import com.shampaggon.crackshot.compatibility.SoundCache;
import com.shampaggon.crackshot.compatibility.SoundManager;
import com.shampaggon.crackshot.events.*;
import fun.cactus.utils.*;
import fun.cactus.utils.commands.CommandStorage;
import fun.cactus.utils.commands.CommandUtils;
import fun.cactus.utils.commands.WeaponCommandManager;
import fun.cactus.utils.config.ConfigCache;
import fun.cactus.utils.config.ConfigCacheUtils;
import fun.cactus.utils.devices.*;
import fun.cactus.utils.entuty.EntityConfigurator;
import fun.cactus.utils.entuty.EntityFactory;
import fun.cactus.utils.entuty.SpawnEntityData;
import fun.cactus.utils.entuty.SpawnEntityParser;
import fun.cactus.utils.potion.PotionActivation;
import fun.cactus.utils.sound.*;
import fun.cactus.utils.enchantment.*;
import fun.cactus.utils.projectileSub.ProjectileSubtypeData;
import fun.cactus.utils.projectileSub.ProjectileSubtypeParser;
import fun.cactus.utils.projectileSub.ProjectileSubtypeType;
import fun.cactus.modules.ModuleFactory;
import fun.cactus.modules.WeaponModule;
import fun.cactus.utils.weapon.WeaponAttachmentUtils;
import fun.cactus.utils.weapon.WeaponHelperUtils;
import fun.cactus.utils.weapon.WeaponIndexUtils;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Основной класс CrackShot.
 * Здесь находятся жизненный цикл плагина, обработчики Bukkit-событий,
 * стрельба, урон, перезарядка, магазины, мины, C4 и служебные кэши.
 */
public class CSDirector extends JavaPlugin implements Listener {


    public CSDirector plugin = this;

    public Map<String, int[]> zoomStorage = new HashMap<>();
    public Map<String, Collection<Integer>> burst_task_IDs = new HashMap<>();
    public Map<String, Collection<Integer>> global_reload_IDs = new HashMap<>();
    public Map<String, Set<String>> grouplist = new HashMap<>();
    public Map<String, Boolean> morobust = new HashMap<>();
    public FileConfiguration weaponConfig = null;
    public Set<String> melees = new HashSet<>();
    public Map<String, Integer> rpm_ticks = new HashMap<>();
    public Map<String, Integer> rpm_shots = new HashMap<>();
    public Map<String, Map<Integer, Long>> last_shot_list = new HashMap<>();
    public Map<String, Map<String, String>> c4_backup = new HashMap<>();
    public Map<String, Integer> delayed_reload_IDs = new HashMap<>();
    public Map<String, Map<String, Integer>> delay_list = new HashMap<>();
    public Map<String, Map<String, ArrayDeque<Item>>> itembombs = new HashMap<>();
    public Map<String, String> convIDs = new HashMap<>();
    public Map<String, String[]> enchlist = new HashMap<>();
    public Map<String, String> parentlist = new HashMap<>();
    public Map<String, String> rdelist = new HashMap<>();
    public Map<Integer, String> wlist = new HashMap<>();
    public Map<String, String> boobs = new HashMap<>();
    public Map<UUID, DropAttempt> last_drop = new HashMap<>();
    public static PotionEffectType slowness;
    public static Map<String, Integer> ints = new HashMap<>();
    public static Map<String, Double> dubs = new HashMap<>();
    public static Map<String, Boolean> bools = new HashMap<>();
    public static Map<String, String> strings = new HashMap<>();
    public static Map<String, WeaponModule> weaponModules = new HashMap<>();
    public String[] disWorlds = new String[]{"0"};

    @Getter
    private String heading = "§7░ §c[-§l¬§cº§lc§7§ls§7] §c- §7";

    @Getter
    private String version = "0.98.13";

    // cactus codes
    @Getter
    private static CSDirector instance;
    @Getter
    private ReloadTaskManager reloadTaskManager;
    @Getter
    private SoundEffectManager soundEffectManager;
    private final EnchantmentCheckManager enchantManager = new EnchantmentCheckManager();
    private EntityFactory entityFactory = new EntityFactory();
    private MineDeviceParser mineDeviceParser;
    private ItemBombDeviceParser itemBombDeviceParser;
    private CommandStorage commandStorage = new CommandStorage();
    private WeaponCommandManager weaponCommandManager = new WeaponCommandManager();
    // end cactus codes

    public final CSMinion csminion = new CSMinion(this);

    private static final String COMMAND_LIST_DELIMITER = "่๋້";


    // Инициализация совместимости, загрузка конфигов и регистрация команды/слушателей.
    public void onEnable() {
        instance = this;
        try {
            Material.valueOf("SKULL");
        } catch (IllegalArgumentException e) {
            MaterialManager.pre113 = false;
        }
        // cactus codes
        reloadTaskManager = new ReloadTaskManager();

        soundEffectManager = new SoundEffectManager(
                this,
                reloadTaskManager
        );
        mineDeviceParser = new MineDeviceParser(csminion);
        itemBombDeviceParser = new ItemBombDeviceParser(csminion);
        // end cactus codes

        SoundCache.init();

        try {
            slowness = PotionEffectType.getByName("SLOW");
            if (slowness == null) {
                slowness = PotionEffectType.getByName("SLOWNESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (slowness == null) {
                this.printM("Failed to load zoom effect.");
            }

        }

        try {
            Class.forName("org.bukkit.projectiles.ProjectileSource");
        } catch (ClassNotFoundException e) {
            this.printM("Failed to load. Your version of CraftBukkit is outdated!");
            this.setEnabled(false);
            return;
        }

        this.csminion.loadWeapons(null);
        this.csminion.loadGeneralConfig();
        this.csminion.loadMessagesConfig();
        this.csminion.customRecipes();
        this.printM("Gun-mode activated. Boop!");
        Bukkit.getPluginManager().registerEvents(this, this);
        ((CraftServer) Bukkit.getServer()).getCommandMap().register("crackshot", new CrackshotCMD("crackshot", this));


    }

    // При выключении сначала чистим активное состояние игроков и сущностей, затем кэши и рецепты.
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        StateClearUtils.cleanupOnlinePlayers();
        StateClearUtils.clearTrackedItemBombs();
        StateClearUtils.clearTransientState();
        StateClearUtils.clearConfigurationCaches();
        this.csminion.clearRecipes();
    }

    public void fillHashMaps(FileConfiguration config) {
        // Сначала кэшируем простые значения, затем собираем производные индексы для быстрого доступа по оружию.
        ConfigCacheUtils.cachePrimitiveValues(config);

        for (String parentNode : config.getKeys(false)) {
            String attachmentType = ConfigCache.getString(parentNode + ".Item_Information.Attachments.Type");
            boolean accessory = WeaponHelperUtils.isAccessory(attachmentType);

            ConfigCacheUtils.cacheDirectWeaponValues(config, parentNode);
            WeaponIndexUtils.registerInventoryGroups(parentNode);
            this.registerEnchantmentCheck(parentNode);
            this.registerItemAlias(parentNode);
            this.registerDurabilityTracking(config, parentNode, accessory);
            this.commandStorage.registerRunCommands(config, parentNode);
            WeaponIndexUtils.registerWeaponName(config, parentNode, accessory);
            this.registerMeleeFlag(parentNode, attachmentType);
            this.registerExplosiveMappings(config, parentNode);
            this.initializeWeaponModule(config, parentNode);
        }
    }

    /**
     * Инициализирует WeaponModule для оружия из конфигурации
     */
    private void initializeWeaponModule(FileConfiguration config, String parentNode) {
        try {
            WeaponModule weapon = new WeaponModule(parentNode);
            weapon.setAmmoModule(ModuleFactory.parseAmmoModule(config, parentNode));
            weapon.setReloadModule(ModuleFactory.parseReloadModule(config, parentNode));
            weapon.setShootingModule(ModuleFactory.parseShootingModule(config, parentNode));
            weapon.setFirearmActionModule(ModuleFactory.parseFirearmActionModule(config, parentNode));
            
            if (weapon.validateAllModules()) {
                weaponModules.put(parentNode, weapon);
            } else {
                this.plugin.getLogger().warning("Weapon '" + parentNode + "' has invalid module configuration");
            }
        } catch (Exception e) {
            this.plugin.getLogger().warning("Failed to initialize modules for weapon '" + parentNode + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Получает WeaponModule для оружия по названию
     */
    public WeaponModule getWeaponModule(String weaponName) {
        return weaponModules.get(weaponName);
    }

    /**
     * Проверяет наличие WeaponModule для оружия
     */
    public boolean hasWeaponModule(String weaponName) {
        return weaponModules.containsKey(weaponName);
    }

    public void reloadConfig(CommandSender sender) {
        this.csminion.clearRecipes();
        StateClearUtils.clearConfigurationCaches();
        Player cmdReloader = sender instanceof Player ? (Player) sender : null;
        this.csminion.loadWeapons(cmdReloader);
        this.csminion.loadGeneralConfig();
        this.csminion.loadMessagesConfig();
        this.csminion.customRecipes();
    }







    private void registerEnchantmentCheck(String parentNode) {

        String enchantKey = ConfigCache.getString(parentNode + ".Item_Information.Enchantment_To_Check");

        EnchantmentCheck check = EnchantmentCheckParser.parse(enchantKey);

        if (check == null && enchantKey != null) {
            printM("For the weapon '" + parentNode +
                    "', the value provided for 'Enchantment_To_Check' is invalid.");
            return;
        }

        enchantManager.register(parentNode, check);
    }

    private void registerItemAlias(String parentNode) {
        if (!ConfigCache.getBoolean(parentNode + ".Item_Information.Skip_Name_Check")) {
            return;
        }

        String itemInfo = ConfigCache.getString(parentNode + ".Item_Information.Item_Type");
        ItemStack item = this.csminion.parseItemStack(itemInfo);
        if (item != null) {
            this.convIDs.put(item.getType() + "-" + item.getDurability(), parentNode);
        }
    }

    private void registerDurabilityTracking(FileConfiguration config, String parentNode, boolean accessory) {
        if (accessory) {
            return;
        }

        String itemType = config.getString(parentNode + ".Item_Information.Item_Type");
        String durabilityNode = config.getString(parentNode + ".Item_Information.Item_Has_Durability");
        if (itemType == null) {
            this.printM("The weapon '" + parentNode + "' does not have a value for Item_Type.");
        } else if (durabilityNode == null && this.csminion.durabilityCheck(itemType)) {
            this.morobust.put(parentNode, true);
        }
    }

    private void registerMeleeFlag(String parentNode, String attachmentType) {
        boolean meleeMode = ConfigCache.getBoolean(parentNode + ".Item_Information.Melee_Mode");
        String meleeAttach = ConfigCache.getString(parentNode + ".Item_Information.Melee_Attachment");
        if (meleeAttach != null || meleeMode || "main".equalsIgnoreCase(attachmentType)) {
            this.melees.add(parentNode);
        }
    }


    public void registerExplosiveMappings(FileConfiguration config, String parentNode) {

        if (!config.getBoolean(parentNode + ".Explosive_Devices.Enable")) {
            return;
        }

        String deviceInfoString = config.getString(parentNode + ".Explosive_Devices.Device_Info");

        if (deviceInfoString != null) {

            try {

                RDEDeviceInfo info = RDEDeviceParser.parse(deviceInfoString);

                rdelist.put(info.getId(), parentNode);

            } catch (Exception ignored) {
            }
        }

        String deviceType = config.getString(parentNode + ".Explosive_Devices.Device_Type");

        if ("trap".equalsIgnoreCase(deviceType)) {

            String itemName = ConfigCache.getString(parentNode + ".Item_Information.Item_Name");

            String displayName = NameUtils.toDisplayForm(itemName);

            boobs.put(displayName, parentNode);
        }
    }











    // Главный обработчик взаимодействий: выстрел, прицел, переключение обвесов, установка мин и работа ловушек.
    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock().getType().toString().contains("WALL_SIGN") && this.shopEvent(event)) {
                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK && MaterialManager.isSkullBlock(event.getClickedBlock()) && event.getClickedBlock().hasMetadata("CS_transformers")) {
                event.setCancelled(true);
            }

            Player shooter = event.getPlayer();
            ItemStack item = shooter.getItemInHand();
            String parent_node = ItemUtils.returnParentNode(shooter);
            if (parent_node == null) {
                return;
            }

            if (!ConfigCache.getBoolean(parent_node + ".Item_Information.Melee_Mode") && !this.validHotbar(shooter, parent_node)) {
                return;
            }

            if (!this.regionAndPermCheck(shooter, parent_node, false)) {
                return;
            }

            boolean rightShoot = ConfigCache.getBoolean(parent_node + ".Shooting.Right_Click_To_Shoot");
            boolean dualWield = WeaponHelperUtils.isDualWield(shooter, parent_node, item);
            boolean leftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
            boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
            UUID playerUuid = shooter.getUniqueId();
            if (event.getAction() == Action.LEFT_CLICK_AIR && this.last_drop.containsKey(playerUuid)) {
                DropAttempt lastDrop = this.last_drop.get(playerUuid);
                if (lastDrop.IsOnSameSlotAndTick(shooter)) {
                    return;
                }
            }

            boolean rdeEnable = ConfigCache.getBoolean(parent_node + ".Explosive_Devices.Enable");
            String[] attachTypeAndInfo = WeaponAttachmentUtils.getAttachment(parent_node, item);
            // Обвесы и dual wield взаимоисключают друг друга, поэтому проверяем конфликт здесь.
            if (attachTypeAndInfo[0] != null) {
                if (attachTypeAndInfo[0].equalsIgnoreCase("accessory") && rdeEnable) {
                    shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' is an attachment. It cannot use the Explosive_Devices module!");
                    return;
                }

                if (dualWield) {
                    shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' cannot use attachments and be dual wielded at the same time!");
                    return;
                }
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                boolean noBlockDmg = ConfigCache.getBoolean(parent_node + ".Shooting.Cancel_Left_Click_Block_Damage");
                if (noBlockDmg) {
                    event.setCancelled(true);
                }
            }

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

                boolean rightInteract = ConfigCache.getBoolean(parent_node + ".Shooting.Cancel_Right_Click_Interactions");
                if (rightInteract) {
                    event.setCancelled(true);
                }
            }

            if (!item.getItemMeta().getDisplayName().contains("§")) {
                shooter.setItemInHand(this.csminion.vendingMachine(parent_node));
                event.setCancelled(true);
                return;
            }

            if (!ConfigCache.getBoolean(parent_node + ".Item_Information.Remove_Unused_Tag")) {
                this.checkCorruption(item, attachTypeAndInfo[0] != null, dualWield);
            }

            if ((!rightShoot || !rightClick) && (rightShoot || !leftClick) && !dualWield) {
                if (!dualWield && (rightShoot && leftClick || !rightShoot && rightClick)) {
                    if (ConfigCache.getBoolean(parent_node + ".Reload.Reload_With_Mouse")) {
                        this.reloadAnimation(shooter, parent_node);
                        return;
                    }

                    if (this.tossBomb(shooter, parent_node, item, rdeEnable)) {
                        return;
                    }

                    if (attachTypeAndInfo[0] != null) {
                        int gunSlot = shooter.getInventory().getHeldItemSlot();
                        boolean hasDelay = shooter.hasMetadata("togglesnoShooting" + gunSlot);
                        if (hasDelay) {
                            return;
                        }

                        boolean main = attachTypeAndInfo[0].equalsIgnoreCase("main");
                        boolean accessory = attachTypeAndInfo[0].equalsIgnoreCase("accessory");
                        if (main || accessory) {
                            if (main) {
                                String attachValid = ConfigCache.getString(attachTypeAndInfo[1] + ".Item_Information.Attachments.Type");
                                if (attachTypeAndInfo[1] == null) {
                                    shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' is missing the weapon title of an attachment!");
                                    return;
                                }

                                if (attachValid == null) {
                                    shooter.sendMessage(this.heading + "The weapon '" + parent_node + "' has an invalid attachment. The weapon '" + attachTypeAndInfo[1] + "' has to be an accessory!");
                                    return;
                                }
                            }

                            int toggleDelay = ConfigCache.getInt(parent_node + ".Item_Information.Attachments.Toggle_Delay");
                            WeaponAttachmentToggleEvent toggleEvent = new WeaponAttachmentToggleEvent(shooter, parent_node, item, toggleDelay);
                            this.getServer().getPluginManager().callEvent(toggleEvent);
                            if (toggleEvent.isCancelled()) {
                                return;
                            }

                            SoundUtils.playSoundEffects(shooter, parent_node, ".Item_Information.Attachments.Sounds_Toggle", false, null);
                            this.reloadShootDelay(shooter, parent_node, gunSlot, toggleEvent.getToggleDelay(), "noShooting", "toggles");
                            this.terminateAllBursts(shooter);
                            this.terminateReload(shooter);
                            this.removeInertReloadTag(shooter, 0, true);
                            if (ItemUtils.itemIsSafe(item)) {
                                String itemName = item.getItemMeta().getDisplayName();
                                String triOne = String.valueOf('▶');
                                String triTwo = String.valueOf('▷');
                                String triThree = String.valueOf('◀');
                                String triFour = String.valueOf('◁');
                                if (itemName.contains(triThree)) {
                                    this.csminion.setItemName(item, itemName.replaceAll(triThree + triTwo, triFour + triOne));
                                } else {
                                    this.csminion.setItemName(item, itemName.replaceAll(triFour + triOne, triThree + triTwo));
                                }
                            }

                            return;
                        }
                    }

                    boolean zoomEnable = ConfigCache.getBoolean(parent_node + ".Scope.Enable");
                    boolean nightScope = ConfigCache.getBoolean(parent_node + ".Scope.Night_Vision");
                    if (!zoomEnable || shooter.hasMetadata("markOfTheReload")) {
                        return;
                    }

                    int zoomAmount = ConfigCache.getInt(parent_node + ".Scope.Zoom_Amount");
                    if (zoomAmount < 0 || zoomAmount == 0 || zoomAmount > 10) {
                        return;
                    }

                    WeaponScopeEvent scopeEvent = new WeaponScopeEvent(shooter, parent_node, !shooter.hasMetadata("ironsights"));
                    this.getServer().getPluginManager().callEvent(scopeEvent);
                    if (scopeEvent.isCancelled()) {
                        return;
                    }

                    SoundUtils.playSoundEffects(shooter, parent_node, ".Scope.Sounds_Toggle_Zoom", false, null);
                    if (shooter.hasPotionEffect(slowness)) {
                        for (PotionEffect pe : shooter.getActivePotionEffects()) {
                            if (pe.getType() == slowness) {
                                if (shooter.hasMetadata("ironsights")) {
                                    this.unscopePlayer(shooter, true);
                                } else {
                                    if (!shooter.hasPotionEffect(PotionEffectType.NIGHT_VISION) && nightScope) {
                                        shooter.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(2400, 1));
                                        shooter.setMetadata("night_scoping", new FixedMetadataValue(this, true));
                                    }

                                    shooter.setMetadata("ironsights", new FixedMetadataValue(this, parent_node));
                                    this.zoomStorage.put(shooter.getName(), new int[]{pe.getDuration(), pe.getAmplifier()});
                                    shooter.removePotionEffect(slowness);
                                    shooter.addPotionEffect(slowness.createEffect(2400, zoomAmount));
                                }
                                break;
                            }
                        }
                    } else {
                        if (!shooter.hasPotionEffect(PotionEffectType.NIGHT_VISION) && nightScope) {
                            shooter.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(2400, 1));
                            shooter.setMetadata("night_scoping", new FixedMetadataValue(this, true));
                        }

                        shooter.setMetadata("ironsights", new FixedMetadataValue(this, parent_node));
                        shooter.addPotionEffect(slowness.createEffect(2400, zoomAmount));
                    }
                }
            } else if (rdeEnable) {
                String type = ConfigCache.getString(parent_node + ".Explosive_Devices.Device_Type");
                if (type != null) {
                    if (!type.equalsIgnoreCase("remote") && !type.equalsIgnoreCase("itembomb")) {
                        if (type.equalsIgnoreCase("trap") && ItemUtils.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains("«?»")) {
                            String itemName = ConfigCache.getString(parent_node + ".Item_Information.Item_Name");
                            this.csminion.setItemName(shooter.getInventory().getItemInHand(), itemName + " «" + shooter.getName() + "»");
                            SoundUtils.playSoundEffects(shooter, parent_node, ".Explosive_Devices.Sounds_Deploy", false, null);
                        } else if (type.equalsIgnoreCase("landmine")) {
                            this.csminion.oneTime(shooter);
                            SoundUtils.playSoundEffects(shooter, parent_node, ".Explosive_Devices.Sounds_Deploy", false, null);
                            this.deployMine(shooter, parent_node, null);
                        }
                    } else {
                        this.detonateC4(shooter, item, parent_node, type);
                    }
                }
            } else if (item.getType() != Material.BOW) {
                this.csminion.weaponInteraction(shooter, parent_node, leftClick);
            }
        } else if (MaterialManager.isPressurePlate(event.getClickedBlock())) {
            Player victim = event.getPlayer();

            for (Entity e : victim.getNearbyEntities(4.0F, 4.0F, 4.0F)) {
                if (e instanceof ItemFrame) {
                    this.csminion.boobyAction(event.getClickedBlock(), victim, ((ItemFrame) e).getItem());
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack heldItem = player.getItemInHand();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            return;
        }

        String parentNode = ItemUtils.returnParentNode(player);
        if (parentNode == null) {
            return;
        }

        if (ConfigCache.getBoolean(parentNode + ".Item_Information.Melee_Mode")) {
            return;
        }

        if (!WeaponHelperUtils.isDualWield(player, parentNode, heldItem)) {
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock != null && !WeaponHelperUtils.isAir(targetBlock.getType())) {
            return;
        }

        int gunSlot = player.getInventory().getHeldItemSlot();
        String animationTag = parentNode + "leftClickAirAnimation" + gunSlot;
        if (player.hasMetadata(animationTag)) {
            return;
        }

        player.setMetadata(animationTag, new FixedMetadataValue(this, true));
        this.csminion.tempVars(player, animationTag, 1L);
        this.csminion.weaponInteraction(player, parentNode, true);
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    // Централизованный конвейер урона: melee, projectile, explosions, riot shield и служебные исключения.
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity entVictim = event.getEntity();
        Entity entDmger = event.getDamager();
        boolean cancelMelee = false;
        if (entVictim instanceof Player && entVictim.hasMetadata("CS_singed")) {
            cancelMelee = true;
            if (!event.isCancelled()) {
                entVictim.setMetadata("CS_singed", new FixedMetadataValue(this, true));
                event.setCancelled(true);
            } else {
                entVictim.removeMetadata("CS_singed", this);
            }
        }

        if (entVictim instanceof Player && entVictim.hasMetadata("deep_fr1ed")) {
            cancelMelee = true;
            String parent_node = null;
            Player pPlayer = null;
            boolean nodam = false;
            Player victim = (Player) entVictim;
            double damage = victim.getMetadata("deep_fr1ed").get(0).asDouble();
            victim.removeMetadata("deep_fr1ed", this);
            if (victim.hasMetadata("CS_nodam")) {
                nodam = true;
            }

            if (victim.hasMetadata("CS_potex") && victim.getMetadata("CS_potex") != null) {
                parent_node = victim.getMetadata("CS_potex").get(0).asString();
            }

            if (entDmger instanceof Player) {
                pPlayer = (Player) entDmger;
            }

            victim.removeMetadata("CS_potex", this);
            if (!event.isCancelled()) {
                this.csminion.explosionPackage(victim, parent_node, pPlayer);
                if (!nodam) {
                    event.setDamage(damage);
                } else {
                    event.setCancelled(true);
                }
            }
        }

        if (entDmger instanceof Player && entVictim instanceof LivingEntity) {
            Player player = (Player) entDmger;
            Location finalLoc = player.getEyeLocation();
            Vector direction = player.getEyeLocation().getDirection().normalize().multiply((double) 0.5F);

            for (int i = 0; i < 10; ++i) {
                finalLoc.add(direction);
                if (!WeaponHelperUtils.isAir(finalLoc.getBlock().getType())) {
                    this.OnPlayerInteract(new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, player.getItemInHand(), null, null));
                    break;
                }
            }
        }

        if (entVictim instanceof LargeFireball && entVictim.hasMetadata("CS_NoDeflect")) {
            event.setCancelled(true);
        } else {
            if (entDmger instanceof Player && event.getDamage() == (double) 8.0F && event.getCause() == DamageCause.ENTITY_ATTACK) {
                for (Entity closeEnt : entVictim.getNearbyEntities(4.0F, 4.0F, 4.0F)) {
                    if (closeEnt instanceof WitherSkull && ((Projectile) closeEnt).getShooter() == entDmger) {
                        event.setCancelled(true);
                    }
                }
            }

            if (!cancelMelee && entDmger instanceof Player && event.getCause() == DamageCause.ENTITY_ATTACK && !event.isCancelled() && entVictim instanceof LivingEntity) {
                Player player = (Player) entDmger;
                String parentNode = ItemUtils.returnParentNode(player);
                if (parentNode != null && this.regionAndPermCheck(player, parentNode, true)) {
                    int punchDelay = ConfigCache.getInt(parentNode + ".Shooting.Delay_Between_Shots");
                    int gunSlot = player.getInventory().getHeldItemSlot();
                    if (!player.hasMetadata(parentNode + "meleeDelay" + gunSlot)) {
                        if (ConfigCache.getBoolean(parentNode + ".Item_Information.Melee_Mode")) {
                            ItemStack item = player.getItemInHand();
                            String ammoInfo = ConfigCache.getString(parentNode + ".Ammo.Ammo_Item_ID");
                            boolean reloadOn = ConfigCache.getBoolean(parentNode + ".Reload.Enable");
                            boolean ammoPerShot = ConfigCache.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
                            boolean ammoEnable = ConfigCache.getBoolean(parentNode + ".Ammo.Enable");
                            boolean takeAmmo = ConfigCache.getBoolean(parentNode + ".Reload.Take_Ammo_On_Reload");
                            int detectedAmmo = AmmoUtils.getAmmoBetweenBrackets(player, parentNode, item);
                            if (!this.validHotbar(player, parentNode)) {
                                return;
                            }

                            player.setMetadata(parentNode + "meleeDelay" + gunSlot, new FixedMetadataValue(this, true));
                            this.csminion.tempVars(player, parentNode + "meleeDelay" + gunSlot, (long) punchDelay);
                            if (ammoEnable) {
                                // Для melee-оружия тоже учитываем ammo-логику, если она включена в конфиге.
                                if (!takeAmmo && !ammoPerShot) {
                                    player.sendMessage(this.heading + "The weapon '" + parentNode + "' has enabled the Ammo module, but at least one of the following nodes need to be set to true: Take_Ammo_On_Reload, Take_Ammo_Per_Shot.");
                                    return;
                                }

                                if (!this.csminion.containsItemStack(player, ammoInfo, 1, parentNode) && (ammoPerShot || takeAmmo && detectedAmmo == 0)) {
                                    SoundUtils.playSoundEffects(player, parentNode, ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);
                                    return;
                                }
                            }

                            if (ItemUtils.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains(String.valueOf('ᴿ'))) {
                                if (detectedAmmo <= 0) {
                                    this.reloadAnimation(player, parentNode);
                                    return;
                                }

                                this.terminateReload(player);
                                this.removeInertReloadTag(player, 0, true);
                            }

                            if (reloadOn) {
                                if (detectedAmmo <= 0) {
                                    this.reloadAnimation(player, parentNode);
                                    return;
                                }

                                this.ammoOperation(player, parentNode, detectedAmmo, item);
                            } else {
                                String itemName = item.getItemMeta().getDisplayName();
                                if (itemName.contains("«") && !itemName.contains(String.valueOf('×'))) {
                                    this.csminion.replaceBrackets(item, String.valueOf('×'), parentNode);
                                }
                            }

                            this.dealDamage(player, (LivingEntity) entVictim, event, parentNode);
                        } else {
                            String meleeNode = ConfigCache.getString(parentNode + ".Item_Information.Melee_Attachment");
                            if (meleeNode != null) {
                                punchDelay = ConfigCache.getInt(meleeNode + ".Shooting.Delay_Between_Shots");
                                if (this.melees.contains(meleeNode)) {
                                    if (this.validHotbar(player, parentNode)) {
                                        player.setMetadata(parentNode + "meleeDelay" + gunSlot, new FixedMetadataValue(this, true));
                                        this.csminion.tempVars(player, parentNode + "meleeDelay" + gunSlot, (long) punchDelay);
                                        this.dealDamage(player, (LivingEntity) entVictim, event, meleeNode);
                                    }
                                } else {
                                    player.sendMessage(this.heading + "The weapon '" + parentNode + "' has an unknown melee attachment. '" + meleeNode + "' could not be found!");
                                }
                            }
                        }
                    } else {
                        event.setCancelled(true);
                    }
                }
            }

            if ((entDmger instanceof WitherSkull || entDmger instanceof LargeFireball) && entDmger.hasMetadata("projParentNode")) {
                event.setCancelled(true);
            }

            if (entDmger instanceof Player && entVictim instanceof Player && entVictim.hasMetadata("CS_Energy") && !event.isCancelled()) {
                this.dealDamage(entDmger, (LivingEntity) entVictim, event, entVictim.getMetadata("CS_Energy").get(0).asString());
                entVictim.removeMetadata("CS_Energy", this);
            }

            if ((entDmger instanceof Arrow || entDmger instanceof Egg || entDmger instanceof Snowball) && entDmger.hasMetadata("projParentNode") && entVictim instanceof LivingEntity && !event.isCancelled()) {
                this.dealDamage(entDmger, (LivingEntity) entVictim, event, entDmger.getMetadata("projParentNode").get(0).asString());
            }

            if (entDmger instanceof TNTPrimed && entDmger.hasMetadata("CS_Label")) {
                if (entDmger.hasMetadata("nullify") && (entVictim instanceof Painting || entVictim instanceof ItemFrame || entVictim instanceof ArmorStand || entVictim instanceof Item || entVictim instanceof ExperienceOrb)) {
                    event.setCancelled(true);
                }

                if (entDmger.hasMetadata("CS_nodam") || entVictim.hasMetadata("CS_shrapnel")) {
                    if (entVictim instanceof Player) {
                        entVictim.setMetadata("CS_nodam", new FixedMetadataValue(this, true));
                        this.csminion.tempVars((Player) entVictim, "CS_nodam", 2L);
                    }

                    event.setCancelled(true);
                }

                String parent_node = null;
                double totalDmg = event.getDamage();
                if (entDmger.hasMetadata("CS_potex")) {
                    parent_node = entDmger.getMetadata("CS_potex").get(0).asString();
                    if (event.getDamage() > (double) 1.0F && parent_node != null) {
                        try {
                            String multiString = ConfigCache.getString(parent_node + ".Explosions.Damage_Multiplier");
                            if (multiString != null) {
                                double multiplier = (double) Integer.parseInt(multiString) * 0.01;
                                totalDmg *= multiplier;
                                totalDmg = this.csminion.getSuperDamage(entVictim.getType(), parent_node, totalDmg);
                            }
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }

                int knockBack = ConfigCache.getInt(parent_node + ".Explosions.Knockback");
                if (knockBack != 0 && !entVictim.hasMetadata("CS_shrapnel")) {
                    Vector vector = this.csminion.getAlignedDirection(entDmger.getLocation(), entVictim.getLocation());
                    entVictim.setVelocity(vector.multiply((double) knockBack * 0.1));
                }

                String pName = "Player";
                Player pPlayer = null;
                if (entDmger.hasMetadata("CS_pName")) {
                    pName = entDmger.getMetadata("CS_pName").get(0).asString();
                    pPlayer = Bukkit.getServer().getPlayer(pName);
                }

                boolean noDam = entVictim instanceof Player && entDmger.hasMetadata("0wner_nodam") && entVictim.getName().equals(pName);
                if (noDam) {
                    totalDmg = 0.0F;
                }

                WeaponDamageEntityEvent weaponEvent = new WeaponDamageEntityEvent(pPlayer, entVictim, entDmger, parent_node, totalDmg, false, false, false);
                this.getServer().getPluginManager().callEvent(weaponEvent);
                event.setDamage(weaponEvent.getDamage());
                if (weaponEvent.isCancelled()) {
                    event.setCancelled(true);
                } else if (entVictim instanceof Player) {
                    Player victim = (Player) entVictim;
                    if (noDam) {
                        event.setCancelled(true);
                        return;
                    }

                    victim.setNoDamageTicks(0);
                    if (entDmger.hasMetadata("CS_ffcheck")) {
                        if (victim.getName().equals(pName)) {
                            this.csminion.explosionPackage(victim, parent_node, pPlayer);
                        } else if (pPlayer != null) {
                            victim.setMetadata("deep_fr1ed", new FixedMetadataValue(this, event.getDamage()));
                            if (parent_node != null) {
                                victim.setMetadata("CS_potex", new FixedMetadataValue(this, parent_node));
                            }

                            this.csminion.illegalSlap(pPlayer, victim, 0);
                            event.setCancelled(true);
                        }
                    } else {
                        this.csminion.explosionPackage(victim, parent_node, pPlayer);
                    }
                } else if (entVictim instanceof LivingEntity) {
                    ((LivingEntity) entVictim).setNoDamageTicks(0);
                    this.csminion.explosionPackage((LivingEntity) entVictim, parent_node, pPlayer);
                }
            }

            if (entVictim instanceof Player && !event.isCancelled()) {
                Player blocker = (Player) entVictim;
                String parentNode = ItemUtils.returnParentNode(blocker);
                if (parentNode == null) {
                    return;
                }

                int durabPerHit = ConfigCache.getInt(parentNode + ".Riot_Shield.Durability_Loss_Per_Hit");
                boolean riotEnable = ConfigCache.getBoolean(parentNode + ".Riot_Shield.Enable");
                boolean durabDmg = ConfigCache.getBoolean(parentNode + ".Riot_Shield.Durablity_Based_On_Damage");
                boolean noProj = ConfigCache.getBoolean(parentNode + ".Riot_Shield.Do_Not_Block_Projectiles");
                boolean noMelee = ConfigCache.getBoolean(parentNode + ".Riot_Shield.Do_Not_Block_Melee_Attacks");
                boolean forceField = ConfigCache.getBoolean(parentNode + ".Riot_Shield.Forcefield_Mode");
                boolean mustBlock = ConfigCache.getBoolean(parentNode + ".Riot_Shield.Only_Works_While_Blocking");
                if (mustBlock && !blocker.isBlocking()) {
                    return;
                }

                if (!riotEnable || !this.regionAndPermCheck(blocker, parentNode, false)) {
                    return;
                }

                if (entDmger instanceof Projectile) {
                    if (noProj) {
                        return;
                    }

                    if (!forceField) {
                        Projectile objProj = (Projectile) entDmger;
                        double faceAngle = blocker.getLocation().getDirection().dot(((Entity) objProj.getShooter()).getLocation().getDirection());
                        if (faceAngle > (double) 0.0F && !(objProj.getShooter() instanceof Skeleton)) {
                            return;
                        }
                    }
                } else {
                    if (noMelee) {
                        return;
                    }

                    if (!forceField) {
                        double faceAngle = blocker.getLocation().getDirection().dot(entDmger.getLocation().getDirection());
                        if (faceAngle > (double) 0.0F) {
                            return;
                        }
                    }
                }

                if (durabDmg) {
                    durabPerHit = (int) ((double) durabPerHit * event.getDamage());
                }

                ItemStack shield = blocker.getInventory().getItemInHand();
                shield.setDurability((short) (shield.getDurability() + durabPerHit));
                SoundUtils.playSoundEffects(blocker, parentNode, ".Riot_Shield.Sounds_Blocked", false, null);
                if (shield.getType().getMaxDurability() <= shield.getDurability()) {
                    SoundUtils.playSoundEffects(blocker, parentNode, ".Riot_Shield.Sounds_Break", false, null);
                    blocker.getInventory().clear(blocker.getInventory().getHeldItemSlot());
                    blocker.updateInventory();
                }

                event.setCancelled(true);
            }

        }
    }

    // Здесь собираются все модификаторы попадания: headshot, крит, backstab, fire, knockback и flight-time.
    public void dealDamage(Entity entDmger, LivingEntity victim, EntityDamageByEntityEvent event, String parent_node) {
        boolean energyMode = entDmger instanceof Player;
        Projectile objProj = null;
        Player shooter;
        if (!energyMode) {
            objProj = (Projectile) entDmger;
            shooter = (Player) objProj.getShooter();
            objProj.setMetadata("Collided", new FixedMetadataValue(this, true));
        } else {
            shooter = (Player) entDmger;
        }

        if (shooter != null) {
            double projSpeed = (double) ConfigCache.getInt(parent_node + ".Shooting.Projectile_Speed") * 0.1;
            boolean hitEnable = ConfigCache.getBoolean(parent_node + ".Hit_Events.Enable");
            boolean headShots = ConfigCache.getBoolean(parent_node + ".Headshot.Enable");
            boolean bsEnable = ConfigCache.getBoolean(parent_node + ".Backstab.Enable");
            boolean critEnable = ConfigCache.getBoolean(parent_node + ".Critical_Hits.Enable");
            boolean fireEnable = ConfigCache.getBoolean(parent_node + ".Shooting.Projectile_Incendiary.Enable");
            int fireDuration = ConfigCache.getInt(parent_node + ".Shooting.Projectile_Incendiary.Duration");
            boolean zapEnable = ConfigCache.getBoolean(parent_node + ".Lightning.Enable");
            boolean resetHits = ConfigCache.getBoolean(parent_node + ".Abilities.Reset_Hit_Cooldown");
            boolean flightEnable = ConfigCache.getBoolean(parent_node + ".Damage_Based_On_Flight_Time.Enable");
            String makeSpeak = ConfigCache.getString(parent_node + ".Extras.Make_Victim_Speak");
            String makeRunCmd = ConfigCache.getString(parent_node + ".Extras.Make_Victim_Run_Commmand");
            String runConsole = ConfigCache.getString(parent_node + ".Extras.Run_Console_Command");
            int knockBack = ConfigCache.getInt(parent_node + ".Abilities.Knockback");
            String bonusDrops = ConfigCache.getString(parent_node + ".Abilities.Bonus_Drops");
            int activTime = ConfigCache.getInt(parent_node + ".Explosions.Projectile_Activation_Time");
            int projFlight = 0;
            double projTotalDmg = ConfigCache.getInt(parent_node + ".Shooting.Projectile_Damage");
            boolean instantKill = this.csminion.shouldInstantKill(victim.getType(), parent_node);
            double exactKillDamage = 0.0D;
            boolean BS = false;
            boolean crit = false;
            boolean boomHS = false;
            if (flightEnable && !energyMode) {
                int dmgPerTick = ConfigCache.getInt(parent_node + ".Damage_Based_On_Flight_Time.Bonus_Damage_Per_Tick");
                int flightMax = ConfigCache.getInt(parent_node + ".Damage_Based_On_Flight_Time.Maximum_Damage");
                int flightMin = ConfigCache.getInt(parent_node + ".Damage_Based_On_Flight_Time.Minimum_Damage");
                boolean negDmg = dmgPerTick < 0 && flightMax < 0;
                projFlight = objProj.getTicksLived();
                int tickDmgTotal = projFlight * dmgPerTick;
                if (tickDmgTotal < flightMin && flightMin != 0) {
                    tickDmgTotal = 0;
                }

                if (!negDmg) {
                    if (tickDmgTotal > flightMax && flightMax != 0) {
                        tickDmgTotal = flightMax;
                    }
                } else if (tickDmgTotal < flightMax && flightMax != 0) {
                    tickDmgTotal = flightMax;
                }

                projTotalDmg += tickDmgTotal;
            }

            if (bsEnable) {
                int bsBonusDmg = ConfigCache.getInt(parent_node + ".Backstab.Bonus_Damage");
                double faceAngle = victim.getLocation().getDirection().dot(shooter.getLocation().getDirection());
                if (faceAngle > (double) 0.0F) {
                    BS = true;
                    projTotalDmg += bsBonusDmg;
                }
            }

            if (critEnable) {
                int critBonus = ConfigCache.getInt(parent_node + ".Critical_Hits.Bonus_Damage");
                int critChance = ConfigCache.getInt(parent_node + ".Critical_Hits.Chance");
                Random ranGen = new Random();
                int Chance = ranGen.nextInt(100);
                if (Chance <= critChance) {
                    crit = true;
                    projTotalDmg += critBonus;
                }
            }

            if (headShots && !energyMode && this.csminion.isHesh(objProj, victim)) {
                boomHS = true;
                projTotalDmg += ConfigCache.getInt(parent_node + ".Headshot.Bonus_Damage");
            }

            projTotalDmg = this.csminion.getSuperDamage(victim.getType(), parent_node, projTotalDmg);
            if (projTotalDmg < (double) 0.0F) {
                projTotalDmg = 0.0F;
            }

            if (instantKill) {
                exactKillDamage = victim.getHealth();
                if (victim instanceof Player) {
                    exactKillDamage += ((Player) victim).getAbsorptionAmount();
                }

                if (exactKillDamage > projTotalDmg) {
                    projTotalDmg = exactKillDamage;
                }
            }

            WeaponDamageEntityEvent weaponEvent = new WeaponDamageEntityEvent(shooter, victim, objProj, parent_node, projTotalDmg, boomHS, BS, crit);
            this.getServer().getPluginManager().callEvent(weaponEvent);
            if (weaponEvent.isCancelled()) {
                if (event != null) {
                    event.setCancelled(true);
                }

            } else {
                if (resetHits) {
                    this.setTempVulnerability(victim);
                }

                if (instantKill && victim instanceof Player) {
                    Player victimPlayer = (Player) victim;
                    victimPlayer.setMetadata("deep_fr1ed", new FixedMetadataValue(this, Math.max(weaponEvent.getDamage(), exactKillDamage)));
                    if (parent_node != null) {
                        victimPlayer.setMetadata("CS_potex", new FixedMetadataValue(this, parent_node));
                    }

                    this.csminion.illegalSlap(shooter, victimPlayer, 0);
                    if (event != null) {
                        event.setCancelled(true);
                    }
                    return;
                } else if (event != null) {
                    event.setDamage(weaponEvent.getDamage());
                } else {
                    victim.damage(weaponEvent.getDamage(), shooter);
                }

                AmmoUtils.applyAbilityKnockback(victim, shooter, parent_node, knockBack);

                if (energyMode || objProj.getTicksLived() >= activTime) {
                    this.projectileExplosion(victim, parent_node, false, shooter, false, false, null, null, false, 0);
                }

                if (zapEnable) {
                    boolean zapNoDmg = ConfigCache.getBoolean(parent_node + ".Lightning.No_Damage");
                    this.csminion.projectileLightning(victim.getLocation(), zapNoDmg);
                }

                if (fireEnable && fireDuration != 0) {
                    victim.setFireTicks(fireDuration);
                }

                String flyTime = String.valueOf(projFlight);
                String dmgTotal = String.valueOf(projTotalDmg);
                String nameShooter = shooter.getName();
                String nameVic = "Entity";
                if (victim instanceof Player) {
                    nameVic = victim.getName();
                } else if (victim instanceof LivingEntity) {
                    nameVic = victim.getType().getName();
                }

                if (boomHS) {
                    CommandUtils.sendPlayerMessage(shooter, parent_node, ".Headshot.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
                    CommandUtils.sendPlayerMessage(victim, parent_node, ".Headshot.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
                    SoundUtils.playSoundEffects(shooter, parent_node, ".Headshot.Sounds_Shooter", false, null);
                    SoundUtils.playSoundEffects(victim, parent_node, ".Headshot.Sounds_Victim", false, null);
                    this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Headshot", false, null);
                    this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Headshot");
                    this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", /*"head"*/PotionActivation.HEAD);
                    this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", /*"head"*/PotionActivation.HEAD);
                }

                if (crit) {
                    CommandUtils.sendPlayerMessage(shooter, parent_node, ".Critical_Hits.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
                    CommandUtils.sendPlayerMessage(victim, parent_node, ".Critical_Hits.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
                    SoundUtils.playSoundEffects(shooter, parent_node, ".Critical_Hits.Sounds_Shooter", false, null);
                    SoundUtils.playSoundEffects(victim, parent_node, ".Critical_Hits.Sounds_Victim", false, null);
                    this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Critical", false, null);
                    this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Critical");
                    this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", /*"crit"*/PotionActivation.CRIT);
                    this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", /*"crit"*/PotionActivation.CRIT);
                }

                if (BS) {
                    CommandUtils.sendPlayerMessage(shooter, parent_node, ".Backstab.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
                    CommandUtils.sendPlayerMessage(victim, parent_node, ".Backstab.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
                    SoundUtils.playSoundEffects(shooter, parent_node, ".Backstab.Sounds_Shooter", false, null);
                    SoundUtils.playSoundEffects(victim, parent_node, ".Backstab.Sounds_Victim", false, null);
                    this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Backstab", false, null);
                    this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Backstab");
                    this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", /*"back"*/PotionActivation.BACK);
                    this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", /*"back"*/PotionActivation.BACK);
                }

                if (hitEnable) {
                    CommandUtils.sendPlayerMessage(shooter, parent_node, ".Hit_Events.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
                    CommandUtils.sendPlayerMessage(victim, parent_node, ".Hit_Events.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
                    SoundUtils.playSoundEffects(shooter, parent_node, ".Hit_Events.Sounds_Shooter", false, null);
                    SoundUtils.playSoundEffects(victim, parent_node, ".Hit_Events.Sounds_Victim", false, null);
                }

                this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Impact_Anything", false, null);
                this.csminion.giveParticleEffects(victim, parent_node, ".Particles.Particle_Hit", false, null);
                this.csminion.displayFireworks(victim, parent_node, ".Fireworks.Firework_Hit");
                this.csminion.givePotionEffects(shooter, parent_node, ".Potion_Effects.Potion_Effect_Shooter", /*"hit"*/PotionActivation.HIT);
                this.csminion.givePotionEffects(victim, parent_node, ".Potion_Effects.Potion_Effect_Victim", /*"hit"*/PotionActivation.HIT);
                if (this.spawnEntities(victim, parent_node, ".Spawn_Entity_On_Hit.EntityType_Baby_Explode_Amount", shooter)) {
                    CommandUtils.sendPlayerMessage(shooter, parent_node, ".Spawn_Entity_On_Hit.Message_Shooter", nameShooter, nameVic, flyTime, dmgTotal);
                    CommandUtils.sendPlayerMessage(victim, parent_node, ".Spawn_Entity_On_Hit.Message_Victim", nameShooter, nameVic, flyTime, dmgTotal);
                }

                if (victim instanceof Player) {
                    if (makeSpeak != null) {
                        ((Player) victim).chat(CommandUtils.variableParser(makeSpeak, nameShooter, nameVic, flyTime, dmgTotal));
                    }

                    if (makeRunCmd != null) {
                        CommandUtils.executeCommands(victim, parent_node, ".Extras.Make_Victim_Run_Commmand", nameShooter, nameVic, flyTime, dmgTotal, false);
                    }
                }

                if (runConsole != null) {
                    CommandUtils.executeCommands(shooter, parent_node, ".Extras.Run_Console_Command", nameShooter, nameVic, flyTime, dmgTotal, true);
                }

                if (!(victim instanceof Player) && victim.getHealth() <= (double) 0.0F && bonusDrops != null) {
                    String[] dropInfo = bonusDrops.split(",");

                    for (String drop : dropInfo) {
                        try {
                            shooter.getWorld().dropItemNaturally(victim.getLocation(), new ItemStack(MaterialManager.getMaterial(drop)));
                        } catch (IllegalArgumentException var42) {
                            this.printM("'" + drop + "' of weapon '" + parent_node + "' for 'Bonus_Drops' is not a valid item ID!");
                            break;
                        }
                    }
                }

            }
        }
    }

    public void setTempVulnerability(final LivingEntity ent) {
        final int maxNoDamageTicks = ent.getMaximumNoDamageTicks();
        ent.setMaximumNoDamageTicks(0);
        ent.setNoDamageTicks(0);
        if (!ent.hasMetadata("[CS] NHC")) {
            ent.setMetadata("[CS] NHC", new FixedMetadataValue(this, true));
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    ent.setMaximumNoDamageTicks(maxNoDamageTicks);
                    ent.setNoDamageTicks(0);
                    ent.removeMetadata("[CS] NHC", CSDirector.this.plugin);
                }
            }, 2L);
        }

    }

    @EventHandler
    // Обрабатывает столкновение снаряда с блоком или сущностью и поднимает связанные эффекты/события.
    public void onProjectileHit(ProjectileHitEvent event) {
        if ((event.getEntity() instanceof Arrow || event.getEntity() instanceof Egg || event.getEntity() instanceof Snowball) && event.getEntity().hasMetadata("projParentNode") && event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) event.getEntity().getShooter();
            Projectile objProj = event.getEntity();
            String parentNode = objProj.getMetadata("projParentNode").get(0).asString();
            Location destLoc = objProj.getLocation();
            objProj.removeMetadata(parentNode, this);
            boolean collided = event.getEntity().hasMetadata("Collided");
            boolean terrain = ConfigCache.getBoolean(parentNode + ".Particles.Particle_Terrain");
            boolean airstrike = ConfigCache.getBoolean(parentNode + ".Airstrikes.Enable");
            boolean zapEnable = ConfigCache.getBoolean(parentNode + ".Lightning.Enable");
            boolean zapNoDam = ConfigCache.getBoolean(parentNode + ".Lightning.No_Damage");
            boolean zapImpact = ConfigCache.getBoolean(parentNode + ".Lightning.On_Impact_With_Anything");
            boolean arrowImpact = ConfigCache.getBoolean(parentNode + ".Shooting.Remove_Arrows_On_Impact");
            boolean explodeImpact = ConfigCache.getBoolean(parentNode + ".Explosions.On_Impact_With_Anything");
            int actTime = ConfigCache.getInt(parentNode + ".Explosions.Projectile_Activation_Time");
            String breakBlocks = ConfigCache.getString(parentNode + ".Abilities.Break_Blocks");
            String[] blockList = breakBlocks == null ? null : breakBlocks.split("-");
            Block hitBlock = objProj.getLocation().getBlock();
            if (!collided) {
                double projSpeed = (double) ConfigCache.getInt(parentNode + ".Shooting.Projectile_Speed") * 0.1;
                if (projSpeed > (double) 256.0F) {
                    projSpeed = 256.0F;
                }

                for (double i = 0.0F; i <= projSpeed; ++i) {
                    Location finalLoc = objProj.getLocation();
                    Vector direction = objProj.getVelocity().normalize();
                    direction.multiply(i);
                    finalLoc.add(direction);
                    hitBlock = finalLoc.getBlock();
                    if (!WeaponHelperUtils.isAir(hitBlock.getType())) {
                        if (terrain) {
                            objProj.getWorld().playEffect(finalLoc, Effect.STEP_SOUND, hitBlock.getType());
                        }

                        if (blockList != null && blockList.length == 2) {
                            boolean passWhiteList = false;
                            boolean whiteList = Boolean.parseBoolean(blockList[0]);
                            String blockMat = hitBlock.getType().toString();
                            if (this.csminion.regionCheck(objProj, parentNode)) {
                                for (String compMat : blockList[1].split(",")) {
                                    boolean hasSecdat = compMat.contains("~");
                                    String[] secdat = hasSecdat ? compMat.split("~") : new String[]{"", ""};
                                    Material mat = MaterialManager.getMaterial(compMat);
                                    if (mat != null) {
                                        secdat[0] = mat.toString();
                                        compMat = mat.toString();
                                    }

                                    if (blockMat.equals(hasSecdat ? secdat[0] : compMat) && (!hasSecdat || hitBlock.getData() == Byte.parseByte(secdat[1]))) {
                                        if (!whiteList) {
                                            List<Block> brokenBlocks = new ArrayList<>();
                                            brokenBlocks.add(hitBlock);
                                            EntityExplodeEvent breakBlockEvent = EntityExplodeEventFactory.create(objProj, objProj.getLocation(), brokenBlocks, 0.0F);
                                            this.getServer().getPluginManager().callEvent(breakBlockEvent);
                                            hitBlock.setType(Material.AIR);
                                            break;
                                        }

                                        passWhiteList = true;
                                    }
                                }

                                if (whiteList && !passWhiteList) {
                                    List<Block> brokenBlocks = new ArrayList<>();
                                    brokenBlocks.add(hitBlock);
                                    EntityExplodeEvent breakBlockEvent = EntityExplodeEventFactory.create(objProj, objProj.getLocation(), brokenBlocks, 0.0F);
                                    this.getServer().getPluginManager().callEvent(breakBlockEvent);
                                    hitBlock.setType(Material.AIR);
                                }
                            }
                        }
                        break;
                    }

                    destLoc = finalLoc;
                }

                if (explodeImpact && objProj.getTicksLived() >= actTime) {
                    Entity tempOrb = objProj.getWorld().spawn(destLoc, ExperienceOrb.class);
                    this.projectileExplosion(tempOrb, parentNode, false, shooter, false, false, null, null, false, 0);
                    tempOrb.remove();
                }

                if (zapEnable && zapImpact) {
                    this.csminion.projectileLightning(destLoc, zapNoDam);
                }

                this.csminion.giveParticleEffects(null, parentNode, ".Particles.Particle_Impact_Anything", false, destLoc);
            }

            SoundUtils.playSoundEffects((Entity) null, parentNode, ".Hit_Events.Sounds_Impact", false, destLoc);
            this.csminion.giveParticleEffects(null, parentNode, ".Airstrikes.Particle_Call_Airstrike", false, destLoc);
            if (airstrike) {
                this.csminion.callAirstrike(event.getEntity(), parentNode, shooter);
            }

            if (arrowImpact && objProj.getType() == EntityType.ARROW) {
                objProj.remove();
            }

            WeaponHitBlockEvent blockHitEvent = new WeaponHitBlockEvent(shooter, objProj, parentNode, hitBlock, destLoc.getBlock());
            this.getServer().getPluginManager().callEvent(blockHitEvent);
        }

    }

    @EventHandler
    // Перехватывает взрывы CrackShot, чтобы применить правила урона и разрушения блоков.
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity boomer = event.getEntity();
        if (boomer instanceof TNTPrimed) {
            if (boomer.hasMetadata("CS_potex")) {
                String parent_node = boomer.getMetadata("CS_potex").get(0).asString();
                SoundUtils.playSoundEffects(boomer, parent_node, ".Explosions.Sounds_Explode", false, null);
            }

            if (boomer.hasMetadata("nullify") && event.blockList() != null) {
                event.blockList().clear();
            }

            if (MaterialManager.isSkullBlock(boomer.getLocation().getBlock()) && !boomer.hasMetadata("C4_Friendly")) {
                BlockState state = boomer.getLocation().getBlock().getState();
                if (state instanceof Skull) {
                    Skull skull;
                    try {
                        skull = (Skull) state;
                    } catch (ClassCastException var6) {
                        return;
                    }

                    if (skull.getOwner().contains("،")) {
                        boomer.getLocation().getBlock().removeMetadata("CS_transformers", this);
                        boomer.getLocation().getBlock().setType(Material.AIR);
                    }
                }
            }
        } else if ((boomer instanceof WitherSkull || boomer instanceof LargeFireball) && boomer.hasMetadata("projParentNode") && ((Projectile) boomer).getShooter() instanceof Player) {
            Player shooter = (Player) ((Projectile) boomer).getShooter();
            String parent_node = boomer.getMetadata("projParentNode").get(0).asString();
            if (boomer.getTicksLived() >= ConfigCache.getInt(parent_node + ".Explosions.Projectile_Activation_Time")) {
                this.projectileExplosion(boomer, parent_node, false, shooter, false, false, null, null, false, 0);
            }

            event.setCancelled(true);
        }

    }



// Основной движок выстрела: проверка боезапаса, события, спавн projectile и burst/recoil-логика.
    public void fireProjectile(final Player player, final String parentNode, final boolean leftClick) {
        int gunSlot = player.getInventory().getHeldItemSlot();
        int shootDelay = ConfigCache.getInt(parentNode + ".Shooting.Delay_Between_Shots");
        final int projAmount = ConfigCache.getInt(parentNode + ".Shooting.Projectile_Amount");
        final boolean ammoEnable = ConfigCache.getBoolean(parentNode + ".Ammo.Enable");
        final boolean oneTime = ConfigCache.getBoolean(parentNode + ".Extras.One_Time_Use");
        String deviceType = ConfigCache.getString(parentNode + ".Explosive_Devices.Device_Type");
        final String proType = ConfigCache.getString(parentNode + ".Shooting.Projectile_Type");
        ItemStack item = player.getInventory().getItemInHand();
        final boolean isFullyAuto = ConfigCache.getBoolean(parentNode + ".Fully_Automatic.Enable");
        int fireRate = ConfigCache.getInt(parentNode + ".Fully_Automatic.Fire_Rate");
        boolean burstEnable = ConfigCache.getBoolean(parentNode + ".Burstfire.Enable");
        int burstShots = ConfigCache.getInt(parentNode + ".Burstfire.Shots_Per_Burst");
        int burstDelay = ConfigCache.getInt(parentNode + ".Burstfire.Delay_Between_Shots_In_Burst");
        boolean meleeMode = ConfigCache.getBoolean(parentNode + ".Item_Information.Melee_Mode");
        boolean shootDisable = ConfigCache.getBoolean(parentNode + ".Shooting.Disable");
        final boolean reloadOn = ConfigCache.getBoolean(parentNode + ".Reload.Enable");
        final boolean dualWield = WeaponHelperUtils.isDualWield(player, parentNode, item);
        if (!shootDisable && !meleeMode) {
            Vector shiftVector = MathUtils.determinePosition(player, dualWield, leftClick);
            final Location projLoc = player.getEyeLocation().toVector().add(shiftVector.multiply(0.2)).toLocation(player.getWorld());
            final String actType = ConfigCache.getString(parentNode + ".Firearm_Action.Type");
            final boolean tweakyAction = actType != null && (actType.toLowerCase().contains("bolt") || actType.toLowerCase().contains("lever") || actType.toLowerCase().contains("pump"));
            if (!player.hasMetadata(parentNode + "shootDelay" + gunSlot + leftClick)) {
                if (!player.hasMetadata(parentNode + "noShooting" + gunSlot)) {
                    if (!player.hasMetadata("togglesnoShooting" + gunSlot)) {
                        if (oneTime && ammoEnable) {
                            player.sendMessage(this.heading + "For '" + parentNode + "' - the 'One_Time_Use' node is incompatible with weapons using the Ammo module.");
                        } else if (proType != null && (proType.equalsIgnoreCase("grenade") || proType.equalsIgnoreCase("flare")) && projAmount == 0) {
                            player.sendMessage(this.heading + "The weapon '" + parentNode + "' is missing a value for 'Projectile_Amount'.");
                        } else {
                            if (isFullyAuto) {
                                if (burstEnable) {
                                    player.sendMessage(this.heading + "The weapon '" + parentNode + "' is using Fully_Automatic and Burstfire at the same time. Pick one; you cannot enable both!");
                                    return;
                                }

                                if (shootDelay > 1) {
                                    player.sendMessage(this.heading + "For '" + parentNode + "' - the Fully_Automatic module can only be used if 'Delay_Between_Shots' is removed or set to a value no greater than 1.");
                                    return;
                                }

                                if (fireRate <= 0 || fireRate > 16) {
                                    player.sendMessage(this.heading + "The weapon '" + parentNode + "' has an invalid value for 'Fire_Rate'. The accepted values are 1 to 16.");
                                    return;
                                }
                            }

                            if (ItemUtils.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains("ᴿ")) {
                                if (AmmoUtils.getAmmoBetweenBrackets(player, parentNode, item) <= 0) {
                                    this.reloadAnimation(player, parentNode);
                                    return;
                                }

                                if (!dualWield) {
                                    this.terminateReload(player);
                                    this.removeInertReloadTag(player, 0, true);
                                } else {
                                    int[] ammoReading = AmmoUtils.grabDualAmmo(item, parentNode);
                                    if (ammoReading[0] > 0 && leftClick || ammoReading[1] > 0 && !leftClick) {
                                        this.terminateReload(player);
                                        this.removeInertReloadTag(player, 0, true);
                                    }
                                }
                            }

                            if (!player.hasMetadata(parentNode + "reloadShootDelay" + gunSlot)) {
                                if (!tweakyAction && (actType == null || !actType.equalsIgnoreCase("slide") || !item.getItemMeta().getDisplayName().contains("▫"))) {
                                    player.setMetadata(parentNode + "shootDelay" + gunSlot + leftClick, new FixedMetadataValue(this, true));
                                    this.csminion.tempVars(player, parentNode + "shootDelay" + gunSlot + leftClick, (long) shootDelay);
                                }

                                final String ammoInfo = ConfigCache.getString(parentNode + ".Ammo.Ammo_Item_ID");
                                final boolean ammoPerShot = ConfigCache.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
                                final double zoomAcc = ConfigCache.getDouble(parentNode + ".Scope.Zoom_Bullet_Spread");
                                final boolean sneakOn = ConfigCache.getBoolean(parentNode + ".Sneak.Enable");
                                boolean sneakToShoot = ConfigCache.getBoolean(parentNode + ".Sneak.Sneak_Before_Shooting");
                                final boolean sneakNoRec = ConfigCache.getBoolean(parentNode + ".Sneak.No_Recoil");
                                final double sneakAcc = ConfigCache.getDouble(parentNode + ".Sneak.Bullet_Spread");
                                final boolean exploDevs = ConfigCache.getBoolean(parentNode + ".Explosive_Devices.Enable");
                                boolean takeAmmo = ConfigCache.getBoolean(parentNode + ".Reload.Take_Ammo_On_Reload");
                                String dragRemInfo = ConfigCache.getString(parentNode + ".Shooting.Removal_Or_Drag_Delay");
                                final String[] dragRem = dragRemInfo == null ? null : dragRemInfo.split("-");
                                if (dragRem != null) {
                                    try {
                                        Integer.valueOf(dragRem[0]);
                                    } catch (NumberFormatException var51) {
                                        player.sendMessage(this.heading + "For the weapon '" + parentNode + "', the 'Removal_Or_Drag_Delay' node is incorrectly configured.");
                                        return;
                                    }
                                }

                                if (ConfigCache.getBoolean(parentNode + ".Ammo.Take_Ammo_On_Reload")) {
                                    player.sendMessage(this.heading + "For the weapon '" + parentNode + "', the Ammo module does not support the 'Take_Ammo_On_Reload' node. Did you mean to place it in the Reload module?");
                                } else {
                                    if (ammoEnable) {
                                        if (!takeAmmo && !ammoPerShot) {
                                            player.sendMessage(this.heading + "The weapon '" + parentNode + "' has enabled the Ammo module, but at least one of the following nodes need to be set to true: Take_Ammo_On_Reload, Take_Ammo_Per_Shot.");
                                            return;
                                        }

                                        if (!this.csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {
                                            boolean isPumpOrBolt = actType != null && !actType.equalsIgnoreCase("pump") && !actType.equalsIgnoreCase("bolt");
                                            boolean hasLoadedChamber = item.getItemMeta().getDisplayName().contains("▪ «");
                                            if (ammoPerShot || takeAmmo && AmmoUtils.getAmmoBetweenBrackets(player, parentNode, item) == 0 && (isPumpOrBolt || !hasLoadedChamber)) {
                                                SoundUtils.playSoundEffects(player, parentNode, ".Ammo.Sounds_Shoot_With_No_Ammo", false, (Location) null);
                                                return;
                                            }
                                        }
                                    }

                                    if (!sneakToShoot || player.isSneaking() && !WeaponHelperUtils.isAir(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType())) {
                                        if (!this.checkBoltPosition(player, parentNode)) {
                                            if (!burstEnable) {
                                                burstShots = 1;
                                            }

                                            if (isFullyAuto) {
                                                burstShots = 5;
                                                burstDelay = 1;
                                            }

                                            final double projSpeed = (double) ConfigCache.getInt(parentNode + ".Shooting.Projectile_Speed") * 0.1;
                                            final boolean setOnFire = ConfigCache.getBoolean(parentNode + ".Shooting.Projectile_Flames");
                                            final boolean noBulletDrop = ConfigCache.getBoolean(parentNode + ".Shooting.Remove_Bullet_Drop");
                                            if (!ConfigCache.getBoolean(parentNode + ".Scope.Zoom_Before_Shooting") || player.hasMetadata("ironsights")) {
                                                int shootReloadBuffer = ConfigCache.getInt(parentNode + ".Reload.Shoot_Reload_Buffer");
                                                if (shootReloadBuffer > 0) {
                                                    Map<Integer, Long> lastShot = (Map) this.last_shot_list.get(player.getName());
                                                    if (lastShot == null) {
                                                        lastShot = new HashMap();
                                                        this.last_shot_list.put(player.getName(), lastShot);
                                                    }

                                                    lastShot.put(gunSlot, System.currentTimeMillis());
                                                }

                                                int burstStart = 0;
                                                if (isFullyAuto) {
                                                    WeaponFireRateEvent event = new WeaponFireRateEvent(player, parentNode, item, fireRate);
                                                    this.getServer().getPluginManager().callEvent(event);
                                                    fireRate = event.getFireRate();
                                                    String playerName = player.getName();
                                                    if (!this.rpm_ticks.containsKey(playerName)) {
                                                        this.rpm_ticks.put(playerName, 1);
                                                    }

                                                    if (!this.rpm_shots.containsKey(playerName)) {
                                                        this.rpm_shots.put(playerName, 0);
                                                    }

                                                    burstStart = (Integer) this.rpm_shots.get(playerName);
                                                    this.rpm_shots.put(playerName, 5);
                                                }

                                                final int fireRateFinal = fireRate;
                                                final int itemSlot = player.getInventory().getHeldItemSlot();

                                                for (int burst = burstStart; burst < burstShots; ++burst) {
                                                    final boolean isLastShot = burst >= burstShots - 1;
                                                    int task_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                                        public void run() {
                                                            if (isFullyAuto) {
                                                                String playerName = player.getName();
                                                                int shotsLeft = (Integer) CSDirector.this.rpm_shots.get(playerName) - 1;
                                                                CSDirector.this.rpm_shots.put(playerName, shotsLeft);
                                                                int tick = (Integer) CSDirector.this.rpm_ticks.get(playerName);
                                                                CSDirector.this.rpm_ticks.put(playerName, tick >= 20 ? 1 : tick + 1);
                                                                if (shotsLeft == 0) {
                                                                    CSDirector.this.burst_task_IDs.remove(playerName);
                                                                }

                                                                if (!WeaponHelperUtils.isValid(tick, fireRateFinal)) {
                                                                    return;
                                                                }
                                                            } else if (isLastShot) {
                                                                CSDirector.this.burst_task_IDs.remove(player.getName());
                                                            }

                                                            ItemStack item = player.getInventory().getItemInHand();
                                                            if (!oneTime) {
                                                                if (CSDirector.this.switchedTheItem(player, parentNode) || itemSlot != player.getInventory().getHeldItemSlot()) {
                                                                    CSDirector.this.unscopePlayer(player);
                                                                    CSDirector.this.terminateAllBursts(player);
                                                                    return;
                                                                }

                                                                boolean normalAction = false;
                                                                if (actType == null) {
                                                                    normalAction = true;
                                                                    String attachType = WeaponAttachmentUtils.getAttachment(parentNode, item)[0];
                                                                    String filter = item.getItemMeta().getDisplayName();
                                                                    if (attachType == null || !attachType.equalsIgnoreCase("accessory")) {
                                                                        if (filter.contains("▪ «")) {
                                                                            CSDirector.this.csminion.setItemName(item, filter.replaceAll("▪ «", "«"));
                                                                        } else if (filter.contains("▫ «")) {
                                                                            CSDirector.this.csminion.setItemName(item, filter.replaceAll("▫ «", "«"));
                                                                        } else if (filter.contains("˗ «")) {
                                                                            CSDirector.this.csminion.setItemName(item, filter.replaceAll("˗ «", "«"));
                                                                        }
                                                                    }
                                                                } else if (!tweakyAction) {
                                                                    normalAction = true;
                                                                }

                                                                if (ammoEnable && ammoPerShot && !CSDirector.this.csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {
                                                                    CSDirector.this.burst_task_IDs.remove(player.getName());
                                                                    return;
                                                                }

                                                                if (reloadOn) {
                                                                    if (item.getItemMeta().getDisplayName().contains("ᴿ")) {
                                                                        return;
                                                                    }

                                                                    int detectedAmmo = AmmoUtils.getAmmoBetweenBrackets(player, parentNode, item);
                                                                    if (normalAction) {
                                                                        if (detectedAmmo <= 0) {
                                                                            CSDirector.this.reloadAnimation(player, parentNode);
                                                                            return;
                                                                        }

                                                                        if (!dualWield) {
                                                                            CSDirector.this.ammoOperation(player, parentNode, detectedAmmo, item);
                                                                        } else if (!CSDirector.this.ammoSpecOps(player, parentNode, detectedAmmo, item, leftClick)) {
                                                                            return;
                                                                        }
                                                                    }
                                                                } else {
                                                                    String itemName = item.getItemMeta().getDisplayName();
                                                                    if (itemName.contains("«") && !itemName.contains(String.valueOf('×')) && !exploDevs) {
                                                                        CSDirector.this.csminion.replaceBrackets(item, String.valueOf('×'), parentNode);
                                                                    }
                                                                }
                                                            }

                                                            double bulletSpread = ConfigCache.getDouble(parentNode + ".Shooting.Bullet_Spread");
                                                            if (player.isSneaking() && sneakOn) {
                                                                bulletSpread = sneakAcc;
                                                            }

                                                            if (player.hasMetadata("ironsights")) {
                                                                bulletSpread = zoomAcc;
                                                            }

                                                            if (bulletSpread == (double) 0.0F) {
                                                                bulletSpread = 0.1;
                                                            }

                                                            boolean noVertRecoil = ConfigCache.getBoolean(parentNode + ".Abilities.No_Vertical_Recoil");
                                                            boolean jetPack = ConfigCache.getBoolean(parentNode + ".Abilities.Jetpack_Mode");
                                                            double recoilAmount = (double) ConfigCache.getInt(parentNode + ".Shooting.Recoil_Amount") * 0.1;
                                                            if (recoilAmount != (double) 0.0F && (!sneakOn || !sneakNoRec || !player.isSneaking())) {
                                                                if (!jetPack) {
                                                                    Vector velToAdd = player.getLocation().getDirection().multiply(-recoilAmount);
                                                                    if (noVertRecoil) {
                                                                        velToAdd.multiply(new Vector(1, 0, 1));
                                                                    }

                                                                    player.setVelocity(velToAdd);
                                                                } else {
                                                                    player.setVelocity(new Vector((double) 0.0F, recoilAmount, (double) 0.0F));
                                                                }
                                                            }

                                                            boolean clearFall = ConfigCache.getBoolean(parentNode + ".Shooting.Reset_Fall_Distance");
                                                            if (clearFall) {
                                                                player.setFallDistance(0.0F);
                                                            }

                                                            CSDirector.this.csminion.giveParticleEffects(player, parentNode, ".Particles.Particle_Player_Shoot", true, (Location) null);
                                                            CSDirector.this.csminion.givePotionEffects(player, parentNode, ".Potion_Effects.Potion_Effect_Shooter", /*"shoot"*/PotionActivation.SHOOT);
                                                            CSDirector.this.csminion.displayFireworks(player, parentNode, ".Fireworks.Firework_Player_Shoot");
                                                            CSDirector.this.weaponCommandManager.runCommand(player, parentNode);
                                                            if (ConfigCache.getBoolean(parentNode + ".Abilities.Hurt_Effect")) {
                                                                player.playEffect(EntityEffect.HURT);
                                                            }

                                                            String projectile_type = ConfigCache.getString(parentNode + ".Shooting.Projectile_Type");
                                                            int timer = ConfigCache.getInt(parentNode + ".Explosions.Explosion_Delay");
                                                            boolean airstrike = ConfigCache.getBoolean(parentNode + ".Airstrikes.Enable");
                                                            if (airstrike) {
                                                                timer = ConfigCache.getInt(parentNode + ".Airstrikes.Flare_Activation_Delay");
                                                            }

                                                            String soundsShoot = ConfigCache.getString(parentNode + ".Shooting.Sounds_Shoot");
                                                            WeaponPreShootEvent event = new WeaponPreShootEvent(player, parentNode, soundsShoot, bulletSpread, leftClick);
                                                            CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
                                                            SoundUtils.playSoundEffects(player, parentNode, (String) null, false, (Location) null, event.getSounds());
                                                            if (!event.isCancelled()) {
                                                                bulletSpread = event.getBulletSpread();
                                                                final ProjectileSubtypeData projectileSubtypeData;

                                                                try {
                                                                    projectileSubtypeData = ProjectileSubtypeParser.parseProjectileSubtype(parentNode);
                                                                } catch (IllegalArgumentException ex) {
                                                                    player.sendMessage(CSDirector.this.heading + ex.getMessage());
                                                                    return;
                                                                }

                                                                for (int i = 0; i < projAmount; ++i) {
                                                                    Random r = new Random();
                                                                    double yaw = Math.toRadians((double) (-player.getLocation().getYaw() - 90.0F));
                                                                    double pitch = Math.toRadians((double) (-player.getLocation().getPitch()));
                                                                    double[] spread = new double[]{(double) 1.0F, (double) 1.0F, (double) 1.0F};

                                                                    for (int t = 0; t < 3; ++t) {
                                                                        spread[t] = (r.nextDouble() - r.nextDouble()) * bulletSpread * 0.1;
                                                                    }

                                                                    double x = Math.cos(pitch) * Math.cos(yaw) + spread[0];
                                                                    double y = Math.sin(pitch) + spread[1];
                                                                    double z = -Math.sin(yaw) * Math.cos(pitch) + spread[2];
                                                                    Vector dirVel = new Vector(x, y, z);
                                                                    if (proType == null || !proType.equalsIgnoreCase("grenade") && !proType.equalsIgnoreCase("flare")) {
                                                                        if (proType.equalsIgnoreCase("energy")) {
                                                                            PermissionAttachment attachment = player.addAttachment(CSDirector.this.plugin);
                                                                            attachment.setPermission("nocheatplus", true);
                                                                            attachment.setPermission("anticheat.check.exempt", true);
                                                                            if (projectileSubtypeData == null) {
                                                                                player.sendMessage(CSDirector.this.heading + "The weapon '" + parentNode + "' does not have a value for 'Projectile_Subtype'.");
                                                                                return;
                                                                            }

                                                                            if (projectileSubtypeData.getType() != ProjectileSubtypeType.NUMERIC) {
                                                                                player.sendMessage(CSDirector.this.heading + "The value provided for 'Projectile_Subtype' of the weapon '" + parentNode + "' has an incorrect format.");
                                                                                return;
                                                                            }

                                                                            int wallLimit = projectileSubtypeData.getWallLimit();
                                                                            int hitCount = 0;
                                                                            int wallCount = 0;
                                                                            int range = projectileSubtypeData.getRange();
                                                                            int hitLimit = projectileSubtypeData.getHitLimit();
                                                                            double radius = projectileSubtypeData.getRadius();

                                                                            Set<Block> hitBlocks = new HashSet();
                                                                            Set<Integer> hitMobs = new HashSet();
                                                                            Vector vecShift = dirVel.normalize().multiply(radius);
                                                                            Location locStart = player.getEyeLocation();

                                                                            label238:
                                                                            for (double k = (double) 0.0F; k < (double) range; k += radius) {
                                                                                locStart.add(vecShift);
                                                                                Block hitBlock = locStart.getBlock();
                                                                                if (!WeaponHelperUtils.isAir(hitBlock.getType())) {
                                                                                    if (wallLimit != -1 && !hitBlocks.contains(hitBlock)) {
                                                                                        ++wallCount;
                                                                                        if (wallCount > wallLimit) {
                                                                                            break;
                                                                                        }

                                                                                        hitBlocks.add(hitBlock);
                                                                                    }
                                                                                } else {
                                                                                    FallingBlock tempEnt = player.getWorld().spawnFallingBlock(locStart, Material.AIR, (byte) 0);

                                                                                    for (Entity ent : tempEnt.getNearbyEntities(radius, radius, radius)) {
                                                                                        if (ent instanceof LivingEntity && ent != player && !hitMobs.contains(ent.getEntityId()) && !ent.isDead()) {
                                                                                            if (ent instanceof Player) {
                                                                                                ent.setMetadata("CS_Energy", new FixedMetadataValue(CSDirector.this.plugin, parentNode));
                                                                                                ((LivingEntity) ent).damage((double) 0.0F, player);
                                                                                            } else {
                                                                                                CSDirector.this.dealDamage(player, (LivingEntity) ent, (EntityDamageByEntityEvent) null, parentNode);
                                                                                            }

                                                                                            hitMobs.add(ent.getEntityId());
                                                                                            ++hitCount;
                                                                                            if (hitLimit != 0 && hitCount >= hitLimit) {
                                                                                                break label238;
                                                                                            }
                                                                                        }
                                                                                    }

                                                                                    tempEnt.remove();
                                                                                }
                                                                            }

                                                                            ProjectileUtils.callShootEvent(player, (Entity) null, parentNode);
                                                                            SoundUtils.playSoundEffects(player, parentNode, ".Shooting.Sounds_Projectile", false, (Location) null);
                                                                            player.removeAttachment(attachment);
                                                                        } else if (proType.equalsIgnoreCase("splash")) {
                                                                            ThrownPotion splashPot = (ThrownPotion) player.getWorld().spawn(projLoc, ThrownPotion.class);
                                                                            ItemStack potType = projectileSubtypeData != null && projectileSubtypeData.getType() == ProjectileSubtypeType.ITEM ? projectileSubtypeData.getItem() : null;
                                                                            if (potType != null) {
                                                                                try {
                                                                                    splashPot.setItem(potType);
                                                                                } catch (
                                                                                        IllegalArgumentException var49) {
                                                                                    player.sendMessage(CSDirector.this.heading + "The value for 'Projectile_Subtype' of weapon '" + parentNode + "' is not a splash potion!");
                                                                                }
                                                                            }

                                                                            if (setOnFire) {
                                                                                splashPot.setFireTicks(6000);
                                                                            }

                                                                            if (noBulletDrop) {
                                                                                ProjectileUtils.noArcInArchery(splashPot, dirVel.multiply(projSpeed));
                                                                            }

                                                                            splashPot.setShooter(player);
                                                                            splashPot.setMetadata("projParentNode", new FixedMetadataValue(CSDirector.this.plugin, parentNode));
                                                                            splashPot.setVelocity(dirVel.multiply(projSpeed));
                                                                            ProjectileUtils.callShootEvent(player, splashPot, parentNode);
                                                                            if (dragRem != null) {
                                                                                ProjectileUtils.prepareTermination(splashPot, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
                                                                            }
                                                                        } else {
                                                                            Projectile snowball;
                                                                            if (projectile_type.equalsIgnoreCase("arrow")) {
                                                                                snowball = (Projectile) player.getWorld().spawnEntity(projLoc, EntityType.ARROW);
                                                                            } else if (projectile_type.equalsIgnoreCase("egg")) {
                                                                                snowball = (Projectile) player.getWorld().spawnEntity(projLoc, EntityType.EGG);
                                                                                snowball.setMetadata("CS_Hardboiled", new FixedMetadataValue(CSDirector.this.plugin, true));
                                                                            } else if (projectile_type.equalsIgnoreCase("fireball")) {
                                                                                snowball = player.launchProjectile(LargeFireball.class);
                                                                                if (projectileSubtypeData != null && projectileSubtypeData.getType() == ProjectileSubtypeType.BOOLEAN && Boolean.TRUE.equals(projectileSubtypeData.getFlag())) {
                                                                                    snowball.setMetadata("CS_NoDeflect", new FixedMetadataValue(CSDirector.this.plugin, true));
                                                                                }
                                                                            } else if (projectile_type.equalsIgnoreCase("witherskull")) {
                                                                                snowball = player.launchProjectile(WitherSkull.class);
                                                                            } else {
                                                                                snowball = (Projectile) player.getWorld().spawnEntity(projLoc, EntityType.SNOWBALL);
                                                                            }

                                                                            if (setOnFire) {
                                                                                snowball.setFireTicks(6000);
                                                                            }

                                                                            if (noBulletDrop) {
                                                                                ProjectileUtils.noArcInArchery(snowball, dirVel.multiply(projSpeed));
                                                                            }

                                                                            snowball.setShooter(player);
                                                                            snowball.setVelocity(dirVel.multiply(projSpeed));
                                                                            snowball.setMetadata("projParentNode", new FixedMetadataValue(CSDirector.this.plugin, parentNode));
                                                                            ProjectileUtils.callShootEvent(player, snowball, parentNode);
                                                                            SoundUtils.playSoundEffects(snowball, parentNode, ".Shooting.Sounds_Projectile", false, (Location) null);
                                                                            if (dragRem != null) {
                                                                                ProjectileUtils.prepareTermination(snowball, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
                                                                            }
                                                                        }
                                                                    } else {
                                                                        CSDirector.this.launchGrenade(player, parentNode, timer, dirVel.multiply(projSpeed), (Location) null, 0);
                                                                    }
                                                                }

                                                            }
                                                        }
                                                    }, Long.valueOf((long) (burstDelay * burst)) + 1L);
                                                    if (oneTime && burst == 0 && (deviceType == null || !deviceType.equalsIgnoreCase("remote") && !deviceType.equalsIgnoreCase("trap"))) {
                                                        this.csminion.oneTime(player);
                                                    }

                                                    String user = player.getName();
                                                    Collection<Integer> values = (Collection) this.burst_task_IDs.get(user);
                                                    if (values == null) {
                                                        values = new ArrayList();
                                                        this.burst_task_IDs.put(user, values);
                                                    }

                                                    values.add(task_ID);
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void reloadAnimation(final Player player, final String parent_node, boolean... reloadStart) {
        if (ConfigCache.getBoolean(parent_node + ".Reload.Enable") && !player.hasMetadata("markOfTheReload")) {
            String playerName = player.getName();
            if (this.delayed_reload_IDs.containsKey(playerName)) {
                Bukkit.getScheduler().cancelTask((Integer) this.delayed_reload_IDs.get(playerName));
                this.delayed_reload_IDs.remove(playerName);
            }

            int relDuration = ConfigCache.getInt(parent_node + ".Reload.Reload_Duration");
            ItemStack held = player.getItemInHand();
            boolean isStart = reloadStart.length == 0;
            final boolean takeAsMag = ConfigCache.getBoolean(parent_node + ".Reload.Take_Ammo_As_Magazine");
            final boolean takeAmmo = ConfigCache.getBoolean(parent_node + ".Reload.Take_Ammo_On_Reload");
            final boolean reloadIndie = ConfigCache.getBoolean(parent_node + ".Reload.Reload_Bullets_Individually");
            final boolean ammoEnable = ConfigCache.getBoolean(parent_node + ".Ammo.Enable");
            final String ammoInfo = ConfigCache.getString(parent_node + ".Ammo.Ammo_Item_ID");
            int openTime = ConfigCache.getInt(parent_node + ".Firearm_Action.Open_Duration");
            final int closeTime = ConfigCache.getInt(parent_node + ".Firearm_Action.Close_Duration") + ConfigCache.getInt(parent_node + ".Firearm_Action.Reload_Close_Delay");
            boolean akimboSingleReload = false;
            String reloadSound = ".Reload.Sounds_Reloading";
            final boolean dualWield = WeaponHelperUtils.isDualWield(player, parent_node, held);
            final int reloadAmt = dualWield ? this.getReloadAmount(player, parent_node, held) * 2 : this.getReloadAmount(player, parent_node, held);
            final String replacer = dualWield ? reloadAmt / 2 + " | " + reloadAmt / 2 : String.valueOf(reloadAmt);
            String actionType = dualWield ? null : ConfigCache.getString(parent_node + ".Firearm_Action.Type");
            if (reloadAmt <= 0) {
                player.sendMessage(this.heading + "The weapon '" + parent_node + "' is using the Reload module, but is missing a valid value for 'Reload_Amount'.");
            } else if (ConfigCache.getBoolean(parent_node + ".Reload.Destroy_When_Empty") && held != null && held.getType() != Material.AIR && held.hasItemMeta()) {
                if (AmmoUtils.getAmmoBetweenBrackets(player, parent_node, held) == 0) {
                    boolean validAction = actionType == null || actionType.equalsIgnoreCase("slide") || actionType.equalsIgnoreCase("break") || actionType.equalsIgnoreCase("revolver");
                    if (validAction || !held.getItemMeta().getDisplayName().contains("▪")) {
                        player.setItemInHand((ItemStack) null);
                    }
                }

            } else if (ConfigCache.getBoolean("Merged_Reload.Disable") && held.getAmount() > 1) {
                String deniedMsg = ConfigCache.getString("Merged_Reload.Message_Denied");
                if (deniedMsg != null) {
                    player.sendMessage(deniedMsg);
                }

                SoundUtils.playSoundEffects(player, "Merged_Reload", "Sounds_Denied", false, (Location) null);
            } else {
                boolean boltAct = false;
                final boolean pumpAct = actionType != null && actionType.equalsIgnoreCase("pump");
                boolean breakAct;
                boolean slide = false;
                if (actionType != null) {
                    if (!actionType.equalsIgnoreCase("break") && !actionType.equalsIgnoreCase("revolver")) {
                        breakAct = false;
                        if (actionType.equalsIgnoreCase("slide")) {
                            slide = true;
                        } else if (actionType.equalsIgnoreCase("bolt") || actionType.equalsIgnoreCase("lever")) {
                            boltAct = true;
                        }
                    } else {
                        breakAct = true;
                    }
                } else {
                    breakAct = false;
                }

                boolean isSwitched = this.switchedTheItem(player, parent_node);
                boolean isOutOfAmmo = takeAmmo && ammoEnable && !this.csminion.containsItemStack(player, ammoInfo, 1, parent_node);
                if (!isSwitched && !isOutOfAmmo) {
                    if (dualWield) {
                        int[] ammoReading = AmmoUtils.grabDualAmmo(held, parent_node);
                        if (ammoReading[0] + ammoReading[1] >= reloadAmt) {
                            player.removeMetadata("markOfTheReload", this);
                            return;
                        }

                        boolean oneIsFull = ammoReading[0] == reloadAmt / 2 || ammoReading[1] == reloadAmt / 2;
                        boolean oneAmmoOnly = takeAmmo && ammoEnable && this.csminion.countItemStacks(player, ammoInfo, parent_node) == 1;
                        if (!reloadIndie && (oneIsFull || oneAmmoOnly)) {
                            relDuration = ConfigCache.getInt(parent_node + ".Reload.Dual_Wield.Single_Reload_Duration");
                            reloadSound = ".Reload.Dual_Wield.Sounds_Single_Reload";
                            akimboSingleReload = true;
                        }
                    } else {
                        String attachType = WeaponAttachmentUtils.getAttachment(parent_node, held)[0];
                        String displayName = held.getItemMeta().getDisplayName();
                        boolean isAccessory = attachType != null && attachType.equalsIgnoreCase("accessory");
                        boolean boltFull = boltAct && displayName.contains("▪ «" + (reloadAmt - 1)) && !isAccessory;
                        if (boltFull) {
                            player.removeMetadata("markOfTheReload", this);
                            return;
                        }

                        if (displayName.contains("«" + reloadAmt + "»") || isAccessory && displayName.contains(reloadAmt + "»") || attachType != null && attachType.equalsIgnoreCase("main") && displayName.contains("«" + reloadAmt)) {
                            if (breakAct) {
                                this.checkBoltPosition(player, parent_node);
                            } else if (!displayName.contains("▪")) {
                                this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
                            }

                            player.removeMetadata("markOfTheReload", this);
                            return;
                        }

                        if (slide && displayName.contains("▫") && openTime > 0) {
                            this.correctBoltPosition(player, parent_node, true, openTime, true, false, false, false);
                            return;
                        }

                        if (!pumpAct && !slide && !isAccessory) {
                            if (!breakAct && (displayName.contains("▪") || displayName.contains("▫"))) {
                                this.correctBoltPosition(player, parent_node, true, openTime, true, false, false, false);
                                return;
                            }

                            if (displayName.contains("▪")) {
                                this.correctBoltPosition(player, parent_node, true, openTime, true, false, false, false);
                                return;
                            }
                        }
                    }

                    this.terminateReload(player);
                    this.removeInertReloadTag(player, 0, true);
                    this.unscopePlayer(player);
                    player.setMetadata("markOfTheReload", new FixedMetadataValue(this, true));
                    this.terminateAllBursts(player);
                    this.clearShootDelayMetadata(player, parent_node, player.getInventory().getHeldItemSlot());
                    if (!held.getItemMeta().getDisplayName().contains("ᴿ")) {
                        this.csminion.setItemName(held, held.getItemMeta().getDisplayName() + 'ᴿ');
                    }

                    if (reloadIndie && isStart) {
                        relDuration += ConfigCache.getInt(parent_node + ".Reload.First_Reload_Delay");
                    }

                    int shootReloadBuffer = ConfigCache.getInt(parent_node + ".Reload.Shoot_Reload_Buffer");
                    if (shootReloadBuffer > 0) {
                        Map<Integer, Long> map = (Map) this.last_shot_list.get(playerName);
                        if (map != null) {
                            Long lastShot = (Long) map.get(player.getInventory().getHeldItemSlot());
                            if (lastShot != null) {
                                int ticksPassed = (int) ((System.currentTimeMillis() - lastShot) / 50L);
                                int ticksToWait = shootReloadBuffer - ticksPassed;
                                if (ticksToWait > 0) {
                                    relDuration += ticksToWait;
                                }
                            }
                        }
                    }

                    WeaponReloadEvent event = new WeaponReloadEvent(player, parent_node, ConfigCache.getString(parent_node + reloadSound), relDuration);
                    this.plugin.getServer().getPluginManager().callEvent(event);
                    final String soundsReload = event.getSounds();
                    relDuration = event.getReloadDuration();
                    if (event.getReloadSpeed() != (double) 1.0F) {
                        double reloadSpeed = event.getReloadSpeed();
                        relDuration = (int) ((double) relDuration * reloadSpeed);
                        if (!reloadIndie) {
                            SoundUtils.playSoundEffectsScaled(player, parent_node, (String) null, true, reloadSpeed, soundsReload);
                        }
                    } else if (!reloadIndie) {
                        SoundUtils.playSoundEffects(player, parent_node, (String) null, true, (Location) null, soundsReload);
                    }

                    final int reloadShootDelay = akimboSingleReload ? ConfigCache.getInt(parent_node + ".Reload.Dual_Wield.Single_Reload_Shoot_Delay") : ConfigCache.getInt(parent_node + ".Reload.Reload_Shoot_Delay");
                    int task_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                        public void run() {
                            if (takeAmmo && ammoEnable && !CSDirector.this.csminion.containsItemStack(player, ammoInfo, 1, parent_node)) {
                                CSDirector.this.removeInertReloadTag(player, 0, true);
                            } else {
                                CSDirector.this.terminateReload(player);
                                if (!CSDirector.this.switchedTheItem(player, parent_node)) {
                                    ItemStack item = player.getInventory().getItemInHand();
                                    if (item.getItemMeta().getDisplayName().contains("ᴿ")) {
                                        CSDirector.this.csminion.givePotionEffects(player, parent_node, ".Potion_Effects.Potion_Effect_Shooter", /*"reload"*/ PotionActivation.RELOAD);
                                        CSDirector.this.removeInertReloadTag(player, 0, true);
                                        CSDirector.this.clearShootDelayMetadata(player, parent_node, player.getInventory().getHeldItemSlot());
                                        int currentAmmo = AmmoUtils.getAmmoBetweenBrackets(player, parent_node, item);
                                        if (takeAmmo && ammoEnable) {
                                            if (reloadIndie) {
                                                if (!dualWield) {
                                                    ++currentAmmo;
                                                    CSDirector.this.csminion.replaceBrackets(item, String.valueOf(currentAmmo), parent_node);
                                                } else {
                                                    int[] ammoReading = AmmoUtils.grabDualAmmo(item, parent_node);
                                                    int leftGun = ammoReading[0];
                                                    int rightGun = ammoReading[1];
                                                    if (leftGun != reloadAmt / 2 && leftGun <= rightGun) {
                                                        if (rightGun == reloadAmt / 2 || rightGun > leftGun || leftGun == rightGun) {
                                                            ++leftGun;
                                                        }
                                                    } else {
                                                        ++rightGun;
                                                    }

                                                    CSDirector.this.csminion.replaceBrackets(item, String.valueOf(leftGun + " | " + rightGun), parent_node);
                                                }

                                                CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
                                                SoundUtils.playSoundEffects(player, parent_node, (String) null, false, (Location) null, soundsReload);
                                                CSDirector.this.csminion.removeNamedItem(player, ammoInfo, 1, parent_node, false);
                                                WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
                                                CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
                                                CSDirector.this.reloadAnimation(player, parent_node, false);
                                                return;
                                            }

                                            if (!takeAsMag) {
                                                int invAmmo = CSDirector.this.csminion.countItemStacks(player, ammoInfo, parent_node);
                                                int fillAmt = reloadAmt - currentAmmo;
                                                currentAmmo += invAmmo;
                                                if (currentAmmo > reloadAmt) {
                                                    currentAmmo = reloadAmt;
                                                }

                                                if (!dualWield) {
                                                    CSDirector.this.csminion.replaceBrackets(item, String.valueOf(currentAmmo), parent_node);
                                                } else if (currentAmmo < reloadAmt) {
                                                    int[] ammoReading = AmmoUtils.grabDualAmmo(item, parent_node);
                                                    int leftGun = ammoReading[0];

                                                    int rightGun;
                                                    for (rightGun = ammoReading[1]; invAmmo > 0; --invAmmo) {
                                                        if (leftGun != reloadAmt / 2 && leftGun <= rightGun) {
                                                            if (rightGun == reloadAmt / 2 || rightGun > leftGun || leftGun == rightGun) {
                                                                ++leftGun;
                                                            }
                                                        } else {
                                                            ++rightGun;
                                                        }
                                                    }

                                                    CSDirector.this.csminion.replaceBrackets(item, String.valueOf(leftGun + " | " + rightGun), parent_node);
                                                } else {
                                                    CSDirector.this.csminion.replaceBrackets(item, String.valueOf(replacer), parent_node);
                                                }

                                                CSDirector.this.csminion.removeNamedItem(player, ammoInfo, fillAmt, parent_node, false);
                                            } else if (!dualWield) {
                                                CSDirector.this.csminion.replaceBrackets(item, String.valueOf(replacer), parent_node);
                                                CSDirector.this.csminion.removeNamedItem(player, ammoInfo, 1, parent_node, false);
                                            } else {
                                                int invAmmo = CSDirector.this.csminion.countItemStacks(player, ammoInfo, parent_node);
                                                int[] ammoReading = AmmoUtils.grabDualAmmo(item, parent_node);
                                                int amtToRemove = 0;

                                                for (int i = 0; i < 2; ++i) {
                                                    if (ammoReading[i] != reloadAmt / 2 && invAmmo > 0) {
                                                        ammoReading[i] = reloadAmt / 2;
                                                        ++amtToRemove;
                                                        --invAmmo;
                                                    }
                                                }

                                                CSDirector.this.csminion.replaceBrackets(item, String.valueOf(ammoReading[0]) + " | " + ammoReading[1], parent_node);
                                                CSDirector.this.csminion.removeNamedItem(player, ammoInfo, amtToRemove, parent_node, false);
                                            }

                                            CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
                                            if (breakAct) {
                                                CSDirector.this.checkBoltPosition(player, parent_node);
                                            } else if (!item.getItemMeta().getDisplayName().contains("▪ «")) {
                                                CSDirector.this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
                                            }

                                            CSDirector.this.removeInertReloadTag(player, 0, true);
                                            WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
                                            CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
                                        } else {
                                            if (reloadIndie) {
                                                if (!dualWield) {
                                                    ++currentAmmo;
                                                    CSDirector.this.csminion.replaceBrackets(item, String.valueOf(currentAmmo), parent_node);
                                                } else {
                                                    int[] ammoReading = AmmoUtils.grabDualAmmo(item, parent_node);
                                                    int leftGun = ammoReading[0];
                                                    int rightGun = ammoReading[1];
                                                    if (leftGun != reloadAmt / 2 && leftGun <= rightGun) {
                                                        if (rightGun == reloadAmt / 2 || rightGun > leftGun || leftGun == rightGun) {
                                                            ++leftGun;
                                                        }
                                                    } else {
                                                        ++rightGun;
                                                    }

                                                    CSDirector.this.csminion.replaceBrackets(item, String.valueOf(leftGun + " | " + rightGun), parent_node);
                                                }

                                                CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
                                                SoundUtils.playSoundEffects(player, parent_node, (String) null, false, (Location) null, soundsReload);
                                                WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
                                                CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
                                                CSDirector.this.reloadAnimation(player, parent_node, false);
                                                return;
                                            }

                                            player.removeMetadata("markOfTheReload", CSDirector.this.plugin);
                                            CSDirector.this.reloadShootDelay(player, parent_node, player.getInventory().getHeldItemSlot(), reloadShootDelay);
                                            CSDirector.this.csminion.replaceBrackets(item, String.valueOf(replacer), parent_node);
                                            if (breakAct) {
                                                CSDirector.this.checkBoltPosition(player, parent_node);
                                            } else if (!item.getItemMeta().getDisplayName().contains("▪ «")) {
                                                CSDirector.this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
                                            }

                                            WeaponReloadCompleteEvent event = new WeaponReloadCompleteEvent(player, parent_node);
                                            CSDirector.this.plugin.getServer().getPluginManager().callEvent(event);
                                        }
                                    }

                                }
                            }
                        }
                    }, Long.valueOf((long) relDuration));
                    String user = player.getName();
                    Collection<Integer> values_reload = (Collection) this.global_reload_IDs.get(user);
                    if (values_reload == null) {
                        values_reload = new ArrayList();
                        this.global_reload_IDs.put(user, values_reload);
                    }

                    values_reload.add(task_ID);
                } else {
                    this.removeInertReloadTag(player, 0, true);
                    if (isOutOfAmmo) {
                        player.removeMetadata("markOfTheReload", this);
                        if (boltAct && !held.getItemMeta().getDisplayName().contains("▪") && !held.getItemMeta().getDisplayName().contains("«0")) {
                            this.correctBoltPosition(player, parent_node, false, closeTime, false, true, pumpAct, false);
                        }
                    }

                }
            }
        }
    }

    // Ставит временный запрет на стрельбу после reload/toggle/burst, чтобы не ломались тайминги.
    public void reloadShootDelay(final Player player, String parentNode, int gunSlot, int delay, String... customTag) {
        if (delay >= 1) {
            final String playerName = player.getName();
            Map<String, Integer> tagsAndDelays = this.delay_list.get(playerName);
            if (tagsAndDelays == null) {
                tagsAndDelays = new HashMap();
                this.delay_list.put(playerName, tagsAndDelays);
            }

            final String metadataTag = (customTag.length > 1 ? customTag[1] : parentNode) + (customTag.length > 0 ? customTag[0] : "reloadShootDelay") + gunSlot;
            Integer prevTaskID = tagsAndDelays.get(metadataTag);
            if (prevTaskID != null) {
                Bukkit.getScheduler().cancelTask(prevTaskID);
            }

            player.setMetadata(metadataTag, new FixedMetadataValue(this, true));
            int newTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    player.removeMetadata(metadataTag, CSDirector.this.plugin);
                    Map<String, Integer> tagsAndDelays = CSDirector.this.delay_list.get(playerName);
                    if (tagsAndDelays != null) {
                        tagsAndDelays.remove(metadataTag);
                    }

                }
            }, (long) delay);
            tagsAndDelays.put(metadataTag, newTaskID);
        }
    }

    // Создаёт взрывной пакет CrackShot: урон, блоки, шрапнель, дополнительные взрывы и вторичные эффекты.
    public void projectileExplosion(final Entity objProj, final String parent_node, boolean grenade, final Player player, final boolean landmine, final boolean rde, final Location loc, final Block c4, final boolean trap, final int cTimes) {
        if (ConfigCache.getBoolean(parent_node + ".Explosions.Enable") && (rde || this.csminion.regionCheck(objProj, parent_node))) {
            int delay = grenade ? 0 : ConfigCache.getInt(parent_node + ".Explosions.Explosion_Delay");
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    Location location = null;
                    World world = null;
                    if (!rde) {
                        world = objProj.getWorld();
                        location = objProj.getLocation().getBlock().getLocation().add(0.5F, 0.5F, 0.5F);
                        if (objProj instanceof WitherSkull || objProj instanceof LargeFireball) {
                            BlockIterator checker = new BlockIterator(world, objProj.getLocation().toVector(), objProj.getVelocity().normalize().multiply(-1), (double) 0.0F, 4);
                            Block block = null;

                            while (checker.hasNext()) {
                                block = checker.next();
                                if (WeaponHelperUtils.isAir(block.getType())) {
                                    location = block.getLocation().add(0.5F, 0.5F, 0.5F);
                                    break;
                                }
                            }
                        }

                        if (landmine) {
                            objProj.remove();
                        }
                    } else if (!trap) {
                        c4.removeMetadata("CS_transformers", CSDirector.this.plugin);
                        c4.setType(Material.AIR);
                        location = loc;
                        world = loc.getWorld();
                    } else {
                        c4.removeMetadata("CS_btrap", CSDirector.this.plugin);
                        location = c4.getRelative(BlockFace.UP).getLocation().add(0.5F, 0.5F, 0.5F);
                        world = c4.getLocation().getWorld();
                    }

                    boolean airstrike = ConfigCache.getBoolean(parent_node + ".Airstrikes.Enable");
                    boolean cEnable = ConfigCache.getBoolean(parent_node + ".Cluster_Bombs.Enable");
                    int cOfficialTimes = ConfigCache.getInt(parent_node + ".Cluster_Bombs.Number_Of_Splits");
                    if (cEnable && !airstrike && cTimes < cOfficialTimes) {
                        int cAmount = ConfigCache.getInt(parent_node + ".Cluster_Bombs.Number_Of_Bomblets");
                        int cSpeed = ConfigCache.getInt(parent_node + ".Cluster_Bombs.Speed_Of_Bomblets");
                        int timer = ConfigCache.getInt(parent_node + ".Cluster_Bombs.Delay_Before_Detonation");
                        Random r = new Random();
                        int totalAmount = (int) Math.pow(cAmount, cOfficialTimes);
                        if (totalAmount > 1000) {
                            if (player != null) {
                                player.sendMessage(CSDirector.this.heading + cAmount + " to the power of " + cOfficialTimes + " equates to " + totalAmount + " bomblets and consequent explosions! For your safety, CrackShot does not accept total bomblet amounts of over 1000. Please lower the value for 'Number_Of_Splits' and/or 'Number_Of_Bomblets' for the weapon '" + parent_node + "'.");
                            }

                        } else {
                            for (int i = 0; i < cAmount; ++i) {
                                location.setPitch((float) (-(r.nextInt(90) + r.nextInt(90))));
                                location.setYaw((float) r.nextInt(360));
                                double cSpeedF = (double) cSpeed * (double) (100 - (r.nextInt(25) - r.nextInt(25))) * 0.001;
                                CSDirector.this.launchGrenade(player, parent_node, timer, location.getDirection().multiply(cSpeedF), location, cTimes + 1);
                            }

                            CSDirector.this.csminion.giveParticleEffects(null, parent_node, ".Cluster_Bombs.Particle_Release", false, location);
                            SoundUtils.playSoundEffects(null, parent_node, ".Cluster_Bombs.Sounds_Release", false, location);
                            WeaponExplodeEvent explodeEvent = new WeaponExplodeEvent(player, location, parent_node, true, false);
                            CSDirector.this.plugin.getServer().getPluginManager().callEvent(explodeEvent);
                        }
                    } else {
                        boolean shrapEnable = ConfigCache.getBoolean(parent_node + ".Shrapnel.Enable");
                        if (shrapEnable) {
                            String shrapType = ConfigCache.getString(parent_node + ".Shrapnel.Block_Type");
                            int shrapAmount = ConfigCache.getInt(parent_node + ".Shrapnel.Amount");
                            int shrapSpeed = ConfigCache.getInt(parent_node + ".Shrapnel.Speed");
                            boolean placeBlocks = ConfigCache.getBoolean(parent_node + ".Shrapnel.Place_Blocks");
                            String[] blockInfo = shrapType.split("~");
                            if (blockInfo.length < 2) {
                                blockInfo = new String[]{blockInfo[0], "0"};
                            }

                            Material blockMat = MaterialManager.getMaterial(shrapType);
                            if (blockMat == null) {
                                player.sendMessage(CSDirector.this.heading + "'" + shrapType + "' of weapon '" + parent_node + "' is not a valid block-type.");
                                return;
                            }

                            byte secData;
                            try {
                                secData = Byte.parseByte(blockInfo[1]);
                            } catch (NumberFormatException var19) {
                                player.sendMessage(CSDirector.this.heading + "'" + shrapType + "' of weapon '" + parent_node + "' has an invalid secondary data value.");
                                return;
                            }

                            Random r = new Random();

                            for (int i = 0; i < shrapAmount; ++i) {
                                location.setPitch((float) (-(r.nextInt(90) + r.nextInt(90))));
                                location.setYaw((float) r.nextInt(360));
                                FallingBlock shrapnel = location.getWorld().spawnFallingBlock(location, blockMat, secData);
                                if (!placeBlocks) {
                                    shrapnel.setMetadata("CS_shrapnel", new FixedMetadataValue(CSDirector.this.plugin, true));
                                }

                                shrapnel.setDropItem(false);
                                double shrapSpeedF = (double) shrapSpeed * (double) (100 - (r.nextInt(25) - r.nextInt(25))) * 0.001;
                                shrapnel.setVelocity(location.getDirection().multiply(shrapSpeedF));
                            }
                        }

                        WeaponExplodeEvent explodeEvent = new WeaponExplodeEvent(player, location, parent_node, false, false);
                        CSDirector.this.plugin.getServer().getPluginManager().callEvent(explodeEvent);
                        CSDirector.this.csminion.displayFireworks(objProj, parent_node, ".Fireworks.Firework_Explode");
                        boolean ownerNoDam = ConfigCache.getBoolean(parent_node + ".Explosions.Enable_Owner_Immunity");
                        boolean noDam = ConfigCache.getBoolean(parent_node + ".Explosions.Explosion_No_Damage");
                        boolean frenFire = ConfigCache.getBoolean(parent_node + ".Explosions.Enable_Friendly_Fire");
                        boolean noGrief = ConfigCache.getBoolean(parent_node + ".Explosions.Explosion_No_Grief");
                        boolean isFire = ConfigCache.getBoolean(parent_node + ".Explosions.Explosion_Incendiary");
                        int boomRadius = ConfigCache.getInt(parent_node + ".Explosions.Explosion_Radius");
                        if (boomRadius > 20) {
                            boomRadius = 20;
                        }

                        TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);
                        tnt.setYield((float) boomRadius);
                        tnt.setIsIncendiary(isFire);
                        tnt.setFuseTicks(0);
                        tnt.setMetadata("CS_Label", new FixedMetadataValue(CSDirector.this.plugin, true));
                        tnt.setMetadata("CS_potex", new FixedMetadataValue(CSDirector.this.plugin, parent_node));
                        if (!rde) {
                            tnt.setMetadata("C4_Friendly", new FixedMetadataValue(CSDirector.this.plugin, true));
                        }

                        if (noGrief) {
                            tnt.setMetadata("nullify", new FixedMetadataValue(CSDirector.this.plugin, true));
                        }

                        if (noDam) {
                            tnt.setMetadata("CS_nodam", new FixedMetadataValue(CSDirector.this.plugin, true));
                        }

                        if (player != null) {
                            tnt.setMetadata("CS_pName", new FixedMetadataValue(CSDirector.this.plugin, player.getName()));
                            if (!frenFire) {
                                tnt.setMetadata("CS_ffcheck", new FixedMetadataValue(CSDirector.this.plugin, true));
                            }

                            if (ownerNoDam) {
                                tnt.setMetadata("0wner_nodam", new FixedMetadataValue(CSDirector.this.plugin, true));
                            }
                        }

                    }
                }
            }, (long) Math.abs(delay));
        }
    }



    @EventHandler
    // Сброс оружейного состояния при смене слота.
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        this.removeInertReloadTag(player, event.getPreviousSlot(), false);
        this.removeInertReloadTag(player, event.getNewSlot(), false);
        this.unscopePlayer(player);
        this.terminateAllBursts(player);
        this.terminateReload(player);
        ItemStack heldItem = player.getInventory().getItem(event.getNewSlot());
        if (heldItem != null) {
            String[] pc = ItemUtils.itemParentNode(heldItem, player);
            if (pc == null || !Boolean.parseBoolean(pc[1])) {
                return;
            }

            ItemStack weapon = this.csminion.vendingMachine(pc[0]);
            if (weapon != null && ItemUtils.itemIsSafe(heldItem)) {
                // Preserve the display name when replacing corrupted items to maintain ammo/reload state
                ItemMeta meta = weapon.getItemMeta();
                if (meta != null && heldItem.getItemMeta() != null) {
                    meta.setDisplayName(heldItem.getItemMeta().getDisplayName());
                    weapon.setItemMeta(meta);
                }
            }
            weapon.setAmount(player.getInventory().getItem(event.getNewSlot()).getAmount());
            player.getInventory().setItem(event.getNewSlot(), weapon);
        }

    }

    @EventHandler
    // Сброс оружейного состояния при выходе игрока.
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.removeInertReloadTag(player, 0, true);
        this.unscopePlayer(player);
        this.terminateAllBursts(player);
        this.terminateReload(player);
        String playerName = player.getName();
        if (this.itembombs.containsKey(playerName)) {
            Map<String, ArrayDeque<Item>> subList = this.itembombs.get(playerName);

            for (ArrayDeque<Item> subSubList : subList.values()) {
                while (!subSubList.isEmpty()) {
                    subSubList.removeFirst().remove();
                }
            }

            this.itembombs.remove(playerName);
        }

        this.delay_list.remove(playerName);
        this.last_drop.remove(playerName);
        this.delayed_reload_IDs.remove(playerName);
        this.c4_backup.remove(playerName);
        this.last_shot_list.remove(playerName);
        this.rpm_ticks.remove(playerName);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType().getMaxDurability() <= 0) {
            return;
        }

        String[] weaponInfo = ItemUtils.itemParentNode(item, event.getPlayer());
        if (weaponInfo == null || weaponInfo.length == 0 || weaponInfo[0] == null) {
            return;
        }

        String parentNode = weaponInfo[0];
        if (!ConfigCache.getBoolean(parentNode + ".Item_Information.Prevent_Item_Break")) {
            return;
        }

        int resultingDurability = item.getDurability() + event.getDamage();
        if (resultingDurability >= item.getType().getMaxDurability()) {
            event.setCancelled(true);
            short safeDurability = (short) (item.getType().getMaxDurability() - 1);
            if (item.getDurability() > safeDurability) {
                item.setDurability(safeDurability);
            }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    // Контроль выброса оружия, чтобы не дублировать действия клика/дропа в один тик.
    public void onGunThrow(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        ItemStack trash = event.getItemDrop().getItemStack();
        String[] pc = ItemUtils.itemParentNode(trash, event.getPlayer());
        if (pc != null) {
            if (ConfigCache.getBoolean(pc[0] + ".Reload.Enable")) {
                if (!ConfigCache.getBoolean(pc[0] + ".Reload.Reload_With_Mouse")) {

                    DropAttempt attempt = new DropAttempt(player);
                    this.last_drop.put(player.getUniqueId(), attempt);
                    if (!player.hasMetadata("dr0p_authorised")) {
                        event.setCancelled(true);
                        this.delayedReload(player, pc[0]);
                    }

                }
            }
        }
    }

    @EventHandler
    // После смерти очищаем эффекты CrackShot и обновляем предметы игрока.
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        this.removeInertReloadTag(player, 0, true);
        this.unscopePlayer(player);
        this.terminateAllBursts(player);
        this.terminateReload(player);
        List<ItemStack> newInv = new ArrayList();
        Iterator<ItemStack> it = event.getDrops().iterator();

        while (it.hasNext()) {
            ItemStack item = it.next();
            if (item != null && ItemUtils.itemIsSafe(item)) {
                String[] parent_node = ItemUtils.itemParentNode(item, player);
                if (parent_node != null && ConfigCache.getBoolean(parent_node[0] + ".Abilities.Death_No_Drop")) {
                    newInv.add(item);
                    it.remove();
                }
            }
        }

        if (!newInv.isEmpty()) {
            final ItemStack[] newStack = newInv.toArray(new ItemStack[newInv.size()]);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    player.getInventory().setContents(newStack);
                }
            });
        }

        if (event.getDeathMessage() != null) {
            String message = event.getDeathMessage().replaceAll("(?<=«).*?(?=»)", "");
            message = message.replaceAll(" «", "");
            message = message.replaceAll(String.valueOf('ᴿ'), "");
            message = message.replaceAll("[»▪▫˗]", "");
            event.setDeathMessage(message);
        }

        if (event.getEntity().getKiller() instanceof Player) {
            Player shooter = event.getEntity().getKiller();
            String parent_node = ItemUtils.returnParentNode(shooter);
            if (parent_node == null) {
                return;
            }

            String msg = ConfigCache.getString(parent_node + ".Custom_Death_Message.Normal");
            if (msg == null) {
                return;
            }

            msg = msg.replaceAll("<shooter>", shooter.getName());
            msg = msg.replaceAll("<victim>", player.getName());
            event.setDeathMessage(msg);
        }

    }

    @EventHandler
    // Блокирует недопустимые действия с оружием в инвентаре и корректирует повреждённые предметы.
    public void clickGun(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            ItemStack currentItem = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();
            if (event.getSlotType() == SlotType.QUICKBAR) {
                this.removeInertReloadTag(player, event.getSlot(), false);
                this.unscopePlayer(player);
                this.terminateAllBursts(player);
                this.terminateReload(player);
            }

            if (event.getSlot() != -1 && currentItem != null) {
                String[] pc = ItemUtils.itemParentNode(currentItem, player);
                if (pc == null) {
                    return;
                }

                if (event.getInventory().getType() == InventoryType.ANVIL && event.getSlot() == 2 && event.getSlotType() == SlotType.RESULT) {
                    player.playSound(player.getLocation(), SoundManager.get("WOOD_CLICK"), 0.5F, 2.0F);
                    event.setCancelled(true);
                    return;
                }

                if (!Boolean.valueOf(pc[1])) {
                    return;
                }

                ItemStack weapon = this.csminion.vendingMachine(pc[0]);
                weapon.setAmount(currentItem.getAmount());
                event.setCurrentItem(weapon);
            }

            if (event.getSlot() == -999) {
                ItemStack trash = event.getCursor();
                String[] pc = ItemUtils.itemParentNode(trash, player);
                if (pc == null) {
                    return;
                }

                player.setMetadata("dr0p_authorised", new FixedMetadataValue(this, true));
                this.csminion.tempVars(player, "dr0p_authorised", 1L);
            }
        }

    }

    // Снимает эффект прицела и восстанавливает исходные эффекты замедления/ночного зрения.
    public void unscopePlayer(Player player, boolean... manual) {
        if (player.hasMetadata("ironsights")) {
            String pName = player.getName();
            String parentNode = player.getMetadata("ironsights").get(0).asString();
            if (manual.length == 0) {
                WeaponScopeEvent scopeEvent = new WeaponScopeEvent(player, parentNode, false);
                this.getServer().getPluginManager().callEvent(scopeEvent);
                if (scopeEvent.isCancelled()) {
                    return;
                }
            }

            player.removeMetadata("ironsights", this);
            player.removePotionEffect(slowness);
            if (player.hasMetadata("night_scoping")) {
                player.removeMetadata("night_scoping", this);
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }

            if (this.zoomStorage.containsKey(pName)) {
                int[] durAmp = this.zoomStorage.get(pName);
                player.addPotionEffect(slowness.createEffect(durAmp[0], durAmp[1]));
            }

            this.zoomStorage.remove(pName);
        }

    }

    // Убирает служебную метку перезарядки с конкретного слота либо со всего инвентаря.
    public void removeInertReloadTag(Player player, int item_slot, boolean no_slot) {
        ItemStack item = player.getInventory().getItem(item_slot);
        if (no_slot) {
            item = player.getInventory().getItemInHand();
        }

        if (item != null && ItemUtils.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains(String.valueOf('ᴿ'))) {
            String cleaner = item.getItemMeta().getDisplayName().replaceAll(String.valueOf('ᴿ'), "");
            if (no_slot) {
                this.csminion.setItemName(player.getInventory().getItemInHand(), cleaner);
            } else {
                this.csminion.setItemName(player.getInventory().getItem(item_slot), cleaner);
            }
        }

    }

    public boolean switchedTheItem(Player player, String parent_node) {
        ItemStack item = player.getInventory().getItemInHand();
        String attachType = WeaponAttachmentUtils.getAttachment(parent_node, item)[0];
        boolean attachment = attachType != null && attachType.equalsIgnoreCase("accessory");
        return item == null || !ItemUtils.itemIsSafe(item) || !attachment && WeaponHelperUtils.isDifferentItem(item, parent_node);
    }

    // Останавливает все burst-задачи игрока.
    public void terminateAllBursts(Player player) {
        Collection<Integer> values = this.burst_task_IDs.get(player.getName());
        if (values != null) {
            for (int taskID : values) {
                Bukkit.getScheduler().cancelTask(taskID);
            }
        }

        this.burst_task_IDs.remove(player.getName());
        this.rpm_shots.remove(player.getName());
    }

    // Останавливает активную перезарядку игрока и связанные таймеры.
    public void terminateReload(Player player) {
        String playerName = player.getName();
        Collection<Integer> values = this.global_reload_IDs.get(playerName);
        if (values != null) {
            for (Integer value : values) {
                Bukkit.getScheduler().cancelTask(value);
            }
        }

        this.global_reload_IDs.remove(playerName);
        player.removeMetadata("markOfTheReload", this);
        if (this.delayed_reload_IDs.containsKey(playerName)) {
            Bukkit.getScheduler().cancelTask(this.delayed_reload_IDs.get(playerName));
            this.delayed_reload_IDs.remove(playerName);
        }

        UUID uuid = player.getUniqueId();
        last_drop.remove(uuid);

        player.removeMetadata("markOfTheReload", plugin);
        player.removeMetadata("ironsights", plugin);

        int slot = player.getInventory().getHeldItemSlot();
        player.removeMetadata("noShooting" + slot, plugin);
        player.removeMetadata("togglesnoShooting" + slot, plugin);
    }

    private void clearShootDelayMetadata(Player player, String parentNode, int gunSlot) {
        player.removeMetadata(parentNode + "shootDelay" + gunSlot + true, this);
        player.removeMetadata(parentNode + "shootDelay" + gunSlot + false, this);
    }


    // Спавнит дополнительных мобов/сущности по конфигу после попадания, смерти или взрыва.
    public boolean spawnEntities(
            LivingEntity player,
            String parentNode,
            String childNode,
            LivingEntity tamer
    ) {

        if (!ConfigCache.getBoolean(parentNode + ".Spawn_Entity_On_Hit.Enable")) {
            return false;
        }

        String projectileType = ConfigCache.getString(parentNode + ".Shooting.Projectile_Type");

        if ("energy".equalsIgnoreCase(projectileType)) {

            printM(
                    "For the weapon '" + parentNode +
                            "', the 'energy' projectile-type does not support the Spawn_Entity_On_Hit module."
            );

            return false;
        }

        String entitiesString = ConfigCache.getString(parentNode + childNode);

        if (entitiesString == null) {
            return false;
        }

        int spawnChance = ConfigCache.getInt(parentNode + ".Spawn_Entity_On_Hit.Chance");

        if (ThreadLocalRandom.current().nextInt(100) > spawnChance) {
            return false;
        }

        String customName = ConfigCache.getString(parentNode + ".Spawn_Entity_On_Hit.Mob_Name");

        boolean targetVictim = ConfigCache.getBoolean(parentNode + ".Spawn_Entity_On_Hit.Make_Entities_Target_Victim");

        boolean noDrops = ConfigCache.getBoolean(parentNode + ".Spawn_Entity_On_Hit.Entity_Disable_Drops");

        int timedDeath = ConfigCache.getInt(parentNode + ".Spawn_Entity_On_Hit.Timed_Death");

        for (String rawData : entitiesString.split(",")) {

            SpawnEntityData data;

            try {
                data = SpawnEntityParser.parse(rawData);
            } catch (Exception ex) {
                printM("Invalid entity format '" + rawData + "' in weapon '" + parentNode + "'");
                continue;
            }

            for (int i = 0; i < data.getAmount(); i++) {

                LivingEntity mob = entityFactory.create(player.getWorld(), player.getLocation(), data);

                EntityConfigurator.configure(mob, data, tamer instanceof AnimalTamer ? (AnimalTamer) tamer : null, customName);

                if (noDrops) {
                    mob.setMetadata("CS_NoDrops", new FixedMetadataValue(this, true)
                );
                }

                if (targetVictim) {
                    mob.damage(0.0D, player);
                }

                if (timedDeath > 0) {

                    Bukkit.getScheduler().runTaskLater(this, () -> mob.damage(400.0D), timedDeath);
                }
            }
        }

        return true;
    }

    @EventHandler
    public void onSpawnedEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("CS_Boomer")) {
            TNTPrimed tnt = event.getEntity().getWorld().spawn(event.getEntity().getLocation(), TNTPrimed.class);
            tnt.setYield(2.0F);
            tnt.setFuseTicks(0);
            tnt.setMetadata("nullify", new FixedMetadataValue(this, true));
        }

        if (event.getEntity().hasMetadata("CS_NoDrops")) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }

    }

    @EventHandler
    // Обработчик магазинов на табличках.
    public void createGunShop(SignChangeEvent event) {
        String lineOne = event.getLine(0);
        if (lineOne.contains("[CS]")) {
            String filter = lineOne.replaceAll(Pattern.quote("[CS]"), "");

            try {
                Integer.valueOf(filter);
            } catch (NumberFormatException var7) {
                return;
            }

            for (String parent_node : this.parentlist.values()) {
                if (ConfigCache.getBoolean(parent_node + ".SignShops.Enable")) {
                    if (!event.getPlayer().hasPermission("crackshot.shops." + parent_node) && !event.getPlayer().hasPermission("crackshot.shops.all")) {
                        CSMessages.sendMessage(event.getPlayer(), this.heading, Message.NP_STORE_CREATE.getMessage());
                        return;
                    }

                    int gunID = ConfigCache.getInt(parent_node + ".SignShops.Sign_Gun_ID");
                    if (gunID != 0 && gunID == Integer.parseInt(filter)) {
                        event.setLine(0, "§fStore No᎐ " + gunID);
                        CSMessages.sendMessage(event.getPlayer(), this.heading, Message.STORE_CREATED.getMessage());
                        break;
                    }
                }
            }

        }
    }

    // Логика покупки/получения оружия по клику на табличку-магазин.
    public boolean shopEvent(PlayerInteractEvent event) {
        boolean retVal = false;
        Sign signState = (Sign) event.getClickedBlock().getState();
        if (signState.getLine(0).contains("§fStore No᎐")) {
            Player player = event.getPlayer();
            String signLineOne = signState.getLine(0).replaceAll("§fStore No᎐ ", "");

            for (String parentNode : this.parentlist.values()) {
                if (ConfigCache.getBoolean(parentNode + ".SignShops.Enable")
                        && ConfigCache.getString(parentNode + ".SignShops.Price") != null) {
                    int gunID = ConfigCache.getInt(parentNode + ".SignShops.Sign_Gun_ID");
                    String priceInfo = ConfigCache.getString(parentNode + ".SignShops.Price");

                    String[] signInfo = priceInfo.split("-");

                    int shopID;
                    try {
                        shopID = Integer.parseInt(signLineOne);
                    } catch (NumberFormatException var17) {
                        break;
                    }

                    String currency;
                    int amount;
                    try {
                        currency = signInfo[0];
                        amount = Integer.parseInt(signInfo[1]);
                    } catch (NumberFormatException var16) {
                        player.sendMessage(this.heading + "'Price: " + priceInfo + "' of weapon '" + parentNode + "' does not contain a valid item ID and/or amount!");
                        break;
                    }

                    if (gunID == shopID) {
                        boolean creativeMode = player.getGameMode() != GameMode.CREATIVE;
                        if (creativeMode || !player.hasPermission("crackshot.store." + parentNode) && !player.hasPermission("crackshot.store.all")) {
                            event.setCancelled(true);
                        }

                        if (!player.hasPermission("crackshot.buy." + parentNode) && !player.hasPermission("crackshot.buy.all")) {
                            CSMessages.sendMessage(player, this.heading, Message.NP_STORE_PURCHASE.getMessage());
                        } else if (creativeMode) {
                            if (this.csminion.countItemStacks(player, signInfo[0], parentNode) < amount) {
                                CSMessages.sendMessage(player, this.heading, Message.STORE_CANNOT_AFFORD.getMessage());
                                CSMessages.sendMessage(player, this.heading, Message.STORE_ITEMS_NEEDED.getMessage(amount, MaterialManager.getMaterial(currency).toString()));
                            } else if (player.getInventory().firstEmpty() != -1) {
                                this.csminion.removeNamedItem(player, signInfo[0], amount, parentNode, true);
                                this.csminion.getWeaponCommand(player, parentNode, false, (String) null, false, false);
                                String milk = ConfigCache.getString(parentNode + ".Item_Information.Item_Name");
                                CSMessages.sendMessage(player, this.heading, Message.STORE_PURCHASED.getMessage(milk));
                                retVal = true;
                            }
                        }
                        break;
                    }
                }
            }
        }

        return retVal;
    }

    // Проверка текущего положения затвора/механики перед выстрелом.
    public boolean checkBoltPosition(Player player, String parent_node) {
        ItemStack item = player.getInventory().getItemInHand();
        String actType = ConfigCache.getString(parent_node + ".Firearm_Action.Type");
        if (actType != null && !WeaponHelperUtils.isDualWield(player, parent_node, item)) {
            String[] validTypes = new String[]{"bolt", "lever", "pump", "break", "revolver", "slide"};

            for (String str : validTypes) {
                if (actType.equalsIgnoreCase(str)) {
                    break;
                }

                if (str.equals("slide")) {
                    this.printM("'" + actType + "' of weapon '" + parent_node + "' is not a valid firearm action! The accepted values are slide, bolt, lever, pump, break or revolver!");
                    return false;
                }
            }

            int openTime = ConfigCache.getInt(parent_node + ".Firearm_Action.Open_Duration");
            int closeTime = ConfigCache.getInt(parent_node + ".Firearm_Action.Close_Duration");
            if (!ItemUtils.itemIsSafe(item)) {
                return false;
            } else {
                String itemName = item.getItemMeta().getDisplayName();
                int chamberPos = itemName.lastIndexOf("§") + 3;
                char chamber = itemName.charAt(chamberPos);
                if (chamber == 171) {
                    this.csminion.setItemName(item, itemName.replace("«", "▪ «"));
                } else if (chamber != 9642 && chamber != 9643 && chamber != 727) {
                    this.csminion.setItemName(item, itemName.substring(0, chamberPos) + '▪' + itemName.substring(chamberPos + 1));
                }

                int detectedAmmo = AmmoUtils.getAmmoBetweenBrackets(player, parent_node, item);
                if (!actType.toLowerCase().contains("break") && !actType.toLowerCase().contains("revolver") && !actType.toLowerCase().contains("slide")) {
                    boolean chamberFired = chamber == 9642;
                    boolean chamberOpened = chamber == 727;
                    if (chamberFired) {
                        this.csminion.setItemName(item, itemName.replace("▪", "▫"));
                    }

                    this.correctBoltPosition(player, parent_node, !chamberOpened, chamberOpened ? closeTime : openTime, detectedAmmo <= 0, false, false, false);
                    return !chamberFired;
                } else if (detectedAmmo > 0) {
                    if (chamber == 9643) {
                        this.correctBoltPosition(player, parent_node, false, closeTime, false, false, false, true);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    this.reloadAnimation(player, parent_node);
                    boolean ammoEnable = ConfigCache.getBoolean(parent_node + ".Ammo.Enable");
                    String ammoInfo = ConfigCache.getString(parent_node + ".Ammo.Ammo_Item_ID");
                    boolean takeAmmo = ConfigCache.getBoolean(parent_node + ".Reload.Take_Ammo_On_Reload");
                    if (ammoEnable && takeAmmo && !this.csminion.containsItemStack(player, ammoInfo, 1, parent_node)) {
                        SoundUtils.playSoundEffects(player, parent_node, ".Ammo.Sounds_Shoot_With_No_Ammo", false, (Location) null);
                    }

                    return true;
                }
            }
        } else {
            return false;
        }
    }

    // Запускает анимацию и тайминг bolt/lever/pump/slide/revolver/break.
    public void correctBoltPosition(final Player player, final String parent_node, final boolean boltPull, int delay, final boolean reloadPrep, final boolean reloadFin, final boolean pumpExit, final boolean breakAct) {
        final String actType = ConfigCache.getString(parent_node + ".Firearm_Action.Type");
        if (actType != null && !WeaponHelperUtils.isDualWield(player, parent_node, player.getItemInHand())) {
            String[] validTypes = new String[]{"bolt", "lever", "pump", "break", "revolver", "slide"};

            for (String str : validTypes) {
                if (actType.equalsIgnoreCase(str)) {
                    break;
                }

                if (str.equals("slide")) {
                    this.printM("'" + actType + "' of weapon '" + parent_node + "' is not a valid firearm action! The accepted values are slide, bolt, lever, pump, break or revolver!");
                    return;
                }
            }

            final int heldSlot = player.getInventory().getHeldItemSlot();
            if (!player.hasMetadata("fiddling")) {
                player.setMetadata("fiddling", new FixedMetadataValue(this, true));
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        player.removeMetadata("fiddling", CSDirector.this.plugin);
                        ItemStack item = player.getInventory().getItemInHand();
                        int currentSlot = player.getInventory().getHeldItemSlot();
                        int closeTime = ConfigCache.getInt(parent_node + ".Firearm_Action.Close_Duration");
                        int closeShootDelay = ConfigCache.getInt(parent_node + ".Firearm_Action.Close_Shoot_Delay");
                        if (ItemUtils.itemIsSafe(item)) {
                            String itemName = item.getItemMeta().getDisplayName();
                            if (!WeaponHelperUtils.isDifferentItem(item, parent_node)) {
                                int chamberPos = itemName.lastIndexOf("§") + 3;
                                char chamber = itemName.charAt(chamberPos);
                                if (chamber == 171) {
                                    CSDirector.this.csminion.setItemName(item, itemName.replace("«", "▪ «"));
                                } else if (chamber != 9642 && chamber != 9643 && chamber != 727) {
                                    CSDirector.this.csminion.setItemName(item, itemName.substring(0, chamberPos) + '▪' + itemName.substring(chamberPos + 1));
                                } else {
                                    boolean isAttachment = itemName.contains(String.valueOf('▶'));
                                    boolean isReloading = itemName.contains(String.valueOf('ᴿ'));
                                    boolean switchedItems = CSDirector.this.switchedTheItem(player, parent_node) || heldSlot != currentSlot;
                                    boolean isCocked = reloadFin && chamber == 9642;
                                    if (!isAttachment && !isReloading && !switchedItems && !isCocked) {
                                        if (breakAct) {
                                            SoundUtils.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Close", false, (Location) null);
                                            CSDirector.this.csminion.setItemName(item, itemName.replaceAll("▫", "▪"));
                                            CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, closeShootDelay, "noShooting");
                                        } else if (pumpExit && chamber == 9643) {
                                            CSDirector.this.correctBoltPosition(player, parent_node, true, 0, false, false, false, false);
                                        } else if (reloadPrep) {
                                            boolean isBreak = actType.equalsIgnoreCase("break") || actType.equalsIgnoreCase("revolver");
                                            String nameToSet = itemName.replaceAll("▪", "▫");
                                            if (!isBreak) {
                                                nameToSet = nameToSet.replaceAll("▫", "˗");
                                            }

                                            if (!itemName.contains("ᴿ")) {
                                                CSDirector.this.csminion.setItemName(item, nameToSet + 'ᴿ');
                                            } else {
                                                CSDirector.this.csminion.setItemName(item, nameToSet);
                                            }

                                            int reloadOpenDelay = ConfigCache.getInt(parent_node + ".Firearm_Action.Reload_Open_Delay");
                                            SoundUtils.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Open", reloadOpenDelay > 0, (Location) null);
                                            if (reloadOpenDelay > 0) {
                                                CSDirector.this.delayedReload(player, parent_node, Long.valueOf((long) reloadOpenDelay));
                                                CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, reloadOpenDelay, "noShooting");
                                            } else {
                                                CSDirector.this.reloadAnimation(player, parent_node);
                                            }

                                        } else {
                                            if (boltPull) {
                                                SoundUtils.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Open", false, (Location) null);
                                                CSDirector.this.csminion.setItemName(item, itemName.replaceAll("▫", "˗"));
                                                CSDirector.this.correctBoltPosition(player, parent_node, false, closeTime, false, false, false, false);
                                            } else if (actType.equalsIgnoreCase("slide") && (chamber == 9643 || chamber == 727)) {
                                                if (chamber == 9643) {
                                                    CSDirector.this.csminion.setItemName(item, itemName.replaceAll("▫", "▪"));
                                                } else {
                                                    CSDirector.this.csminion.setItemName(item, itemName.replaceAll("˗", "▪"));
                                                }

                                                SoundUtils.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Close", false, (Location) null);
                                                CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, closeShootDelay, "noShooting");
                                            } else {
                                                int detectedAmmo = AmmoUtils.getAmmoBetweenBrackets(player, parent_node, item);
                                                if (detectedAmmo > 0) {
                                                    CSDirector.this.csminion.setItemName(item, itemName.replaceAll("˗", "▪"));
                                                    SoundUtils.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Close", false, (Location) null);
                                                    CSDirector.this.reloadShootDelay(player, parent_node, currentSlot, closeShootDelay, "noShooting");
                                                    if (detectedAmmo != 125622) {
                                                        CSDirector.this.ammoOperation(player, parent_node, detectedAmmo, item);
                                                    }
                                                } else {
                                                    CSDirector.this.reloadAnimation(player, parent_node);
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }, (long) delay);
            }
        }
    }

    // Обновляет патроны на оружии и списывает ammo в зависимости от режима стрельбы.
    public void ammoOperation(Player player, String parent_node, int detectedAmmo, ItemStack item) {
        boolean ammoEnable = ConfigCache.getBoolean(parent_node + ".Ammo.Enable");
        String ammoInfo = ConfigCache.getString(parent_node + ".Ammo.Ammo_Item_ID");
        boolean takeAmmo = ConfigCache.getBoolean(parent_node + ".Ammo.Take_Ammo_Per_Shot");
        --detectedAmmo;
        this.csminion.replaceBrackets(item, String.valueOf(detectedAmmo), parent_node);
        if (ammoEnable && takeAmmo) {
            this.csminion.removeNamedItem(player, ammoInfo, 1, parent_node, false);
        }

        if (detectedAmmo == 0) {
            String actType = ConfigCache.getString(parent_node + ".Firearm_Action.Type");
            SoundUtils.playSoundEffects(player, parent_node, ".Reload.Sounds_Out_Of_Ammo", false, (Location) null);
            if (!ItemUtils.itemIsSafe(item)) {
                return;
            }

            String itemName = item.getItemMeta().getDisplayName();
            if (actType != null) {
                if (!actType.equalsIgnoreCase("bolt") && !actType.equalsIgnoreCase("lever") && !actType.equalsIgnoreCase("pump")) {
                    if (actType.equalsIgnoreCase("break") || actType.equalsIgnoreCase("revolver") || actType.equalsIgnoreCase("slide")) {
                        if (actType.toLowerCase().contains("slide") && itemName.contains("▪")) {
                            int openTime = ConfigCache.getInt(parent_node + ".Firearm_Action.Open_Duration");
                            if (openTime < 1) {
                                SoundUtils.playSoundEffects(player, parent_node, ".Firearm_Action.Sound_Open", false, (Location) null);
                            }

                            this.csminion.setItemName(item, itemName.replaceAll("▪", "▫"));
                        }

                        this.delayedReload(player, parent_node);
                    }
                } else if (!itemName.contains("▪")) {
                    this.delayedReload(player, parent_node);
                }
            } else {
                this.delayedReload(player, parent_node);
            }
        }

    }

    // Частный случай ammoOperation для dual wield и нестандартных схем расхода патронов.
    public boolean ammoSpecOps(Player player, String parentNode, int detectedAmmo, ItemStack item, boolean leftClick) {
        boolean ammoEnable = ConfigCache.getBoolean(parentNode + ".Ammo.Enable");
        boolean takeAmmo = ConfigCache.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
        String ammoInfo = ConfigCache.getString(parentNode + ".Ammo.Ammo_Item_ID");
        int[] ammoReading = AmmoUtils.grabDualAmmo(item, parentNode);
        int ammoAmount;
        if (leftClick) {
            if (ammoReading[0] <= 0) {
                SoundUtils.playSoundEffects(player, parentNode, ".Reload.Dual_Wield.Sounds_Shoot_With_No_Ammo", false, (Location) null);
                return false;
            }

            ammoAmount = ammoReading[0] - 1;
            this.csminion.replaceBrackets(item, ammoAmount + " | " + ammoReading[1], parentNode);
        } else {
            if (ammoReading[1] <= 0) {
                SoundUtils.playSoundEffects(player, parentNode, ".Reload.Dual_Wield.Sounds_Shoot_With_No_Ammo", false, (Location) null);
                return false;
            }

            ammoAmount = ammoReading[1] - 1;
            this.csminion.replaceBrackets(item, ammoReading[0] + " | " + ammoAmount, parentNode);
        }

        if (ammoAmount <= 0) {
            SoundUtils.playSoundEffects(player, parentNode, ".Reload.Sounds_Out_Of_Ammo", false, (Location) null);
        }

        if (ammoEnable && takeAmmo) {
            this.csminion.removeNamedItem(player, ammoInfo, 1, parentNode, false);
        }

        if (detectedAmmo - 1 == 0) {
            this.reloadAnimation(player, parentNode);
        }

        return true;
    }



    @EventHandler
    public void explosiveTipCrossbow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && event.getForce() == 1.0F) {
            Player shooter = (Player) event.getEntity();
            String parentNode = ItemUtils.returnParentNode(shooter);
            if (parentNode == null) {
                return;
            }

            event.setCancelled(true);
            if (!this.regionAndPermCheck(shooter, parentNode, false)) {
                return;
            }

            this.csminion.weaponInteraction(shooter, parentNode, false);
        }

    }







    @EventHandler
    // Проверка крафта оружия и боеприпасов по правилам плагина.
    public void onCraft(CraftItemEvent event) {
        for (String parent_node : this.parentlist.values()) {
            if (ConfigCache.getBoolean(parent_node + ".Crafting.Enable")) {
                ItemStack weapon = this.csminion.vendingMachine(parent_node);
                if (event.getRecipe().getResult().isSimilar(weapon)) {
                    if (event.getWhoClicked() instanceof Player) {
                        Player crafter = (Player) event.getWhoClicked();
                        if (!crafter.hasPermission("crackshot.craft." + parent_node) && !crafter.hasPermission("crackshot.craft.all")) {
                            event.setCancelled(true);
                            CSMessages.sendMessage(crafter, this.heading, Message.NP_WEAPON_CRAFT.getMessage());
                        }
                    }
                    break;
                }
            }
        }

    }

    public void printM(String msg) {
        System.out.println("[CrackShot] " + msg);
    }



    public Integer getCustomModelData(String parentNode) {
        String[] supportedPaths = {
                parentNode + ".Item_Information.Custom_Model_Data",
                parentNode + ".Item_Information.CustomModelData",
                parentNode + ".Item_Information.CMD"
        };

        for (String path : supportedPaths) {
            Integer cached = ints.get(path);
            if (cached != null) {
                return cached;
            }

            if (this.weaponConfig != null && this.weaponConfig.isInt(path)) {
                return this.weaponConfig.getInt(path);
            }
        }

        return null;
    }

    public boolean hasDurab(String nodes) {
        Boolean result = this.morobust.get(nodes);
        return result != null ? result : false;
    }



    // Единая проверка: можно ли игроку использовать оружие в регионе и по permission.
    public boolean regionAndPermCheck(Player shooter, String parentNode, boolean noMsg) {
        for (String worName : this.disWorlds) {
            if (worName == null) {
                break;
            }

            World world = Bukkit.getWorld(worName);
            if (world == shooter.getWorld()) {
                return false;
            }
        }

        if (!shooter.hasPermission("crackshot.use." + parentNode) && !shooter.hasPermission("crackshot.use.all")) {
            if (!noMsg) {
                CSMessages.sendMessage(shooter, this.heading, Message.NP_WEAPON_USE.getMessage());
            }

            return false;
        } else if (!shooter.hasPermission("crackshot.bypass." + parentNode) && !shooter.hasPermission("crackshot.bypass.all") && !this.csminion.regionCheck(shooter, parentNode)) {
            if (!noMsg && ConfigCache.getString(parentNode + ".Region_Check.Message_Of_Denial") != null) {
                shooter.sendMessage(ConfigCache.getString(parentNode + ".Region_Check.Message_Of_Denial"));
            }

            return false;
        } else {
            return true;
        }
    }

    @EventHandler
    public void onEggSplat(PlayerEggThrowEvent event) {
        if (event.getEgg().hasMetadata("CS_Hardboiled")) {
            event.setHatching(false);
        }

    }



    // Бросает grenade/flare/bомbleт с задержкой активации и метаданными CrackShot.
    public void launchGrenade(final Player player, final String parent_node, int delay, Vector vel, Location splitLoc, final int cTimes) {
        boolean cEnable = ConfigCache.getBoolean(parent_node + ".Cluster_Bombs.Enable");
        int cOfficialTimes = ConfigCache.getInt(parent_node + ".Cluster_Bombs.Number_Of_Splits");
        String itemType = ConfigCache.getString(parent_node + ".Shooting.Projectile_Subtype");
        String nodeName = "Projectile_Subtype:";
        ProjectileSubtypeData projectileSubtypeData = null;

        if (cEnable && cTimes != 0) {
            nodeName = "Bomblet_Type:";
            itemType = ConfigCache.getString(parent_node + ".Cluster_Bombs.Bomblet_Type");
        } else if (itemType != null) {
            projectileSubtypeData = ProjectileSubtypeParser.parseProjectileSubtype(parent_node);
        }

        if (itemType == null) {
            player.sendMessage(this.heading + "The '" + nodeName + "' node of '" + parent_node + "' has not been defined.");
        } else {
            ItemStack item = null;
            if (projectileSubtypeData != null) {
                if (projectileSubtypeData.getType() == ProjectileSubtypeType.ITEM) {
                    item = projectileSubtypeData.getItem();
                } else {
                    player.sendMessage(this.heading + "The '" + nodeName + "' node of '" + parent_node + "' has an incorrect value.");
                }
            }

            if (item == null) {
                item = this.csminion.parseItemStack(itemType);
            }

            if (item == null) {
                player.sendMessage(this.heading + "The '" + nodeName + "' node of '" + parent_node + "' has an incorrect value.");
            } else {
                Location loc = player.getEyeLocation();
                if (splitLoc != null) {
                    loc = splitLoc;
                }

                final Item grenade = player.getWorld().dropItem(loc, item);
                grenade.setVelocity(vel);
                grenade.setPickupDelay(delay + 20);
                ItemStack grenStack = grenade.getItemStack();
                this.csminion.setItemName(grenStack, "૮" + grenade.getUniqueId());
                grenade.setItemStack(grenStack);
                ProjectileUtils.callShootEvent(player, grenade, parent_node);
                final boolean airstrike = ConfigCache.getBoolean(parent_node + ".Airstrikes.Enable");
                int cDelay = ConfigCache.getInt(parent_node + ".Cluster_Bombs.Delay_Before_Split");
                int cDelayDiff = ConfigCache.getInt(parent_node + ".Cluster_Bombs.Detonation_Delay_Variation");
                if (cEnable && !airstrike && cTimes < cOfficialTimes) {
                    if (cTimes == 0) {
                        SoundUtils.playSoundEffects(grenade, parent_node, ".Shooting.Sounds_Projectile", false, (Location) null);
                    }

                    delay = cDelay;
                } else if (cEnable) {
                    if (cDelay != 0 && cDelayDiff != 0) {
                        Random r = new Random();
                        delay += r.nextInt(cDelayDiff) - r.nextInt(cDelayDiff);
                    }
                } else {
                    SoundUtils.playSoundEffects(grenade, parent_node, ".Shooting.Sounds_Projectile", false, (Location) null);
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        boolean zapEnable = ConfigCache.getBoolean(parent_node + ".Lightning.Enable");
                        boolean zapNoDam = ConfigCache.getBoolean(parent_node + ".Lightning.No_Damage");
                        if (!airstrike) {
                            if (zapEnable) {
                                CSDirector.this.csminion.projectileLightning(grenade.getLocation(), zapNoDam);
                            }

                            CSDirector.this.projectileExplosion(grenade, parent_node, true, player, true, false, (Location) null, (Block) null, false, cTimes);
                        } else {
                            CSDirector.this.csminion.callAirstrike(grenade, parent_node, player);
                        }

                        grenade.remove();
                    }
                }, (long) delay);
            }
        }
    }

    @EventHandler
    public void onAnyDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
            Player shooter = (Player) event.getEntity();
            ItemStack heldItem = shooter.getItemInHand();
            if (heldItem != null && ItemUtils.itemIsSafe(heldItem)) {
                String parentNode = ItemUtils.returnParentNode(shooter);
                if (parentNode == null) {
                    return;
                }

                if (ConfigCache.getBoolean(parentNode + ".Abilities.No_Fall_Damage")) {
                    event.setCancelled(true);
                }
            }
        }

    }

    // Откладывает авто-перезарядку, если так настроено оружие.
    public void delayedReload(final Player player, final String parentNode, long... delay) {
        int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                CSDirector.this.reloadAnimation(player, parentNode);
                CSDirector.this.delayed_reload_IDs.remove(player.getName());
            }
        }, delay.length == 0 ? 1L : delay[0]);
        this.delayed_reload_IDs.put(player.getName(), taskID);
    }

    @EventHandler
    // Служебные обработчики предметов/транспорта, связанных с минами, C4 и itembomb.
    public void onPickUp(PlayerPickupItemEvent event) {
        if (this.csminion.fastenSeatbelts(event.getItem()) != null) {
            this.csminion.reseatTag(event.getItem());
            event.setCancelled(true);
            if (!(event.getItem().getVehicle() instanceof Minecart)) {
                event.getItem().remove();
            }
        } else {
            ItemStack item = event.getItem().getItemStack();
            if (ItemUtils.itemIsSafe(item)) {
                String fullName = item.getItemMeta().getDisplayName();
                if (fullName.contains("૮")) {
                    event.setCancelled(true);
                    event.getItem().remove();
                } else {
                    String itemName = NameUtils.getPureName(fullName);
                    if (this.boobs.containsKey(itemName)) {
                        String parentNode = this.boobs.get(itemName);
                        if (!this.csminion.getBoobean(2, parentNode)) {
                            return;
                        }

                        Player picker = event.getPlayer();
                        String detectedName = this.csminion.extractReading(fullName);
                        if (detectedName.equals("?")) {
                            return;
                        }

                        Player planter = Bukkit.getServer().getPlayer(detectedName);
                        if (planter == picker) {
                            return;
                        }

                        event.getItem().setPickupDelay(60);
                        this.slapAndReaction(picker, planter, event.getItem().getLocation().getBlock(), parentNode, (Inventory) null, (ItemStack[]) null, detectedName, event.getItem());
                        event.setCancelled(true);
                    }
                }
            }
        }

    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if (MaterialManager.isSkullItem(item.getType()) && item.hasItemMeta()) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            if (skullMeta != null && skullMeta.hasOwner() && skullMeta.getOwner().contains("،")) {
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }

    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Entity ent = event.getRightClicked();
        if (ent instanceof Minecart) {
            this.csminion.reseatTag((Vehicle) event.getRightClicked());
            if (ent.getPassenger() instanceof Item) {
                event.setCancelled(true);
            }
        } else if (ent instanceof Villager || ent instanceof Horse) {
            Player player = event.getPlayer();
            ItemStack heldItem = player.getItemInHand();
            String parentNode = ItemUtils.returnParentNode(player);
            if (parentNode != null && ConfigCache.getBoolean(parentNode + ".Shooting.Cancel_Right_Click_Interactions")) {
                this.OnPlayerInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, heldItem, (Block) null, (BlockFace) null));
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void tagDespawn(ItemDespawnEvent event) {
        if (this.csminion.fastenSeatbelts(event.getEntity()) != null) {
            event.setCancelled(true);
        }

        ItemStack item = event.getEntity().getItemStack();
        if (ItemUtils.itemIsSafe(item)) {
            String itemName = NameUtils.getPureName(item.getItemMeta().getDisplayName());
            if (itemName.contains("૮૮")) {
                event.setCancelled(true);
            } else if (this.boobs.containsKey(itemName)) {
                String parentNode = this.boobs.get(itemName);
                if (this.csminion.getBoobean(5, parentNode)) {
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onMobShotgun(VehicleEnterEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            this.csminion.reseatTag(event.getVehicle());
            if (event.getVehicle().getPassenger() instanceof Item) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onBoatMine(VehicleEntityCollisionEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            this.csminion.reseatTag(event.getVehicle());
            if (event.getVehicle().getPassenger() instanceof Item && event.getEntity() instanceof LivingEntity) {
                Entity victim = event.getEntity();
                Item psngr = (Item) event.getVehicle().getPassenger();
                String[] seagullInfo = this.csminion.fastenSeatbelts(psngr);
                if (seagullInfo == null) {
                    return;
                }

                event.setCancelled(true);
                Player fisherman = Bukkit.getServer().getPlayer(seagullInfo[1]);
                WeaponTriggerEvent trigEvent = new WeaponTriggerEvent(fisherman, (LivingEntity) victim, seagullInfo[2]);
                this.getServer().getPluginManager().callEvent(trigEvent);
                if (!trigEvent.isCancelled()) {
                    if (fisherman != null && victim instanceof Player) {
                        if (((Player) victim).getName().equals(seagullInfo[1])) {
                            event.setCancelled(false);
                        } else {
                            this.csminion.callAndResponse((Player) victim, fisherman, event.getVehicle(), seagullInfo, false);
                        }
                    } else {
                        this.csminion.mineAction(event.getVehicle(), seagullInfo, fisherman, false, victim.getType().getName(), victim);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onBoatMineShoot(VehicleDamageEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            this.csminion.reseatTag(event.getVehicle());
            if (event.getVehicle().getPassenger() instanceof Item) {
                Entity attacker = event.getAttacker();
                Item psngr = (Item) event.getVehicle().getPassenger();
                String[] seagullInfo = this.csminion.fastenSeatbelts(psngr);
                if (seagullInfo == null) {
                    return;
                }

                event.setCancelled(true);
                Player fisherman = Bukkit.getServer().getPlayer(seagullInfo[1]);
                if (attacker instanceof Player) {
                    Player player = (Player) attacker;
                    if (player.getName().equals(seagullInfo[1])) {
                        this.csminion.mineAction(event.getVehicle(), seagullInfo, fisherman, true, (String) null, attacker);
                    } else {
                        this.csminion.callAndResponse(player, fisherman, event.getVehicle(), seagullInfo, true);
                    }
                } else {
                    this.csminion.mineAction(event.getVehicle(), seagullInfo, fisherman, true, (String) null, attacker);
                }
            }

        }
    }

    // Устанавливает мину/взрывное устройство в мир и навешивает на него служебные метаданные.
    public void deployMine(Player player, String parentNode, Location location) {

        String deviceInfoString = ConfigCache.getString(parentNode + ".Explosive_Devices.Device_Info");

        if (deviceInfoString == null) {
            return;
        }

        MineDeviceInfo info;

        try {
            info = mineDeviceParser.parse(deviceInfoString);
        } catch (Exception ex) {
            player.sendMessage(heading + "Invalid Device_Info for weapon '" + parentNode + "'");
            return;
        }

        ItemStack fuseItem = info.getFuseItem();

        if (fuseItem == null) {
            player.sendMessage(heading + "No valid item-ID for 'Device_Info' of weapon '" + parentNode + "'");
            return;
        }

        Location spawnLocation = location == null ? player.getLocation().add(0.0D, 0.75D, 0.0D) : location;

        Entity mine = player.getWorld().spawnEntity(spawnLocation, info.getMinecartType());

        ItemMeta meta = fuseItem.getItemMeta();

        meta.setDisplayName("§cS3AGULLL~" + player.getName() + "~" + parentNode + "~" + mine.getUniqueId());

        fuseItem.setItemMeta(meta);

        Entity fusePassenger = player.getWorld().dropItem(spawnLocation, fuseItem);

        mine.setPassenger(fusePassenger);

        WeaponPlaceMineEvent event = new WeaponPlaceMineEvent(player, mine, parentNode);

        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler
    public void airstrikeKaboom(EntityChangeBlockEvent event) {
        if (event.getEntity().hasMetadata("CS_strike")) {
            Entity bomb = event.getEntity();
            String info = bomb.getMetadata("CS_strike").get(0).asString();
            String[] parsedInfo = info.split("~");
            Player player = Bukkit.getServer().getPlayer(parsedInfo[1]);
            this.projectileExplosion(bomb, parsedInfo[0], false, player, true, false, (Location) null, (Block) null, false, 0);
            bomb.remove();
            event.setCancelled(true);
        } else if (event.getEntity().hasMetadata("CS_shrapnel")) {
            event.getEntity().remove();
            event.setCancelled(true);
        }

    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onC4Place(BlockPlaceEvent event) {
        final Player placer = event.getPlayer();
        if (event.getItemInHand() != null) {
            final String[] parent_node = ItemUtils.itemParentNode(event.getItemInHand(), placer);
            if (parent_node != null) {
                if (this.regionAndPermCheck(placer, parent_node[0], false) && ConfigCache.getBoolean(parent_node[0] + ".Explosive_Devices.Enable")) {
                    placer.updateInventory();
                    String type = ConfigCache.getString(parent_node[0] + ".Explosive_Devices.Device_Type");
                    if (type != null && type.equalsIgnoreCase("remote")) {
                        if (ItemUtils.itemIsSafe(event.getItemInHand()) && event.getItemInHand().getItemMeta().getDisplayName().contains("«0»")) {
                            event.setCancelled(true);
                        } else {
                            boolean placeAnywhere = ConfigCache.getBoolean(parent_node[0] + ".Explosive_Devices.Remote_Bypass_Regions");
                            boolean allowed = !event.isCancelled() && event.canBuild();
                            final Block block = event.getBlockPlaced();
                            event.setCancelled(true);
                            if (allowed || placeAnywhere) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                    public void run() {
                                        CSDirector.this.setupC4(placer, block, parent_node);
                                    }
                                });
                            }

                        }
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    // Подготовка и размещение C4-головы с сохранением owner/unique id для дальнейшей детонации.
    public void setupC4(Player placer, Block block, String[] parent_node) {
        Material mat = MaterialManager.getSkullBlock();
        if (mat == null) {
            throw new UnsupportedOperationException();
        } else {
            block.setType(mat);
            if (MaterialManager.pre113) {
                try {
                    Method method = block.getClass().getMethod("setData", Byte.TYPE);
                    method.invoke(block, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            BlockState state = block.getState();
            if (state instanceof Skull) {
                int capacity = 0;
                String uniqueID = null;

                Skull skull;
                try {
                    skull = (Skull) state;
                } catch (ClassCastException var27) {
                    return;
                }

                if (MaterialManager.pre113) {
                    skull.setSkullType(SkullType.PLAYER);
                }

                String[] refinedOre = this.csminion.returnRefinedOre(placer, parent_node[0]);
                if (refinedOre != null) {
                    capacity = Integer.valueOf(refinedOre[0]);
                    uniqueID = refinedOre[1];
                }

                String storedOwner = placer.getName();
                if (storedOwner.length() > 13) {
                    storedOwner = storedOwner.substring(0, 12) + 'ظ';
                }

                skull.setOwner(uniqueID + "،" + storedOwner);
                if (MaterialManager.pre113) {
                    skull.setRotation(MathUtils.getBlockDirection(placer.getLocation().getYaw()));
                }

                skull.update(true);
                String world = placer.getWorld().getName();
                String x = String.valueOf(block.getLocation().getBlockX());
                String y = String.valueOf(block.getLocation().getBlockY());
                String z = String.valueOf(block.getLocation().getBlockZ());
                Map<String, String> placedHeads = (Map) this.c4_backup.get(storedOwner);
                if (placedHeads == null) {
                    placedHeads = new HashMap();
                    this.c4_backup.put(storedOwner, placedHeads);
                }

                placedHeads.put(world + "," + x + "," + y + "," + z, uniqueID);
                ItemStack detonator = placer.getItemInHand();
                boolean ammoEnable = ConfigCache.getBoolean(parent_node[0] + ".Ammo.Enable");
                String ammoInfo = ConfigCache.getString(parent_node[0] + ".Ammo.Ammo_Item_ID");
                boolean takeAmmo = ConfigCache.getBoolean(parent_node[0] + ".Ammo.Take_Ammo_Per_Shot");
                String bracketInfo = this.csminion.extractReading(detonator.getItemMeta().getDisplayName());
                int detectedAmmo = 0;

                try {
                    detectedAmmo = Integer.valueOf(bracketInfo);
                } catch (NumberFormatException e) {

                }

                if (detectedAmmo <= 0) {
                    block.setType(Material.AIR);
                    return;
                }

                if (ammoEnable && takeAmmo) {
                    if (!this.csminion.containsItemStack(placer, ammoInfo, 1, parent_node[0])) {
                        SoundUtils.playSoundEffects(placer, parent_node[0], ".Ammo.Sounds_Shoot_With_No_Ammo", false, (Location) null);
                        block.setType(Material.AIR);
                        return;
                    }

                    this.csminion.removeNamedItem(placer, ammoInfo, 1, parent_node[0], false);
                }

                this.csminion.replaceBrackets(detonator, String.valueOf(detectedAmmo - 1), parent_node[0]);
                if (detonator.getItemMeta().hasLore()) {
                    List<String> lore = detonator.getItemMeta().getLore();
                    String lastLine = (String) lore.get(lore.size() - 1);
                    if (lastLine.contains(String.valueOf('᎐'))) {
                        String numInBrack = lastLine.split("\\[")[1].split("\\]")[0];
                        int lastNumber = Integer.valueOf(numInBrack);
                        if (lastNumber >= capacity) {
                            block.setType(Material.AIR);
                            return;
                        }

                        lore.add("§e§l[" + (lastNumber + 1) + "]§r§e " + world.toUpperCase() + '᎐' + " " + x + ", " + y + ", " + z);
                    } else {
                        lore.add("§e§l[1]§r§e " + world.toUpperCase() + '᎐' + " " + x + ", " + y + ", " + z);
                    }

                    ItemMeta detmeta = detonator.getItemMeta();
                    detmeta.setLore(lore);
                    detonator.setItemMeta(detmeta);
                    placer.getInventory().setItemInHand(detonator);
                    SoundUtils.playSoundEffects(placer, parent_node[0], ".Explosive_Devices.Sounds_Deploy", false, (Location) null);
                }
            }

        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void breakC4(BlockBreakEvent event) {
        if (MaterialManager.isSkullBlock(event.getBlock())) {
            BlockState state = event.getBlock().getState();
            if (state instanceof Skull) {
                Skull skull;
                try {
                    skull = (Skull) state;
                } catch (ClassCastException var20) {
                    return;
                }

                String ownerOre = skull.getOwner();
                if (ownerOre != null && ownerOre.contains("،")) {
                    String[] refinedOwner = ownerOre.split("،");
                    Block block = event.getBlock();
                    Player breaker = event.getPlayer();
                    Player placer = null;
                    List<Player> candidates = Bukkit.matchPlayer(refinedOwner[1].replace(String.valueOf('ظ'), ""));
                    if (candidates != null && !candidates.isEmpty()) {
                        placer = candidates.get(0);
                    }

                    String world = block.getWorld().getName();
                    String x = String.valueOf(block.getLocation().getBlockX());
                    String y = String.valueOf(block.getLocation().getBlockY());
                    String z = String.valueOf(block.getLocation().getBlockZ());
                    String[] itemInfo = new String[]{"-", world, x, y, z};

                    for (String exploDevID : this.rdelist.keySet()) {
                        if (exploDevID.equals(refinedOwner[0])) {
                            String parent_node = (String) this.rdelist.get(exploDevID);
                            boolean bypassRegions = ConfigCache.getBoolean(parent_node + ".Explosive_Devices.Remote_Bypass_Regions");
                            if (!event.isCancelled() || bypassRegions) {
                                if (breaker != placer) {
                                    this.csminion.callAndResponse(breaker, placer, (Vehicle) null, itemInfo, false);
                                } else {
                                    String msg = ConfigCache.getString(parent_node + ".Explosive_Devices.Message_Disarm");
                                    if (msg != null) {
                                        breaker.sendMessage(msg);
                                    }

                                    block.removeMetadata("CS_transformers", this);
                                    block.setType(Material.AIR);
                                }

                                event.setCancelled(true);
                            }
                            break;
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void liquidContact(BlockFromToEvent event) {
        if (MaterialManager.isSkullBlock(event.getToBlock())) {
            BlockState state = event.getToBlock().getState();
            if (state instanceof Skull) {
                Skull skull;
                try {
                    skull = (Skull) state;
                } catch (ClassCastException var5) {
                    return;
                }

                if (skull.getOwner() != null && skull.getOwner().contains("،")) {
                    event.setCancelled(true);
                }
            }
        }

    }






    @EventHandler
    // Ловушки на сундуках и диспенсерах.
    public void trapCard(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.CHEST && event.getPlayer() instanceof Player && !this.boobs.isEmpty()) {
            Player opener = (Player) event.getPlayer();
            Inventory chest = event.getInventory();
            Block block = null;
            if (chest.getHolder() instanceof Chest) {
                Chest chestHolder = (Chest) chest.getHolder();
                if (chestHolder != null) {
                    block = chestHolder.getBlock();
                }
            } else if (chest.getHolder() instanceof DoubleChest) {
                block = ((DoubleChest) chest.getHolder()).getLocation().getBlock();
            }

            if (block != null) {
                if (block.hasMetadata("CS_btrap")) {
                    event.setCancelled(true);
                } else {
                    ItemStack[] contents = chest.getContents();

                    for (ItemStack susItem : contents) {
                        if (susItem != null && ItemUtils.itemIsSafe(susItem)) {
                            String weaponTitle = NameUtils.getPureName(susItem.getItemMeta().getDisplayName());
                            if (this.boobs.containsKey(weaponTitle)) {
                                String parentNode = this.boobs.get(weaponTitle);
                                if (!this.csminion.getBoobean(1, parentNode)) {
                                    return;
                                }

                                String ammoReading = this.csminion.extractReading(susItem.getItemMeta().getDisplayName());
                                if (!ammoReading.equals("?")) {
                                    Player planter = Bukkit.getServer().getPlayer(ammoReading);
                                    if (planter != event.getPlayer()) {
                                        if (!this.csminion.getBoobean(4, parentNode)) {
                                            susItem.setAmount(susItem.getAmount() - 1);
                                        }

                                        this.slapAndReaction(opener, planter, block, parentNode, chest, contents, ammoReading, (Item) null);
                                        return;
                                    }
                                }
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    public void slapAndReaction(final Player opener, final Player planter, final Block block, final String parent_node, final Inventory chest, final ItemStack[] content, final String planterName, final Item picked) {
        if (!opener.hasMetadata("CS_trigDelay")) {
            if (planter == null) {
                this.activateTrapCard(opener, planter, block, parent_node, chest, content, planterName, picked);
            } else {
                opener.setMetadata("CS_trigDelay", new FixedMetadataValue(this, false));
                this.csminion.tempVars(opener, "CS_trigDelay", 200L);
                opener.setMetadata("CS_singed", new FixedMetadataValue(this, false));
                this.csminion.illegalSlap(planter, opener, 0);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        if (opener.hasMetadata("CS_singed") && opener.getMetadata("CS_singed").get(0).asBoolean()) {
                            opener.removeMetadata("CS_singed", CSDirector.this.plugin);
                            opener.removeMetadata("CS_trigDelay", CSDirector.this.plugin);
                            CSDirector.this.activateTrapCard(opener, planter, block, parent_node, chest, content, planterName, picked);
                        }

                    }
                }, 1L);
            }
        }
    }

    public void activateTrapCard(Player opener, Player planter, Block block, String parent_node, Inventory chest, ItemStack[] content, String planterName, Item picked) {
        boolean unlimited = this.csminion.getBoobean(4, parent_node);
        if (planter != null) {
            CommandUtils.sendPlayerMessage(planter, parent_node, ".Explosive_Devices.Message_Trigger_Placer", planterName, opener.getName(), "<flight>", "<damage>");
            SoundUtils.playSoundEffects(planter, parent_node, ".Explosive_Devices.Sounds_Alert_Placer", false, (Location) null);
        }

        if (picked == null) {
            this.projectileExplosion(null, parent_node, false, planter, false, true, (Location) null, block, true, 0);
            block.setMetadata("CS_btrap", new FixedMetadataValue(this, false));
            if (!unlimited) {
                chest.setContents(content);
            }
        } else {
            this.projectileExplosion(null, parent_node, false, planter, false, true, (Location) null, block.getRelative(BlockFace.DOWN), true, 0);
            if (!unlimited) {
                picked.remove();
            }
        }

        CommandUtils.sendPlayerMessage(opener, parent_node, ".Explosive_Devices.Message_Trigger_Victim", planterName, opener.getName(), "<flight>", "<damage>");
        SoundUtils.playSoundEffects(null, parent_node, ".Explosive_Devices.Sounds_Trigger", false, block.getLocation().add((double) 0.5F, (double) 0.5F, (double) 0.5F));
    }

    @EventHandler
    public void onHopperGulp(InventoryPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (ItemUtils.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains("૮")) {
            event.getItem().remove();
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onTrapDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.DISPENSER) {
            MaterialData data = block.getState().getData();
            Dispenser dispenser = (Dispenser) data;
            BlockFace face = dispenser.getFacing();
            if (this.csminion.boobyAction(block.getRelative(face).getRelative(BlockFace.DOWN), (Entity) null, event.getItem())) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onPressurePlate(EntityInteractEvent event) {
        if (MaterialManager.isPressurePlate(event.getBlock()) && event.getEntity() instanceof LivingEntity) {
            for (Entity e : event.getEntity().getNearbyEntities(4.0F, 4.0F, 4.0F)) {
                if (e instanceof ItemFrame) {
                    this.csminion.boobyAction(event.getBlock(), event.getEntity(), ((ItemFrame) e).getItem());
                }
            }
        }

    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onSplash(PotionSplashEvent event) {
        ThrownPotion splashPot = event.getEntity();
        if (splashPot.hasMetadata("projParentNode")) {
            Entity shooter = (Entity) splashPot.getShooter();
            if (shooter != null && shooter instanceof Player) {
                PermissionAttachment attachment = shooter.addAttachment(this);
                attachment.setPermission("nocheatplus", true);
                attachment.setPermission("anticheat.check.exempt", true);
                String parentNode = splashPot.getMetadata("projParentNode").get(0).asString();
                boolean enableExplode = ConfigCache.getBoolean(parentNode + ".Explosions.Enable");
                boolean impactExplode = ConfigCache.getBoolean(parentNode + ".Explosions.On_Impact_With_Anything");
                if (enableExplode && impactExplode) {
                    this.projectileExplosion(null, parentNode, false, (Player) shooter, false, true, (Location) null, splashPot.getLocation().getBlock(), true, 0);
                }

                for (Entity ent : event.getAffectedEntities()) {
                    if (ent != shooter && !ent.isDead() && !event.isCancelled()) {
                        if (ent instanceof Player) {
                            ent.setMetadata("CS_Energy", new FixedMetadataValue(this, parentNode));
                            ((LivingEntity) ent).damage(0.0F, shooter);
                        } else {
                            this.dealDamage(shooter, (LivingEntity) ent, null, parentNode);
                        }
                    }
                }

                event.setCancelled(true);
                shooter.removeAttachment(attachment);
            }
        }

    }

    public boolean validHotbar(Player shooter, String parent_node) {
        boolean retVal = true;
        String invCtrl = ConfigCache.getString(parent_node + ".Item_Information.Inventory_Control");
        if (invCtrl != null) {
            Inventory playerInv = shooter.getInventory();
            String[] groupList = invCtrl.replaceAll(" ", "").split(",");

            for (String invGroup : groupList) {
                int groupLimit = ConfigCache.getInt(invGroup + ".Limit");
                int groupCount = 0;

                for (int i = 0; i < 9; ++i) {
                    ItemStack checkItem = playerInv.getItem(i);
                    if (checkItem != null && ItemUtils.itemIsSafe(checkItem)) {
                        String[] checkParent = ItemUtils.itemParentNode(checkItem, shooter);
                        if (checkParent != null) {
                            String groupCheck = ConfigCache.getString(checkParent[0] + ".Item_Information.Inventory_Control");
                            if (groupCheck != null && groupCheck.contains(invGroup)) {
                                ++groupCount;
                            }
                        }
                    }
                }

                if (groupCount > groupLimit) {
                    CommandUtils.sendPlayerMessage(shooter, invGroup, ".Message_Exceeded", "<shooter>", "<victim>", "<flight>", "<damage>");
                    SoundUtils.playSoundEffects(shooter, invGroup, ".Sounds_Exceeded", false, null);
                    retVal = false;
                }
            }
        }

        return retVal;
    }

    // Определяет, нужно ли вместо перезарядки/выстрела выбросить itembomb или другое устройство.
    public boolean tossBomb(Player player, String parentNode, ItemStack heldItem, boolean rdeEnable) {

        String type = ConfigCache.getString(parentNode + ".Explosive_Devices.Device_Type");

        if (!rdeEnable || type == null || !type.equalsIgnoreCase("itembomb")) {

            return false;
        }

        int gunSlot = player.getInventory().getHeldItemSlot();

        String metaTag = parentNode + "shootDelay" + gunSlot;

        if (player.hasMetadata(metaTag)) {
            return false;
        }

        player.setMetadata(metaTag, new FixedMetadataValue(this, true));

        csminion.tempVars(player, metaTag, (long) ConfigCache.getInt(parentNode + ".Shooting.Delay_Between_Shots"));

        String deviceInfoString = ConfigCache.getString(parentNode + ".Explosive_Devices.Device_Info");

        if (deviceInfoString == null) {
            return false;
        }

        ItemBombDeviceInfo info;

        try {
            info = itemBombDeviceParser.parse(deviceInfoString);
        } catch (Exception ex) {
            printM("Invalid Device_Info in weapon '" + parentNode + "'");
            return false;
        }

        boolean ammoEnable = ConfigCache.getBoolean(parentNode + ".Ammo.Enable");

        boolean takeAmmo = ConfigCache.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");

        String ammoInfo = ConfigCache.getString(parentNode + ".Ammo.Ammo_Item_ID");

        int detectedAmmo = 0;

        String bracketInfo = csminion.extractReading(heldItem.getItemMeta().getDisplayName());

        try {
            detectedAmmo = Integer.parseInt(bracketInfo);
        } catch (NumberFormatException ignored) {
        }

        if (detectedAmmo <= 0) {
            return true;
        }

        if (ammoEnable && takeAmmo) {

            if (!csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {
                SoundUtils.playSoundEffects(player, parentNode, ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);
                return true;
            }

            csminion.replaceBrackets(heldItem, String.valueOf(detectedAmmo - 1), parentNode);

            csminion.removeNamedItem(player, ammoInfo, 1, parentNode, false);

        } else {
            csminion.replaceBrackets(heldItem, String.valueOf(detectedAmmo - 1), parentNode);
        }

        Item itemBomb = player.getWorld().dropItem(player.getEyeLocation(), info.getBombItem());

        itemBomb.setVelocity(player.getEyeLocation().getDirection().multiply(info.getSpeed()));

        itemBomb.setPickupDelay(24000);

        SoundUtils.playSoundEffects(player, parentNode, ".Explosive_Devices.Sounds_Deploy", false, null);

        String playerName = player.getName();

        Map<String, ArrayDeque<Item>> playerBombs = itembombs.computeIfAbsent(playerName, k -> new HashMap<>());

        ArrayDeque<Item> bombs = playerBombs.computeIfAbsent(parentNode, k -> new ArrayDeque<>());

        bombs.add(itemBomb);

        if (bombs.size() > info.getMaxBombs()) {

            Item oldBomb = bombs.removeFirst();

            if (oldBomb != null) {
                oldBomb.remove();
            }
        }

        ItemStack bombStack = itemBomb.getItemStack();

        csminion.setItemName(bombStack, playerName + "૮૮" + itemBomb.getUniqueId());

        itemBomb.setItemStack(bombStack);

        ProjectileUtils.callShootEvent(player, itemBomb, parentNode);

        return true;
    }

    // Детонация C4 и itembomb по команде игрока.
    public void detonateC4(Player shooter, ItemStack item, String parentNode, String deviceType) {

        List<String> lore = null;

        String playerName = shooter.getName();

        boolean rdeFound = false;
        boolean itemMode = false;
        boolean noneToBoom = true;

        ItemBombDeviceInfo bombInfo = null;

        if ("itembomb".equalsIgnoreCase(deviceType)) {

            String itemName = item.getItemMeta().getDisplayName();

            String deviceInfoString = ConfigCache.getString(parentNode + ".Explosive_Devices.Device_Info");

            if (deviceInfoString == null) {
                return;
            }

            try {
                bombInfo = itemBombDeviceParser.parse(deviceInfoString);
            } catch (Exception ex) {
                printM("Invalid Device_Info for weapon '" + parentNode + "'");
                return;
            }

            if (itemName.contains("«" + bombInfo.getMaxBombs() + "»")) {
                return;
            }

            rdeFound = true;
            itemMode = true;

            Map<String, ArrayDeque<Item>> playerBombs = itembombs.get(playerName);

            if (playerBombs != null) {

                int delay = ConfigCache.getInt(parentNode + ".Explosions.Explosion_Delay");

                ArrayDeque<Item> bombs = playerBombs.get(parentNode);

                if (bombs != null) {

                    while (!bombs.isEmpty()) {

                        noneToBoom = false;
                        Item bomb = bombs.removeFirst();
                        SoundUtils.playSoundEffects(bomb, parentNode, ".Explosive_Devices.Sounds_Trigger", false, null);
                        projectileExplosion(bomb, parentNode, false, shooter, false, false, null, null, false, 0);

                        ItemStack detItem = bombInfo.getDetonatorItem().clone();
                        detItem.setItemMeta(bomb.getItemStack().getItemMeta());
                        bomb.setItemStack(detItem);
                        ProjectileUtils.prepareTermination(bomb,true, (long) delay);
                    }

                    playerBombs.remove(parentNode);
                }
            }

        } else if (item.getItemMeta().hasLore()) {

            lore = item.getItemMeta().getLore();

            Iterator<String> iterator = lore.iterator();

            while (iterator.hasNext()) {

                String line = iterator.next();

                if (!line.contains(String.valueOf('᎐'))) {
                    continue;
                }

                line = line.replace(" ", "");
                line = line.replace("]§r§e", "]§e");

                String[] itemInfo = line.split("]§e|\\᎐|,");
                csminion.detonateRDE(shooter, null, itemInfo, true);
                iterator.remove();
                rdeFound = true;
            }
        }

        if (!rdeFound) {
            return;
        }

        String capacity = "0";

        if (itemMode && bombInfo != null) {

            capacity = String.valueOf(bombInfo.getMaxBombs());

        } else {

            String[] refinedOre = csminion.returnRefinedOre(shooter, parentNode);

            if (refinedOre != null) {
                capacity = refinedOre[0];
            }
        }

        if (!itemMode || !noneToBoom) {
            SoundUtils.playSoundEffects(shooter, parentNode, ".Explosive_Devices.Sounds_Alert_Placer", false, null);
        }

        if (!ConfigCache.getBoolean(parentNode + ".Extras.One_Time_Use")) {

            csminion.replaceBrackets(item, capacity, parentNode);

        } else if (
                item.getItemMeta().getDisplayName() != null
                        && item.getItemMeta().getDisplayName().contains("«0»")
        ) {
            shooter.getInventory().setItemInHand(null);
            shooter.updateInventory();
            return;
        }
        if (!itemMode) {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(lore);
            item.setItemMeta(meta);
            shooter.getInventory().setItemInHand(item);
        }
    }

    // Исправляет display name повреждённых/частично сломанных оружейных предметов.
    public void checkCorruption(ItemStack item, boolean isAttachment, boolean isDual) {
        String itemName = item.getItemMeta().getDisplayName();
        boolean noBracket = !itemName.contains("«");
        boolean noArrow = isAttachment && !itemName.contains(String.valueOf('◀')) && !itemName.contains(String.valueOf('◁'));
        if (noBracket || noArrow) {
            Pattern pattern = Pattern.compile("-?\\d+");
            int startingPos = !isAttachment && !isDual ? itemName.lastIndexOf(" ") : StringUtils.getLastChar(itemName, ' ', 3);
            String[] bracketInfo = itemName.substring(startingPos + 1).split(" ");
            String[] ammo = new String[]{"", "", ""};
            if (!isAttachment && !isDual) {
                Matcher matcher = pattern.matcher(bracketInfo[0]);
                ammo[0] = matcher.find() ? matcher.group() : String.valueOf('×');
                itemName = itemName.substring(0, startingPos + 1) + "«" + ammo[0] + "»";
            } else {
                for (int i = 0; i < 3; i += 2) {
                    Matcher matcher = pattern.matcher(bracketInfo[i]);
                    ammo[i] = matcher.find() ? matcher.group() : String.valueOf('×');
                }

                String splitter = isDual ? " | " : " ◀▷ ";
                itemName = itemName.substring(0, startingPos + 1) + "«" + ammo[0] + splitter + ammo[2] + "»";
            }

            this.csminion.setItemName(item, itemName);
        }

    }

    // Возвращает текущий боезапас/ёмкость через event, чтобы другие плагины могли вмешаться.
    public int getReloadAmount(Player player, String weaponTitle, ItemStack item) {
        int capacity = ConfigCache.getInt(weaponTitle + ".Reload.Reload_Amount");
        WeaponCapacityEvent event = new WeaponCapacityEvent(player, weaponTitle, item, capacity);
        this.getServer().getPluginManager().callEvent(event);
        return event.getCapacity();
    }

}
