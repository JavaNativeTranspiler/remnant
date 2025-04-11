package com.chorus.impl.events.render;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

@Getter
@Setter
public class Render3DEvent extends Event {
    private final Mode mode;
    private final MatrixStack matrices;
    private final float tickDelta;
    private final Camera camera;
    private final GameRenderer gameRenderer;
    private final Matrix4f projectionMatrix;
    private final WorldRenderer worldRenderer;

    public enum Mode { PRE, POST }

    public Render3DEvent(Mode mode, MatrixStack matrices, float tickDelta,
                         Camera camera, GameRenderer gameRenderer,
                         Matrix4f projectionMatrix, WorldRenderer worldRenderer) {
        this.mode = mode;
        this.matrices = matrices;
        this.tickDelta = tickDelta;
        this.camera = camera;
        this.gameRenderer = gameRenderer;
        this.projectionMatrix = projectionMatrix;
        this.worldRenderer = worldRenderer;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
