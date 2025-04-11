
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.DamageUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

@ModuleInfo(
    name        = "InventoryManager",
    description = "Manages Your Inventory",
    category    = ModuleCategory.UTILITY
)
public class InventoryManager extends BaseModule implements QuickImports {

    private final RangeSetting<Integer> delay = new RangeSetting<>("Delay", "Delay to Manage Items", 0, 500, 250, 200);
    private final BooleanSetting throwJunk = new BooleanSetting("Throw Junk", "Throw Not Needed Items", false);
    private final BooleanSetting goldTools = new BooleanSetting("Ignore Gold", "Set golden tools priority less when managing inventory", false);
    private final ModeSetting swordSlot = new ModeSetting("Sword Slot", "Choose Which Slot the sword is in", "1", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private final ModeSetting pickaxeSlot = new ModeSetting("Pickaxe Slot", "Choose Which Slot the sword is in", "3", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private final ModeSetting axeSlot = new ModeSetting("Axe Slot", "Choose Which Slot the sword is in", "4", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private final ModeSetting shovelSlot = new ModeSetting("Shovel Slot", "Choose Which Slot the sword is in", "5", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private final TimerUtils waitTimer = new TimerUtils();
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.world == null | mc.player == null) return;
            if (!(mc.currentScreen instanceof InventoryScreen)) {
                return;
            }
            if (!waitTimer.hasReached(delay.getRandomValue().floatValue())) return;
            ScreenHandler screenHandler = mc.player.currentScreenHandler;
            Slot bestSword = null;
            Slot bestPickaxe = null;
            Slot bestAxe = null;
            Slot bestShovel = null;
            int convertedSword = Integer.parseInt(swordSlot.getValue()) - 1;
            int convertedPickaxe = Integer.parseInt(pickaxeSlot.getValue()) - 1;
            int convertedAxe = Integer.parseInt(axeSlot.getValue()) - 1;
            int convertedShovel = Integer.parseInt(shovelSlot.getValue()) - 1;
            for (Slot slot : screenHandler.slots) {
                ItemStack stack = slot.getStack();
                if (stack.isEmpty()) continue;
                // sword
                if (stack.getItem() instanceof SwordItem &&
                        (mc.player.getInventory().getStack(convertedSword).isEmpty() || isBetterSword(bestSword != null ? bestSword.getStack() : null, stack))) {
                    bestSword = slot;
                }
                // pickaxe
                if (stack.getItem() instanceof PickaxeItem && (mc.player.getInventory().getStack(convertedPickaxe).isEmpty() || isBetterTool(bestPickaxe != null ? bestPickaxe.getStack() : null, stack))) {
                    bestPickaxe = slot;
                }
                // axe
                if (stack.getItem() instanceof AxeItem && (mc.player.getInventory().getStack(convertedAxe).isEmpty() || isBetterTool(bestAxe != null ? bestAxe.getStack() : null, stack))) {
                    bestAxe = slot;
                }
                // shovel
                if (stack.getItem() instanceof ShovelItem && (mc.player.getInventory().getStack(convertedShovel).isEmpty() || isBetterTool(bestShovel != null ? bestShovel.getStack() : null, stack))) {
                    bestShovel = slot;
                }
            }
            // throw junk
            for (Slot slot : screenHandler.slots) {
                if (!throwJunk.getValue()) break;
                if (slot.getStack().isEmpty()) continue;
                if (!waitTimer.hasReached(delay.getRandomValue().floatValue())) break;
                if (slot.getStack().getItem() instanceof MiningToolItem || slot.getStack().getItem() instanceof SwordItem) {
                    if (slot == bestSword || slot == bestPickaxe || slot == bestAxe || slot == bestShovel) continue;
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.id, 0, SlotActionType.THROW, mc.player);
                    waitTimer.reset();
                }
            }
            // swap items
            if (bestSword != null) {
                ItemStack currentSword = mc.player.getInventory().getStack(convertedSword);
                if (currentSword.isEmpty() || isBetterSword(currentSword, bestSword.getStack())) {
                    swap(bestSword.id, convertToSlot(swordSlot.getValue()), throwJunk.getValue());
                }
            }
            if (bestPickaxe != null) {
                ItemStack currentPickaxe = mc.player.getInventory().getStack(convertedPickaxe);
                if (currentPickaxe.isEmpty() || isBetterTool(currentPickaxe, bestPickaxe.getStack())) {
                    swap(bestPickaxe.id, convertToSlot(pickaxeSlot.getValue()), throwJunk.getValue());

                }
            }
            if (bestAxe != null) {
                ItemStack currentAxe = mc.player.getInventory().getStack(convertedAxe);
                if (currentAxe.isEmpty() || isBetterTool(currentAxe, bestAxe.getStack())) {
                    swap(bestAxe.id, convertToSlot(axeSlot.getValue()), throwJunk.getValue());
                }
            }
            if (bestShovel != null) {
                ItemStack currentShovel = mc.player.getInventory().getStack(convertedShovel);
                if (currentShovel.isEmpty() || isBetterTool(currentShovel, bestShovel.getStack())) {
                    swap(bestShovel.id, convertToSlot(shovelSlot.getValue()), throwJunk.getValue());
                }
            }
        }
    }

    private void swap(int fromSlot, int toSlot, boolean throwItem) {
        if (!waitTimer.hasReached(delay.getRandomValue().floatValue())) return;

        if (throwItem)
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, toSlot, 0, SlotActionType.THROW, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, toSlot, 0, SlotActionType.PICKUP, mc.player);
        if (!throwItem)
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
        waitTimer.reset();
    }



    public boolean isBetterTool(ItemStack currentTool, ItemStack consideredTool) {
        if (consideredTool.getItem() == null || currentTool == null) return true;
        return getBreakingStrength(currentTool) < getBreakingStrength(consideredTool);
    }
    public float getBreakingStrength(ItemStack stack) {
        if (!(stack.getItem() instanceof MiningToolItem toolItem)) {
            return 1;
        }
        int efficiency = DamageUtils.getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
        int baseSpeed = getBaseSpeed(toolItem);
        baseSpeed *= (efficiency * 0.2);
        return baseSpeed;
    }

    public int getBaseSpeed(Item item) {
        if (item instanceof MiningToolItem) {
            var itemEntry = Registries.ITEM.getEntry(item);

            if (itemEntry.isIn(ItemTags.WOODEN_TOOL_MATERIALS)) return 2;
            if (itemEntry.isIn(ItemTags.STONE_TOOL_MATERIALS)) return 4;
            if (itemEntry.isIn(ItemTags.IRON_TOOL_MATERIALS)) return 6;
            if (itemEntry.isIn(ItemTags.DIAMOND_TOOL_MATERIALS)) return 8;
            if (itemEntry.isIn(ItemTags.NETHERITE_TOOL_MATERIALS)) return 9;
            if (itemEntry.isIn(ItemTags.GOLD_TOOL_MATERIALS)) return goldTools.getValue() ? 5 : 12;
        }
        return 1;
    }




    public boolean isBetterSword(ItemStack currentSword, ItemStack consideredSword) {
        if (consideredSword.getItem() == null || currentSword == null) return true;
        return getDamage(currentSword) < getDamage(consideredSword);
    }
    public double getDamage(ItemStack sword) {
        double sharpness = 0.5 * DamageUtils.getEnchantmentLevel(sword, Enchantments.SHARPNESS) + 0.5;

        return getBaseDamage(sword) + sharpness;
    }
    public static double getBaseDamage(ItemStack weapon) {
        double baseDamage = 0.0;

        AttributeModifiersComponent modifiers = weapon.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (AttributeModifiersComponent.Entry entry : modifiers.modifiers()) {
                if (entry.attribute().equals(EntityAttributes.ATTACK_DAMAGE)) {
                    baseDamage += entry.modifier().value();
                }
            }
        }

        return baseDamage;
    }
    public int convertToSlot(String number) {
        int num = Integer.parseInt(number);
        if (num >= 1 && num <= 9) {
            return 35 + num;
        }
        return -1;
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
    public InventoryManager() {
        getSettingRepository().registerSettings(delay, throwJunk, swordSlot, pickaxeSlot, axeSlot, shovelSlot, goldTools);
    }
}
