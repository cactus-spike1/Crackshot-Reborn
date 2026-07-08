package fun.cactus.utils;

import com.shampaggon.crackshot.CSDirector;
import fun.cactus.utils.config.ConfigCache;
import fun.cactus.utils.weapon.WeaponAttachmentUtils;
import fun.cactus.utils.weapon.WeaponHelperUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class AmmoUtils {
    private AmmoUtils(){}

    // Считывает текущее число патронов прямо из display name оружия.
    public static int getAmmoBetweenBrackets(Player player, String parent_node, ItemStack item) {
        boolean reloadEnable = ConfigCache.getBoolean(parent_node + ".Reload.Enable");
        boolean dualWield = WeaponHelperUtils.isDualWield(player, parent_node, item);
        int reloadAmt = getPlugin().getReloadAmount(player, parent_node, item);
        String replacer = dualWield ? reloadAmt + " | " + reloadAmt : String.valueOf(reloadAmt);
        if (dualWield) {
            reloadAmt *= 2;
        }

        String attachType = WeaponAttachmentUtils.getAttachment(parent_node, item)[0];
        String bracketInfo = getPlugin().csminion.extractReading(item.getItemMeta().getDisplayName());
        int detectedAmmo = reloadAmt;

        try {
            if (attachType != null) {
                int[] ammoReading = grabDualAmmo(item, parent_node);
                if (attachType.equalsIgnoreCase("main")) {
                    detectedAmmo = ammoReading[0];
                } else if (attachType.equalsIgnoreCase("accessory")) {
                    detectedAmmo = ammoReading[1];
                }
            } else if (dualWield) {
                String ammoReading = bracketInfo.replaceAll(" ", "");
                String[] dualAmmo = ammoReading.split("\\|");
                if (dualAmmo[0].equals(String.valueOf('×')) || dualAmmo[1].equals(String.valueOf('×'))) {
                    return 125622;
                }

                detectedAmmo = Integer.parseInt(dualAmmo[0]) + Integer.parseInt(dualAmmo[1]);
            } else {
                if (bracketInfo.equals(String.valueOf('×')) && !reloadEnable) {
                    return 125622;
                }

                detectedAmmo = Integer.parseInt(bracketInfo);
            }
        } catch (Exception e) {
            getPlugin().csminion.replaceBrackets(item, replacer, parent_node);
        }

        if (detectedAmmo > reloadAmt) {
            getPlugin().csminion.replaceBrackets(item, replacer, parent_node);
        }

        return detectedAmmo;
    }

    // Читает боезапас для обоих стволов из общего display name dual wield оружия.
    public static int[] grabDualAmmo(ItemStack item, String parentNode) {
        try {
            String strInBracks = getPlugin().csminion.extractReading(item.getItemMeta().getDisplayName());
            String[] dualAmmo = strInBracks.split(" ");
            if (dualAmmo.length != 3) {
                getPlugin().csminion.resetItemName(item, parentNode);
                strInBracks = getPlugin().csminion.extractReading(item.getItemMeta().getDisplayName());
                dualAmmo = strInBracks.split(" ");
            }

            int leftGun;
            if (dualAmmo[0].equals(String.valueOf('×'))) {
                leftGun = 1;
            } else {
                leftGun = Integer.valueOf(dualAmmo[0]);
            }

            int rightGun;
            if (dualAmmo[2].equals(String.valueOf('×'))) {
                rightGun = 1;
            } else {
                rightGun = Integer.valueOf(dualAmmo[2]);
            }

            return new int[]{leftGun, rightGun};
        } catch (NumberFormatException var7) {
            return new int[2];
        }
    }

    public static void applyAbilityKnockback(LivingEntity victim, Entity attacker, String parentNode, int knockBack) {
        if (knockBack == 0 || attacker == null) {
            return;
        }
        double factorX = ConfigCache.getConfigDouble(parentNode + ".Abilities.Knockback_View_X", 1.0D);
        double factorY = ConfigCache.getConfigDouble(parentNode + ".Abilities.Knockback_View_Y", 1.0D);
        double factorZ = ConfigCache.getConfigDouble(parentNode + ".Abilities.Knockback_View_Z", 1.0D);
        Vector viewDirection;
        if (attacker instanceof LivingEntity) {
            viewDirection = ((LivingEntity) attacker).getEyeLocation().getDirection().normalize();
        } else {
            viewDirection = attacker.getLocation().getDirection().normalize();
        }

        Vector velocity = new Vector(
                viewDirection.getX() * knockBack * factorX,
                viewDirection.getY() * knockBack * factorY,
                viewDirection.getZ() * knockBack * factorZ
        );
        victim.setVelocity(velocity);
        return;
    }

    private static CSDirector getPlugin() {
        return CSDirector.getInstance();
    }
}
