package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.rotation.RotationComponent;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.misc.EntityHitboxEvent;
import com.chorus.impl.events.player.SwingEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "Hitboxes", description = "Expands Enemies Hit-boxes", category = ModuleCategory.COMBAT)
public class Hitboxes extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General");
    private final ModeSetting mode = new ModeSetting(general, "Expand Mode", "Select the expand mode", "Blatant", "Blatant", "Legit");
    private final NumberSetting<Double> expandAmount = new NumberSetting<>(general, "Expand Amount", "how big", 0.1, 0.0, 1.0);

    int go = 150;
    @RegisterEvent
    private void eventEntityHitboxEventListener(EntityHitboxEvent event) {
        if (event.getEntity() == null ||
                mc.player == null ||
                event.getEntity().getId() == mc.player.getId()) return;

        Box expandedBox = event.getBox().expand(
                expandAmount.getValue(),
                0,
                expandAmount.getValue()
        );

        if (mode.getValue().equals("Blatant"))
            event.setBox(expandedBox);
    }
    @RegisterEvent
    private void SwingEventListener(SwingEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null || mc.currentScreen != null) return;
        if (event.getMode().equals(SwingEvent.Mode.PRE)) {
            if (raytrace() != null && mc.crosshairTarget instanceof BlockHitResult) {
                go = 0;
                event.setCancelled(true);
            }
        }
    }
    @RegisterEvent
    private void Render3DEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null || mc.currentScreen != null) return;
        if (event.getMode().equals(Render3DEvent.Mode.PRE)) {
            if (raytrace() != null) {
                if (go < 50) {
                    if (mc.crosshairTarget instanceof EntityHitResult entityHitResult) {
                        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                            if (livingEntity == raytrace()) {
                                go = 150;
                                InputUtils.simulateClick(0, 35);
                            }
                        }
                    }
                    rotationComponent.setMultiPoint(1);
                    rotationComponent.setSilentRotation(true);
                    rotationComponent.setHorizontalSpeed(100);
                    rotationComponent.setVerticalSpeed(100);
                    go++;
                    rotationComponent.queueRotation(raytrace(), RotationComponent.RotationPriority.HIGH, RotationComponent.AimType.BLATANT, RotationComponent.EntityPoints.STRAIGHT);
                }
            }
        }
    }
    private LivingEntity raytrace() {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d viewVector = mc.player.getRotationVecClient();
        Vec3d extendedPoint = cameraPos.add(viewVector.x * 3.0, viewVector.y * 3.0, viewVector.z * 3.0);

        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player) {
                if (entity.getBoundingBox().expand(expandAmount.getValue()).intersects(cameraPos, extendedPoint)) {
                    double distance = cameraPos.distanceTo(entity.getPos());

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestEntity = entity;
                    }
                }
            }
        }

        return (LivingEntity) closestEntity;
    }
    public Hitboxes() {
        getSettingRepository().registerSettings(general, mode, expandAmount);
    }
}