/**
 * Created: 2/4/2025
 */

package chorus0.asm.mixins;

import chorus0.Chorus;
import chorus0.asm.accessors.WorldRendererAccessor;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.misc.EntityHitboxEvent;
import com.chorus.impl.events.player.MoveFixEvent;
import com.chorus.impl.modules.movement.NoPush;
import com.chorus.impl.modules.visual.Nametags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements QuickImports {

    @Shadow
    private Box boundingBox;

    @Shadow
    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return null;
    }

    @Inject(method = "shouldRenderName", at = @At("HEAD"), cancellable = true)
    private void shouldRenderNameInject(CallbackInfoReturnable<Boolean> cir) {
        if (Chorus.getInstance() == null || Chorus.getInstance().getModuleManager() == null) return;

        if (Chorus.getInstance().getModuleManager().getModule(Nametags.class).isEnabled()) {
            Entity entity = (Entity) (Object) this;

            if (!(entity instanceof LivingEntity)) return;
            if (entity instanceof PlayerEntity && entity == mc.player && mc.options.getPerspective().isFirstPerson())
                return;

            if (!((WorldRendererAccessor) mc.worldRenderer).getFrustum().isVisible(entity.getBoundingBox())) {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d updateVelocityInject(Vec3d movementInput, float speed, float yaw) {
        if (mc.player != null && Chorus.getInstance() != null && Chorus.getInstance().getEventManager() != null) {
            MoveFixEvent eventYawMoveFix = new MoveFixEvent(yaw);
            Chorus.getInstance().getEventManager().post(eventYawMoveFix);
            yaw = eventYawMoveFix.getYaw();
        }
        return movementInputToVelocity(movementInput, speed, yaw);
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    public void pushAwayFrom(Entity entity, CallbackInfo ci) {
        if (Chorus.getInstance() == null || Chorus.getInstance().getModuleManager() == null) return;

        if (Chorus.getInstance().getModuleManager().getModule(NoPush.class).isEnabled()) ci.cancel();
    }

    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getBoundBox(CallbackInfoReturnable<Box> cir) {
        if (Chorus.getInstance() == null || Chorus.getInstance().getEventManager() == null) return;

        EntityHitboxEvent eventHitBox = new EntityHitboxEvent((Entity) (Object) this, this.boundingBox).run();
        cir.setReturnValue(eventHitBox.getBox());
    }

    @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z"))
    private boolean fallDistanceFix(boolean original) {
        if ((Object) this == mc.player) {
            return false;
        }

        return original;
    }
}