/**
 * Created: 2/9/2025
 */

package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemUseCooldownEvent extends Event {
    int speed;

    public ItemUseCooldownEvent(int speed) {
        this.speed = speed;
    }
}