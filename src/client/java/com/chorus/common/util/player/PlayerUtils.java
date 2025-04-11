
/**
 * Created: 10/19/2024
 */
package com.chorus.common.util.player;

import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SocialManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

public class PlayerUtils {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    /**
     * Checks if you can critical hit
     * DOES NOT INCLUDE SPRINT STATE
     */
    public static boolean canCriticalHit() {
        if (mc.player == null || mc.world == null) return false;
        boolean canCrit = (mc.player.fallDistance > 0) &&
                !mc.player.isTouchingWater() &&
                !mc.player.isInLava() &&
                !mc.player.isClimbing() &&
                !mc.player.hasVehicle() &&
                !mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS);
        return canCrit;
    }

    public static Entity raytraceEntity(double range) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d viewVector = mc.player.getRotationVecClient();
        Vec3d extendedPoint = cameraPos.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != mc.player) {
                if (entity.getBoundingBox().intersects(cameraPos, extendedPoint)) {
                    return entity;
                }
            }
        }
        return null;
    }
    /**
     * Checks if specified player is unable to be hit due to shield blocking
     * not sure, but I think this doesn't count the edges of the enemy hit-box
     */
    public static boolean isShieldFacingAway(LivingEntity player) {
        if (!player.isPlayer()) return true;
        if (mc.player == null) return false;
        Vec3d directionToPlayer = mc.player.getPos().subtract(player.getPos()).normalize();

        Vec3d facingDirection = new Vec3d(
                -Math.sin(Math.toRadians(player.getYaw())) * Math.cos(Math.toRadians(player.getPitch())),
                -Math.sin(Math.toRadians(player.getPitch())),
                Math.cos(Math.toRadians(player.getYaw())) * Math.cos(Math.toRadians(player.getPitch()))
        ).normalize();

        double dotProduct = facingDirection.dotProduct(directionToPlayer);

        return dotProduct < -0.06;
    }

    /**
     * Attacks Specified Enemy
     */
    public static void attackEnemy(boolean packetAttack, LivingEntity entity) {
        if (packetAttack) {
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            InputUtils.simulateClick(GLFW.GLFW_MOUSE_BUTTON_LEFT, 50);
        }
    }
    /**
     * Finds Closest Enemy
     */
    public static Entity getClosestEnemy(float range) {
        return mc.world.getPlayers()
                .stream()
                .filter(player -> player != mc.player && mc.player.distanceTo(player) <= range && SocialManager.isEnemy(player))
                .min(Comparator.comparingDouble(mc.player::distanceTo))
                .orElse(null);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
