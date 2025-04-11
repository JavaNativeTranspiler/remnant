
/**
 * Created: 12/8/2024
 */
package com.chorus.api.module.setting.implement;

import com.chorus.api.module.setting.AbstractSetting;
import lombok.Getter;

@Getter
public class StringSetting extends AbstractSetting<String> {
    private final String name;
    private final String description;
    private String       value;
    private final String defaultValue;
    private SettingCategory parent;

    public StringSetting(String name, String description, String defaultValue) {
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
    }

    public StringSetting(SettingCategory parent, String name, String description, String defaultValue) {
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.parent = parent;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com