/**
 * Created: 2/25/2025
 */

package com.chorus.common.util.math;

import com.chorus.api.system.prot.MathProt;
import com.chorus.common.QuickImports;
import lombok.Getter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ProjectileUtils implements QuickImports {
    private static final List<Vec3d> landingPositions = new ArrayList<>();
    private static final World world = mc.world;

    public static void trackProjectile(ItemStack itemStack, LivingEntity shooter) {
        EntityType<? extends ProjectileEntity> entityType = getEntityTypeFromItem(itemStack.getItem());
        if (entityType == null || world == null) return;

        ProjectileEntity projectile = entityType.create(world, SpawnReason.COMMAND);
        if (projectile == null) return;

        projectile.setPosition(shooter.getX(), shooter.getEyeY(), shooter.getZ());
        setProjectileVelocity(projectile, shooter);
        simulateProjectilePath(projectile);
    }

    private static EntityType<? extends ProjectileEntity> getEntityTypeFromItem(Item item) {
        if (item instanceof BowItem || item instanceof CrossbowItem) return EntityType.ARROW;
        if (item instanceof SnowballItem) return EntityType.SNOWBALL;
        if (item instanceof EggItem) return EntityType.EGG;
        if (item instanceof EnderPearlItem) return EntityType.ENDER_PEARL;
        if (item instanceof ExperienceBottleItem) return EntityType.EXPERIENCE_BOTTLE;
        if (item instanceof PotionItem) return EntityType.POTION;
        if (item instanceof TridentItem) return EntityType.TRIDENT;
        return null;
    }

    private static void setProjectileVelocity(ProjectileEntity projectile, LivingEntity shooter) {
        float pitch = shooter.getPitch();
        float yaw = shooter.getYaw();
        float velocity = 3.0F;

        float x = -MathHelper.sin(yaw * (float) MathProt.PI() / 180.0F) * MathHelper.cos(pitch * (float) MathProt.PI() / 180.0F);
        float y = -MathHelper.sin(pitch * (float) MathProt.PI() / 180.0F);
        float z = MathHelper.cos(yaw * (float) MathProt.PI() / 180.0F) * MathHelper.cos(pitch * (float) MathProt.PI() / 180.0F);

        projectile.setVelocity(new Vec3d(x * velocity, y * velocity, z * velocity));
    }

    private static void simulateProjectilePath(ProjectileEntity projectile) {
        Vec3d position = projectile.getPos();
        Vec3d velocity = projectile.getVelocity();
        double gravity = 0.006;

        while (position.y > 0) {
            velocity = velocity.multiply(0.99);
            velocity = velocity.add(0, -gravity, 0);
            position = position.add(velocity);
            BlockPos blockPos = convertToBlockPos(position);

            if (!world.isAir(blockPos)) break;
            if (world.getFluidState(blockPos).isStill()) {
                velocity = velocity.multiply(0.8);
            }
        }

        landingPositions.add(position);
    }

    public static BlockPos convertToBlockPos(Vec3d vec3d) {
        return new BlockPos((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
    }

    public static List<Vec3d> getLandingPositions() {
        return new ArrayList<>(landingPositions);
    }
}