
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.movement;

import cc.polymorphism.eventbus.EventPriority;
import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.MovementUtils;
import com.chorus.impl.events.misc.WebSlowdownEvent;

@ModuleInfo(
        name = "WebSpeed",
        description = "Boosts Speed While In Cobwbes",
        category = ModuleCategory.MOVEMENT
)
public class WebSpeed extends BaseModule implements QuickImports {
    public ModeSetting mode = new ModeSetting("Boost Mode", "Choose which mode you want", "Legit", "Legit", "Set Speed", "Universal", "Full");
    private final NumberSetting<Double> speed = new NumberSetting<>("Speed", "How much to speed up", 0.2, 0.0, 1.0);

    @RegisterEvent(value = EventPriority.LOW)
    private void WebSlowdownEventListener(WebSlowdownEvent event) {
        if (event.getMode().equals(WebSlowdownEvent.Mode.PRE)) {
            if (mc.player == null || mc.world == null) return;
            switch (mode.getValue()) {
                case "Legit" -> {
                    if (!MovementUtils.hasMovementInput()) return;
                    if (mc.player.isOnGround()) {
                        mc.player.jump();
                    }
                }
                case "Set Speed" -> {
                    if (!MovementUtils.hasMovementInput()) return;
                    if (mc.options.jumpKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x, speed.getValue(), mc.player.getVelocity().z);
                    }
                    if (mc.options.sneakKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x, -speed.getValue(), mc.player.getVelocity().z);
                    }
                    MovementUtils.setSpeedWithStrafe(speed.getValue());
                }
                case "Universal" -> {
                    if (!MovementUtils.hasMovementInput()) return;
                    if (mc.player.isOnGround()) {
                        mc.player.jump();
                        mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
                    }
                }
                case "Full" -> event.setCancelled(true);
            }
        }
    }

    public WebSpeed() {
        speed.setRenderCondition(() -> mode.getValue().equals("Set Speed"));
        getSettingRepository().registerSettings(mode, speed);
    }
}
