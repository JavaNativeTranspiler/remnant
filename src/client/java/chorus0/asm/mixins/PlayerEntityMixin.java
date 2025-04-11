package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.core.listener.impl.TickEventListener;
import com.chorus.impl.events.player.CurrentItemAttackStrengthDelayEvent;
import com.chorus.impl.events.player.ReachEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements QuickImports {

    @ModifyExpressionValue(method = "tickNewAi", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float tickNewAiInject(float original) {
        if ((Object) this != mc.player) {
            return original;
        }

        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        float[] currentRotation = rotationManager.getCurrentRotation();

        if (rotationManager.isRotating()) {
            rotationManager.setPrevPitch(rotationManager.getPitch());
            rotationManager.setPitch(currentRotation[1]);

            return currentRotation[0];
        }

        return original;
    }


    @Inject(method = "getAttackCooldownProgressPerTick", at = @At("HEAD"), cancellable = true)
    public void getAttackCooldownProgress(CallbackInfoReturnable<Float> ci) {
        CurrentItemAttackStrengthDelayEvent eventCurrentItemAttackStrengthDelay = new CurrentItemAttackStrengthDelayEvent().run();
        if (eventCurrentItemAttackStrengthDelay.getValue() != -1)
            ci.setReturnValue((float) (eventCurrentItemAttackStrengthDelay.getValue() / ((PlayerEntity) (Object) this).getAttributeInstance(EntityAttributes.ATTACK_SPEED).getValue() * 20.0D));
    }

    @ModifyReturnValue(method = "getBlockInteractionRange", at = @At("RETURN"))
    private double modifyBlockInteractionRange(double original) {
        ReachEvent event = new ReachEvent(ReachEvent.Type.BLOCK, original);
        Chorus.getInstance().getEventManager().post(event);
        return Math.max(0, event.getDistance());
    }

    @ModifyReturnValue(method = "getEntityInteractionRange", at = @At("RETURN"))
    private double modifyEntityInteractionRange(double original) {
        ReachEvent event = new ReachEvent(ReachEvent.Type.ENTITY, original);
        Chorus.getInstance().getEventManager().post(event);
        return Math.max(0, event.getDistance());
    }

}