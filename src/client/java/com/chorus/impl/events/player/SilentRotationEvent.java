/**
 * Created: 2/8/2025
 */

package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SilentRotationEvent extends Event {

    private final float initYaw;
    private final float initPitch;
    private float yaw, pitch;

    public SilentRotationEvent(float yaw, float pitch) {
        this.initYaw = yaw;
        this.initPitch = pitch;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public boolean hasBeenModified() {
        return initYaw != yaw || initPitch != pitch;
    }
}