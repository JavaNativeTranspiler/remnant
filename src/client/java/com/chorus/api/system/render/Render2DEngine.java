package com.chorus.api.system.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import com.chorus.common.QuickImports;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ExcludeFlow
@ExcludeConstant
public class Render2DEngine implements QuickImports {

    public static BlurProgram BLUR_PROGRAM = new BlurProgram();

    public static void drawLine(MatrixStack matrices, float x, float y, float x1, float y1, float width, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        BufferBuilder bufferBuilder =Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) x, (float) y, 0f)
                .color(r, g, b, a)
        ;
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), (float) x1, (float) y1, 0f)
                .color(r, g, b, a)
        ;

        RenderSystem.lineWidth(width);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    public static void drawVerticalGradient(MatrixStack matrices, float x, float y, float width, float height, Color color, Color color1) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float r1 = color1.getRed() / 255f;
        float g1 = color1.getGreen() / 255f;
        float b1 = color1.getBlue() / 255f;
        float a1 = color1.getAlpha() / 255f;

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y + height, 0f).color(r1, g1, b1, a1);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y + height, 0f).color(r1, g1, b1, a1);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y, 0f).color(r, g, b, a);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y, 0f).color(r, g, b, a);

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    public static void drawGradientRect(MatrixStack matrices, float x, float y, float width, float height, Color color, Color color1) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        float r1 = color1.getRed() / 255f;
        float g1 = color1.getGreen() / 255f;
        float b1 = color1.getBlue() / 255f;
        float a1 = color1.getAlpha() / 255f;

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y + height, 0f).color(r, g, b, a);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y + height, 0f).color(r1, g1, b1, a1);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y, 0f).color(r1, g1, b1, a1);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y, 0f).color(r, g, b, a);

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        BufferBuilder bufferBuilder =Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y + height, 0f).color(r, g, b, a);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y + height, 0f).color(r, g, b, a);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x + width, y, 0f).color(r, g, b, a);
        bufferBuilder.vertex(matrices.peek().getPositionMatrix(), x, y, 0f).color(r, g, b, a);

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, Color color) {
        drawRoundedRect(matrices, x1, y1, x2, y2, radius, color, color, color, color);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x1, float y1, float x2, float y2, float topLeft, float topRight, float bottomLeft, float bottomRight, Color color) {
        drawRoundedRect(matrices, x1, y1, x2, y2, topLeft, topRight, bottomLeft, bottomRight, color, color, color, color);
    }

    public static void drawTopRoundedRect(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, Color color) {
        drawRoundedRect(matrices, x1, y1, x2, y2, radius, radius, 0, 0, color, color, color, color);
    }

    public static void drawBottomRoundedRect(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, Color color) {
        drawRoundedRect(matrices, x1, y1, x2, y2, 0, 0, radius, radius, color, color, color, color);
    }

    public static void drawRoundedGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, Color color1, Color color2) {
        drawRoundedRect(matrices, x1, y1, x2, y2, radius, color1, color1, color2, color2);
    }

    public static void drawRoundedVerticalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, Color color1, Color color2) {
        drawRoundedRect(matrices, x1, y1, x2, y2, radius, color2, color1, color2, color1);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, Color color1, Color color2, Color color3, Color color4) {
        drawRoundedRect(matrices, x1, y1, x2, y2, radius, radius, radius, radius, color1, color2, color3, color4);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x1, float y1, float x2, float y2, float topLeft, float topRight, float bottomLeft, float bottomRight, Color color1, Color color2, Color color3, Color color4) {
        if (Shaders.ROUNDED_RECT == null)
            Shaders.load();

        x2 = x1 + x2;
        y2 = y1 + y2;

        float scaleFactor = (float) mc.getWindow().getScaleFactor();
        int windowHeight = mc.getWindow().getHeight();

        ShaderProgram shaderProg = Shaders.ROUNDED_RECT;
        if (shaderProg == null) throw new IllegalStateException("Shader program is not available.");

        Vector3f start = transformPosition(matrices, x1, y1, 0f);
        Vector3f end = transformPosition(matrices, x2, y2, 0f);

        float[] actualCoords = getActualCoordinates(start, end, scaleFactor, windowHeight);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        prepareBuffer(buffer, matrices.peek().getPositionMatrix(), x1, y1, x2, y2, 0f, color1);

        shaderProg.getUniform("RadiusTopLeft").set(topLeft * scaleFactor);
        shaderProg.getUniform("RadiusTopRight").set(topRight * scaleFactor);
        shaderProg.getUniform("RadiusBottomLeft").set(bottomLeft * scaleFactor);
        shaderProg.getUniform("RadiusBottomRight").set(bottomRight * scaleFactor);
        shaderProg.getUniform("Bounds").set(actualCoords[0], actualCoords[3], actualCoords[2], actualCoords[1]);
        shaderProg.getUniform("Smoothness").set(2f);
        shaderProg.getUniform("color1").set(color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
        shaderProg.getUniform("color2").set(color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
        shaderProg.getUniform("color3").set(color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
        shaderProg.getUniform("color4").set(color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

        renderShape(buffer, shaderProg);
    }

    public static void drawRoundedOutline(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, float width, Color color) {
        drawRoundedOutline(matrices, x1, y1, x2, y2, radius, width, color, color, color, color);
    }

    public static void drawOutlinedGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, float width, Color color1, Color color2) {
        drawRoundedOutline(matrices, x1, y1, x2, y2, radius, width, color1, color1, color2, color2);
    }

    public static void drawOutlinedVerticalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, float width, Color color1, Color color2) {
        drawRoundedOutline(matrices, x1, y1, x2, y2, radius, width, color2, color1, color2, color1);
    }

    public static void drawRoundedOutline(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, float width, Color color1, Color color2, Color color3, Color color4) {
        if (Shaders.ROUNDED_OUTLINE == null)
            Shaders.load();

        x2 = x1 + x2;
        y2 = y1 + y2;

        ShaderProgram shaderProg = Shaders.ROUNDED_OUTLINE;
        if (shaderProg == null) throw new IllegalStateException("Shader program is not available.");

        float scaleFactor = (float) mc.getWindow().getScaleFactor();
        int windowHeight = mc.getWindow().getHeight();

        Vector3f start = transformPosition(matrices, x1, y1, 0f);
        Vector3f end = transformPosition(matrices, x2, y2, 0f);

        float[] actualCoords = getActualCoordinates(start, end, scaleFactor, windowHeight);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        prepareBuffer(buffer, matrices.peek().getPositionMatrix(), x1, y1, x2, y2, 0f, color1);

        shaderProg.getUniform("Radius").set(radius * scaleFactor);
        shaderProg.getUniform("Bounds").set(actualCoords[0] + 1, actualCoords[3] + 1, actualCoords[2] - 1, actualCoords[1] - 1);
        shaderProg.getUniform("Smoothness").set(2f);
        shaderProg.getUniform("StrokeWidth").set(width);
        shaderProg.getUniform("color1").set(color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, color1.getAlpha() / 255f);
        shaderProg.getUniform("color2").set(color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, color2.getAlpha() / 255f);
        shaderProg.getUniform("color3").set(color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, color3.getAlpha() / 255f);
        shaderProg.getUniform("color4").set(color4.getRed() / 255f, color4.getGreen() / 255f, color4.getBlue() / 255f, color4.getAlpha() / 255f);

        renderShape(buffer, Shaders.ROUNDED_OUTLINE);
    }

    public static void drawCircle(MatrixStack matrices, float ox, float oy, float radius, Color color) {
        drawCircle(matrices, ox, oy, radius, color, 0, 360);
    }

    public static void drawCircle(MatrixStack matrices, float x, float y, float radius, Color color, float startAngle, float angleRange) {
        if (Shaders.CIRCLE == null)
            Shaders.load();

        float scaleFactor = (float) mc.getWindow().getScaleFactor();

        float x1 = x - radius;
        float y1 = y - radius;
        float x2 = x + radius;
        float y2 = y + radius;

        int wh = mc.getWindow().getHeight();

        float actualX = x * scaleFactor;
        float actualY = wh - y * scaleFactor;

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        prepareBuffer(buffer, positionMatrix, x1, y1, x2, y2, 0f, color);

        ShaderProgram shaderProg = Shaders.CIRCLE;
        shaderProg.getUniform("Origin").set(actualX, actualY);
        shaderProg.getUniform("Radius").set(radius * scaleFactor);
        shaderProg.getUniform("StartAngle").set(startAngle);
        shaderProg.getUniform("AngleRange").set(angleRange);

        renderShape(buffer, Shaders.CIRCLE);
    }

    private static Vector3f transformPosition(MatrixStack matrices, float x, float y, float z) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        return positionMatrix.transformPosition(x, y, z, new Vector3f());
    }

    private static float[] getActualCoordinates(Vector3f start, Vector3f end, float scaleFactor, int windowHeight) {
        float actualX1 = start.x * scaleFactor;
        float actualX2 = end.x * scaleFactor;
        float actualY1 = windowHeight - start.y * scaleFactor;
        float actualY2 = windowHeight - end.y * scaleFactor;
        return new float[]{actualX1, actualY1, actualX2, actualY2};
    }

    public static void prepareBuffer(BufferBuilder buffer, Matrix4f positionMatrix, float x1, float y1, float x2, float y2, float z, Color color) {
        buffer.vertex(positionMatrix, x1, y1, z).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vertex(positionMatrix, x1, y2, z).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vertex(positionMatrix, x2, y2, z).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        buffer.vertex(positionMatrix, x2, y1, z).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    private static void renderShape(BufferBuilder buffer, ShaderProgram shaderProgram) {
        RenderSystem.disableDepthTest();

        ShaderProgram last = RenderSystem.getShader();
        RenderSystem.setShader(shaderProgram);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.disableBlend();

        RenderSystem.setShader(last);

        RenderSystem.enableDepthTest();
    }

    public static void drawOutline(MatrixStack matrices, float x, float y, float width, float height, Color color) {
        drawLine(matrices, x, y, x + width, y, 1, color);
        drawLine(matrices, x + width, y, x + width, y + height, 1, color);
        drawLine(matrices, x - 0.5f, y + height, x + width, y + height, 1, color);
        drawLine(matrices, x, y, x, y + height + 0.5f, 1, color);
    }

    public static void drawQuads(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color color) {
        bufferBuilder.vertex(matrix, x, y1, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        bufferBuilder.vertex(matrix, x1, y1, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        bufferBuilder.vertex(matrix, x1, y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static void drawColorPicker(MatrixStack matrices, float x, float y, float width, float height, float hue, float alpha) {
        if (Shaders.COLOR_PICKER == null)
            Shaders.load();

        float scaleFactor = (float) mc.getWindow().getScaleFactor();
        int windowHeight = mc.getWindow().getHeight();

        ShaderProgram shaderProg = Shaders.COLOR_PICKER;
        if (shaderProg == null) throw new IllegalStateException("Shader program is not available.");

        Vector3f start = transformPosition(matrices, x, y, 0f);
        Vector3f end = transformPosition(matrices, x + width, y + height, 0f);

        float[] actualCoords = getActualCoordinates(start, end, scaleFactor, windowHeight);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        prepareBuffer(buffer, matrices.peek().getPositionMatrix(), x, y, x + width, y + height, 0f, Color.WHITE);

        Shaders.colorPickerResolution.set(width * scaleFactor, height * scaleFactor);
        Shaders.colorPickerPosition.set(actualCoords[0], actualCoords[3]);
        Shaders.colorPickerHue.set(hue);
        Shaders.colorPickerAlpha.set(alpha);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(shaderProg);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public static void drawAlphaSlider(MatrixStack matrices, float x, float y, float width, float height, Color baseColor) {
        float scaleFactor = (float) mc.getWindow().getScaleFactor();
        int windowHeight = mc.getWindow().getHeight();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (int i = 0; i <= 10; i++) {
            float alpha = i / 10.0f;
            float xPos = x + (width * i / 10.0f);

            buffer.vertex(matrix, xPos, y, 0)
                    .color(baseColor.getRed() / 255f, baseColor.getGreen() / 255f, baseColor.getBlue() / 255f, alpha);
            buffer.vertex(matrix, xPos, y + height, 0)
                    .color(baseColor.getRed() / 255f, baseColor.getGreen() / 255f, baseColor.getBlue() / 255f, alpha);
        }

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    public static void drawRoundedBlur(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, float blurRadius, Color color) {
        if (Shaders.BLUR == null)
            Shaders.load();

        x2 = x1 + x2;
        y2 = y1 + y2;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        buffer.vertex(matrices.peek().getPositionMatrix(), x1, y1, 0f);
        buffer.vertex(matrices.peek().getPositionMatrix(), x1, y2, 0f);
        buffer.vertex(matrices.peek().getPositionMatrix(), x2, y2, 0f);
        buffer.vertex(matrices.peek().getPositionMatrix(), x2, y1, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        BLUR_PROGRAM.setParameters(x1, y1, x2 - x1, y2 - y1, radius, blurRadius, 1.0f);
        BLUR_PROGRAM.use();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawRoundedBlur(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, float blurRadius) {
        if (Shaders.BLUR == null)
            Shaders.load();

        x2 = x1 + x2;
        y2 = y1 + y2;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        buffer.vertex(matrices.peek().getPositionMatrix(), x1, y1, 0f);
        buffer.vertex(matrices.peek().getPositionMatrix(), x1, y2, 0f);
        buffer.vertex(matrices.peek().getPositionMatrix(), x2, y2, 0f);
        buffer.vertex(matrices.peek().getPositionMatrix(), x2, y1, 0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        BLUR_PROGRAM.setParameters(x1, y1, x2 - x1, y2 - y1, radius, blurRadius, 1.0f);
        BLUR_PROGRAM.use();
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void drawBlurredRoundedRect(MatrixStack matrices, float x1, float y1, float x2, float y2, float radius, float blurRadius, Color color) {
        if (Shaders.BLUR == null)
            Shaders.load();

        BLUR_PROGRAM.beginBlur(x1, y1, x2, y2, radius, blurRadius, 1.0f);

        drawRoundedRect(matrices, x1, y1, x2, y2, radius, color);

        BLUR_PROGRAM.endBlur();
    }

    public static void drawBlurredRect(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, Color color) {
        if (Shaders.BLUR == null)
            Shaders.load();

        BLUR_PROGRAM.beginBlur(x, y, width, height, 0, blurRadius, 1.0f);
        
        drawRect(matrices, x, y, width, height, color);
        
        BLUR_PROGRAM.endBlur();
    }
}