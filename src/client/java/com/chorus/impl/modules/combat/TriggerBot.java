package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.PlayerUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.network.PacketSendEvent;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.events.player.TickEvent;
import com.chorus.impl.modules.other.MultiTask;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "TriggerBot", description = "Attacks Enemies", category = ModuleCategory.COMBAT)
public class TriggerBot extends BaseModule implements QuickImports {

    private final SettingCategory attack = new SettingCategory("Attack Settings");
    private final SettingCategory critical = new SettingCategory("Critical Hit Settings");
    private final SettingCategory targeting = new SettingCategory("Targeting Settings");
    private final SettingCategory additional = new SettingCategory("Additional Settings");

    private final ModeSetting attackMode = new ModeSetting(attack, "Attack Timing", "Select the attack timing", "Cooldown", "Cooldown", "Delay");
    private final RangeSetting<Integer> reactionTime = new RangeSetting<>(attack, "Reaction Time", "Adjust Reaction Time", 0, 250, 0, 0);
    private final RangeSetting<Integer> attackDelay = new RangeSetting<>(attack, "Attack Delay", "Adjust Attack Delay", 0, 1000, 10, 25);
    private final NumberSetting<Integer> cooldown = new NumberSetting<>(attack, "Cooldown", "Attack Cooldown (%)", 95, 0, 100);
    private final ModeSetting handleShield = new ModeSetting(attack, "Handle Shield", "Handles Player Shield", "Multi-Task", "Unblock Shield");

    private final ModeSetting criticalType = new ModeSetting(critical, "Criticals", "None", "Desync", "Desync", "Smart");
    private final NumberSetting<Double> maceFallDistance = new NumberSetting<>(critical, "Mace Fall Distance", "Set Minimum Fall Distance before attacking with a mace", 3.0, 0.0, 25.0);

    private final MultiSetting enemyConditions = new MultiSetting(targeting, "Enemy Conditions", "Conditions", "Attackable", "Ignore Shield");
    private final MultiSetting conditions = new MultiSetting(targeting, "Conditions", "Set activation conditions", "Clicking", "Holding Weapon");
    private final ModeSetting targetSorting = new ModeSetting(targeting, "Target Sorting", "Choose targets", "Player", "Visible", "Player");

    private final NumberSetting<Integer> fakeChance = new NumberSetting<>(additional, "Fail Swing Chance", "Chance to fail when optimal", 0, 0, 100);
    private final RangeSetting<Double> fakeRange = new RangeSetting<>(additional, "Fail Range", "Adjust Range To Fail Swings", 0.0, 10.0, 4.0, 10.0);
    private final MultiSetting fakeItems = new MultiSetting(additional, "Fail With", "not implemented", "Sword", "Axe", "Fist", "Mace");

    private final TimerUtils hitTimer = new TimerUtils();
    private final TimerUtils reactionTimer = new TimerUtils();

    private boolean unblocked;
    private boolean attacked;
    private boolean attackedThisTick;
    private boolean sprintDesynced;
    private boolean isSprinting;

    @RegisterEvent
    private void PacketSendEventListener(PacketSendEvent event) {
        if (event.getMode().equals(PacketSendEvent.Mode.PRE)) {
            if (event.getPacket() instanceof ClientCommandC2SPacket clientCommandC2SPacket && clientCommandC2SPacket.getEntityId() == mc.player.getId()) {
                isSprinting = clientCommandC2SPacket.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING;
                sprintDesynced = false;
            }
        }
    }

    @RegisterEvent
    private void attackEventListener(AttackEvent event) {
        if (event.getMode().equals(AttackEvent.Mode.POST)) {
            sprintDesynced = true;
        }
    }

    @RegisterEvent
    private void Render3DEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) return;
            if (attacked) {
                if (unblocked && InputUtils.mouseDown(1)) {
                    if (mc.player.getOffHandStack().getItem() == Items.SHIELD) {
                        mc.options.useKey.setPressed(true);
                        unblocked = false;
                    }
                }
                attacked = false;
            }
            setSuffix(criticalType.getValue());
            if (!mc.options.forwardKey.isPressed()) {
                sprintDesynced = false;
            }
            if (conditions.getSpecificValue("Holding Weapon")
                    && !(mc.player.getMainHandStack().getItem() instanceof SwordItem
                    || mc.player.getMainHandStack().getItem() instanceof AxeItem
                    || mc.player.getMainHandStack().getItem() instanceof TridentItem
                    || mc.player.getMainHandStack().getItem() instanceof MaceItem)) {
                return;
            }
            if (conditions.getSpecificValue("Clicking") && !InputUtils.mouseDown(0)) return;

            if (mc.player.getMainHandStack().getItem() instanceof MaceItem) {
                if (mc.player.fallDistance < maceFallDistance.getValue()) return;
            }
            if (mc.player.getAttackCooldownProgress((float) mc.player.getAttributeValue(EntityAttributes.ATTACK_SPEED)) < (cooldown.getValue() * 0.01)) {
                if (attackMode.getValue().equals("Cooldown")) {
                    hitTimer.reset();
                    return;
                }
            } else {
                Entity enemy = PlayerUtils.raytraceEntity(10f);
                if (enemy != null) {
                    if (enemy.distanceTo(mc.player) > fakeRange.getValueMin() && enemy.distanceTo(mc.player) < fakeRange.getValueMax()) {
                        if (MathUtils.randomInt(1, 100) <= fakeChance.getValue() && mc.player.age % 20 == 0) {
                            InputUtils.simulateClick(GLFW.GLFW_MOUSE_BUTTON_LEFT, 50);
                        }
                    }
                }
            }

            if (!hitTimer.hasReached(MathUtils.randomInt(attackDelay.getValueMin(), attackDelay.getValueMax()))) return;
            HitResult crosshairTarget = mc.crosshairTarget;

            if (crosshairTarget == null) return;
            if (!(crosshairTarget instanceof EntityHitResult entityHitResult)) {
                reactionTimer.reset();
                return;
            }
            if (entityHitResult.getEntity() instanceof LivingEntity entity) {
                if (!reactionTimer.hasReached(MathUtils.randomInt(reactionTime.getValueMin(), reactionTime.getValueMax()))) return;
                switch (targetSorting.getValue()) {
                    case "Player" -> {
                        if (!entity.isPlayer()) return;

                    }
                    case "Visible" -> {
                        if (!entity.canSee(entity)) return;
                    }
                }
                if (entity.isPlayer()) {
                    if (SocialManager.isTargetedPlayer((PlayerEntity) entity) != entity) {
                        return;
                    }
                    if (!SocialManager.isEnemy((PlayerEntity) entity)) return;
                }

                if (enemyConditions.getSpecificValue("Attackable"))
                    if (entity.hurtTime != 0) return;

                if (enemyConditions.getSpecificValue("Ignore Shield"))
                    if (entity.isPlayer() && entity.isUsingItem() && entity.getActiveItem().getItem() == Items.SHIELD && !PlayerUtils.isShieldFacingAway(entity)) return;

                boolean inCobweb = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB ||
                        mc.world.getBlockState(mc.player.getBlockPos().up()).getBlock() == Blocks.COBWEB ||
                        mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() == Blocks.COBWEB;

                switch (criticalType.getValue()) {
                    case "Smart" -> {
                        if (sprintDesynced || mc.player.isGliding() || inCobweb) {
                            // Continue with attack
                        } else {
                            if (!mc.options.forwardKey.isPressed() && mc.player.hurtTime != 0) {
                                if (mc.player.fallDistance == 0) return;
                            } else {
                                if (mc.player.fallDistance == 0 && !mc.player.isOnGround()) return;
                            }
                        }
                    }
                    case "Desync" -> {
                        if (sprintDesynced) {
                            // Continue with attack
                        } else {
                            if (!PlayerUtils.canCriticalHit() && !mc.player.isOnGround()) return;
                        }
                    }
                    default -> {
                        return;
                    }
                }
                if (mc.player.getActiveHand() == Hand.OFF_HAND && !mc.player.getActiveItem().getUseAction().equals(UseAction.NONE)) {
                    if (handleShield.getValue().equals("Unblock Shield") && mc.player.getActiveItem().getItem() == Items.SHIELD) {
                        mc.options.useKey.setPressed(false);
                        unblocked = true;
                        return;
                    }
                    if (handleShield.getValue().equals("Multi-Task") && Chorus.getInstance().getModuleManager().isModuleEnabled(MultiTask.class)) {
                        if (mc.player == null || mc.world == null || attackedThisTick) return;
                        attacked = true;
                        attackedThisTick = true;
                        PlayerUtils.attackEnemy(false, entity);
                        hitTimer.reset();
                        // ChatUtils.sendFormattedMessage("Attacked At " + mc.player.age);
                    }
                } else {
                    if (mc.player == null || mc.world == null || attackedThisTick) return;
                    attacked = true;
                    attackedThisTick = true;
                    PlayerUtils.attackEnemy(false, entity);
                    hitTimer.reset();
                    // ChatUtils.sendFormattedMessage("Attacked At " + mc.player.age);
                }
            }
        } else {
            attackedThisTick = false;
        }
    }

    public TriggerBot() {
        getSettingRepository().registerSettings(attack,
                targeting,
                critical,
                additional,
                attackMode,
                reactionTime,
                attackDelay,
                conditions,
                targetSorting,
                criticalType,
                cooldown,
                handleShield,
                maceFallDistance,
                fakeChance,
                fakeRange,
                fakeItems,
                enemyConditions);

        fakeChance.setRenderCondition(() -> !fakeItems.getValue().isEmpty());
        fakeRange.setRenderCondition(() -> !fakeItems.getValue().isEmpty());
    }
}