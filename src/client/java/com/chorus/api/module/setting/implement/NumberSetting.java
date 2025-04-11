package com.chorus.api.module.setting.implement;

import com.chorus.api.module.exception.ModuleException;
import com.chorus.api.module.setting.AbstractSetting;
import lombok.Getter;
import net.jodah.typetools.TypeResolver;

@Getter
public class NumberSetting<T extends Number & Comparable<T>> extends AbstractSetting<T> {
    private final String name;
    private final String description;
    private T            value;
    private final T      defaultValue;
    private final T      minValue;
    private final T      maxValue;
    private SettingCategory parent;

    public NumberSetting(String name, String description, T defaultValue, T minValue, T maxValue) {
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue     = minValue;
        this.maxValue     = maxValue;

        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(NumberSetting.class, getClass());

        //System.out.println("Resolved type arguments: " + Arrays.toString(typeArgs));
    }

    public NumberSetting(SettingCategory parent, String name, String description, T defaultValue, T minValue, T maxValue) {
        this.name         = name;
        this.description  = description;
        this.value        = defaultValue;
        this.defaultValue = defaultValue;
        this.minValue     = minValue;
        this.maxValue     = maxValue;
        this.parent = parent;

        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(NumberSetting.class, getClass());

        //System.out.println("Resolved type arguments: " + Arrays.toString(typeArgs));
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(T value) {
        if ((value.compareTo(minValue) < 0) || (value.compareTo(maxValue) > 0)) {
            throw new ModuleException("Value " + value + " is out of bounds for setting: " + name
                                      + ". Must be between " + minValue + " and " + maxValue);
        }

        this.value = value;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
