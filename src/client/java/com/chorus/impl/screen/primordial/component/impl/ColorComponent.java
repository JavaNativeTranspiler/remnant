package com.chorus.impl.screen.primordial.component.impl;

import chorus0.Chorus;
import com.chorus.api.module.setting.implement.ColorSetting;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.animation.Animation;
import com.chorus.api.system.render.animation.EasingType;
import com.chorus.impl.screen.primordial.component.Component;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

@Getter
public class ColorComponent extends Component {
    private final ColorSetting setting;
    private boolean expanded = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private boolean draggingSaturationBrightness = false;
    private final Animation expandAnimation = new Animation(EasingType.LINEAR, 250);

    public ColorComponent(ColorSetting setting) {
        this.setting = setting;
        this.setHeight(20);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        float expandProgress = (float) expandAnimation.getValue();
        this.setHeight(20 + (80 * expandProgress));

        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), 
            setting.getName(), 
            getX() + 16, 
            getY() + 4, 
            6, 
            0xFFEBEBEB);

        Render2DEngine.drawRoundedRect(context.getMatrices(),
            getX() + 5,
            getY() + 3.5f,
            getHeight() - 7,
            getHeight() - 7,
            2f,
            setting.getValue());

        if (expanded) {
            expandAnimation.run(1);
            float baseY = getY() + 20;

            Render2DEngine.drawRoundedRect(context.getMatrices(),
                getX() + 5,
                baseY,
                getWidth() - 10,
                60,
                2f,
                new Color(0xFF141414));

            float hueSliderY = baseY + 65;
            Render2DEngine.drawRoundedRect(context.getMatrices(),
                getX() + 5,
                hueSliderY,
                getWidth() - 10,
                5,
                1f,
                new Color(0xFF141414));

            float alphaSliderY = hueSliderY + 10;
            Render2DEngine.drawRoundedRect(context.getMatrices(),
                getX() + 5,
                alphaSliderY,
                getWidth() - 10,
                5,
                1f,
                new Color(0xFF141414));
        } else {
            expandAnimation.run(0);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (mouseY <= getY() + 20) {
                expanded = !expanded;
                return true;
            }

            if (expanded) {
                float baseY = getY() + 20;
                
                if (mouseY >= baseY && mouseY <= baseY + 60) {
                    draggingSaturationBrightness = true;
                    updateColor(mouseX, mouseY);
                    return true;
                }

                float hueSliderY = baseY + 65;
                if (mouseY >= hueSliderY && mouseY <= hueSliderY + 5) {
                    draggingHue = true;
                    updateColor(mouseX, mouseY);
                    return true;
                }

                float alphaSliderY = hueSliderY + 10;
                if (mouseY >= alphaSliderY && mouseY <= alphaSliderY + 5) {
                    draggingAlpha = true;
                    updateColor(mouseX, mouseY);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingHue = false;
        draggingAlpha = false;
        draggingSaturationBrightness = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingHue || draggingAlpha || draggingSaturationBrightness) {
            updateColor(mouseX, mouseY);
        }
    }

    private void updateColor(double mouseX, double mouseY) {
        if (draggingSaturationBrightness) {
            float baseY = getY() + 20;
            float saturation = (float) (mouseX - (getX() + 5)) / (getWidth() - 10);
            float brightness = 1 - (float) (mouseY - baseY) / 60;
            
            saturation = Math.max(0, Math.min(1, saturation));
            brightness = Math.max(0, Math.min(1, brightness));
            
            float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
            Color newColor = Color.getHSBColor(hsb[0], saturation, brightness);
            setting.setColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), setting.getAlpha());
        } else if (draggingHue) {
            float hue = (float) (mouseX - (getX() + 5)) / (getWidth() - 10);
            hue = Math.max(0, Math.min(1, hue));
            
            float[] hsb = Color.RGBtoHSB(setting.getRed(), setting.getGreen(), setting.getBlue(), null);
            Color newColor = Color.getHSBColor(hue, hsb[1], hsb[2]);
            setting.setColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), setting.getAlpha());
        } else if (draggingAlpha) {
            float alpha = (float) (mouseX - (getX() + 5)) / (getWidth() - 10);
            alpha = Math.max(0, Math.min(1, alpha));
            
            setting.setColor(setting.getRed(), setting.getGreen(), setting.getBlue(), (int) (alpha * 255));
        }
    }
} 