package com.chorus.impl.modules.client;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.common.QuickImports;
import com.chorus.impl.screen.auth.LoginScreen;
import com.chorus.impl.screen.primordial.PrimordialScreen;
import com.chorus.impl.screen.remnant.RemnantScreen;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "ClickGUI", description = "Opens an interface for module management", category = ModuleCategory.CLIENT, key = GLFW.GLFW_KEY_INSERT)
public class ClickGUI extends BaseModule implements QuickImports {
    private final ModeSetting mode = new ModeSetting("Mode", "Choose Clickgui to use",
            "Remnant",
            "Remnant",
            "Primordial");
    @Override
    protected void onModuleEnabled()
    {
        if (mc.currentScreen instanceof LoginScreen) {
            return;
        }

        if (mode.getValue().equals("Remnant")) {
            mc.setScreen(RemnantScreen.getINSTANCE());
        } else {
            mc.setScreen(PrimordialScreen.getINSTANCE());
        }
    }

    public ClickGUI() {
        getSettingRepository().registerSettings(mode);
    }

    @Override
    public void onDisable() {
        if (mode.getValue().equals("Remnant") && mc.currentScreen instanceof RemnantScreen) {
            mc.setScreen(null);
        } else if (mode.getValue().equals("Primordial") && mc.currentScreen instanceof PrimordialScreen) {
            mc.setScreen(null);
        }
        super.onDisable();
    }
}