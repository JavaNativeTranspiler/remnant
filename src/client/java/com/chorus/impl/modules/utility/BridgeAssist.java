
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.utility;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.InventoryUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.item.BlockItem;

@ModuleInfo(
    name        = "BridgeAssist",
    description = "Helps you bridge",
    category    = ModuleCategory.UTILITY
)
public class BridgeAssist extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General");
    private final SettingCategory conditions = new SettingCategory("Conditionals");
    private final RangeSetting<Double> edgeOffset = new RangeSetting<>(general, "Edge Offset", "Adjust Edge Offset", 0.01, 0.3, 0.2, 0.25);
    private final RangeSetting<Double> unsneakDelay = new RangeSetting<>(general, "Un-Sneak Delay", "Set Un-Sneaking Delay", 0.0, 500.0, 250.0, 300.0);
    private final MultiSetting settings = new MultiSetting(conditions, "Activation", "Set activation conditions",
            "Looking Down",
            "Already Sneaking",
            "Not While Moving Forward",
            "Holding Blocks",
            "Above Air");

    private final TimerUtils unsneakTimer = new TimerUtils();
    boolean sneaked = false;

    public BridgeAssist() {
        getSettingRepository().registerSettings(general, conditions, edgeOffset, unsneakDelay, settings);
    }

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
       if (event.getMode().equals(TickEvent.Mode.PRE)) {
           if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
           if (!canAssist()) {
               if (sneaked) {
                   mc.options.sneakKey.setPressed(false);
                   sneaked = false;
               }
               return;
           }
           var pos = mc.player.getPos();
           var xOffset = 0.5 - ((Math.round(pos.x) + 0.5) - pos.x);
           var zOffset = 0.5 - ((Math.round(pos.z) + 0.5) - pos.z);
           if (mc.world.getBlockState(mc.player.getBlockPos().down()).isReplaceable()) {
               var offset = MathUtils.randomDouble(edgeOffset.getValueMin(), edgeOffset.getValueMax());
               if (xOffset <= offset || zOffset <= offset) {
                   mc.options.sneakKey.setPressed(true);
                   if (!sneaked) unsneakTimer.reset(); sneaked = true;
               } else {
                   mc.options.sneakKey.setPressed(false);
               }
           } else {
               var delay = MathUtils.randomDouble(unsneakDelay.getValueMin(), unsneakDelay.getValueMax());
               if (unsneakTimer.hasReached(delay) && sneaked) {
                   mc.options.sneakKey.setPressed(false);
                   sneaked = false;
               }
           }
       }
    }
    public boolean canAssist() {
        if (settings.getSpecificValue("Looking Down") && !(mc.player.getPitch() > 70)) return false;
        if (!mc.player.isOnGround()) return false;
        if (settings.getSpecificValue("Already Sneaking") && !(InputUtils.keyDown(mc.options.sneakKey.getDefaultKey().getCode()))) return false;
        if (settings.getSpecificValue("Not While Moving Forward") && InputUtils.keyDown(mc.options.forwardKey.getDefaultKey().getCode())) return false;
        if (settings.getSpecificValue("Holding Blocks") && !(mc.player.getInventory().getMainHandStack().getItem() instanceof BlockItem || InventoryUtils.getOffHandItem().getItem() instanceof BlockItem)) return false;
        if (settings.getSpecificValue("Above Air") && !mc.world.getBlockState(mc.player.getBlockPos().add(0, -5, 0)).isAir()) return false;
        return true;
    }
}
