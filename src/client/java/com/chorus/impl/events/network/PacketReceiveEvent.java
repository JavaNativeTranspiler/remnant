package com.chorus.impl.events.network;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;

@Getter
@Setter
public class PacketReceiveEvent extends Event {
    private final Packet<?> packet;
    private final Mode   mode;

    public enum Mode { PRE, POST }

    public PacketReceiveEvent(Packet<?> packet, Mode mode) {
        this.packet = packet;
        this.mode = mode;
    }
}