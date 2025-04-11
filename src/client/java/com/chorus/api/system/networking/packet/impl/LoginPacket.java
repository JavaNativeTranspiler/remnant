package com.chorus.api.system.networking.packet.impl;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.packet.Packet;

@IncludeReference
public class LoginPacket implements Packet {
    private final String username;
    private final String password;
    private final String hwid;

    private LoginPacket(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.hwid = builder.hwid;
    }

    @Override
    public String serialize() {
        return "LOGIN" + "user=" + username + ",pass=" + password + ",hwid=" + hwid;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String username;
        private String password;
        private String hwid;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder hwid(String hwid) {
            this.hwid = hwid;
            return this;
        }

        public LoginPacket build() {
            if (username == null || username.isEmpty()) {
                throw new IllegalStateException("Username must be set");
            }

            if (password == null || password.isEmpty()) {
                throw new IllegalStateException("Password must be set");
            }

            if (hwid == null || hwid.isEmpty()) {
                throw new IllegalStateException("HWID must be set");
            }

            return new LoginPacket(this);
        }
    }
}