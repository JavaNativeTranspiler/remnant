package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.core.listener.impl.TickEventListener;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin implements QuickImports {

    @Shadow
    private LivingEntity shooter;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d fireworkMoveFix(LivingEntity instance) {


        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        return rotationManager.isRotating() ? mc.player.getRotationVector(rotationManager.getPitch(), rotationManager.getYaw()) : instance.getRotationVector();
    }

}


//~ Formatted by Jindent --- http://www.jindent.com
