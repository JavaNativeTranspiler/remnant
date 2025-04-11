package com.chorus.api.module.setting.implement;

import com.chorus.api.module.exception.ModuleException;
import com.chorus.api.module.setting.AbstractSetting;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class ModeSetting extends AbstractSetting<String> {
    private final String       name;
    private final String       description;
    @Getter
    private String             value;
    private final String       defaultValue;
    private final List<String> modes;
    private SettingCategory parent;

    public ModeSetting(String name, String description, String defaultValue, String... modes) {
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.modes        = Arrays.asList(modes);
    }

    public ModeSetting(SettingCategory parent, String name, String description, String defaultValue, String... modes) {
        this.parent = parent;
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.modes        = Arrays.asList(modes);
    }

    public void cycle() {
        int index = modes.indexOf(value);

        index = (index + 1) % modes.size();
        value = modes.get(index);
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setMode(String mode) {
        if (modes.contains(mode)) {
            this.value = mode;
        } else {
            throw new ModuleException("Invalid mode: " + mode + " for setting: " + name);
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
