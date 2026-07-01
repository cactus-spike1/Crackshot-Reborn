package com.shampaggon.crackshot.compatibility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Sound;

/**
 * Кэширует доступные значения enum Sound, чтобы не искать их через рефлексию каждый раз.
 */
public class SoundCache {
    private static final Map<String, Object> SOUND_MAP = new HashMap();

    // Загружает все доступные названия звуков из текущей версии Bukkit API.
    public static void init() {
        try {
            Class<?> soundClass = Class.forName("org.bukkit.Sound");
            if (soundClass.isEnum()) {
                Object[] enumConstants = soundClass.getEnumConstants();
                for (Object constant : enumConstants) {
                    String name = ((Enum) constant).name();
                    SOUND_MAP.put(name, constant);
                }
                return;
            }
            for (Field field : soundClass.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && soundClass.isAssignableFrom(field.getType())) {
                    String name2 = field.getName();
                    Object value = field.get(null);
                    SOUND_MAP.put(name2, value);
                }
            }
        } catch (Exception e) {
            System.out.println("[CrackShot] Failed to load sounds.");
            e.printStackTrace();
        }
    }

    // Возвращает звук по имени либо null, если такого значения нет в текущем API.
    public static Sound getSound(String name) {
        if (!SOUND_MAP.containsKey(name)) {
            return null;
        }
        return (Sound) SOUND_MAP.get(name);
    }
}
