package com.chorus.api.system.networking.packet.impl;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.packet.Packet;

@IncludeReference
public class HeartbeatPacket implements Packet {
    private final int heartbeatNumber;

    private HeartbeatPacket(Builder builder) {
        this.heartbeatNumber = builder.heartbeatNumber;
    }

    @Override
    public String serialize() {
        return "HEARTBEAT" + heartbeatNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int heartbeatNumber;

        public Builder heartbeatNumber(int heartbeatNumber) {
            this.heartbeatNumber = heartbeatNumber;
            return this;
        }

        public HeartbeatPacket build() {
            return new HeartbeatPacket(this);
        }
    }
}