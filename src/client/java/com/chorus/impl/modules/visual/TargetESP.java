package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.impl.events.render.Render3DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;

@ModuleInfo(name = "TargetESP", description = "Renders a ring effect around the target", category = ModuleCategory.VISUAL)
public class TargetESP extends BaseModule implements QuickImports {

    private final SettingCategory visual = new SettingCategory("Visual Settings");
    private final ModeSetting espMode = new ModeSetting(visual, "ESP Mode", "Choose the ESP effect", "Ring", "Ring");
    private final NumberSetting<Integer> red = new NumberSetting<>(visual, "Red", "Set Red RGB", 255, 0, 255);
    private final NumberSetting<Integer> green = new NumberSetting<>(visual, "Green", "Set Green RGB", 127, 0, 255);
    private final NumberSetting<Integer> blue = new NumberSetting<>(visual, "Blue", "Set Blue RGB", 127, 0, 255);
    private final NumberSetting<Integer> alpha = new NumberSetting<>(visual, "Alpha", "Set Alpha RGB", 225, 0, 255);

    public TargetESP() {
        getSettingRepository().registerSettings(visual, espMode, red, green, blue, alpha);
    }

    @RegisterEvent
    private void render3DEventListener(Render3DEvent event) {
        if (event.getMode().equals(Render3DEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            PlayerEntity target = mc.world.getPlayers().stream()
                    .filter(player -> player != mc.player
                            && mc.player.distanceTo(player) <= 6.0
                            && Math.toDegrees(MathUtils.angleBetween(mc.player.getRotationVector(), player.getPos().add(0, player.getEyeHeight(player.getPose()), 0).subtract(mc.player.getEyePos()))) <= 90.0)
                    .min(Comparator.comparingDouble(player -> mc.player.distanceTo(player)))
                    .orElse(null);
            if (target == null) return;

            event.getMatrices().push();

            double duration = 2000;
            double elapsed = (System.currentTimeMillis() % duration);
            boolean side = elapsed > (duration / 2);
            double progress = elapsed / (duration / 2);

            if (side) progress -= 1;
            else progress = 1 - progress;

            progress = (progress < 0.5) ? 2 * progress * progress : 1 - Math.pow((-2 * progress + 2), 2) / 2;

            double x = target.lastRenderX + (target.getX() - target.lastRenderX) * event.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = target.lastRenderY + (target.getY() - target.lastRenderY) * event.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = target.lastRenderZ + (target.getZ() - target.lastRenderZ) * event.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
            float height = target.getHeight();

            double eased = (height / 1.2) * ((progress > 0.5) ? 1 - progress : progress) * ((side) ? -1 : 1);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            GL11.glLineWidth(1.5f);

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

            Color color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
            Color fadeColor = new Color(red.getValue(), green.getValue(), blue.getValue(), 1);

            for (int i = 0; i <= 360; i++) {
                double rad = Math.toRadians(i);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                double width = target.getWidth() * 0.8;

                buffer.vertex((float)(x + cos * width), (float)(y + (height * progress)), (float)(z + sin * width))
                        .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                buffer.vertex((float)(x + cos * width), (float)(y + (height * progress) + eased), (float)(z + sin * width))
                        .color(fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), fadeColor.getAlpha());
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());

            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);

            for (int i = 0; i <= 360; i++) {
                double rad = Math.toRadians(i);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                double width = target.getWidth() * 0.8;

                buffer.vertex((float)(x + cos * width), (float)(y + (height * progress)), (float)(z + sin * width))
                        .color(fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), fadeColor.getAlpha());
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            
            event.getMatrices().pop();
        }
    }
} 