/**
 * Created: 2/16/2025
 */

package com.chorus.impl.events.misc;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

@Getter
@Setter
public class EntityHitboxEvent extends Event {
    private final Entity entity;
    private Box box;

    public EntityHitboxEvent(Entity entity, Box box) {
        this.entity = entity;
        this.box = box;
    }
}