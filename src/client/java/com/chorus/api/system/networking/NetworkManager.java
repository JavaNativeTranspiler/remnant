package com.chorus.api.system.networking;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.auth.AuthenticationService;
import com.chorus.api.system.networking.auth.UserData;
import com.chorus.api.system.networking.connection.ConnectionManager;
import com.chorus.api.system.networking.connection.HeartbeatManager;
import com.chorus.api.system.networking.packet.Packet;
import com.chorus.api.system.networking.packet.PacketProcessor;
import com.chorus.api.system.networking.service.JarSizeService;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.util.concurrent.*;

@IncludeReference
public class NetworkManager {
    @Getter
    private static NetworkManager instance = new NetworkManager();

    private final ConnectionManager connectionManager;
    private final HeartbeatManager heartbeatManager;
    private final PacketProcessor packetProcessor;
    private final AuthenticationService authService;
    private final JarSizeService jarSizeService;

    private final ScheduledExecutorService heartbeatExecutor;
    private final ExecutorService networkExecutor;

    private NetworkManager() {
        heartbeatExecutor = createScheduledExecutor(createHeartbeatThreadFactory());
        networkExecutor = createCachedExecutor(createNetworkThreadFactory());

        connectionManager = ConnectionManager.builder().build();

        heartbeatManager = HeartbeatManager.builder()
                .connectionManager(connectionManager)
                .heartbeatExecutor(heartbeatExecutor)
                .build();

        packetProcessor = PacketProcessor.builder()
                .connectionManager(connectionManager)
                .networkExecutor(networkExecutor)
                .build();

        authService = AuthenticationService.builder()
                .connectionManager(connectionManager)
                .networkExecutor(networkExecutor)
                .build();

        jarSizeService = JarSizeService.builder()
                .connectionManager(connectionManager)
                .networkExecutor(networkExecutor)
                .build();

        packetProcessor.start();
    }

    private ThreadFactory createHeartbeatThreadFactory() {
        return r -> {
            Thread t = new Thread(r, "Heartbeat-Thread");
            t.setDaemon(true);
            return t;
        };
    }

    private ThreadFactory createNetworkThreadFactory() {
        return r -> {
            Thread t = new Thread(r, "Network-Thread");
            t.setDaemon(true);
            return t;
        };
    }

    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> connectFuture = connectionManager.connect();
        connectFuture.thenRun(() -> heartbeatManager.startHeartbeat());
        return connectFuture;
    }

    public void disconnect() {
        if (connectionManager.isConnected()) {
            connectionManager.disconnect();

            MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().scheduleStop();
            });
        }
    }

    public CompletableFuture<UserData> login(String username, String password, String hwid) {
        return authService.login(username, password, hwid);
    }

    public CompletableFuture<Boolean> checkJarSize(String version) {
        return jarSizeService.checkJarSize(version);
    }

    public static void queuePacket(String packet) {
        getInstance().packetProcessor.queuePacket(packet);
    }

    public static void queuePacket(Packet packet) {
        getInstance().packetProcessor.queuePacket(packet);
    }

    public boolean isConnected() {
        return connectionManager.isConnected();
    }

    public String readResponse() throws IOException {
        String response = connectionManager.readResponse();
        if (response == null) {
            String lastLoginResponse = authService.getLastLoginResponse();
            if (lastLoginResponse != null) {
                return lastLoginResponse;
            }
        }
        return response;
    }

    public void sendPacket(String packet) {
        connectionManager.sendPacket(packet);
    }

    public void sendPacket(Packet packet) {
        connectionManager.sendPacket(packet.serialize());
    }

    public void shutdown() {
        disconnect();
        heartbeatExecutor.shutdownNow();
        networkExecutor.shutdownNow();
    }

    public UserData getCurrentUser() {
        return authService.getCurrentUser();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public NetworkManager build() {
            return getInstance();
        }
    }

    private ScheduledExecutorService createScheduledExecutor(ThreadFactory factory) {
        return Executors.newSingleThreadScheduledExecutor(factory);
    }

    private ExecutorService createCachedExecutor(ThreadFactory factory) {
        return Executors.newCachedThreadPool(factory);
    }
}