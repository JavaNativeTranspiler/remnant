/**
 * Created: 2/4/2025
 */

package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveFixEvent extends Event {
    float yaw;

    public MoveFixEvent(float yaw) {
        this.yaw = yaw;
    }
    public MoveFixEvent() {
        this.yaw = 0;
    }
}