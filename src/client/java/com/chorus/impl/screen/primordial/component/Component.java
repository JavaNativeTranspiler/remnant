package com.chorus.impl.screen.primordial.component;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;

public abstract class Component {

    @Getter
    @Setter
    private float x, y, width, height;

    public abstract void render(DrawContext context, int mouseX, int mouseY);
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    public abstract void mouseReleased(double mouseX, double mouseY, int button);
    public abstract void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {}

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
