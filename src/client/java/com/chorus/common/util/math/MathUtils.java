
/**
 * Created: 10/19/2024
 */
package com.chorus.common.util.math;

import com.chorus.api.system.prot.MathProt;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class MathUtils {
    private static final Random random = new Random();

    /**
     * Calculates the angle between two Vec3d vectors.
     */
    public static double angleBetween(Vec3d v1, Vec3d v2) {
        double dotProduct       = v1.dotProduct(v2);
        double magnitudeProduct = v1.length() * v2.length();

        return Math.acos(dotProduct / magnitudeProduct);
    }

    /**
     * Applies GCD fix to a rotation value.
     */
    public static float applyGCD(float value, float gcd) {
        return value - (value % gcd);
    }

    /**
     * Calculates the GCD (Greatest Common Divisor) of rotation.
     */
    public static float calculateGCD(float sensitivity) {
        float f = (sensitivity * 0.6f + 0.2f);

        return f * f * f * 1.2f;
    }

    /**
     * Clamps a value between a minimum and maximum value.
     */
    public static float clamp(double val, double min, double max) {
        return (float) Math.max(min, Math.min(max, val));
    }

    /**
     * Calculates the distance between two Vec3d points.
     */
    public static double distance(Vec3d vec1, Vec3d vec2) {
        return distance(vec1.x, vec1.y, vec1.z, vec2.x, vec2.y, vec2.z);
    }

    /**
     * Calculates the distance between two 2D points.
     */
    public static double distance(double x1, double z1, double x2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(z2 - z1, 2));
    }

    /**
     * Calculates the distance between two 3D points.
     */
    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    /**
     * Calculates the factorial of a number.
     */
    public static long factorial(int n) {
        if (n == 0) {
            return 1;
        }

        long result = 1;

        for (int i = 1; i <= n; i++) {
            result *= i;
        }

        return result;
    }

    /**
     * Linear interpolation between two values.
     */
    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    /**
     * Smoothly interpolates between two angles (in degrees).
     */
    public static float smoothLerpAngle(float start, float end, float delta, float speed) {
        float diff = MathHelper.wrapDegrees(end - start);

        float alpha = 1 - (float) Math.exp(-speed * delta);
        if (Math.abs(diff) < speed * alpha) {
            return end;
        }

        return start + MathHelper.clamp(diff, -speed * alpha, speed * alpha);


    }
    /**
     * Smoothly step up lerps between two angles (in degrees).
     */
    public static float lerpAngle(float start, float target, float deltaTime, float lerpSpeed) {
        float delta = MathHelper.wrapDegrees(target - start);
        float alpha = 1 - (float) Math.exp(-lerpSpeed * deltaTime);
        return start + delta * alpha;
    }


    /**
     * Returns a random double between min and max.
     */
    public static double randomDouble(double min, double max) {
        if (min == 0 && max == 0) return 0;
        return min + (max - min) * random.nextDouble();
    }

    /**
     * Returns a random integer between min and max, inclusive.
     */
    public static int randomInt(int min, int max) {
        if (min == 0 && max == 0) return 0;

        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Returns a random float between min and max, inclusive.
     */
    public static float randomFloat(float min, float max) {
        if (min == 0 && max == 0) return 0;
        return min + (random.nextFloat() * (max - min));
    }
    /**
     * Returns a random number between min and max (inclusive) for any number type.
     */
    public static <T extends Number> T randomNumber(T min, T max) {
        if (min.doubleValue() == 0 && max.doubleValue() == 0) return min;
        double result = min.doubleValue() + (random.nextDouble() * (max.doubleValue() - min.doubleValue()));


        return switch (min) {
            case Integer i -> (T) Integer.valueOf((int) result);
            case Long l -> (T) Long.valueOf((long) result);
            case Float v -> (T) Float.valueOf((float) result);
            case Double v -> (T) Double.valueOf(result);
            default -> throw new IllegalArgumentException("Unsupported number type: " + min.getClass());
        };
    }

    /**
     * Rounds a number to a specified number of decimal places.
     */
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);

        value = value * factor;

        long tmp = Math.round(value);

        return (double) tmp / factor;
    }

    public static double smoothStepLerp(double delta, double start, double end) {
        double value;

        delta = Math.max(0, Math.min(1, delta));

        double t = delta * delta * (3 - 2 * delta);

        value = start + MathHelper.wrapDegrees(end - start) * t;

        return value;
    }

    /**
     * Converts radians to degrees.
     */
    public static double toDegrees(double radians) {
        return radians * 180.0 / MathProt.PI();
    }

    /**
     * Converts degrees to radians.
     */
    public static double toRadians(double degrees) {
        return degrees * MathProt.PI() / 180.0;
    }

    /**
     * Checks if a number is within a certain range.
     */
    public static boolean isInRange(double value, double min, double max) {
        return (value >= min) && (value <= max);
    }

    /**
     * Calculates the percentage of a value within a range.
     */
    public static double getPercentage(double value, double min, double max) {
        return (value - min) / (max - min) * 100.0;
    }

    /**
     * Checks if a number is prime.
     */
    public static boolean isPrime(int n) {
        if (n <= 1) {
            return false;
        }

        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com