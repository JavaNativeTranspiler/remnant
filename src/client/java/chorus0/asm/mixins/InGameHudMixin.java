package chorus0.asm.mixins;

import chorus0.Chorus;
import com.chorus.impl.events.render.Render2DEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(
        method      = "render",
        at          = @At("HEAD") ,
        cancellable = true
    )
    public void onWorldRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Render2DEvent preRender2DEvent = new Render2DEvent(context, Render2DEvent.Mode.PRE);

        Chorus.getInstance().getEventManager().post(preRender2DEvent);

        if (preRender2DEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
        method      = "render",
        at          = @At("TAIL") ,
        cancellable = true
    )
    public void onWorldRenderPost(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Render2DEvent postRender2DEvent = new Render2DEvent(context, Render2DEvent.Mode.POST);

        Chorus.getInstance().getEventManager().post(postRender2DEvent);

        if (postRender2DEvent.isCancelled()) {
            ci.cancel();
        }
    }
    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if (Chorus.getInstance().getModuleManager().getModule(com.chorus.impl.modules.visual.Scoreboard.class).isEnabled()) {
            ci.cancel();
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
