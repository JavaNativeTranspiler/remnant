
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.network.PacketReceiveEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@ModuleInfo(
        name = "Atmosphere",
        description = "Changes World Time and Weather",
        category = ModuleCategory.VISUAL
)
public class Atmosphere extends BaseModule implements QuickImports {
    private final SettingCategory general = new SettingCategory("General");

    private final ModeSetting weather = new ModeSetting(general, "Weather", "Set World Weather", "Clear", "Clear", "Thunder", "Rain");
    private final NumberSetting<Double> time = new NumberSetting<>(general, "Time", "Change World Time", 1000.0, 0.0, 24000.0);

    @RegisterEvent
    private void PacketReceiveEventListener(PacketReceiveEvent event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket)
            event.setCancelled(true);
    }

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (mc.world == null) return;
        mc.world.setTime(time.getValue().longValue(), time.getValue().longValue(), true);
        if (weather.getValue().equals("Rain") || weather.getValue().equals("Thunderstorm")) {
            mc.world.setRainGradient(1f);
        }
        if (weather.getValue().equals("Clear")) {
            mc.world.setRainGradient(0f);
            mc.world.setThunderGradient(0f);
        }
        if (weather.getValue().equals("Thunderstorm")) {
            mc.world.setThunderGradient(1f);
        }
    }

    @Override
    protected void onModuleDisabled() {
        if (mc.world == null) return;
        mc.world.setRainGradient(0f);
        mc.world.setThunderGradient(0f);
    }

    public Atmosphere() {
        getSettingRepository().registerSettings(general, weather, time);
    }
}
