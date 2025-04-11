package com.chorus.api.system.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import com.chorus.common.QuickImports;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.Uniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

@Slf4j
@ExcludeFlow
@ExcludeConstant
public class Shaders implements QuickImports, SimpleSynchronousResourceReloadListener {
    public static ShaderProgram ROUNDED_RECT, ROUNDED_OUTLINE, CIRCLE, MSDF, BLUR, COLOR_PICKER;

    public static Uniform msdfPxrange;

    public static Uniform colorPickerResolution;
    public static Uniform colorPickerPosition;
    public static Uniform colorPickerHue;
    public static Uniform colorPickerAlpha;

    public static Uniform blurInputResolution;
    public static Uniform blurSize;
    public static Uniform blurLocation;
    public static Uniform blurRadius;
    public static Uniform blurBrightness;
    public static Uniform blurQuality;
    public static Uniform blurSampler;
    
    private static boolean initialized = false;
    
    static {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new Shaders());
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("chorus", "reload_shaders");
    }

    @Override
    public void reload(ResourceManager manager) {
        log.info("Reloading shaders...");
        load();
        log.info("Shaders reloaded successfully");
    }

    public static void load() {
        try {
            ROUNDED_RECT = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(
                    new ShaderProgramKey(
                            Identifier.of("chorus", "core/rounded_rect"),
                            VertexFormats.POSITION_COLOR,
                            Defines.EMPTY
                    )
            );

            ROUNDED_OUTLINE = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(
                    new ShaderProgramKey(
                            Identifier.of("chorus", "core/rounded_outline"),
                            VertexFormats.POSITION_COLOR,
                            Defines.EMPTY
                    )
            );

            CIRCLE = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(
                    new ShaderProgramKey(
                            Identifier.of("chorus", "core/circle"),
                            VertexFormats.POSITION_COLOR,
                            Defines.EMPTY
                    )
            );

            MSDF = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(
                    new ShaderProgramKey(
                            Identifier.of("chorus", "core/msdf"),
                            VertexFormats.POSITION_TEXTURE_COLOR,
                            Defines.EMPTY
                    )
            );

            COLOR_PICKER = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(
                    new ShaderProgramKey(
                            Identifier.of("chorus", "core/color_picker"),
                            VertexFormats.POSITION_COLOR,
                            Defines.EMPTY
                    )
            );

            BLUR = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(
                    new ShaderProgramKey(
                            Identifier.of("chorus", "core/blur"),
                            VertexFormats.POSITION,
                            Defines.EMPTY
                    ));

            msdfPxrange = MSDF.getUniform("pxRange");

            blurInputResolution = BLUR.getUniform("InputResolution");
            blurBrightness = BLUR.getUniform("Brightness");
            blurQuality = BLUR.getUniform("Quality");
            blurSize = BLUR.getUniform("uSize");
            blurLocation = BLUR.getUniform("uLocation");
            blurRadius = BLUR.getUniform("radius");

            colorPickerResolution = COLOR_PICKER.getUniform("Resolution");
            colorPickerPosition = COLOR_PICKER.getUniform("Position");
            colorPickerHue = COLOR_PICKER.getUniform("Hue");
            colorPickerAlpha = COLOR_PICKER.getUniform("Alpha");
            
            initialized = true;
        } catch (Exception e) {
            log.error("Failed to load shaders: {}", e.getMessage());
        }
    }
    
    public static boolean isInitialized() {
        return initialized;
    }
}