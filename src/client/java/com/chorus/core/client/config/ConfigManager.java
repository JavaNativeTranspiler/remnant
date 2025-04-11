package com.chorus.core.client.config;

import chorus0.Chorus;
import com.chorus.api.module.ModuleManager;
import com.chorus.api.module.setting.Setting;
import com.chorus.api.module.setting.implement.ColorSetting;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.core.client.ClientInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    @Getter
    private final Path configRoot;
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    @Getter
    private String activeProfile = "default";

    public ConfigManager(ClientInfo clientInfo) {
        this.configRoot = clientInfo.configsDir().resolve("profiles");
        initializeFileSystem();
        loadProfiles();
        SCHEDULER.scheduleAtFixedRate(this::autoSave, 5, 5, TimeUnit.SECONDS);
    }

    private void initializeFileSystem() {
        try {
            Files.createDirectories(configRoot);
            if (!Files.exists(configRoot.resolve(activeProfile))) {
                createProfile(activeProfile);
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to initialize config directory", e);
        }
    }

    public void createProfile(String name) {
        Path profilePath = configRoot.resolve(name);
        try {
            Files.createDirectory(profilePath);
            profiles.put(name, new Profile(profilePath));
            saveProfile(name);
        } catch (IOException e) {
            throw new ConfigException("Failed to create profile: " + name, e);
        }
    }

    public void deleteProfile(String name) {
        if (name.equals(activeProfile)) return;
        try {
            FileUtils.deleteDirectory(configRoot.resolve(name).toFile());
            profiles.remove(name);
        } catch (IOException e) {
            throw new ConfigException("Failed to delete profile: " + name, e);
        }
    }

    public void loadProfile(String name) {
        Profile profile = profiles.get(name);
        if (profile != null) {
            profile.load(Chorus.getInstance().getModuleManager());
            activeProfile = name;
        }
    }

    public void saveProfile(String name) {
        Profile profile = profiles.get(name);
        if (profile != null) {
            profile.save(Chorus.getInstance().getModuleManager());
        }
    }

    public void reloadActiveProfile() {
        loadProfile(activeProfile);
    }

    private void loadProfiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(configRoot)) {
            for (Path profileDir : stream) {
                if (Files.isDirectory(profileDir)) {
                    String profileName = profileDir.getFileName().toString();
                    profiles.put(profileName, new Profile(profileDir));
                }
            }
        } catch (IOException e) {
            throw new ConfigException("Failed to load profiles", e);
        }
    }

    private void autoSave() {
        saveProfile(activeProfile);
    }

    public void shutdown() {
        SCHEDULER.shutdown();
        saveProfile(activeProfile);
    }

    public List<String> getProfileNames() {
        return new ArrayList<>(profiles.keySet());
    }

    public boolean profileExists(String name) {
        return profiles.containsKey(name);
    }

    public void saveCurrentProfile() {
        saveProfile(activeProfile);
    }

    private static class Profile {
        private final Path profilePath;
        private final Map<String, ModuleConfig> config = new ConcurrentHashMap<>();

        Profile(Path profilePath) {
            this.profilePath = profilePath;
        }

        void load(ModuleManager moduleManager) {
            Path configFile = profilePath.resolve("modules.json");
            if (Files.exists(configFile)) {
                try (Reader reader = Files.newBufferedReader(configFile)) {
                    Type type = new TypeToken<Map<String, ModuleConfig>>(){}.getType();
                    config.clear();
                    config.putAll(GSON.fromJson(reader, type));
                    applyConfig(moduleManager);
                } catch (Exception e) {
                    throw new ConfigException("Failed to load profile config", e);
                }
            }
        }

        void save(ModuleManager moduleManager) {
            Path configFile = profilePath.resolve("modules.json");
            updateConfig(moduleManager);
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(config, writer);
            } catch (IOException e) {
                throw new ConfigException("Failed to save profile config", e);
            }
        }

        private void updateConfig(ModuleManager moduleManager) {
            moduleManager.getModules().forEach(module -> {
                ModuleConfig moduleConfig = config.computeIfAbsent(module.getName(), k -> new ModuleConfig());
                moduleConfig.enabled = module.isEnabled();
                moduleConfig.keyBind = module.getKey();
                moduleConfig.settings = new HashMap<>();
                module.getSettingRepository().getSettings().forEach((name, setting) -> {
                    if (setting instanceof ColorSetting) {
                        moduleConfig.settings.put(name, ((ColorSetting) setting).getValue().getRGB());
                    } else {
                        moduleConfig.settings.put(name, setting.getValue());
                    }
                });
            });
        }

        private void applyConfig(ModuleManager moduleManager) {
            moduleManager.getModules().forEach(module -> {
                ModuleConfig moduleConfig = config.get(module.getName());
                if (moduleConfig != null) {
                    if (moduleConfig.enabled) module.onEnable();
                    else module.onDisable();

                    module.setKey(moduleConfig.keyBind);
                    moduleConfig.settings.forEach((name, value) -> {
                        Setting<?> setting = module.getSettingRepository().getSetting(name);
                        if (setting != null) {
                            try {
                                setSettingValue(setting, value);
                            } catch (ClassCastException e) {
                                System.err.println("Type mismatch for setting " + name + ": " + e.getMessage());
                            }
                        }
                    });
                }
            });
        }

        @SuppressWarnings("unchecked")
        private <T> void setSettingValue(Setting<T> setting, Object value) {
            try {
                if (setting instanceof NumberSetting) {
                    NumberSetting<? extends Number> numberSetting = (NumberSetting<? extends Number>) setting;
                    Number converted = convertNumericType(value, numberSetting.getValue());
                    ((Setting<Number>) setting).setValue(converted);
                } else if (setting instanceof RangeSetting) {
                    handleRangeSetting((RangeSetting<?>) setting, value);
                } else if (setting instanceof MultiSetting) {
                    if (value instanceof List<?> list) {
                        Set<String> converted = new LinkedHashSet<>();
                        for (Object item : list) {
                            converted.add(item.toString());
                        }
                        setting.setValue((T) converted);
                    }
                } else if (setting instanceof ColorSetting) {
                    if (value instanceof Number) {
                        int intValue = ((Number) value).intValue();
                        Color color = new Color(intValue, true);
                        ((ColorSetting) setting).setValue(color);
                    }
                } else {
                    setting.setValue((T) value);
                }
            } catch (ClassCastException | NumberFormatException e) {
                System.err.println("Type mismatch for " + setting.getName() + ": " + e.getMessage());
            }
        }

        private <T extends Number & Comparable<T>>
        void handleRangeSetting(RangeSetting<T> setting, Object value) {
            if (value instanceof List<?> list && list.size() == 2) {
                T[] converted = (T[]) new Number[2];
                Class<T> type = getSettingType(setting);

                converted[0] = convertToExactType(list.get(0), type);
                converted[1] = convertToExactType(list.get(1), type);

                setting.setValue(converted);
            }
        }

        @SuppressWarnings("unchecked")
        private <T extends Number & Comparable<T>> Class<T> getSettingType(RangeSetting<T> setting) {
            return (Class<T>) setting.getValue()[0].getClass();
        }

        private <T extends Number> T convertToExactType(Object value, Class<T> targetType) {
            if (targetType == Double.class) {
                return targetType.cast(((Number) value).doubleValue());
            } else if (targetType == Integer.class) {
                return targetType.cast(((Number) value).intValue());
            } else if (targetType == Float.class) {
                return targetType.cast(((Number) value).floatValue());
            }
            throw new IllegalArgumentException("Unsupported number type: " + targetType);
        }

        private Number convertNumericType(Object value, Number exampleValue) {
            if (exampleValue == null) {
                return ((Number) value).doubleValue();
            }

            if (exampleValue instanceof Double) {
                return ((Number) value).doubleValue();
            } else if (exampleValue instanceof Integer) {
                return ((Number) value).intValue();
            } else if (exampleValue instanceof Float) {
                return ((Number) value).floatValue();
            } else if (exampleValue instanceof Long) {
                return ((Number) value).longValue();
            }
            return ((Number) value).doubleValue();
        }
    }

    private static class ModuleConfig {
        boolean enabled;
        int keyBind;
        Map<String, Object> settings;
    }

    public static class ConfigException extends RuntimeException {
        public ConfigException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}