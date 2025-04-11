/**
 * Created: 2/4/2025
 */

package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.impl.modules.other.Streamer;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TextVisitFactory.class)
public class TextVisitFactoryMixin implements QuickImports {
    @ModifyArg(method =
            {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
            at=@At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), index = 0)
    private static String injectVisitFormatted(String string) {
        if (Chorus.getInstance() == null || Chorus.getInstance().getModuleManager() == null) return string;
        if (!Chorus.getInstance().getModuleManager().getModule(Streamer.class).isEnabled()) return string;
        if (mc.player == null) return string;
        return string.replace(mc.player.getNameForScoreboard(), "Chorus User");
    }
}