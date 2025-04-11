package com.chorus.api.system.networking.packet.impl;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.packet.Packet;

@IncludeReference
public class JarSizePacket implements Packet {
    private final String version;

    private JarSizePacket(Builder builder) {
        this.version = builder.version;
    }
    @Override
    public String serialize() {
        return "JAR_SIZE" + version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String version;

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public JarSizePacket build() {
            if (version == null || version.isEmpty()) {
                throw new IllegalStateException("Version must be set");
            }
            return new JarSizePacket(this);
        }
    }
} 