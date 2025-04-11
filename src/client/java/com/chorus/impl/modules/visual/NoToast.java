
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render2DEvent;

@ModuleInfo(
        name = "NoToast",
        description = "Hides all minecraft toasts",
        category = ModuleCategory.VISUAL
)
public class NoToast extends BaseModule implements QuickImports {
    @RegisterEvent
    private void Render2DEvent(Render2DEvent event) {
        mc.getToastManager().clear();
    }
}
