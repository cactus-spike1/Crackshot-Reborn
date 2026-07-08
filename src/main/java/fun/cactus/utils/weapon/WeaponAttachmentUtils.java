package fun.cactus.utils.weapon;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponAttachmentEvent;
import fun.cactus.utils.config.ConfigCache;
import org.bukkit.inventory.ItemStack;

public final class WeaponAttachmentUtils {
    private WeaponAttachmentUtils(){}
    // Извлекает тип и название активного/подключённого attachment из display name оружия.
    public static String[] getAttachment(String weaponTitle, ItemStack item) {
        String attachType = ConfigCache.getString(weaponTitle + ".Item_Information.Attachments.Type");
        if (attachType != null && !attachType.equalsIgnoreCase("accessory")) {
            String attachment = ConfigCache.getString(weaponTitle + ".Item_Information.Attachments.Info");
            WeaponAttachmentEvent event = new WeaponAttachmentEvent(weaponTitle, item, attachment);
            CSDirector.getInstance().getServer().getPluginManager().callEvent(event);
            return new String[]{event.isCancelled() ? null : attachType, event.getAttachment()};
        } else {
            return new String[]{attachType, null};
        }
    }

}
