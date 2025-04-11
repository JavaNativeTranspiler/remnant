
/**
 * Created: 12/7/2024
 */
package com.chorus.api.module;

import java.util.List;

public interface ModuleProvider {
    void registerModule(Module module);

    void unregisterModule(Module module);

    <T extends Module> T getModule(Class<T> moduleClass);

    List<Module> getModules();
}


//~ Formatted by Jindent --- http://www.jindent.com
