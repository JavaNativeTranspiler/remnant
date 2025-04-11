package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.rotation.RotationComponent;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.player.ReachEvent;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ModuleInfo(name = "KillAura", description = "Attacks Enemies Within Range", category = ModuleCategory.COMBAT)
public class KillAura extends BaseModule implements QuickImports {

    private final SettingCategory aimingCategory = new SettingCategory("Aiming");
    private final SettingCategory targetingCategory = new SettingCategory("Targeting");
    private final SettingCategory behaviorCategory = new SettingCategory("Behavior");

    private final ModeSetting aimMath = new ModeSetting(aimingCategory, "Aim Math", "Select the aiming math", "Regular", "Regular", "Adaptive", "Linear", "Blatant");
    private final ModeSetting aimVector = new ModeSetting(aimingCategory, "Aim Vector", "Select the aiming location behavior", "Straight", "Straight", "Closest", "Random");
    private final RangeSetting<Double> horizontalSpeed = new RangeSetting<>(aimingCategory, "Horizontal Aim Speed", "Adjust the speed", 0.0, 100.0, 30.0, 50.0);
    private final RangeSetting<Double> verticalSpeed = new RangeSetting<>(aimingCategory, "Vertical Aim Speed", "Adjust the speed", 0.0, 100.0, 30.0, 50.0);

    private final NumberSetting<Double> range = new NumberSetting<>(behaviorCategory, "Reach", "Set the maximum distance", 3.0, 0.1, 6.0);
    private final MultiSetting conditions = new MultiSetting(targetingCategory, "Conditions", "Set activation conditions", "Clicking", "Holding Weapon", "Break Blocks");
    private final ModeSetting targetSorting = new ModeSetting(targetingCategory, "Target Sorting", "Choose how targets are prioritized", "Distance", "Distance", "HurtTime", "Health", "Rotation");

    @RegisterEvent
    private void render3DEventEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (event.getMode().equals(Render3DEvent.Mode.PRE)) {
            LivingEntity target = (LivingEntity) toList(mc.world.getEntities()).stream()
                    .filter(entity -> {
                        if (entity == mc.player) return false;
                        if (mc.player.distanceTo(entity) >= range.getValue()) return false;
                        if (!(entity instanceof LivingEntity)) return false;
                        if (entity.isPlayer()) {
                            if (SocialManager.isTargetedPlayer((PlayerEntity) entity) != entity) return false;
                        }
                        if (entity instanceof ArmorStandEntity) return false;
                        return true;
                    }).min(Comparator.comparingDouble(entity -> switch (targetSorting.getValue()) {
                        case "HurtTime" -> ((LivingEntity) entity).hurtTime;
                        case "Distance" -> mc.player.distanceTo(entity);
                        case "Health" -> ((LivingEntity) entity).getHealth();
                        case "Rotation" ->
                                Math.abs(MathHelper.wrapDegrees((float) MathUtils.toDegrees(Math.atan2(entity.getEyePos().subtract(mc.player.getEyePos()).z, entity.getEyePos().subtract(mc.player.getEyePos()).x)) - 90.0f) - mc.player.getYaw());
                        default -> throw new IllegalStateException("Unexpected value: " + targetSorting.getValue());
                    })).orElse(null);
            rotationComponent.setMultiPoint(0.5f);
            rotationComponent.setSilentRotation(true);
            rotationComponent.setHorizontalSpeed(horizontalSpeed.getRandomValue().floatValue());
            rotationComponent.setVerticalSpeed(verticalSpeed.getRandomValue().floatValue());
            if (target != null && !checkConditions()) {
                rotationComponent.queueRotation(target, RotationComponent.RotationPriority.HIGH, getAimMath(), getAimVecMode());
            }
        }
        setSuffix(aimMath.getValue());
    }

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (event.getMode().equals(TickEvent.Mode.PRE)) {

            LivingEntity target = (LivingEntity) toList(mc.world.getEntities()).stream()
                    .filter(entity -> {
                        if (entity == mc.player) return false;
                        if (mc.player.distanceTo(entity) >= range.getValue()) return false;
                        if (!(entity instanceof LivingEntity)) return false;
                        if (entity.isPlayer()) {
                            if (SocialManager.isTargetedPlayer((PlayerEntity) entity) != entity) return false;
                        }
                        if (entity instanceof ArmorStandEntity) return false;
                        return true;
                    }).min(Comparator.comparingDouble(entity -> switch (targetSorting.getValue()) {
                        case "HurtTime" -> ((LivingEntity) entity).hurtTime;
                        case "Distance" -> mc.player.distanceTo(entity);
                        case "Health" -> ((LivingEntity) entity).getHealth();
                        case "Rotation" ->
                                Math.abs(MathHelper.wrapDegrees((float) MathUtils.toDegrees(Math.atan2(entity.getEyePos().subtract(mc.player.getEyePos()).z, entity.getEyePos().subtract(mc.player.getEyePos()).x)) - 90.0f) - mc.player.getYaw());
                        default -> throw new IllegalStateException("Unexpected value: " + targetSorting.getValue());
                    })).orElse(null);
            if (target != null && !checkConditions()) {
                if (mc.player.getAttackCooldownProgress((float) mc.player.getAttributeValue(EntityAttributes.ATTACK_SPEED)) < 0.93) return;
                if (mc.crosshairTarget instanceof EntityHitResult result && result.getEntity() == target) {
                    InputUtils.simulateClick(0, 35);
//                    ChatUtils.sendFormattedMessage("atk" + mc.player.age);
                }
            }
        }
    }

    @RegisterEvent
    private void ReachEventListener(ReachEvent event) {
        event.setDistance(range.getValue());
    }
    public static <T> List<T> toList(Iterable<T> it) {
        return it == null ? List.of() : new ArrayList<>() {{ it.forEach(this::add); }};
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

    public KillAura() {
        getSettingRepository().registerSettings(aimingCategory, targetingCategory, behaviorCategory,
                aimMath, aimVector, horizontalSpeed, verticalSpeed, range, conditions, targetSorting);
    }
}