package com.chorus.api.module.setting;

import com.chorus.api.module.setting.implement.SettingCategory;

import java.util.function.Supplier;

public interface Setting<T> {
    T getDefaultValue();
    String getDescription();
    String getName();
    T getValue();
    void setValue(T value);
    SettingCategory getParent();

    Supplier<Boolean> getRenderCondition();
    void setRenderCondition(Supplier<Boolean> condition);
    default boolean shouldRender() {
        if (getParent() != null) {
            if (!getParent().shouldRender())
                return false;
        }
        return getRenderCondition().get();
    }
}