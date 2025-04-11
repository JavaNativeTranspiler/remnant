/**
 * Created: 2/4/2025
 */

package com.chorus.api.system.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import com.chorus.common.QuickImports;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ExcludeFlow
@ExcludeConstant
public class Render3DEngine implements QuickImports {


    public static void renderOutlinedBox(Vec3d position, Color color, MatrixStack stack, float width, float height) {
        int originalDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean originalCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean originalBlend = GL11.glIsEnabled(GL11.GL_BLEND);

        ShaderProgram originalShader = RenderSystem.getShader();

        try {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = color.getAlpha() / 255f;

            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            Vec3d start = position.subtract(camPos);

            float x = (float) start.x;
            float y = (float) start.y;
            float z = (float) start.z;

            stack.push();
            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder buffer;

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            GL11.glDepthFunc(GL11.GL_ALWAYS);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();

            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            // side lines
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);

            // top lines
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);
            // bottom lines
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            GL11.glDepthFunc(originalDepthFunc);
            if (originalCull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (originalBlend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }

            RenderSystem.setShader(originalShader);

            stack.pop();
        }
    }

    public static void renderOutlinedBox(PlayerEntity player, Color color, MatrixStack stack) {
        int originalDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean originalCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean originalBlend = GL11.glIsEnabled(GL11.GL_BLEND);

        ShaderProgram originalShader = RenderSystem.getShader();

        try {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = color.getAlpha() / 255f;

            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            Vec3d start = player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)).subtract(camPos);
            float x = (float) start.x;
            float y = (float) start.y;
            float z = (float) start.z;

            stack.push();
            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder buffer;

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            GL11.glDepthFunc(GL11.GL_ALWAYS);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();


            float width = player.getWidth() / 2;
            float height = player.getHeight();
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            // side lines
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);

            // top lines
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);
            // bottom lines
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);

            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            GL11.glDepthFunc(originalDepthFunc);
            if (originalCull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (originalBlend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }

            RenderSystem.setShader(originalShader);

            stack.pop();
        }
    }

    public static void renderOutlinedShadedBox(Vec3d position, Color color, int shadedAlpha, MatrixStack stack, float width, float height) {
        int originalDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean originalCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean originalBlend = GL11.glIsEnabled(GL11.GL_BLEND);

        ShaderProgram originalShader = RenderSystem.getShader();

        try {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = color.getAlpha() / 255f;
            float specialAlpha = shadedAlpha / 255f;

            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            Vec3d start = position.subtract(camPos);

            float x = (float) start.x;
            float y = (float) start.y;
            float z = (float) start.z;

            stack.push();
            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder buffer;

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            GL11.glDepthFunc(GL11.GL_ALWAYS);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            // side lines
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);

            // top lines
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);

            // bottom lines
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, alpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());


            // bottom rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // top rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // west rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());


            // east rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());


            // north rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // south rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            GL11.glDepthFunc(originalDepthFunc);
            if (originalCull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (originalBlend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }

            RenderSystem.setShader(originalShader);

            stack.pop();
        }
    }

    public static void renderOutlinedShadedBox(PlayerEntity player, Color color, int shadedAlpha, MatrixStack stack) {
        int originalDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean originalCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean originalBlend = GL11.glIsEnabled(GL11.GL_BLEND);

        ShaderProgram originalShader = RenderSystem.getShader();

        try {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float alpha = color.getAlpha() / 255f;
            float specialAlpha = shadedAlpha / 255f;

            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            Vec3d start = player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)).subtract(camPos);

            float x = (float) start.x;
            float y = (float) start.y;
            float z = (float) start.z;

            stack.push();
            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder buffer;

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            GL11.glDepthFunc(GL11.GL_ALWAYS);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();

            float width = player.getWidth() / 2;
            float height = player.getHeight();

            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            // side lines
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, alpha);

            // top lines
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, alpha);
            buffer.vertex(matrix, x + width,y + height,z - width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x + width,y + height,z + width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x + width,y + height,z + width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x - width,y + height,z + width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x - width,y + height,z + width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x - width,y + height,z - width).color(red ,green ,blue ,alpha );

            // bottom lines
            buffer.vertex(matrix,x - width,y,z - width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x - width,y + height,z - width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x + width,y,z - width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x + width,y + height,z - width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x + width,y,z + width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x + width,y + height,z + width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x - width,y,z + width).color(red ,green ,blue ,alpha );
            buffer.vertex(matrix,x - width,y + height,z + width).color(red ,green ,blue ,alpha );
            BufferRenderer.drawWithGlobalProgram(buffer.end());


            // bottom rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // top rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // west rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());


            // east rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());



            // north rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // south rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            GL11.glDepthFunc(originalDepthFunc);
            if (originalCull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (originalBlend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }

            RenderSystem.setShader(originalShader);

            stack.pop();
        }
    }

    public static void renderShadedBox(Vec3d position, Color color, int shadedAlpha, MatrixStack stack, float width, float height) {
        int originalDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean originalCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean originalBlend = GL11.glIsEnabled(GL11.GL_BLEND);

        ShaderProgram originalShader = RenderSystem.getShader();
        try {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float specialAlpha = shadedAlpha / 255f;

            Vec3d camPos = mc.gameRenderer.getCamera().getPos();
            Vec3d start = position.subtract(camPos);

            float x = (float) start.x;
            float y = (float) start.y;
            float z = (float) start.z;

            stack.push();
            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder buffer;

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();


            // bottom rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // top rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // west rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());


            // east rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());



            // north rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // south rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        } finally {
            GL11.glDepthFunc(originalDepthFunc);
            if (originalCull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (originalBlend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }

            RenderSystem.setShader(originalShader);

            stack.pop();
        }
    }

    public static void renderShadedBox(PlayerEntity player, Color color, int shadedAlpha, MatrixStack stack) {
        int originalDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean originalCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean originalBlend = GL11.glIsEnabled(GL11.GL_BLEND);

        ShaderProgram originalShader = RenderSystem.getShader();

        try {
            float red = color.getRed() / 255f;
            float green = color.getGreen() / 255f;
            float blue = color.getBlue() / 255f;
            float specialAlpha = shadedAlpha / 255f;

            stack.push();
            Matrix4f matrix = stack.peek().getPositionMatrix();
            BufferBuilder buffer;

            Vec3d start = player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(true)).subtract( mc.getEntityRenderDispatcher().camera.getPos());
            float x = (float) start.x;
            float y = (float) start.y;
            float z = (float) start.z;

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            GL11.glDepthFunc(GL11.GL_ALWAYS);

            float width = player.getWidth() / 2;
            float height = player.getHeight();


            // bottom rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // top rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha) ;
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // west rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // east rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            // north rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z + width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z + width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

            // south rect
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            buffer.vertex(matrix, x - width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x - width, y, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y + height, z - width).color(red, green, blue, specialAlpha);
            buffer.vertex(matrix, x + width, y, z - width).color(red, green, blue, specialAlpha);
            BufferRenderer.drawWithGlobalProgram(buffer.end());

        } finally {
            GL11.glDepthFunc(originalDepthFunc);
            if (originalCull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (originalBlend) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }

            RenderSystem.setShader(originalShader);

            stack.pop();
        }
    }


    public static Pair<Vec3d, Boolean> project(Matrix4f modelView, Matrix4f projection, Vec3d vector) {
        Vec3d camPos = vector.subtract(mc.gameRenderer.getCamera().getPos());
        Vector4f vec = new Vector4f((float) camPos.x, (float) camPos.y, (float) camPos.z, 1F);
        
        vec.mul(modelView);
        vec.mul(projection);
        
        boolean isVisible = vec.w() > 0.0;
        
        if (vec.w() != 0) {
            vec.x /= vec.w();
            vec.y /= vec.w();
            vec.z /= vec.w();
        }
        
        double screenX = (vec.x() * 0.5 + 0.5) * mc.getWindow().getScaledWidth();
        double screenY = (0.5 - vec.y() * 0.5) * mc.getWindow().getScaledHeight();
        
        Vec3d position = new Vec3d(screenX, screenY, vec.z());
        
        return new Pair<>(position, isVisible);
    }

    public static Vec3d getEntityPositionInterpolated(Entity entity, float delta) {
        return new Vec3d(MathHelper.lerp(delta, entity.prevX, entity.getX()),
                MathHelper.lerp(delta, entity.prevY, entity.getY()),
                MathHelper.lerp(delta, entity.prevZ, entity.getZ()));
    }

    public static Vec3d getEntityPositionOffsetInterpolated(Entity entity, float delta) {
        Vec3d interpolated = getEntityPositionInterpolated(entity, delta);
        return entity.getPos().subtract(interpolated);
    }
}