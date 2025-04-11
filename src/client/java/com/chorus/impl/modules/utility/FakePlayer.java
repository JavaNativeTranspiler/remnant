package com.chorus.impl.modules.utility;

import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.UUID;

@ModuleInfo(name = "FakePlayer", description = "Spawn Fake Player", category = ModuleCategory.UTILITY)
public class FakePlayer extends BaseModule implements QuickImports {
    public static OtherClientPlayerEntity fakePlayer;

    @Override
    protected void onModuleEnabled() {
        fakePlayer = new OtherClientPlayerEntity(mc.world,
                new GameProfile(UUID.fromString("43b3ea67-5318-40d2-bbd5-dfbb80e1cb6a"), "ionreal"));
        fakePlayer.copyPositionAndRotation(mc.player);

        mc.world.addEntity(fakePlayer);
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (25555 * 20), 255));
    }

    @Override
    protected void onModuleDisabled() {
        if (fakePlayer == null) return;
        fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
        fakePlayer.onRemoved();
        fakePlayer = null;
    }
}