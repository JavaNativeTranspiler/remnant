/**
 * Created: 2/4/2025
 */

package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.render.ColorUtils;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render3DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ModuleInfo(
        name        = "PlayerESP",
        description = "can u see me",
        category    = ModuleCategory.VISUAL
)
public class PlayerESP extends BaseModule implements QuickImports {

    private final SettingCategory visual = new SettingCategory("Visual Settings");
    private final SettingCategory color = new SettingCategory("Color Settings");
    public final ModeSetting espMode = new ModeSetting(visual, "ESP Mode", "Decide which ESP mode to use", "Shaded 3D", "Shaded 3D", "Outlined 2D", "Outlined 3D", "Outlined And Shaded 3D", "Glow Minecraft");
    private final BooleanSetting showHealthBar = new BooleanSetting(visual, "Show Health Bar", "Displays a health bar for players", true);
    private final ModeSetting healthBarPosition = new ModeSetting(visual, "Health Bar Position", "Choose where to display the health bar", "Bottom", "Bottom", "Top", "Left", "Right");
    private final NumberSetting<Integer> red = new NumberSetting<>(color, "Red", "Set Red RGB", 255, 0, 255);
    private final NumberSetting<Integer> green = new NumberSetting<>(color, "Green", "Set Green RGB", 255, 0, 255);
    private final NumberSetting<Integer> blue = new NumberSetting<>(color, "Blue", "Set Blue RGB", 255, 0, 255);
    private final NumberSetting<Integer> alpha = new NumberSetting<>(color, "Alpha", "Set Alpha RGB", 100, 0, 255);
    public final MultiSetting exclude = new MultiSetting(visual, "Exclude...", "Choose Players to Exclude", "Bots", "Friends", "Teams");

    private Map<Entity, Pair<Rectangle, Boolean>> hashMap = new HashMap<>();
    RenderTickCounter renderTickCounter = mc.getRenderTickCounter();

    @RegisterEvent
    private void Render3DEvent(Render3DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render3DEvent.Mode.PRE)) {
            hashMap.clear();
            if (mc.player == null || mc.world == null) return;
            for (PlayerEntity entity : mc.world.getPlayers()) {
                Color settingColor = ColorUtils.getAssociatedColor(entity);
                if (entity != mc.player) {
                    if (exclude.getSpecificValue("Friends") && friendRepository.isFriend(entity.getUuid())) return;
                    if (exclude.getSpecificValue("Bots") && npcRepository.isNPC(entity.getNameForScoreboard())) return;
                    if (exclude.getSpecificValue("Teams") && teamRepository.isMemberOfCurrentTeam(entity.getNameForScoreboard())) return;
                    switch (espMode.getValue()) {
                        case "Shaded 3D" -> Render3DEngine.renderShadedBox(entity, settingColor, alpha.getValue(), event.getMatrices());
                        case "Outlined 3D" -> Render3DEngine.renderOutlinedBox(entity, settingColor, event.getMatrices());
                        case "Outlined And Shaded 3D" -> Render3DEngine.renderOutlinedShadedBox(entity, settingColor, alpha.getValue(), event.getMatrices());
                    }
                    Vec3d prevPos = new Vec3d(entity.lastRenderX, entity.lastRenderY, entity.lastRenderZ);
                    Vec3d interpolated = prevPos.add(entity.getPos().subtract(prevPos).multiply(renderTickCounter.getTickDelta(false)));
                    float halfWidth = entity.getWidth() / 2.0f;
                    Box boundingBox = new Box(
                            interpolated.x - halfWidth,
                            interpolated.y,
                            interpolated.z - halfWidth,
                            interpolated.x + halfWidth,
                            interpolated.y + entity.getHeight() + (entity.isSneaking() ? -0.2 : 0),
                            interpolated.z + halfWidth
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
        if (event.getMode().equals(com.chorus.impl.events.render.Render2DEvent.Mode.PRE) && espMode.getValue().equals("Outlined 2D")) {
            Matrix4f matrix = event.getContext().getMatrices().peek().getPositionMatrix();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            if (!hashMap.isEmpty() && hashMap.entrySet().stream().anyMatch(entityPairEntry -> entityPairEntry.getValue().getRight())) {
                for (Map.Entry<Entity, Pair<Rectangle, Boolean>> entry : hashMap.entrySet()) {
                    Pair<Rectangle, Boolean> pair = entry.getValue();
                    Color settingColor = ColorUtils.getAssociatedColor((PlayerEntity) entry.getKey());
                    if (pair.getRight()) {
                        Rectangle rect = pair.getLeft();
                        double posX = rect.x;
                        double posY = rect.y;
                        double endX = rect.z;
                        double endY = rect.w;

                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) posX - 1F, (float) posY, (float) posX + 0.5f, (float) endY + 0.5f, Color.BLACK);
                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) posX - 1F, (float) posY - 0.5f, (float) endX + 0.5f, (float) posY + 1f, Color.BLACK);
                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) endX - 1f, (float) posY, (float) endX + 0.5f, (float) endY + 0.5f, Color.BLACK);
                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) posX - 1f, (float) endY - 1f, (float) endX + 0.5f, (float) endY + 0.5f, Color.BLACK);

                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) posX - 0.5F, (float) posY, (float) posX, (float) endY, settingColor);
                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) posX, (float) endY - 0.5f, (float) endX, (float) endY, settingColor);
                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) posX - 0.5f, (float) posY, (float) endX, (float) posY + 0.5f, settingColor);
                        Render2DEngine.drawQuads(bufferBuilder, matrix, (float) endX - 0.5f, (float) posY, (float) endX, (float) endY, settingColor);

                        if (entry.getKey() instanceof PlayerEntity player && showHealthBar.getValue()) {
                            float healthPercentage = player.getHealth() / player.getMaxHealth();
                            float barWidth = 0;
                            float barHeight = 0;
                            float barPosX = 0;
                            float barPosY = 0;

                            switch (healthBarPosition.getValue()) {
                                case "Bottom":
                                    barWidth = (float) (endX - posX);
                                    barHeight = 2f;
                                    barPosX = (float) posX;
                                    barPosY = (float) endY + 2f;
                                    break;
                                case "Top":
                                    barWidth = (float) (endX - posX);
                                    barHeight = 2f;
                                    barPosX = (float) posX;
                                    barPosY = (float) posY - 4f;
                                    break;
                                case "Left":
                                    barWidth = 2f;
                                    barHeight = (float) (endY - posY);
                                    barPosX = (float) posX - 4f;
                                    barPosY = (float) posY;
                                    break;
                                case "Right":
                                    barWidth = 2f;
                                    barHeight = (float) (endY - posY);
                                    barPosX = (float) endX + 2f;
                                    barPosY = (float) posY;
                                    break;
                            }

                            Render2DEngine.drawQuads(bufferBuilder, matrix, barPosX - 0.6f, barPosY - 0.6f, barPosX + barWidth + 0.6f, barPosY + barHeight + 0.6f, Color.BLACK);
                            Render2DEngine.drawQuads(bufferBuilder, matrix, barPosX, barPosY, barPosX + barWidth, barPosY + barHeight, settingColor.darker().darker());

                            if (healthBarPosition.getValue().equals("Left") || healthBarPosition.getValue().equals("Right")) {
                                Render2DEngine.drawQuads(bufferBuilder, matrix, barPosX, barPosY + (barHeight * (1 - healthPercentage)), barPosX + barWidth, barPosY + barHeight, getHealthColor(healthPercentage));
                            } else {
                                Render2DEngine.drawQuads(bufferBuilder, matrix, barPosX, barPosY, barPosX + (barWidth * healthPercentage), barPosY + barHeight, getHealthColor(healthPercentage));
                            }
                        }
                    }
                }

                BufferRenderer.drawWithGlobalProgram(Objects.requireNonNull(bufferBuilder.endNullable()));
            }

            RenderSystem.disableBlend();
        }
    }

    private Color getHealthColor(float healthPercentage) {
        if (healthPercentage > 0.75f) {
            return new Color(100, 255, 100);
        } else if (healthPercentage > 0.5f) {
            return new Color(255, 255, 100);
        } else if (healthPercentage > 0.25f) {
            return new Color(255, 165, 100);
        } else {
            return new Color(255, 100, 100);
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

    public PlayerESP() {
        getSettingRepository().registerSettings(visual, color, espMode, healthBarPosition, showHealthBar,red, green, blue, alpha, exclude);
    }
}