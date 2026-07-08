package fun.cactus.utils;

import com.shampaggon.crackshot.CSDirector;
import fun.cactus.utils.config.ConfigCache;
import fun.cactus.utils.weapon.WeaponAttachmentUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class ItemUtils {
    private ItemUtils(){}

    public static boolean itemIsSafe(ItemStack item) {
        return item.hasItemMeta() && item.getItemMeta().getDisplayName() != null;
    }

    public static String isSkipNameItem(ItemStack item) {
        String itemInfo = item.getType() + "-" + item.getDurability();
        return getPlugin().convIDs.get(itemInfo);
    }

    public static String convItem(ItemStack item) {
        String retNode = isSkipNameItem(item);
        if (retNode == null && item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            Map<Enchantment, Integer> enchList = item.getEnchantments();

            for (String parentNode : getPlugin().enchlist.keySet()) {
                String[] enchInfo = (String[]) getPlugin().enchlist.get(parentNode);
                Enchantment givenEnch = Enchantment.getByName(enchInfo[0]);
                int enchLevel = Integer.valueOf(enchInfo[1]);
                ItemStack comp = getPlugin().csminion.parseItemStack(ConfigCache.getString(parentNode + ".Item_Information.Item_Type"));
                boolean equal = comp != null && comp.getType() == item.getType() && (comp.getDurability() == item.getDurability() || getPlugin().hasDurab(parentNode));
                if (equal && enchList.containsKey(givenEnch) && (Integer) enchList.get(givenEnch) == enchLevel) {
                    retNode = parentNode;
                    break;
                }
            }
        }

        return retNode;
    }
    // Определяет parent_node оружия по предмету в руке игрока.
    public static String returnParentNode(Player player) {
        String retNode = null;
        ItemStack item = player.getItemInHand();
        if (item == null) {
            return null;
        } else {
            if (itemIsSafe(item)) {
                String parentNode = isSkipNameItem(item);
                if (parentNode == null) {
                    parentNode = getPlugin().parentlist.get(NameUtils.getPureName(item.getItemMeta().getDisplayName()));
                }

                if (parentNode != null) {
                    if (player.getItemInHand().getItemMeta().getDisplayName().contains(String.valueOf('▶'))) {
                        String attachInfo = WeaponAttachmentUtils.getAttachment(parentNode, item)[1];
                        retNode = attachInfo;
                    } else {
                        retNode = parentNode;
                    }
                }
            } else {
                String convNode = convItem(item);
                if (convNode != null && getPlugin().regionAndPermCheck(player, convNode, true)) {
                    getPlugin().csminion.removeEnchantments(item);
                    ItemStack weapon = getPlugin().csminion.vendingMachine(convNode);
                    weapon.setAmount(player.getItemInHand().getAmount());
                    player.setItemInHand(weapon);
                }
            }

            return retNode;
        }
    }

    // Пытается определить оружие по ItemStack, включая skip-name режим и проверки enchant/display.
    public static String[] itemParentNode(ItemStack item, Player player) {
        String[] retVal = null;
        if (itemIsSafe(item)) {
            String parentNode = isSkipNameItem(item);
            if (parentNode == null) {
                parentNode = getPlugin().parentlist.get(NameUtils.getPureName(item.getItemMeta().getDisplayName()));
            }

            if (parentNode != null) {
                if (item.getItemMeta().getDisplayName().contains(String.valueOf('▶'))) {
                    String attachInfo = WeaponAttachmentUtils.getAttachment(parentNode, item)[1];
                    retVal = new String[]{attachInfo, "false"};
                } else {
                    retVal = new String[]{parentNode, "false"};
                }
            }
        } else {
            String convNode = convItem(item);
            if (convNode != null && player != null && getPlugin().regionAndPermCheck(player, convNode, true)) {
                getPlugin().csminion.removeEnchantments(item);
                retVal = new String[]{convNode, "true"};
            }
        }

        return retVal;
    }
    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
