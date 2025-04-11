/**
 * Created: 12/11/2024
 */

package com.chorus.common.util.world;


import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    /**
     * Checks if a block at a given position is of a specific type.
     *
     * @param pos The position to check.
     * @param block The block type to compare against.
     * @return true if the block at the position is of the specified type, false otherwise.
     */
    public static boolean isBlockType(final BlockPos pos, final Block block) {
        return mc.world.getBlockState(pos).getBlock() == block;
    }

    /**
     * Checks if a respawn anchor at the given position can be exploded.
     *
     * @param pos The position of the respawn anchor.
     * @return true if the anchor can be exploded, false otherwise.
     */
    public static boolean canExplodeAnchor(final BlockPos pos) {
        return isBlockType(pos, Blocks.RESPAWN_ANCHOR) && mc.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) != 0;
    }

    /**
     * Checks if a block is air.
     *
     * @param pos The position to check.
     * @return true if the block is air, false otherwise.
     */
    public static boolean isAir(BlockPos pos) {
        return mc.world.getBlockState(pos).isAir();
    }

    /**
     * Checks if a block is replaceable (like tall grass, flowers, etc.).
     *
     * @param pos The position to check.
     * @return true if the block is replaceable, false otherwise.
     */
    public static boolean isReplaceable(BlockPos pos) {
        return mc.world.getBlockState(pos).isReplaceable();
    }

    /**
     * Checks if a block is liquid (water or lava).
     *
     * @param pos The position to check.
     * @return true if the block is liquid, false otherwise.
     */
    public static boolean isLiquid(BlockPos pos) {
        return mc.world.getBlockState(pos).getFluidState().isStill();
    }

    /**
     * Gets the hardness of a block at the given position.
     *
     * @param pos The position of the block.
     * @return The hardness value of the block.
     */
    public static float getHardness(BlockPos pos) {
        return mc.world.getBlockState(pos).getHardness(mc.world, pos);
    }

    /**
     * Checks if a block is breakable (not bedrock or barrier).
     *
     * @param pos The position to check.
     * @return true if the block is breakable, false otherwise.
     */
    public static boolean isBreakable(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block != Blocks.BEDROCK && block != Blocks.BARRIER;
    }

    /**
     * Gets a list of adjacent block positions.
     *
     * @param pos The central position.
     * @return A list of adjacent BlockPos.
     */
    public static List<BlockPos> getAdjacentBlocks(BlockPos pos) {
        List<BlockPos> adjacentBlocks = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            adjacentBlocks.add(pos.offset(direction));
        }
        return adjacentBlocks;
    }

    /**
     * Checks if a block can be placed at the given position.
     *
     * @param pos The position to check.
     * @return true if a block can be placed, false otherwise.
     */
    public static boolean canPlaceBlock(BlockPos pos) {
        return isAir(pos) || isReplaceable(pos);
    }

    /**
     * Gets the light level at a given position.
     *
     * @param pos The position to check.
     * @return The light level (0-15).
     */
    public static int getLightLevel(BlockPos pos) {
        return mc.world.getLightLevel(pos);
    }

    /**
     * Checks if a position is exposed to sky.
     *
     * @param pos The position to check.
     * @return true if exposed to sky, false otherwise.
     */
    public static boolean isExposedToSky(BlockPos pos) {
        return mc.world.isSkyVisible(pos);
    }

    /**
     * Checks if a block is considered "safe" to stand on (solid and not harmful).
     *
     * @param pos The position to check.
     * @return true if the block is safe to stand on, false otherwise.
     */
    public static boolean isSafeBlock(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.isSolid() &&
                !(state.getBlock() instanceof MagmaBlock) &&
                !(state.getBlock() instanceof CactusBlock) &&
                !(state.getBlock() instanceof AbstractFireBlock);
    }

    /**
     * Gets the blast resistance of a block.
     *
     * @param pos The position of the block.
     * @return The blast resistance value.
     */
    public static float getBlastResistance(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().getBlastResistance();
    }

    /**
     * Checks if a block is considered "natural" (not player-placed).
     *
     * @param pos The position to check.
     * @return true if the block is likely natural, false otherwise.
     */
    public static boolean isNaturalBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return block == Blocks.GRASS_BLOCK ||
                block == Blocks.DIRT ||
                block == Blocks.STONE ||
                block == Blocks.SAND ||
                block == Blocks.GRAVEL;
    }

    /**
     * Finds the nearest block of a specific type within a given radius.
     *
     * @param center The center position to search from.
     * @param block The block type to search for.
     * @param radius The search radius.
     * @return The nearest BlockPos of the specified block type, or null if not found.
     */
    public static BlockPos findNearestBlock(BlockPos center, Block block, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (isBlockType(pos, block)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}