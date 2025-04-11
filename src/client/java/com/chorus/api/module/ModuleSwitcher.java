package com.chorus.api.module;

public class ModuleSwitcher {
    private final ModuleProvider provider;

    public ModuleSwitcher(ModuleProvider provider) {
        this.provider = provider;
    }

    public <T extends Module> void disableModule(Class<T> moduleClass) {
        T module = provider.getModule(moduleClass);

        if ((module != null) && module.isEnabled()) {
            module.onDisable();
        }
    }

    public <T extends Module> void enableModule(Class<T> moduleClass) {
        T module = provider.getModule(moduleClass);

        if ((module != null) &&!module.isEnabled()) {
            module.onEnable();
        }
    }

    public <T extends Module> void toggleModule(Class<T> moduleClass) {
        T module = provider.getModule(moduleClass);

        if (module != null) {
            if (module.isEnabled()) {
                module.onDisable();
            } else {
                module.onEnable();
            }
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
