package com.chorus.impl.events.render;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;

@Getter
@Setter
public class Render2DEvent extends Event {
    private final Mode mode;
    private final DrawContext context;

    public enum Mode { PRE, POST }

    public Render2DEvent(DrawContext context, Mode mode) {
        this.mode = mode;
        this.context = context;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
