package com.chorus.api.system.networking.packet.factory;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.packet.Packet;
import com.chorus.api.system.networking.packet.impl.ConstantPacket;
import com.chorus.api.system.networking.packet.impl.HeartbeatPacket;
import com.chorus.api.system.networking.packet.impl.JarSizePacket;
import com.chorus.api.system.networking.packet.impl.LoginPacket;

@IncludeReference
public class PacketFactory {
    public static Packet createLoginPacket(String username, String password, String hwid) {
        return LoginPacket.builder()
                .username(username)
                .password(password)
                .hwid(hwid)
                .build();
    }

    public static Packet createHeartbeatPacket(int heartbeatNumber) {
        return HeartbeatPacket.builder()
                .heartbeatNumber(heartbeatNumber)
                .build();
    }

    public static Packet createJarSizePacket(String version) {
        return JarSizePacket.builder()
                .version(version)
                .build();
    }

    public static Packet createConstantPacket(String constantName) {
        return ConstantPacket.builder()
                .constantName(constantName)
                .build();
    }
}