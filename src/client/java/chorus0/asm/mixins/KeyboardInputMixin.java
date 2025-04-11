/**
 * Created: 2/15/2025
 */

package chorus0.asm.mixins;


import chorus0.Chorus;
import com.chorus.impl.events.input.MovementInputEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends InputMixin {


    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/util/PlayerInput;"))
    private PlayerInput playerInput(PlayerInput original) {

        float movementForward = KeyboardInput.getMovementMultiplier(original.forward(), original.backward());
        float movementSideways = KeyboardInput.getMovementMultiplier(original.left(), original.right());

        MovementInputEvent inputEvent = new MovementInputEvent(
                original.forward(),
                original.backward(),
                original.left(),
                original.right(),
                original.jump(),
                original.sneak(),
                original.sprint(),
                movementForward,
                movementSideways
        );

        Chorus.getInstance().getEventManager().post(inputEvent);

        return new PlayerInput(
                inputEvent.getMovementForward() > 0,
                inputEvent.getMovementForward() < 0,
                inputEvent.getMovementSideways() > 0,
                inputEvent.getMovementSideways() < 0,
                inputEvent.isJumping(),
                inputEvent.isSneaking(),
                inputEvent.isSprinting()
        );
    }

}