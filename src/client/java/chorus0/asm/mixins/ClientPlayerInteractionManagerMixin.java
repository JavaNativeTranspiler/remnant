
/**
 * Created: 12/7/2024
 */
package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.modules.other.NoDelay;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @ModifyConstant(
        method   = "updateBlockBreakingProgress",
        constant = @Constant(intValue = 5)
    )
    private int noBreakDelay(int value) {
        return Chorus.getInstance().getModuleManager().getModule(NoDelay.class).miningDelay.getValue();
    }

    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", shift = At.Shift.AFTER))
    private void onPreAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        Chorus.getInstance().getEventManager().post(new AttackEvent(AttackEvent.Mode.PRE, target));
    }

    @Inject(method = "attackEntity", at = @At("RETURN"))
    private void onPostAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        Chorus.getInstance().getEventManager().post(new AttackEvent(AttackEvent.Mode.POST, target));
    }
}