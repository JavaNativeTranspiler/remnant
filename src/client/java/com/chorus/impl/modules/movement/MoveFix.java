
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.movement;

import cc.polymorphism.eventbus.EventPriority;
import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.input.MovementInputEvent;
import com.chorus.impl.events.player.MoveFixEvent;
import com.chorus.impl.events.player.SilentRotationEvent;
import net.minecraft.util.math.MathHelper;

@ModuleInfo(
        name = "MoveFix",
        description = "Corrects Movement Based Off Rotations",
        category = ModuleCategory.MOVEMENT
)
public class MoveFix extends BaseModule implements QuickImports {
    private final BooleanSetting keepMovementDirection = new BooleanSetting("Keep Movement Direction", "Keeps Your Direction Of Movement", false);
    float yaw;

    @RegisterEvent(value = EventPriority.VERY_HIGH)
    private void silentRotationEventListener(SilentRotationEvent event) {
        if (mc.player == null || mc.world == null) return;
        yaw = event.getYaw();
    }

    @RegisterEvent(value = EventPriority.VERY_HIGH)
    private void moveFixEventListener(MoveFixEvent event) {
        if (mc.player == null || mc.world == null) return;
        event.setYaw(yaw);
    }

    @RegisterEvent(value = EventPriority.VERY_HIGH)
    private void movementInputEventEventListener(MovementInputEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (keepMovementDirection.getValue()) {
            fixMoveDirection(event, yaw);
        }
    }

    public static void fixMoveDirection(final MovementInputEvent event, final float targetYaw) {
        float forwardInput = (event.isPressingForward() ? 1.0f : 0.0f) - (event.isPressingBack() ? 1.0f : 0.0f);
        float sidewaysInput = (event.isPressingLeft() ? 1.0f : 0.0f) - (event.isPressingRight() ? 1.0f : 0.0f);

        float deltaYaw = mc.player.getYaw() - targetYaw;
        float rotatedSideways = sidewaysInput * MathHelper.cos(deltaYaw * 0.017453292f) - forwardInput * MathHelper.sin(deltaYaw * 0.017453292f);
        float rotatedForward = forwardInput * MathHelper.cos(deltaYaw * 0.017453292f) + sidewaysInput * MathHelper.sin(deltaYaw * 0.017453292f);

        event.setMovementForward(Math.round(rotatedForward));
        event.setMovementSideways(Math.round(rotatedSideways));
    }

    public MoveFix() {
        getSettingRepository().registerSetting(keepMovementDirection);
    }

}
