
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.repository.bot.BotRepository;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.player.TickEvent;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(
        name = "AntiBot",
        description = "Prevents Targeting Bots",
        category = ModuleCategory.OTHER
)

public class AntiBot extends BaseModule implements QuickImports {
    private final SettingCategory checks = new SettingCategory("Checks");
    private final BooleanSetting moved = new BooleanSetting(checks, "Moved", "Checks if a player has ever moved", false);
    private final BooleanSetting touchedGround = new BooleanSetting(checks, "Ground", "Checks if a player has ever been on the ground.", false);
    private final BooleanSetting touchedAir = new BooleanSetting(checks, "Air", "Checks if a player has ever been in the air", false);
    private final BooleanSetting swung = new BooleanSetting(checks, "Swung", "Checks if a player has swung their hand", false);
    private final BooleanSetting damaged = new BooleanSetting(checks, "Damaged", "Checks if a player has ever been damaged.", false);
    private final BooleanSetting rotated = new BooleanSetting(checks, "Rotated", "Checks if a player has ever rotated before.", false);

    private final List<BotRepository.Flag> flags = new ArrayList<>();


    public void onModuleDisabled() {
        npcRepository.clear(true, false);
    }

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.world == null | mc.player == null) {
                npcRepository.clear(true, true);
                return;
            }
            if (mc.player.age <= 5) npcRepository.clear(true, true);
            mc.world.getPlayers().forEach(player -> {
                if (player == null) return;
                if (!npcRepository.isRegistered(player.getNameForScoreboard())) {
                    npcRepository.registerPlayer(player.getNameForScoreboard(), player.getUuid());
                    return;
                }


                if (player.hurtTime != 0) {
                    npcRepository.addFlags(player.getNameForScoreboard(), BotRepository.Flag.DAMAGED);
                }
                if (player.isOnGround()) {
                    npcRepository.addFlags(player.getNameForScoreboard(), BotRepository.Flag.TOUCHED_GROUND);
                } else {
                    npcRepository.addFlags(player.getNameForScoreboard(), BotRepository.Flag.TOUCHED_AIR);
                }
                if (player.handSwingTicks != 0) {
                    npcRepository.addFlags(player.getNameForScoreboard(), BotRepository.Flag.SWUNG);
                }
                if (player.getX() != player.prevX || player.getY() != player.prevY || player.getZ() != player.prevZ) {
                    npcRepository.addFlags(player.getNameForScoreboard(), BotRepository.Flag.MOVED);
                }
                if (player.getYaw() != player.prevYaw || player.getPitch() != player.prevPitch) {
                    npcRepository.addFlags(player.getNameForScoreboard(), BotRepository.Flag.ROTATED);
                }
            });

            addFlag(moved.getValue(), BotRepository.Flag.MOVED);
            addFlag(touchedGround.getValue(), BotRepository.Flag.TOUCHED_GROUND);
            addFlag(touchedAir.getValue(), BotRepository.Flag.TOUCHED_AIR);
            addFlag(swung.getValue(), BotRepository.Flag.SWUNG);
            addFlag(damaged.getValue(), BotRepository.Flag.DAMAGED);
            addFlag(rotated.getValue(), BotRepository.Flag.ROTATED);

            if (!flags.isEmpty()) {
                npcRepository.meetsCriteria(flags);
            } else {
                npcRepository.clear(true, false);
            }
        }
    }

    private void addFlag(boolean condition, BotRepository.Flag flag) {
        if (condition) {
            if (!flags.contains(flag)) {
                flags.add(flag);
            }
        } else {
            flags.remove(flag);
        }
    }

    public AntiBot() {
        getSettingRepository().registerSettings(checks, moved, touchedGround, touchedAir, swung, damaged, rotated);
    }
}
