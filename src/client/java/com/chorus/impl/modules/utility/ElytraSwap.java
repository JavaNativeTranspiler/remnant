/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;

@ModuleInfo(
        name = "ElytraSwap",
        description = "Swaps Elytra Automatically",
        category = ModuleCategory.UTILITY
)
public class ElytraSwap extends BaseModule implements QuickImports {
    private final BooleanSetting fireworkBoost = new BooleanSetting("Boost With Fireworks", "Uses a firework for you", false);

    boolean startedWithElytra = false;
    int originalSlot = -1;
    boolean swapped = false;
    boolean resetJump = false;
    
    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        var mainHand = InventoryUtils.getMainHandItem().getItem();
        boolean wearingElytra = mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA;
        if (!swapped) {
            if (holdingChestplate() && startedWithElytra || mainHand.equals(Items.ELYTRA) && !startedWithElytra) {
                InputUtils.simulateClick(1, 50);
                swapped = true;
            } else {
                int slot = startedWithElytra ? chestplateSlot() : InventoryUtils.findItemWithPredicateInHotbar(ItemStack -> ItemStack.getItem().equals(Items.ELYTRA));
                if (slot == -1) {
                    Chorus.getInstance().getModuleManager().toggleModule(ElytraSwap.class);
                } else {
                    InventoryUtils.swapToMainHand(slot);
                }
            }
        }

        if (startedWithElytra != wearingElytra && swapped) {
            if (fireworkBoost.getValue() && wearingElytra) {
                int slot = InventoryUtils.findItemWithPredicateInHotbar(ItemStack -> ItemStack.getItem().equals(Items.FIREWORK_ROCKET));
                if (!mainHand.equals(Items.FIREWORK_ROCKET)) {
                    if (slot == -1) {
                        resetJump = true;
                        reset();
                    } else {
                        InventoryUtils.swapToMainHand(slot);
                    }
                }

                if (mc.player.isGliding()) {
                    if (mainHand.equals(Items.FIREWORK_ROCKET)) {
                        InputUtils.simulateClick(1, 35);
                        if (originalSlot != -1) InventoryUtils.swapToMainHand(originalSlot);
                    }
                    if (mc.player.getGlidingTicks() == 2) reset();
                } else {
                    resetJump = true;
                    mc.options.jumpKey.setPressed(mc.player.getVelocity().y < 0 || mc.player.isOnGround());
                    mc.player.fallDistance = 0;
                }
            } else {
                if (originalSlot != -1) InventoryUtils.swapToMainHand(originalSlot);

                reset();
            }
        }
    }

    private void reset() {
        swapped = false;
        Chorus.getInstance().getModuleManager().toggleModule(ElytraSwap.class);
        if (resetJump)
            mc.options.jumpKey.setPressed(false);
        originalSlot = -1;
    }

    private boolean holdingChestplate() {
        var mainHand = InventoryUtils.getMainHandItem().getItem();
        return mainHand == Items.NETHERITE_CHESTPLATE || mainHand == Items.CHAINMAIL_CHESTPLATE || mainHand == Items.IRON_CHESTPLATE || mainHand == Items.GOLDEN_CHESTPLATE || mainHand == Items.DIAMOND_CHESTPLATE || mainHand == Items.LEATHER_CHESTPLATE;
    }

    private int chestplateSlot() {
        final PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i <= 8; i++) {
            if (inv.getStack(i).isOf(Items.NETHERITE_CHESTPLATE) || inv.getStack(i).isOf(Items.CHAINMAIL_CHESTPLATE) || inv.getStack(i).isOf(Items.IRON_CHESTPLATE) || inv.getStack(i).isOf(Items.GOLDEN_CHESTPLATE) || inv.getStack(i).isOf(Items.DIAMOND_CHESTPLATE) || inv.getStack(i).isOf(Items.LEATHER_CHESTPLATE)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onModuleEnabled() {
        swapped = false;
        if (!(mc.player.getMainHandStack().getItem() == Items.ELYTRA))
            originalSlot = mc.player.getInventory().selectedSlot;
        startedWithElytra = mc.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA;
    }

    public ElytraSwap() {
        getSettingRepository().registerSettings(fireworkBoost);
    }
}