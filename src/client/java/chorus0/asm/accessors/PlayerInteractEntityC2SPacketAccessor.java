package chorus0.asm.accessors;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerInteractEntityC2SPacket.class)
public interface PlayerInteractEntityC2SPacketAccessor {
    @Accessor(value="type")
    PlayerInteractEntityC2SPacket.InteractTypeHandler getType();

    @Accessor(value="entityId")
    int getEntityId();
}
