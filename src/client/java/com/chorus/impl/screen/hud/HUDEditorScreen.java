package com.chorus.impl.screen.hud;

import chorus0.Chorus;
import com.chorus.api.module.Module;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;

public class HUDEditorScreen extends Screen implements QuickImports {
    @Getter
    private static final HUDEditorScreen INSTANCE = new HUDEditorScreen();
    private final ArrayList<Module> hudModules = new ArrayList<>();
    private Module draggingModule = null;
    private int dragStartX = 0;
    private int dragStartY = 0;

    public HUDEditorScreen() {
        super(Text.literal("HUD Editor"));
        for (Module module : Chorus.getInstance().getModuleManager().getModules()) {
            if (module.isDraggable()) {
                hudModules.add(module);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (context == null) return;
        
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        context.drawVerticalLine(width / 2, 0, height, 0xFFFFFF);
        context.drawHorizontalLine(0, height / 2, width, 0xFFFFFF);
        if (draggingModule != null) {
            int verticalRatio = Math.max(1, height / 4);
            int horizontalRatio = Math.max(1, width / 4);

            for (int i = 0; i <= verticalRatio; i++) {
                float yPos = i * (height / (float) verticalRatio);
                Render2DEngine.drawRect(context.getMatrices(), 0, yPos, width, 0.5f, new Color(255, 255, 255, 50));
            }

            for (int i = 0; i <= horizontalRatio; i++) {
                float xPos = i * (width / (float) horizontalRatio);
                Render2DEngine.drawRect(context.getMatrices(), xPos, 0, 0.5f, height, new Color(255, 255, 255, 50));
            }
        }
        int gridX = Math.round(mouseX / 4f) * 4; // the 4 is the grid so if you want it to clamp to smaller, make it 1 or remove, vice versa.
        int gridY = Math.round(mouseY / 4f) * 4;


        for (Module module : hudModules) {
            if (module == null || !module.isEnabled()) continue;

            int xPos = (int) module.getSettingRepository().getSetting("xPos").getValue();
            int yPos = (int) module.getSettingRepository().getSetting("yPos").getValue();

            int moduleWidth = (int) module.getWidth();
            int moduleHeight = (int) module.getHeight();

            int middleX = xPos + (moduleWidth / 2);
            int middleY = yPos + (moduleHeight / 2);

            if (draggingModule == module) {
                int snap = 10; // if u want this shi to snap less aggressively make this like 5
                int middleXDistance = Math.abs(width / 2 - middleX);
                int middleYDistance = Math.abs(height / 2 - middleY);
                if (middleXDistance < 5 && Math.abs(middleX - mouseX) < snap) {
                    Render2DEngine.drawRect(context.getMatrices(), (float) width / 2, 0, 1, height, Color.white);
                    ((NumberSetting<Integer>) module.getSettingRepository().getSetting("xPos")).setValue(Math.clamp(middleX - (moduleWidth / 2), 0, width - moduleWidth));
                } else {
                    ((NumberSetting<Integer>) module.getSettingRepository().getSetting("xPos")).setValue(Math.clamp(gridX - dragStartX, 0, width - moduleWidth));
                }
                if (middleYDistance < 5 && Math.abs(middleY - mouseY) < snap) {
                    Render2DEngine.drawRect(context.getMatrices(), (float) 0, height / 2f, width, 1, Color.white);
                    ((NumberSetting<Integer>) module.getSettingRepository().getSetting("yPos")).setValue(Math.clamp(middleY - (moduleHeight / 2), 0, height - moduleHeight));
                } else {
                    ((NumberSetting<Integer>) module.getSettingRepository().getSetting("yPos")).setValue(Math.clamp(gridY - dragStartY, 0, height - moduleHeight));
                }
            }
            FontAtlas font = Chorus.getInstance().getFonts().getPoppins();
            font.render(context.getMatrices(), module.getName(), xPos + moduleWidth / 2f - font.getWidth(module.getName(), 8f) / 2, yPos - font.getLineHeight(8f), 8f, Color.white.getRGB());
            Render2DEngine.drawRoundedOutline(context.getMatrices(), xPos, yPos, moduleWidth, moduleHeight, 5, 1, new Color(255, 255, 255, 255));


        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingModule == null) {
            for (Module module : hudModules) {
                if (module == null || !module.isEnabled()) continue;

                int xPos = (int) module.getSettingRepository().getSetting("xPos").getValue();
                int yPos = (int) module.getSettingRepository().getSetting("yPos").getValue();

                int moduleWidth = (int) module.getWidth();
                int moduleHeight = (int) module.getHeight();

                int gridX = (int) (Math.round(mouseX / 4f) * 4); // the 4 is the grid so if you want it to clamp to smaller, make it 1 or remove, vice versa.
                int gridY = (int) (Math.round(mouseY / 4f) * 4);
                if (gridX >= xPos && gridX <= xPos + moduleWidth && gridY >= yPos && gridY <= yPos + moduleHeight) {
                    draggingModule = module;
                    dragStartX = gridX - xPos;
                    dragStartY = gridY - yPos;
                    break;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingModule != null) {
            draggingModule = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
} 