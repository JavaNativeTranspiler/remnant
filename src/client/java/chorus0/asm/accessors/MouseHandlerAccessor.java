
/**
 * Created: 12/11/2024
 */
package chorus0.asm.accessors;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mouse.class)
public interface MouseHandlerAccessor {
    @Invoker("onMouseButton")
    void press(long window, int button, int action, int mods);
}


//~ Formatted by Jindent --- http://www.jindent.com
