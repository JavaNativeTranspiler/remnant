package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.system.render.ColorUtils;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.common.util.world.SimulatedPlayer;
import com.chorus.impl.events.render.Render3DEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.awt.*;

@ModuleInfo(name = "Predict", description = "Predicts the path of players", category = ModuleCategory.VISUAL)
public class ElytraPredict extends BaseModule implements QuickImports {
    private final NumberSetting<Float> predictionTime = new NumberSetting<>(null, "Prediction Time", "How many ticks to predict ahead", 1.F, 0.1F, 25F);
    private final BooleanSetting showSelf = new BooleanSetting(null, "Show Self", "Show your own Elytra path", false);

    public ElytraPredict() {
        getSettingRepository().registerSettings(predictionTime, showSelf);
    }

    @RegisterEvent
    private void Render3DEvent(Render3DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render3DEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            SimulatedPlayer simulator = new SimulatedPlayer(mc.player);
            simulator.setInput(
                    mc.options.forwardKey.isPressed(),
                    mc.options.backKey.isPressed(),
                    mc.options.leftKey.isPressed(),
                    mc.options.rightKey.isPressed(),
                    mc.options.jumpKey.isPressed(), mc.player.isSprinting());
            for (int i = 0; i <= predictionTime.getValue().intValue() ; i++) {
                simulator.tick();
            }

            Render3DEngine.renderShadedBox(simulator.getPosition(), Color.white, 25, event.getMatrices(), 0.5f, 1);
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (entity == mc.player && !showSelf.getValue()) {
                    continue;
                }

                Color settingColor = ColorUtils.getAssociatedColor(entity);
                
                if (entity.isGliding()) {
                    PredictedPlayer predictedPlayer = predictElytraPosition(entity.getLerpedPos(mc.getRenderTickCounter().getTickDelta(false)), entity.getVelocity(), predictionTime.getValue());
                    Render3DEngine.renderOutlinedBox(predictedPlayer.getLerpedPos(event.getTickDelta()), settingColor, event.getMatrices(), predictedPlayer.getWidth(), predictedPlayer.getHeight());
                }
            }
        }
    }

    @Getter
    @AllArgsConstructor
    class PredictedPlayer {
        private Vec3d position;
        private Vec3d motion;
        private float width;
        private float height;
        private Vec3d prevPosition;

        public PredictedPlayer(Vec3d position, Vec3d motion, float width, float height) {
            this.position = position;
            this.motion = motion;
            this.width = width;
            this.height = height;
            this.prevPosition = position;
        }

        public void update(Vec3d position, Vec3d motion) {
            this.prevPosition = this.position;
            this.position = position;
            this.motion = motion;
        }

        public final Vec3d getLerpedPos(float delta) {
            double d = MathHelper.lerp((double)delta, this.prevPosition.x, this.position.x);
            double e = MathHelper.lerp((double)delta, this.prevPosition.y, this.position.y);
            double f = MathHelper.lerp((double)delta, this.prevPosition.z, this.position.z);
            return new Vec3d(d, e, f);
        }

        public boolean isOnGround() {
            BlockPos floorPos = BlockPos.ofFloored(position.subtract(0, 0.2, 0));
            BlockState state = mc.world.getBlockState(floorPos);
            return !state.isAir() && state.isSolidBlock(mc.world, floorPos);
        }

        public float getWidth() {
            return !this.isOnGround() ? width : width / 0.8F;
        }

        public float getHeight() {
            return !this.isOnGround() ? height : height / 0.8F;  // fix this, should height on ground should be the default player height
        }

        public void applyElytraPhysics(double stepTime) {
            double horizontalDrag = 0.99;
            double verticalDrag = 0.98;
            double gravity = 0.08;

            this.motion = new Vec3d(
                motion.x * horizontalDrag,
                motion.y * verticalDrag - gravity * stepTime,
                motion.z * horizontalDrag
            );
        }

        public void handleCollision(BlockHitResult result) {
            if (result.getType() != HitResult.Type.MISS) {
                BlockState hitState = mc.world.getBlockState(result.getBlockPos());
                if (!hitState.isSolidBlock(mc.world, result.getBlockPos())) {
                    this.position = result.getPos();
                    return;
                }
                
                Vec3d normal = Vec3d.of(result.getSide().getVector());
                
                if (normal.y != 0) {
                    this.position = result.getPos();
                    return;
                }
                
                double dot = motion.dotProduct(normal);
                Vec3d reflection = motion.subtract(normal.multiply(2.0 * dot));
                
                double angle = Math.abs(dot / (motion.length() * normal.length()));
                double slideRatio = 1.0 - angle;
                
                Vec3d slideMotion = motion.subtract(normal.multiply(dot));
                this.motion = reflection.multiply(angle).add(slideMotion.multiply(slideRatio)).multiply(0.7);
                
                this.position = result.getPos();
            }
        }
    }
    
    private PredictedPlayer predictElytraPosition(Vec3d currentPos, Vec3d currentMotion, double time) {
        PredictedPlayer player = new PredictedPlayer(currentPos, currentMotion, mc.player.getWidth(), mc.player.getHeight());
        
        int steps = (int)(time * 20);
        double stepTime = time / steps;
        
        for (int i = 0; i < steps; i++) {
            if (player.isOnGround()) {
                return player;
            }
            
            player.applyElytraPhysics(stepTime);
            Vec3d nextPos = player.getPosition().add(player.getMotion().multiply(stepTime));
            
            RaycastContext context = new RaycastContext(
                player.getPosition(),
                nextPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
            );
            BlockHitResult result = mc.world.raycast(context);
            
            if (result.getType() != HitResult.Type.MISS) {
                player.handleCollision(result);
                continue;
            }
            
            player.update(nextPos, player.getMotion());
        }
        
        return player;
    }
}