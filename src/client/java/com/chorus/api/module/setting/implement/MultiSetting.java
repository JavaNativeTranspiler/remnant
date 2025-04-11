package com.chorus.api.module.setting.implement;

import com.chorus.api.module.exception.ModuleException;
import com.chorus.api.module.setting.AbstractSetting;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class MultiSetting extends AbstractSetting<Set<String>> {
    private final String       name;
    private final String       description;
    private final List<String> modes;
    private Set<String>        selectedModes;
    private SettingCategory parent;

    public MultiSetting(String name, String description, String... modes) {
        this.name          = name;
        this.description   = description;
        this.modes         = Arrays.asList(modes);
        this.selectedModes = new HashSet<>();
    }

    public MultiSetting(SettingCategory parent, String name, String description, String... modes) {
        this.name          = name;
        this.description   = description;
        this.modes         = Arrays.asList(modes);
        this.selectedModes = new HashSet<>();
        this.parent = parent;
    }

    public void deselectMode(String mode) {
        if (!modes.contains(mode)) {
            throw new ModuleException("Cannot deselect invalid mode: " + mode + " for MultiSetting: " + name);
        }

        selectedModes.remove(mode);
    }

    public void selectMode(String mode) {
        if (modes.contains(mode)) {
            selectedModes.add(mode);
        } else {
            throw new ModuleException("Invalid mode: " + mode + " for MultiSetting: " + name);
        }
    }

    @Override
    public Set<String> getDefaultValue() {
        return new HashSet<>(selectedModes);
    }

    public Boolean getSpecificValue(String string) {
        return selectedModes.contains(string);
    }

    @Override
    public Set<String> getValue() {
        return selectedModes;
    }

    @Override
    public void setValue(Set<String> value) {
        for (String mode : value) {
            if (!modes.contains(mode)) {
                throw new ModuleException("Invalid mode: " + mode + " in set for MultiSetting: " + name);
            }
        }

        selectedModes = value;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com