
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.EventPriority;
import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.api.system.rotation.RotationComponent;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.rotation.RotationUtils;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.impl.events.player.SilentRotationEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

@ModuleInfo(
        name = "Scaffold",
        description = "Bridges Automatically",
        category = ModuleCategory.UTILITY
)
public class Scaffold extends BaseModule implements QuickImports {
    private final SettingCategory behaviorSettings = new SettingCategory("Behavior");
    private final ModeSetting mode = new ModeSetting(behaviorSettings, "Mode", "Select the scaffold behavior", "Normal", "Normal", "Eagle", "Keep-y", "Telly");
    private final ModeSetting aimMode = new ModeSetting(behaviorSettings, "Aim Mode", "Select the aiming behavior", "Linear", "Normal", "Adaptive", "Linear", "Blatant");
    private final RangeSetting<Double> horizontalSpeed = new RangeSetting<>(behaviorSettings, "Horizontal Aim Speed", "Adjust the speed", 0.0, 100.0, 50.0, 50.0);
    private final RangeSetting<Double> verticalSpeed = new RangeSetting<>(behaviorSettings, "Vertical Aim Speed", "Adjust the speed", 0.0, 100.0, 50.0, 50.0);


    int[][] offsets = {
            {1, 0, 0},    // Right
            {0, 0, 1},    // Forward
            {-1, 0, 0},   // Left
            {0, 0, -1},    // Back
            {0, 1, 0}, // Top
    };

    private float[] lastRotations = new float[2];
    int originalSlot = -1;

    @RegisterEvent
    private void render3DEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        rotationComponent.setSilentRotation(true);
        rotationComponent.setHorizontalSpeed(horizontalSpeed.getRandomValue().floatValue());
        rotationComponent.setVerticalSpeed(verticalSpeed.getRandomValue().floatValue());
        setSuffix(mode.getValue());

        if (InventoryUtils.getMainHandItem().getItem() instanceof BlockItem) {
            Vec3d closestPoint = findScaffoldPoints(4, event.getMatrices());
            if (closestPoint == null) return;
            Direction closestSide = RotationUtils.getClosestSide(BlockPos.ofFloored(closestPoint), mc.player.getPos());
            //ChatUtils.sendFormattedMessage("" + allowScaffold() + " "  + " " + closestSide.toString());
            if (allowScaffold()) {
                Render3DEngine.renderOutlinedShadedBox(closestPoint, Color.WHITE, 1, event.getMatrices(), 0.5f, 1);
                float[] targetRotations = RotationUtils.getRotationToBlock(BlockPos.ofFloored(closestPoint), closestSide);
                if (closestSide != Direction.UP) {
                    rotationComponent.queueRotation(targetRotations, RotationComponent.RotationPriority.HIGH, getAimMode());
                } else {
                    rotationComponent.queueRotation(lastRotations, RotationComponent.RotationPriority.HIGH, getAimMode());
                }
            } else {
                if (mode.getValue().equals("Telly")) {
                    rotationComponent.queueRotation(new float[]{mc.player.getYaw(), mc.player.getPitch()}, RotationComponent.RotationPriority.HIGH, getAimMode());
                } else {
                    rotationComponent.queueRotation(lastRotations, RotationComponent.RotationPriority.HIGH, getAimMode());
                }
            }
        } else {
            int slot = InventoryUtils.findItemWithPredicateInHotbar(item -> {
                if (!(item.getItem() instanceof BlockItem blockItem)) return false;
                if (!blockItem.getBlock().getDefaultState().isFullCube(mc.world, BlockPos.ORIGIN)) return false;
                if (blockItem.getBlock().getDefaultState().hasBlockEntity()) return false;
                return true;
            });
            if (slot != -1) {
                if (originalSlot == -1) {
                    originalSlot = mc.player.getInventory().selectedSlot;
                }
                mc.player.getInventory().selectedSlot = slot;
            }
        }

        lastRotations = rotationComponent.getLastRotations();
    }

    @RegisterEvent(value = EventPriority.HIGH)
    private void silentRotationEventListener(SilentRotationEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!(InventoryUtils.getMainHandItem().getItem() instanceof BlockItem)) return;

        BlockHitResult hitResult = RotationUtils.rayTrace(lastRotations[0], lastRotations[1], 4f);
        if (hitResult.getType().equals(HitResult.Type.BLOCK)) {
            if (!allowScaffold()) return;
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    @Override
    protected void onModuleDisabled() {
        if (mc.player == null || mc.world == null) return;
        if (originalSlot == -1) return;
        mc.player.getInventory().selectedSlot = originalSlot;
        originalSlot = -1;
    }

    public boolean allowScaffold() {
        if (mc.player == null || mc.world == null) return false;

        Vec3d closestPoint = findScaffoldPoints(4, null);
        if (closestPoint == null) return false;

        BlockPos closestBlockPos = BlockPos.ofFloored(closestPoint);
        Direction closestSide = RotationUtils.getClosestSide(closestBlockPos, mc.player.getPos());

        String mode = this.mode.getValue();

        if (!mode.equals("Telly")) {

        } else {
            double distance = closestPoint.distanceTo(mc.player.getPos());
            if (mc.player.fallDistance < 0 || distance < 1.05) return false;
        }

        return !mode.equals("Keep-y");
    }


    private RotationComponent.AimType getAimMode() {
        return switch (aimMode.getValue()) {
            case "Normal" -> RotationComponent.AimType.REGULAR;
            case "Adaptive" -> RotationComponent.AimType.ADAPTIVE;
            case "Linear" -> RotationComponent.AimType.LINEAR;
            case "Blatant" -> RotationComponent.AimType.BLATANT;
            default -> RotationComponent.AimType.REGULAR;
        };
    }


    public Vec3d findScaffoldPoints(int offset, MatrixStack matrices) {

        ArrayList<Vec3d> possibleScaffoldPoints = new ArrayList<>();
        for (float x = -offset; x <= offset; x += 1f) {
            for (float y = -3; y <= -1; y += 1f) {
                for (float z = -offset; z <= offset; z += 1f) {
                    Vec3d point = new Vec3d(
                            mc.player.getBlockPos().getX() + x + .5,
                            mc.player.getBlockPos().getY() + y,
                            mc.player.getBlockPos().getZ() + z + .5);
                    for (int[] blockOffset : offsets) {
                        Vec3d newPoint = point.add(blockOffset[0], blockOffset[1], blockOffset[2]);
                        BlockState blockState = mc.world.getBlockState(BlockPos.ofFloored(newPoint));
                        if (blockState.isAir()) continue;
                        if (blockState.hasBlockEntity()) continue;
                        if (!RotationUtils.rayTrace(point).getPos().equals(point)) continue;
                        if (!blockState.isFullCube(mc.world, BlockPos.ofFloored(newPoint))) continue;
                        if (blockState.getBlock().equals(Blocks.WATER) || blockState.getBlock().equals(Blocks.LAVA) || blockState.getBlock().equals(Blocks.BUBBLE_COLUMN))
                            continue;
                        if (possibleScaffoldPoints.stream().noneMatch(pos -> newPoint == pos)) {
                            if (matrices != null) {
                                Render3DEngine.renderOutlinedBox(newPoint, Color.GREEN, matrices, 0.5f, 1f);
                            }
                            possibleScaffoldPoints.add(newPoint);
                        }
                    }
                }
            }
        }
        possibleScaffoldPoints.sort(Comparator.comparingDouble(pos -> pos.distanceTo(mc.player.getPos())));
        return possibleScaffoldPoints.isEmpty() ? null : possibleScaffoldPoints.getFirst();
    }


    public Scaffold() {
        getSettingRepository().registerSettings(behaviorSettings, mode, aimMode, horizontalSpeed, verticalSpeed);
    }
}
