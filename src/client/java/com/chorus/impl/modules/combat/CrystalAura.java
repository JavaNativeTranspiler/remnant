
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.math.rotation.RotationUtils;
import com.chorus.common.util.player.DamageUtils;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@ModuleInfo(
        name = "CrystalAura",
        description = "CrystalAura",
        category = ModuleCategory.COMBAT
)
public class CrystalAura extends BaseModule implements QuickImports {
    // conditions
    private final SettingCategory conditionSetting = new SettingCategory("Condition Settings");
    private final SettingCategory placeSetting = new SettingCategory("Placing Settings");
    private final SettingCategory breakSetting = new SettingCategory("Breaking Settings");
    private final SettingCategory otherSetting = new SettingCategory("Other Settings");

    private final MultiSetting actions = new MultiSetting(conditionSetting, "Actions", "Decide what autocrystal does", "Place", "Break");
    private final BooleanSetting onRightClick = new BooleanSetting(conditionSetting, "On Right Click", "Activate Crystal Only Right Click", true);
    private final BooleanSetting pauseOnKill = new BooleanSetting(conditionSetting, "Pause On Kill", "Pause On Enemy Death", true);
    private final BooleanSetting damageTick = new BooleanSetting(conditionSetting, "Damage Tick", "Time Crystal Explosions To Hurt-Time", true);
    // place
    private final ModeSetting placeMode = new ModeSetting(placeSetting, "Place Input", "Select Place Input", "Click", "Click", "Packet");
    private final RangeSetting<Integer> placeDelay = new RangeSetting<>(placeSetting, "Place Delay", "Delay it uses to Place crystals", 0, 250, 50, 50);
    // break
    private final ModeSetting breakMode = new ModeSetting(breakSetting, "Break Input", "Select Break Input", "Click", "Click", "Packet");
    private final RangeSetting<Integer> breakDelay = new RangeSetting<>(breakSetting, "Break Delay", "Delay it uses to break crystals", 0, 250, 50, 50);
    private final NumberSetting<Integer> failChance = new NumberSetting<>(breakSetting, "Fail Chance", "Chance To Fail Breaking Crystal", 10, 0, 100);
    //break expansion scan type
    private final ModeSetting scanType = new ModeSetting(breakSetting, "Scan Type", "Select Method To Scan For Crystals", "Raytrace", "Raytrace", "Expanded Raytrace");
    private final NumberSetting<Double> expansion = new NumberSetting<>(breakSetting, "Expansion", "Expansion For Expanded Raytrace Scan Type", 0.0, 0.0, 1.0);
    private final NumberSetting<Double> maxDamage = new NumberSetting<>(breakSetting, "Max Self Damage", "Max Self Damage", 10.0, 0.0, 20.0);
    // other
    private final BooleanSetting preventPlace = new BooleanSetting(otherSetting, "Prevent Minecraft Place", "Allows only the client to place crystals", true);

    private final TimerUtils placeTimer = new TimerUtils();
    private final TimerUtils breakTimer = new TimerUtils();
    int lastAttackTime = 0;

    public CrystalAura() {
        getSettingRepository().registerSettings(
                conditionSetting,
                placeSetting,
                breakSetting,
                otherSetting,
                actions,
                onRightClick,
                pauseOnKill,
                damageTick,
                placeMode,
                placeDelay,
                breakMode,
                breakDelay,
                failChance,
                scanType,
                expansion,
                maxDamage,
                preventPlace);
        placeSetting.setRenderCondition(() -> actions.getSpecificValue("Place"));
        breakSetting.setRenderCondition(() -> actions.getSpecificValue("Break"));
    }

    @RegisterEvent
    private void Render3DEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null || !mc.isWindowFocused()) return;
        if (pauseOnKill.getValue() && mc.world.getPlayers().stream().anyMatch(player -> player != mc.player && !player.isAlive() && player.squaredDistanceTo(mc.player) < 36))
            return;
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) return;

        if (lastAttackTime < 0)
            lastAttackTime = mc.player.age;

        if (!breakTimer.hasReached(50)) return;
        if (SocialManager.getTarget() == null) return;
        Vec3d bestPoint = findPlacePoints(3, event.getMatrices(), SocialManager.getTarget());
        if (bestPoint == null) return;

        Render3DEngine.renderShadedBox(bestPoint, Color.GREEN, 50, event.getMatrices(), 0.5f, 1f);
        float[] rots = RotationUtils.calculate(bestPoint.add(0, 1, 0), bestPoint);
        BlockHitResult hitResult = RotationUtils.rayTrace(rots[0], rots[1], 6f);
        if (placeTimer.hasReached(50) && hitResult != null) {
            placeTimer.reset();
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(bestPoint, Direction.UP, BlockPos.ofFloored(bestPoint), true));

        }
        if (findCrystal(3.0, SocialManager.getTarget()) != null) {
            mc.interactionManager.attackEntity(mc.player, findCrystal(3.0, SocialManager.getTarget()));
            mc.player.swingHand(Hand.MAIN_HAND);
            breakTimer.reset();
        }
    }

    public Vec3d findPlacePoints(int offset, MatrixStack matrices, PlayerEntity player) {
        Vec3d bestPoint = null;
        float currentDamage = -1;
        for (float x = -offset; x <= offset; x += 1f) {
            for (float y = -offset; y <= offset; y += 1f) {
                for (float z = -offset; z <= offset; z += 1f) {
                    Vec3d point = new Vec3d(
                            player.getBlockPos().getX() + x + .5,
                            player.getBlockPos().getY() + y,
                            player.getBlockPos().getZ() + z + .5);

                    BlockState blockState = mc.world.getBlockState(BlockPos.ofFloored(point));
                    BlockState aboveBlock = mc.world.getBlockState(BlockPos.ofFloored(point).up());
                    if (!(blockState.getBlock().equals(Blocks.OBSIDIAN) || blockState.getBlock().equals(Blocks.BEDROCK))) {
                        continue;
                    }
                    Vec3d up = point.add(0, 1, 0);
                    if (player.getBoundingBox().intersects(up.x - 0.5, up.y - 0.5, up.z - 0.5, up.x + 0.5, up.y + 0.5, up.z + 0.5))
                        continue;
                    if (aboveBlock.isAir()) {
                        Render3DEngine.renderShadedBox(point, Color.WHITE, 50, matrices, 0.5f, 1f);
                        if (DamageUtils.calculateCrystalDamage(player, BlockPos.ofFloored(point).up().toCenterPos()) > currentDamage) {
                            currentDamage = DamageUtils.calculateCrystalDamage(player, BlockPos.ofFloored(point).up().toCenterPos());
                            bestPoint = point;
                        }
                    } else {
                        //Render3DEngine.renderShadedBox(point, Color.RED, 50, matrices, 0.5f, 1f);
                    }
                }
            }
        }
        return bestPoint;
    }

    public void handlePlace(BlockHitResult blockHitResult) {
        if (!placeTimer.hasReached(placeDelay.getRandomValue().floatValue())) return;
        if (!blockHitResult.getBlockPos().isWithinDistance(mc.player.getPos(), 4.0)) return;

        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, 0));
        mc.player.swingHand(mc.player.getActiveHand());
        placeTimer.reset();
        if (preventPlace.getValue()) mc.options.useKey.setPressed(false);
    }

    public void handleBreak(Entity entity) {
        if (!breakTimer.hasReached(breakDelay.getRandomValue().floatValue())) return;
        if (DamageUtils.calculateCrystalDamage(mc.player, entity.getPos()) > maxDamage.getValue()) return;
        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private Entity findCrystal(double range, PlayerEntity player) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d viewVector = mc.player.getRotationVecClient();
        Vec3d extendedPoint = cameraPos.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
                if (player.distanceTo(entity) <= range) {
                    return entity;
                }
            }
        }
        return null;
    }

}
