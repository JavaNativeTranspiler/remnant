
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.client;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.repository.team.TeamRepository;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(
        name = "Teams",
        description = "Prevents Targeting Teammates",
        category = ModuleCategory.CLIENT
)
public class Teams extends BaseModule implements QuickImports {
    public TeamRepository repo;
    private final Queue<PlayerEntity> playerList = new ConcurrentLinkedQueue<>();
    private final Queue<PlayerEntity> hasSameArmor = new ConcurrentLinkedQueue<>();

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.world == null | mc.player == null) return;

            if (mc.player.age < 5) {
                teamRepository.clear();
                return;
            }
            if (teamRepository.getCurrentTeam() == null) {
                teamRepository.setTeam("gangstas");
                return;
            }
            //ChatUtils.sendFormattedMessage("Wow");
            mc.world.getPlayers().forEach(player -> {
                if (!checkNameColor(player)) {
                    return;
                }
                if (!checkArmorColor(player)) {
                    //return;
                }
                if (!checkScoreboardTeam(player)) {
                    return;
                }
                teamRepository.addMemberToCurrentTeam(player.getNameForScoreboard());
            });
        }
    }

    private boolean checkNameColor(PlayerEntity player) {
        Text playerDisplayName = player.getDisplayName();
        Text mcPlayerDisplayName = mc.player.getDisplayName();

        var isTeam = playerDisplayName != null && mcPlayerDisplayName != null &&
                playerDisplayName.getStyle() != null && mcPlayerDisplayName.getStyle() != null &&
                Objects.equals(playerDisplayName.getStyle().getColor(), mcPlayerDisplayName.getStyle().getColor());

        if (!isTeam) {
            teamRepository.removeMemberFromCurrentTeam(player.getNameForScoreboard());
            return false;
        }
        return true;
    }

    private boolean checkArmorColor(PlayerEntity player) {
        int armorAmount = 0;
        for (int i = 3; i >= 0; i--) {
            var armor = player.getInventory().getArmorStack(i);
            var playerArmor = mc.player.getInventory().getArmorStack(i);
            if (armor.getItem() instanceof DyeItem dyeableArmorItem &&
                    playerArmor.getItem() instanceof DyeItem dyeableArmorItem2) {

                if (dyeableArmorItem.getColor() == dyeableArmorItem2.getColor()) {
                    armorAmount++;
                }
            }
        }
        if (armorAmount > 0) {
            return true;
        } else {
            teamRepository.removeMemberFromCurrentTeam(player.getNameForScoreboard());
        }
        return false;
    }

    private boolean checkScoreboardTeam(PlayerEntity player) {
        return player.getScoreboardTeam() != null &&
                mc.player.getScoreboardTeam() != null &&
                Objects.equals(player.getScoreboardTeam(), mc.player.getScoreboardTeam());
    }

    public Teams() {
    }
}
