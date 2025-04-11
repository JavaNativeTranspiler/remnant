package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.Module;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.render.ColorUtils;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render2DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.*;
import java.util.List;

@ModuleInfo(name = "Arraylist", description = "Displays client information on your HUD", category = ModuleCategory.VISUAL)
public class Arraylist extends BaseModule implements QuickImports {


    private final SettingCategory text = new SettingCategory("Text Settings");
    private final SettingCategory visual = new SettingCategory("Visual Settings");
    private final SettingCategory position = new SettingCategory("Position Settings");
    private final SettingCategory colors = new SettingCategory("Color Settings");

    private final NumberSetting<Integer> xPos = new NumberSetting<>(position, "xPos", "Internal setting", 5, 0, 1920);
    private final NumberSetting<Integer> yPos = new NumberSetting<>(position, "yPos", "Internal setting", 5, 0, 1080);

    private final ModeSetting type = new ModeSetting(text, "Type", "Choose Arraylist Type", "Outlined", "Outlined", "Rounded", "Basic");
    private final ModeSetting font = new ModeSetting(text, "Font", "Choose Font to use", "Poppins", "Inter Semi-Bold", "Inter Bold", "Inter Medium", "Proggy Clean", "Poppins");
    private final ModeSetting capitalization = new ModeSetting(text, "Capitalization", "Choose Module Capitalization", "Default", "Lowercase", "Default", "Uppercase");
    private final ModeSetting brackets = new ModeSetting(text, "Suffix Brackets", "Choose Suffix Brackets", "()", "()", "[]", "<>", "><");
    private final MultiSetting hideCategories = new MultiSetting(text, "Hide Categories", "Hide Specific Module Categories", "Combat", "Movement", "Visual", "Utility", "Other");
    private final NumberSetting<Integer> textSize = new NumberSetting<>(text, "Text Size", "Adjust Text Size", 10, 5, 20);

    private final MultiSetting outlineSettings = new MultiSetting(visual, "Outline", "Choose Which Side Outline To Render", "Left", "Right", "Top", "Bottom");
    private final NumberSetting<Integer> outlineOverall = new NumberSetting<>(visual, "Outline Thickness", "The Thickness of the outlines", 0, 0, 10);
    private final NumberSetting<Integer> outlineSides = new NumberSetting<>(visual, "Side Outline Height", "The Height of the sides of the outlines (%)", 100, 0, 100);
    private final NumberSetting<Integer> hPadding = new NumberSetting<>(visual, "Horizontal Padding", "", 4, 0, 10);
    private final NumberSetting<Integer> vPadding = new NumberSetting<>(position, "Vertical Padding", "", 2, 0, 25);
    private final ColorSetting primaryColor = new ColorSetting(colors, "Primary Color", "The main color for module names", new Color(184, 112, 242));
    private final ColorSetting secondaryColor = new ColorSetting(colors, "Secondary Color", "The secondary color for module names", new Color(184, 112, 242));
    private final ColorSetting suffixColor = new ColorSetting(colors, "Suffix Color", "The color for module suffixes", new Color(235, 235, 235));



    String leftBracket = "";
    String rightBracket = "";
    String spacing = "";
    public int alignment = 0;
    private final Map<String, float[]> modulePositions = new HashMap<>();
    @RegisterEvent
    private void render2DListener(Render2DEvent event) {
        if (mc.player == null || mc.world == null || mc.getDebugHud().shouldShowDebugHud()) {
            modulePositions.clear();
            return;
        }

        MatrixStack matrices = event.getContext().getMatrices();
        FontAtlas font = getFont();
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        List<Module> filteredModules = new ArrayList<>(
                Chorus.getInstance().getModuleManager().getModules().stream()
                        .filter(module -> hideCategories.getValue().stream()
                                .noneMatch(condition -> switch (condition) {
                                    case "Combat" -> module.getCategory() == ModuleCategory.COMBAT;
                                    case "Movement" -> module.getCategory() == ModuleCategory.MOVEMENT;
                                    case "Visual" -> module.getCategory() == ModuleCategory.VISUAL;
                                    case "Utility" -> module.getCategory() == ModuleCategory.UTILITY;
                                    case "Other" -> module.getCategory() == ModuleCategory.OTHER;
                                    default -> throw new IllegalStateException("Unexpected value: " + condition);
                                })
                        )
                        .sorted(Comparator.comparingDouble(m ->
                            -font.getWidth(getName(m) + " " + getSuffix(m), textSize.getValue())
                        ))
                        .toList());

        List<Module> enabledModules = new ArrayList<>(
                Chorus.getInstance().getModuleManager().getModules().stream()
                        .filter(module -> hideCategories.getValue().stream()
                                .noneMatch(condition -> switch (condition) {
                                    case "Combat" -> module.getCategory() == ModuleCategory.COMBAT;
                                    case "Movement" -> module.getCategory() == ModuleCategory.MOVEMENT;
                                    case "Visual" -> module.getCategory() == ModuleCategory.VISUAL;
                                    case "Utility" -> module.getCategory() == ModuleCategory.UTILITY;
                                    case "Other" -> module.getCategory() == ModuleCategory.OTHER;
                                    default -> throw new IllegalStateException("Unexpected value: " + condition);
                                })
                        )
                        .filter(Module::isEnabled)
                        .sorted(Comparator.comparingDouble(m ->
                                -font.getWidth(getName(m) + " " + getSuffix(m), textSize.getValue())
                        ))
                        .toList());
        float yPosition = 0f;
        double scale = mc.getWindow().getScaleFactor();
        RenderSystem.enableScissor((int) (((xPos.getValue() + getWidth() - (hPadding.getValue() * 2) - 2 - font.getWidth(getFullName(enabledModules.getFirst()), textSize.getValue()))) * scale),
                0,
                (int) ((font.getWidth(getFullName(enabledModules.getFirst()), textSize.getValue()) + (hPadding.getValue() * 2) + 4) * scale),
                screenHeight * 2);
        for (int i = 0; i < filteredModules.size(); i++) {
            Module module = filteredModules.get(i);
            String moduleKey = module.toString();

            modulePositions.putIfAbsent(moduleKey, new float[]{-screenWidth, yPosition - 25});

            int size = textSize.getValue();
            int padding = hPadding.getValue();
            float lineHeight = font.getLineHeight(size) + vPadding.getValue();
            float thickness = outlineOverall.getValue();
            float sideHeight = outlineSides.getValue() * 0.01f;
            float moduleX = modulePositions.get(module.toString())[0];
            float moduleY = modulePositions.get(module.toString())[1];

            if (module.isEnabled() && enabledModules.getFirst() == module) {
                setWidth(font.getWidth(getFullName(enabledModules.getFirst()), size) + (padding * 2) + (thickness * 2) + 1 + 2);
                setHeight(enabledModules.size() * lineHeight);
            }
            float lerp = (mc.currentScreen == null ? 3f / mc.getCurrentFps() : 1f);
            moduleX = MathHelper.lerp(lerp, moduleX,
                    (module.isEnabled() ? 0 : -200) + screenWidth - getWidth() - xPos.getValue());
            moduleY = MathHelper.lerp(lerp, moduleY,
                    yPosition + (MathHelper.clamp(yPos.getValue(), mc.player.getActiveStatusEffects().isEmpty() ? 0 : 25,1000)));

            modulePositions.put(module.toString(), new float[]{moduleX, moduleY});


            float x = screenWidth - moduleX - padding - 1;
            float y = moduleY;

            Color firstColor = ColorUtils.interpolateColor(primaryColor.getValue(), secondaryColor.getValue(), 3, (int) -(yPosition * 2));
            Color secondColor = ColorUtils.interpolateColor(primaryColor.getValue(), secondaryColor.getValue(), 3, (int) -((yPosition + lineHeight) * 2));

            Color bgFirst = new Color(
                firstColor.getRed(),
                firstColor.getGreen(),
                    firstColor.getBlue(),
                    25
            );
            Color bgNext = new Color(
                    secondColor.getRed(),
                    secondColor.getGreen(),
                    secondColor.getBlue(),
                    25
            );


            Render2DEngine.drawVerticalGradient(matrices,
                    x - font.getWidth(getFullName(module), size) - padding,
                    y,
                    font.getWidth(getFullName(module), size) + (padding * 2),
                    lineHeight,
                    bgFirst,
                    bgNext);

            Render2DEngine.drawBlurredRoundedRect(matrices,
                    x - font.getWidth(getFullName(module), size) - padding,
                    y,
                    font.getWidth(getFullName(module), size) + (padding * 2),
                    lineHeight,
                    1,
                    8,
                    new Color(0, 0, 0, 10)
            );

            if (type.getValue().equals("Outlined")) {
                if (outlineSettings.getSpecificValue("Bottom")) {
                    Module next = enabledModules.indexOf(module) < enabledModules.size() - 1 ? enabledModules.get(enabledModules.indexOf(module) + 1) : enabledModules.get(enabledModules.indexOf(module));
                    if (module.isEnabled() && next != null) {
                        float nextWidth = (font.getWidth(getFullName(next), size));
                        Render2DEngine.drawRect(matrices,
                                x - font.getWidth(getFullName(module), size) - padding - thickness,
                                y + lineHeight,
                                font.getWidth(getFullName(module), size) - (module == enabledModules.getLast() ? -(padding * 2 + thickness * 2) - 1 : nextWidth),
                                1 + thickness,
                                secondColor
                        );
                    }
                }
                if (module == enabledModules.getFirst() && outlineSettings.getSpecificValue("Top"))
                    Render2DEngine.drawRect(matrices,
                            x - font.getWidth(getFullName(module), size) - padding - thickness,
                            y - thickness,
                            font.getWidth(getFullName(module), size) + (padding * 2) + (thickness * 2) + 1,
                            1 + thickness,
                            firstColor);

                if (outlineSettings.getSpecificValue("Right"))
                    Render2DEngine.drawVerticalGradient(matrices,
                            x + padding,
                            y + (lineHeight / 2) - (sideHeight * lineHeight / 2f),
                            1 + thickness,
                            (sideHeight * lineHeight) + (filteredModules.getLast() == module ? 1 : 0),
                            firstColor, secondColor);

                if (outlineSettings.getSpecificValue("Left"))
                    Render2DEngine.drawVerticalGradient(matrices,
                            x - font.getWidth(getFullName(module), size) - padding - (thickness),
                            y + (lineHeight / 2) - (sideHeight * lineHeight / 2f),
                            1 + thickness,
                            (sideHeight * lineHeight), firstColor, secondColor);
            }
            if (filteredModules.getLast() == module) {
                alignment = (int) (y + lineHeight);
            }


            font.renderWithShadow(
                    matrices,
                    getName(module),
                    x - font.getWidth(getFullName(module), size),
                    y + (lineHeight / 2f) - (font.getLineHeight(size) / 2) + 0.5f,
                    size,
                    firstColor.getRGB());

            if (!getSuffix(module).isEmpty())
                font.renderWithShadow(
                        matrices,
                        getSuffix(module),
                        x - font.getWidth(getSuffix(module), size) - 1,
                        y + (lineHeight / 2f) - (font.getLineHeight(size) / 2) + 1,
                        size,
                        suffixColor.getValue().getRGB());
            if (module.isEnabled())
                yPosition += lineHeight;
        }
        RenderSystem.disableScissor();
    }
    public String getSuffix(Module module) {
        switch (brackets.getValue()) {
            case "()" -> { leftBracket = "("; rightBracket = ")"; }
            case "[]" -> { leftBracket = "["; rightBracket = "]"; }
            case "<>" -> { leftBracket = "<"; rightBracket = ">"; }
            case "><" -> { leftBracket = ">"; rightBracket = "<"; }
        }
        String suffix = module.getSuffix();
        switch (capitalization.getValue()) {
            case "Lowercase" -> suffix = suffix.toLowerCase();
            case "Uppercase" -> suffix = suffix.toUpperCase();
        }
        spacing = " ";
        return suffix.isEmpty() ? "" : spacing + leftBracket + suffix + rightBracket;
    }

    public String getName(Module module) {
        String moduleName = module.getName();
        switch (capitalization.getValue()) {
            case "Lowercase" -> moduleName = moduleName.toLowerCase();
            case "Uppercase" -> moduleName = moduleName.toUpperCase();
        }
        return moduleName;
    }

    public String getFullName(Module module) {
        return getName(module) + getSuffix(module);
    }
    public FontAtlas getFont() {
        return switch (font.getValue()) {
            case "Inter Semi-Bold" -> Chorus.getInstance().getFonts().getInterSemiBold();
            case "Inter Bold" -> Chorus.getInstance().getFonts().getInterBold();
            case "Inter Medium" -> Chorus.getInstance().getFonts().getInterMedium();
            case "Proggy Clean" -> Chorus.getInstance().getFonts().getProggyClean();
            case "Poppins" -> Chorus.getInstance().getFonts().getPoppins();
            default -> throw new IllegalStateException("Unexpected value: " + font.getValue());
        };

    }
    public Arraylist() {

        xPos.setRenderCondition(() -> false);
        yPos.setRenderCondition(() -> false);
        outlineSides.setRenderCondition(() -> type.getValue().equals("Outlined"));
        outlineOverall.setRenderCondition(() -> type.getValue().equals("Outlined"));
        outlineSettings.setRenderCondition(() -> type.getValue().equals("Outlined"));
        setDraggable(true);
        getSettingRepository().registerSettings(

                font,
                text,
                position,
                visual,
                type,
                xPos,
                yPos,
                colors,
                capitalization,
                brackets,
                hideCategories,
                outlineSettings,
                textSize,
                outlineOverall,
                outlineSides,
                hPadding,
                vPadding,
                primaryColor,
                secondaryColor,
                suffixColor);
    }

}