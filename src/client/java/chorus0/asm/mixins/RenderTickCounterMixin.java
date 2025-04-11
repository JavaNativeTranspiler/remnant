/**
 * Created: 2/10/2025
 */

package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.impl.modules.other.Timer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public class RenderTickCounterMixin {

    @Shadow
    private float lastFrameDuration;

    @Inject(method = "beginRenderTick(J)I", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;lastFrameDuration:F", shift = At.Shift.AFTER))
    private void beginRenderTickInject(CallbackInfoReturnable<Integer> callback) {
        if (Chorus.getInstance() == null || Chorus.getInstance().getModuleManager() == null) return;
        
        if (!Chorus.getInstance().getModuleManager().getModule(Timer.class).isEnabled()) return;
        float timerSpeed = (float)(double)
                Chorus.getInstance().getModuleManager().getModule(Timer.class)
                        .getSettingRepository().getSetting("Speed").getValue();
        if (timerSpeed > 0) {
            lastFrameDuration *= timerSpeed;
        }
    }
}
