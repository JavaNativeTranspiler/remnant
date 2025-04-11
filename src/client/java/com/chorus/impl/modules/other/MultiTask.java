package com.chorus.impl.modules.other;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;

@ModuleInfo(
        name        = "MultiTask",
        description = "Lets you use items and attack at the same time",
        category    = ModuleCategory.OTHER
)
public class MultiTask extends BaseModule {}     /* handled in MinecraftClientMixin */