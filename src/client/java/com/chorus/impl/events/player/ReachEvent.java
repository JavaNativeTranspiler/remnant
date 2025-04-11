/**
 * Created: 3/16/2025
 */

package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReachEvent extends Event {
    public enum Type {
        BLOCK,
        ENTITY
    }

    private final Type type;
    private double distance;

    public ReachEvent(Type type, double originalDistance) {
        this.type = type;
        this.distance = originalDistance;
    }
}