package com.chorus.api.module;

import com.chorus.api.module.exception.ModuleException;
import com.chorus.api.module.setting.SettingManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@NoArgsConstructor
public class ModuleManager implements ModuleProvider {
    @Getter
    private static final ModuleManager                 INSTANCE       = new ModuleManager();
    private final Map<Class<? extends Module>, Module> modules = Collections.synchronizedMap(new LinkedHashMap<>());
    private final SettingManager                       settingManager = new SettingManager();
    private final ModuleSwitcher moduleSwitcher = new ModuleSwitcher(this);

    public <T extends Module> void disableModule(Class<T> moduleClass) {
        T module = getModule(moduleClass);
        if (module != null) {
            moduleSwitcher.disableModule(moduleClass);
            log.info("Disabled module: {}", module.getName());
        }
    }

    public <T extends Module> void enableModule(Class<T> moduleClass) {
        T module = getModule(moduleClass);
        if (module != null) {
            moduleSwitcher.enableModule(moduleClass);
            log.info("Enabled module: {}", module.getName());
        }
    }

    @Override
    public void registerModule(Module module) {
        if (module == null) {
            throw new ModuleException("Attempted to register a null module.");
        }

        Class<? extends Module> moduleClass = module.getClass();

        if (modules.containsKey(moduleClass)) {
            throw new ModuleException("Module '" + module.getName() + "' is already registered.");
        }

        modules.put(moduleClass, module);

        try {
            settingManager.registerSettings(module);
            log.info("Registered module: {}", module.getName());
        } catch (ModuleException e) {
            log.error("Failed to register settings for module '{}': {}", module.getName(), e.getMessage());

            throw e;
        }
    }

    public <T extends Module> void toggleModule(Class<T> moduleClass) {
        T module = getModule(moduleClass);
        if (module != null) {
            moduleSwitcher.toggleModule(moduleClass);
            log.info("Toggled module: {}", module.getName());
        }
    }

    @Override
    public void unregisterModule(Module module) {
        if (module == null) {
            throw new ModuleException("Attempted to unregister a null module.");
        }

        Class<? extends Module> moduleClass = module.getClass();

        if (modules.remove(moduleClass) != null) {
            settingManager.unregisterSettings(module);
            log.info("Unregistered module: {}", module.getName());
        } else {
            log.warn("Module '{}' was not found for unregistration.", module.getName());

            throw new ModuleException("Module '" + module.getName() + "' was not found for unregistration.");
        }
    }

    @Override
    public <T extends Module> T getModule(Class<T> moduleClass) {
        return (T) modules.get(moduleClass);
    }

    public <T extends Module> boolean isModuleEnabled(Class<T> moduleClass) {
        T module = getModule(moduleClass);

        return (module != null) && module.isEnabled();
    }

    @Override
    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> categorizedModules = new ArrayList<>();

        for (Module module : modules.values()) {
            if (module.getCategory() == category) {
                categorizedModules.add(module);
            }
        }

        return categorizedModules;
    }

    public Module getModuleByName(String name) {
        for (Module module : modules.values()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

}


//~ Formatted by Jindent --- http://www.jindent.com
