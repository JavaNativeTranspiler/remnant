package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.impl.modules.client.Capes;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin implements QuickImports {
    @Shadow
    @Final
    private GameProfile profile;
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void getSkinTextures(CallbackInfoReturnable<SkinTextures> ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        var textures = ci.getReturnValue();
        if (!Chorus.getInstance().getModuleManager().getModule(Capes.class).isEnabled() || mc.player != null && !profile.getId().equals(mc.player.getGameProfile().getId())) {

        } else {
            Identifier customCape = Identifier.of("chorus", "capes/" + Chorus.getInstance().getModuleManager().getModule(Capes.class).cape + ".png");
            ci.setReturnValue(new SkinTextures(textures.texture(), textures.textureUrl(), customCape, textures.elytraTexture(), textures.model(), textures.secure()));
        }
    }
}