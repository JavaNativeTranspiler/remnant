package com.chorus.api.module.setting.implement;

import com.chorus.api.module.exception.ModuleException;
import com.chorus.api.module.setting.AbstractSetting;
import com.chorus.common.util.math.MathUtils;
import lombok.Getter;
import net.jodah.typetools.TypeResolver;

import java.util.Arrays;

@Getter
public class RangeSetting<T extends Number & Comparable<T>> extends AbstractSetting<Number[]> {
    private final String name;
    private final String description;
    private T            min;
    private T            max;
    private T            valueMin;
    private T            valueMax;
    private SettingCategory parent;

    public RangeSetting(String name, String description, T min, T max, T valueMin, T valueMax) {
        this.name        = name;
        this.description = description;
        this.min         = min;
        this.max         = max;
        this.valueMin    = valueMin;
        this.valueMax    = valueMax;

        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RangeSetting.class, getClass());

        System.out.println("Resolved type arguments: " + Arrays.toString(typeArgs));
    }

    public RangeSetting(SettingCategory parent, String name, String description, T min, T max, T valueMin, T valueMax) {
        this.parent = parent;
        this.name        = name;
        this.description = description;
        this.min         = min;
        this.max         = max;
        this.valueMin    = valueMin;
        this.valueMax    = valueMax;

        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(RangeSetting.class, getClass());

        //System.out.println("Resolved type arguments: " + Arrays.toString(typeArgs));
    }

    @Override
    public Number[] getDefaultValue() {
        return new Number[] { min, max };
    }

    @Override
    public Number[] getValue() {
        return new Number[] { valueMin, valueMax };
    }

    public Number getRandomValue() {
        return MathUtils.randomNumber(valueMin, valueMax);
    }

    @Override
    public void setValue(Number[] values) {
        if ((values.length != 2) ||!((values[0] instanceof Number) && (values[1] instanceof Number))) {
            throw new ModuleException("Values must be an array of two Numbers for RangeSetting: " + name);
        }

        if (((T) values[0]).compareTo(min) < 0 || ((T) values[1]).compareTo(max) > 0) {
            throw new ModuleException("Values must be within bounds [" + min + ", " + max + "] for RangeSetting: "
                                      + name);
        }

        this.valueMin = (T) values[0];
        this.valueMax = (T) values[1];
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
