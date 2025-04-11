/**
 * Created: 12/12/2024
 */
package com.chorus.common.util.math;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;

public final class TimerUtils {
    @Getter
    private long lastMS;
    private long lastUpdateTick;

    /**
     * Gets the current game time in ticks.
     *
     * @return The current game time in ticks, or 0 if player is null.
     */
    private long currentTick() {
        var player = MinecraftClient.getInstance().player;

        return (player != null)
               ? player.age
               : 0;
    }

    /**
     * Gets the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    public long currentTimeMillis() {
        return System.nanoTime() / 1000000L;
    }

    /**
     * Checks if the specified delay in milliseconds has passed.
     *
     * @param milliSec The delay in milliseconds.
     * @return true if the delay has passed, false otherwise.
     */
    public boolean delay(final float milliSec) {
        return currentTimeMillis() - lastMS >= milliSec;
    }

    /**
     * Resets both the millisecond and tick timers.
     */
    public void reset() {
        lastMS = currentTimeMillis();
        lastUpdateTick = currentTick();
    }

    /**
     * Checks if the specified number of ticks has elapsed.
     *
     * @param ticks The number of ticks to check against.
     * @return true if the specified number of ticks has elapsed, false otherwise.
     */
    public boolean hasElapsed(int ticks) {
        if (ticks == 0) return true;
        long currentTick = currentTick();

        if (currentTick < lastUpdateTick) {
            lastUpdateTick = currentTick;

            return false;
        }

        return currentTick - lastUpdateTick >= ticks;
    }

    /**
     * Gets the number of ticks elapsed since the last reset.
     *
     * @return The number of ticks elapsed.
     */
    public long getElapsedTicks() {
        return currentTick() - lastUpdateTick;
    }

    /**
     * Checks if the specified number of milliseconds has elapsed.
     *
     * @param milliseconds The number of milliseconds to check against.
     * @return true if the specified time has elapsed, false otherwise.
     */
    public boolean hasReached(final double milliseconds) {
        if (milliseconds == 0) return true;
        return currentTimeMillis() - lastMS >= milliseconds;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
