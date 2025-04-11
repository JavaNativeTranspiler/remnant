package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.ColorUtils;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render3DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@ModuleInfo(
        name = "Nametags",
        description = "mahahah slink slink",
        category = ModuleCategory.VISUAL
)
public class Nametags extends BaseModule implements QuickImports {
    private final SettingCategory visual = new SettingCategory("Visual Settings");
    private final BooleanSetting showHealth = new BooleanSetting(visual, "Show Health", "Display player health", true);
    private final BooleanSetting showDistance = new BooleanSetting(visual, "Show Distance", "Display distance to player", true);
    private final BooleanSetting showBackground = new BooleanSetting(visual, "Show Background", "Display a background behind the text", true);
    private final BooleanSetting combine = new BooleanSetting(visual, "Combine", "Background", false);
    private final BooleanSetting showArmor = new BooleanSetting(visual, "Show Armor", "Display the players armor", true);
    private final NumberSetting<Integer> range = new NumberSetting<>(visual, "Range", "Maximum distance to render nametags", 64, 0, 256);
    private final NumberSetting<Integer> scale = new NumberSetting<>(visual, "Scale", "Size of the nametags", 100, 50, 200);
    private final BooleanSetting scaleWithDistance = new BooleanSetting(visual, "Scale with Distance", "Decrease size with distance", true);
    private Map<Entity, Pair<Rectangle, Boolean>> hashMap = new HashMap<>();
    RenderTickCounter renderTickCounter = mc.getRenderTickCounter();

    @RegisterEvent
    private void Render3DEvent(Render3DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render3DEvent.Mode.PRE)) {
            hashMap.clear();
            if (mc.player == null || mc.world == null) return;
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity != null && !npcRepository.isNPC(entity.getNameForScoreboard())) {
                    Vec3d prevPos = new Vec3d(entity.lastRenderX, entity.lastRenderY, entity.lastRenderZ);
                    Vec3d interpolated = prevPos.add(entity.getPos().subtract(prevPos).multiply(renderTickCounter.getTickDelta(false))).add(0, 0.05f, 0);

                    Box boundingBox = new Box(
                            interpolated.x,
                            interpolated.y,
                            interpolated.z,
                            interpolated.x,
                            interpolated.y + entity.getHeight() + (entity.isSneaking() ? -0.2 : 0),
                            interpolated.z
                    );

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
                    FontAtlas font = Chorus.getInstance().getFonts().getInterBold();
                    if (pair.getRight()) {
                        PlayerEntity player = (PlayerEntity) entry.getKey();
                        if (player == mc.player && mc.options.getPerspective() == Perspective.FIRST_PERSON) continue;
                        if (!player.isPartOfGame() || player.isRemoved() || player.isSpectator()) return;
                        float distance = mc.player.distanceTo(player);
                        if (distance <= range.getValue()) {
                            float currentScale = scaleWithDistance.getValue()
                                    ? Math.max((scale.getValue() / 100f) * (1.0f - (distance / range.getValue())), 0.5f * (scale.getValue() / 100f))
                                    : scale.getValue() / 100f;

                            float centerX = (float) ((pair.getLeft().x + pair.getLeft().z) / 2f);
                            float y = (float) (pair.getLeft().y - player.getHeight() - 4.5f) - 10;

                            String name = player.getNameForScoreboard();
                            if (name == null || name.isEmpty()) name = "Unknown";
                            name = name.replaceAll("§", "");
                            StringBuilder combinedText = new StringBuilder(name);


                            Color backgroundColor = new Color(0, 0, 0, 200);
                            DecimalFormat format = new DecimalFormat("#.#");

                            float nameWidth = font.getWidth(combinedText.toString(), 10f) * currentScale;
                            float textHeight = font.getLineHeight(10f) * currentScale;

                            String healthText = format.format(Math.clamp(player.getHealth() / 2, 0, 10000000)) + "hp";
                            float healthWidth = font.getWidth(healthText, 10f) * currentScale;

                            String absorptionText = " +" + format.format(player.getAbsorptionAmount() / 2);
                            float absorptionWidth = player.getAbsorptionAmount() != 0 ? font.getWidth(absorptionText, 10f) * currentScale : 0;

                            String distanceText = format.format(distance) + "m";
                            float distanceWidth = font.getWidth(distanceText, 10f) * currentScale;
                            
                            float padding = 10 * currentScale;
                            
                            float totalWidth = nameWidth;
                            if (showHealth.getValue()) {
                                totalWidth += healthWidth + padding + absorptionWidth;
                            }
                            if (showDistance.getValue()) {
                                totalWidth += distanceWidth + padding;
                            }
                            
                            float contentLeftEdge = centerX - (totalWidth / 2);
                            
                            float currentX = contentLeftEdge;
                            
                            float distanceX = currentX;
                            if (showDistance.getValue()) {
                                currentX += distanceWidth + padding;
                            }
                            
                            float nameX = currentX;
                            currentX += nameWidth + padding;
                            
                            float healthX = currentX;
                            
                            if (showArmor.getValue()) {
                                int armorAmount = 0;
                                for (int i = 3; i >= 0; i--) {
                                    if (!player.getInventory().getArmorStack(i).isEmpty()) {
                                        armorAmount++;
                                    }
                                }

                                int totalArmorWidth = armorAmount * 15;
                                float startX = ((contentLeftEdge - 5) + ((totalWidth + 10) / 2f)) + (totalArmorWidth / 2f);

                                for (int i = 3; i >= 0; i--) {
                                    if (!player.getInventory().getArmorStack(i).isEmpty()) {
                                        context.drawItem(player.getInventory().armor.get(i), (int) (startX - armorAmount * 15), (int) y - 15);
                                        armorAmount--;
                                    }
                                }
                            }
                            
                            if (combine.getValue() && showBackground.getValue()) {
                                Render2DEngine.drawRoundedBlur(matrix, contentLeftEdge - 5,
                                        y - 1,
                                        totalWidth + 10,
                                        textHeight + 2,
                                        3,
                                        8,
                                        backgroundColor);
                                Render2DEngine.drawRoundedRect(matrix,
                                        contentLeftEdge - 5,
                                        y - 1,
                                        totalWidth + 10,
                                        textHeight + 2,
                                        3, backgroundColor);
                            }

                            if (showDistance.getValue()) {
                                if (showBackground.getValue() && !combine.getValue()) {
                                    Render2DEngine.drawRoundedRect(matrix,
                                            distanceX - 5,
                                            y - 1,
                                            distanceWidth + 10,
                                            textHeight + 2,
                                            3, backgroundColor);
                                }
                                font.renderWithShadow(
                                        matrix,
                                        distanceText,
                                        distanceX,
                                        y,
                                        10f * currentScale,
                                        Color.GRAY.getRGB()
                                );
                            }
                            
                            if (showBackground.getValue() && !combine.getValue()) {
                                Render2DEngine.drawRoundedRect(matrix,
                                        nameX - 5,
                                        y - 1,
                                        nameWidth + 10,
                                        textHeight + 2,
                                        3,
                                        backgroundColor);
                            }
                            font.renderWithShadow(
                                    matrix,
                                    combinedText.toString(),
                                    nameX,
                                    y,
                                    10f * currentScale,
                                    getAssociatedColor(player).getRGB()
                            );

                            if (showHealth.getValue()) {
                                if (showBackground.getValue() && !combine.getValue()) {
                                    float healthBgWidth = healthWidth + absorptionWidth + 10;
                                    Render2DEngine.drawRoundedRect(matrix,
                                            healthX - 5,
                                            y - 1,
                                            healthBgWidth,
                                            textHeight + 2,
                                            3, backgroundColor);
                                }
                                float multiplier = (player.getHealth() / player.getMaxHealth());
                                font.renderWithShadow(
                                        matrix,
                                        healthText,
                                        healthX,
                                        y,
                                        10f * currentScale,
                                        ColorUtils.interpolateColor(new Color(255, 127, 127), new Color(127, 255, 127), multiplier * multiplier * multiplier).getRGB()
                                );
                                if (player.getAbsorptionAmount() != 0)
                                    font.renderWithShadow(
                                            matrix,
                                            absorptionText,
                                            healthX + healthWidth,
                                            y,
                                            10f * currentScale,
                                            new Color(254, 222, 53).getRGB()
                                    );
                            }
                        }
                    }
                }
            }
            RenderSystem.disableBlend();
        }
    }

    public static Color getAssociatedColor(PlayerEntity player) {
        if (player == mc.player) return new Color(127,255,127);
        if (friendRepository.isFriend(player.getUuid())) return new Color(127,255,127);
        if (npcRepository.isNPC(player.getNameForScoreboard())) return new Color(118,118,118);
        if (player.getScoreboardTeam() != null && player.getScoreboardTeam().getColor().getColorValue() != null) {
            return ColorUtils.formattingToRGB(player.getScoreboardTeam().getColor().getColorValue());
        }
        return new Color(255,127,127);
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

    public Nametags() {
        getSettingRepository().registerSettings(visual, showHealth, showDistance, showArmor, showBackground, combine, range, scale, scaleWithDistance);
    }
}