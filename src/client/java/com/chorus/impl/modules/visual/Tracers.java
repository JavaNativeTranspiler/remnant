package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.render.Render3DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ModuleInfo(
        name = "Tracers",
        description = "Draw Lines to players",
        category = ModuleCategory.VISUAL
)
public class Tracers extends BaseModule implements QuickImports {
    private Map<Entity, Pair<Rectangle, Boolean>> hashMap = new HashMap<>();
    RenderTickCounter renderTickCounter = mc.getRenderTickCounter();

    @RegisterEvent
    private void Render3DEvent(Render3DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render3DEvent.Mode.PRE)) {
            hashMap.clear();
            if (mc.player == null || mc.world == null) return;
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity != mc.player && entity != null && SocialManager.isEnemy(entity)) {
                    Vec3d prevPos = new Vec3d(entity.lastRenderX, entity.lastRenderY, entity.lastRenderZ);
                    Vec3d interpolated = prevPos.add(entity.getPos().subtract(prevPos).multiply(renderTickCounter.getTickDelta(false)));

                    float halfWidth = entity.getWidth() / 2.0f;
                    Box boundingBox = new Box(
                            interpolated.x,
                            interpolated.y,
                            interpolated.z,
                            interpolated.x,
                            interpolated.y + entity.getHeight() + (entity.isSneaking() ? -0.2 : 0),
                            interpolated.z
                    ).expand(0.1, 0.1, 0.1);

                    Vec3d[] corners = new Vec3d[]{
                            new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                            new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                            new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                            new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),

                            new Vec3d(boundingBox.minX, boundingBox.maxY + 0.1, boundingBox.minZ),
                            new Vec3d(boundingBox.maxX, boundingBox.maxY + 0.1, boundingBox.minZ),
                            new Vec3d(boundingBox.maxX, boundingBox.maxY + 0.1, boundingBox.maxZ),
                            new Vec3d(boundingBox.minX, boundingBox.maxY + 0.1, boundingBox.maxZ)
                    };

                    Rectangle rectangle = null;
                    boolean visible = false;

                    for (Vec3d corner : corners) {
                        Pair<Vec3d, Boolean> projection = Render3DEngine.project(event.getMatrices().peek().getPositionMatrix(), event.getProjectionMatrix(), corner);
                        if (projection.getRight()) {
                            visible = true;
                        }
                        Vec3d projected = projection.getLeft();

                        if (rectangle == null) {
                            rectangle = new Rectangle((int) projected.getX(), (int) projected.getY(), (int) projected.getX(), (int) projected.getY());
                        } else {
                            if (rectangle.x > projected.getX()) {
                                rectangle.x = (int) projected.getX();
                            }
                            if (rectangle.y > projected.getY()) {
                                rectangle.y = (int) projected.getY();
                            }
                            if (rectangle.z < projected.getX()) {
                                rectangle.z = (int) projected.getX();
                            }
                            if (rectangle.w < projected.getY()) {
                                rectangle.w = (int) projected.getY();
                            }
                        }
                    }

                    hashMap.put(entity, new Pair<>(rectangle, visible));
                }
            }
        }
    }

    @RegisterEvent
    private void Render2DEvent(com.chorus.impl.events.render.Render2DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render2DEvent.Mode.PRE)) {
            MatrixStack matrix = event.getContext().getMatrices();
            DrawContext context = event.getContext();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            if (!hashMap.isEmpty() && hashMap.entrySet().stream().anyMatch(entityPairEntry -> entityPairEntry.getValue().getRight())) {
                for (Map.Entry<Entity, Pair<Rectangle, Boolean>> entry : hashMap.entrySet()) {
                    Pair<Rectangle, Boolean> pair = entry.getValue();
                    if (pair.getRight()) {
                        Rectangle rect = pair.getLeft();
                        var color = new Color(184, 112, 242);
                        int screenWidth = mc.getWindow().getScaledWidth() / 2;
                        int screenHeight = -5;
                        BufferBuilder buffer;
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        GL11.glEnable(GL11.GL_LINE_SMOOTH);
                        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                        buffer.vertex(matrix.peek(), screenWidth, screenHeight, 0).color(color.getRGB());
                        buffer.vertex(matrix.peek(), (float) rect.x, (float) rect.y, (float) rect.z).color(color.getRGB());
                        BufferRenderer.drawWithGlobalProgram(Objects.requireNonNull(buffer.endNullable()));
                        GL11.glDisable(GL11.GL_LINE_SMOOTH);
                        RenderSystem.disableBlend();
                    }
                }
            }
            RenderSystem.disableBlend();
        }
    }
    public static class Rectangle {
        public double x;
        public double y;
        public double z;
        public double w;

        public Rectangle(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }

    public Tracers() {
        getSettingRepository().registerSettings();
    }
}