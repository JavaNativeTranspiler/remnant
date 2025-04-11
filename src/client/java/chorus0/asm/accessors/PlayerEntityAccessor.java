/**
 * Created: 12/31/2024
 */

package chorus0.asm.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {
    @Invoker("getDamageAgainst")
    float invokeGetDamageAgainst(Entity target, float amount, DamageSource source);
}