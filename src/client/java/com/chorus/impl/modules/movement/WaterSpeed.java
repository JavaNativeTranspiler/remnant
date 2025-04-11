package com.chorus.impl.modules.movement;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

@ModuleInfo(
        name        = "WaterSpeed",
        description = "Increases your speed while swimming",
        category    = ModuleCategory.MOVEMENT
)
public class WaterSpeed extends BaseModule implements QuickImports {
    private final ModeSetting mode = new ModeSetting("Mode", "WaterSpeed mode", "Safe", "Safe", "Fast");

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (!isEnabled() || mc.player == null) {
                return;
            }

            if (mc.player.isAlive() && mc.player.isSwimming()) {
                double speed = 1.045;

                if (mode.getValue().equals("Fast")) {
                    speed = 1.054;
                }

                double motionX = mc.player.getMovement().x * speed;
                double motionY = mc.player.getMovement().y;
                double motionZ = mc.player.getMovement().z * speed;

                mc.player.setVelocity(motionX, motionY, motionZ);

                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(WaterSpeed.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(WaterSpeed.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
        }
    }

    public WaterSpeed() {
        getSettingRepository().registerSettings(mode);
    }
}
