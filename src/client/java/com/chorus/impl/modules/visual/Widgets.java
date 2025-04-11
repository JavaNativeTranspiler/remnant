package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render2DEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.text.DecimalFormat;

@ModuleInfo(name = "Widgets", description = "Information Widgets", category = ModuleCategory.VISUAL)
public class Widgets extends BaseModule implements QuickImports {
    private final SettingCategory general = new SettingCategory("General");
    private final ModeSetting mode = new ModeSetting(general, "Mode", "Choose widget style", "Remnant", "Remnant");
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
        }
    }

    private void renderRemnant(MatrixStack matrices, DrawContext context) {
        FontAtlas font = Chorus.getInstance().getFonts().getPoppins();
        DecimalFormat format = new DecimalFormat("#.##");
        float rectHeight = 15;
        float textX = xPos.getValue();
        float textY = yPos.getValue();
        double speed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ);
        String speedText = "b/s: " + format.format(speed * 20);
        String coordsText = "XYZ: " + Math.round(mc.player.getX()) + " " + Math.round(mc.player.getY()) + " " + Math.round(mc.player.getZ());
        float centerY = textY - (font.getLineHeight(7f) / 2) + (rectHeight / 2);

        //xyz
        Render2DEngine.drawBlurredRoundedRect(matrices, textX, textY, font.getWidth(coordsText, 7f) + 7.5f, rectHeight, 4, 8, new Color(255, 255, 255, 10));
        Render2DEngine.drawRoundedOutline(matrices, textX, textY, font.getWidth(coordsText, 7f) + 7.5f, rectHeight, 4, 1, new Color(200, 200, 200, 75));
        font.render(matrices, coordsText, textX + 2.5f, centerY, 7f, Color.WHITE.getRGB());

        //speed
        Render2DEngine.drawBlurredRoundedRect(matrices, textX + font.getWidth(coordsText, 7f) + 12.5f, textY, font.getWidth(speedText, 7f) + 7.5f, rectHeight, 4, 8, new Color(255, 255, 255, 10));
        Render2DEngine.drawRoundedOutline(matrices, textX + font.getWidth(coordsText, 7f) + 12.5f, textY, font.getWidth(speedText, 7f) + 7.5f, rectHeight, 4, 1, new Color(200, 200, 200, 75));
        font.render(matrices, speedText, textX + font.getWidth(coordsText, 7f) + 15.5f, centerY, 7f, Color.WHITE.getRGB());
        setWidth(font.getWidth(speedText, 7f) + 7.5f + font.getWidth(coordsText, 7f) + 12.5f);
        setHeight(rectHeight);
    }


    public Widgets() {
        setDraggable(true);
        getSettingRepository().registerSettings(general, mode, xPos, yPos);
        
        xPos.setRenderCondition(() -> false);
        yPos.setRenderCondition(() -> false);
    }
}