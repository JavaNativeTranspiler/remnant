package com.chorus.api.system.networking.service;

import com.chorus.api.system.networking.connection.ConnectionManager;
import com.chorus.api.system.networking.packet.factory.PacketFactory;
import com.chorus.api.system.networking.response.factory.ResponseHandlerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class JarSizeService {
    private final ConnectionManager connectionManager;
    private final ExecutorService networkExecutor;
    
    public JarSizeService(ConnectionManager connectionManager, ExecutorService networkExecutor) {
        this.connectionManager = connectionManager;
        this.networkExecutor = networkExecutor;
    }
    
    public CompletableFuture<Boolean> checkJarSize(String version) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connectionManager.sendPacket(PacketFactory.createJarSizePacket(version).serialize());
                String response = connectionManager.readResponse();
                
                return ResponseHandlerFactory.getJarSizeResponseHandler().handle(response);
            } catch (IOException e) {
                return false;
            }
        }, networkExecutor);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConnectionManager connectionManager;
        private ExecutorService networkExecutor;

        public Builder connectionManager(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            return this;
        }

        public Builder networkExecutor(ExecutorService networkExecutor) {
            this.networkExecutor = networkExecutor;
            return this;
        }

        public JarSizeService build() {
            return new JarSizeService(connectionManager, networkExecutor);
        }
    }
} 