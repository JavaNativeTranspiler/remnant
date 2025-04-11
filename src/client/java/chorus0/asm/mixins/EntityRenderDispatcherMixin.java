
/**
 * Created: 12/10/2024
 */
package chorus0.asm.mixins;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
//    @Inject(
//        method = "render",
//        at     = @At("RETURN")
//    )
//    public <E extends Entity> void onRenderPost(E entity, double x, double y, double z, float yaw, float tickDelta,
//                                                MatrixStack matrices, VertexConsumerProvider vertexConsumers,
//                                                int light, CallbackInfo ci) {
//        if (Chorus.getInstance().getModuleManager().isModuleEnabled(Chams.class)) {
//            GL11.glPolygonOffset(1.0f, 1100000.0f);
//            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
//        }
//    }
//
//    @Inject(
//        method      = "render",
//        at          = @At("HEAD") ,
//        cancellable = true
//    )
//    public <E extends Entity> void onRenderPre(E entity, double x, double y, double z, float yaw, float tickDelta,
//                                               MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
//                                               CallbackInfo ci) {
//        if (Chorus.getInstance().getModuleManager().isModuleEnabled(Chams.class)) {
//            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
//            GL11.glPolygonOffset(1.0f, -1100000.0f);
//        }
//    }
}


//~ Formatted by Jindent --- http://www.jindent.com
