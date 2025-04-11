package com.chorus.impl.screen.primordial.component.impl;

import chorus0.Chorus;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.impl.screen.primordial.component.Component;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

@Getter
public class SliderComponent<T extends Number & Comparable<T>> extends Component {

    private final NumberSetting<T> setting;
    public boolean dragging;

    public SliderComponent(NumberSetting<T> setting) {
        this.setting = setting;
        this.setHeight(20);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), setting.getName(), getX() + 5, getY() + 2.5f, 6, 0xFFEBEBEB);
        String value = String.format("%.2f", setting.getValue().floatValue());
        float valueWidth = Chorus.getInstance().getFonts().getInterMedium().getWidth(value, 6);
        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), value, getX() + getWidth() - 5 - valueWidth, getY() + 2.5f, 6, 0xFF909090);

        float width = (getWidth() - 10) * ((setting.getValue().floatValue() - setting.getMinValue().floatValue()) / (setting.getMaxValue().floatValue() - setting.getMinValue().floatValue()));
        Render2DEngine.drawRoundedRect(context.getMatrices(), getX() + 5, getY() + 13, getWidth() - 10, getHeight() - 17, 1f, new Color(0xFF141414));
        Render2DEngine.drawRoundedRect(context.getMatrices(), getX() + 5, getY() + 13, width, getHeight() - 17, 1f, new Color(0xFFB870F2));
        Render2DEngine.drawRoundedRect(context.getMatrices(), getX() + 5 + width - 1, getY() + 12, 2, getHeight() - 15, 0.5f, new Color(0xFFFDF3FF));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && (button == 0)) {
            dragging = true;
            slide(mouseX);
        }

        return isHovered(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) dragging = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            slide(mouseX);
        }
    }

    @SuppressWarnings("unchecked")
    private T convertToSettingType(double value) {
        if (setting.getValue() instanceof Integer) {
            return (T) Integer.valueOf((int) Math.round(value));
        } else if (setting.getValue() instanceof Float) {
            return (T) Float.valueOf((float) value);
        } else if (setting.getValue() instanceof Double) {
            return (T) Double.valueOf(value);
        } else if (setting.getValue() instanceof Long) {
            return (T) Long.valueOf(Math.round(value));
        }
        return (T) Double.valueOf(value);
    }

    private void slide(double mouseX) {
        double relativeX = MathHelper.clamp(mouseX - (getX() + 5), 0, getWidth() - 10);

        double percentage = relativeX / (getWidth() - 10);

        double range = setting.getMaxValue().doubleValue() - setting.getMinValue().doubleValue();
        double newValue = percentage * range + setting.getMinValue().doubleValue();

        setting.setValue(convertToSettingType(newValue));
    }
}
