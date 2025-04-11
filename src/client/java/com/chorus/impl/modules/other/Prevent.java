
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.system.notification.NotificationManager;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.player.PlayerUtils;
import com.chorus.common.util.world.SimulatedPlayer;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.events.player.ItemUseEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(
    name        = "Prevent",
    description = "Prevents Bad Actions",
    category    = ModuleCategory.OTHER
)
public class Prevent extends BaseModule implements QuickImports {
    public final MultiSetting prevent = new MultiSetting("Prevent", "Set activation conditions",
            "Shield Hit",
            "Missed Glowstone",
            "Useless Anchor Charges",
            "Chest Opening",
            "Pearl Onto Crystal Hitboxes",
            "Movement",
            "Off-Handing",
            "Dropping");


    public final MultiSetting movementActions = new MultiSetting("Actions", "Choose what to prevent walking into",
            "Void",
            "Lava",
            "Cobweb");
    public final MultiSetting slots = new MultiSetting("Slot", "Choose Specific Slots", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    @RegisterEvent
    private void ItemUseEvent(ItemUseEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        HitResult hitResult = mc.crosshairTarget;
        BlockHitResult blockHitResult = (BlockHitResult) mc.crosshairTarget;
        BlockPos blockPos = blockHitResult.getBlockPos();

        Entity crystal = raycastCrystal(6.0f);
        if (crystal != null && InventoryUtils.getMainHandItem().getItem() instanceof EnderPearlItem && !crystal.getBoundingBox().intersects(mc.player.getBoundingBox())) {
            if (prevent.getSpecificValue("Pearl Onto Crystal Hitboxes")) {
                event.setCancelled(true);
            }
        }
        if (blockPos == null) return;
        if (mc.world.getBlockState(blockPos).getBlock() instanceof EnderChestBlock || mc.world.getBlockState(blockPos).getBlock() instanceof ChestBlock || mc.world.getBlockState(blockPos).getBlock() instanceof TrappedChestBlock) {
            if (prevent.getSpecificValue("Chest Opening")) {
                event.setCancelled(true);
            }
        }

        if (InventoryUtils.getMainHandItem().getItem() == Items.GLOWSTONE) {
            if (mc.world.getBlockState(blockPos).getBlock() instanceof RespawnAnchorBlock) {
                int charges = mc.world.getBlockState(blockPos).get(net.minecraft.block.RespawnAnchorBlock.CHARGES);
                if (charges > 0) {
                    if (prevent.getSpecificValue("Useless Anchor Charges")) event.setCancelled(true);
                }
            } else {
                if (mc.world.getBlockState(blockPos.add(1, 1, 0)).getBlock() instanceof RespawnAnchorBlock) return;
                if (mc.world.getBlockState(blockPos.add(0, 1, 1)).getBlock() instanceof RespawnAnchorBlock) return;
                if (mc.world.getBlockState(blockPos.add(-1, 1, 0)).getBlock() instanceof RespawnAnchorBlock) return;
                if (mc.world.getBlockState(blockPos.add(0, 1, -1)).getBlock() instanceof RespawnAnchorBlock) return;

                if (prevent.getSpecificValue("Missed Glowstone")) event.setCancelled(true);

            }
        }
    }
    @RegisterEvent
    private void attackEventListener(AttackEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (event.getTarget() instanceof PlayerEntity player) {
            if (player.isBlocking() && !PlayerUtils.isShieldFacingAway(player)) {
                if (prevent.getSpecificValue("Shield Hit")) event.setCancelled(true);
            }
        }
    }
    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;

        for (int i = 0; i <= 8; i++) {
            if (slots.getSpecificValue("" + (i + 1))) {
                if (mc.player.getInventory().selectedSlot == (i)) {
                    // TODO figure out how to make it so u cant drop shit
                    if (prevent.getSpecificValue("Dropping")) {
                        mc.options.dropKey.setPressed(false);
                    }
                    if (prevent.getSpecificValue("Off-Handing")) {
                        mc.options.swapHandsKey.setPressed(false);
                    }
                }
            }
        }
        SimulatedPlayer simulator = new SimulatedPlayer(mc.player);
        simulator.setInput(
                mc.options.forwardKey.isPressed(),
                mc.options.backKey.isPressed(),
                mc.options.leftKey.isPressed(),
                mc.options.rightKey.isPressed(),
                mc.options.jumpKey.isPressed(), mc.player.isSprinting());
        for (int i = 0; i <= 5; i++) {
            simulator.tick();
        }
        if (prevent.getSpecificValue("Movement")) {
            if (movementActions.getSpecificValue("Void")) {

                for (float i = (float) simulator.getPosition().y; i >= -64; i--) {
                    if (mc.world.getBlockState(BlockPos.ofFloored(new Vec3d(simulator.getPosition().x, i, simulator.getPosition().z))).isAir()) {
                    } else {

                        NotificationManager.notificationManager.addNotification("Warning", "You Are Close To Falling Into the void", 2500);
                        return;
                    }
                }
            }
            if (movementActions.getSpecificValue("Cobweb")) {
                if (mc.world.getBlockState(BlockPos.ofFloored(simulator.getPosition())).getBlock() instanceof CobwebBlock) {
                    NotificationManager.notificationManager.addNotification("Warning", "u gonna be in the cobweb ho", 2500);
                }
            }
            if (movementActions.getSpecificValue("Lava")) {
                if (mc.world.getFluidState(BlockPos.ofFloored(simulator.getPosition())).getFluid() instanceof LavaFluid) {
                    NotificationManager.notificationManager.addNotification("Warning", "u gonna be in the lava ho", 2500);
                }
            }
        }
    }
    public int convertToSlot(String number) {
        int num = Integer.parseInt(number);
        if (num >= 1 && num <= 9) {
            return 35 + num;
        }
        return -1;
    }
    private Entity raycastCrystal(double range) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d viewVector = mc.player.getRotationVecClient();
        Vec3d extendedPoint = cameraPos.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
                if (entity.getBoundingBox().expand(0.225).intersects(cameraPos, extendedPoint)) {
                    return entity;
                }
            }
        }

        return null;
    }
    public Prevent() {
        slots.setRenderCondition(() -> (prevent.getSpecificValue("Off-Handing") || prevent.getSpecificValue("Dropping")));
        movementActions.setRenderCondition(() -> prevent.getSpecificValue("Movement"));
        getSettingRepository().registerSettings(prevent, movementActions, slots);
    }
}
