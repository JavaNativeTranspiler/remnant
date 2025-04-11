package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;

@ModuleInfo(
        name        = "HorseJump",
        description = "Always Jump perfectly with a horse",
        category    = ModuleCategory.OTHER
)
public class HorseJump extends BaseModule implements QuickImports {

    @RegisterEvent
    private void Render2DEvent(com.chorus.impl.events.render.Render2DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getJumpingMount() != null && mc.options.jumpKey.wasPressed()) {
            mc.player.getJumpingMount().setJumpStrength(100);
        }
    }
}

