package com.chorus.api.module.setting;

import lombok.Getter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingRepository {
    @Getter
    private final Map<String, Setting<?>> settings = Collections.synchronizedMap(new LinkedHashMap<>());

    public void registerSetting(Setting<?> setting) {
        if (setting != null) {
            settings.put(setting.getName(), setting);
        } else {
            throw new IllegalArgumentException("Cannot register a null setting.");
        }
    }

    public void registerSettings(Setting<?>... settings) {
        if (settings != null) {
            for (Setting<?> setting : settings) {
                registerSetting(setting);
            }
        } else {
            throw new IllegalArgumentException("Cannot register a null array of settings.");
        }
    }

    public void unregisterSetting(Setting<?> setting) {
        if (setting != null) {
            settings.remove(setting.getName());
        } else {
            throw new IllegalArgumentException("Cannot unregister a null setting.");
        }
    }

    public Setting<?> getSetting(String name) {
        return settings.get(name);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
