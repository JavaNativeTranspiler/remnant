package com.chorus.impl.screen.primordial;

import chorus0.Chorus;
import com.chorus.api.module.Module;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.animation.Animation;
import com.chorus.api.system.render.animation.EasingType;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.impl.modules.client.ClickGUI;
import com.chorus.impl.screen.primordial.component.ModuleComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.function.Function;

public class PrimordialScreen extends Screen implements QuickImports {

    private static final float MIN_WIDTH = 415;
    private static final float MIN_HEIGHT = 295;
    private static final float RESIZE_HANDLE_SIZE = 11;

    private float x, y;
    private float width = 600;
    private float height = 380;
    private float moduleY = 0;

    private boolean dragging = false;
    private boolean resizing = false;
    private double lastMouseX;
    private double lastMouseY;

    private final Animation moduleAnimation = new Animation(EasingType.LINEAR, 4000);
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;

    private Module selectedModule;
    private ModuleComponent component;

    @Getter
    private static final PrimordialScreen INSTANCE = new PrimordialScreen();

    public PrimordialScreen() {
        super(Text.empty());

        this.x = Math.max((mc.getWindow().getScaledWidth() - width) / 2f, 10);
        this.y = Math.max((mc.getWindow().getScaledHeight() - height) / 2f, 10);

        selectedModule = Chorus.getInstance().getModuleManager().getModules().stream().filter(m -> m.getCategory() == selectedCategory).toList().getFirst();
        component = new ModuleComponent(selectedModule);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        MatrixStack matrices = context.getMatrices();

        drawBackgroundAndBorders(context, matrices, mouseX, mouseY);
        drawResizeHandle(context);
        drawCategorySelector(matrices);
        context.enableScissor((int) x, (int) y + 28, (int) x + 75, (int) (y + height - 38));
        drawModuleSelector(matrices);
        context.disableScissor();
    }

    private void drawBackgroundAndBorders(DrawContext context, MatrixStack matrices, int mouseX, int mouseY) {
        Render2DEngine.drawRoundedRect(matrices, x, y, width, height, 7.5f, new Color(0xFF1c1c1c));

        int innerHeight = (int) (height - 65);
        int innerY = (int) (y + 27);
        Render2DEngine.drawLine(matrices, x, innerY - 1, x + width, innerY - 1, 1, new Color(0xFFB870F2));
        Render2DEngine.drawLine(matrices, x, innerY + innerHeight + 1, x + width, innerY + innerHeight + 1, 1, new Color(0xFFB870F2));

        float innerX = x + 85;
        float innerWidth = width - 85;
        Render2DEngine.drawRect(matrices, innerX, innerY, innerWidth, innerHeight, new Color(0xFF171717));

        RenderSystem.enableBlend();

        Function<Identifier, RenderLayer> renderLayers = RenderLayer::getGuiTextured;
        Identifier logo = Identifier.of("chorus", "img/logo.png");
        context.drawTexture(renderLayers, logo, (int) (x + 4), (int) (y + 3), 0, 0, 21, 21, 21, 21);

        RenderSystem.disableBlend();
        Chorus.getInstance().getFonts().getInterMedium().render(context.getMatrices(), "Chorus", x + 27.5f, y + 4f, 14, 0xFFB870F2);

        component.setBounds(innerX, innerY, innerWidth, innerHeight);
        component.render(context, mouseX, mouseY);
    }

    private void drawResizeHandle(DrawContext context) {
        float handleX = x + width - RESIZE_HANDLE_SIZE;
        float handleY = y + height - RESIZE_HANDLE_SIZE;
    }

    private void drawModuleSelector(MatrixStack matrices) {
        FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();
        FontAtlas medium = Chorus.getInstance().getFonts().getInterMedium();

        float moduleX = x + 7;
        float currentY = y + 31 + moduleY;

        for (Module module : Chorus.getInstance().getModuleManager().getModules().stream().filter(m -> m.getCategory() == selectedCategory).toList()) {
            String name = module.getName();
            String description = medium.truncate(module.getDescription(), 58, 6);

            if (module == selectedModule) {
                Render2DEngine.drawGradientRect(matrices, x + 4, currentY, 75, 23, new Color(0x29B870F2, true), new Color(0x00B870F2, true));
                Render2DEngine.drawLine(matrices, x + 4, currentY, x + 4, currentY + 23, 2, new Color(0xFFB870F2));
            }

            font.render(matrices, name, moduleX, currentY + 3, 7,
                    module.isEnabled() ? new Color(174, 102, 232, 255).getRGB() :
                            module == selectedModule ? new Color(255, 255, 255, 255).darker().getRGB() :
                                    new Color(197, 197, 197, 255).darker().getRGB());
            medium.render(matrices, description, moduleX, currentY + 12.5f, 6, new Color(197, 197, 197, 255).darker().getRGB());
            currentY += 24f;
        }
    }

    private void drawCategorySelector(MatrixStack matrices) {
        FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();

        float totalWidth = computeTotalWidth(font);
        float startX = x + (width - totalWidth) / 2;
        float categoryY = y + height - 15;

        float currentX = startX;

        for (ModuleCategory category : ModuleCategory.values()) {
            String name = category.getName();
            float nameWidth = font.getWidth(name, 6);

            if (selectedCategory == category) {
                Render2DEngine.drawRoundedRect(matrices, currentX - 6, categoryY - 16.5f, nameWidth + 12, 26, 4, new Color(0xFF2B2B2B));
            }
            font.render(matrices, name, currentX, categoryY, 6, selectedCategory
                    == category ? new Color(197, 197, 197, 255).getRGB() :
                    new Color(197, 197, 197, 255).darker().getRGB());
            Chorus.getInstance().getFonts().getIcons().renderCenteredString(matrices, category.getIcon(), currentX + nameWidth / 2f - 1.5f, categoryY - 14, 12, selectedCategory
                    == category ? new Color(197, 197, 197, 255).getRGB() :
                    new Color(197, 197, 197, 255).darker().getRGB());

            currentX += nameWidth + 22f;
        }
    }

    private float computeTotalWidth(FontAtlas font) {
        float totalWidth = 0;
        for (ModuleCategory category : ModuleCategory.values()) {
            totalWidth += font.getWidth(category.getName(), 6);
        }
        totalWidth += (ModuleCategory.values().length - 1) * 22f;
        return totalWidth;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 27) {
                dragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }

            float handleX = x + width - RESIZE_HANDLE_SIZE;
            float handleY = y + height - RESIZE_HANDLE_SIZE;

            if (mouseX >= handleX && mouseX <= handleX + RESIZE_HANDLE_SIZE && mouseY >= handleY && mouseY <= handleY + RESIZE_HANDLE_SIZE) {
                resizing = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }

            FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();
            float totalWidth = computeTotalWidth(font);
            float startX = x + (width - totalWidth) / 2;
            float categoryY = y + height - 15;

            float currentX = startX;

            for (ModuleCategory category : ModuleCategory.values()) {
                String name = category.getName();
                float nameWidth = font.getWidth(name, 6);

                float boxStartX = currentX - 6;
                float boxEndX = currentX + nameWidth + 12;
                float boxStartY = categoryY - 16.5f;
                float boxEndY = categoryY + 9.5f;

                if (mouseX >= boxStartX && mouseX <= boxEndX && mouseY >= boxStartY && mouseY <= boxEndY) {
                    selectedCategory = category;
                    moduleY = 0;
                    selectedModule = Chorus.getInstance()
                            .getModuleManager()
                            .getModules()
                            .stream()
                            .filter(m -> m.getCategory() == selectedCategory)
                            .findFirst()
                            .orElse(selectedModule);
                    if (selectedModule != component.getModule())
                        component = new ModuleComponent(selectedModule);
                    return true;
                }

                currentX += nameWidth + 22f;
            }
        }

        float currentY = y + 31 + moduleY;

        for (Module module : Chorus.getInstance().getModuleManager().getModules().stream().filter(m -> m.getCategory() == selectedCategory).toList()) {
            if (mouseX >= x && mouseX <= x + 85 && mouseY >= currentY && mouseY <= currentY + 24f) {
                if (button == 0) {
                    if (module.isEnabled()) module.onDisable();
                    else module.onEnable();
                }
                if (button == 1 && !module.getSettingRepository().getSettings().isEmpty()) {
                    selectedModule = module;
                    if (selectedModule != component.getModule())
                        component = new ModuleComponent(selectedModule);
                }
            }
            currentY += 24f;
        }

        if (component.isHovered(mouseX, mouseY)) {
            component.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {

            x += (float) (mouseX - lastMouseX);
            y += (float) (mouseY - lastMouseY);

            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        if (resizing && button == 0) {

            float newWidth = (float) (width + (mouseX - lastMouseX));
            float newHeight = (float) (height + (mouseY - lastMouseY));

            width = Math.max(newWidth, MIN_WIDTH);
            height = Math.max(newHeight, MIN_HEIGHT);

            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        if (component.isHovered(mouseX, mouseY)) {
            component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
            resizing = false;
        }
        component.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX > x && mouseX <= x + 75 && mouseY > y + 27 && mouseY <= y + height - 38) {
            moduleY += (float) verticalAmount * 5;
            moduleY = Math.min(moduleY, 0);
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (component.isHovered(mouseX, mouseY)) {
            component.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        Chorus.getInstance().getModuleManager().getModule(ClickGUI.class).onDisable();
        super.close();
    }
}