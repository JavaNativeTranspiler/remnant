package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SimulatedPlayer;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.network.PacketSendEvent;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@ModuleInfo(
    name = "Criticals",
    description = "Always Deal Critical Hits",
    category = ModuleCategory.COMBAT
)
public class Criticals extends BaseModule implements QuickImports {
    private final ModeSetting mode = new ModeSetting("Mode", "Choose Mode", "Legit", "Legit", "Packet", "Blatant");
    private PlayerInteractEntityC2SPacket packet = null;
    private PlayerEntity lastEnemy = null;
    @RegisterEvent
    private void tickEventListener(TickEvent event) {
        setSuffix(mode.getValue());
        if (mc.player == null || mc.world == null) return;

        if (event.getMode().equals(TickEvent.Mode.POST)) return;
        boolean hasAttackingEnemy = mc.player.getAttacking() != null && mc.player.getAttacking() instanceof PlayerEntity;
        if (hasAttackingEnemy) {
            lastEnemy = (PlayerEntity) mc.player.getAttacking();
        }

        if (lastEnemy != null) {
            if (!lastEnemy.isPartOfGame() || lastEnemy.isDead() || lastEnemy.isSpectator() || lastEnemy.distanceTo(mc.player) > 6.5f || mc.player.age - mc.player.getLastAttackTime() > 25) {
                lastEnemy = null;
            }
        }
        if (SocialManager.getTarget() != null) {
            lastEnemy = SocialManager.getTarget();
        }
        switch (mode.getValue()) {
            case "Legit" -> {
                if (lastEnemy != null) {
                    SimulatedPlayer simulator = new SimulatedPlayer(mc.player);
                    simulator.setInput(
                            mc.options.forwardKey.isPressed(),
                            mc.options.backKey.isPressed(),
                            mc.options.leftKey.isPressed(),
                            mc.options.rightKey.isPressed(),
                            true, false);
                    for (int i = 0; i <= 11 - (Math.round(mc.player.getAttackCooldownProgress((float) mc.player.getAttributeValue(EntityAttributes.ATTACK_SPEED)) * 10)); i++) {
                        simulator.tick();
                    }
                    if (simulator.getFallDistance() != 0 && simulator.getFallDistance() < 0.75 && mc.player.isOnGround()) {
                        InputUtils.simulateKeyPress(mc.options.jumpKey, 35);
                        //ChatUtils.sendFormattedMessage("Jumped " + simulator.getFallDistance());
                    }
                }
            }
        }
    }
    @RegisterEvent
    private void AttackEventListener(AttackEvent event) {
        if (event.getMode().equals(AttackEvent.Mode.POST)) return;
        if (mc.player == null || mc.world == null) return;
        Entity entity = event.getTarget();
        if (canDealCriticals()) return;
        if (entity == null) return;
        switch (mode.getValue()) {
            case "Packet", "Blatant" -> {
                if (entity instanceof LivingEntity) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625f, mc.player.getZ(), false, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, false));
                }
            }
        }
    }
    @RegisterEvent
    private void packetSendEventListener(PacketSendEvent event) {
        if (event.getMode() == PacketSendEvent.Mode.POST) return;
        if (mc.player == null || mc.world == null) return;
        if (canDealCriticals()) return;
        if (mode.getValue().equals("Blatant"))
            if (event.getPacket() instanceof ClientCommandC2SPacket packet) {
                if (packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                    event.setCancelled(true);
                }
            }
    }
    public boolean canDealCriticals() {
        if (mc.player == null || mc.player.isOnGround() || mc.player.isSubmergedInWater() || mc.player.isInLava() || mc.player.isClimbing()) return false;
        return true;
    }
    public double predictedMotion(final double motion, final int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;
        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98F;
        }
        return predicted;
    }
    public Criticals() {
        getSettingRepository().registerSettings(mode);
    }
}

