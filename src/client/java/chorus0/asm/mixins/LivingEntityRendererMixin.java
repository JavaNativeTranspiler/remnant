package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.core.listener.impl.TickEventListener;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> implements QuickImports {
    @ModifyExpressionValue(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F"
            ))
    private float setPitch(float original, LivingEntity entity, S state, float tickDelta) {
        if (mc.player != entity) {
            return original;
        }

        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        if (rotationManager.isRotating() && rotationManager.getCurrentRotation() != null) {
            return MathHelper.lerp(tickDelta, rotationManager.getPrevPitch(), rotationManager.getPitch());
        }
        return original;
    }
}