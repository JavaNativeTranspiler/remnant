package com.chorus.api.system.rotation;

import lombok.Getter;

@Getter
public class RotationRequest implements Comparable<RotationRequest> {
    private final float[] rotation;
    private final RotationComponent.RotationPriority priority;
    private final RotationComponent.AimType aimType;

    public RotationRequest(float[] rotation, RotationComponent.RotationPriority priority, RotationComponent.AimType aimType) {
        this.rotation = rotation;
        this.priority = priority;
        this.aimType = aimType;
    }

    @Override
    public int compareTo(RotationRequest other) {
        return other.priority.getValue() - this.priority.getValue(); // priorities
    }
}
