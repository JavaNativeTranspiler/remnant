/**
 * Created: 2/15/2025
 */

package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.impl.modules.visual.PlayerESP;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements QuickImports  {
    @Unique
    private boolean shouldRenderOutline(Entity entity) {
        PlayerESP module = Chorus.getInstance().getModuleManager().getModule(PlayerESP.class);
        entity.setGlowing(true);
        return true;
    }
}