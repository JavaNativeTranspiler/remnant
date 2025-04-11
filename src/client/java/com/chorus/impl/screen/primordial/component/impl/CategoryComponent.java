package com.chorus.impl.screen.primordial.component.impl;

import chorus0.Chorus;
import com.chorus.api.module.setting.Setting;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.impl.screen.primordial.component.Component;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CategoryComponent extends Component {

    private final String name;
    private final List<Setting<?>> settings;
    private final List<Component> components = new ArrayList<>();

    public CategoryComponent(String name, List<Setting<?>> settings) {
        this.name = name;
        this.settings = settings;
        for (Setting<?> setting : settings) {
            if (setting instanceof BooleanSetting booleanSetting) {
                components.add(new BooleanComponent(booleanSetting));
            }
            if (setting instanceof NumberSetting numberSetting) {
                components.add(new SliderComponent(numberSetting));
            }
            if (setting instanceof ModeSetting modeSetting) {
                components.add(new ModeComponent(modeSetting));
            }
            if (setting instanceof RangeSetting rangeSetting) {
                components.add(new RangeComponent(rangeSetting));
            }
            if (setting instanceof MultiSetting multiSetting) {
                components.add(new MultiComponent(multiSetting));
            }
            if (setting instanceof ColorSetting colorSetting) {
                components.add(new ColorComponent(colorSetting));
            }
        }
        float height = 15;
        for (Component component : components) {
            height += component.getHeight();
        }
        this.setHeight(height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        float height = 15;
        for (Component component : components) {
            height += component.getHeight();
        }
        this.setHeight(height);

        Render2DEngine.drawRoundedRect(context.getMatrices(), getX(), getY(), getWidth(), getHeight(), 4, new Color(0x1c1c1c));
        Render2DEngine.drawTopRoundedRect(context.getMatrices(), getX(), getY(), getWidth(), 13, 4, new Color(0x242424));
        Render2DEngine.drawLine(context.getMatrices(), getX(), getY() + 13, getX() + getWidth(), getY() + 13, 1, new Color(0xB870F2));
        Chorus.getInstance().getFonts().getInterSemiBold().render(context.getMatrices(), name, getX() + 4, getY() + 3, 6, 0xFFFFFFFF);

        float y = 0;
        for (Component component : components) {
            component.setBounds(getX(), getY() + 13.5f + y, getWidth(), component.getHeight());
            component.render(context, mouseX, mouseY);
            y += component.getHeight();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        components.forEach(c -> c.mouseClicked(mouseX, mouseY, button));
        return isHovered(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        components.forEach(c -> c.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        components.forEach(c -> c.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
    }
}
