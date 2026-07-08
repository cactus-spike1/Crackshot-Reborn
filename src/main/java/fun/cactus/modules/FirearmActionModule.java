package fun.cactus.modules;

import fun.cactus.utils.FirearmActionsType;
import fun.cactus.utils.sound.SoundEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class FirearmActionModule {
    FirearmActionsType type;
    int openDuration;
    int closeDuration;
    int closeShootDelay;
    int reloadOpenDelay;
    int reloadCloseDelay;
    List<SoundEffect> soundOpen;
    List<SoundEffect> soundClose;

    /**
     * Проверяет текущую позицию затвора из display name оружия
     */
    public ChamberState getChamberState(ItemStack weapon) {
        if (weapon == null || !weapon.hasItemMeta()) {
            return ChamberState.NORMAL;
        }
        
        ItemMeta meta = weapon.getItemMeta();
        String displayName = meta.getDisplayName();
        
        // Ищем символы состояния затвора
        if (displayName.contains("▪")) {
            return ChamberState.CLOSED;
        } else if (displayName.contains("▫")) {
            return ChamberState.OPEN;
        } else if (displayName.contains("˗")) {
            return ChamberState.CHARGING;
        }
        
        return ChamberState.NORMAL;
    }

    /**
     * Устанавливает состояние затвора в display name оружия
     */
    public void setChamberState(ItemStack weapon, ChamberState state) {
        if (weapon == null || !weapon.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = weapon.getItemMeta();
        String displayName = meta.getDisplayName();
        
        // Удаляем старые маркеры
        displayName = displayName.replaceAll("[▪▫˗]", "");
        
        // Добавляем новый маркер
        switch (state) {
            case CLOSED:
                displayName = displayName.replaceAll("«", "▪ «");
                break;
            case OPEN:
                displayName = displayName.replaceAll("«", "▫ «");
                break;
            case CHARGING:
                displayName = displayName.replaceAll("«", "˗ «");
                break;
        }
        
        meta.setDisplayName(displayName);
        weapon.setItemMeta(meta);
    }

    /**
     * Проверяет, требует ли действие затвора время для операции
     */
    public boolean requiresAction() {
        if (type == null) {
            return false;
        }
        
        switch (type) {
            case BOLT:
            case LEVER:
            case PUMP:
            case SLIDE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Получает время открытия затвора
     */
    public int getOpenDuration() {
        return openDuration;
    }

    /**
     * Получает время закрытия затвора
     */
    public int getCloseDuration() {
        return closeDuration;
    }

    /**
     * Валидация конфигурации модуля
     */
    public boolean validate() {
        if (type == null) {
            System.out.println("[CrackShot] FirearmActionModule: Type не установлен!");
            return false;
        }
        
        if (requiresAction()) {
            if (openDuration < 0 || closeDuration < 0) {
                System.out.println("[CrackShot] FirearmActionModule: Длительность должна быть >= 0!");
                return false;
            }
        }
        
        return true;
    }

    public FirearmActionsType getType() {
        return type;
    }

    public List<SoundEffect> getSoundOpen() {
        return soundOpen;
    }

    public List<SoundEffect> getSoundClose() {
        return soundClose;
    }

    public int getCloseShootDelay() {
        return closeShootDelay;
    }

    public int getReloadOpenDelay() {
        return reloadOpenDelay;
    }

    public int getReloadCloseDelay() {
        return reloadCloseDelay;
    }

    /**
     * Перечисление состояний затвора
     */
    public enum ChamberState {
        NORMAL,      // Обычное состояние
        CLOSED,      // Затвор закрыт (▪)
        OPEN,        // Затвор открыт (▫)
        CHARGING     // Идёт зарядка (˗)
    }
}
