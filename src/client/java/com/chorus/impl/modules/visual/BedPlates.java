package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render3DEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@ModuleInfo(
        name = "BedPlates",
        description = "Renders Defense A Bed Has.",
        category = ModuleCategory.VISUAL
)
public class BedPlates extends BaseModule implements QuickImports {
    private final SettingCategory visual = new SettingCategory("Visual Settings");
    private final BooleanSetting showBackground = new BooleanSetting(visual, "Show Background", "Display a background behind the text", true);

    private Map<BlockEntity, Pair<Rectangle, Boolean>> hashMap = new HashMap<>();
    RenderTickCounter renderTickCounter = mc.getRenderTickCounter();

    @RegisterEvent
    private void Render3DEvent(Render3DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render3DEvent.Mode.PRE)) {
            hashMap.clear();
            if (mc.player == null || mc.world == null) return;
            ChunkPos currentPosition = mc.player.getChunkPos();
            int viewDistance = mc.options.getClampedViewDistance();
            ChunkPos start = new ChunkPos(currentPosition.x - viewDistance, currentPosition.z - viewDistance);
            ChunkPos end = new ChunkPos(currentPosition.x + viewDistance, currentPosition.z + viewDistance);

            for (int x = start.x; x <= end.x; x++) {
                for (int z = start.z; z <= end.z; z++) {
                    if (!mc.world.isChunkLoaded(x, z)) continue;
                    for (BlockPos pos : mc.world.getChunk(x, z).getBlockEntityPositions()) {
                        BlockState blockState = mc.world.getBlockState(pos);
                        if (!(blockState.getBlock() instanceof BedBlock)) continue;

                        if (blockState.get(BedBlock.PART) == BedPart.HEAD) continue;

                        BlockEntity blockEntity = mc.world.getBlockEntity(pos);
                        int tickDelta = (int) renderTickCounter.getTickDelta(false);
                        Vec3d prevPos = new Vec3d(blockEntity.getPos().getX() + 0.5, blockEntity.getPos().getY(), blockEntity.getPos().getZ() + 0.5);
                        Vec3d interpolated = prevPos.add(blockEntity.getPos().toCenterPos().add(prevPos).multiply(tickDelta));

                        float halfWidth = 0.5f / 2.0f;
                        Box boundingBox = new Box(
                                interpolated.x,
                                interpolated.y,
                                interpolated.z,
                                interpolated.x,
                                interpolated.y + 1,
                                interpolated.z
                        ).expand(0.1, 0.1, 0.1);

                        Vec3d[] corners = new Vec3d[]{
                                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),

                                new Vec3d(boundingBox.minX, boundingBox.maxY + 0.1, boundingBox.minZ),
                                new Vec3d(boundingBox.maxX, boundingBox.maxY + 0.1, boundingBox.minZ),
                                new Vec3d(boundingBox.maxX, boundingBox.maxY + 0.1, boundingBox.maxZ),
                                new Vec3d(boundingBox.minX, boundingBox.maxY + 0.1, boundingBox.maxZ)
                        };

                        Rectangle rectangle = null;
                        boolean visible = false;

                        for (Vec3d corner : corners) {
                            Pair<Vec3d, Boolean> projection = Render3DEngine.project(event.getMatrices().peek().getPositionMatrix(), event.getProjectionMatrix(), corner);
                            if (projection.getRight()) {
                                visible = true;
                            }
                            Vec3d projected = projection.getLeft();

                            if (rectangle == null) {
                                rectangle = new Rectangle((int) projected.getX(), (int) projected.getY(), (int) projected.getX(), (int) projected.getY());
                            } else {
                                if (rectangle.x > projected.getX()) {
                                    rectangle.x = (int) projected.getX();
                                }
                                if (rectangle.y > projected.getY()) {
                                    rectangle.y = (int) projected.getY();
                                }
                                if (rectangle.z < projected.getX()) {
                                    rectangle.z = (int) projected.getX();
                                }
                                if (rectangle.w < projected.getY()) {
                                    rectangle.w = (int) projected.getY();
                                }
                            }
                        }

                        hashMap.put(blockEntity, new Pair<>(rectangle, visible));
                    }
                }
            }
        }
    }
    int[][] offsets = {
            {1, 0, 0},    // Right
            {0, 0, 1},    // Forward
            {-1, 0, 0},   // Left
            {0, 0, -1},    // Back
            {0, 1, 0}    // Top
    };
    @RegisterEvent
    private void Render2DEvent(com.chorus.impl.events.render.Render2DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render2DEvent.Mode.PRE)) {
            MatrixStack matrix = event.getContext().getMatrices();
            DrawContext context = event.getContext();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            if (!hashMap.isEmpty() && hashMap.entrySet().stream().anyMatch(entityPairEntry -> entityPairEntry.getValue().getRight())) {
                for (Map.Entry<BlockEntity, Pair<Rectangle, Boolean>> entry : hashMap.entrySet()) {
                    Pair<Rectangle, Boolean> pair = entry.getValue();
                    FontAtlas font = Chorus.getInstance().getFonts().getInterSemiBold();
                    if (pair.getRight()) {
                        float y = (float) (pair.getLeft().y - 2 - 1 - 4.5f);

                        Set<ItemStack> blocks = new LinkedHashSet<>();
                        for (int[] offsetArr : offsets) {
                            BlockPos newPos = entry.getKey().getPos().add(offsetArr[0], 0, offsetArr[2]);
                            Block block = mc.world.getBlockState(newPos).getBlock();
                            ItemStack itemStack = block.asItem().getDefaultStack();
                            if (block instanceof BedBlock) continue;
                            if (block instanceof AirBlock) continue;
                            boolean exists = blocks.stream().noneMatch(existingStack -> existingStack.getItem().equals(itemStack.getItem()));

                            if (exists) {
                                blocks.add(itemStack);
                            }
                        }

                        if (blocks.isEmpty()) continue;
                        int xOffset = blocks.size() * 18;

                        float centerX = (float) ((pair.getLeft().x + pair.getLeft().z ) / 2);
                        if (showBackground.getValue() ) {
                            context.fill(
                                    (int)(centerX - 2),
                                    (int)(y - 2),
                                    (int)(centerX + xOffset),
                                    (int)(y + 18),
                                    0x80000000
                            );
                        }
                        int itemOffset = 0;
                        for (ItemStack item : blocks) {
                            context.drawItem(item, (int) (centerX + itemOffset), (int) y);
                            itemOffset += 18;
                        }

                    }
                }
            }
            RenderSystem.disableBlend();
        }
    }

    public static class Rectangle {
        public double x;
        public double y;
        public double z;
        public double w;

        public Rectangle(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }

    public BedPlates() {
        getSettingRepository().registerSettings(visual, showBackground);
    }
}