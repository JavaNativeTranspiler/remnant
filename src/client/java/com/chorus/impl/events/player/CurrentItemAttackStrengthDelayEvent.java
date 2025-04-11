/**
 * Created: 2/16/2025
 */

package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentItemAttackStrengthDelayEvent extends Event {
    private double value = 1;
}