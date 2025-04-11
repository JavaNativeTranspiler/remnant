package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Getter
@Setter
public class AttackEvent extends Event {
    private final Mode mode;
    public enum Mode { PRE, POST }
    Entity target;

    public AttackEvent(Mode mode, Entity target) {
        this.mode = mode;
        this.target = target;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
