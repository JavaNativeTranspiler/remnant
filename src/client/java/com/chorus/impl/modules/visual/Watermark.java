package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.ColorUtils;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render2DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.function.Function;

@ModuleInfo(name = "Watermark", description = "bah", category = ModuleCategory.VISUAL)
public class Watermark extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General");
    private final ModeSetting mode = new ModeSetting(general, "Mode", "Choose watermark style", "Remnant", "Remnant", "Gamesense", "Wurst");
    private final NumberSetting<Integer> xPos = new NumberSetting<>(general, "xPos", "Internal setting", 5, 0, 1920);
    private final NumberSetting<Integer> yPos = new NumberSetting<>(general, "yPos", "Internal setting", 5, 0, 1080);

    @RegisterEvent
    private void render2DListener(Render2DEvent event) {
        DrawContext context = event.getContext();
        MatrixStack matrices = context.getMatrices();
        if (mc.player == null || mc.getDebugHud().shouldShowDebugHud()) return;

        switch (mode.getValue()) {
            case "Remnant":
                renderRemnant(matrices, context);
                break;
            case "Gamesense":
                renderGamesense(matrices, context);
                break;
            case "Wurst":
                renderWurst(matrices, context);
                break;
        }
    }

    private void renderRemnant(MatrixStack matrices, DrawContext context) {
        FontAtlas font = Chorus.getInstance().getFonts().getInterMedium();
        FontAtlas icons = Chorus.getInstance().getFonts().getLucide();
        float rectWidth = 65;
        float rectHeight = 15;
        float textHeight = font.getLineHeight();
        float textX = xPos.getValue() + 5;
        float textY = yPos.getValue() + (rectHeight / 2) - textHeight / 2 + 1;
        Render2DEngine.drawBlurredRoundedRect(matrices, xPos.getValue(), yPos.getValue(), rectWidth, rectHeight, 4, 8, new Color(255, 255, 255, 10));
        Render2DEngine.drawRoundedOutline(matrices, xPos.getValue(), yPos.getValue(), rectWidth, rectHeight, 4, 1, new Color(200, 200, 200, 75));
        icons.render(matrices, "", textX, textY, 9f, Color.WHITE.getRGB());
        font.render(matrices, "remnant.wtf", textX + 13, textY, 7f, Color.WHITE.getRGB());
        setWidth(rectWidth);
        setHeight(rectHeight);
    }

    private void renderGamesense(MatrixStack matrices, DrawContext context) {
        FontAtlas font = Chorus.getInstance().getFonts().getInterMedium();
        String text = String.format("chorus | " + mc.player.getNameForScoreboard() + " | %s",
                mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : (mc.isInSingleplayer() ? "Local" : "Disconnected")
        );

        int height = 15;
        int x = xPos.getValue() + 5;
        int y = yPos.getValue() + 5;
        int padding = 12;

        Color darkBlack = new Color(45, 45, 45, 255);
        Color middleBlack = new Color(60, 60, 60, 255);
        Color lightBlack = new Color(100, 100, 100, 255);
        Render2DEngine.drawRect(matrices, x - 2.5f, y - 2.5f, font.getWidth(text, 8f) + padding + 6f, height + 6f, darkBlack.darker());
        Render2DEngine.drawRect(matrices, x - 1.5f, y - 1.5f, font.getWidth(text, 8f) + padding + 4, height + 4, lightBlack);
        Render2DEngine.drawRect(matrices, x - 1f, y - 1f, font.getWidth(text, 8f) + padding + 3f, height + 3f, middleBlack);
        Render2DEngine.drawRect(matrices, x + .5f, y + .5f, font.getWidth(text, 8f) + padding, height, lightBlack);
        Render2DEngine.drawRect(matrices, x + 1, y + 1, font.getWidth(text, 8f) + padding - 1, height - 1, darkBlack);
        Color primary = new Color(184, 112, 242);
        Color secondary = primary.brighter().brighter();
        Color color1 = ColorUtils.interpolateColor(secondary, primary, 2, mc.player.age % 10 * 2);
        Color color2 = ColorUtils.interpolateColor(primary, secondary, 2, mc.player.age % 10 * 2);
        Render2DEngine.drawGradientRect(matrices, x + 1, y + 1, (font.getWidth(text, 8f) + padding - 1) / 2, 1.5f,
                color1,
                color2
        );
        Render2DEngine.drawGradientRect(matrices, x + 1 + (font.getWidth(text, 8f) + padding - 1) / 2, y + 1, (font.getWidth(text, 8f) + padding - 1) / 2, 1.5f,
                color2,
                color1
        );

        float xPos = x + 4;
        String[] parts = text.split(" \\| ");
        for (int i = 0; i < parts.length; i++) {
            font.render(matrices, parts[i], xPos, y + 3.5f, 8f, Color.WHITE.getRGB());
            xPos += font.getWidth(parts[i], 8f);

            if(i < parts.length - 1) {
                font.render(matrices, "|", xPos + 1, y + 4.5f, 7f, Color.white.getRGB());
                xPos += font.getWidth("|", 8f) + 2;
            }
        }
        setWidth(font.getWidth(text, 8f) + padding + 8.5f);
        setHeight(height + 8.5f);
    }

    private void renderWurst(MatrixStack matrices, DrawContext context) {
        String version = "v" + Chorus.getInstance().getClientInfo().version() + " MC" + SharedConstants.getGameVersion().getName();
        context.fill(0, 6, mc.textRenderer.getWidth(version) + 76, 17, new Color(255, 255, 255, 128).getRGB());
        context.drawText(mc.textRenderer, version, 74, 8, Color.BLACK.getRGB(), false);
        RenderSystem.enableBlend();

        Function<Identifier, RenderLayer> renderLayers = RenderLayer::getGuiTextured;
        Identifier wurst = Identifier.of("chorus", "img/wurst.png");

        context.drawTexture(renderLayers, wurst, 0, 3, 0, 0, 72, 18, 72, 18);
        RenderSystem.disableBlend();
    }


    public Watermark() {
        setDraggable(true);
        getSettingRepository().registerSettings(general, mode, xPos, yPos);

        xPos.setRenderCondition(() -> false);
        yPos.setRenderCondition(() -> false);
    }
}