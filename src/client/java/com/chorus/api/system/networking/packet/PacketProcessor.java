package com.chorus.api.system.networking.packet;

import cc.polymorphism.annot.IncludeReference;
import com.chorus.api.system.networking.connection.ConnectionManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@IncludeReference
public class PacketProcessor {
    private final BlockingQueue<String> packetQueue;
    private final ConnectionManager connectionManager;
    private final ExecutorService networkExecutor;

    private PacketProcessor(Builder builder) {
        this.packetQueue = builder.packetQueue;
        this.connectionManager = builder.connectionManager;
        this.networkExecutor = builder.networkExecutor;
    }

    public void start() {
        networkExecutor.submit(this::packetProcessingLoop);
    }

    private void packetProcessingLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String packet = packetQueue.poll(100, TimeUnit.MILLISECONDS);
                if (packet != null) {
                    connectionManager.sendPacket(packet);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void queuePacket(String packet) {
        packetQueue.offer(packet);
    }

    public void queuePacket(Packet packet) {
        packetQueue.offer(packet.serialize());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BlockingQueue<String> packetQueue = new LinkedBlockingQueue<>();
        private ConnectionManager connectionManager;
        private ExecutorService networkExecutor;

        public Builder packetQueue(BlockingQueue<String> packetQueue) {
            this.packetQueue = packetQueue;
            return this;
        }

        public Builder connectionManager(ConnectionManager connectionManager) {
            this.connectionManager = connectionManager;
            return this;
        }

        public Builder networkExecutor(ExecutorService networkExecutor) {
            this.networkExecutor = networkExecutor;
            return this;
        }

        public PacketProcessor build() {
            if (connectionManager == null) {
                throw new IllegalStateException("ConnectionManager must be set");
            }

            if (networkExecutor == null) {
                throw new IllegalStateException("NetworkExecutor must be set");
            }

            return new PacketProcessor(this);
        }
    }
}