package com.chorus.api.system.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import com.chorus.common.QuickImports;
import com.chorus.common.util.world.SocialManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

@ExcludeFlow
@ExcludeConstant
public class ColorUtils implements QuickImports {


    /**
     * Interpolates between two colors.
     *
     * @param oldValue The original value.
     * @param newValue The new value.
     * @param factor The interpolation factor (0.0 to 1.0).
     * @return The interpolated value.
     */
    private static int interpolate(int oldValue, int newValue, float factor) {
        return (int) (oldValue + (newValue - oldValue) * factor);
    }

    public static Color interpolateColor(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolate(color1.getRed(), color2.getRed(), amount),
                interpolate(color1.getGreen(), color2.getGreen(), amount),
                interpolate(color1.getBlue(), color2.getBlue(), amount),
                interpolate(color1.getAlpha(), color2.getAlpha(), amount));
    }
    public static Color getAssociatedColor(PlayerEntity player) {
        if (player == mc.player) return new Color(127,255,127);
        if (friendRepository.isFriend(player.getUuid())) return new Color(127,255,127);
        if (npcRepository.isNPC(player.getNameForScoreboard())) return new Color(118,118,118);
        if (SocialManager.isTarget(player)) return new Color(255,127,127);
        if (player.getScoreboardTeam() != null && player.getScoreboardTeam().getColor().getColorValue() != null) {
            return formattingToRGB(player.getScoreboardTeam().getColor().getColorValue());
        }
        return new Color(184, 112, 242);
    }
    public static Color formattingToRGB(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return new Color(red, green, blue);
    }
    /**
     * Gets the middle color between two colors.
     *
     * @param color1 The first color (ARGB format).
     * @param color2 The second color (ARGB format).
     * @return The middle color (ARGB format).
     */
    public static int getMiddleColor(int color1, int color2) {
        return interpolateColor(new Color(color1), new Color(color1), 0.5f).getRGB();
    }

    /**
     * Generates a gradient of colors.
     *
     * @param startColor The starting color (ARGB format).
     * @param endColor   The ending color (ARGB format).
     * @param steps      The number of colors in the gradient.
     * @return An array of interpolated colors.
     */
    public static int[] generateGradient(int startColor, int endColor, int steps) {
        int[] gradient = new int[steps];
        for (int i = 0; i < steps; i++) {
            float factor = (float) i / (steps - 1);
            gradient[i] = interpolateColor(new Color(startColor), new Color(endColor), factor).getRGB();
        }
        return gradient;
    }

    /**
     * Combines two colors by averaging their components.
     *
     * @param color1 The first color (ARGB format).
     * @param color2 The second color (ARGB format).
     * @return The combined color (ARGB format).
     */
    public static int combineColors(int color1, int color2) {
        int a = ((color1 >> 24) & 0xff) + ((color2 >> 24) & 0xff) >> 1;
        int r = ((color1 >> 16) & 0xff) + ((color2 >> 16) & 0xff) >> 1;
        int g = ((color1 >> 8) & 0xff) + ((color2 >> 8) & 0xff) >> 1;
        int b = (color1 & 0xff) + (color2 & 0xff) >> 1;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Converts HSV color values to RGB.
     *
     * @param hue        Hue value (0-360).
     * @param saturation Saturation value (0-1).
     * @param value      Value/Brightness value (0-1).
     * @return The RGB color (ARGB format).
     */
    public static int hsvToRgb(float hue, float saturation, float value) {
        int h = (int) (hue / 60);
        float f = hue / 60 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        float r, g, b;
        switch (h) {
            case 0:
                r = value;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = value;
                b = p;
                break;
            case 2:
                r = p;
                g = value;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = value;
                break;
            case 4:
                r = t;
                g = p;
                b = value;
                break;
            default:
                r = value;
                g = p;
                b = q;
                break;
        }

        int ri = MathHelper.clamp((int) (r * 255), 0, 255);
        int gi = MathHelper.clamp((int) (g * 255), 0, 255);
        int bi = MathHelper.clamp((int) (b * 255), 0, 255);

        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    /**
     * Converts an ARGB integer to a Color object.
     *
     * @param argb The color in ARGB format.
     * @return A Color object representing the ARGB color.
     */
    public static Color intToColor(int argb) {
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;
        return new Color(r, g, b, a);
    }

    /**
     * Converts a Color object to an ARGB integer.
     *
     * @param color The Color object to convert.
     * @return The color as an ARGB integer.
     */
    public static int colorToInt(Color color) {
        return color.getRGB();
    }


    /**
     * Interpolates between two colors based on time and a speed factor.
     *
     * @param color1  The first color to interpolate from.
     * @param color2  The second color to interpolate to.
     * @param speed   The speed at which the interpolation occurs in seconds.
     * @param index   An index value to offset the interpolation.
     * @return        The interpolated color.
     */
    public static Color interpolateColor(Color color1, Color color2, int speed, int index) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return interpolateColor(color1, color2, angle / 360f);
    }
}