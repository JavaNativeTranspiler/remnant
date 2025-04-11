
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
import com.chorus.impl.events.player.TickEvent;

@ModuleInfo(
        name = "Sprint",
        description = "Automatically Sprints For You",
        category = ModuleCategory.MOVEMENT
)
public class Sprint extends BaseModule implements QuickImports {
    @RegisterEvent(value = EventPriority.LOW)
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            mc.options.sprintKey.setPressed(true);
        }
    }
}