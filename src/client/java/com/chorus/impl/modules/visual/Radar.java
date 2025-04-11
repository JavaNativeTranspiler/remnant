package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.ColorUtils;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.common.QuickImports;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.render.Render2DEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@ModuleInfo(name = "Radar", description = "Shows People On Radar", category = ModuleCategory.VISUAL)
public class Radar extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General");
    private final ModeSetting mode = new ModeSetting(general, "Mode", "Choose style", "Chorus", "Chorus");
    private final NumberSetting<Integer> xPos = new NumberSetting<>(general, "xPos", "Internal setting", 50, 0, 1920);
    private final NumberSetting<Integer> yPos = new NumberSetting<>(general, "yPos", "Internal setting", 75, 0, 1080);

    @RegisterEvent
    private void render2DListener(Render2DEvent event) {
        DrawContext context = event.getContext();
        MatrixStack matrices = context.getMatrices();
        if (mc.player == null || mc.getDebugHud().shouldShowDebugHud()) return;
        switch (mode.getValue()) {
            case "Chorus":
                renderChorus(matrices, context);
                break;
        }
    }

    private void renderChorus(MatrixStack matrices, DrawContext context) {
        int x = xPos.getValue();
        int y = yPos.getValue();
        float width = 90, height = 90;


        Render2DEngine.drawRoundedBlur(matrices, x, y, width, height, 4, 8, new Color(0, 0, 0, 150));
        Render2DEngine.drawRoundedOutline(matrices, x, y, width, height, 4, 1, new Color(200, 200, 200, 75));

        for (PlayerEntity player : mc.world.getPlayers()) {
            float[] position = calculateRadarPosition(player);

            Color color = player == mc.player ? new Color(127, 255, 127) : SocialManager.isTargetedPlayer(player) == player ? new Color(255, 127, 127) : Color.WHITE;
            if (Math.abs(position[0]) >= width / 2 - 1 || Math.abs(position[1]) >= height / 2 - 1) continue;
            Render2DEngine.drawCircle(matrices, x + position[0] + (width / 2), y + position[1] + (height / 2), 1.5f, color);
        }
        setWidth(width);
        setHeight(height);
    }
    public Color getColor(PlayerEntity player) {
        if (player.getScoreboardTeam() != null && player.getScoreboardTeam().getColor().getColorValue() != null) {
            return ColorUtils.formattingToRGB(player.getScoreboardTeam().getColor().getColorValue());
        }
        if (player == mc.player) return new Color(127, 255, 127);
        if (SocialManager.isTargetedPlayer(player) == player) {
            return new Color(255, 127, 127);
        } else {
            return Color.white;
        }
    }
    public float[] calculateRadarPosition(PlayerEntity player) {
        Vec3d localPos = mc.player.getPos();
        Vec3d pos = player.getPos();
        double x = pos.x - localPos.x;
        double z = pos.z - localPos.z;

        double calculation = Math.atan2(x, z) * 57.2957795131;
        double angle = (mc.player.getYaw() + calculation) * 0.01745329251;

        double hypotenuse = player.distanceTo(mc.player);
        return new float[]{(float) (-hypotenuse * Math.sin(angle)), (float) (-hypotenuse * Math.cos(angle))};
    }

    public Radar() {
        setDraggable(true);
        getSettingRepository().registerSettings(general, mode, xPos, yPos);
        xPos.setRenderCondition(() -> false);
        yPos.setRenderCondition(() -> false);
    }
}