/**

 * Created: 12/10/2024

 */

package com.chorus.impl.modules.combat;


import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.RangeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.TimerUtils;
import com.chorus.impl.events.network.PacketSendEvent;
import com.chorus.impl.events.player.AttackEvent;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(

        name = "SprintReset",

        description = "Resets Sprint To Deal More Knock-back",

        category = ModuleCategory.COMBAT

)

public class SprintReset extends BaseModule implements QuickImports {


    private final SettingCategory general = new SettingCategory("Attack Settings");

    private final ModeSetting sprintMode = new ModeSetting(general, "Reset Mode", "Select the reset type", "W-tap", "W-Tap", "S-Tap", "No Stop", "Shift");

    private final RangeSetting<Integer> delay = new RangeSetting<>(general, "Delay", "Adjust reset delay", 0, 1000, 50, 50);
    private final TimerUtils waitTimer = new TimerUtils();
    @RegisterEvent

    private void AttackEventListener(AttackEvent event) {

        if (event.getMode().equals(AttackEvent.Mode.PRE)) {

            if (mc.player == null || mc.world == null) return;

            if (waitTimer.hasReached(delay.getValueMax())) {

                if (mc.crosshairTarget instanceof EntityHitResult hitResult) {

                    if (hitResult.getEntity() instanceof PlayerEntity player) {


                        if (player.hurtTime == 0) {

                            waitTimer.reset();

                        }

                    }

                }

            }

        }

    }
    public boolean reset = false;

    public boolean isSprinting = false;
    @RegisterEvent

    private void PacketSendListener(PacketSendEvent event) {

        if (event.getMode().equals(PacketSendEvent.Mode.PRE)) {

            if (mc.player == null || mc.world == null) return;

            if (event.getPacket() instanceof ClientCommandC2SPacket clientCommandC2SPacket && clientCommandC2SPacket.getEntityId() == mc.player.getId()) {

                isSprinting = clientCommandC2SPacket.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING;

            }

        }

    }
    @RegisterEvent

    private void TickEventListener(TickEvent event) {

        if (event.getMode().equals(TickEvent.Mode.PRE)) {

            if (mc.player == null || mc.world == null) return;


            if (waitTimer.hasReached(delay.getValueMax())) {

                if (reset) {

                    switch (sprintMode.getValue()) {

                        case "W-Tap":

                            if (com.chorus.common.util.player.input.InputUtils.keyDown(GLFW.GLFW_KEY_W)) {

                                mc.options.forwardKey.setPressed(true);

                            }

                            break;

                        case "S-Tap":

                            mc.options.backKey.setPressed(false);

                            break;

                        case "Shift":

                            mc.options.sneakKey.setPressed(false);

                            break;

                    }

                    reset = false;

                }

            } else {

                switch (sprintMode.getValue()) {

                    case "W-Tap":

                        mc.options.forwardKey.setPressed(false);

                        break;

                    case "S-Tap":

                        mc.options.backKey.setPressed(true);

                        break;

                    case "Shift":

                        mc.options.sneakKey.setPressed(true);

                        break;

                    case "No Stop":

                        if (mc.options.forwardKey.isPressed()) {

                            if (isSprinting)

                                mc.getNetworkHandler().getConnection().send(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

                            mc.getNetworkHandler().getConnection().send(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

                        }

                        break;

                }

                reset = true;

            }

        }

    }


    public SprintReset() {

        getSettingRepository().registerSettings(general, sprintMode, delay);

    }


}