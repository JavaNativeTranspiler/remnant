/**
 * Created: 12/7/2024
 */

package com.chorus.api.module;

import lombok.Getter;

@Getter
public enum ModuleCategory {
    COMBAT("Combat", "A", ""),
    MOVEMENT("Movement", "C", ""),
    VISUAL("Visual", "D", ""),
    UTILITY("Utility", "B", ""),
    OTHER("Other", "F", ""),
    CLIENT("Client", "B", "");

    private final String name;
    private final String icon;
    private final String lucideIcon;

    ModuleCategory(final String name, final String icon, final String lucideIcon) {
        this.name = name;
        this.icon = icon;
        this.lucideIcon = lucideIcon;
    }

    @Override
    public final String toString() {
        return this.name;
    }
}
