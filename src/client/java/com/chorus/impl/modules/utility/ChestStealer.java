
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(
    name        = "ChestStealer",
    description = "Steals Items Out of Chests",
    category    = ModuleCategory.UTILITY
)
public class ChestStealer extends BaseModule implements QuickImports {

    private final RangeSetting<Integer> delay = new RangeSetting<>("Steal Delay", "Delay to Steal Items", 0, 250, 100, 100);
    private final BooleanSetting relevantItems = new BooleanSetting("Only Relevant Items", "", false);
    private final TimerUtils waitTimer = new TimerUtils();
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
       if (event.getMode().equals(TickEvent.Mode.PRE)) {
           if (mc.world == null | mc.player == null) return;
           if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler inventory) ) {
               return;
           }

           if (InventoryUtils.isInventoryFull()) return;
           for (int i = 0; i < inventory.getInventory().size(); i++) {
               if (inventory.getSlot(i).getStack().isEmpty()) {
                   if (i == inventory.getInventory().size() - 1) {
                       mc.player.closeHandledScreen();
                   }
                   continue;
               }
               if (!waitTimer.hasReached(delay.getRandomValue().intValue())) return;
               if (isRelevant(inventory.getSlot(i).getStack()) || !relevantItems.getValue()) {
                   mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                   waitTimer.reset();
               }
           }
       }
    }


    private boolean isRelevant(ItemStack stack) {
            return stack.getItem() instanceof SwordItem
                    || stack.getUseAction().equals(UseAction.EAT)
                    || stack.getItem() instanceof ArmorItem
                    || stack.getItem() instanceof MiningToolItem
                    || stack.getItem() instanceof EnderPearlItem
                    || stack.getItem() instanceof BlockItem
                    || stack.getItem() instanceof FishingRodItem
                    || stack.getItem() instanceof PotionItem;
    }
    public ChestStealer() {
        getSettingRepository().registerSettings(delay, relevantItems);
    }
}
