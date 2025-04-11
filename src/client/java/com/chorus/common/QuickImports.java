package com.chorus.common;

import chorus0.Chorus;
import com.chorus.api.module.ModuleManager;
import com.chorus.api.repository.bot.BotRepository;
import com.chorus.api.repository.friend.FriendRepository;
import com.chorus.api.repository.team.TeamRepository;
import com.chorus.api.system.notification.NotificationManager;
import com.chorus.api.system.rotation.RotationComponent;
import net.minecraft.client.MinecraftClient;

public interface QuickImports {
    MinecraftClient mc = MinecraftClient.getInstance();
    TeamRepository teamRepository = Chorus.getInstance().getTeamRepository();
    FriendRepository friendRepository = Chorus.getInstance().getFriendRepository();
    BotRepository npcRepository = Chorus.getInstance().getNpcRepository();
    RotationComponent rotationComponent = Chorus.getInstance().getRotationComponent();
    NotificationManager notificationManager = Chorus.getInstance().getNotificationManager();
    ModuleManager moduleManager = Chorus.getInstance().getModuleManager();
}
