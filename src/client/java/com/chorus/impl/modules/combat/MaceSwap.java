package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.impl.events.player.SwingEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.item.AxeItem;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.EntityHitResult;

@ModuleInfo(
        name = "MaceSwap",
        description = "swap",
        category = ModuleCategory.COMBAT
)
public class MaceSwap extends BaseModule implements QuickImports {
    private final BooleanSetting swapSilently = new BooleanSetting("Swap Silently", "Swaps to a mace silently", false);
    private int lastSlot = -1;

    @RegisterEvent
    private void onUpdate(TickEvent event) {
        if (mc.player == null || lastSlot == -1) return;
        if (event.getMode() != TickEvent.Mode.PRE) return;
        swap(lastSlot);
        lastSlot = -1;
    }

    @RegisterEvent
    private void SwingEvent(SwingEvent event) {
        if (mc.player == null || mc.world == null) return;
        int slot = InventoryUtils.findItemWithPredicateInHotbar(itemStack -> itemStack.getItem() instanceof MaceItem);

        if (slot == mc.player.getInventory().selectedSlot) return;
        if (slot == -1 || lastSlot != -1) return;
        if (InventoryUtils.getMainHandItem().getItem() instanceof AxeItem) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult)) return;

        lastSlot = mc.player.getInventory().selectedSlot;
        swap(slot);
    }

    public void swap(int slot) {
        //ChatUtils.sendFormattedMessage("Swapped");
        if (swapSilently.getValue()) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        } else {
            InventoryUtils.swapToMainHand(slot);
        }
    }

    public MaceSwap() {
        getSettingRepository().registerSettings(swapSilently);
    }
}
