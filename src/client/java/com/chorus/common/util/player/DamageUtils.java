package com.chorus.common.util.player;

import com.chorus.common.QuickImports;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;

import java.util.Set;
import java.util.function.BiFunction;

/*
    stolen from METEOR TEMPORARILY. will be rewritten in very near future
    just need this to work, either me (pathos) or ion will rewrite this before release.
    https://github.com/MeteorDevelopment/meteor-client/blob/d58787c741267cccc3d499e88e2da013acfddf1c/src/main/java/meteordevelopment/meteorclient/utils/entity/DamageUtils.java#L44
 */
public final class DamageUtils implements QuickImports {
    private static final float CRYSTAL_POWER = 12.0f;
    private static final float BED_POWER = 10.0f;
    private static final float ANCHOR_POWER = 10.0f;
    private static final float BLAST_RANGE_FACTOR = 2.0f;
    private static final float EXPLOSION_BASE_POWER = 7.0f;
    private static final float EXPLOSION_MODIFIER = 12.0f;

    public interface ExplosionRaycast extends BiFunction<ExplosionContext, BlockPos, BlockHitResult> {}

    public record ExplosionContext(Vec3d start, Vec3d end) {}

    public static final ExplosionRaycast DEFAULT_RAYCAST = (context, pos) -> {
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock().getBlastResistance() >= 600 ?
                state.getCollisionShape(mc.world, pos).raycast(context.start(), context.end(), pos) : null;
    };

    public static float calculateExplosionDamage(LivingEntity entity, Vec3d explosionPos, float power) {
        if (!isValidTarget(entity)) return 0f;
        return computeExplosionDamage(entity, entity.getPos(), entity.getBoundingBox(), explosionPos, power, DEFAULT_RAYCAST);
    }

    public static float calculateCrystalDamage(LivingEntity entity, Vec3d crystal) {
        return calculateExplosionDamage(entity, crystal, CRYSTAL_POWER);
    }

    public static float calculateBedDamage(LivingEntity entity, Vec3d bed) {
        return calculateExplosionDamage(entity, bed, BED_POWER);
    }

    public static float calculateAnchorDamage(LivingEntity entity, Vec3d anchor) {
        return calculateExplosionWithOverride(entity, anchor, ANCHOR_POWER, BlockPos.ofFloored(anchor), Blocks.AIR.getDefaultState());
    }

    public static float calculateCombatDamage(LivingEntity attacker, LivingEntity target) {
        float baseDamage = (float) attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        DamageSource source = getDamageSource(attacker);
        return applyDamageModifiers(attacker, target, attacker.getWeaponStack(), source, baseDamage);
    }

    public static float calculateWeaponDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon) {
        EntityAttributeInstance damageAttribute = createDamageAttribute(attacker, weapon);
        float baseDamage = (float) damageAttribute.getValue();
        DamageSource source = getDamageSource(attacker);
        return applyDamageModifiers(attacker, target, weapon, source, baseDamage);
    }

    public static float calculateFallDamage(LivingEntity entity) {
        if (shouldIgnoreFallDamage(entity)) return 0f;

        int surfaceY = getSurfaceHeight(entity);
        if (entity.getBlockY() >= surfaceY) return calculateFallReduction(entity, surfaceY);

        BlockHitResult result = raycastToGround(entity);
        return result.getType() == HitResult.Type.MISS ? 0f : calculateFallReduction(entity, result.getBlockPos().getY());
    }

    private static float computeExplosionDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, float power, ExplosionRaycast raycast) {
        double distance = targetPos.distanceTo(explosionPos);
        if (distance > power) return 0f;

        double exposure = getExposure(explosionPos, targetBox, raycast);
        double impact = (1.0 - distance / power) * exposure;
        float damage = (float) ((impact * impact + impact) / BLAST_RANGE_FACTOR * EXPLOSION_BASE_POWER * EXPLOSION_MODIFIER + 1);

        return applyDamageReductions(damage, target, mc.world.getDamageSources().explosion(null));
    }

    private static float getExposure(Vec3d source, Box box, ExplosionRaycast raycast) {
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
            int miss = 0;
            int hit = 0;
            // code pasted straight from mc itself LOL
            for (double k = 0.0; k <= 1.0; k += d) {
                for (double l = 0.0; l <= 1.0; l += e) {
                    for (double m = 0.0; m <= 1.0; m += f) {
                        double n = MathHelper.lerp(k, box.minX, box.maxX);
                        double o = MathHelper.lerp(l, box.minY, box.maxY);
                        double p = MathHelper.lerp(m, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(n + g, o, p + h);
                        if (mc.world.raycast(new RaycastContext(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS) {
                            ++miss;
                        }

                        ++hit;
                    }
                }
            }

            return (float) miss / (float) hit;
        } else {
            return 0.0F;
        }
    }

    private static boolean isLineOfSight(Vec3d start, Vec3d end) {
        return mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    private static float applyDamageReductions(float damage, LivingEntity entity, DamageSource source) {
        damage = applyDifficultyModifier(damage, source);
        damage = applyArmorReduction(damage, entity, source);
        damage = applyResistanceEffect(damage, entity);
        damage = applyProtectionEnchantments(damage, entity, source);
        return Math.max(damage, 0f);
    }

    private static float applyDifficultyModifier(float damage, DamageSource source) {
        if (!source.isScaledWithDifficulty()) return damage;
        return switch (mc.world.getDifficulty()) {
            case EASY -> Math.min(damage / 2 + 1, damage);
            case HARD -> damage * 1.5f;
            default -> damage;
        };
    }

    private static float applyArmorReduction(float damage, LivingEntity entity, DamageSource source) {
        return DamageUtil.getDamageLeft(
                entity,
                damage,
                source,
                (float) Math.floor(entity.getAttributeValue(EntityAttributes.ARMOR)),
                (float) entity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS)
        );
    }

    private static float applyResistanceEffect(float damage, LivingEntity entity) {
        StatusEffectInstance resistance = entity.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null) {
            int level = resistance.getAmplifier() + 1;
            damage *= (1.0f - level * 0.2f);
        }
        return Math.max(damage, 0f);
    }

    private static float applyProtectionEnchantments(float damage, LivingEntity entity, DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return damage;

        int totalProtection = 0;
        for (ItemStack armor : entity.getAllArmorItems()) {
            Object2IntMap<RegistryEntry<Enchantment>> enchants = new Object2IntOpenHashMap<>();
            getItemEnchantments(armor, enchants);
            totalProtection += calculateProtectionValue(enchants, source);
        }

        return DamageUtil.getInflictedDamage(damage, totalProtection);
    }

    private static void getItemEnchantments(ItemStack item, Object2IntMap<RegistryEntry<Enchantment>> enchants) {
        enchants.clear();
        if (item.isEmpty()) return;

        Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> entries = item.getItem() == Items.ENCHANTED_BOOK ?
                item.get(DataComponentTypes.STORED_ENCHANTMENTS).getEnchantmentEntries() :
                item.getEnchantments().getEnchantmentEntries();

        entries.forEach(entry -> enchants.put(entry.getKey(), entry.getIntValue()));
    }

    private static int calculateProtectionValue(Object2IntMap<RegistryEntry<Enchantment>> enchants, DamageSource source) {
        int protection = 0;

        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(enchants)) {
            if (entry.getKey().matchesKey(Enchantments.PROTECTION)) {
                protection += entry.getIntValue();
            }
            if (source.isIn(DamageTypeTags.IS_FIRE) && entry.getKey().matchesKey(Enchantments.FIRE_PROTECTION)) {
                protection += 2 * entry.getIntValue();
            }
            if (source.isIn(DamageTypeTags.IS_EXPLOSION) && entry.getKey().matchesKey(Enchantments.BLAST_PROTECTION)) {
                protection += 2 * entry.getIntValue();
            }
            if (source.isIn(DamageTypeTags.IS_PROJECTILE) && entry.getKey().matchesKey(Enchantments.PROJECTILE_PROTECTION)) {
                protection += 2 * entry.getIntValue();
            }
            if (source.isIn(DamageTypeTags.IS_FALL) && entry.getKey().matchesKey(Enchantments.FEATHER_FALLING)) {
                protection += 3 * entry.getIntValue();
            }
        }

        return protection;
    }

    private static boolean isValidTarget(LivingEntity entity) {
        if (entity == null) return false;
        if (entity instanceof PlayerEntity player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return true;
    }

    private static float calculateFallReduction(LivingEntity entity, int surfaceY) {
        int fallHeight = (int) (entity.getY() - surfaceY + entity.fallDistance - 3.0);
        StatusEffectInstance jumpBoost = entity.getStatusEffect(StatusEffects.JUMP_BOOST);
        if (jumpBoost != null) {
            fallHeight -= jumpBoost.getAmplifier() + 1;
        }
        return applyDamageReductions(fallHeight, entity, mc.world.getDamageSources().fall());
    }

    private static boolean shouldIgnoreFallDamage(LivingEntity entity) {
        return (entity instanceof PlayerEntity player && player.getAbilities().flying) ||
                entity.hasStatusEffect(StatusEffects.SLOW_FALLING) ||
                entity.hasStatusEffect(StatusEffects.LEVITATION);
    }

    private static int getSurfaceHeight(LivingEntity entity) {
        return mc.world.getWorldChunk(entity.getBlockPos())
                .getHeightmap(Heightmap.Type.MOTION_BLOCKING)
                .get(entity.getBlockX() & 15, entity.getBlockZ() & 15);
    }

    private static BlockHitResult raycastToGround(LivingEntity entity) {
        return mc.world.raycast(new RaycastContext(
                entity.getPos(),
                new Vec3d(entity.getX(), mc.world.getBottomY(), entity.getZ()),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.WATER,
                entity
        ));
    }

    private static DamageSource getDamageSource(LivingEntity attacker) {
        return attacker instanceof PlayerEntity player ?
                mc.world.getDamageSources().playerAttack(player) :
                mc.world.getDamageSources().mobAttack(attacker);
    }

    private static EntityAttributeInstance createDamageAttribute(LivingEntity attacker, ItemStack weapon) {
        EntityAttributeInstance original = attacker.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE);
        EntityAttributeInstance copy = new EntityAttributeInstance(EntityAttributes.ATTACK_DAMAGE, o -> {});

        copy.setBaseValue(original.getBaseValue());
        original.getModifiers().forEach(copy::addTemporaryModifier);
        copy.removeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID);

        AttributeModifiersComponent modifiers = weapon.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            modifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry == EntityAttributes.ATTACK_DAMAGE) copy.updateModifier(modifier);
            });
        }

        return copy;
    }


    private static float calculateExplosionWithOverride(LivingEntity entity, Vec3d pos, float power, BlockPos override, BlockState state) {
        ExplosionRaycast raycast = (context, blockPos) -> {
            BlockState blockState = blockPos.equals(override) ? state :
                    mc.world.getBlockState(blockPos);
            return blockState.getBlock().getBlastResistance() >= 600 ?
                    blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos) : null;
        };
        return calculateExplosionDamage(entity, pos, power);
    }

    private static float applyDamageModifiers(LivingEntity attacker, LivingEntity target, ItemStack weapon, DamageSource source, float baseDamage) {
        float damage = baseDamage;

        Object2IntMap<RegistryEntry<Enchantment>> enchantments = new Object2IntOpenHashMap<>();
        getItemEnchantments(weapon, enchantments);

        float enchantDamage = 0f;

        int sharpness = getEnchantmentLevel(enchantments, Enchantments.SHARPNESS);
        if (sharpness > 0) {
            enchantDamage += 1 + 0.5f * (sharpness - 1);
        }

        int baneOfArthropods = getEnchantmentLevel(enchantments, Enchantments.BANE_OF_ARTHROPODS);
        if (baneOfArthropods > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            enchantDamage += 2.5f * baneOfArthropods;
        }

        int impaling = getEnchantmentLevel(enchantments, Enchantments.IMPALING);
        if (impaling > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
            enchantDamage += 2.5f * impaling;
        }

        int smite = getEnchantmentLevel(enchantments, Enchantments.SMITE);
        if (smite > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_SMITE)) {
            enchantDamage += 2.5f * smite;
        }

        if (attacker instanceof PlayerEntity player) {
            float cooldown = player.getAttackCooldownProgress(0.5f);
            damage *= 0.2f + cooldown * cooldown * 0.8f;
            enchantDamage *= cooldown;

            if (weapon.getItem() instanceof MaceItem mace) {
                float bonusDamage = mace.getBonusAttackDamage(target, damage, source);
                if (bonusDamage > 0f) {
                    int density = getEnchantmentLevel(weapon, Enchantments.DENSITY);
                    if (density > 0) {
                        bonusDamage += 0.5f * attacker.fallDistance;
                    }
                    damage += bonusDamage;
                }
            }

            if (cooldown > 0.9f &&
                    attacker.fallDistance > 0f &&
                    !attacker.isOnGround() &&
                    !attacker.isClimbing() &&
                    !attacker.isTouchingWater() &&
                    !attacker.hasStatusEffect(StatusEffects.BLINDNESS) &&
                    !attacker.hasVehicle()) {
                damage *= 1.5f;
            }
        }

        return applyDamageReductions(damage + enchantDamage, target, source);
    }

    public static int getEnchantmentLevel(ItemStack stack, RegistryKey<Enchantment> enchantment) {
        if (stack.isEmpty()) return 0;
        Object2IntMap<RegistryEntry<Enchantment>> enchantments = new Object2IntArrayMap<>();
        getItemEnchantments(stack, enchantments);
        return getEnchantmentLevel(enchantments, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> enchantments, RegistryKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : Object2IntMaps.fastIterable(enchantments)) {
            if (entry.getKey().matchesKey(enchantment)) return entry.getIntValue();
        }
        return 0;
    }
}