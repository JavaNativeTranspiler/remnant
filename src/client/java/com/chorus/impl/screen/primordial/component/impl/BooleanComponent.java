package com.chorus.impl.screen.primordial.component.impl;

import chorus0.Chorus;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.animation.Animation;
import com.chorus.api.system.render.animation.EasingType;
import com.chorus.impl.screen.primordial.component.Component;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

@Getter
public class BooleanComponent extends Component {

    private final BooleanSetting setting;

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        this.setHeight(15);
    }

    Animation colorAnimation = new Animation(EasingType.LINEAR, 250);
    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
//        Render2DEngine.drawRect(context.getMatrices(), getX(), getY(), getWidth(), getHeight(), Color.RED);
        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), setting.getName(), getX() + 16, getY() + 4, 6, 0xFFEBEBEB);
        double animation = colorAnimation.getValue();
        Render2DEngine.drawRoundedRect(context.getMatrices(), getX() + 5, getY() + 3.5f, getHeight() - 7, getHeight() - 7, 2f, new Color(0xFF141414));
        Render2DEngine.drawRoundedRect(context.getMatrices(), getX() + 5, getY() + 3.5f, getHeight() - 7, getHeight() - 7, 2f, new Color(184, 112, 242, (int) (255 * animation)));
        colorAnimation.run(setting.getValue() ? 1 : 0);
        if (!setting.getValue()) {
            colorAnimation.setStartPoint(1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            setting.setValue(!setting.getValue());
        }
        return isHovered(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

    }
}
