/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.DamageUtils;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.ItemUseCooldownEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

@ModuleInfo(
        name = "Anchor Macro",
        description = "Automatically Fills And Explodes Anchors",
        category = ModuleCategory.COMBAT
)
public class AnchorMacro extends BaseModule implements QuickImports {

    private final SettingCategory generalSettings = new SettingCategory("General Settings");
    private final SettingCategory chargeSettings = new SettingCategory("Anchor Charge Settings");
    private final SettingCategory explosionSettings = new SettingCategory("Anchor Explosion Settings");
    private final SettingCategory swapSettings = new SettingCategory("Hotbar Swap Settings");

    private final MultiSetting condition = new MultiSetting(generalSettings, "Conditions", "Only Anchor When", "Clicking", "Holding Anchor Related Item");
    private final RangeSetting<Float> range = new RangeSetting<>(generalSettings, "Range", "", 0f, 6f, 3f, 3f);

    private final BooleanSetting charge = new BooleanSetting(chargeSettings, "Charge Anchors", "Decide To Automatically Charge Anchors", true);
    private final NumberSetting<Integer> chargeAmount = new NumberSetting<>(chargeSettings, "Charge Amount", "", 1, 1, 4);
    private final RangeSetting<Integer> chargeDelay = new RangeSetting<>(chargeSettings, "Charge Delay", "", 0, 250, 100, 100);

    private final BooleanSetting explode = new BooleanSetting(explosionSettings, "Explode Anchors", "Decide To Automatically Explode Anchors", true);
    private final NumberSetting<Float> explodeDamage = new NumberSetting<>(explosionSettings, "Explode Max Self Damage", "Max Damage An Anchor Can Do", 1f, 1f, 20f);
    private final RangeSetting<Integer> explodeDelay = new RangeSetting<>(explosionSettings, "Explode Delay", "", 0, 250, 100, 100);

    private final ModeSetting swap = new ModeSetting(swapSettings, "Swap To", "Swap To An Item After Exploding An Anchor", "Anchor", "Anchor", "Totem", "Shield", "Obsidian");
    private final NumberSetting<Integer> swapDelay = new NumberSetting<>(swapSettings, "Swap Delay", "", 50, 0, 250);

    private final TimerUtils chargeTimer = new TimerUtils();
    private final TimerUtils explodeTimer = new TimerUtils();
    private final TimerUtils swapTimer = new TimerUtils();

    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
        if (event.getMode() != TickEvent.Mode.PRE) return;

        HitResult crosshairTarget = mc.crosshairTarget;
        if (!(crosshairTarget instanceof BlockHitResult blockHit)) return;
        Item mainHand = InventoryUtils.getMainHandItem().getItem();
        if (condition.getSpecificValue("Clicking") && !InputUtils.mouseDown(1)) return;
        if (condition.getSpecificValue("Holding Anchor Related Item") && !(mainHand == Items.RESPAWN_ANCHOR || mainHand == Items.GLOWSTONE))
            return;

        if (mc.world.getBlockState(blockHit.getBlockPos()).getBlock() == Blocks.RESPAWN_ANCHOR) {
            mc.options.useKey.setPressed(false);
            int charges = mc.world.getBlockState(blockHit.getBlockPos()).get(net.minecraft.block.RespawnAnchorBlock.CHARGES);
            if (charges < chargeAmount.getValue()) {
                explodeTimer.reset();
                chargeTimer.reset();
                if (!charge.getValue()) return;
                if (mainHand != Items.GLOWSTONE) {
                    if (InventoryUtils.findItemInHotBar(Items.GLOWSTONE) != -1) {
                        if (swapTimer.hasReached(swapDelay.getValue())) {
                            InventoryUtils.swapToMainHand(InventoryUtils.findItemInHotBar(Items.GLOWSTONE));
                            swapTimer.reset();
                        }
                    }
                } else {
                    if (chargeTimer.hasReached(chargeDelay.getRandomValue().intValue())) {
                        mc.options.useKey.setPressed(false);
                        InputUtils.simulateClick(1, 50);
                    }
                }
            } else {
                if (!explode.getValue()) return;
                if (mainHand == Items.GLOWSTONE) {
                    explodeTimer.reset();
                    if (swapTimer.hasReached(swapDelay.getValue())) {
                        InventoryUtils.swapToMainHand(InventoryUtils.findItemInHotBar(Items.RESPAWN_ANCHOR));
                        swapTimer.reset();
                    }
                } else {
                    if (explodeTimer.hasReached(explodeDelay.getRandomValue().intValue())) {
                        if (DamageUtils.calculateAnchorDamage(mc.player, blockHit.getBlockPos().toCenterPos()) > explodeDamage.getValue())
                            return;
                        InputUtils.simulateClick(1, 50);
                    }
                }
            }
        } else {
            //InputUtils.simulateClick(1, 1);
            swapTimer.reset();
        }
    }

    @RegisterEvent
    private void itemUseCooldownEventEventListener(ItemUseCooldownEvent event) {
        event.setSpeed(meetsConditions() ? 0 : 4);
    }

    public boolean meetsConditions() {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) return false;
        if (condition.getSpecificValue("Clicking") && !InputUtils.mouseDown(1)) return false;
        Item mainHand = InventoryUtils.getMainHandItem().getItem();
        return !condition.getSpecificValue("Holding Anchor Related Item") || (mainHand == Items.RESPAWN_ANCHOR || mainHand == Items.GLOWSTONE);
    }

    public AnchorMacro() {
        getSettingRepository().registerSettings(generalSettings, chargeSettings, explosionSettings, swapSettings, condition, range, charge, chargeAmount, chargeDelay, explode, explodeDamage, explodeDelay, swap, swapDelay);
        chargeAmount.setRenderCondition(() -> charge.getValue());
        chargeAmount.setRenderCondition(() -> charge.getValue());
        explodeDamage.setRenderCondition(() -> explode.getValue());
        swapDelay.setRenderCondition(() -> !swap.getValue().equals("Anchor"));
    }
}