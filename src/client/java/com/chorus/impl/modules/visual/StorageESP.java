/**
 * Created: 2/4/2025
 */

package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.MultiSetting;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

@ModuleInfo(
        name        = "StorageESP",
        description = "Renders Positions Of Storage Blocks",
        category    = ModuleCategory.VISUAL
)
public class StorageESP extends BaseModule implements QuickImports {

    private final ModeSetting boxType = new ModeSetting("Box Type", "Choose which type of box is rendered"
            , "Outlined Shaded", "Outlined Shaded", "Outlined", "Shaded");

    private final MultiSetting storageTypes = new MultiSetting("Storage", "Choose which type of storage is rendered"
            , "Chest", "Trapped Chest", "Ender Chest", "Barrel", "Shulker");
    @RegisterEvent
    private void Render3DEvent(Render3DEvent event) {
        if (event.getMode().equals(com.chorus.impl.events.render.Render3DEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;

            ChunkPos currentPosition = mc.player.getChunkPos();
            int viewDistance = mc.options.getClampedViewDistance();
            ChunkPos start = new ChunkPos(currentPosition.x - viewDistance, currentPosition.z - viewDistance);
            ChunkPos end = new ChunkPos(currentPosition.x + viewDistance, currentPosition.z + viewDistance);

            for (int x = start.x; x <= end.x; x++) {
                for (int z = start.z; z <= end.z; z++) {
                    if (!mc.world.isChunkLoaded(x, z)) continue;
                    for (BlockPos pos : mc.world.getChunk(x, z).getBlockEntityPositions()) {
                        if (!isInFov(pos)) continue;
                        if (!
                                ((mc.world.getBlockState(pos).getBlock() instanceof ChestBlock && storageTypes.getSpecificValue("Chest"))
                                || (mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock && storageTypes.getSpecificValue("Shulker"))
                                || (mc.world.getBlockState(pos).getBlock() instanceof BarrelBlock && storageTypes.getSpecificValue("Barrel"))
                                || (mc.world.getBlockState(pos).getBlock() instanceof EnderChestBlock && storageTypes.getSpecificValue("Ender Chest"))
                                || (mc.world.getBlockState(pos).getBlock() instanceof TrappedChestBlock && storageTypes.getSpecificValue("Trapped Chest")))) continue;
                        switch (boxType.getValue()) {
                            case "Outlined Shaded" -> Render3DEngine.renderOutlinedShadedBox(pos.toCenterPos().subtract(0, 0.5, 0), getColor(Objects.requireNonNull(mc.world.getBlockEntity(pos))), 50, event.getMatrices(), 0.5f, 1);
                            case "Shaded" -> Render3DEngine.renderShadedBox(pos.toCenterPos().subtract(0, 0.5, 0), getColor(Objects.requireNonNull(mc.world.getBlockEntity(pos))), 50, event.getMatrices(), 0.5f, 1);
                            case "Outlined" -> Render3DEngine.renderOutlinedBox(pos.toCenterPos().subtract(0, 0.5, 0), getColor(Objects.requireNonNull(mc.world.getBlockEntity(pos))), event.getMatrices(), 0.5f, 1);
                        }
                    }
                }
            }
        }
    }
    private Map<Class<? extends BlockEntity>, Color> blockColors = Map.of(
            TrappedChestBlockEntity.class, new Color(255, 50, 20),
            ChestBlockEntity.class, new Color(176, 111, 20),
            EnderChestBlockEntity.class, new Color(137, 20, 255),
            ShulkerBoxBlockEntity.class, new Color(154, 20, 178),
            BarrelBlockEntity.class, new Color(255, 160, 160)
    );


    private Color getColor(BlockEntity blockEntity) {
        return blockColors.getOrDefault(blockEntity.getClass(), new Color(255, 255, 255, 0));
    }
    public boolean isInFov(BlockPos blockPos) {
        Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d lookVec = mc.player.getRotationVec(1.0F);
        Vec3d blockVec = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5).subtract(playerPos).normalize();

        double dotProduct = lookVec.dotProduct(blockVec);
        double fovThreshold = Math.cos(Math.toRadians(mc.options.getFov().getValue().doubleValue()));

        return dotProduct > fovThreshold;
    }




    public StorageESP() {
        getSettingRepository().registerSettings(boxType, storageTypes);
    }
}