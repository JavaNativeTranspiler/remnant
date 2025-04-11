package com.chorus.api.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModuleRepository implements ModuleProvider {
    private final Map<Class<? extends Module>, Module> modules = new ConcurrentHashMap<>();

    @Override
    public void registerModule(Module module) {
        modules.put(module.getClass(), module);
    }

    @Override
    public void unregisterModule(Module module) {
        modules.remove(module.getClass());
    }

    @Override
    public <T extends Module> T getModule(Class<T> moduleClass) {
        return (T) modules.get(moduleClass);
    }

    @Override
    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
