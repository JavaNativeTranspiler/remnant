/**
 * Created: 2/27/2025
 */

package chorus0.asm.accessors;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatMessageC2SPacket.class)
public interface ChatMessageC2SPacketAccessor {
    @Mutable @Accessor("chatMessage")
    void setChatMessage(String chatMessage);
}