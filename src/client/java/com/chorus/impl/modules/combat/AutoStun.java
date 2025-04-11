/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.player.PlayerUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.modules.other.MultiTask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

@ModuleInfo(
        name = "AutoStun",
        description = "Stuns Enemies Shields",
        category = ModuleCategory.COMBAT
)
public class AutoStun extends BaseModule implements QuickImports {
    private final SettingCategory conditional = new SettingCategory("Conditional Settings");
    private final SettingCategory delay = new SettingCategory("Delay Settings");
    private final SettingCategory shield = new SettingCategory("Shield Settings");

    private final RangeSetting<Integer> reactionDelay = new RangeSetting<>(delay, "Reaction Time", "Adjust Reaction Time", 0, 250, 10, 25);
    private final RangeSetting<Integer> initialDelay = new RangeSetting<>(delay, "Swap Delay", "Adjust Swap Delay", 0, 500, 10, 25);
    private final RangeSetting<Integer> attackDelay = new RangeSetting<>(delay, "Attack Delay", "Adjust Attack Delay", 0, 500, 10, 25);
    private final RangeSetting<Integer> swapBackDelay = new RangeSetting<>(delay, "Swap Back Delay", "Adjust Swapping Back Delay", 0, 500, 10, 25);

    private final ModeSetting swap = new ModeSetting(conditional, "Swap", "Swap Setting", "Swap", "Swap", "None");

    private final ModeSetting handleShield = new ModeSetting(shield, "Handle Shield", "Handles Player Shield", "None", "Multi-Task", "Unblock Shield", "None");

    private int oldSlot = -1;
    private boolean unblocked = false;

    private final TimerUtils reactionTimer = new TimerUtils();
    private final TimerUtils initialSwapTimer = new TimerUtils();
    private final TimerUtils actionTimer = new TimerUtils();
    private final TimerUtils swapBackTimer = new TimerUtils();

    @Override
    protected void onModuleDisabled() {
        oldSlot = -1;
    }

    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;


        if (mc.crosshairTarget instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof PlayerEntity target) {
            boolean isBlocking = target.isUsingItem() && target.getActiveItem().getItem() instanceof ShieldItem;
            boolean canBreakShield = !PlayerUtils.isShieldFacingAway(target);
            ItemStack mainHand = InventoryUtils.getMainHandItem();
            if (mc.player.getActiveItem().getItem() instanceof ShieldItem && mc.player.getActiveHand() == Hand.OFF_HAND) {
                if (handleShield.getValue().equals("Multi-Task") && !Chorus.getInstance().getModuleManager().isModuleEnabled(MultiTask.class)) {
                    return;
                }
                if (handleShield.getValue().equals("Unblock Shield")) {
                    mc.options.useKey.setPressed(false);
                    unblocked = true;
                }
            }
            if (isBlocking && canBreakShield) {
                if (mainHand.getItem() instanceof AxeItem) {
                    if (actionTimer.hasReached(attackDelay.getRandomValue().intValue())) {
                        InputUtils.simulateClick(0, 35);
                        actionTimer.reset();
                        swapBackTimer.reset();
                    }
                } else {
                    if (swap.getValue().equals("None")) return;
                    if (!reactionTimer.hasReached(reactionDelay.getRandomValue().intValue())) return;
                    int itemSlot = InventoryUtils.findItemWithPredicateInHotbar(itemStack -> itemStack.getItem() instanceof AxeItem);
                    if (itemSlot != -1 && oldSlot == -1 && initialSwapTimer.hasReached(initialDelay.getRandomValue().intValue())) {
                        oldSlot = mc.player.getInventory().selectedSlot;
                        InventoryUtils.swapToMainHand(itemSlot);
                        actionTimer.reset();
                    }
                }
            } else {
                reactionTimer.reset();
                if (oldSlot != -1 && swapBackTimer.hasReached(initialDelay.getRandomValue().intValue())) {
                    InventoryUtils.swapToMainHand(oldSlot);
                    oldSlot = -1;
                    if (unblocked) {
                        reblockShield();
                    }
                }
            }
        } else {
            initialSwapTimer.reset();
        }
    }

    public void reblockShield() {
        if (mc.player == null || mc.world == null) return;
        if (InputUtils.mouseDown(1)) {
            if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
                mc.options.useKey.setPressed(true);
                unblocked = false;
            }
        }
    }


    public AutoStun() {
        getSettingRepository().registerSettings(conditional, delay, swap, reactionDelay, initialDelay, attackDelay, swapBackDelay, handleShield, shield);
    }
}