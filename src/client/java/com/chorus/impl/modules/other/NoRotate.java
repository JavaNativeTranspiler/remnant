
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.network.PacketReceiveEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@ModuleInfo(
        name = "NoRotate",
        description = "Blocks Forced Rotations",
        category = ModuleCategory.OTHER
)

public class NoRotate extends BaseModule implements QuickImports {

    float yaw = 0.0f;
    float pitch = 0.0f;

    @RegisterEvent
    private void PacketReceiveEventListener(PacketReceiveEvent event) {
        if (event.getMode().equals(PacketReceiveEvent.Mode.PRE)) {
            if (mc.player == null) return;
            if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
                yaw = mc.player.getYaw();
                pitch = mc.player.getPitch();
            }
        }
    }

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.player == null) return;
            if (yaw != 0.0f || pitch != 0.0f) {
                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);
                yaw = 0.0f;
                pitch = 0.0f;
            }
        }
    }
}
