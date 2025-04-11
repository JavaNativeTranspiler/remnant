/**
 * Created: 2/4/2025
 */

package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.core.listener.impl.TickEventListener;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin implements QuickImports {

    @ModifyExpressionValue(
            method = "raycast",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;"
            ))
    private static Vec3d silentRotationItemUse(Vec3d original, World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        TickEventListener rotationManager = (TickEventListener) Chorus.getInstance().getListenerRepository().getListeners().get(0);
        return rotationManager.isRotating() ? mc.player.getRotationVector(rotationManager.getPitch(), rotationManager.getYaw()) : player.getRotationVector();
    }
}