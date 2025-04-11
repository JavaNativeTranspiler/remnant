package com.chorus.impl.screen.remnant;

import chorus0.Chorus;
import com.chorus.api.module.Module;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.setting.Setting;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.prot.CrashUtil;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.modules.client.ClickGUI;
import com.chorus.impl.screen.hud.HUDEditorScreen;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.text.Text;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RemnantScreen extends Screen implements QuickImports {
    @Getter
    private static final RemnantScreen INSTANCE = new RemnantScreen();

    private static final float PANEL_WIDTH = 120;
    private static final float PANEL_HEIGHT = 17.5f;
    private static final float ANIMATION_DURATION = 500f;
    private static final float TOGGLE_WIDTH = 18;
    private static final float TOGGLE_HEIGHT = 9;
    private static final float KNOB_SIZE = TOGGLE_HEIGHT - 3;
    private static final float PANEL_SPACING = 8;
    private static final float INITIAL_Y = 15;
    private static final float SETTING_PADDING = 6;
    private static final float SETTING_TEXT_SIZE = 6;
    private static final float TITLE_TEXT_SIZE = 8;
    private static final float SLIDER_THICKNESS = 2;
    private static final float OUTLINE_THICKNESS = 1;
    private static final float TOGGLE_KNOB_OFFSET = 1.5f;
    private static final float COLOR_PICKER_HEIGHT = 80;
    private static final float TOOLTIP_DELAY = 1000f;
    private static final float TOOLTIP_PADDING = 4f;
    private static final float TOOLTIP_TEXT_SIZE = 7f;

    private static final Color HIGHLIGHT_COLOR = new Color(150, 150, 150);
    private static final Color DISABLED_COLOR = new Color(70, 70, 70);
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 175);
    private static final Color OUTLINE_COLOR = new Color(100, 100, 100, 100);
    private static final Color TOGGLE_KNOB_COLOR = new Color(255, 255, 255, 200);
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFAAAAAA;
    private static final int TEXT_DISABLED = 0xFF909090;

    @Getter @Setter
    private Module selectedModule;

    @Getter @Setter
    private Panel selectedPanel;

    private Module hoveredModule;
    private long hoverStartTime;
    private boolean showingTooltip;

    private NumberSetting<?> draggingNumberSetting = null;
    private RangeSetting<?> draggingRangeSetting = null;
    private boolean draggingRangeMin = false;
    private ColorSetting draggedColorSetting = null;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private boolean draggingSaturationBrightness = false;
    
    @Getter
    class Panel {
        private final ModuleCategory category;
        private Vector2f position;

        @Setter @Getter
        private boolean isOpen;

        @Setter @Getter
        private boolean isDragging;

        private double dragX, dragY;

        @Setter
        private List<Module> modules;

        @Getter
        private boolean showingSettings;

        @Getter @Setter
        private MultiSetting openMultiSetting;

        @Getter @Setter
        private ModeSetting openModeSetting;

        @Getter @Setter
        private float settingsAnimationProgress = 0f;
        private long lastSettingsAnimationTime = 0;

        @Getter @Setter
        private float currentHeight = PANEL_HEIGHT;
        private float targetHeight = PANEL_HEIGHT;
        private long lastHeightAnimationTime = System.currentTimeMillis();
        private float animationProgress = 0f;
        @Getter @Setter
        private float moduleScrollOffset = 0f;
        private float maxModuleScrollOffset = 0f;
        private float settingScrollOffset = 0f;
        private float maxSettingScrollOffset = 0f;

        private final Map<BooleanSetting, Float> booleanAnimations = new HashMap<>();
        private final Map<BooleanSetting, Long> booleanAnimationTimes = new HashMap<>();

        private final Map<ModeSetting, Float> modeAnimations = new HashMap<>();
        private final Map<ModeSetting, Long> modeAnimationTimes = new HashMap<>();

        private final Map<MultiSetting, Float> multiAnimations = new HashMap<>();
        private final Map<MultiSetting, Long> multiAnimationTimes = new HashMap<>();

        private final Map<NumberSetting<?>, Float> numberAnimations = new HashMap<>();
        private final Map<NumberSetting<?>, Long> numberAnimationTimes = new HashMap<>();

        private final Map<RangeSetting<?>, Float[]> rangeAnimations = new HashMap<>();
        private final Map<RangeSetting<?>, Long> rangeAnimationTimes = new HashMap<>();

        private final Map<ColorSetting, Float> colorAnimations = new HashMap<>();
        private final Map<ColorSetting, Long> colorAnimationTimes = new HashMap<>();
        private final Map<ColorSetting, Boolean> colorSettingExpanded = new HashMap<>();

        public Panel(ModuleCategory category, boolean isOpen, Vector2f position) {
            this.category = category;
            this.position = position;
            this.isOpen = isOpen;
            this.isDragging = false;
            this.showingSettings = false;
            this.openMultiSetting = null;
            this.openModeSetting = null;
            this.modules = new ArrayList<>();
            this.moduleScrollOffset = 0f;
            this.maxModuleScrollOffset = 0f;
            this.settingScrollOffset = 0f;
            this.maxSettingScrollOffset = 0f;
        }

        public void updateHeight(float newTargetHeight) {
            if (this.targetHeight != newTargetHeight) {
                this.targetHeight = newTargetHeight;
                this.lastHeightAnimationTime = System.currentTimeMillis();
                this.animationProgress = 0f;
            }
        }

        public void animateHeight() {
            if (currentHeight != targetHeight) {
                long currentTime = System.currentTimeMillis();
                float elapsed = Math.min(1f, (currentTime - lastHeightAnimationTime) / ANIMATION_DURATION);
                lastHeightAnimationTime = currentTime;

                animationProgress = Math.min(1f, animationProgress + elapsed);
                float smoothProgress = (float) (1 - Math.pow(1 - animationProgress, 3));
                currentHeight = currentHeight + (targetHeight - currentHeight) * smoothProgress;

                if (Math.abs(currentHeight - targetHeight) < 0.01f) {
                    currentHeight = targetHeight;
                }
            }
        }

        public void setShowingSettings(boolean showingSettings) {
            if (this.showingSettings != showingSettings) {
                this.showingSettings = showingSettings;
                this.lastSettingsAnimationTime = System.currentTimeMillis();
                this.animationProgress = 0f;
                this.lastHeightAnimationTime = System.currentTimeMillis();
                this.settingScrollOffset = 0f;
                this.maxSettingScrollOffset = 0f;
            }
        }

        public void startDragging(double mouseX, double mouseY) {
            isDragging = true;
            dragX = mouseX - position.getX();
            dragY = mouseY - position.getY();
        }

        public void updatePosition(double mouseX, double mouseY) {
            if (isDragging) {
                float newX = (float)(mouseX - dragX);
                float newY = (float)(mouseY - dragY);
                
                float screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
                float screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
                
                newX = Math.max(0, Math.min(screenWidth - PANEL_WIDTH, newX));
                
                newY = Math.max(0, Math.min(screenHeight - PANEL_HEIGHT, newY));
                
                position = new Vector2f(newX, newY);
            }
        }
    }

    private final Map<ModuleCategory, Panel> panels = new HashMap<>();

    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;

    public RemnantScreen() {
        super(Text.literal("Remnant"));

        float totalWidth = ModuleCategory.values().length * PANEL_WIDTH + (ModuleCategory.values().length - 1) * PANEL_SPACING;
        float x = mc.getWindow().getScaledWidth() / 2f - totalWidth / 2f;

        for (ModuleCategory category : ModuleCategory.values()) {
            panels.put(category, new Panel(category, true, new Vector2f(x, INITIAL_Y)));
            panels.get(category).setModules(
                moduleManager.getModules().stream()
                    .filter(module -> module.getCategory() == category)
                    .collect(Collectors.toList())
            );
            x += PANEL_WIDTH + PANEL_SPACING;
        }
    }

    private void recenterPanels() {
        float totalWidth = ModuleCategory.values().length * PANEL_WIDTH + (ModuleCategory.values().length - 1) * PANEL_SPACING;
        float x = mc.getWindow().getScaledWidth() / 2f - totalWidth / 2f;
        
        for (ModuleCategory category : ModuleCategory.values()) {
            Panel panel = panels.get(category);
            if (panel != null) {
                float currentY = panel.getPosition().getY();
                panel.position = new Vector2f(x, currentY);
                x += PANEL_WIDTH + PANEL_SPACING;
            }
        }
    }

    private void renderSetting(Setting<?> setting, DrawContext context, Vector2f position, float moduleY, FontAtlas font) {
        if (!setting.shouldRender()) return;

        int size = 25;
        float posX = position.getX();
        String result = (setting.getName() != null && setting.getName().length() > size) ? setting.getName().substring(0, size) + "..." : setting.getName();
        font.render(context.getMatrices(),
            result,
            posX + SETTING_PADDING,
            moduleY + 4,
            SETTING_TEXT_SIZE,
            TEXT_SECONDARY);

        switch (setting) {
            case BooleanSetting booleanSetting -> {
                float toggleY = moduleY + (PANEL_HEIGHT - TOGGLE_HEIGHT) / 2;

                Panel panel = selectedPanel;
                float animationProgress = panel.booleanAnimations.computeIfAbsent(booleanSetting, k -> booleanSetting.getValue() ? 1f : 0f);
                long lastAnimationTime = panel.booleanAnimationTimes.computeIfAbsent(booleanSetting, k -> System.currentTimeMillis());

                long currentTime = System.currentTimeMillis();
                float elapsed = Math.min(1f, (currentTime - lastAnimationTime) / ANIMATION_DURATION * 3);

                animationProgress = setAnimationDuration(booleanSetting.getValue(), animationProgress, elapsed, -1);


                float smoothProgress;
                if (animationProgress < 0.5f) {
                    smoothProgress = 2 * animationProgress * animationProgress;
                } else {
                    float t = -2 * animationProgress + 2;
                    smoothProgress = 1 - t * t / 2;
                }

                panel.booleanAnimations.put(booleanSetting, animationProgress);
                panel.booleanAnimationTimes.put(booleanSetting, currentTime);

                Color toggleColor = new Color(
                        DISABLED_COLOR.getRed() + (int) ((HIGHLIGHT_COLOR.getRed() - DISABLED_COLOR.getRed()) * smoothProgress),
                        DISABLED_COLOR.getGreen() + (int) ((HIGHLIGHT_COLOR.getGreen() - DISABLED_COLOR.getGreen()) * smoothProgress),
                        DISABLED_COLOR.getBlue() + (int) ((HIGHLIGHT_COLOR.getBlue() - DISABLED_COLOR.getBlue()) * smoothProgress)
                );

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        posX + PANEL_WIDTH - TOGGLE_WIDTH - SETTING_PADDING - 2,
                        toggleY,
                        TOGGLE_WIDTH,
                        TOGGLE_HEIGHT,
                        TOGGLE_HEIGHT / 2.3f,
                        toggleColor);

                float knobStartX = posX + PANEL_WIDTH - TOGGLE_WIDTH - SETTING_PADDING;
                float knobEndX = posX + PANEL_WIDTH - KNOB_SIZE - SETTING_PADDING - 4;
                float knobX = knobStartX + (knobEndX - knobStartX) * smoothProgress;

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        knobX,
                        toggleY + TOGGLE_KNOB_OFFSET,
                        KNOB_SIZE,
                        KNOB_SIZE,
                        KNOB_SIZE / 2,
                        TOGGLE_KNOB_COLOR);

            }
            case NumberSetting<?> numberSetting -> {
                float sliderY = moduleY + PANEL_HEIGHT;
                DecimalFormat format = new DecimalFormat("#.##");
                String value = format.format(numberSetting.getValue().doubleValue());
                float valueWidth = font.getWidth(value, SETTING_TEXT_SIZE);

                font.render(context.getMatrices(),
                        value,
                        posX + PANEL_WIDTH - SETTING_PADDING - 2 - valueWidth,
                        moduleY + 4,
                        SETTING_TEXT_SIZE,
                        TEXT_DISABLED);

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        posX + SETTING_PADDING,
                        sliderY,
                        PANEL_WIDTH - SETTING_PADDING * 2,
                        SLIDER_THICKNESS,
                        1,
                        DISABLED_COLOR);

                double progress = (numberSetting.getValue().doubleValue() - numberSetting.getMinValue().doubleValue()) /
                        (numberSetting.getMaxValue().doubleValue() - numberSetting.getMinValue().doubleValue());
                float targetProgress = (float) progress;
                Panel panel = selectedPanel;
                float animationProgress = panel.numberAnimations.computeIfAbsent(numberSetting, k -> targetProgress);
                long lastAnimationTime = panel.numberAnimationTimes.computeIfAbsent(numberSetting, k -> System.currentTimeMillis());

                long currentTime = System.currentTimeMillis();
                float elapsed = Math.min(1f, (currentTime - lastAnimationTime) / ANIMATION_DURATION * 3);

                animationProgress = setAnimationDuration((animationProgress < targetProgress), animationProgress, elapsed, targetProgress);


                panel.numberAnimations.put(numberSetting, animationProgress);
                panel.numberAnimationTimes.put(numberSetting, currentTime);

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        posX + SETTING_PADDING,
                        sliderY,
                        (float) ((PANEL_WIDTH - SETTING_PADDING * 2) * animationProgress),
                        SLIDER_THICKNESS,
                        1,
                        HIGHLIGHT_COLOR);

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        posX + SETTING_PADDING + (float) ((PANEL_WIDTH - SETTING_PADDING * 2) * animationProgress) - 3,
                        sliderY - 2,
                        6,
                        6,
                        3,
                        TOGGLE_KNOB_COLOR);
            }
            case RangeSetting rangeSetting -> {
                float sliderY = moduleY + PANEL_HEIGHT;
                DecimalFormat format = new DecimalFormat("#.##");
                String value = format.format(rangeSetting.getValueMin().doubleValue()) + " - " + format.format(rangeSetting.getValueMax().doubleValue());
                float valueWidth = font.getWidth(value, SETTING_TEXT_SIZE);

                font.render(context.getMatrices(),
                        value,
                        posX + PANEL_WIDTH - SETTING_PADDING - 2 - valueWidth,
                        moduleY + 4,
                        SETTING_TEXT_SIZE,
                        TEXT_DISABLED);

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        posX + SETTING_PADDING,
                        sliderY,
                        PANEL_WIDTH - SETTING_PADDING * 2,
                        SLIDER_THICKNESS,
                        1,
                        DISABLED_COLOR);

                double minProgress = (rangeSetting.getValueMin().doubleValue() - rangeSetting.getMin().doubleValue()) /
                        (rangeSetting.getMax().doubleValue() - rangeSetting.getMin().doubleValue());
                double maxProgress = (rangeSetting.getValueMax().doubleValue() - rangeSetting.getMin().doubleValue()) /
                        (rangeSetting.getMax().doubleValue() - rangeSetting.getMin().doubleValue());

                float targetMinProgress = (float) minProgress;
                float targetMaxProgress = (float) maxProgress;
                Panel panel = selectedPanel;
                Float[] animationProgress = panel.rangeAnimations.computeIfAbsent(rangeSetting, k -> new Float[]{targetMinProgress, targetMaxProgress});
                long lastAnimationTime = panel.rangeAnimationTimes.computeIfAbsent(rangeSetting, k -> System.currentTimeMillis());

                long currentTime = System.currentTimeMillis();
                float elapsed = Math.min(1f, (currentTime - lastAnimationTime) / ANIMATION_DURATION * 3);

                animationProgress[0] = setAnimationDuration((animationProgress[0] < targetMinProgress), animationProgress[0], elapsed, targetMinProgress);
                animationProgress[1] = setAnimationDuration((animationProgress[1] < targetMaxProgress), animationProgress[1], elapsed, targetMaxProgress);

                panel.rangeAnimations.put(rangeSetting, animationProgress);
                panel.rangeAnimationTimes.put(rangeSetting, currentTime);

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        posX + SETTING_PADDING + ((PANEL_WIDTH - SETTING_PADDING * 2) * animationProgress[0]),
                        sliderY,
                        (PANEL_WIDTH - SETTING_PADDING * 2) * (animationProgress[1] - animationProgress[0]),
                        SLIDER_THICKNESS,
                        1,
                        HIGHLIGHT_COLOR);

                float minKnobX = posX + SETTING_PADDING + ((PANEL_WIDTH - SETTING_PADDING * 2) * animationProgress[0]);
                float maxKnobX = posX + SETTING_PADDING + ((PANEL_WIDTH - SETTING_PADDING * 2) * animationProgress[1]);
                float knobY = sliderY + 2;

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        minKnobX - 4 + 3,
                        knobY - 4,
                        6,
                        6,
                        3,
                        TOGGLE_KNOB_COLOR);

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        maxKnobX - 4,
                        knobY - 4,
                        6,
                        6,
                        3,
                        TOGGLE_KNOB_COLOR);
            }
            case ModeSetting modeSetting -> {
                Panel panel = selectedPanel;
                boolean isOpen = panel.getOpenModeSetting() == modeSetting;

                float animationProgress = panel.modeAnimations.computeIfAbsent(modeSetting, k -> isOpen ? 1f : 0f);
                long lastAnimationTime = panel.modeAnimationTimes.computeIfAbsent(modeSetting, k -> System.currentTimeMillis());

                long currentTime = System.currentTimeMillis();
                float elapsed = Math.min(1f, (currentTime - lastAnimationTime) / ANIMATION_DURATION * 3);

                animationProgress = setAnimationDuration(isOpen, animationProgress, elapsed, -1);


                float smoothProgress;
                if (animationProgress < 0.5f) {
                    smoothProgress = 2 * animationProgress * animationProgress;
                } else {
                    float t = -2 * animationProgress + 2;
                    smoothProgress = 1 - t * t / 2;
                }

                panel.modeAnimations.put(modeSetting, animationProgress);
                panel.modeAnimationTimes.put(modeSetting, currentTime);

                String value = modeSetting.getValue();
                float valueWidth = font.getWidth(value, 6);
                font.render(context.getMatrices(),
                        value,
                        posX + PANEL_WIDTH - 10 - valueWidth,
                        moduleY + 4,
                        6,
                        TEXT_DISABLED);

                if (animationProgress > 0) {
                    float optionY = moduleY + PANEL_HEIGHT;
                    for (String mode : modeSetting.getModes()) {
                        boolean isSelected = mode.equals(modeSetting.getValue());

                        float alpha = (int) (smoothProgress * 255);
                        Color outlineColor = isSelected ?
                                new Color(255, 255, 255, (int) (100 * smoothProgress)) :
                                new Color(100, 100, 100, (int) (100 * smoothProgress));

                        Render2DEngine.drawRoundedOutline(context.getMatrices(),
                                posX + 6,
                                optionY,
                                PANEL_WIDTH - 12,
                                PANEL_HEIGHT - 2,
                                1,
                                1,
                                outlineColor);

                        font.render(context.getMatrices(),
                                mode,
                                posX + 10,
                                optionY + (PANEL_HEIGHT - 2) / 2 - font.getLineHeight() / 2 + 1,
                                6,
                                isSelected ?
                                        ((TEXT_PRIMARY & 0x00FFFFFF) | ((int) (alpha) << 24)) :
                                        ((TEXT_DISABLED & 0x00FFFFFF) | ((int) (alpha) << 24)));

                        optionY += PANEL_HEIGHT - 2;
                    }
                }
            }
            case MultiSetting multiSetting -> {
                Panel panel = selectedPanel;
                boolean isOpen = panel.getOpenMultiSetting() == multiSetting;

                float animationProgress = panel.multiAnimations.computeIfAbsent(multiSetting, k -> isOpen ? 1f : 0f);
                long lastAnimationTime = panel.multiAnimationTimes.computeIfAbsent(multiSetting, k -> System.currentTimeMillis());

                long currentTime = System.currentTimeMillis();
                float elapsed = Math.min(1f, (currentTime - lastAnimationTime) / ANIMATION_DURATION * 3);

                animationProgress = setAnimationDuration(isOpen, animationProgress, elapsed, -1);

                float smoothProgress;
                if (animationProgress < 0.5f) {
                    smoothProgress = 2 * animationProgress * animationProgress;
                } else {
                    float t = -2 * animationProgress + 2;
                    smoothProgress = 1 - t * t / 2;
                }

                panel.multiAnimations.put(multiSetting, animationProgress);
                panel.multiAnimationTimes.put(multiSetting, currentTime);

                String value = multiSetting.getValue().isEmpty() ? "None" : String.valueOf(multiSetting.getValue().size());
                float valueWidth = font.getWidth(value, 6);
                font.render(context.getMatrices(),
                        value,
                        posX + PANEL_WIDTH - 10 - valueWidth,
                        moduleY + 4,
                        6,
                        TEXT_DISABLED);

                if (animationProgress > 0) {
                    float optionY = moduleY + PANEL_HEIGHT;
                    for (String mode : multiSetting.getModes()) {
                        boolean isSelected = multiSetting.getSpecificValue(mode);

                        float alpha = (int) (smoothProgress * 255);
                        Color outlineColor = isSelected ?
                                new Color(255, 255, 255, (int) (100 * smoothProgress)) :
                                new Color(100, 100, 100, (int) (100 * smoothProgress));

                        Render2DEngine.drawRoundedOutline(context.getMatrices(),
                                posX + 6,
                                optionY,
                                PANEL_WIDTH - 12,
                                PANEL_HEIGHT - 2,
                                1,
                                1,
                                outlineColor);

                        font.render(context.getMatrices(),
                                mode,
                                posX + 10,
                                optionY + (PANEL_HEIGHT - 2) / 2 - font.getLineHeight() / 2 + 1,
                                6,
                                isSelected ?
                                        ((TEXT_PRIMARY & 0x00FFFFFF) | ((int) (alpha) << 24)) :
                                        ((TEXT_DISABLED & 0x00FFFFFF) | ((int) (alpha) << 24)));

                        optionY += PANEL_HEIGHT - 2;
                    }
                }
            }
            case ColorSetting colorSetting -> {
                Panel panel = selectedPanel;
                float animationProgress = panel.colorAnimations.computeIfAbsent(colorSetting, k -> 0f);
                long lastAnimationTime = panel.colorAnimationTimes.computeIfAbsent(colorSetting, k -> System.currentTimeMillis());
                boolean expanded = panel.colorSettingExpanded.computeIfAbsent(colorSetting, k -> false);

                long currentTime = System.currentTimeMillis();
                float elapsed = Math.min(1f, (currentTime - lastAnimationTime) / ANIMATION_DURATION * 3);
                animationProgress = setAnimationDuration(expanded, animationProgress, elapsed, -1);

                panel.colorAnimations.put(colorSetting, animationProgress);
                panel.colorAnimationTimes.put(colorSetting, currentTime);
                
                if (animationProgress > 0 && animationProgress < 1 && selectedModule != null && selectedPanel == panel) {
                    float targetHeight = PANEL_HEIGHT;
                    targetHeight += PANEL_HEIGHT + 2;

                    List<Setting<?>> settings = new ArrayList<>();
                    for (Setting<?> s : selectedModule.getSettingRepository().getSettings().values()) {
                        if (!(s instanceof SettingCategory)) {
                            settings.add(s);
                        }
                    }

                    targetHeight += (PANEL_HEIGHT + 2) * settings.size() + SETTING_PADDING;

                    for (Setting<?> s : settings) {
                        float extraHeight = getSettingHeight(s, panel) - PANEL_HEIGHT;
                        float padding = (s instanceof NumberSetting<?> || s instanceof RangeSetting) ? 2 : 0;
                        targetHeight += extraHeight + padding - 2;
                    }
                    
                    targetHeight += 2;
                    panel.updateHeight(targetHeight);
                }

                font.render(context.getMatrices(),
                    colorSetting.getName(),
                        posX + SETTING_PADDING,
                    moduleY + 4,
                    SETTING_TEXT_SIZE,
                    TEXT_SECONDARY);

                Render2DEngine.drawRoundedRect(context.getMatrices(),
                        posX + PANEL_WIDTH - TOGGLE_WIDTH - SETTING_PADDING - 2,
                    moduleY + (PANEL_HEIGHT - TOGGLE_HEIGHT) / 2,
                    TOGGLE_WIDTH,
                    TOGGLE_HEIGHT,
                    TOGGLE_HEIGHT / 2.3f,
                    colorSetting.getValue());

                if (expanded) {
                    float baseY = moduleY + PANEL_HEIGHT;
                    Render2DEngine.drawRoundedRect(context.getMatrices(),
                            posX + SETTING_PADDING,
                        baseY,
                        PANEL_WIDTH - SETTING_PADDING * 2,
                        60 * animationProgress,
                        2f,
                        new Color(0xFF141414));

                    float[] hsb = Color.RGBtoHSB(colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue(), null);
                    
                    Render2DEngine.drawColorPicker(context.getMatrices(),
                            posX + SETTING_PADDING,
                        baseY,
                        PANEL_WIDTH - SETTING_PADDING * 2,
                        60,
                        hsb[0],
                        255);

                    float hueSliderY = baseY + 65 * animationProgress;
                    float segmentWidth = (PANEL_WIDTH - SETTING_PADDING * 2) / 6.0f;
                    for (int i = 0; i < 6; i++) {
                        float hue1 = i / 6.0f;
                        float hue2 = (i + 1) / 6.0f;
                        Color color1 = Color.getHSBColor(hue1, 1.0f, 1.0f);
                        Color color2 = Color.getHSBColor(hue2, 1.0f, 1.0f);
                        
                        Render2DEngine.drawGradientRect(context.getMatrices(),
                                posX + SETTING_PADDING + (segmentWidth * i),
                            hueSliderY,
                            segmentWidth,
                            5,
                            color1,
                            color2);
                    }
                    float colorAlphaX = posX + SETTING_PADDING;
                    float colorAlphaY = hueSliderY + 10;
                    float alphaWidth = PANEL_WIDTH - SETTING_PADDING * 2;

                    Color baseColor = new Color(colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue());

                    Render2DEngine.drawGradientRect(context.getMatrices(), colorAlphaX, colorAlphaY, alphaWidth, 5,
                            new Color(baseColor.getRed() / 255f, baseColor.getGreen() / 255f, baseColor.getBlue() / 255f, 1 / 255f),
                            new Color(baseColor.getRed() / 255f, baseColor.getGreen() / 255f, baseColor.getBlue() / 255f, 255 / 255f));

                    if (draggedColorSetting == colorSetting) {
                        if (draggingHue) {
                            float hueX = posX + SETTING_PADDING + (PANEL_WIDTH - SETTING_PADDING * 2) * hsb[0];
                            Render2DEngine.drawRoundedRect(context.getMatrices(),
                                hueX - 2,
                                hueSliderY - 2,
                                4,
                                9,
                                2,
                                Color.WHITE);
                        }
                        if (draggingAlpha) {
                            float alphaX = posX + SETTING_PADDING + (PANEL_WIDTH - SETTING_PADDING * 2) * (colorSetting.getAlpha() / 255f);
                            Render2DEngine.drawRoundedRect(context.getMatrices(),
                                alphaX - 2,
                                colorAlphaY - 2,
                                4,
                                9,
                                2,
                                Color.WHITE);
                        }
                        if (draggingSaturationBrightness) {
                            float saturationX = posX + SETTING_PADDING + (PANEL_WIDTH - SETTING_PADDING * 2) * hsb[1];
                            float constrainedBrightness = Math.max(0, Math.min(1, hsb[2]));
                            
                            float brightnessY = baseY + 60 * (1 - constrainedBrightness);
                            
                            Render2DEngine.drawRoundedRect(context.getMatrices(),
                                saturationX - 2,
                                brightnessY - 2,
                                4,
                                4,
                                2,
                                Color.WHITE);
                        }
                    }
                }
            }
            default -> {
            }
        }
    }
    public float setAnimationDuration(boolean condition, float progress, float elapsed, float targetProgress) {
        if (condition) {
            progress = Math.min(targetProgress != -1 ? targetProgress : 1f, progress + elapsed);
        } else {
            progress = Math.max(targetProgress != -1 ? targetProgress : 0, progress - elapsed);
        }
        return progress;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int currentWidth = mc.getWindow().getScaledWidth();
        int currentHeight = mc.getWindow().getScaledHeight();
        
        if (currentWidth != lastScreenWidth || currentHeight != lastScreenHeight) {
            recenterPanels();
            lastScreenWidth = currentWidth;
            lastScreenHeight = currentHeight;
        }

        if (mc.world == null) super.render(context, mouseX, mouseY, delta);

        FontAtlas inter = Chorus.getInstance().getFonts().getInterSemiBold();
        FontAtlas icons = Chorus.getInstance().getFonts().getLucide();
        MatrixStack matrices = context.getMatrices();
        if (mc.player != null) {
            inter.render(matrices, "HUD Editor", mc.getWindow().getScaledWidth() - inter.getWidth("HUD Editor", 8) - 10, 10, 8, Color.WHITE.getRGB());
        }

        if (!Chorus.getInstance().isAuthenticated) {
            try {
                CrashUtil.exit(CrashUtil.Severity.SUSPICIOUS);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        boolean foundHoveredModule = false;
        long currentTime = System.currentTimeMillis();

        for (Panel panel : panels.values()) {
            if (panel.isOpen()) {
                Vector2f pos = panel.getPosition();
                float moduleY = pos.getY() + PANEL_HEIGHT + 2 - panel.getModuleScrollOffset();
                float moduleXOffset = -panel.getSettingsAnimationProgress() * PANEL_WIDTH;

                for (Module module : panel.getModules()) {
                    if (mouseX >= pos.getX() + moduleXOffset && mouseX <= pos.getX() + PANEL_WIDTH + moduleXOffset &&
                        mouseY >= moduleY && mouseY <= moduleY + PANEL_HEIGHT &&
                        mouseY >= pos.getY() + PANEL_HEIGHT + 2 && mouseY <= pos.getY() + panel.getCurrentHeight()) {
                        
                        foundHoveredModule = true;
                        if (hoveredModule != module) {
                            hoveredModule = module;
                            hoverStartTime = currentTime;
                            showingTooltip = false;
                        } else if (currentTime - hoverStartTime >= TOOLTIP_DELAY && !showingTooltip) {
                            showingTooltip = true;
                        }
                        break;
                    }
                    moduleY += PANEL_HEIGHT + 2;
                }
            }
        }

        if (!foundHoveredModule) {
            hoveredModule = null;
            showingTooltip = false;
        }

        for (Panel panel : panels.values()) {
            if (panel.isOpen()) {
                float contentHeight = (PANEL_HEIGHT + 2) * panel.getModules().size();
                float panelBottom = panel.getPosition().getY() + contentHeight + PANEL_HEIGHT + 4;
                float screenHeight = mc.getWindow().getScaledHeight();
                
                if (panelBottom > screenHeight) {
                    float visibleHeight = screenHeight - panel.getPosition().getY() - PANEL_HEIGHT - 4;
                    panel.maxModuleScrollOffset = Math.max(0, contentHeight - visibleHeight);
                } else {
                    panel.maxModuleScrollOffset = 0;
                }
                panel.setModuleScrollOffset(Math.max(0, Math.min(panel.getModuleScrollOffset(), panel.getMaxModuleScrollOffset())));

                if (panel.isShowingSettings() && selectedModule != null && selectedPanel == panel) {
                    float settingsContentHeight = PANEL_HEIGHT + 4;
                    List<Setting<?>> settings = new ArrayList<>();
                    for (Setting<?> setting : selectedModule.getSettingRepository().getSettings().values()) {
                        if (!(setting instanceof SettingCategory)) {
                            settings.add(setting);
                            float settingHeight = getSettingHeight(setting, panel);
                            float padding = (setting instanceof NumberSetting<?> || setting instanceof RangeSetting) ? 2 : 0;
                            settingsContentHeight += settingHeight + padding;
                        }
                    }

                    float visibleSettingsHeight = screenHeight - panel.getPosition().getY() - PANEL_HEIGHT - 4;
                    panel.maxSettingScrollOffset = Math.max(0, settingsContentHeight - visibleSettingsHeight);
                    panel.settingScrollOffset = Math.max(0, Math.min(panel.settingScrollOffset, panel.maxSettingScrollOffset));
                }
            }

            if (panel.isShowingSettings()) {
                if (panel.getSettingsAnimationProgress() < 1f) {
                    float elapsed = (currentTime - panel.lastSettingsAnimationTime) / (ANIMATION_DURATION * 0.2f);
                    panel.setSettingsAnimationProgress(Math.min(1f, panel.getSettingsAnimationProgress() + elapsed));
                    panel.lastSettingsAnimationTime = currentTime;
                }
            } else {
                if (panel.getSettingsAnimationProgress() > 0f) {
                    float elapsed = (currentTime - panel.lastSettingsAnimationTime) / (ANIMATION_DURATION * 0.2f);
                    panel.setSettingsAnimationProgress(Math.max(0f, panel.getSettingsAnimationProgress() - elapsed));
                    panel.lastSettingsAnimationTime = currentTime;
                }
            }

            float targetHeight = PANEL_HEIGHT;
            if (panel.isOpen()) {
                if (panel.isShowingSettings() && selectedModule != null && selectedPanel == panel) {
                    targetHeight += PANEL_HEIGHT + 2;

                    List<Setting<?>> settings = new ArrayList<>();
                    for (Setting<?> setting : selectedModule.getSettingRepository().getSettings().values()) {
                        if (!(setting instanceof SettingCategory)) {
                            settings.add(setting);
                        }
                    }

                    targetHeight += (PANEL_HEIGHT + 2) * settings.size() + SETTING_PADDING;

                    for (Setting<?> setting : settings) {
                        float extraHeight = getSettingHeight(setting, panel) - PANEL_HEIGHT;
                        float padding = (setting instanceof NumberSetting<?> || setting instanceof RangeSetting) ? 2 : 0;
                        targetHeight += extraHeight + padding - 2;
                    }
                } else {
                    targetHeight += (PANEL_HEIGHT + 2) * panel.getModules().size();
                }
            }
            targetHeight += 2;

            panel.updateHeight(targetHeight);
            panel.animateHeight();

            Vector2f position = panel.getPosition();
            String icon = panel.getCategory().getLucideIcon();
            float posX = position.getX();
            float posY = position.getY();
            float panelHeight = panel.getCurrentHeight();

            Render2DEngine.drawBlurredRoundedRect(matrices,
                    posX,
                    posY,
                    PANEL_WIDTH,
                    panelHeight + 1,
                    5,
                8,
                BACKGROUND_COLOR);

            Render2DEngine.drawRoundedOutline(matrices,
                    posX,
                posY,
                PANEL_WIDTH,
                    panelHeight,
                5,
                1,
                OUTLINE_COLOR);


            inter.render(matrices,
                panel.getCategory().getName(),
                posX + 6,
                posY + (PANEL_HEIGHT / 2) - inter.getLineHeight() / 2.5f,
                TITLE_TEXT_SIZE,
                TEXT_PRIMARY);
            

            float iconWidth = icons.getWidth(icon, 8);

            icons.render(matrices,
                icon,
                posX + PANEL_WIDTH - 6 - iconWidth,
                posY + (PANEL_HEIGHT / 2) - inter.getLineHeight() / 2.5f,
                TITLE_TEXT_SIZE,
                TEXT_PRIMARY);

            float moduleY = posY + PANEL_HEIGHT + 2 - panel.getModuleScrollOffset();
            if (panel.isOpen()) {
                context.enableScissor(
                    (int) posX + 1,
                    (int) (posY + PANEL_HEIGHT + 2),
                    (int) (posX + PANEL_WIDTH - 1),
                    (int) (posY + panelHeight)
                );

                float settingsOffset = (1f - easeInOutCubic(panel.getSettingsAnimationProgress())) * PANEL_WIDTH;
                float moduleXOffset = -easeInOutCubic(panel.getSettingsAnimationProgress()) * PANEL_WIDTH;

                if (!panel.isShowingSettings() || panel.getSettingsAnimationProgress() < 1f) {
                    for (Module module : panel.getModules()) {
                        if (moduleY + PANEL_HEIGHT >= posY + PANEL_HEIGHT + 2 &&
                            moduleY <= posY + panelHeight) {
                            Render2DEngine.drawRoundedOutline(matrices,
                                posX + 2 + moduleXOffset,
                                moduleY,
                                PANEL_WIDTH - 4,
                                PANEL_HEIGHT,
                                2,
                                OUTLINE_THICKNESS,
                                OUTLINE_COLOR);

                            if (module.isEnabled()) {
                                Render2DEngine.drawRoundedRect(matrices,
                                    posX + 2 + moduleXOffset,
                                    moduleY,
                                    PANEL_WIDTH - 4,
                                    PANEL_HEIGHT,
                                    2,
                                    OUTLINE_COLOR);
                            }

                            String bind = InputUtils.getKeyName(module.getKey());
                            if (!bind.equals("None")) {
                                float bindLength = inter.getWidth(bind, 7f);
                                float bindX = posX + 2 + moduleXOffset + PANEL_WIDTH - bindLength - 20;
                                Render2DEngine.drawRoundedOutline(matrices,
                                        bindX,
                                        moduleY + (PANEL_HEIGHT / 2) - inter.getLineHeight(7f) / 2,
                                        bindLength + 5,
                                        PANEL_HEIGHT / 2f,
                                        2,
                                        OUTLINE_THICKNESS,
                                        OUTLINE_COLOR);
                                inter.render(matrices,
                                        bind,
                                        bindX + 2.5f,
                                        moduleY + (PANEL_HEIGHT / 2) - inter.getLineHeight(7f) / 2,
                                        7f,
                                        module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA
                                );
                            }

                            inter.renderWithShadow(matrices,
                                module.getName(),
                                posX + 6 + moduleXOffset,
                                moduleY + (PANEL_HEIGHT / 2) - inter.getLineHeight() / 2,
                                8,
                                module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA
                            );

                            if (!module.getSettingRepository().getSettings().isEmpty()) {
                                icons.render(matrices,
                                        "",
                                        posX + moduleXOffset + PANEL_WIDTH - 14,
                                        moduleY + (PANEL_HEIGHT / 2) - inter.getLineHeight() / 2,
                                        10,
                                        module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA);
                            }
                        }
                        moduleY += PANEL_HEIGHT + 2;
                    }
                }

                if (panel.isShowingSettings() && selectedModule != null && selectedPanel == panel) {
                    moduleY = posY + PANEL_HEIGHT + 2 - panel.settingScrollOffset;
                    float settingsX = posX + settingsOffset;

                    inter.render(matrices,
                        selectedModule.getName(),
                        settingsX + 6,
                        moduleY + (PANEL_HEIGHT / 2) - inter.getLineHeight() / 2,
                        7,
                        selectedModule.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA);

                    String backArrow = "\ue0ab";
                    float arrowWidth = icons.getWidth(backArrow, 8);
                    icons.render(matrices,
                        backArrow,
                        settingsX + PANEL_WIDTH - 10 - arrowWidth,
                        moduleY + (PANEL_HEIGHT / 2) - inter.getLineHeight() / 2,
                        8,
                        0xFF909090);

                    Render2DEngine.drawLine(matrices,
                        settingsX + 4,
                        moduleY + PANEL_HEIGHT,
                        settingsX + PANEL_WIDTH - 4,
                        moduleY + PANEL_HEIGHT,
                        1,
                        new Color(100, 100, 100, 100));

                    moduleY += PANEL_HEIGHT + 4;

                    List<Setting<?>> settings = new ArrayList<>();

                    for (Setting<?> setting : selectedModule.getSettingRepository().getSettings().values()) {
                        if (!(setting instanceof SettingCategory)) {
                            settings.add(setting);
                        }
                    }

                    context.enableScissor(
                        (int) settingsX + 1,
                        (int) (posY + PANEL_HEIGHT + 2),
                        (int) (settingsX + PANEL_WIDTH - 1),
                        (int) (posY + panelHeight)
                    );

                    for (Setting<?> setting : settings) {
                        if (moduleY + getSettingHeight(setting, panel) >= posY + PANEL_HEIGHT + 2 &&
                            moduleY <= posY + panelHeight) {
                            renderSetting(setting, context, new Vector2f(settingsX, posY), moduleY, inter);
                        }
                        float padding = (setting instanceof NumberSetting<?> || setting instanceof RangeSetting) ? 2 : 0;
                        moduleY += getSettingHeight(setting, panel) + padding;
                    }

                    context.disableScissor();
                }

                context.disableScissor();
            }

            if (panel.isDragging()) {
                panel.updatePosition(mouseX, mouseY);
            }
        }

        if (showingTooltip && hoveredModule != null && hoveredModule.getDescription() != null) {
            String description = hoveredModule.getDescription();
            float tooltipWidth = inter.getWidth(description, TOOLTIP_TEXT_SIZE) + TOOLTIP_PADDING * 3;
            float tooltipHeight = inter.getLineHeight(TOOLTIP_TEXT_SIZE) + TOOLTIP_PADDING * 2;
            
            float tooltipX = Math.min(mouseX + 10, mc.getWindow().getScaledWidth() - tooltipWidth - 5);
            float tooltipY = Math.min(mouseY + 10, mc.getWindow().getScaledHeight() - tooltipHeight - 5);

            Render2DEngine.drawBlurredRoundedRect(matrices,
                tooltipX,
                tooltipY,
                tooltipWidth,
                tooltipHeight,
                4,
                8,
                BACKGROUND_COLOR);

            Render2DEngine.drawRoundedOutline(matrices,
                tooltipX,
                tooltipY,
                tooltipWidth,
                tooltipHeight,
                4,
                1,
                OUTLINE_COLOR);

            inter.render(matrices,
                description,
                tooltipX + TOOLTIP_PADDING,
                tooltipY + TOOLTIP_PADDING,
                TOOLTIP_TEXT_SIZE,
                TEXT_PRIMARY);
        }
    }

    private float getSettingHeight(Setting<?> setting, Panel panel) {
        float height = PANEL_HEIGHT;
        if (!setting.shouldRender()) return 0;

        switch (setting) {
            case RangeSetting<?> rangeSetting -> height += 2;
            case NumberSetting<?> numberSetting -> height += 2;
            case ColorSetting colorSetting -> {
                boolean expanded = panel.colorSettingExpanded.getOrDefault(colorSetting, false);
                float animationProgress = panel.colorAnimations.getOrDefault(colorSetting, expanded ? 1f : 0f);
                if (expanded || animationProgress > 0) {
                    height += COLOR_PICKER_HEIGHT * animationProgress;
                }
            }
            case ModeSetting modeSetting -> {
                if (panel.getOpenModeSetting() == modeSetting && !modeSetting.getModes().isEmpty()) {
                    height += (PANEL_HEIGHT - 2) * modeSetting.getModes().size();
                }
            }
            case MultiSetting multiSetting -> {
                if (panel.getOpenMultiSetting() == multiSetting && !multiSetting.getModes().isEmpty()) {
                    height += (PANEL_HEIGHT - 2) * multiSetting.getModes().size();
                }
            }
            default -> {}
        }

        return height;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mc.player != null) {
                FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();
                String hudEditorText = "HUD Editor";
                float hudEditorWidth = font.getWidth(hudEditorText, 8);
                float hudEditorX = mc.getWindow().getScaledWidth() - hudEditorWidth - 10;
                float hudEditorY = 10;
                
                if (mouseX >= hudEditorX && mouseX <= hudEditorX + hudEditorWidth &&
                    mouseY >= hudEditorY && mouseY <= hudEditorY + font.getLineHeight(8)) {
                    mc.setScreen(HUDEditorScreen.getINSTANCE());
                    Chorus.getInstance().getModuleManager().getModule(ClickGUI.class).onDisable();
                    return true;
                }
            }

            for (Panel panel : panels.values()) {
                Vector2f pos = panel.getPosition();

                if (mouseX >= pos.getX() && mouseX <= pos.getX() + PANEL_WIDTH &&
                    mouseY >= pos.getY() && mouseY <= pos.getY() + PANEL_HEIGHT) {
                    panel.startDragging(mouseX, mouseY);
                    return true;
                }

                if (!panel.isOpen()) continue;
                float moduleY = pos.getY() + PANEL_HEIGHT + 2;
                float adjustedMouseY = (float)mouseY + panel.getModuleScrollOffset();

                if (!panel.isShowingSettings()) {
                    float moduleXOffset = -panel.getSettingsAnimationProgress() * PANEL_WIDTH;
                    for (Module module : panel.getModules()) {
                        if (mouseX >= pos.getX() + moduleXOffset && mouseX <= pos.getX() + PANEL_WIDTH + moduleXOffset &&
                            adjustedMouseY >= moduleY && adjustedMouseY <= moduleY + PANEL_HEIGHT &&
                            mouseY >= pos.getY() + PANEL_HEIGHT + 2 && mouseY <= pos.getY() + panel.getCurrentHeight()) {
                            if (module.isEnabled()) {
                                module.onDisable();
                            } else {
                                module.onEnable();
                            }
                            return true;
                        }
                        moduleY += PANEL_HEIGHT + 2;
                    }
                } else if (selectedModule != null && selectedPanel == panel) {
                    float settingsOffset = (1f - panel.getSettingsAnimationProgress()) * PANEL_WIDTH;
                    float settingsX = pos.getX() + settingsOffset;
                    float adjustedSettingsMouseY = (float)mouseY + panel.settingScrollOffset;

                if (mouseX >= settingsX && mouseX <= settingsX + PANEL_WIDTH &&
                    mouseY >= moduleY && mouseY <= moduleY + PANEL_HEIGHT) {
                    panel.setShowingSettings(false);
                    panel.lastSettingsAnimationTime = System.currentTimeMillis();
                    selectedModule = null;
                    selectedPanel = null;
                    return true;
                }
                moduleY += PANEL_HEIGHT + 4;

                List<Setting<?>> settings = new ArrayList<>();
                for (Setting<?> setting : selectedModule.getSettingRepository().getSettings().values()) {
                    if (!(setting instanceof SettingCategory)) {
                        settings.add(setting);
                    }
                }

                for (Setting<?> setting : settings) {
                    float settingHeight = getSettingHeight(setting, panel);

                    if (mouseX >= settingsX + 4 && mouseX <= settingsX + PANEL_WIDTH - 4 &&
                        adjustedSettingsMouseY >= moduleY && adjustedSettingsMouseY <= moduleY + settingHeight &&
                        mouseY >= pos.getY() + PANEL_HEIGHT + 2 && mouseY <= pos.getY() + panel.getCurrentHeight()) {

                        switch (setting) {
                            case BooleanSetting booleanSetting -> {
                                float toggleWidth = 18;
                                float toggleHeight = 9;
                                float toggleY = moduleY + (PANEL_HEIGHT - toggleHeight) / 2;

                                if (mouseX >= settingsX + PANEL_WIDTH - toggleWidth - 8 &&
                                    mouseX <= settingsX + PANEL_WIDTH - 8 &&
                                    adjustedSettingsMouseY >= toggleY && adjustedSettingsMouseY <= toggleY + toggleHeight) {
                                    booleanSetting.toggle();
                                    panel.booleanAnimationTimes.put(booleanSetting, System.currentTimeMillis());
                                    return true;
                                }
                            }
                            case NumberSetting<?> numberSetting -> {
                                float sliderY = moduleY + PANEL_HEIGHT;
                                if (adjustedSettingsMouseY >= sliderY - 5 && adjustedSettingsMouseY <= sliderY + 7) {
                                    draggingNumberSetting = numberSetting;
                                    float relativeX = (float) (mouseX - (settingsX + 6));
                                    float totalWidth = PANEL_WIDTH - 12;
                                    float percentage = Math.max(0, Math.min(1, relativeX / totalWidth));

                                    float range = numberSetting.getMaxValue().floatValue() - numberSetting.getMinValue().floatValue();
                                    float newValue = numberSetting.getMinValue().floatValue() + (range * percentage);

                                    if (numberSetting.getValue() instanceof Integer) {
                                        @SuppressWarnings("unchecked")
                                        NumberSetting<Integer> intSetting = (NumberSetting<Integer>) numberSetting;
                                        intSetting.setValue((int) Math.round(newValue));
                                    } else if (numberSetting.getValue() instanceof Float) {
                                        @SuppressWarnings("unchecked")
                                        NumberSetting<Float> floatSetting = (NumberSetting<Float>) numberSetting;
                                        floatSetting.setValue(newValue);
                                    } else if (numberSetting.getValue() instanceof Double) {
                                        @SuppressWarnings("unchecked")
                                        NumberSetting<Double> doubleSetting = (NumberSetting<Double>) numberSetting;
                                        doubleSetting.setValue((double) newValue);
                                    }
                                    return true;
                                }
                            }
                            case RangeSetting rangeSetting -> {
                                float sliderY = moduleY + PANEL_HEIGHT;
                                if (adjustedSettingsMouseY >= sliderY - 5 && adjustedSettingsMouseY <= sliderY + 7) {
                                    draggingRangeSetting = rangeSetting;
                                    float relativeX = (float) (mouseX - (settingsX + 6));
                                    float totalWidth = PANEL_WIDTH - 12;
                                    float percentage = Math.max(0, Math.min(1, relativeX / totalWidth));

                                    float minProgress = (rangeSetting.getValueMin().floatValue() - rangeSetting.getMin().floatValue()) /
                                            (rangeSetting.getMax().floatValue() - rangeSetting.getMin().floatValue());
                                    float maxProgress = (rangeSetting.getValueMax().floatValue() - rangeSetting.getMin().floatValue()) /
                                            (rangeSetting.getMax().floatValue() - rangeSetting.getMin().floatValue());

                                    draggingRangeMin = Math.abs(percentage - minProgress) < Math.abs(percentage - maxProgress);

                                    float range = rangeSetting.getMax().floatValue() - rangeSetting.getMin().floatValue();
                                    Number newValue = switch (rangeSetting.getMin()) {
                                        case Double v -> rangeSetting.getMin().doubleValue() + (range * percentage);
                                        case Float v -> rangeSetting.getMin().floatValue() + (range * percentage);
                                        case Integer i -> (int) (rangeSetting.getMin().intValue() + (range * percentage));
                                        case null, default -> rangeSetting.getMin().floatValue() + (range * percentage);
                                    };

                                        Number[] currentValues = rangeSetting.getValue();
                                        if (draggingRangeMin) {
                                            if (newValue.floatValue() <= currentValues[1].floatValue()) {
                                                rangeSetting.setValue(new Number[]{newValue, currentValues[1]});
                                            }
                                        } else {
                                            if (newValue.floatValue() >= currentValues[0].floatValue()) {
                                                rangeSetting.setValue(new Number[]{currentValues[0], newValue});
                                            }
                                        }
                                        return true;
                                    }
                                }
                                case ModeSetting modeSetting -> {
                                    if (adjustedSettingsMouseY <= moduleY + PANEL_HEIGHT) {
                                        panel.setOpenModeSetting(panel.getOpenModeSetting() == modeSetting ? null : modeSetting);
                                        return true;
                                    }
                                }
                                case MultiSetting multiSetting -> {
                                    if (adjustedSettingsMouseY <= moduleY + PANEL_HEIGHT) {
                                        panel.setOpenMultiSetting(panel.getOpenMultiSetting() == multiSetting ? null : multiSetting);
                                        return true;
                                    }
                                }
                                case ColorSetting colorSetting -> {
                                    if (handleSettingClick(setting, panel, moduleY, settingsX, mouseX, adjustedSettingsMouseY)) {
                                        return true;
                                    }
                                }
                                default -> {
                                }
                            }
                        }

                        if ((setting instanceof ModeSetting modeSetting && panel.getOpenModeSetting() == modeSetting) ||
                            (setting instanceof MultiSetting multiSetting && panel.getOpenMultiSetting() == multiSetting)) {
                            float optionY = moduleY + PANEL_HEIGHT;
                            List<String> modes = setting instanceof ModeSetting ?
                                ((ModeSetting) setting).getModes() :
                                ((MultiSetting) setting).getModes();

                            for (String mode : modes) {
                                if (mouseX >= settingsX + 6 && mouseX <= settingsX + PANEL_WIDTH - 6 &&
                                    adjustedSettingsMouseY >= optionY && adjustedSettingsMouseY <= optionY + PANEL_HEIGHT - 2 &&
                                    mouseY >= pos.getY() + PANEL_HEIGHT + 2 && mouseY <= pos.getY() + panel.getCurrentHeight()) {
                                    if (setting instanceof ModeSetting modeSetting2) {
                                        modeSetting2.setValue(mode);
                                    } else if (setting instanceof MultiSetting multiSetting2) {
                                        if (multiSetting2.getSpecificValue(mode)) {
                                            multiSetting2.deselectMode(mode);
                                        } else {
                                            multiSetting2.selectMode(mode);
                                        }
                                    }
                                    return true;
                                }
                                optionY += PANEL_HEIGHT - 2;
                            }
                        }

                        float padding = (setting instanceof NumberSetting<?> || setting instanceof RangeSetting) ? 2 : 0;
                        moduleY += settingHeight + padding;
                    }
                }
            }
        } else if (button == 1) {
            for (Panel panel : panels.values()) {
                Vector2f pos = panel.getPosition();

                if (mouseX >= pos.getX() && mouseX <= pos.getX() + PANEL_WIDTH &&
                    mouseY >= pos.getY() && mouseY <= pos.getY() + PANEL_HEIGHT) {
                    panel.setOpen(!panel.isOpen());
                    if (!panel.isOpen()) {
                        panel.setShowingSettings(false);
                        if (selectedPanel == panel) {
                            selectedModule = null;
                            selectedPanel = null;
                        }
                    }
                    return true;
                }

                if (panel.isOpen()) {
                    float moduleY = pos.getY() + PANEL_HEIGHT + 2;
                    float moduleXOffset = -panel.getSettingsAnimationProgress() * PANEL_WIDTH;
                    float adjustedMouseY = (float)mouseY + panel.getModuleScrollOffset();

                    if (panel.isShowingSettings() && selectedModule != null && selectedPanel == panel) {
                        float settingsOffset = (1f - panel.getSettingsAnimationProgress()) * PANEL_WIDTH;
                        float settingsX = pos.getX() + settingsOffset;

                        if (mouseX >= settingsX && mouseX <= settingsX + PANEL_WIDTH &&
                            mouseY >= moduleY && mouseY <= moduleY + PANEL_HEIGHT) {
                            panel.setShowingSettings(false);
                            panel.lastSettingsAnimationTime = System.currentTimeMillis();
                            selectedModule = null;
                            selectedPanel = null;
                            return true;
                        }
                    } else {
                        for (Module module : panel.getModules()) {
                            if (mouseX >= pos.getX() + moduleXOffset && mouseX <= pos.getX() + PANEL_WIDTH + moduleXOffset &&
                                adjustedMouseY >= moduleY && adjustedMouseY <= moduleY + PANEL_HEIGHT &&
                                mouseY >= pos.getY() + PANEL_HEIGHT + 2 && mouseY <= pos.getY() + panel.getCurrentHeight() &&
                                !module.getSettingRepository().getSettings().isEmpty()) {
                                for (Panel otherPanel : panels.values()) {
                                    if (otherPanel != panel) {
                                        otherPanel.setShowingSettings(false);
                                    }
                                }
                                panel.setShowingSettings(true);
                                panel.lastSettingsAnimationTime = System.currentTimeMillis();
                                selectedModule = module;
                                selectedPanel = panel;
                                return true;
                            }
                            moduleY += PANEL_HEIGHT + 2;
                        }
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Panel panel : panels.values()) {
                if (panel.isDragging()) {
                    panel.setDragging(false);
                    return true;
                }
            }

            if (draggingNumberSetting != null || draggingRangeSetting != null) {
                draggingNumberSetting = null;
                draggingRangeSetting = null;
                return true;
            }
        }
        draggedColorSetting = null;
        draggingHue = false;
        draggingAlpha = false;
        draggingSaturationBrightness = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        Chorus.getInstance().getModuleManager().getModule(ClickGUI.class).onDisable();
        super.close();
    }

    @Override
    public void onDisplayed() {
        for (Panel panel : panels.values()) {
            panel.setCurrentHeight(0);
            panel.updateHeight(panel.isOpen() ? 
                PANEL_HEIGHT + (PANEL_HEIGHT + 2) * panel.getModules().size() + 2 : 
                PANEL_HEIGHT + 2);
            panel.lastHeightAnimationTime = System.currentTimeMillis();
            panel.animationProgress = 0f;
            
            for (BooleanSetting setting : panel.booleanAnimations.keySet()) {
                panel.booleanAnimations.put(setting, setting.getValue() ? 1f : 0f);
                panel.booleanAnimationTimes.put(setting, System.currentTimeMillis());
            }
            
            for (ModeSetting setting : panel.modeAnimations.keySet()) {
                panel.modeAnimations.put(setting, panel.getOpenModeSetting() == setting ? 1f : 0f);
                panel.modeAnimationTimes.put(setting, System.currentTimeMillis());
            }
            
            for (MultiSetting setting : panel.multiAnimations.keySet()) {
                panel.multiAnimations.put(setting, panel.getOpenMultiSetting() == setting ? 1f : 0f);
                panel.multiAnimationTimes.put(setting, System.currentTimeMillis());
            }
            
            for (NumberSetting<?> setting : panel.numberAnimations.keySet()) {
                double progress = (setting.getValue().doubleValue() - setting.getMinValue().doubleValue()) /
                        (setting.getMaxValue().doubleValue() - setting.getMinValue().doubleValue());
                panel.numberAnimations.put(setting, (float) progress);
                panel.numberAnimationTimes.put(setting, System.currentTimeMillis());
            }
            
            for (RangeSetting<?> setting : panel.rangeAnimations.keySet()) {
                double minProgress = (setting.getValueMin().doubleValue() - setting.getMin().doubleValue()) /
                        (setting.getMax().doubleValue() - setting.getMin().doubleValue());
                double maxProgress = (setting.getValueMax().doubleValue() - setting.getMin().doubleValue()) /
                        (setting.getMax().doubleValue() - setting.getMin().doubleValue());
                panel.rangeAnimations.put(setting, new Float[]{(float) minProgress, (float) maxProgress});
                panel.rangeAnimationTimes.put(setting, System.currentTimeMillis());
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            for (Panel panel : panels.values()) {
                if (panel.isDragging()) {
                    panel.updatePosition(mouseX, mouseY);
                    return true;
                }
            }

            if (draggingNumberSetting != null) {
                Vector2f pos = selectedPanel.getPosition();
                float settingsOffset = (1f - selectedPanel.getSettingsAnimationProgress()) * PANEL_WIDTH;
                float settingsX = pos.getX() + settingsOffset;
                float relativeX = (float) (mouseX - (settingsX + 6));
                float totalWidth = PANEL_WIDTH - 12;
                float percentage = Math.max(0, Math.min(1, relativeX / totalWidth));
                
                float range = draggingNumberSetting.getMaxValue().floatValue() - draggingNumberSetting.getMinValue().floatValue();
                float newValue = draggingNumberSetting.getMinValue().floatValue() + (range * percentage);
                if (draggingNumberSetting.getValue() instanceof Integer) {
                    @SuppressWarnings("unchecked")
                    NumberSetting<Integer> intSetting = (NumberSetting<Integer>) draggingNumberSetting;
                    intSetting.setValue((int) Math.round(newValue));
                } else if (draggingNumberSetting.getValue() instanceof Float) {
                    @SuppressWarnings("unchecked")
                    NumberSetting<Float> floatSetting = (NumberSetting<Float>) draggingNumberSetting;
                    floatSetting.setValue(newValue);
                } else if (draggingNumberSetting.getValue() instanceof Double) {
                    @SuppressWarnings("unchecked")
                    NumberSetting<Double> doubleSetting = (NumberSetting<Double>) draggingNumberSetting;
                    doubleSetting.setValue((double) newValue);
                }
                return true;
            }
            if (draggingRangeSetting != null) {
                Vector2f pos = selectedPanel.getPosition();
                float settingsOffset = (1f - selectedPanel.getSettingsAnimationProgress()) * PANEL_WIDTH;
                float settingsX = pos.getX() + settingsOffset;
                float relativeX = (float) (mouseX - (settingsX + 6));
                float totalWidth = PANEL_WIDTH - 12;
                float percentage = Math.max(0, Math.min(1, relativeX / totalWidth));
                
                float range = draggingRangeSetting.getMax().floatValue() - draggingRangeSetting.getMin().floatValue();
                Number newValue;
                if (draggingRangeSetting.getMin() instanceof Double) {
                    newValue = draggingRangeSetting.getMin().doubleValue() + (range * percentage);
                } else if (draggingRangeSetting.getMin() instanceof Float) {
                    newValue = draggingRangeSetting.getMin().floatValue() + (range * percentage);
                } else if (draggingRangeSetting.getMin() instanceof Integer) {
                    newValue = (int)(draggingRangeSetting.getMin().intValue() + (range * percentage));
                } else {
                    newValue = draggingRangeSetting.getMin().floatValue() + (range * percentage);
                }
                Number[] currentValues = draggingRangeSetting.getValue();
                if (draggingRangeMin) {
                    if (newValue.floatValue() <= currentValues[1].floatValue()) {
                        draggingRangeSetting.setValue(new Number[] { newValue, currentValues[1] });
                    }
                } else {
                    if (newValue.floatValue() >= currentValues[0].floatValue()) {
                        draggingRangeSetting.setValue(new Number[] { currentValues[0], newValue });
                    }
                }
                return true;
            }
        }
        if (draggedColorSetting != null && selectedPanel != null && selectedModule != null) {
            Vector2f pos = selectedPanel.getPosition();
            float settingsOffset = (1f - selectedPanel.getSettingsAnimationProgress()) * PANEL_WIDTH;
            float settingsX = pos.getX() + settingsOffset;
            
            float moduleY = pos.getY() + PANEL_HEIGHT + 2;
            
            moduleY += PANEL_HEIGHT + 4;
            
            for (Setting<?> setting : selectedModule.getSettingRepository().getSettings().values()) {
                if (!(setting instanceof SettingCategory)) {
                    if (setting == draggedColorSetting) {
                        float baseY = moduleY + PANEL_HEIGHT;
                        updateColorSetting(mouseX, mouseY, settingsX, baseY);
                        break;
                    }
                    float padding = (setting instanceof NumberSetting<?> || setting instanceof RangeSetting) ? 2 : 0;
                    moduleY += getSettingHeight(setting, selectedPanel) + padding;
                }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private boolean handleSettingClick(Setting<?> setting, Panel panel, float moduleY, float settingsX, double mouseX, double mouseY) {
        if (setting instanceof ColorSetting colorSetting) {
            float toggleY = moduleY + (PANEL_HEIGHT - TOGGLE_HEIGHT) / 2;

            if (mouseX >= settingsX + PANEL_WIDTH - TOGGLE_WIDTH - SETTING_PADDING - 2 &&
                mouseX <= settingsX + PANEL_WIDTH - SETTING_PADDING - 2 &&
                mouseY >= toggleY && mouseY <= toggleY + TOGGLE_HEIGHT) {
                boolean newExpandedState = !panel.colorSettingExpanded.getOrDefault(colorSetting, false);
                panel.colorSettingExpanded.put(colorSetting, newExpandedState);
                panel.colorAnimationTimes.put(colorSetting, System.currentTimeMillis());

                panel.colorAnimations.put(colorSetting, newExpandedState ? 0f : 1f);

                if (selectedModule != null && selectedPanel == panel) {
                    float targetHeight = PANEL_HEIGHT;
                    targetHeight += PANEL_HEIGHT + 2;

                    List<Setting<?>> settings = new ArrayList<>();
                    for (Setting<?> s : selectedModule.getSettingRepository().getSettings().values()) {
                        if (!(s instanceof SettingCategory)) {
                            settings.add(s);
                        }
                    }

                    targetHeight += (PANEL_HEIGHT + 2) * settings.size() + SETTING_PADDING;

                    for (Setting<?> s : settings) {
                        float extraHeight = getSettingHeight(s, panel) - PANEL_HEIGHT;
                        float padding = (s instanceof NumberSetting<?> || s instanceof RangeSetting) ? 2 : 0;
                        targetHeight += extraHeight + padding - 2;
                    }

                    targetHeight += 2;
                    panel.updateHeight(targetHeight);
                }

                return true;
            }

            if (panel.colorSettingExpanded.getOrDefault(colorSetting, false)) {
                float baseY = moduleY + PANEL_HEIGHT;

                if (mouseY >= baseY && mouseY <= baseY + 60) {
                    draggedColorSetting = colorSetting;
                    draggingSaturationBrightness = true;
                    updateColorSetting(mouseX, mouseY, settingsX, baseY);
                    return true;
                }

                float hueSliderY = baseY + 65;
                if (mouseY >= hueSliderY && mouseY <= hueSliderY + 5) {
                    draggedColorSetting = colorSetting;
                    draggingHue = true;
                    updateColorSetting(mouseX, mouseY, settingsX, baseY);
                    return true;
                }

                float alphaSliderY = hueSliderY + 10;
                if (mouseY >= alphaSliderY && mouseY <= alphaSliderY + 5) {
                    draggedColorSetting = colorSetting;
                    draggingAlpha = true;
                    updateColorSetting(mouseX, mouseY, settingsX, baseY);
                    return true;
                }
            }
        }
        return false;
    }

    private void updateColorSetting(double mouseX, double mouseY, float settingsX, float baseY) {
        if (draggedColorSetting == null) return;

        double constrainedMouseX = Math.max(settingsX + SETTING_PADDING, Math.min(settingsX + PANEL_WIDTH - SETTING_PADDING, mouseX));

        int red = draggedColorSetting.getRed();
        int green = draggedColorSetting.getGreen();
        int blue = draggedColorSetting.getBlue();

        float[] hsb = Color.RGBtoHSB(red, green, blue, null);

        if (draggingSaturationBrightness) {
            float saturation = Math.max(0, Math.min(1, (float) (constrainedMouseX - (settingsX + SETTING_PADDING)) / (PANEL_WIDTH - SETTING_PADDING * 2)));
            double constrainedMouseY = Math.max(baseY, Math.min(baseY + 60, mouseY));
            float brightness = Math.max(0, Math.min(1, 1 - (float) (constrainedMouseY - baseY) / 60));
            Color newColor = Color.getHSBColor(hsb[0], saturation, brightness);
            draggedColorSetting.setColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), draggedColorSetting.getAlpha());
        }

        if (draggingHue) {
            float hue = Math.max(0, Math.min(1, (float) (constrainedMouseX - (settingsX + SETTING_PADDING)) / (PANEL_WIDTH - SETTING_PADDING * 2)));
            Color newColor = Color.getHSBColor(hue, hsb[1], hsb[2]);
            draggedColorSetting.setColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), draggedColorSetting.getAlpha());
        }

        if (draggingAlpha) {
            float alpha = Math.max(0, Math.min(1, (float) (constrainedMouseX - (settingsX + SETTING_PADDING)) / (PANEL_WIDTH - SETTING_PADDING * 2)));
            draggedColorSetting.setColor(red, green, blue, (int) (alpha * 255));
        }
    }

    private float easeInOutCubic(float t) {
        return (float) (t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Panel panel : panels.values()) {
            Vector2f pos = panel.getPosition();
            if (panel.isOpen() && mouseX >= pos.getX() && mouseX <= pos.getX() + PANEL_WIDTH &&
                mouseY >= pos.getY() && mouseY <= pos.getY() + panel.getCurrentHeight()) {
                
                if (panel.isShowingSettings() && selectedModule != null && selectedPanel == panel) {
                    float settingsOffset = (1f - panel.getSettingsAnimationProgress()) * PANEL_WIDTH;
                    float settingsX = pos.getX() + settingsOffset;
                    
                    if (mouseX >= settingsX && mouseX <= settingsX + PANEL_WIDTH) {
                        float newSettingScrollOffset = panel.settingScrollOffset - (float)(verticalAmount * 10);
                        panel.settingScrollOffset = Math.max(0, Math.min(panel.maxSettingScrollOffset, newSettingScrollOffset));
                        return true;
                    }
                }
                
                if (panel.maxModuleScrollOffset > 0) {
                    float newModuleScrollOffset = panel.getModuleScrollOffset() - (float)(verticalAmount * 10);
                    panel.setModuleScrollOffset(Math.max(0, Math.min(panel.getMaxModuleScrollOffset(), newModuleScrollOffset)));
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}

