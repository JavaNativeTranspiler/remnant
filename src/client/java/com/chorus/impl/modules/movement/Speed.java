/**
 * Created: 2/5/2025
 */

package com.chorus.impl.modules.movement;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.MovementUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Box;

@ModuleInfo(
        name = "Speed",
        description = "hack",
        category = ModuleCategory.MOVEMENT
)
public class Speed extends BaseModule implements QuickImports {
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (!isEnabled()) return;
            MovementUtils.setSpeedWithStrafe(1f);
            if (mc.player.input.movementForward == 0.0f && mc.player.input.movementSideways == 0.0f) {
                return;
            }

            int collisions = 0;
            Box box = mc.player.getBoundingBox().expand(1.0);

            for (Entity entity : mc.world.getEntities()) {
                Box entityBox = entity.getBoundingBox();

                if (canCauseSpeed(entity) && box.intersects(entityBox)) {
                    collisions++;
                }
            }

            // Grim gives 0.08 leniency per entity which is customizable by speed.
            double yaw = Math.toRadians(mc.player.getYaw());
            double boost = 0.08 * collisions;
            mc.player.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
        }
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.player && entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity);
    }

}
