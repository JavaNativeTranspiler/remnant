/**
 * Created: 12/11/2024
 */
package chorus0.asm.accessors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor
    Mouse getMouse();
}


//~ Formatted by Jindent --- http://www.jindent.com
