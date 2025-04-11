
/**
 * Created: 10/19/2024
 */
package com.chorus.common.util.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class MovementUtils {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    /**
     * Calculates the current speed of the player.
     *
     * @return The player's speed as a double value.
     */
    public static double getSpeed() {
        if (mc.player == null) {
            return 0.0D;
        }
        return Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
    }
    /**
     * Calculates the current speed of the player.
     *
     * @return The player's speed as a double value.
     */
    public static double getSpeed(LivingEntity entity) {
        if (mc.player == null) {
            return 0.0D;
        }
        return Math.hypot(entity.getVelocity().x, entity.getVelocity().z);
    }

    public static int[] convertToMoveDirection(float deltaYaw, float forwardMultiplier, float sidewaysMultiplier) {
        float forwardMovement = forwardMultiplier * MathHelper.cos(deltaYaw * 0.017453292f) - forwardMultiplier *
                MathHelper.sin(deltaYaw * 0.017453292f);
        float sidewaysMovement = sidewaysMultiplier * MathHelper.cos(deltaYaw * 0.017453292f) + sidewaysMultiplier *
                MathHelper.sin(deltaYaw * 0.017453292f);

        var movementForward = Math.round(forwardMovement);
        var movementSideways = Math.round(sidewaysMovement);
        return new int[]{movementForward, movementSideways};
    }


    /**
     * Sets the player's speed with strafe movement.
     *
     * @param speed The desired speed to set.
     */
    public static void setSpeedWithStrafe(double speed) {
        if (mc.player == null) {
            return;
        }

        mc.player.setVelocity(-Math.sin(getDirection()) * speed,
                mc.player.getVelocity().getY(),
                Math.cos(getDirection()) * speed);
    }
    /**
     * Sets the player's speed with strafe movement.
     *
     * @param speed The desired speed to set.
     */
    public static void setSpeedWithStrafe(double speed, float yaw) {
        if (mc.player == null) {
            return;
        }

        mc.player.setVelocity(-Math.sin(getDirection()) * speed,
                mc.player.getVelocity().getY(),
                Math.cos(getDirection()) * speed);
    }
    /**
     * Determines if the player has any movement input.
     *
     */
    public static boolean hasMovementInput() {
        if (mc.player == null) {
            return false;
        }
        return mc.options.forwardKey.isPressed() ||
                mc.options.backKey.isPressed() ||
                mc.options.leftKey.isPressed() ||
                mc.options.rightKey.isPressed()
                || mc.options.jumpKey.isPressed();

    }

    /**
     * Calculates the direction the player should move based on input.
     *
     * @return The direction in radians.
     */
    public static double getDirection() {
        if (mc.player == null) {
            return 0.0D;
        }

        float moveForward = mc.player.input.movementForward;
        float moveStrafing = mc.player.input.movementSideways;
        float rotationYaw = mc.player.getYaw();

        if (moveForward < 0F) {
            rotationYaw += 180F;
        }

        float forward = Math.abs(moveForward) > 0.1F ? (moveForward > 0F ? 0.5F : -0.5F) : 0F;

        if (moveStrafing > 0F) {
            rotationYaw -= 90F * forward;
        } else if (moveStrafing < 0F) {
            rotationYaw += 90F * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    /**
     * Calculates the direction the player should move based on input.
     * @param rotationYaw
     * @param moveForward
     * @param moveStrafing
     * @return
     */
    public static double getDirection(float rotationYaw, double moveForward, double moveStrafing) {
        if (mc.player == null) {
            return 0.0D;
        }

        float rotationYawCalced = rotationYaw;

        if (moveForward < 0F) {
            rotationYawCalced += 180F;
        }

        float forward = Math.abs(moveForward) > 0.1F ? (moveForward > 0F ? 0.5F : -0.5F) : 0F;

        if (moveStrafing > 0F) {
            rotationYawCalced -= 90F * forward;
        } else if (moveStrafing < 0F) {
            rotationYawCalced += 90F * forward;
        }

        return Math.toRadians(rotationYawCalced);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
