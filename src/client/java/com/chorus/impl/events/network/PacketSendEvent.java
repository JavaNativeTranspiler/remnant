package com.chorus.impl.events.network;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;

@Getter
@Setter
public class PacketSendEvent extends Event {
    private final Packet packet;
    private final Mode mode;

    public enum Mode { PRE, POST }

    public PacketSendEvent(Mode mode, Packet packet) {
        this.mode = mode;
        this.packet = packet;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
