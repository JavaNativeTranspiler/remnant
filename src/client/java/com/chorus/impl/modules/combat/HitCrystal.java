/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

@ModuleInfo(
        name = "Hit Crystal",
        description = "Automatically Hit Crystals For You",
        category = ModuleCategory.COMBAT
)
public class HitCrystal extends BaseModule implements QuickImports {

    private final SettingCategory delays = new SettingCategory("Delays");
    private final SettingCategory behavior = new SettingCategory("Behavior");
    private final NumberSetting<Integer> obsidianSwitchDelay = new NumberSetting<>(delays, "Obsidian Swap Delay (ms)", "w", 5, 0, 250);
    private final NumberSetting<Integer> obsidianPlaceDelay = new NumberSetting<>(delays, "Obsidian Place Delay (ms)", "w", 25, 0, 250);

    private final NumberSetting<Integer> crystalSwitchDelay = new NumberSetting<>(delays, "Crystal Swap Delay (ms)", " w", 5, 0, 250);
    private final NumberSetting<Integer> crystalPlaceDelay = new NumberSetting<>(delays, "Crystal Place Delay (ms)", " w", 5, 0, 250);

    private final MultiSetting whileHolding = new MultiSetting(behavior, "While Holding", "Activate When Holding... Item", "Sword", "Totem");
    private final TimerUtils obsidianPlace = new TimerUtils();
    private final TimerUtils obsidianSwitch = new TimerUtils();
    private final TimerUtils crystalPlace = new TimerUtils();
    private final TimerUtils crystalSwitch = new TimerUtils();

    private int stage = 0;
    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (!InputUtils.mouseDown(1)) {
            resetStages();
            return;
        }

        HitResult hitResult = mc.crosshairTarget;

        if (hitResult instanceof BlockHitResult blockHitResult) {
            if (mc.world.getBlockState(blockHitResult.getBlockPos()).isAir()) return;
            if (whileHolding.getSpecificValue("Sword")) {
                if (InventoryUtils.getMainHandItem().getItem() instanceof SwordItem) {
                    stage = 1;
                }
            }
            if (whileHolding.getSpecificValue("Totem")) {
                if (InventoryUtils.getMainHandItem().getItem() == Items.TOTEM_OF_UNDYING) {
                    stage = 1;
                }
            }
            if (isLookingAtCrystal()) return;

            if (mc.world.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.OBSIDIAN) {
                handleExistingObsidian(blockHitResult);
            } else {
                handleNewPlacement(blockHitResult);
            }
        } else {
            resetStages();
        }
    }

    private boolean isLookingAtCrystal() {
        if (mc.crosshairTarget instanceof EntityHitResult entityHitResult) {
            Entity targetEntity = entityHitResult.getEntity();
            return targetEntity instanceof EndCrystalEntity;
        }
        return false;
    }

    private void handleExistingObsidian(BlockHitResult blockHitResult) {
        if (stage < 3 && stage != 0) stage = 3;

        if (crystalSwitch.hasReached(crystalSwitchDelay.getValue())) {
            if (stage == 3) {
                crystalSwitch.reset();
                crystalPlace.reset();
                InventoryUtils.swap(Items.END_CRYSTAL);
                stage = 4;
            }
        }
        if (crystalPlace.hasReached(crystalPlaceDelay.getValue())) {
            if (stage == 4) {
                crystalPlace.reset();
                InputUtils.simulateClick(1);
                InputUtils.simulateRelease(1);
                stage = 0;
            }
        }
    }

    private void handleNewPlacement(BlockHitResult blockHitResult) {

        if (obsidianSwitch.hasReached(obsidianSwitchDelay.getValue())) {
            if (stage == 1) {
                obsidianSwitch.reset();
                obsidianPlace.reset();
                InventoryUtils.swap(Items.OBSIDIAN);
                stage = 2;
            }
        }
        if (obsidianPlace.hasReached(obsidianPlaceDelay.getValue())) {
            if (stage == 2) {
                obsidianPlace.reset();
                crystalPlace.reset();
                InputUtils.simulateClick(1);
                InputUtils.simulateRelease(1);
                stage = 3;
            }
        }
    }

    private void resetStages() {
        stage = 0;
        obsidianSwitch.reset();
        obsidianPlace.reset();
        crystalSwitch.reset();
        crystalPlace.reset();
    }

    public HitCrystal() {
        getSettingRepository().registerSettings(delays, behavior, obsidianSwitchDelay, obsidianPlaceDelay, crystalSwitchDelay, crystalPlaceDelay, whileHolding);
    }
}