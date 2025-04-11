package chorus0.asm.accessors;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HeldItemRenderer.class)
public interface HeldItemRendererAccessor {
    @Accessor
    float getEquipProgressMainHand();

    @Accessor
    void setEquipProgressMainHand(float equipProgressMainHand);

    @Accessor
    float getEquipProgressOffHand();

    @Accessor
    void setEquipProgressOffHand(float equipProgressOffHand);

    @Accessor
    ItemStack getMainHand();

    @Accessor
    void setMainHand(ItemStack mainHand);

    @Accessor
    ItemStack getOffHand();

    @Accessor
    void setOffHand(ItemStack offHand);
}
