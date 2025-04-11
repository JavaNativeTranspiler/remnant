
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render2DEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@ModuleInfo(
    name        = "Scoreboard",
    description = "Change the look of the scoreboard",
    category    = ModuleCategory.VISUAL
)

public class Scoreboard extends BaseModule implements QuickImports {
    public final BooleanSetting hideScoreboard = new BooleanSetting("Hide Scoreboard", "Hides the scoreboard", false);
    public final BooleanSetting alignToArraylist = new BooleanSetting("Align With Arraylist", "Hides the scoreboard", false);
    public final BooleanSetting hideScores = new BooleanSetting("Hide Scores", "Hides red numbers on the side", false);
    public final BooleanSetting blur = new BooleanSetting("blur", "Makes Scoreboard Blurred", false);
    private final NumberSetting<Integer> xPos = new NumberSetting<>("xPos", "Internal setting", 50, 0, 1920);
    private final NumberSetting<Integer> yPos = new NumberSetting<>("yPos", "Internal setting", 75, 0, 1080);

    @Override
    protected void onModuleEnabled() {
        if (xPos.getValue() == 50 || yPos.getValue() == 75) {
            xPos.setValue(mc.getWindow().getScaledWidth() - 1);
            yPos.setValue(mc.getWindow().getScaledHeight() / 2);
        }
    }

    @RegisterEvent
    private void render2DListener(Render2DEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null || mc.inGameHud == null || mc.player.getScoreboard() == null || mc.player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) == null) return;
        if (hideScoreboard.getValue()) return;
        renderScoreboard(event.getContext());
    }


    public void renderScoreboard(DrawContext drawContext) {
        ScoreboardObjective objective = Objects.requireNonNull(mc.player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR));
        net.minecraft.scoreboard.Scoreboard scoreboard = objective.getScoreboard();
        NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);

        List<SidebarEntry> sidebarEntries = scoreboard.getScoreboardEntries(objective)
                .stream()
                .filter((score) -> !score.hidden()).sorted(Comparator.comparing(ScoreboardEntry::value)
                .reversed()
                .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER))
                .limit(15L)
                .map(scoreboardEntry -> {
            Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
            Text name = Team.decorateName(team, scoreboardEntry.name());
            Text scoreText = scoreboardEntry.formatted(numberFormat);
            int scoreWidth = mc.textRenderer.getWidth(scoreText);
            return new SidebarEntry(name, scoreText, scoreWidth);
        }).toList();
        Text title = objective.getDisplayName();
        int titleWidth = mc.textRenderer.getWidth(title);
        int maxWidth = titleWidth;
        int separatorWidth = mc.textRenderer.getWidth(": ");

        for (SidebarEntry entry : sidebarEntries) {
            maxWidth = Math.max(maxWidth, mc.textRenderer.getWidth(entry.name) + (entry.scoreWidth > 0 ? separatorWidth + entry.scoreWidth : 0));
        }

        int entryHeight = sidebarEntries.size() * 9;
        Arraylist arraylist = Chorus.getInstance().getModuleManager().getModule(Arraylist.class);
        int startY = (arraylist.alignment != 0 && alignToArraylist.getValue() ? arraylist.alignment : yPos.getValue());
        int startX = xPos.getValue() - 3;
        int endX = xPos.getValue() + maxWidth - 1;
        int background1 = mc.options.getTextBackgroundColor(0.15f);
        int background2 = mc.options.getTextBackgroundColor(0.05f);

        if (blur.getValue())
            Render2DEngine.drawRoundedBlur(drawContext.getMatrices(), startX, startY, endX - startX + 4, entryHeight + 9 + 1, 1, 8);
        drawContext.fill(startX, startY, endX + 4, startY - entryHeight + 9 + entryHeight, background2);
        drawContext.fill(startX, startY, endX + 4, startY + entryHeight + 9 + 1, background1);
        drawContext.drawText(mc.textRenderer, title, startX + maxWidth / 2 - titleWidth / 2 + 2, startY + 1, -1, false);
        setHeight(entryHeight + 1);
        setWidth(maxWidth + 4);
        for (int i = 0; i < sidebarEntries.size(); i++) {
            SidebarEntry entry = sidebarEntries.get(i);
            int y = startY + 9 + (i * 9);
            drawContext.drawText(mc.textRenderer, entry.name, startX + 2, y , -1, false);
            if (!hideScores.getValue())
                drawContext.drawText(mc.textRenderer, entry.score, endX - entry.scoreWidth + 2, y, -1, false);
        }
    }
    private record SidebarEntry(Text name, Text score, int scoreWidth) {}
    public Scoreboard() {
        setDraggable(true);
        getSettingRepository().registerSettings(hideScoreboard, alignToArraylist, hideScores, blur, xPos, yPos);
        xPos.setRenderCondition(() -> false);
        yPos.setRenderCondition(() -> false);
    }
}
