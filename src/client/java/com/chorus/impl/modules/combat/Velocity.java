package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.network.PacketReceiveEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.hit.HitResult;

@ModuleInfo(
        name = "Velocity",
        description = "Reduces Taken Knock-Back",
        category = ModuleCategory.COMBAT
)
public class Velocity extends BaseModule implements QuickImports {
    private final ModeSetting mode = new ModeSetting("Mode", "Velocity mode", "Jump Reset", "Jump Reset", "Normal");
    private final RangeSetting<Double> horizontal = new RangeSetting<>("Horizontal Motion", "The amount of velocity to reduce horizontally", 0.0, 100.0, 50.0, 75.0);
    private final RangeSetting<Double> vertical = new RangeSetting<>("Vertical Motion", "The amount of velocity to reduce horizontally", 0.0, 100.0, 50.0, 75.0);
    private final BooleanSetting fullZero = new BooleanSetting("Full Zero", "Makes it so Knockback does not touch your movement at all", false);
    private final NumberSetting<Double> chance = new NumberSetting<>("Chance", "Probability of performing action", 25.0, 0.0, 100.0);
    private final NumberSetting<Float> range = new NumberSetting<>("Range", "Maximum distance to consider nearby players", 6.0f, 1.0f, 10.0f);
    private final MultiSetting conditions = new MultiSetting("Conditions", "Conditions before reducing knockback",
            "Sprinting",
            "Holding Weapon",
            "Going Forward",
            "Looking At enemy", "Only In Air");
    @RegisterEvent
    private void PacketReceiveEventListener(PacketReceiveEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket velocityPacket) {
            if (velocityPacket.getEntityId() == mc.player.getId() && shouldModify() && mode.getValue().equals("Normal")) {
                event.cancel();

                double motionX = velocityPacket.getVelocityX() / 8000.0;
                double motionY = velocityPacket.getVelocityY() / 8000.0;
                double motionZ = velocityPacket.getVelocityZ() / 8000.0;

                double xzVelocityFactor = horizontal.getRandomValue().floatValue() * 0.01;
                double yVelocityFactor = vertical.getRandomValue().floatValue() * 0.01;
                if (xzVelocityFactor != 0 || yVelocityFactor != 0) {
                    if (!fullZero.getValue()) {
                        mc.player.setVelocity(motionX * xzVelocityFactor, motionY * yVelocityFactor, motionZ * xzVelocityFactor);
                    }
                }
            }
        }
    }
    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null || mc.currentScreen instanceof HandledScreen) {
                return;
            }
            switch (mode.getValue()) {
                case "Jump Reset" -> {
                    if (!mc.player.isOnGround() || mc.options.jumpKey.isPressed() || mc.player.isTouchingWater() || !mc.player.isSprinting()) {
                        return;
                    }

                    boolean playerNearby = mc.world.getPlayers().stream()
                            .filter(player -> player != mc.player)
                            .anyMatch(player -> mc.player.distanceTo(player) <= range.getValue());

                    if (!playerNearby) {
                        return;
                    }

                    if (mc.player.hurtTime == 9 && MathUtils.randomInt(0, 100) <= chance.getValue()) {
                        InputUtils.simulateKeyPress(mc.options.jumpKey, 35);
                    }
                }
            }
        }
    }
    private boolean shouldModify() {
        if (mc.world == null || mc.player == null) return false;
        if (conditions.getSpecificValue("Sprinting") && !mc.player.isSprinting()) return false;
        if (conditions.getSpecificValue("Holding Weapon") && !(mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem)) return false;
        if (conditions.getSpecificValue("Going Forward") && !mc.options.forwardKey.isPressed()) return false;
        if (conditions.getSpecificValue("Looking At Enemy") && mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return false;
        if (conditions.getSpecificValue("Only In Air") && mc.player.isOnGround()) return false;
        return MathUtils.randomInt(0, 100) <= chance.getValue();
    }
    public Velocity() {
        horizontal.setRenderCondition(() -> mode.getValue().equals("Normal"));
        vertical.setRenderCondition(() -> mode.getValue().equals("Normal"));
        fullZero.setRenderCondition(() -> mode.getValue().equals("Normal"));
        getSettingRepository().registerSettings(mode, horizontal, vertical, fullZero, chance, range, conditions);
    }
}