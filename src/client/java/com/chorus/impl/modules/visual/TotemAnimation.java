
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.visual;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;

@ModuleInfo(
    name        = "TotemAnimation",
    description = "Changes Totem Animation Duration",
    category    = ModuleCategory.VISUAL
)
public class TotemAnimation extends BaseModule implements QuickImports {
    private final SettingCategory general = new SettingCategory("General");

    public final NumberSetting<Integer> time = new NumberSetting<>(general, "Reduction", "Change Totem Time", 50, 0, 100);

    public TotemAnimation() {
        getSettingRepository().registerSettings(general, time);
    }
}
