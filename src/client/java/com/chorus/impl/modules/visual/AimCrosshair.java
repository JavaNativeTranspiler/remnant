
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.system.render.Render3DEngine;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.player.SilentRotationEvent;
import com.chorus.impl.events.render.Render3DEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

@ModuleInfo(
        name = "AimCrosshair",
        description = "Shows you where you are aiming",
        category = ModuleCategory.VISUAL
)
public class AimCrosshair extends BaseModule implements QuickImports {
    double yaw, pitch;
    private Vec3d pos;

    @RegisterEvent
    private void silentRotationEventEventListener(SilentRotationEvent event) {
        if (mc.player == null) return;
        yaw = event.getYaw() == mc.player.getYaw() ? 0 : event.getYaw();
        pitch = event.getPitch() == mc.player.getPitch() ? 0 : event.getPitch();
    }

    @RegisterEvent
    private void render3DEventEventListener(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.crosshairTarget == null) return;
        if (yaw == 0 || pitch == 0) return;
        if (pos == null) pos = mc.crosshairTarget.getPos();
        Render3DEngine.renderOutlinedShadedBox(pos.subtract(0, 0.05f, 0), (mc.crosshairTarget instanceof EntityHitResult) ? Color.red : Color.white, 50, event.getMatrices(), 0.05f, 0.1f);
        pos = pos.lerp(mc.crosshairTarget.getPos(), 40f / mc.getCurrentFps());
    }

}
