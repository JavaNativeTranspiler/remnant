package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.impl.events.network.PacketReceiveEvent;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "Backtrack", description = "Fakes High Ping To Gain An Advantage", category = ModuleCategory.COMBAT)
public class Backtrack extends BaseModule implements QuickImports {

    private final SettingCategory backtrack = new SettingCategory("Backtrack");
    private final SettingCategory behavior = new SettingCategory("Behavior");

    private final ModeSetting delayType = new ModeSetting(backtrack, "Backtrack Type", "", "Dynamic", "Dynamic");
    private final RangeSetting<Float> range = new RangeSetting<>(backtrack, "Range", "Sets Serverside Range Limits", 0f, 10f, 1.5f, 6f);
    private final RangeSetting<Integer> latency = new RangeSetting<>(backtrack, "Latency", "Latency: Sets Latency Amounts, Dynamic: Sets Latency Limits", 0, 2500, 100, 150);

    private final BooleanSetting flushOnVelocity = new BooleanSetting(behavior, "Flush On Velocity", "", true);
    private final NumberSetting<Integer> flushDelay = new NumberSetting<>(behavior, "Delay On Velocity", "Delay To Backtrack After Taking Velocity", 50, 0 ,500);

    private final ConcurrentLinkedQueue<PacketData> packetQueue = new ConcurrentLinkedQueue<>();
    private TrackedPosition position;
    private final TimerUtils waitTimer = new TimerUtils();
    private Vec3d currentPosition = new Vec3d(0.0D, 0.0D, 0.0D);
    private Vec3d oldPlayerPosition = new Vec3d(0.0D, 0.0D, 0.0D);

    private Entity target;
    private float currentDelay;

    public Backtrack() {
        getSettingRepository().registerSettings(backtrack, behavior, delayType, range, latency, flushOnVelocity, flushDelay);
    }

    @RegisterEvent
    private void packetReceiveListener(PacketReceiveEvent event) {
        if (event.getMode() != PacketReceiveEvent.Mode.PRE || mc.world == null || mc.player == null) return;
        if (!waitTimer.hasReached(flushDelay.getValue()))  {
            //resetModule(true);
            //return;
        }
        setSuffix((latency.getValueMin() + "-" + latency.getValueMax() + "ms"));
        if (mc.player.age < 10) {
            resetModule(true);
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof PlayerPositionLookS2CPacket || packet instanceof DisconnectS2CPacket) {
            resetModule(true);
            waitTimer.reset();
            return;
        }
        if (flushOnVelocity.getValue()) {
            if (packet instanceof EntityVelocityUpdateS2CPacket velocityUpdate && velocityUpdate.getEntityId() == mc.player.getId()
                    || packet instanceof ExplosionS2CPacket explosionPacket && explosionPacket.playerKnockback().get().x != 0 && explosionPacket.playerKnockback().get().y != 0 && explosionPacket.playerKnockback().get().z != 0) {
                resetModule(true);
                waitTimer.reset();
                return;
            }
        }
        if (packet instanceof ChatMessageC2SPacket || packet instanceof GameMessageS2CPacket || packet instanceof CommandExecutionC2SPacket || packet instanceof PlaySoundS2CPacket || packet instanceof HealthUpdateS2CPacket) {
            return;
        }
        if (target != null && target.isAlive()) {
            boolean isEntityPosition = packet instanceof EntityPositionS2CPacket && ((EntityPositionS2CPacket) packet).entityId() == target.getId();
            boolean isEntityPositionSync = packet instanceof EntityPositionSyncS2CPacket && ((EntityPositionSyncS2CPacket) packet).id() == target.getId();
            boolean isEntityS2C = packet instanceof EntityS2CPacket && ((EntityS2CPacket) packet).getEntity(mc.world) == target;
            boolean isRelevant = isEntityS2C || isEntityPosition || isEntityPositionSync;
            if (isRelevant) {
                Vec3d newPos = new Vec3d(0, 0, 0);
                if (packet instanceof EntityS2CPacket) {
                    newPos = position.withDelta(((EntityS2CPacket) packet).getDeltaX(), ((EntityS2CPacket) packet).getDeltaY(), ((EntityS2CPacket) packet).getDeltaZ());
                } else if (packet instanceof EntityPositionS2CPacket) {
                    Vec3d pos = ((EntityPositionS2CPacket) packet).change().position();
                    newPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                } else if (packet instanceof EntityPositionSyncS2CPacket syncS2CPacket) {
                    newPos = syncS2CPacket.values().position();
                }
                if (Objects.equals(newPos, new Vec3d(0, 0, 0))) return;
                position.setPos(newPos);
                currentPosition = newPos;
                if (mc.player.distanceTo(target) > newPos.distanceTo(mc.player.getPos())) {
                    processPacketQueue(true);
                    return;
                }
            }
        }

        event.cancel();
        packetQueue.add(new PacketData(packet, System.currentTimeMillis()));
    }

    @RegisterEvent
    private void tickListener(TickEvent event) {
        if (event.getMode() != TickEvent.Mode.PRE) return;

        if (mc.world == null || mc.player == null) {
            resetModule(false);
            return;
        }

        if (target != null && target.isAlive() && target != null && isEntityInRange(target)) {
            processPacketQueue(false);
        } else {
            resetModule(true);
        }
    }

    @RegisterEvent
    private void attackListener(AttackEvent event) {
        if (event.getMode() != AttackEvent.Mode.PRE) return;

        Entity enemy = event.getTarget();
        if (!(enemy instanceof PlayerEntity) || !isEntityInRange(enemy)) return;

        if (enemy != target) {
            oldPlayerPosition = new Vec3d(0.0D, 0.0D, 0.0D);
            currentPosition = new Vec3d(0.0D, 0.0D, 0.0D);
            resetModule(true);
            position = new TrackedPosition();
            position.setPos(enemy.getTrackedPosition().getPos());
        }
        target = enemy;
    }
    
    @RegisterEvent
    private void Render3DEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null || currentPosition.y == 0.0d) return;
        double lerpSpeed = Math.min(oldPlayerPosition.distanceTo(currentPosition) * 0.1, event.getTickDelta());

        lerpSpeed = Math.min(lerpSpeed, oldPlayerPosition.distanceTo(currentPosition) * 0.01);
        oldPlayerPosition = oldPlayerPosition.lerp(currentPosition, lerpSpeed);

        if (target == null) return;
        Render3DEngine.renderOutlinedShadedBox(oldPlayerPosition, new Color(255, 255, 255), 50, event.getMatrices(), target.getWidth() / 2, target.getHeight());
    }
    @Override
    protected void onModuleEnabled() {
        resetModule(false);
        currentDelay = 100;
    }

    @Override
    protected void onModuleDisabled() {
        resetModule(true);
        oldPlayerPosition = new Vec3d(0.0D, 0.0D, 0.0D);
        currentPosition = new Vec3d(0.0D, 0.0D, 0.0D);
    }

    private void processPacketQueue(boolean force) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
        packetQueue.removeIf(data -> {
            switch (delayType.getValue()) {
                case "Static" -> currentDelay = latency.getRandomValue().floatValue();
                case "Dynamic" -> {
                    if (target != null) {
                        currentDelay = MathHelper.clamp(mc.player.distanceTo(target) * 200, latency.getValueMin(), latency.getValueMax());
                    }
                }
            }
            if (force || System.currentTimeMillis() - data.timestamp >= currentDelay) {
                mc.execute(() -> {
                    try {
                        if (mc.getNetworkHandler() != null) {
                            ((Packet<ClientPlayNetworkHandler>) data.packet).apply(mc.getNetworkHandler());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                return true;
            }
            return false;
        });
    }

    private void resetModule(boolean handlePackets) {
        if (handlePackets) {
            processPacketQueue(true);
        } else {
            packetQueue.clear();
        }

        target = null;
        position = null;
    }

    private boolean isEntityInRange(Entity entity) {
        if (mc.player == null || mc.world == null) return false;
        double distance = mc.player.distanceTo(entity);
        return distance >= range.getValueMin() && distance <= range.getValueMax();
    }

    private static class PacketData {
        final Packet<?> packet;
        final long timestamp;

        PacketData(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }
}