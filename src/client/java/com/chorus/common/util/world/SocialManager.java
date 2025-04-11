/**
 * Created: 12/11/2024
 */

package com.chorus.common.util.world;


import chorus0.Chorus;
import com.chorus.common.QuickImports;
import com.chorus.impl.modules.other.Target;
import net.minecraft.entity.player.PlayerEntity;

public final class SocialManager implements QuickImports {

    public static boolean isEnemy(PlayerEntity player) {
        if (Chorus.getInstance().getModuleManager().getModule(Target.class).enemy == player) return true;
        if (teamRepository.isMemberOfCurrentTeam(player.getNameForScoreboard())) return false;
        if (friendRepository.isFriend(player.getNameForScoreboard())) return false;
        if (npcRepository.isNPC(player.getNameForScoreboard())) return false;
        return true;
    }

    public static PlayerEntity isTargetedPlayer(PlayerEntity player) {
        PlayerEntity enemy = Chorus.getInstance().getModuleManager().getModule(Target.class).enemy;
        if (enemy != null) {
            return enemy;
        }
        return player;
    }
    public static Boolean isTarget(PlayerEntity player) {
        PlayerEntity enemy = Chorus.getInstance().getModuleManager().getModule(Target.class).enemy;
        if (enemy == null) return false;
        return enemy == player;
    }
    public static PlayerEntity getTarget() {
        PlayerEntity enemy = Chorus.getInstance().getModuleManager().getModule(Target.class).enemy;
        return enemy;
    }

}