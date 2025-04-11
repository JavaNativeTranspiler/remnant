/**
 * Created: 2/8/2025
 */

package com.chorus.impl.events.world;

import cc.polymorphism.eventbus.Event;
import lombok.Getter;
import net.minecraft.client.world.ClientWorld;

@Getter
public class WorldChangeEvent extends Event {
    ClientWorld world;
    public WorldChangeEvent(ClientWorld world){
        this.world = world;
    }
}