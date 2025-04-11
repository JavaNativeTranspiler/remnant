package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.network.PacketReceiveEvent;
import com.chorus.impl.events.network.PacketSendEvent;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements QuickImports {
    @Inject(
        method      = "handlePacket",
        at          = @At("HEAD") ,
        cancellable = true
    )
    private static <T extends PacketListener> void onPacketReceive(Packet<T> packet, PacketListener listener,
                                                                   CallbackInfo ci) {
        PacketReceiveEvent prePacketReceive = new PacketReceiveEvent(packet, PacketReceiveEvent.Mode.PRE);

        Chorus.getInstance().getEventManager().post(prePacketReceive);

        if (prePacketReceive.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
        method = "handlePacket",
        at     = @At("TAIL")
    )
    private static <T extends PacketListener> void onPacketReceivePost(Packet<T> packet, PacketListener listener,
                                                                       CallbackInfo ci) {
        PacketReceiveEvent postPacketReceive = new PacketReceiveEvent(packet, PacketReceiveEvent.Mode.POST);

        Chorus.getInstance().getEventManager().post(postPacketReceive);
    }

    @Inject(
        method      = "send(Lnet/minecraft/network/packet/Packet;)V",
        at          = @At("HEAD") ,
        cancellable = true
    )
    private void onPacketSend(Packet<?> packet, CallbackInfo ci) {
        PacketSendEvent prePacketSend = new PacketSendEvent(PacketSendEvent.Mode.PRE, packet);

        Chorus.getInstance().getEventManager().post(prePacketSend);

        if (prePacketSend.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
        method = "send(Lnet/minecraft/network/packet/Packet;)V",
        at     = @At("TAIL")
    )
    private void onPacketSendPost(Packet<?> packet, CallbackInfo ci) {
        PacketSendEvent postPacketSend = new PacketSendEvent(PacketSendEvent.Mode.POST, packet);

        Chorus.getInstance().getEventManager().post(postPacketSend);
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (packet instanceof ChatMessageC2SPacket && ((ChatMessageC2SPacket) packet).chatMessage().startsWith(Chorus.getInstance().getCommandManager().getPrefix())) {
            try {
                Chorus.getInstance().getCommandManager().dispatch(((ChatMessageC2SPacket) packet).chatMessage().substring(Chorus.getInstance().getCommandManager().getPrefix().length()));
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                mc.execute(()->{
                    mc.inGameHud.getChatHud().addMessage(Text.of(e.getMessage()));
                });
            }
            ci.cancel();
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
