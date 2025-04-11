/**
 * Created: 2/8/2025
 */

package com.chorus.impl.modules.movement;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;

@ModuleInfo(
        name = "SnapTap",
        description = "wooting :3",
        category = ModuleCategory.MOVEMENT
)
public class SnapTap extends BaseModule implements QuickImports {

    public static long LEFT_STRAFE_LAST_PRESS_TIME;
    public static long RIGHT_STRAFE_LAST_PRESS_TIME;
    public static long FORWARD_STRAFE_LAST_PRESS_TIME;
    public static long BACKWARD_STRAFE_LAST_PRESS_TIME;

} /* handled in KeybindingMixin */