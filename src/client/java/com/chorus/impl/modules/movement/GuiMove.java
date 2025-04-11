
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.movement;

import cc.polymorphism.eventbus.EventPriority;
import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.client.gui.screen.ChatScreen;

@ModuleInfo(
        name = "GuiMove",
        description = "Allows you to move in GUIS",
        category = ModuleCategory.MOVEMENT
)
public class GuiMove extends BaseModule implements QuickImports {
    @RegisterEvent(value = EventPriority.LOW)
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            if (mc.currentScreen == null) return;
            if (mc.currentScreen instanceof ChatScreen) return;

            mc.options.forwardKey.setPressed(InputUtils.keyDown(mc.options.forwardKey.getDefaultKey().getCode()));
            mc.options.leftKey.setPressed(InputUtils.keyDown(mc.options.leftKey.getDefaultKey().getCode()));
            mc.options.rightKey.setPressed(InputUtils.keyDown(mc.options.rightKey.getDefaultKey().getCode()));
            mc.options.backKey.setPressed(InputUtils.keyDown(mc.options.backKey.getDefaultKey().getCode()));
            mc.options.jumpKey.setPressed(InputUtils.keyDown(mc.options.jumpKey.getDefaultKey().getCode()));
        }
    }
}
