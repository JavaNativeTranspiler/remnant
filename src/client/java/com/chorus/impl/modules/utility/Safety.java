
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.DamageUtils;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.world.BlockUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(
        name = "Safety",
        description = "Automatically Swaps to shield or totem when in danger",
        category = ModuleCategory.UTILITY
)
public class Safety extends BaseModule implements QuickImports {
    private final MultiSetting dangers = new MultiSetting("Considered Dangers", "Decide what dangers to consider when calculating damage",
            "Beds"
            , "Players"
            , "Anchors"
            , "Carts"
            , "Fall Damage"
            , "Crystals");


    float maxCrystalDamage = 0;
    float maxAnchorDamage = 0;
    float maxMeleeDamage = 0;
    float maxCartDamage = 0;
    float maxFallDamage = 0;
    float maxBedDamage = 0;
    int slot = -1;
    boolean saved = false;

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.world == null || mc.player == null) return;
            if (mc.currentScreen instanceof HandledScreen) return;
            if (InventoryUtils.findItemInHotBar(Items.TOTEM_OF_UNDYING) == -1) return;
            if (slot != -1 && saved) {
                mc.player.getInventory().selectedSlot = slot;
                slot = -1;
                saved = false;
            }
            for (Entity real : mc.world.getEntities()) {
                // Crystals
                if (real instanceof EndCrystalEntity) {
                    if (real.distanceTo(mc.player) < 6) {
                        if (mc.world.getPlayers().stream().anyMatch(player -> player.distanceTo(real) <= 4)) {
                            if (DamageUtils.calculateCrystalDamage(mc.player, real.getPos()) > maxCrystalDamage) {
                                maxCrystalDamage = DamageUtils.calculateCrystalDamage(mc.player, real.getPos());
                            }
                        }
                    }
                }
                // Player Entity
                if (real instanceof PlayerEntity) {
                    if (real.distanceTo(mc.player) < 6) {
                        if (DamageUtils.calculateCombatDamage((LivingEntity) real, mc.player) > maxMeleeDamage) {
                            maxMeleeDamage = DamageUtils.calculateCombatDamage((LivingEntity) real, mc.player);
                        }
                    }
                }
                // Mine carts
                if (real instanceof TntMinecartEntity) {
                    if (real.distanceTo(mc.player) < 6) {
                        if (DamageUtils.calculateExplosionDamage(mc.player, real.getPos(), 10f) > maxCartDamage) {
                            maxCartDamage = DamageUtils.calculateExplosionDamage(mc.player, real.getPos(), 10f);
                        }
                    }
                }
            }
            // Fall Damage
            if (DamageUtils.calculateFallDamage(mc.player) > maxFallDamage) {
                maxFallDamage = DamageUtils.calculateFallDamage(mc.player);
            }
            int radius = 5;
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        // Anchors
                        BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                        if (BlockUtils.isBlockType(pos, Blocks.RESPAWN_ANCHOR)) {
                            int charges = mc.world.getBlockState(pos).get(net.minecraft.block.RespawnAnchorBlock.CHARGES);
                            if (charges != 0) {
                                float damage = DamageUtils.calculateAnchorDamage(mc.player, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                                if (damage > maxAnchorDamage) {
                                    maxAnchorDamage = damage;
                                }
                            }
                        }
                        // Beds
                        if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
                            float damage = DamageUtils.calculateBedDamage(mc.player, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
                            if (damage > maxBedDamage) {
                                maxBedDamage = damage;
                            }
                        }
                    }
                }
            }
            if (dangers.getSpecificValue("Beds") && checkHealth(maxBedDamage)) doubleHand();
            if (dangers.getSpecificValue("Anchors") && checkHealth(maxAnchorDamage)) doubleHand();
            if (dangers.getSpecificValue("Players") && checkHealth(maxMeleeDamage)) doubleHand();
            if (dangers.getSpecificValue("Crystals") && checkHealth(maxCrystalDamage)) doubleHand();
            if (dangers.getSpecificValue("Carts") && checkHealth(maxCartDamage)) doubleHand();
            if (dangers.getSpecificValue("Fall Damage") && checkHealth(maxFallDamage)) doubleHand();
            maxCrystalDamage = 0;
            maxAnchorDamage = 0;
            maxMeleeDamage = 0;
            maxCartDamage = 0;
            maxFallDamage = 0;
            maxBedDamage = 0;
        }
    }

    private boolean checkHealth(float maxDamage) {
        return (mc.player.getHealth() + mc.player.getAbsorptionAmount()) - maxDamage < 0;
    }

    private void doubleHand() {
        if (InventoryUtils.getOffHandItem().isEmpty()) {
            if (slot == -1) {
                slot = mc.player.getInventory().selectedSlot;
                saved = true;
                InventoryUtils.swap(Items.TOTEM_OF_UNDYING);
            }
        }
    }

    public Safety() {
        getSettingRepository().registerSettings(dangers);
    }
}
