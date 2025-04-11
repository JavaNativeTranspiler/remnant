
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.api.system.rotation.RotationComponent;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.math.rotation.RotationUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.LinkedHashSet;
import java.util.Objects;

@ModuleInfo(
        name = "AutoWeb",
        description = "Places Webs On Enemies",
        category = ModuleCategory.UTILITY
)
public class AutoWeb extends BaseModule implements QuickImports {
    private final SettingCategory general = new SettingCategory("Aim");

    private final SettingCategory main = new SettingCategory("General");
    private final ModeSetting aimMode = new ModeSetting(general, "Aim Mode", "Select the aiming behavior", "Regular", "Regular", "Adaptive", "Linear", "Blatant");
    private final BooleanSetting silent = new BooleanSetting(general, "Silent Rotations", "Rotates Silently", true);
    private final RangeSetting<Double> horizontalSpeed = new RangeSetting<>(general, "Horizontal Aim Speed", "Adjust the speed", 0.0, 100.0, 30.0, 50.0);
    private final RangeSetting<Double> verticalSpeed = new RangeSetting<>(general, "Vertical Aim Speed", "Adjust the speed", 0.0, 100.0, 30.0, 50.0);

    private final BooleanSetting includeHead = new BooleanSetting("Include Head", "Blocks the enemies head too", false);
    private final RangeSetting<Double> range = new RangeSetting<>(main, "Range", "Set the maximum distance", 0.0, 12.0, 1.0, 3.5);
    private final NumberSetting<Integer> webDelay = new NumberSetting<>(main, "Web Delay", "Adjust the of the autoweb (in seconds)", 10, 0, 100);
    private final NumberSetting<Double> fov = new NumberSetting<>(main, "FOV", "Define the field of view for target detection", 90.0, 0.0, 360.0);
    private final MultiSetting conditions = new MultiSetting(main, "Conditions", "Set activation conditions", "Clicking", "Holding Web", "Break Blocks", "Not When Affects Player");

    private final TimerUtils waitTimer = new TimerUtils();

    private float[] lastRotations = new float[2];
    private BlockPos lastPos;

    @RegisterEvent
    private void Render3DEventListener(Render3DEvent event) {
        if (event.getMode().equals(Render3DEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            PlayerEntity enemy = SocialManager.getTarget();
            rotationComponent.setHorizontalSpeed(horizontalSpeed.getRandomValue().floatValue());
            rotationComponent.setVerticalSpeed(verticalSpeed.getRandomValue().floatValue());
            rotationComponent.setSilentRotation(silent.getValue());

            if (enemy == null) {
                lastRotations = rotationComponent.getLastRotations();
                lastPos = null;
                return;
            }

            Vec3d feetPos = getBestPlacementSpot(event.getMatrices(), enemy, true);
            Vec3d eyePos = getBestPlacementSpot(event.getMatrices(), enemy, false);

            boolean revertRotation = false;

            if (feetPos == null && eyePos == null) revertRotation = true;

            if (enemy.distanceTo(mc.player) > range.getValueMax() || enemy.distanceTo(mc.player) < range.getValueMin()) {
                revertRotation = true;
            }

            Vec3d enemyEyePos = enemy.getPos().add(0, enemy.getEyeHeight(enemy.getPose()), 0);
            Vec3d playerEyePos = mc.player.getEyePos();
            if (Math.toDegrees(MathUtils.angleBetween(mc.player.getRotationVector(), enemyEyePos.subtract(playerEyePos))) >= fov.getValue() / 2) {
                revertRotation = true;
            }

            if (conditions.getSpecificValue("Not When Affects Player")) {
                if (mc.player.getBoundingBox().intersects(new Box(feetPos.x - 0.5f, feetPos.y, feetPos.z - 0.5f, feetPos.x + 0.5f, feetPos.y + 1, feetPos.z + 0.5f)))
                    revertRotation = true;
                if (includeHead.getValue() && mc.player.getBoundingBox().intersects(new Box(eyePos.x - 0.5f, eyePos.y, eyePos.z - 0.5f, eyePos.x + 0.5f, eyePos.y + 1, eyePos.z + 0.5f)))
                    revertRotation = true;
            }

            if (!waitTimer.hasReached(webDelay.getValue() * 1000)) revertRotation = true;
            if (checkConditions()) revertRotation = true;
            if (revertRotation) {
                rotationComponent.queueRotation(new float[]{mc.player.getYaw(), mc.player.getPitch()}, RotationComponent.RotationPriority.MEDIUM, getAimMode());
            }
            if (feetPos != null && !revertRotation) {
                Vec3d targetPosition = (eyePos != null && includeHead.getValue()) ? eyePos : feetPos;
                BlockPos placeBlock = BlockPos.ofFloored(targetPosition).down();
                BlockPos blockAbove = placeBlock.up();

                if (eyePos != null) {
                    Render3DEngine.renderOutlinedBox(eyePos.subtract(0, 0.5, 0), Color.GREEN, event.getMatrices(), 0.5f, 1);
                } else {
                    Render3DEngine.renderOutlinedBox(feetPos.subtract(0, 0.5, 0), Color.RED, event.getMatrices(), 0.5f, 1);
                }
                if (mc.world.getBlockState(blockAbove).isAir()) {
                    float[] rotation = getRotationToBlock(targetPosition.subtract(0, 1.5, 0), Direction.UP, enemy, event.getMatrices());
                    rotationComponent.queueRotation(
                            Objects.requireNonNullElseGet(rotation, () -> new float[]{mc.player.getYaw(), mc.player.getPitch()}),
                            RotationComponent.RotationPriority.HIGH,
                            getAimMode()
                    );
                    lastPos = placeBlock;
                }
            }
            lastRotations = rotationComponent.getLastRotations();
        }
    }

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (lastPos == null) return;
        if (mc.player.age % 2 == 0) return;
        HitResult hitResult = mc.crosshairTarget;
        if (hitResult instanceof BlockHitResult blockHitResult && waitTimer.hasReached(webDelay.getValue() * 1000)) {
            if (blockHitResult.getBlockPos().toShortString().equals(lastPos.toShortString()) && blockHitResult.getSide() == Direction.UP) {
                InputUtils.simulateClick(1, 50);
                waitTimer.reset();
            }
        }
    }

    private RotationComponent.AimType getAimMode() {
        return switch (aimMode.getValue()) {
            case "Regular" -> RotationComponent.AimType.REGULAR;
            case "Adaptive" -> RotationComponent.AimType.ADAPTIVE;
            case "Linear" -> RotationComponent.AimType.LINEAR;
            case "Blatant" -> RotationComponent.AimType.BLATANT;
            default -> RotationComponent.AimType.REGULAR;
        };
    }

    private boolean checkConditions() {
        for (String check : new String[]{"Holding Weapon", "Clicking", "Break Blocks"}) {
            boolean isTrue = switch (check) {
                case "Holding Web" ->
                        conditions.getSpecificValue(check) && mc.player.getInventory().getMainHandStack().getItem().equals(Items.COBWEB);
                case "Clicking" -> conditions.getSpecificValue(check) && !InputUtils.mouseDown(1);
                case "Break Blocks" -> conditions.getSpecificValue(check) && mc.interactionManager.isBreakingBlock();
                default -> false;
            };
            if (isTrue) {
                return true;
            }
        }
        return false;
    }

    public Vec3d getBestPlacementSpot(MatrixStack matrices, PlayerEntity player, boolean feet) {
        Vec3d feetPosition = player.getPos();
        Vec3d eyePosition = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        LinkedHashSet<Vec3d> feetPositions = new LinkedHashSet<>();
        LinkedHashSet<Vec3d> eyePositions = new LinkedHashSet<>();
        Box bBox = player.getBoundingBox();
        // this gets the blocks the players feet intersects with (I think)
        for (float xOffset = -5; xOffset <= 5; xOffset += 1) {
            for (float zOffset = -5; zOffset <= 5; zOffset += 1) {
                float offset = (float) (bBox.getAverageSideLength() / 2f);
                BlockPos feetPos = BlockPos.ofFloored(new Vec3d(feetPosition.x + xOffset, feetPosition.y, feetPosition.z + zOffset));
                BlockPos eyePos = BlockPos.ofFloored(new Vec3d(eyePosition.x + xOffset, eyePosition.y, eyePosition.z + zOffset));
                Vec3d feetCenter = feetPos.toCenterPos();
                Vec3d eyeCenter = eyePos.toCenterPos();
                if (bBox.intersects(feetCenter.x - offset, feetCenter.y - offset, feetCenter.z - offset, feetCenter.x + offset, feetCenter.y + offset, feetCenter.z + offset)) {
                    feetPositions.add(feetPos.toCenterPos());
                }
                if (bBox.intersects(eyeCenter.x - offset, eyeCenter.y - offset, eyeCenter.z - offset, eyeCenter.x + offset, eyeCenter.y + offset, eyeCenter.z + offset)) {
                    eyePositions.add(eyePos.toCenterPos());
                }
            }
        }
        // finds the position in which the player intersects the most kinda
        Vec3d bestFeetPosition = null;
        if (!feetPositions.isEmpty()) {
            double distance = Double.MAX_VALUE;
            for (Vec3d position : feetPositions) {
                if (!mc.world.getBlockState(BlockPos.ofFloored(position)).isAir()) continue;
                double distanceToEnemy = position.distanceTo(player.getPos());
                if (distanceToEnemy < distance && distanceToEnemy > 0.5) {
                    bestFeetPosition = position;
                    distance = distanceToEnemy;
                }
            }
        }
        Vec3d bestEyePosition = null;
        if (!eyePositions.isEmpty()) {
            double distance = Double.MAX_VALUE;
            for (Vec3d position : eyePositions) {
                if (mc.world.getBlockState(BlockPos.ofFloored(position).down()).isAir()) continue;
                double distanceToEnemy = position.distanceTo(player.getPos());
                if (distanceToEnemy < distance && distanceToEnemy > 0.5) {
                    bestEyePosition = position;
                    distance = distanceToEnemy;
                }
            }

        }
        rotationComponent.setSilentRotation(true);

        return feet ? bestFeetPosition : bestEyePosition;
    }

    public static float[] getRotationToBlock(Vec3d blockPos, Direction direction, PlayerEntity entity, MatrixStack matrixStack) {
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d pos = null;
        Box bBox = entity.getBoundingBox();
        double distance = Double.MAX_VALUE;

        // gets rotations: figures out if theere are any possible places to place, witthout intersecting hitbox
        for (float xOffset = -0.45f; xOffset <= 0.45f; xOffset += 0.05f) {
            for (float zOffset = -0.45f; zOffset <= 0.45f; zOffset += 0.05f) {
                Vec3d target = blockPos.add(direction.getOffsetX() - xOffset, direction.getOffsetY() + 0.05f, direction.getOffsetZ() - zOffset);

                if (RotationUtils.rayTraceWithCobwebs(target) instanceof BlockHitResult blockHitResult) {
                    if (!blockHitResult.getBlockPos().equals(BlockPos.ofFloored(target))) {
                        continue;
                    }
                } else {
                    continue;
                }
                if (bBox.intersects(target.x, target.y, target.z, target.x, target.y + 1, target.z)) {
                    continue;
                }
                if (playerPos.distanceTo(target) < distance) {
                    distance = playerPos.distanceTo(target);
                    pos = target;
                }
            }
        }
        if (pos == null) {
            return null;
        }
        Render3DEngine.renderShadedBox(blockPos, Color.white, 50, matrixStack, 0.5f, 1f);
        Render3DEngine.renderShadedBox(pos, Color.red, 50, matrixStack, 0.05f, 0.1f);
        Vec3d delta = pos.subtract(playerPos);

        double distanceXZ = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, distanceXZ));

        return new float[]{yaw, pitch};
    }

    public AutoWeb() {
        getSettingRepository().registerSettings(general, main, aimMode, silent, horizontalSpeed, verticalSpeed, includeHead, webDelay, range, fov, conditions);
    }
}
