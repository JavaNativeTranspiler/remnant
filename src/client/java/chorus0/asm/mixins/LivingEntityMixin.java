
/**
 * Created: 12/7/2024
 */
package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.core.listener.impl.TickEventListener;
import com.chorus.impl.events.player.MoveFixEvent;
import com.chorus.impl.events.player.TickAIEvent;
import com.chorus.impl.modules.other.NoDelay;
import com.chorus.impl.modules.visual.AntiDebuff;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements QuickImports {
    @Shadow
    public int jumpingCooldown;
    @ModifyConstant(
            method = "getBlockingItem",
            constant = @Constant(intValue = 5)
    )
    private int noShieldDelay(int originalValue) {
        return Chorus.getInstance().getModuleManager().getModule(NoDelay.class).shieldDelay.getValue();
    }

    @Inject(method = "tickNewAi", at = @At("HEAD"))
    private void hookAITIck(CallbackInfo callbackInfo) {
        Chorus.getInstance().getEventManager().post(new TickAIEvent());
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F", ordinal = 1)))
    private float tickInject(float original) {
        if ((Object) this != mc.player) {
            return original;
        }

        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        return rotationManager.isRotating() ? rotationManager.getCurrentRotation()[0] : original;
    }

    @ModifyExpressionValue(method = "turnHead", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float turnHeadInject(float original) {
        if ((Object) this != mc.player) {
            return original;
        }

        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        return rotationManager.isRotating() ? rotationManager.getCurrentRotation()[0] : original;
    }
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void jumpDelay(CallbackInfo callbackInfo) {
        if (Chorus.getInstance().getModuleManager().getModule(NoDelay.class).jumpDelay.getValue())
            jumpingCooldown = 0;
    }
    @Redirect(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F", ordinal = 0))
    private float hookJump(LivingEntity entity) {
        if (mc.player != null) {
            MoveFixEvent eventJump = new MoveFixEvent(entity.getYaw());
            Chorus.getInstance().getEventManager().post(eventJump);
            return eventJump.getYaw();
        }
        return entity.getYaw();
    }

    @ModifyExpressionValue(method = "calcGlidingVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"))
    private float setElytraPitch(float original) {
        if ((Object) this != mc.player) {
            return original;
        }
        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        return rotationManager.isRotating() ? rotationManager.getCurrentRotation()[1] : original;
    }

    @ModifyExpressionValue(method = "calcGlidingVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d setElytraDirection(Vec3d original) {
        if ((Object) this != mc.player) {
            return original;
        }
        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        return rotationManager.isRotating() ? mc.player.getRotationVector(rotationManager.getPitch(), rotationManager.getYaw()) : original;
    }

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void antiDebuffNausea(RegistryEntry<StatusEffect> potionEffect, CallbackInfoReturnable<Boolean> cir) {
        if (Chorus.getInstance().getModuleManager().getModule(AntiDebuff.class).isEnabled()) {
            if (potionEffect == StatusEffects.NAUSEA && Chorus.getInstance().getModuleManager().getModule(AntiDebuff.class).mode.getSpecificValue("Nausea")) {
                cir.cancel();
                cir.setReturnValue(false);
            }
            if (potionEffect == StatusEffects.BLINDNESS && Chorus.getInstance().getModuleManager().getModule(AntiDebuff.class).mode.getSpecificValue("Blindness")) {
                cir.cancel();
                cir.setReturnValue(false);
            }
            if (potionEffect == StatusEffects.DARKNESS && Chorus.getInstance().getModuleManager().getModule(AntiDebuff.class).mode.getSpecificValue("Darkness")) {
                cir.cancel();
                cir.setReturnValue(false);
            }
        }
    }
}