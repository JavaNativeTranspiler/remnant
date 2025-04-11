package com.chorus.impl.screen.primordial.component.impl;

import chorus0.Chorus;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.animation.Animation;
import com.chorus.api.system.render.animation.EasingType;
import com.chorus.impl.screen.primordial.component.Component;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

@Getter
public class MultiComponent extends Component {

    private boolean open = false;
    private final MultiSetting setting;

    public MultiComponent(MultiSetting multiSetting) {
        this.setting = multiSetting;
        this.setHeight(24);
    }
    Animation animation = new Animation(EasingType.LINEAR, 250);
    @Override
    public void render(DrawContext context, int mouseX, int mouseY) {
        float extraHeight = 9 * setting.getModes().size();
       this.setHeight((float) (24 + (extraHeight * animation.getValue())));

        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), setting.getName(), getX() + 5, getY() + 2.5f, 6, 0xFFEBEBEB);
        Render2DEngine.drawRoundedRect(context.getMatrices(), getX() + 5, getY() + 12, getWidth() - 10, getHeight() - 15, 1.5f, new Color(0xFF141414));
        String selectedModes = setting.getValue().isEmpty()
                ? "None selected"
                : String.join(", ", setting.getValue());

        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), selectedModes, getX() + 7, getY() + 12.5f, 6, 0xFFBBBBBB);
        animation.run(open ? 1 : 0);
        if (!open) {
            animation.setStartPoint(1);
        }
        float y = 21.5f + (!open ? (9 * setting.getModes().size()) : 0);
        for (String mode : setting.getModes()) {
            if (getY() + (y * animation.getValue()) > getY() + 15.5)
                Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), mode, getX() + 7, (float) (getY() + (y * animation.getValue())), 6, setting.getSpecificValue(mode) ? 0xFFB870F2 : 0xFF707070);
            y += open ? 9 : -9;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            if (mouseY <= getY() + 21) {
                open = !open;
            }
            if (open) {
                float y = 21.5f;
                for (String mode : setting.getModes()) {
                    if (mouseY >= getY() + y && mouseY <= getY() + y + 9) {
                        if (setting.getSpecificValue(mode)) {
                            setting.deselectMode(mode);
                        } else {
                            setting.selectMode(mode);
                        }
                    }
                    y += 9;
                }
            }
        }
        return isHovered(mouseX, mouseY);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}
}