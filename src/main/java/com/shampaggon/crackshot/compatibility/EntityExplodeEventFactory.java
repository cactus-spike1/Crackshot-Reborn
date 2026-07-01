package com.shampaggon.crackshot.compatibility;

import java.lang.reflect.Constructor;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Фабрика для создания EntityExplodeEvent с учётом различий сигнатур между версиями Bukkit.
 */
public class EntityExplodeEventFactory {
    private static Constructor<? extends EntityExplodeEvent> cachedConstructor;
    private static Object cachedDestroyEnum;

    static {
        cachedConstructor = null;
        cachedDestroyEnum = null;

        // Попытка найти конструктор старой сигнатуры:
        try {
            cachedConstructor = EntityExplodeEvent.class.getConstructor(Entity.class, Location.class, List.class, float.class);;
        } catch (NoSuchMethodException ignored) {
            // Неудача — попробуем новую сигнатуру с ExplosionResult / Explosion.Result
        }

        try {
            // Пытаемся подгрузить тип перечисления результата взрыва (название может меняться между версиями)
            Class<?> explosionResultClass = null;
            try {
                explosionResultClass = Class.forName("org.bukkit.ExplosionResult");
            } catch (ClassNotFoundException cnfe) {
                // Альтернативное имя (в некоторых версиях может быть вложенный класс)
                try {
                    explosionResultClass = Class.forName("org.bukkit.entity.Explosion$Result");
                } catch (ClassNotFoundException ignored) { }
            }

            if (explosionResultClass != null) {
                // Получаем значение enum DESTROY, если оно есть
                try {
                    cachedDestroyEnum = Enum.valueOf((Class<? extends Enum>) explosionResultClass, "DESTROY");
                } catch (Exception ignored) { /* нет такого значения — оставляем null */ }

                // Конструктор с дополнительным параметром результата взрыва
                cachedConstructor = EntityExplodeEvent.class.getConstructor(Entity.class, Location.class, List.class, float.class, explosionResultClass);
            }
        } catch (Exception e) {
            // Любые исключения при рефлексии логируем в консоль
            e.printStackTrace();
        }
    }

    private EntityExplodeEventFactory() {
        // Статическая фабрика — не инстанцируем
    }

    /**
     * Создаёт EntityExplodeEvent, совместимый с различными версиями Bukkit/Spigot.
     *
     * @param entity   источник взрыва (может быть null)
     * @param location место взрыва
     * @param blocks   список блоков, попадающих под взрыв
     * @param yield    сила взрыва
     * @return экземпляр EntityExplodeEvent
     */
    public static EntityExplodeEvent create(Entity entity, Location location, List<Block> blocks, float yield) {
        if (cachedConstructor == null) {
            throw new IllegalStateException("No compatible EntityExplodeEvent constructor found.");
        }

        try {
            if (cachedDestroyEnum == null) {
                // Старая сигнатура
                return cachedConstructor.newInstance(entity, location, blocks, yield);
            } else {
                // Новая сигнатура с указанием результата взрыва
                return cachedConstructor.newInstance(entity, location, blocks, yield, cachedDestroyEnum);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create EntityExplodeEvent", e);
        }
    }
}
