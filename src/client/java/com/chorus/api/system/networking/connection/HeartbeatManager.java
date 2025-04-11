package com.chorus.api.system.networking.connection;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.packet.factory.PacketFactory;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@IncludeReference
public class HeartbeatManager {
    private static final long HEARTBEAT_INTERVAL = 5000;
    private static final int MAX_MISSED_HEARTBEATS = 3;
    
    private final ConnectionManager connectionManager;
    private final ScheduledExecutorService heartbeatExecutor;
    private final Random random = new SecureRandom();
    private volatile String lastHeartbeat;
    private int missedHeartbeats = 0;
    
    public HeartbeatManager(ConnectionManager connectionManager, ScheduledExecutorService heartbeatExecutor) {
        this.connectionManager = connectionManager;
        this.heartbeatExecutor = heartbeatExecutor;
    }
    
    public void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (!connectionManager.isConnected()) {
                MinecraftClient.getInstance().scheduleStop();
                return;
            }
            
            connectionManager.getSocketLock().lock();
            try {
                int heartbeatNumber = random.nextInt();
                connectionManager.sendPacket(PacketFactory.createHeartbeatPacket(heartbeatNumber).serialize());
                
                connectionManager.setTimeout(3000);
                
                try {
                    String response = connectionManager.readResponse();

                    if (lastHeartbeat != null && lastHeartbeat.equals(response)) {
                        MinecraftClient.getInstance().scheduleStop();
                    }
                    
                    if (response == null) {
                        missedHeartbeats++;
                        
                        if (missedHeartbeats >= MAX_MISSED_HEARTBEATS) {
                            MinecraftClient.getInstance().scheduleStop();
                        }
                    } else {
                        missedHeartbeats = 0;
                        lastHeartbeat = response;
                    }
                } catch (IOException e) {
                    missedHeartbeats++;
                    
                    if (missedHeartbeats >= MAX_MISSED_HEARTBEATS) {
                        MinecraftClient.getInstance().scheduleStop();
                    }
                } finally {
                    try {
                        connectionManager.setTimeout(0);
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                missedHeartbeats++;
                
                if (missedHeartbeats >= MAX_MISSED_HEARTBEATS) {
                    MinecraftClient.getInstance().scheduleStop();
                }
            } finally {
                connectionManager.getSocketLock().unlock();
            }
        }, 5000, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConnectionManager connectionManager;
        private ScheduledExecutorService heartbeatExecutor;

        public Builder connectionManager(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            return this;
        }

        public Builder heartbeatExecutor(ScheduledExecutorService heartbeatExecutor) {
            this.heartbeatExecutor = heartbeatExecutor;
            return this;
        }

        public HeartbeatManager build() {
            return new HeartbeatManager(connectionManager, heartbeatExecutor);
        }
    }
} 