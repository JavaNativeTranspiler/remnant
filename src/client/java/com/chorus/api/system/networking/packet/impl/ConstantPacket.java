package com.chorus.api.system.networking.packet.impl;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.packet.Packet;

@IncludeReference
public class ConstantPacket implements Packet {
    private final String constantName;

    private ConstantPacket(Builder builder) {
        this.constantName = builder.constantName;
    }
    @Override
    public String serialize() {
        return "CONST" + constantName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String constantName;

        public Builder constantName(String constantName) {
            this.constantName = constantName;
            return this;
        }

        public ConstantPacket build() {
            return new ConstantPacket(this);
        }
    }
} 