package com.chorus.api.module.setting.implement;

import com.chorus.api.module.setting.AbstractSetting;
import lombok.Getter;

@Getter
public class SettingCategory extends AbstractSetting<Object> {

    private final String name;

    public SettingCategory(String name) {
        this.name = name;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void setValue(Object value) {

    }

    @Override
    public SettingCategory getParent() {
        return null;
    }
}
