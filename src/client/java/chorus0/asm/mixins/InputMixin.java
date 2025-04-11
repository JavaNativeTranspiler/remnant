/**
 * Created: 2/15/2025
 */

package chorus0.asm.mixins;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Input.class)
public class InputMixin {
    @Shadow
    public PlayerInput playerInput;
    @Shadow
    public float movementForward;
    @Shadow
    public float movementSideways;
}