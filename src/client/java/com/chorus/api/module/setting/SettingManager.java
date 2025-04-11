package com.chorus.api.module.setting;

import com.chorus.api.module.exception.ModuleException;
import lombok.Getter;

import java.lang.reflect.Field;

public class SettingManager {
    @Getter
    private final SettingRepository settingRepository = new SettingRepository();

    public void registerSettings(Object module) {
        for (Field field : module.getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    Setting<?> setting = (Setting<?>) field.get(module);

                    if (setting != null) {
                        settingRepository.registerSetting(setting);
                    } else {
                        throw new ModuleException("Setting field " + field.getName() + " is null in "
                                                  + module.getClass().getName());
                    }
                } catch (IllegalAccessException e) {
                    throw new ModuleException("Failed to access setting field: " + field.getName(), e);
                }
            }
        }
    }

    public void unregisterSettings(Object module) {
        for (Field field : module.getClass().getDeclaredFields()) {
            if (Setting.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);

                try {
                    Setting<?> setting = (Setting<?>) field.get(module);

                    if (setting != null) {
                        settingRepository.unregisterSetting(setting);
                    } else {
                        throw new ModuleException("Setting field " + field.getName() + " is null in "
                                                  + module.getClass().getName());
                    }
                } catch (IllegalAccessException e) {
                    throw new ModuleException("Failed to access setting field: " + field.getName(), e);
                }
            }
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
