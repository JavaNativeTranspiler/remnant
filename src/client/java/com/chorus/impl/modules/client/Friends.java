
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.client;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.repository.friend.Friend;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.impl.events.player.TickEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(
        name = "Friends",
        description = "Prevents Targeting Friends",
        category = ModuleCategory.CLIENT
)
public class Friends extends BaseModule implements QuickImports {
    boolean pressed = false;

    @RegisterEvent
    private void TickEventListener(TickEvent event) {
        if (event.getMode().equals(TickEvent.Mode.PRE)) {
            if (mc.world == null | mc.player == null) return;
            if (!InputUtils.mouseDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
                pressed = false;
                return;
            }
            HitResult hitResult = mc.crosshairTarget;
            if (pressed) return;
            if (hitResult instanceof EntityHitResult entityHitResult) {
                if (entityHitResult.getEntity() instanceof PlayerEntity player) {
                    pressed = true;

                    if (friendRepository.isFriend(player.getUuid())) {
                        friendRepository.removeFriend(player.getUuid());
                        Chorus.getInstance().getNotificationManager().addNotification("Removed", player.getNameForScoreboard(), 5000);
                    } else {
                        friendRepository.addFriend(new Friend(player.getUuid(), player.getNameForScoreboard()));
                        Chorus.getInstance().getNotificationManager().addNotification("Added", player.getNameForScoreboard(), 5000);
                    }
                }
            }
        }
    }
}
