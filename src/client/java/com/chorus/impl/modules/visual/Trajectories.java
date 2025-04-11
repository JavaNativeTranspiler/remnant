/**
 * Created: 3/2/2025
 */

package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render3DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;
import java.util.function.Predicate;

@ModuleInfo(name = "Trajectories", description = "where projectile", category = ModuleCategory.VISUAL)
public class Trajectories extends BaseModule implements QuickImports {
    @RegisterEvent
    private void render2DListener(Render3DEvent event) {
        Matrix4f matrix = event.getMatrices().peek().getPositionMatrix();
        ItemStack itemStack = mc.player.getMainHandStack();

        if (isThrowable(itemStack)) {
            float initialVelocity = 52f;
            if (itemStack.getItem() == Items.BOW && mc.player.isUsingItem())
                initialVelocity *= BowItem.getPullProgress(mc.player.getItemUseTime());

            Camera camera = mc.gameRenderer.getCamera();
            Vec3d offset = Render3DEngine.getEntityPositionOffsetInterpolated(mc.cameraEntity,
                    event.getTickDelta());
            Vec3d eyePos = mc.cameraEntity.getEyePos();

            Vec3d right = Vec3d.fromPolar(0, camera.getYaw() + 90).multiply(0.14f);
            Vec3d lookDirection = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
            Vec3d velocity = lookDirection.multiply(initialVelocity).multiply(0.2f);

            Vec3d prevPoint = new Vec3d(0, 0, 0).add(eyePos).subtract(offset).add(right);
            Vec3d landPosition = null;

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            Tessellator tessellator = RenderSystem.renderThreadTesselator();
            for (int iteration = 0; iteration < 150; iteration++) {
                BufferBuilder buffer;
                buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                Vec3d nextPoint = prevPoint.add(velocity.multiply(0.1));
                Vec3d start = mc.player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(true)).add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).subtract(mc.gameRenderer.getCamera().getPos());
                if (iteration == 0) {
                    buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                            .color(255, 255, 255, 255).color(1 / 255f, 255, 1 / 255f, 255);
                }
                Vec3d x1 = prevPoint.subtract(mc.gameRenderer.getCamera().getPos());
                Vec3d x2 = nextPoint.subtract(mc.gameRenderer.getCamera().getPos());
                buffer.vertex(matrix, (float) x1.x, (float) x1.y, (float) x1.z)
                        .color(255, 255, 255, 255).color(1 / 255f, 255 / 255f, 1 / 255f, 255);
                buffer.vertex(matrix, (float) x2.x, (float) x2.y, (float) x2.z)
                        .color(255, 255, 255, 255).color(1 / 255f, 255 / 255f, 1 / 255f, 255);

                RaycastContext context = new RaycastContext(prevPoint, nextPoint, RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE, mc.player);
                BlockHitResult result = mc.world.raycast(context);
                if (result.getType() != HitResult.Type.MISS) {
                    landPosition = result.getPos();
                    break;
                } else {
                    Box box = new Box(prevPoint, nextPoint);
                    Predicate<Entity> predicate = e -> !e.isSpectator() && e.canHit();
                    EntityHitResult entityResult = ProjectileUtil.raycast(mc.player, prevPoint, nextPoint, box,
                            predicate, 4096);

                    if (entityResult != null && entityResult.getType() != HitResult.Type.MISS) {
                        landPosition = entityResult.getPos();
                        break;
                    }
                }
                BufferRenderer.drawWithGlobalProgram(Objects.requireNonNull(buffer.endNullable()));
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                RenderSystem.disableBlend();
                prevPoint = nextPoint;
                velocity = velocity.multiply(0.99).add(0, throwableGravity(itemStack.getItem()), 0);
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);

            if (landPosition != null) {
                float size = 0.15f;
                Vec3d pos = landPosition.add(0, size, 0).subtract(size / 2, size / 2, size / 2);
                Color color = Color.WHITE;
                MatrixStack matrixStack = event.getMatrices();

                Render3DEngine.renderOutlinedBox(pos, color, matrixStack, size, size * 2);
            }
        }
    }

    public double throwableGravity(Item item) {
        if (item == Items.BOW)
            return -0.045f;
        else
            return -0.13f;
    }

    public static boolean isThrowable(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.BOW || item == Items.SNOWBALL || item == Items.EGG || item == Items.FIRE_CHARGE || item == Items.TRIDENT || item instanceof EnderPearlItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof FishingRodItem || item instanceof EnderEyeItem;
    }
}
