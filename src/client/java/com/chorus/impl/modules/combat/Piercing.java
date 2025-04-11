package com.chorus.impl.modules.combat;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;

@ModuleInfo(name = "Piercing", description = "Lets You Pierce Blocks And Entities", category = ModuleCategory.COMBAT)
public class Piercing extends BaseModule implements QuickImports {


    public Piercing() {
        getSettingRepository().registerSettings();
    }

}
