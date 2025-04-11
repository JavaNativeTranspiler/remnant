package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.network.PacketReceiveEvent;
import com.chorus.impl.events.network.PacketSendEvent;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "LagRange", description = "Uses Lag To Stay A Set Distance Away From Enemies", category = ModuleCategory.COMBAT)
public class LagRange extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General Settings");
    private final SettingCategory visual = new SettingCategory("Visual Settings");
    private final SettingCategory debug = new SettingCategory("Debug Settings");
    private final NumberSetting<Integer> fov = new NumberSetting<>(debug, "Fov", "Fov", 140, 0, 360);
    private final NumberSetting<Integer> delay = new NumberSetting<>(general, "Delay", "Delay", 250, 0, 2500);
    private final NumberSetting<Double> distance = new NumberSetting<>(general, "Distance", "Distance", 3.0, 0.0, 6.0);
    private final NumberSetting<Double> leniency = new NumberSetting<>(general, "Extra Leniency", "Leniency", 0.1, 0.0, 1.0);
    private final BooleanSetting realPos = new BooleanSetting(visual, "Real Position", "Real Position", false);
    private final BooleanSetting whileLagging = new BooleanSetting(visual, "Only While Lagging", "Only While Lagging", false);

    public final ConcurrentLinkedQueue<DelayedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final TimerUtils waitTimer = new TimerUtils();
    private Vec3d currentPosition = new Vec3d(0.0D, 0.0D, 0.0D);
    private Vec3d oldPlayerPosition = new Vec3d(0.0D, 0.0D, 0.0D);
    @Override
    protected void onModuleDisabled() {
        handlePackets(false, false);
    }
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            setSuffix(delay.getValue() + "ms");
            if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

            PlayerEntity target = (PlayerEntity) getClosestPlayerEntityWithinRange(distance.getValue().floatValue() + 2f);
            if (target == null) {
                handlePackets(false, false);
                waitTimer.reset();
                return;
            }
            if (!waitTimer.hasReached(250)) {
                handlePackets(false, false);
                return;
            }
            //
            if (mc.player.distanceTo(target) > distance.getValue()) {
                handlePackets(false, true);
            } else {
                handlePackets(true, false);
            }

            if (currentPosition.distanceTo(target.getPos()) + leniency.getValue() < mc.player.distanceTo(target)) {
                handlePackets(false, false);
            }
            //
        }
    }
    @RegisterEvent
    private void Render3DEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null || currentPosition.getY() == 0.0d) return;
        if (!realPos.getValue()) return;
        var color = packetQueue.isEmpty() ? new Color(255, 0, 0) : new Color(255, 255, 255);

        oldPlayerPosition = oldPlayerPosition.lerp(currentPosition, 0.1);
        if (whileLagging.getValue() && color.equals(new Color(255, 0, 0))) return;
        PlayerEntity target = (PlayerEntity) getClosestPlayerEntityWithinRange(distance.getValue().floatValue() + 2f);
        if (target == null) return;
        Render3DEngine.renderOutlinedShadedBox(oldPlayerPosition, color, 50, event.getMatrices(), mc.player.getWidth() / 2, mc.player.getHeight());

    }
    @RegisterEvent
    private void packetReceiveEventEventListener(PacketReceiveEvent event) {
        if (mc.player == null || mc.world == null) return;

        var packet = event.getPacket();

        if (packet instanceof PlayerPositionLookS2CPacket
                || packet instanceof HealthUpdateS2CPacket healthPacket && healthPacket.getHealth() != mc.player.getHealth()
                || packet instanceof EntityVelocityUpdateS2CPacket velocityPacket && velocityPacket.getEntityId() == mc.player.getId()
                || packet instanceof ExplosionS2CPacket explosionPacket && explosionPacket.playerKnockback().get().x != 0 && explosionPacket.playerKnockback().get().y != 0 && explosionPacket.playerKnockback().get().z != 0) {
            handlePackets(false, false);
            waitTimer.reset();
        }
    }

    @RegisterEvent
    private void sendEventEventListener(PacketSendEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (mc.player.age < 10) {
            handlePackets(false, false);
            return;
        }
        var packet = event.getPacket();

        PlayerEntity target = (PlayerEntity) getClosestPlayerEntityWithinRange(distance.getValue().floatValue() + 2f);
        if (target == null) {
            handlePackets(false, false);
            waitTimer.reset();
            return;
        }
        if (!waitTimer.hasReached(250)) {
            handlePackets(false, false);
            return;
        }
        if (packet instanceof PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket) {
            handlePackets(false, false);
            waitTimer.reset();
            return;
        }
        packetQueue.offer(new DelayedPacket(event.getPacket(), System.currentTimeMillis()));
        event.setCancelled(true);

    }


    private void handlePackets(Boolean useDelay, Boolean findCloserPosition) {
        if (mc.player == null || mc.world == null || packetQueue.isEmpty()) return;
        PlayerEntity target = (PlayerEntity) getClosestPlayerEntityWithinRange(distance.getValue().floatValue() + 2f);

        List<DelayedPacket> toRemove = new ArrayList<>();
        for (DelayedPacket packet : packetQueue) {
            boolean sincePacket = System.currentTimeMillis() - packet.receiveTime >= (useDelay ? delay.getValue() : -1000);
            if (sincePacket) {
                mc.getNetworkHandler().getConnection().send(packet.packet, null);
                toRemove.add(packet);
                if (packet.packet instanceof PlayerMoveC2SPacket packets) {
                    var pos = mc.player.getPos();
                    if (useDelay) {
                        currentPosition = new Vec3d(((PlayerMoveC2SPacket) packet.packet).getX(pos.x), ((PlayerMoveC2SPacket) packet.packet).getY(pos.y), ((PlayerMoveC2SPacket) packet.packet).getZ(pos.z));
                    } else {
                        currentPosition = mc.player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false));
                    }
                    if (target != null && findCloserPosition && !useDelay)
                        if (target.squaredDistanceTo(packets.getX(pos.x), packets.getY(pos.y), packets.getZ(pos.z)) <= Math.pow(distance.getValue(), 2)) {
                            break;
                        }
                }
            }
        }
        packetQueue.removeAll(toRemove);
    }

    public Entity getClosestPlayerEntityWithinRange(float range) {
        return mc.world.getPlayers()
                .stream()
                .filter(player -> player != mc.player
                        && mc.player.distanceTo(player) <= range
                        && SocialManager.isEnemy(player)
                        && Math.toDegrees(
                                MathUtils.angleBetween(mc.player.getRotationVector(), player.getPos().add(0, player.getEyeHeight(player.getPose()), 0)
                                        .subtract(mc.player.getEyePos()))) <= fov.getValue() / 2f)
                .min(Comparator.comparingDouble(mc.player::distanceTo))
                .orElse(null);
    }

    private static class DelayedPacket {
        final Packet<?> packet;
        final long receiveTime;

        DelayedPacket(Packet<?> packet, long receiveTime) {
            this.packet = packet;
            this.receiveTime = receiveTime;
        }
    }
    public LagRange() {
        getSettingRepository().registerSettings(general, visual, debug, fov, delay, distance, leniency, realPos, whileLagging);
    }
}