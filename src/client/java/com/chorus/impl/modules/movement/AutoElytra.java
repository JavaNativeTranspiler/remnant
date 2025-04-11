
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.movement;

import cc.polymorphism.eventbus.EventPriority;
import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

import java.util.Comparator;

@ModuleInfo(
        name = "AutoElytra",
        description = "Automatically Uses Fireworks",
        category = ModuleCategory.MOVEMENT
)

public class AutoElytra extends BaseModule implements QuickImports {
    private final SettingCategory behaviorCategory = new SettingCategory("Behavior");
    private final RangeSetting<Double> range = new RangeSetting<>(behaviorCategory, "Range", "Range to firework", 0.0, 50.0, 8.0, 20.0);


    boolean swap = false;
    int slot = -1;

    @RegisterEvent(value = EventPriority.LOW)
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            Entity enemy = getClosestPlayerEntityWithinRange(50);

            if (enemy == null) return;
            if (!mc.player.isGliding()) {
                if (mc.player.isOnGround()) {
                    mc.options.jumpKey.setPressed(true);
                } else {
                    if (mc.player.fallDistance > 0) {
                        mc.options.jumpKey.setPressed(true);
                        mc.player.fallDistance = 0;
                    } else {
                        mc.options.jumpKey.setPressed(false);
                    }
                }
                return;
            } else {
                if (mc.player.age % 5 == 0)
                    InputUtils.simulateClick(1, 35);
            }
            if (mc.player.distanceTo(enemy) < range.getValueMax() && mc.player.distanceTo(enemy) > range.getValueMin()) {
                if (!swap) {
                    if (InventoryUtils.findItemInHotBar(Items.FIREWORK_ROCKET) == -1) return;
                    ItemStack firework = mc.player.getInventory().getStack(InventoryUtils.findItemInHotBar(Items.FIREWORK_ROCKET));

                    if (firework == null) return;
                    slot = mc.player.getInventory().selectedSlot;
                    if (mc.player.age % 3 != 0) return;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(InventoryUtils.findItemInHotBar(Items.FIREWORK_ROCKET)));
                    mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                    //ChatUtils.sendFormattedMessage("TEst");
                    //swap = true;
                }
            } else {
                if (swap) {
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
                    //ChatUtils.sendFormattedMessage("TE!st");
                    slot = -1;
                    swap = false;
                }
            }
        }
    }

    public Entity getClosestPlayerEntityWithinRange(float range) {
        return mc.world.getPlayers()
                .stream()
                .filter(player -> player != mc.player
                        && mc.player.distanceTo(player) <= range
                        && SocialManager.isTargetedPlayer(player) == player)
                .min(Comparator.comparingDouble(mc.player::distanceTo))
                .orElse(null);
    }

    @Override
    protected void onModuleDisabled() {
        swap = false;
        slot = -1;
    }

    public AutoElytra() {
        getSettingRepository().registerSettings(behaviorCategory, range);
    }

}
