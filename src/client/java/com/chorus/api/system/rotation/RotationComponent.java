package com.chorus.api.system.rotation;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.rotation.RotationUtils;
import com.chorus.core.listener.Listener;
import com.chorus.impl.events.player.SilentRotationEvent;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.events.render.Render2DEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.PriorityQueue;

public class RotationComponent implements QuickImports, Listener {
    @Setter
    private float multiPoint = 1;
    @Setter
    private float horizontalSpeed = 100;
    @Setter
    private float verticalSpeed = 100;

    @Setter
    private boolean silentRotation = true;
    private final PriorityQueue<RotationRequest> rotationQueue = new PriorityQueue<>();
    @Getter
    @Setter
    private float[] lastRotations;

    public RotationComponent() {
        this.lastRotations = new float[]{0, 0};
        Chorus.getInstance().getEventManager().register(this);
    }

    @Getter
    public enum RotationPriority {
        CRITICAL(5),
        HIGHEST(4),
        HIGH(3),
        MEDIUM(2),
        LOW(1),
        LOWEST(0);

        private final int value;

        RotationPriority(int value) {
            this.value = value;
        }
    }
    @Getter
    public enum EntityPoints {
        CLOSEST,
        STRAIGHT,
        RANDOM
    }
    @Getter
    public enum AimType {
        REGULAR,
        LINEAR,
        ADAPTIVE,
        BLATANT,
    }

    public void queueRotation(Vec3d position, Direction direction, RotationPriority priority, AimType aimType) {
        float[] targetRotations = RotationUtils.getRotationToBlock(BlockPos.ofFloored(position), direction);
        //ChatUtils.sendFormattedMessage("" + targetRotations[0]);
        addRotationRequest(new RotationRequest(targetRotations, priority, aimType));
    }

    public void queueRotation(Vec3d position, RotationPriority priority, AimType aimType) {
        float[] targetRotations = RotationUtils.calculate(position);
        addRotationRequest(new RotationRequest(targetRotations, priority, aimType));
    }

    public void queueRotation(LivingEntity entity, RotationPriority priority, AimType aimType, EntityPoints entityPoints) {
        Optional<Vec3d> position = RotationUtils.getPossiblePoints(entity, multiPoint, entityPoints);
        if (position.isEmpty()) return;
        float[] targetRotations = RotationUtils.calculate(
                mc.player.getCameraPosVec(mc.getRenderTickCounter().getTickDelta(false)),
                position.get()
        );
        addRotationRequest(new RotationRequest(targetRotations, priority, aimType));
    }

    public void queueRotation(float[] rotation, RotationPriority priority, AimType aimType) {
        addRotationRequest(new RotationRequest(RotationUtils.getFixedRotations(lastRotations, rotation), priority, aimType));
    }

    public void addRotationRequest(RotationRequest newRequest) {
        rotationQueue.removeIf(request -> request.getPriority().getValue() < newRequest.getPriority().getValue());
        rotationQueue.offer(newRequest);
    }


    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getMode().equals(TickEvent.Mode.POST)) return;
        RotationRequest highestPriorityRequest = rotationQueue.poll();
        if (highestPriorityRequest == null) return;

        rotationQueue.remove(highestPriorityRequest);
    }
    @RegisterEvent
    private void Render2DEventListener(Render2DEvent event) {
        RotationRequest highestPriorityRequest = rotationQueue.poll();
        if (highestPriorityRequest == null) {
            addRotationRequest(new RotationRequest(new float[]{mc.player.getYaw(), mc.player.getPitch()}, RotationPriority.LOWEST, AimType.BLATANT));
            return;
        }
        lastRotations = RotationUtils.getFixedRotations(lastRotations, getTargetRotations(lastRotations, highestPriorityRequest.getRotation(), horizontalSpeed, verticalSpeed, highestPriorityRequest.getAimType()));
        if (!silentRotation) {
            mc.player.setYaw(lastRotations[0]);
            mc.player.setPitch(lastRotations[1]);
        }
    }
    @RegisterEvent
    private void silentRotationEvent(SilentRotationEvent event) {
        event.setYaw(lastRotations[0]);
        event.setPitch(lastRotations[1]);
    }

    public void clearQueue() {
        rotationQueue.clear();
    }

    public float[] getTargetRotations(float[] lastRotations, float[] targetRotations, float yawSpeed, float pitchSpeed, AimType aimType) {
        if (mc.player == null || mc.world == null) return lastRotations;
        float yaw = silentRotation ? lastRotations[0] : mc.player.getYaw();
        float pitch = silentRotation ? lastRotations[1] : mc.player.getPitch();
        float delta = 1.0f / mc.getCurrentFps();

        switch (aimType) {
            case REGULAR:
                return new float[]{
                        MathUtils.lerpAngle(yaw, targetRotations[0], delta, yawSpeed * 0.5f),
                        MathUtils.lerpAngle(pitch, targetRotations[1], delta, pitchSpeed * 0.5f)
                };
            case LINEAR:
                return new float[]{
                        MathUtils.smoothLerpAngle(yaw, targetRotations[0], delta, yawSpeed * 0.5f),
                        MathUtils.smoothLerpAngle(pitch, targetRotations[1], delta, pitchSpeed * 0.5f)
                };
            case ADAPTIVE:
                float deltaYaw = Math.abs(yaw - targetRotations[0]);
                float deltaPitch = Math.abs(pitch - targetRotations[1]);
                return new float[]{
                        MathUtils.smoothLerpAngle(yaw, targetRotations[0], delta, MathUtils.clamp(deltaYaw, 5, 100) * (yawSpeed * 0.0075f)),
                        MathUtils.smoothLerpAngle(pitch, targetRotations[1], delta, MathUtils.clamp(deltaPitch, 5, 100) * (pitchSpeed * 0.025f))
                };
            case BLATANT:
                return new float[]{
                        targetRotations[0],
                        targetRotations[1]
                };
            default:
                return lastRotations;
        }
    }
}