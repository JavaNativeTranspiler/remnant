
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.movement;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;

@ModuleInfo(
    name        = "NoPush",
    description = "Prevents Being Pushed",
    category    = ModuleCategory.MOVEMENT
)
public class NoPush extends BaseModule implements QuickImports {
    // handled in entity mixin

}
