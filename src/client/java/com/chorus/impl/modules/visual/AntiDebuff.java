
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.visual;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;

@ModuleInfo(
    name        = "AntiDebuff",
    description = "Removes Visual Debuffs",
    category    = ModuleCategory.VISUAL
)

public class AntiDebuff extends BaseModule implements QuickImports {
    private final SettingCategory general = new SettingCategory("General");
    public final MultiSetting mode = new MultiSetting(general, "Mode", "Choose Debuffs to hide",
            "Blindness",
            "Darkness",
            "Nausea",
            "Fire",
            "Explosion Particle");
    // handled in a thousand different mixins fuck you microsoft
    public AntiDebuff() {
        getSettingRepository().registerSettings(general, mode);
    }

}
