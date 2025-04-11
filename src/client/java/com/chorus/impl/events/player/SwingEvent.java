package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Getter
@Setter
public class SwingEvent extends Event {
    private final Mode mode;
    public enum Mode { PRE, POST }
    Entity target;

    public SwingEvent(Mode mode) {
        this.mode = mode;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
