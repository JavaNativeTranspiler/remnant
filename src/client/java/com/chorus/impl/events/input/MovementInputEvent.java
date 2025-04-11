/**
 * Created: 2/15/2025
 */

package com.chorus.impl.events.input;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovementInputEvent extends Event {
    boolean pressingForward;
    boolean pressingBack;
    boolean pressingLeft;
    boolean pressingRight;

    boolean jumping;
    boolean sneaking;
    boolean sprinting;

    float movementForward;
    float movementSideways;

    public MovementInputEvent(boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking, boolean sprinting, float movementForward, float movementSideways) {
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
        this.sprinting = sprinting;

        this.movementForward = movementForward;
        this.movementSideways = movementSideways;
    }
}
