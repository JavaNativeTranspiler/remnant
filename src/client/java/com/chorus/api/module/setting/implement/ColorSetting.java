package com.chorus.api.module.setting.implement;

import com.chorus.api.module.setting.AbstractSetting;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class ColorSetting extends AbstractSetting<Color> {
    private final String name;
    private final String description;
    private Color value;
    private final Color defaultValue;
    private SettingCategory parent;

    public ColorSetting(String name, String description, Color defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public ColorSetting(SettingCategory parent, String name, String description, Color defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.parent = parent;
    }

    public void setColor(int red, int green, int blue, int alpha) {
        this.value = new Color(red, green, blue, alpha);
    }

    public void setColor(int red, int green, int blue) {
        this.value = new Color(red, green, blue);
    }

    public int getRed() {
        return value.getRed();
    }

    public int getGreen() {
        return value.getGreen();
    }

    public int getBlue() {
        return value.getBlue();
    }

    public int getAlpha() {
        return value.getAlpha();
    }

    public int getRGB() {
        return value.getRGB();
    }
} 