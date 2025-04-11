
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;

@ModuleInfo(
    name        = "AutoArmor",
    description = "Automatically Equips The Best Armor",
    category    = ModuleCategory.UTILITY
)
public class AutoArmor extends BaseModule implements QuickImports {
//
//    private final RangeSetting<Integer> delay = new RangeSetting<>("Delay", "Delay to Equip Armor", 0, 500, 100, 100);
//    private final BooleanSetting throwAfterSwapping = new BooleanSetting("Throw After Swapping", "Throw After Swapping", false);
//    private final TimerUtils waitTimer = new TimerUtils();
//    @RegisterEvent
//    private void TickEventListener(TickEvent event) {
//       if (event.getMode().equals(TickEvent.Mode.PRE)) {
//           if (mc.world == null || mc.player == null) return;
//           if (!(mc.currentScreen instanceof InventoryScreen screen)) {
//               return;
//           }
//           ScreenHandler screenHandler = mc.player.currentScreenHandler;
//           mc.player.getInventory().updateItems();
//           if (!waitTimer.hasReached(delay.getRandomValue().floatValue() + 50)) return;
//
//           for (Slot itemSlot : screenHandler.slots) {
//               if (itemSlot.getStack().isEmpty()) continue;
//               if (!(itemSlot.getStack().getItem() instanceof ArmorItem)) continue;
//               ItemStack armor = itemSlot.getStack();
//               if (armor.getItem() instanceof ArmorItem armorItem) {
//                   int targetSlot = switch (armorItem.getDefaultStack().get(DataComponentTypes.EQUIPPABLE).slot()) {
//                       case HEAD -> 5;  // Helmet slot
//                       case CHEST -> 6; // Chestplate slot
//                       case LEGS -> 7;  // Leggings slot
//                       case FEET -> 8;  // Boots slot
//                       default -> -1;   // Invalid (should never happen)
//                   };
//
//                   if (targetSlot == -1) continue;
//
//                   boolean isWearing = !mc.player.getEquippedStack(armorItem.getDefaultStack().get(DataComponentTypes.EQUIPPABLE).slot()).isEmpty();
//                   if (!isWearing || isProtectedMore(armor, armorItem.getDefaultStack().get(DataComponentTypes.EQUIPPABLE).slot())) {
//                       swap(itemSlot.id, targetSlot);
//                       return;
//                   }
//               }
//           }
//       }
//    }
//    private void swap(int fromSlot, int toSlot) {
//        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
//        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, toSlot, 0, SlotActionType.PICKUP, mc.player);
//        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
//            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
//            if (throwAfterSwapping.getValue()) {
//                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.THROW, mc.player);
//            }
//        }
//        waitTimer.reset();
//    }
//    private boolean isProtectedMore(ItemStack stack, EquipmentSlot slot) {
//        ItemStack armorSlot = mc.player.getEquippedStack(slot);
//        (ArmorItem) stack.getItem().getDefaultStack().get(DataComponentTypes.).
//        float stackTotal = MathUtils.clamp((getProtectionValue(stack) * 0.2f), 1, 1000) * (((ArmorItem) stack.getItem()).getProtection());
//        float armorTotal = MathUtils.clamp((getProtectionValue(armorSlot) * 0.2f), 1, 1000) * (((ArmorItem) armorSlot.getItem()).getProtection());
//        if (stackTotal == armorTotal) {
//            return (((ArmorItem) armorSlot.getItem()).getDefaultStack().get) > (((ArmorItem) stack.getItem()).getToughness());
//        }
//        return stackTotal > armorTotal;
//    }
//
//    private float getProtectionValue(ItemStack stack) {
//        if (stack == null || !(stack.getItem() instanceof ArmorItem armorItem)) return 0;
//
//        int protLevel = EnchantmentHelper.getLevel(
//                mc.world.getRegistryManager().ge(Enchantments.PROTECTION.getRegistryRef())
//                        .getEntry(Enchantments.PROTECTION).get(), stack
//        );
//
//        return (1 + protLevel) * armorItem.getProtection();
//    }
//    public AutoArmor() {
//        getSettingRepository().registerSettings(delay, throwAfterSwapping);
//    }
}
