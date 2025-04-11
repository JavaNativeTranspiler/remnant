
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.*;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.player.DamageUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@ModuleInfo(
        name = "AutoCrystal",
        description = "Uses and Explodes Crystals",
        category = ModuleCategory.COMBAT
)
public class AutoCrystal extends BaseModule implements QuickImports {
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

    public AutoCrystal() {
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
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null || mc.currentScreen != null || !mc.isWindowFocused()) return;
            if (pauseOnKill.getValue() && mc.world.getPlayers().stream().anyMatch(player -> player != mc.player && !player.isAlive() && player.squaredDistanceTo(mc.player) < 36))
                return;
            if (onRightClick.getValue() && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 1) != GLFW_PRESS) return;
            if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) return;
            HitResult hitResult = mc.crosshairTarget;


            if (lastAttackTime < 0)
                lastAttackTime = mc.player.age;

            if (actions.getSpecificValue("Place"))
                if (hitResult instanceof BlockHitResult blockHitResult) {
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    Block block = mc.world.getBlockState(blockPos).getBlock();
                    if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
                        handlePlace(blockHitResult);
                    }
                }

            if (actions.getSpecificValue("Break"))
                if (!damageTick.getValue() || (mc.player.getAttacking() instanceof PlayerEntity || mc.world.getPlayers().stream().anyMatch(player -> player != mc.player && player.hurtTime == 0) || mc.player.age - lastAttackTime > 12)) {
                    if (mc.player.getAttacking() instanceof PlayerEntity) {
                        lastAttackTime = mc.player.age;
                    }
                    if (scanType.getValue().equals("Raytrace")) {
                        if (hitResult instanceof EntityHitResult entityHitResult) {
                            Entity entity = entityHitResult.getEntity();
                            if (entity instanceof EndCrystalEntity && entity.isAlive()) {
                                handleBreak(entity);
                            }
                        }
                    } else {
                        Entity hitboxEntity = raycastCrystal(4.0f);
                        if (hitboxEntity != null) {
                            handleBreak(hitboxEntity);
                        }
                    }
                }
        }
    }

    public void handlePlace(BlockHitResult blockHitResult) {
        if (!placeTimer.hasReached(placeDelay.getRandomValue().floatValue())) return;
        if (!blockHitResult.getBlockPos().isWithinDistance(mc.player.getPos(), 4.0)) return;
        var random = MathUtils.randomInt(1, 100) >= failChance.getValue();

        if (random) {
            switch (placeMode.getValue()) {
                case "Click":
                    InputUtils.simulateClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                    mc.options.useKey.setPressed(true);
                    InputUtils.simulateRelease(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                    break;
                case "Packet":
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult, 0));
                    mc.player.swingHand(mc.player.getActiveHand());
                    break;
            }
        } else {
            InputUtils.simulateClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            mc.options.attackKey.setPressed(true);
            mc.options.attackKey.setPressed(false);
            InputUtils.simulateRelease(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }
        placeTimer.reset();
        if (preventPlace.getValue()) mc.options.useKey.setPressed(false);
    }

    public void handleBreak(Entity entity) {
        if (!breakTimer.hasReached(breakDelay.getRandomValue().floatValue())) return;
        if (DamageUtils.calculateCrystalDamage(mc.player, entity.getPos()) > maxDamage.getValue()) return;
        switch (breakMode.getValue()) {
            case "Click":
                InputUtils.simulateClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
                mc.options.attackKey.setPressed(true);
                mc.options.attackKey.setPressed(false);
                InputUtils.simulateRelease(GLFW.GLFW_MOUSE_BUTTON_LEFT);
                break;
            case "Packet":
                mc.interactionManager.attackEntity(mc.player, entity);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
        }
    }

    private Entity raycastCrystal(double range) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d viewVector = mc.player.getRotationVecClient();
        Vec3d extendedPoint = cameraPos.add(viewVector.x * range, viewVector.y * range, viewVector.z * range);

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
                if (entity.getBoundingBox().expand(expansion.getValue()).intersects(cameraPos, extendedPoint)) {
                    return entity;
                }
            }
        }
        return null;
    }

}
