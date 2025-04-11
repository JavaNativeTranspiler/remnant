package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.impl.modules.visual.AntiDebuff;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void noExplosionParticle(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        if (Chorus.getInstance().getModuleManager().getModule(AntiDebuff.class).isEnabled() &&
                Chorus.getInstance().getModuleManager().getModule(AntiDebuff.class).mode.getSpecificValue("Nausea")) {
            if (parameters.getType() == ParticleTypes.EXPLOSION || parameters.getType() == ParticleTypes.EXPLOSION_EMITTER) {
                ci.cancel();
            }
        }
    }
}