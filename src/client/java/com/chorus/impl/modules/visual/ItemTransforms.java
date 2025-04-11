package com.chorus.impl.modules.visual;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.common.QuickImports;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.*;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

@ModuleInfo(
        name = "ItemTransforms",
        description = "Transforms Held Items",
        category = ModuleCategory.VISUAL
)
public class ItemTransforms extends BaseModule implements QuickImports {
    private final NumberSetting<Double> x = new NumberSetting<>("X", "Offset Item's Position On X Axis", 0.0, -1.0, 1.0);
    private final NumberSetting<Double> y = new NumberSetting<>("Y", "Offset Item's Position On Y Axis", 0.0, -1.0, 1.0);
    private final NumberSetting<Double> z = new NumberSetting<>("Z", "Offset Item's Position On Z Axis", 0.0, -1.0, 1.0);
    private final NumberSetting<Double> scale = new NumberSetting<>("Scale", "Scale Your Held Item", 1.0, 0.01, 2.0);


    private final ModeSetting mode = new ModeSetting("Mode", "Choose Animation", "Swong",
            "Swong",
            "Swank",
            "Static",
            "Static 2",
            "Balls",
            "Ion",
            "Test");

    private final MultiSetting handSetting = new MultiSetting("Hand", "Choose Which Hand", "Main Hand", "Offhand");
    public void swordAnimation(float swingProgress, float equipProgress, MatrixStack matrices, int armMultiplier, Arm arm) {
        float swingX = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        float swingY = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
        float swingZ = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
        int hand = arm == Arm.RIGHT ? 1 : -1;
        if ((!handSetting.getSpecificValue("Main Hand") && hand == 1) || (!handSetting.getSpecificValue("Offhand") && hand == -1)) {
            matrices.translate((float)armMultiplier * swingX, swingY, swingZ);
            applyEquipOffset(matrices, arm, equipProgress);
            applySwingOffset(matrices, arm, swingProgress);
            return;
        }
        matrices.translate(x.getValue().floatValue(), y.getValue().floatValue(), z.getValue().floatValue());
        switch (mode.getValue()) {
            case "Swong" -> {
                float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30f * (1f - g) - 30f));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f));
            }
            case "Swank" -> {
                float g = hand * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * 30.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g * 40.0F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(g * 30.0F));

                float bobbing = MathHelper.sin(swingProgress * 6.2831855F) * 0.2F;
                matrices.translate(0, bobbing, 0);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g * 20.0F));
            }
            case "Static" -> {
                applyEquipOffset(matrices, arm, 0);
                applySwingOffset(matrices, arm, swingProgress);
            }
            case "Static 2" -> {
                applyEquipOffset(matrices, arm, equipProgress);
                applySwingOffset(matrices, arm, 0);
            }
            case "Balls" -> {
                float g = hand * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                applyEquipOffset(matrices, arm, 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -20.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g * 70.0F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(g * -20.0F));

                float bounce = hand * MathHelper.sin(swingProgress * 3.1415927F * 2) * 0.3F;
                matrices.translate(0, bounce, 0);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(swingProgress * 40.0F));
            }
            case "Ion" -> {
                matrices.scale(0.5f, 0.5f, 0.5f);
                if (mc.options.useKey.isPressed()) {
                    float g = hand * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
                    applyEquipOffset(matrices, arm, 0);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50f));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30f * (1f - g) - 30f));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110f));
                } else {
                    applyEquipOffset(matrices, arm, 0);
                    applySwingOffset(matrices, arm, swingProgress);
                }
            }
            default -> {
                matrices.translate((float)armMultiplier * swingX, swingY, swingZ);
                applyEquipOffset(matrices, arm, equipProgress);
                applySwingOffset(matrices, arm, swingProgress);
            }
        }
    }
    public void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!player.isUsingSpyglass()) {
            boolean isMainHand = hand == Hand.MAIN_HAND;
            Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            matrices.push();
            if (item.isEmpty()) {
                if (isMainHand && !player.isInvisible()) {
                    renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
                }
            } else if (item.contains(DataComponentTypes.MAP_ID)) {
                if (isMainHand && player.getOffHandStack().isEmpty()) {
                    renderMapInBothHands(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
                } else {
                    renderMapInOneHand(matrices, vertexConsumers, light, equipProgress, arm, swingProgress, item);
                }
            } else {
                boolean isRightHand;
                float useTimeRemaining;
                float progress;
                float sinValue;
                float offsetProgress;
                if (item.isOf(Items.CROSSBOW)) {
                    isRightHand = CrossbowItem.isCharged(item);
                    boolean rightSide = arm == Arm.RIGHT;
                    int handMultiplier = rightSide ? 1 : -1;
                    if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                        applyEquipOffset(matrices, arm, equipProgress);
                        matrices.translate((float)handMultiplier * -0.4785682F, -0.094387F, 0.05731531F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)handMultiplier * 65.3F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)handMultiplier * -9.785F));
                        useTimeRemaining = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
                        progress = useTimeRemaining / (float)CrossbowItem.getPullTime(item, player);
                        if (progress > 1.0F) {
                            progress = 1.0F;
                        }

                        if (progress > 0.1F) {
                            sinValue = MathHelper.sin((useTimeRemaining - 0.1F) * 1.3F);
                            offsetProgress = progress - 0.1F;
                            float animationFactor = sinValue * offsetProgress;
                            matrices.translate(animationFactor * 0.0F, animationFactor * 0.004F, animationFactor * 0.0F);
                        }

                        matrices.translate(progress * 0.0F, progress * 0.0F, progress * 0.04F);
                        matrices.scale(1.0F, 1.0F, 1.0F + progress * 0.2F);
                        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)handMultiplier * 45.0F));
                    } else {
                        swingArm(swingProgress, equipProgress, matrices, handMultiplier, arm);

                        if (isRightHand && swingProgress < 0.001F && isMainHand) {
                            matrices.translate((float)handMultiplier * -0.641864F, 0.0F, 0.0F);
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)handMultiplier * 10.0F));
                        }
                    }

                    renderItem(player, item, rightSide ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !rightSide, matrices, vertexConsumers, light);
                } else {
                    isRightHand = arm == Arm.RIGHT;
                    int handMultiplier = isRightHand ? 1 : -1;
                    if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                        float drawTime;
                        switch (item.getUseAction()) {
                            case NONE:
                                applyEquipOffset(matrices, arm, equipProgress);
                                break;
                            case EAT:
                            case DRINK:
                                applyEatOrDrinkTransformation(matrices, tickDelta, arm, item, player);
                                applyEquipOffset(matrices, arm, equipProgress);
                                break;
                            case BLOCK:
                                applyEquipOffset(matrices, arm, equipProgress);
                                if (!(item.getItem() instanceof ShieldItem)) {
                                    matrices.translate((float)handMultiplier * -0.14142136F, 0.08F, 0.14142136F);
                                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
                                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)handMultiplier * 13.365F));
                                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)handMultiplier * 78.05F));
                                }
                                break;
                            case BOW:
                                applyEquipOffset(matrices, arm, equipProgress);
                                matrices.translate((float)handMultiplier * -0.2785682F, 0.18344387F, 0.15731531F);
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)handMultiplier * 35.3F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)handMultiplier * -9.785F));
                                drawTime = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
                                useTimeRemaining = drawTime / 20.0F;
                                useTimeRemaining = (useTimeRemaining * useTimeRemaining + useTimeRemaining * 2.0F) / 3.0F;
                                if (useTimeRemaining > 1.0F) {
                                    useTimeRemaining = 1.0F;
                                }

                                if (useTimeRemaining > 0.1F) {
                                    progress = MathHelper.sin((drawTime - 0.1F) * 1.3F);
                                    sinValue = useTimeRemaining - 0.1F;
                                    offsetProgress = progress * sinValue;
                                    matrices.translate(offsetProgress * 0.0F, offsetProgress * 0.004F, offsetProgress * 0.0F);
                                }

                                matrices.translate(useTimeRemaining * 0.0F, useTimeRemaining * 0.0F, useTimeRemaining * 0.04F);
                                matrices.scale(1.0F, 1.0F, 1.0F + useTimeRemaining * 0.2F);
                                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)handMultiplier * 45.0F));
                                break;
                            case SPEAR:
                                applyEquipOffset(matrices, arm, equipProgress);
                                matrices.translate((float)handMultiplier * -0.5F, 0.7F, 0.1F);
                                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)handMultiplier * 35.3F));
                                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)handMultiplier * -9.785F));
                                drawTime = (float)item.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
                                useTimeRemaining = drawTime / 10.0F;
                                if (useTimeRemaining > 1.0F) {
                                    useTimeRemaining = 1.0F;
                                }

                                if (useTimeRemaining > 0.1F) {
                                    progress = MathHelper.sin((drawTime - 0.1F) * 1.3F);
                                    sinValue = useTimeRemaining - 0.1F;
                                    offsetProgress = progress * sinValue;
                                    matrices.translate(offsetProgress * 0.0F, offsetProgress * 0.004F, offsetProgress * 0.0F);
                                }

                                matrices.translate(0.0F, 0.0F, useTimeRemaining * 0.2F);
                                matrices.scale(1.0F, 1.0F, 1.0F + useTimeRemaining * 0.2F);
                                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)handMultiplier * 45.0F));
                                break;
                            case BRUSH:
                                applyBrushTransformation(matrices, tickDelta, arm, item, player, equipProgress);
                                break;
                            case BUNDLE:
                                swingArm(swingProgress, equipProgress, matrices, handMultiplier, arm);
                        }
                    } else if (player.isUsingRiptide()) {
                        applyEquipOffset(matrices, arm, equipProgress);
                        matrices.translate((float)handMultiplier * -0.4F, 0.8F, 0.3F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)handMultiplier * 65.0F));
                        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)handMultiplier * -85.0F));
                    } else {
                        swordAnimation(swingProgress, equipProgress, matrices, handMultiplier, arm);
                    }

                    renderItem(player, item, isRightHand ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !isRightHand, matrices, vertexConsumers, light);
                }
            }

            matrices.pop();
        }
    }

    public void renderArmHoldingItem(MatrixStack matrixStack, VertexConsumerProvider renderBuffers, int lightLevel, float equipmentProgress, float swingProgress, Arm armSide) {
        boolean isRightArm = armSide != Arm.LEFT;
        float armDirectionFactor = isRightArm ? 1.0F : -1.0F;
        float swingProgressSqrt = MathHelper.sqrt(swingProgress);
        float horizontalSwingOffset = -0.3F * MathHelper.sin(swingProgressSqrt * 3.1415927F);
        float verticalSwingOffset = 0.4F * MathHelper.sin(swingProgressSqrt * 6.2831855F);
        float forwardSwingOffset = -0.4F * MathHelper.sin(swingProgress * 3.1415927F);

        matrixStack.translate(
                armDirectionFactor * (horizontalSwingOffset + 0.64000005F),
                verticalSwingOffset + -0.6F + equipmentProgress * -0.6F,
                forwardSwingOffset + -0.71999997F
        );

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armDirectionFactor * 45.0F));

        float swingSquaredSin = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float swingSin = MathHelper.sin(swingProgressSqrt * 3.1415927F);

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armDirectionFactor * swingSin * 70.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armDirectionFactor * swingSquaredSin * -20.0F));

        AbstractClientPlayerEntity playerEntity = mc.player;

        matrixStack.translate(armDirectionFactor * -1.0F, 3.6F, 3.5F);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armDirectionFactor * 120.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armDirectionFactor * -135.0F));
        matrixStack.translate(armDirectionFactor * 5.6F, 0.0F, 0.0F);

        PlayerEntityRenderer playerRenderer = (PlayerEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(playerEntity);
        Identifier skinTexture = playerEntity.getSkinTextures().texture();

        if (isRightArm) {
            playerRenderer.renderRightArm(
                    matrixStack,
                    renderBuffers,
                    lightLevel,
                    skinTexture,
                    playerEntity.isPartVisible(PlayerModelPart.RIGHT_SLEEVE)
            );
        } else {
            playerRenderer.renderLeftArm(
                    matrixStack,
                    renderBuffers,
                    lightLevel,
                    skinTexture,
                    playerEntity.isPartVisible(PlayerModelPart.LEFT_SLEEVE)
            );
        }
    }

    public void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, float equipProgress) {
        applyEquipOffset(matrices, arm, equipProgress);
        float brushCycle = (float)(player.getItemUseTimeLeft() % 10);
        float brushTimeRemaining = brushCycle - tickDelta + 1.0F;
        float brushProgress = 1.0F - brushTimeRemaining / 10.0F;
        float brushRotation = -15.0F + 75.0F * MathHelper.cos(brushProgress * 2.0F * 3.1415927F);
        if (arm != Arm.RIGHT) {
            matrices.translate(0.1, 0.83, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(brushRotation));
            matrices.translate(-0.3, 0.22, 0.35);
        } else {
            matrices.translate(-0.25, 0.22, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(brushRotation));
        }
    }

    public void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {
        int handMultiplier = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate((float)handMultiplier * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    public void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) i * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) i * -45.0F));
    }

    public void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!stack.isEmpty()) {
            mc.getItemRenderer().renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, entity.getId() + renderMode.ordinal());
        }
    }

    public void swingArm(float swingProgress, float equipProgress, MatrixStack matrices, int armMultiplier, Arm arm) {
        float swingX = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927F);
        float swingY = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855F);
        float swingZ = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
        matrices.translate((float)armMultiplier * swingX, swingY, swingZ);
        applyEquipOffset(matrices, arm, equipProgress);
        applySwingOffset(matrices, arm, swingProgress);
    }

    public void renderFirstPersonMap(MatrixStack matrixStack, VertexConsumerProvider renderBuffers, int lightLevel, ItemStack itemStack) {
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
        matrixStack.scale(0.38F, 0.38F, 0.38F);
        matrixStack.translate(-0.5F, -0.5F, 0.0F);
        matrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);

        MapIdComponent mapId = (MapIdComponent)itemStack.get(DataComponentTypes.MAP_ID);
        MapState mapData = FilledMapItem.getMapState(mapId, mc.world);
        RenderLayer MAP_BACKGROUND = RenderLayer.getText(Identifier.ofVanilla("textures/map/map_background.png"));
        RenderLayer MAP_BACKGROUND_CHECKERBOARD = RenderLayer.getText(Identifier.ofVanilla("textures/map/map_background_checkerboard.png"));
        VertexConsumer vertexBuffer = renderBuffers.getBuffer(mapData == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f transformMatrix = matrixStack.peek().getPositionMatrix();

        vertexBuffer.vertex(transformMatrix, -7.0F, 135.0F, 0.0F).color(-1).texture(0.0F, 1.0F).light(lightLevel);
        vertexBuffer.vertex(transformMatrix, 135.0F, 135.0F, 0.0F).color(-1).texture(1.0F, 1.0F).light(lightLevel);
        vertexBuffer.vertex(transformMatrix, 135.0F, -7.0F, 0.0F).color(-1).texture(1.0F, 0.0F).light(lightLevel);
        vertexBuffer.vertex(transformMatrix, -7.0F, -7.0F, 0.0F).color(-1).texture(0.0F, 0.0F).light(lightLevel);

        if (mapData != null) {
            MapRenderer mapRenderer = mc.getMapRenderer();
            mapRenderer.update(mapId, mapData, new MapRenderState());
            mapRenderer.draw(new MapRenderState(), matrixStack, renderBuffers, false, lightLevel);
        }
    }
    public void renderMapInOneHand(MatrixStack matrixStack, VertexConsumerProvider renderBuffers, int lightLevel, float equipmentProgress, Arm armSide, float swingProgress, ItemStack itemStack) {
        float armDirectionFactor = armSide == Arm.RIGHT ? 1.0F : -1.0F;
        matrixStack.translate(armDirectionFactor * 0.125F, -0.125F, 0.0F);

        if (!mc.player.isInvisible()) {
            matrixStack.push();
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armDirectionFactor * 10.0F));
            renderArmHoldingItem(matrixStack, renderBuffers, lightLevel, equipmentProgress, swingProgress, armSide);
            matrixStack.pop();
        }

        matrixStack.push();
        matrixStack.translate(armDirectionFactor * 0.51F, -0.08F + equipmentProgress * -1.2F, -0.75F);

        float swingProgressSqrt = MathHelper.sqrt(swingProgress);
        float swingSin = MathHelper.sin(swingProgressSqrt * 3.1415927F);
        float horizontalOffset = -0.5F * swingSin;
        float verticalOffset = 0.4F * MathHelper.sin(swingProgressSqrt * 6.2831855F);
        float forwardOffset = -0.3F * MathHelper.sin(swingProgress * 3.1415927F);

        matrixStack.translate(armDirectionFactor * horizontalOffset, verticalOffset - 0.3F * swingSin, forwardOffset);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingSin * -45.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armDirectionFactor * swingSin * -30.0F));
        renderFirstPersonMap(matrixStack, renderBuffers, lightLevel, itemStack);
        matrixStack.pop();
    }

    public float getMapAngle(float pitchAngle) {
        float normalizedAngle = 1.0F - pitchAngle / 45.0F + 0.1F;
        normalizedAngle = MathHelper.clamp(normalizedAngle, 0.0F, 1.0F);
        normalizedAngle = -MathHelper.cos(normalizedAngle * 3.1415927F) * 0.5F + 0.5F;
        return normalizedAngle;
    }

    public void renderMapInBothHands(MatrixStack matrixStack, VertexConsumerProvider renderBuffers, int lightLevel, float pitchAngle, float equipmentProgress, float swingProgress) {
        float swingProgressSqrt = MathHelper.sqrt(swingProgress);
        float verticalSwingOffset = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
        float forwardSwingOffset = -0.4F * MathHelper.sin(swingProgressSqrt * 3.1415927F);

        matrixStack.translate(0.0F, -verticalSwingOffset / 2.0F, forwardSwingOffset);

        float mapAngle = getMapAngle(pitchAngle);
        matrixStack.translate(0.0F, 0.04F + equipmentProgress * -1.2F + mapAngle * -0.5F, -0.72F);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mapAngle * -85.0F));

        if (!mc.player.isInvisible()) {
            matrixStack.push();
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            renderArm(matrixStack, renderBuffers, lightLevel, Arm.RIGHT);renderArm(matrixStack, renderBuffers, lightLevel, Arm.LEFT);
            matrixStack.pop();
        }

        float swingSin = MathHelper.sin(swingProgressSqrt * 3.1415927F);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingSin * 20.0F));
        matrixStack.scale(2.0F, 2.0F, 2.0F);
        renderFirstPersonMap(matrixStack, renderBuffers, lightLevel, mc.player.getMainHandStack());
    }

    public void renderArm(MatrixStack matrixStack, VertexConsumerProvider renderBuffers, int lightLevel, Arm armSide) {
        PlayerEntityRenderer playerRenderer = (PlayerEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
        matrixStack.push();

        float armDirectionFactor = armSide == Arm.RIGHT ? 1.0F : -1.0F;

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(92.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armDirectionFactor * -41.0F));
        matrixStack.translate(armDirectionFactor * 0.3F, -1.1F, 0.45F);

        Identifier skinTexture = mc.player.getSkinTextures().texture();

        if (armSide == Arm.RIGHT) {
            playerRenderer.renderRightArm(
                    matrixStack,
                    renderBuffers,
                    lightLevel,
                    skinTexture,
                    mc.player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE)
            );
        } else {
            playerRenderer.renderLeftArm(
                    matrixStack,
                    renderBuffers,
                    lightLevel,
                    skinTexture,
                    mc.player.isPartVisible(PlayerModelPart.LEFT_SLEEVE)
            );
        }

        matrixStack.pop();
    }
    public void applyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player) {
        float useTimeRemaining = (float)player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float useProgress = useTimeRemaining / (float)stack.getMaxUseTime(player);
        float bobHeight;
        if (useProgress < 0.8F) {
            bobHeight = MathHelper.abs(MathHelper.cos(useTimeRemaining / 4.0F * 3.1415927F) * 0.1F);
            matrices.translate(0.0F, bobHeight, 0.0F);
        }

        bobHeight = 1.0F - (float) Math.pow(useProgress, 27.0);
        int handMultiplier = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(bobHeight * 0.6F * (float)handMultiplier, bobHeight * -0.5F, bobHeight * 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)handMultiplier * bobHeight * 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(bobHeight * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)handMultiplier * bobHeight * 30.0F));
    }

    public ItemTransforms() {

        getSettingRepository().registerSettings(x, y, z, mode, handSetting);
    }
}