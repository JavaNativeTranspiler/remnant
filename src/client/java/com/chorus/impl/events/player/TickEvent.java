package com.chorus.impl.events.player;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TickEvent extends Event {
    private final Mode mode;

    public enum Mode { PRE, POST }

    public TickEvent(Mode mode) {
        this.mode = mode;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
