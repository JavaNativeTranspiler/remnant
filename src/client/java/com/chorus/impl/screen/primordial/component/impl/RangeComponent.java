package com.chorus.impl.screen.primordial.component.impl;

import chorus0.Chorus;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.impl.screen.primordial.component.Component;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class RangeComponent extends Component {

    private boolean draggingMin;
    private boolean draggingMax;

    private final RangeSetting<? extends Number> setting;

    public RangeComponent(RangeSetting<?> setting) {
        this.setting = setting;
        this.setHeight(20);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), setting.getName(), getX() + 5, getY() + 2.5f, 6, 0xFFEBEBEB);
        String value = String.format("%.2f", setting.getValueMin().floatValue()) + "-" + String.format("%.2f", setting.getValueMax().floatValue());
        float valueWidth = Chorus.getInstance().getFonts().getInterMedium().getWidth(value, 6);
        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), value, getX() + getWidth() - 5 - valueWidth, getY() + 2.5f, 6, 0xFF909090);

        Render2DEngine.drawRoundedRect(context.getMatrices(), getX() + 5, getY() + 13, getWidth() - 10, getHeight() - 17, 1f, new Color(0xFF141414));

        float minX = getX() + 5 + (getWidth() - 10) * (setting.getValueMin().floatValue() / setting.getMax().floatValue());
        float maxX = getX() + 5 + (getWidth() - 10) * (setting.getValueMax().floatValue() / setting.getMax().floatValue());
        float width = maxX - minX;
        Render2DEngine.drawRoundedRect(context.getMatrices(), minX, getY() + 13, width, getHeight() - 17, 1f, new Color(0xFFB870F2));
        Render2DEngine.drawRoundedRect(context.getMatrices(), minX - 1, getY() + 12, 2, getHeight() - 15, 0.5f, new Color(0xFFFDF3FF));
        Render2DEngine.drawRoundedRect(context.getMatrices(), maxX - 1, getY() + 12, 2, getHeight() - 15, 0.5f, new Color(0xFFFDF3FF));
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && (button == 0)) {
            double relativeX = mouseX - (getX() + 5);

            double minHandleX = (getWidth() - 10) * (setting.getValueMin().doubleValue() - setting.getMin().doubleValue())
                    / (setting.getMax().doubleValue() - setting.getMin().doubleValue());
            double maxHandleX = (getWidth() - 10) * (setting.getValueMax().doubleValue() - setting.getMin().doubleValue())
                    / (setting.getMax().doubleValue() - setting.getMin().doubleValue());

            double handleWidth = 2;
            double halfHandleWidth = handleWidth / 2.0;

            if (Math.abs(relativeX - minHandleX) < halfHandleWidth) {
                draggingMin = true;
            } else if (Math.abs(relativeX - maxHandleX) < halfHandleWidth) {
                draggingMax = true;
            } else {
                draggingMin = Math.abs(relativeX - minHandleX) < Math.abs(relativeX - maxHandleX);
                draggingMax = !draggingMin;
            }

            slide(mouseX);
        }

        return isHovered(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingMin = false;
        draggingMax = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingMin || draggingMax) {
            slide(mouseX);
        }
    }

    private Number convertToSettingType(double value) {
        if (setting.getValueMin() instanceof Integer) {
            return (int) Math.round(value);
        } else if (setting.getValueMin() instanceof Float) {
            return (float) value;
        } else if (setting.getValueMin() instanceof Double) {
            return value;
        } else if (setting.getValueMin() instanceof Long) {
            return Math.round(value);
        }
        return value;
    }

    private void slide(double mouseX) {
        double relativeX = mouseX - (getX() + 5);
        double range = setting.getMax().doubleValue() - setting.getMin().doubleValue();

        double relativeMin = MathHelper.clamp(relativeX / (getWidth() - 10), 0, 1);
        double relativeMax = MathHelper.clamp((relativeX + 0.01) / (getWidth() - 10), 0, 1);

        double valueMin = relativeMin * range + setting.getMin().doubleValue();
        double valueMax = relativeMax * range + setting.getMin().doubleValue();

        if (draggingMin && !draggingMax) {
            valueMin = Math.min(valueMin, setting.getValueMax().doubleValue());
            setting.setValue(new Number[]{convertToSettingType(valueMin), setting.getValueMax()});
        } else if (draggingMax && !draggingMin) {
            valueMax = Math.max(valueMax, setting.getValueMin().doubleValue());
            setting.setValue(new Number[]{setting.getValueMin(), convertToSettingType(valueMax)});
        }
    }
}
