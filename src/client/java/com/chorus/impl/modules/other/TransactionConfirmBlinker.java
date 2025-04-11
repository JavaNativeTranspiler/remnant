package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.network.PacketSendEvent;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "TransactionConfirmBlinker", description = "Elite Client paste detected", category = ModuleCategory.OTHER)
public class TransactionConfirmBlinker extends BaseModule implements QuickImports {
    
    private final ConcurrentLinkedQueue<DelayedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    @RegisterEvent
    private void packetEventListener(PacketSendEvent event) {
        if (event.getMode() == PacketSendEvent.Mode.PRE) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround || 
                event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround || 
                event.getPacket() instanceof PlayerMoveC2SPacket.Full || 
                event.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly || 
                event.getPacket() instanceof ClientCommandC2SPacket || 
                event.getPacket() instanceof PlayerActionC2SPacket || 
                event.getPacket() instanceof PlayerInteractItemC2SPacket || 
                event.getPacket() instanceof CommonPongC2SPacket) {
                
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
