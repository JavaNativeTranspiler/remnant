package com.chorus.api.module.setting.implement;

import com.chorus.api.module.setting.AbstractSetting;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BooleanSetting extends AbstractSetting<Boolean> {
    private final String  name;
    private final String  description;
    private Boolean       value;
    private final Boolean defaultValue;
    private SettingCategory parent;

    public BooleanSetting(String name, String description, Boolean defaultValue) {
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
    }

    public BooleanSetting(SettingCategory parent, String name, String description, Boolean defaultValue) {
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.parent = parent;
    }

    public void toggle() {
        this.value = !this.value;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
