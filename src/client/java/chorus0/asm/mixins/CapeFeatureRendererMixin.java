package chorus0.asm.mixins;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentModel.LayerType;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    public CapeFeatureRendererMixin(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Final
    @Shadow
    private BipedEntityModel<PlayerEntityRenderState> model;

    @Shadow
    protected abstract boolean hasCustomModelForLayer(ItemStack stack, EquipmentModel.LayerType layerType);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, PlayerEntityRenderState playerEntityRenderState, float f, float g, CallbackInfo ci) {
        if (!playerEntityRenderState.invisible && playerEntityRenderState.capeVisible) {
            SkinTextures skinTextures = playerEntityRenderState.skinTextures;
            if (skinTextures.capeTexture() != null) {
                if (!this.hasCustomModelForLayer(playerEntityRenderState.equippedChestStack, LayerType.WINGS)) {
                    matrixStack.push();
                    if (this.hasCustomModelForLayer(playerEntityRenderState.equippedChestStack, LayerType.HUMANOID)) {
                        matrixStack.translate(0.0F, -0.063125F, 0.06875F);
                    }
                    
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(skinTextures.capeTexture()));
                    ((PlayerEntityModel)this.getContextModel()).copyTransforms(this.model);
                    this.model.setAngles(playerEntityRenderState);
                    this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
                    matrixStack.pop();
                }
            }
        }
        ci.cancel();
    }
}
