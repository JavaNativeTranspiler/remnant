
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
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

@ModuleInfo(
        name = "AntiResourcePack",
        description = "Blocks Forced Resource Packs",
        category = ModuleCategory.OTHER
)

public class AutoDisable extends BaseModule implements QuickImports {


    @RegisterEvent
    private void PacketReceiveEventListener(PacketReceiveEvent event) {
        if (event.getMode().equals(PacketReceiveEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            if (event.getPacket() instanceof ResourcePackSendS2CPacket resourcePackSendS2CPacket) {
                event.cancel();
                var uuid = mc.player.getUuid();
                mc.getNetworkHandler().getConnection().send(new ResourcePackStatusC2SPacket(uuid, ResourcePackStatusC2SPacket.Status.DOWNLOADED));
                mc.getNetworkHandler().getConnection().send(new ResourcePackStatusC2SPacket(uuid, ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            }
        }
    }
}
