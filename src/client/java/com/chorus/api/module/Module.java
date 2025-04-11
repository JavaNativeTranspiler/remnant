
/**
 * Created: 12/7/2024
 */
package com.chorus.api.module;

import com.chorus.api.module.setting.SettingRepository;

public interface Module {
    void onDisable();

    void onEnable();

    ModuleCategory getCategory();

    String getDescription();

    boolean isEnabled();

    int getKey();

    void setKey(int keyCode);

    String getName();

    String getSuffix();

    SettingRepository getSettingRepository();

    float getWidth();

    float getHeight();

    boolean isDraggable();
}


//~ Formatted by Jindent --- http://www.jindent.com
