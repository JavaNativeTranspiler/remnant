package com.chorus.impl.modules.movement;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.network.PacketSendEvent;
import net.minecraft.network.packet.Packet;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "Blink", description = "Distances SZ From Enemies", category = ModuleCategory.MOVEMENT)
public class Blink extends BaseModule implements QuickImports {

    public final ConcurrentLinkedQueue<DelayedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    @RegisterEvent
    private void PacketSendEventListener(PacketSendEvent event) {
        if (event.getMode() == PacketSendEvent.Mode.PRE) {
            event.setCancelled(true);
            event.cancel();
            packetQueue.add(new DelayedPacket(event.getPacket(), System.currentTimeMillis()));
            long delayMillis = 5000;

            while (!packetQueue.isEmpty()) {
                DelayedPacket delayedPacket = packetQueue.peek();

                if (delayedPacket != null && (System.currentTimeMillis() - delayedPacket.receiveTime) >= delayMillis) {
                    packetQueue.poll();
                    sendPacket(delayedPacket.packet);
                } else {
                    break;
                }
            }
        }
    }

    @Override
    protected void onModuleDisabled() {
        while (!packetQueue.isEmpty()) {
            DelayedPacket delayedPacket = packetQueue.peek();
            packetQueue.poll();
            sendPacket(delayedPacket.packet);
        }
    }


    private static class DelayedPacket {
        final Packet<?> packet;
        final long receiveTime;

        DelayedPacket(Packet<?> packet, long receiveTime) {
            this.packet = packet;
            this.receiveTime = receiveTime;
        }
    }

    private void sendPacket(Packet<?> packet) {
        Objects.requireNonNull(mc.getNetworkHandler()).getConnection().send(packet, null);
    }
}