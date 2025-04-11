package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.rotation.RotationComponent;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.math.rotation.RotationUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;

@ModuleInfo(name = "AimAssist",
        description = "Assists, Or Aims For You.",
        category = ModuleCategory.COMBAT)
public class AimAssist extends BaseModule implements QuickImports {

    private final SettingCategory aimingCategory = new SettingCategory("Aiming");
    private final SettingCategory targetingCategory = new SettingCategory("Targeting");
    private final SettingCategory behaviorCategory = new SettingCategory("Behavior");

    private final ModeSetting aimMode = new ModeSetting(aimingCategory, "Aim Mode", "Select the aiming mode", "Normal", "Normal", "Assist");
    private final ModeSetting aimMath = new ModeSetting(aimingCategory, "Aim Math", "Select the aiming math", "Regular", "Regular", "Adaptive", "Linear", "Blatant");
    private final ModeSetting aimVector = new ModeSetting(aimingCategory, "Aim Vector", "Select the aiming location behavior", "Straight", "Straight", "Closest", "Random");
    private final BooleanSetting silent = new BooleanSetting(aimingCategory, "Silent Rotations", "Rotates Silently", false);
    private final RangeSetting<Double> horizontalSpeed = new RangeSetting<>(aimingCategory, "Horizontal Aim Speed", "Adjust the speed", 0.0, 100.0, 30.0, 50.0);
    private final RangeSetting<Double> verticalSpeed = new RangeSetting<>(aimingCategory, "Vertical Aim Speed", "Adjust the speed", 0.0, 100.0, 30.0, 50.0);

    private final NumberSetting<Double> range = new NumberSetting<>(behaviorCategory, "Range", "Set the maximum distance", 6.0, 1.0, 50.0);
    private final NumberSetting<Double> fov = new NumberSetting<>(behaviorCategory, "FOV", "Define the field of view for target detection", 90.0, 0.0, 360.0);

    private final NumberSetting<Double> multipoint = new NumberSetting<>(behaviorCategory, "Multipoint", "Control aiming point between center and edges", 50.0, 0.0, 100.0);
    private final MultiSetting conditions = new MultiSetting(targetingCategory, "Conditions", "Set activation conditions", "Clicking", "Holding Weapon", "Break Blocks");
    private final ModeSetting targetSorting = new ModeSetting(targetingCategory, "Target Sorting", "Choose how targets are prioritized", "Distance", "Distance", "HurtTime", "Health", "Rotation");

    private final TimerUtils rotationTime = new TimerUtils();
    @RegisterEvent
    private void render3DEventEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null || mc.currentScreen != null) return;
        if (event.getMode().equals(Render3DEvent.Mode.PRE)) {
            PlayerEntity target = mc.world.getPlayers().stream()
                    .filter(player -> player != mc.player
                            && mc.player.distanceTo(player) <= range.getValue()
                            && SocialManager.isEnemy(player)
                            && SocialManager.isTargetedPlayer(player) == player
                            && Math.toDegrees(MathUtils.angleBetween(mc.player.getRotationVector(), player.getPos().add(0, player.getEyeHeight(player.getPose()), 0).subtract(mc.player.getEyePos()))) <= fov.getValue() / 2)
                    .min(Comparator.comparingDouble(player -> switch (targetSorting.getValue()) {
                        case "HurtTime" -> player.hurtTime;
                        case "Distance" -> mc.player.distanceTo(player);
                        case "Health" -> player.getHealth();
                        case "Rotation" ->
                                Math.abs(MathHelper.wrapDegrees((float) MathUtils.toDegrees(Math.atan2(player.getEyePos().subtract(mc.player.getEyePos()).z, player.getEyePos().subtract(mc.player.getEyePos()).x)) - 90.0f) - mc.player.getYaw());
                        default -> throw new IllegalStateException("Unexpected value: " + targetSorting.getValue());
                    }))
                    .orElse(null);
            rotationComponent.setMultiPoint(multipoint.getValue().floatValue() * 0.01f);
            rotationComponent.setSilentRotation(silent.getValue());
            rotationComponent.setHorizontalSpeed(horizontalSpeed.getRandomValue().floatValue());
            rotationComponent.setVerticalSpeed(verticalSpeed.getRandomValue().floatValue());
            if (target != null && !checkConditions()) {
                if (!rotationTime.hasReached(100) || aimMode.getValue().equals("Normal")) {
                    rotationComponent.queueRotation(target, RotationComponent.RotationPriority.MEDIUM, getAimMath(), getAimVecMode());
                }
            }
        }
        setSuffix(aimMath.getValue());
    }
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null || mc.currentScreen != null) return;
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            PlayerEntity target = mc.world.getPlayers().stream()
                    .filter(player -> player != mc.player
                            && mc.player.distanceTo(player) <= range.getValue()
                            && SocialManager.isEnemy(player)
                            && SocialManager.isTargetedPlayer(player) == player
                            && Math.toDegrees(MathUtils.angleBetween(mc.player.getRotationVector(), player.getPos().add(0, player.getEyeHeight(player.getPose()), 0).subtract(mc.player.getEyePos()))) <= fov.getValue() / 2)
                    .min(Comparator.comparingDouble(player -> switch (targetSorting.getValue()) {
                        case "HurtTime" -> player.hurtTime;
                        case "Distance" -> mc.player.distanceTo(player);
                        case "Health" -> player.getHealth();
                        case "Rotation" ->
                                Math.abs(MathHelper.wrapDegrees((float) MathUtils.toDegrees(Math.atan2(player.getEyePos().subtract(mc.player.getEyePos()).z, player.getEyePos().subtract(mc.player.getEyePos()).x)) - 90.0f) - mc.player.getYaw());
                        default -> throw new IllegalStateException("Unexpected value: " + targetSorting.getValue());
                    }))
                    .orElse(null);
            if (target != null) {
                float targetYaw = MathHelper.wrapDegrees(RotationUtils.calculate(target.getEyePos())[0]);
                float prevYawDiff = targetYaw - MathHelper.wrapDegrees(mc.player.prevYaw);
                float currentYawDiff = targetYaw - MathHelper.wrapDegrees(mc.player.getYaw());

                float threshold = 0.5f * (Math.abs(currentYawDiff) / 180); // change the threshold if u want it more/less sensitive

                if (Math.abs(currentYawDiff) > (Math.abs(prevYawDiff) + threshold)) {
                    rotationTime.reset();
                }
            }
        }
    }
    private RotationComponent.AimType getAimMath() {
        return switch (aimMath.getValue()) {
            case "Regular" -> RotationComponent.AimType.REGULAR;
            case "Adaptive" -> RotationComponent.AimType.ADAPTIVE;
            case "Linear" -> RotationComponent.AimType.LINEAR;
            case "Blatant" -> RotationComponent.AimType.BLATANT;
            default -> RotationComponent.AimType.REGULAR;
        };
    }
    private RotationComponent.EntityPoints getAimVecMode() {
        return switch (aimVector.getValue()) {
            case "Straight" -> RotationComponent.EntityPoints.STRAIGHT;
            case "Closest" -> RotationComponent.EntityPoints.CLOSEST;
            case "Random" -> RotationComponent.EntityPoints.RANDOM;
            default -> RotationComponent.EntityPoints.STRAIGHT;
        };
    }
    private boolean checkConditions() {
        for (String check : new String[]{"Holding Weapon", "Clicking", "Break Blocks"}) {
            boolean isTrue = switch (check) {
                case "Holding Weapon" ->
                        conditions.getSpecificValue(check) && !(mc.player.getInventory().getMainHandStack().getItem() instanceof SwordItem);
                case "Clicking" ->
                        conditions.getSpecificValue(check) && !InputUtils.mouseDown(0);
                case "Break Blocks" ->
                        conditions.getSpecificValue(check) && mc.interactionManager.isBreakingBlock();
                default -> false;
            };
            if (isTrue) {
                return true;
            }
        }
        return false;
    }

    public AimAssist() {
        getSettingRepository().registerSettings(aimingCategory, targetingCategory, behaviorCategory,
                aimMode, aimMath, aimVector, silent, horizontalSpeed, verticalSpeed, range, fov, conditions, targetSorting,
                multipoint);
    }
}