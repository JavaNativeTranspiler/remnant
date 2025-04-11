package com.chorus.impl.modules.combat;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.NumberSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.common.util.world.SimulatedPlayer;
import com.chorus.common.util.world.SocialManager;
import com.chorus.impl.events.network.PacketSendEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;

@ModuleInfo(name = "TickBase", description = "Tick gangster", category = ModuleCategory.COMBAT)
public class TickBase extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General Settings");
    private final NumberSetting<Integer> ticks = new NumberSetting<>(general, "Ticks", "Ticks", 5, 0, 40);
    private final RangeSetting<Float> range = new RangeSetting<>(general, "Range", "Sets Serverside Range Limits", 0f, 10f, 1.5f, 4.5f);
    private final NumberSetting<Float> delay = new NumberSetting<>(general, "Delay", "Delay in seconds", 5f, 0f, 25f);

    public int laggedTicks = 0;
    public TimerUtils timer = new TimerUtils();
    public boolean endedTick = false;
    @RegisterEvent
    private void PacketSendEvent(PacketSendEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof ClientTickEndC2SPacket) {
            endedTick = true;
        }
    }
    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        setSuffix(String.valueOf(ticks.getValue()));
        if (mc.player == null || mc.world == null) {
            laggedTicks = 0;
            return;
        }
        if (SocialManager.getTarget() == null) {
            laggedTicks = 0;
            return;
        }
        if (!timer.hasReached(delay.getValue() * 1000)) return;
        if (event.getMode().equals(TickEvent.Mode.PRE) && endedTick) endedTick = false;
        SimulatedPlayer player = new SimulatedPlayer(mc.player);
        player.setInput(
                mc.options.forwardKey.isPressed(),
                mc.options.backKey.isPressed(),
                mc.options.leftKey.isPressed(),
                mc.options.rightKey.isPressed(),
                mc.options.jumpKey.isPressed(), mc.player.isSprinting());
        for (int i = 0; i <= ticks.getValue(); i++) {
            player.tick();
        }
        double simulatedDistance = player.getPosition().distanceTo(SocialManager.getTarget().getPos());
        double distance = mc.player.distanceTo(SocialManager.getTarget());
        if (simulatedDistance > distance || simulatedDistance < range.getValueMin() || simulatedDistance > range.getValueMax()) {
            laggedTicks = 0;
            return;
        }
        if (laggedTicks >= ticks.getValue()) {
            if (endedTick) return;

            for (int i = 1; i <= laggedTicks; i++) {
                mc.player.tick();
                mc.getNetworkHandler().sendPacket(new ClientTickEndC2SPacket());
            }

            laggedTicks = 0;
            timer.reset();
            endedTick = false;
        } else {

            event.setCancelled(true);
            if (event.getMode().equals(TickEvent.Mode.PRE)) {
                laggedTicks++;
            }
        }
    }




    public TickBase() {
        getSettingRepository().registerSettings(general, ticks, range, delay);
    }
}