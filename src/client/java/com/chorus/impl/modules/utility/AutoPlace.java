package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@ModuleInfo(
        name = "AutoPlace",
        description = "Places blocks automatically",
        category = ModuleCategory.UTILITY
)
public class AutoPlace extends BaseModule implements QuickImports {
    private final NumberSetting<Integer> delay = new NumberSetting<>(null, "Delay", "Delay between placements in milliseconds", 50, 0, 1000);
    private final BooleanSetting holdRight = new BooleanSetting(null, "Hold Right", "Continuously place while holding right click", true);
    private long lastPlaceTime = 0L;
    private BlockPos lastPlacedPos = null;

    public AutoPlace() {
        getSettingRepository().registerSettings(delay, holdRight);
    }

    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        if (event.getMode() == TickEvent.Mode.PRE) {
            if (mc.player == null || mc.world == null) return;
            if (holdRight.getValue() && !InputUtils.mouseDown(1)) return;
            if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPlaceTime < delay.getValue()) return;

            if (mc.crosshairTarget instanceof BlockHitResult hitResult) {
                Direction side = hitResult.getSide();
                if (side != Direction.UP && side != Direction.DOWN) {
                    BlockPos pos = hitResult.getBlockPos();
                    BlockState state = mc.world.getBlockState(pos);

                    if (state.getBlock() != Blocks.AIR && !state.getFluidState().isEmpty()) return;
                    if (lastPlacedPos != null && lastPlacedPos.equals(pos)) return;

                    InputUtils.simulatePress(1);
                    mc.itemUseCooldown = 0;
                    mc.interactionManager.interactBlock(mc.player, mc.player.getActiveHand(), hitResult);
                    mc.player.swingHand(mc.player.getActiveHand());
                    InputUtils.simulateRelease(1);

                    lastPlacedPos = pos;
                    lastPlaceTime = currentTime;
                }
            }
        }
    }
}