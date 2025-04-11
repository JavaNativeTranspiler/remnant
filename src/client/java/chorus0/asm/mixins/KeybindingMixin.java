/**
 * Created: 2/8/2025
 */

package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.impl.modules.movement.SnapTap;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeybindingMixin {
    @Final
    @Shadow
    private InputUtil.Key defaultKey;

    @Shadow
    private boolean pressed;

    @Unique
    private static final int[] KEY_CODES = {
            InputUtil.GLFW_KEY_A, InputUtil.GLFW_KEY_D, InputUtil.GLFW_KEY_W, InputUtil.GLFW_KEY_S
    };

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void onGetPressed(CallbackInfoReturnable<Boolean> cir) {
        if (Chorus.getInstance() != null && Chorus.getInstance().getModuleManager() != null && Chorus.getInstance().getModuleManager().isModuleEnabled(SnapTap.class) && this.pressed) {
            int keyCode = this.defaultKey.getCode();
            for (int i = 0; i < KEY_CODES.length; i++) {
                if (keyCode == KEY_CODES[i]) {
                    long currentTime = getCurrentTime(i);
                    long oppositeTime = getOppositeTime(i);

                    if (oppositeTime == 0) {
                        cir.setReturnValue(true);
                    } else {
                        cir.setReturnValue(oppositeTime <= currentTime);
                    }
                    cir.cancel();
                    return;
                }
            }
        }
    }

    @Inject(method = "setPressed", at = @At("HEAD"))
    public void setPressed(boolean pressed, CallbackInfo ci) {
        if (Chorus.getInstance() != null && Chorus.getInstance().getModuleManager() != null && Chorus.getInstance().getModuleManager().isModuleEnabled(SnapTap.class)) {
            int keyCode = this.defaultKey.getCode();
            for (int i = 0; i < KEY_CODES.length; i++) {
                if (keyCode == KEY_CODES[i]) {
                    setCurrentTime(i, pressed ? System.currentTimeMillis() : 0);
                    return;
                }
            }
        }
    }

    @Unique
    private long getCurrentTime(int index) {
        switch (index) {
            case 0: return SnapTap.LEFT_STRAFE_LAST_PRESS_TIME;
            case 1: return SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME;
            case 2: return SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME;
            case 3: return SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME;
            default: return 0;
        }
    }

    @Unique
    private long getOppositeTime(int index) {
        switch (index) {
            case 0: return SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME;
            case 1: return SnapTap.LEFT_STRAFE_LAST_PRESS_TIME;
            case 2: return SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME;
            case 3: return SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME;
            default: return 0;
        }
    }

    @Unique
    private void setCurrentTime(int index, long time) {
        switch (index) {
            case 0: SnapTap.LEFT_STRAFE_LAST_PRESS_TIME = time; break;
            case 1: SnapTap.RIGHT_STRAFE_LAST_PRESS_TIME = time; break;
            case 2: SnapTap.FORWARD_STRAFE_LAST_PRESS_TIME = time; break;
            case 3: SnapTap.BACKWARD_STRAFE_LAST_PRESS_TIME = time; break;
        }
    }
}