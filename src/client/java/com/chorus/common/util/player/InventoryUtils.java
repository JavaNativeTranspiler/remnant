/**
 * Created: 12/11/2024
 */

package com.chorus.common.util.player;


import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InventoryUtils {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    /**
     * Finds the first slot containing the specified item.
     *
     * @param item The item to search for.
     * @return The slot number, or -1 if not found.
     */
    public static int findItemInInventory(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Finds the first slot containing the specified item.
     *
     * @param item The item to search for.
     * @return The slot number, or -1 if not found.
     */
    public static int findItemInStrictlyInventory(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 9; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() == item && i != 40) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Finds the first slot containing the specified item.
     *
     * @param item The item to search for.
     * @return The slot number, or -1 if not found.
     */
    public static int findItemInHotBar(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i <= 8; i++) {
            if (inventory.getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Finds the first slot containing the specified item excluding the crafting menu.
     *
     * @param item The item to search for.
     * @return The slot number, or -1 if not found.
     */
    public static int FindItemInInventoryWindow(Item item, int min, int max, ScreenHandler handler) {
        for (int i = min; i <= max; i++) {
            if (handler.getSlot(i).getStack().getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int findItemByName(String itemName) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i <= 8; i ++) {
            if (inventory.getStack(i).getName().contains(Text.of(itemName))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Counts the total number of a specific item in the player's inventory.
     *
     * @param item The item to count.
     * @return The total count of the item.
     */
    public static int countItem(Item item) {
        PlayerInventory inventory = mc.player.getInventory();
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Swaps the item in the specified slot with the item in the player's main hand.
     *
     * @param slot The slot to swap with.
     */
    public static void swapWithMainHand(int slot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.SWAP, mc.player);
    }

    /**
     * Drops the item in the specified slot.
     *
     * @param slot The slot to drop from.
     */
    public static void dropItem(int slot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 1, SlotActionType.THROW, mc.player);
    }

    /**
     * Checks if the player's inventory is full.
     *
     * @return true if the inventory is full, false otherwise.
     */
    public static boolean isInventoryFull() {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.main.size(); i++) {
            if (inventory.main.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the first empty slot in the player's inventory.
     *
     * @return The empty slot number, or -1 if the inventory is full.
     */
    public static int findEmptySlot() {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.main.size(); i++) {
            if (inventory.main.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Moves an item from one slot to another.
     *
     * @param fromSlot The source slot.
     * @param toSlot The destination slot.
     */
    public static void moveItem(int fromSlot, int toSlot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP_ALL, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, toSlot, 0, SlotActionType.PICKUP, mc.player);
    }    /**
     * Moves all items from one slot to another.
     *
     * @param fromSlot The source slot.
     * @param toSlot The destination slot.
     */
    public static void moveAllItems(int fromSlot, int toSlot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP_ALL, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, toSlot, 0, SlotActionType.PICKUP_ALL, mc.player);
    }

    /**
     * Checks if the player has a specific item in their inventory.
     *
     * @param item The item to check for.
     * @return true if the item is in the inventory, false otherwise.
     */
    public static boolean hasItem(Item item) {
        return findItemInInventory(item) != -1;
    }
    /**
     * Checks if the player has a specific item in their inventory.
     *
     * @param item The item to check for.
     * @return true if the item is in the inventory, false otherwise.
     */
    public static boolean hasItemInInventory(Item item) {
        return findItemInStrictlyInventory(item) != -1;
    }
    /**
     * Finds the first slot that matches a given predicate.
     *
     * @param predicate The predicate to match against.
     * @return The slot number, or -1 if not found.
     */
    public static int findItemWithPredicate(Predicate<ItemStack> predicate) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            if (predicate.test(inventory.getStack(i))) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Finds the first slot that matches a given predicate.
     *
     * @param predicate The predicate to match against.
     * @return The slot number, or -1 if not found.
     */
    public static List<Integer> findItemWithPredicateInventory(Predicate<ItemStack> predicate) {
        PlayerInventory inventory = mc.player.getInventory();
        List<Integer> slots = new ArrayList<>();
        for (int i = 9; i < 45; i++) {
            if (predicate.test(inventory.getStack(i))) {
                slots.add(i);
            }
        }
        return slots;
    }
    /**
     * Finds the first hotbar slot that matches a given predicate.
     *
     * @param predicate The predicate to match against.
     * @return The slot number, or -1 if not found.
     */
    public static int findItemWithPredicateInHotbar(Predicate<ItemStack> predicate) {
        PlayerInventory inventory = mc.player.getInventory();
        for (int i = 0; i <= 8; i++) {
            if (predicate.test(inventory.getStack(i))) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Gets the item in the player's main hand.
     *
     * @return The ItemStack in the main hand.
     */
    public static ItemStack getMainHandItem() {
        return mc.player.getMainHandStack();
    }

    /**
     * Gets the item in the player's off hand.
     *
     * @return The ItemStack in the off hand.
     */
    public static ItemStack getOffHandItem() {
        return mc.player.getOffHandStack();
    }

    /**
     * Checks if the player's main hand is empty.
     *
     * @return true if the main hand is empty, false otherwise.
     */
    public static boolean isMainHandEmpty() {
        return mc.player.getMainHandStack().isEmpty();
    }

    /**
     * Swaps the item in the specified slot to the player's main hand.
     *
     * @param slot The slot to swap from.
     */
    public static void swapToMainHand(int slot) {
        PlayerInventory inventory = mc.player.getInventory();
        inventory.selectedSlot = slot;
    }

    /**
     * Moves an item to the offhand slot.
     *
     * @param fromSlot The source slot.
     */
    public static void swapItemToOffhand(int fromSlot) {
        int convertedSlot = fromSlot < 9 ? fromSlot + 36 : fromSlot;
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                convertedSlot,
                40,
                SlotActionType.SWAP,
                mc.player
        );
    }
    /**
     * Checks if player has item in hotbar.
     *
     * @param item item to swap
     *             Returns true or false
     */
    public static boolean swap(Item item) {
        final PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i <= 8; i ++) {
            if (inv.getStack(i).isOf(item)) {
                inv.selectedSlot = i;
                return true;
            }
        }
        return false;
    }

}