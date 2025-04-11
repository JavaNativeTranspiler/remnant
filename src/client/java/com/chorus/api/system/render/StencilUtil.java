package com.chorus.api.system.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

@ExcludeFlow
@ExcludeConstant
public class StencilUtil {

    public static void initStencil() {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);

        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0xFF, 0xFF);
        RenderSystem.stencilMask(0xFF);

        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT);
    }

    public static void eraseStencil() {
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 0xFF, 0xFF);
    }

    public static void disposeStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static void renderStencil(final Runnable init, final Runnable end) {
        initStencil();
        init.run();
        eraseStencil();
        end.run();
        disposeStencil();
    }
}

