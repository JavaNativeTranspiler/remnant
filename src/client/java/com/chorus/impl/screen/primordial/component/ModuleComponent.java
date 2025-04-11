package com.chorus.impl.screen.primordial.component;

import com.chorus.api.module.Module;
import com.chorus.api.module.setting.Setting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.impl.screen.primordial.component.impl.CategoryComponent;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {

    @Getter
    private final Module module;
    private final List<CategoryComponent> components = new ArrayList<>();
    private float scrollHeight = 0;

    public ModuleComponent(Module module) {
        this.module = module;
        List<Setting<?>> noParent = module.getSettingRepository().getSettings().values().stream().filter(setting -> setting.getParent() == null && !(setting instanceof SettingCategory)).toList();
        if (!noParent.isEmpty())
            components.add(new CategoryComponent("General", noParent));
        for (Setting<?> setting : module.getSettingRepository().getSettings().values().stream().filter(setting -> setting instanceof SettingCategory).toList()) {
            SettingCategory categorySetting = (SettingCategory) setting;
            List<Setting<?>> children = module.getSettingRepository().getSettings().values().stream().filter(s -> s.getParent() != null && s.getParent() == categorySetting).toList();
            components.add(new CategoryComponent(categorySetting.getName(), children));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        context.enableScissor((int)getX(), (int)getY(), (int)(getX() + getWidth()), (int) (getY() + getHeight()));

        float startX = getX() + 5;
        float startY = getY() + 5;
        float width = (getWidth() - 15) / 2;
        float spacing = 5;

        float leftX = startX;
        float rightX = startX + width + spacing;
        float leftY = startY;
        float rightY = startY;

        float totalHeight = 0;
        for (CategoryComponent component : components) {
            totalHeight += component.getHeight() + spacing;
        }

        if (!components.isEmpty()) totalHeight -= spacing;

        float availableScrollSpace = -Math.max(0, totalHeight - getHeight());
        scrollHeight = Math.max(scrollHeight, availableScrollSpace - 15);

        if (totalHeight <= getHeight()) scrollHeight = 0;

        for (CategoryComponent component : components) {
            float componentHeight = component.getHeight();

            if (leftY <= rightY) {
                component.setBounds(leftX, leftY + scrollHeight, width, componentHeight);
                leftY += componentHeight + spacing;
            } else {
                component.setBounds(rightX, rightY + scrollHeight, width, componentHeight);
                rightY += componentHeight + spacing;
            }

            component.render(context, mouseX, mouseY);
        }

        Render2DEngine.drawVerticalGradient(context.getMatrices(), getX(), getY() + getHeight() - 20, getWidth(), 20, new Color(0x00171717, true), new Color(0xDD171717, true));
        context.disableScissor();
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

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollHeight += (float) verticalAmount * 5;
        scrollHeight = Math.min(scrollHeight, 0);

        super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
