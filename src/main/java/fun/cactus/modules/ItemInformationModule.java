package fun.cactus.modules;

import fun.cactus.utils.enchantment.EnchantmentCheck;
import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.Material;

import java.util.List;

public class ItemInformationModule {
    String itemName;
    Material itemType;
    String[] itemLore;
    int customModelData;
    String inventoryControl;
    boolean meleeMode;
    String meleeAttachment;
    AttachmentsModule attachments;
    EnchantmentCheck enchantmentToCheck;
    boolean skipNameCheck;
    List<SoundEffect> SoundsAcquired;
    boolean removeUnusedTag;
    boolean hiddenFromList;

}
