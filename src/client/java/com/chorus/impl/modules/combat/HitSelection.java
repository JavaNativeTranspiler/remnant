package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.network.PacketReceiveEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@ModuleInfo(name = "HitSelection", description = "Selects Hits", category = ModuleCategory.COMBAT)
public class HitSelection extends BaseModule implements QuickImports {


    private final SettingCategory general = new SettingCategory("Distances");
    private final SettingCategory conditions = new SettingCategory("Conditions");

    private final BooleanSetting criticalSpam = new BooleanSetting(conditions, "Stop On Jump", "Prevents While Jumping to allow Crit-Spamming", true);
    private final RangeSetting<Double> selectRange = new RangeSetting<>(general, "Select Distance", "Range To Start Selecting", 0.0, 6.0, 2.0, 3.5);
    private final NumberSetting<Double> hitRange = new NumberSetting<>(general, "Hit Distance", "Distance To Move To", 2.85, 0.0, 6.0);
    public HitSelection() {
        getSettingRepository().registerSettings(general, conditions, selectRange, hitRange, criticalSpam);
    }
    boolean spaced = false;
    PlayerEntity player = null;
    @RegisterEvent
    private void PacketReceiveEventListener(PacketReceiveEvent event) {
        if (event.getMode().equals(PacketReceiveEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
                player = null;
            }
        }
    }
    @RegisterEvent
    private void tickEventEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;

            if (mc.player.getAttacking() instanceof PlayerEntity && mc.player.getAttacking() != null) {
                if (mc.player.handSwingTicks == 1) {
                    if (mc.player.age - mc.player.getLastAttackTime() > 5) return;
                    if (mc.player.getVelocity().y > 0) return;
                    player = (PlayerEntity) mc.player.getAttacking();
                    spaced = true;
                    if (player == null) return;
                    if (!SocialManager.isEnemy(player)) return;
                    if (player.isDead() || player.isInCreativeMode() || player.isSpectator() || player.isInvulnerable())  {
                        player = null;
                        return;
                    }
                    if (player.distanceTo(mc.player) >= selectRange.getValueMin() && player.distanceTo(mc.player) <= selectRange.getValueMax()) {
                        if (!criticalSpam.getValue() || !mc.options.jumpKey.isPressed()) {
                            spaced = true;
                            mc.options.forwardKey.setPressed(false);
                            mc.options.backKey.setPressed(true);
                        }
                    }
                }
            }
            if (spaced) {
                if (player != null) {
                    if (player.distanceTo(mc.player) > 12) {
                        player = null;
                        return;
                    }
                    if (player.distanceTo(mc.player) >= hitRange.getValue()) {
                        spaced = false;
                        mc.options.forwardKey.setPressed(InputUtils.keyDown(mc.options.forwardKey.getDefaultKey().getCode()));
                        mc.options.backKey.setPressed(false);
                    }
                } else {
                    spaced = false;
                    mc.options.forwardKey.setPressed(InputUtils.keyDown(mc.options.forwardKey.getDefaultKey().getCode()));
                    mc.options.backKey.setPressed(!mc.options.backKey.isPressed());
                }
            }

        }
    }

}