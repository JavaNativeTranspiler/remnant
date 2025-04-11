/**
 * Created: 2/3/2025
 */

package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.modules.visual.TargetHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Unique
    private boolean ignoreSendChatMsg = false;

    @Shadow
    public abstract void sendChatMessage(String content);

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String content, CallbackInfo ci) {
        if (ignoreSendChatMsg) return;
        if (Chorus.getInstance().getCommandManager().getPrefix() != null && content.startsWith(Chorus.getInstance().getCommandManager().getPrefix())) return;

        ignoreSendChatMsg = true;
        sendChatMessage(content);
        ignoreSendChatMsg = false;
        ci.cancel();
    }
    @Shadow private ClientWorld world;

    @Inject(method = "onEntityStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V"))
    public void updateCounter(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getEntity(MinecraftClient.getInstance().world) instanceof OtherClientPlayerEntity player) {
            if (SocialManager.getTarget() != null) {
                if (packet.getEntity(MinecraftClient.getInstance().world) == SocialManager.getTarget()) {
                    Chorus.getInstance().getModuleManager().getModule(TargetHud.class).totemPops += 1;
                }
            }
        }
    }
}