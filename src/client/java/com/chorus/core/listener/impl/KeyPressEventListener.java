/**
 * Created: 2/16/2025
 */

package com.chorus.core.listener.impl;

import cc.polymorphism.eventbus.RegisterEvent;
import com.chorus.api.module.Module;
import com.chorus.common.QuickImports;
import com.chorus.common.util.player.ChatUtils;
import com.chorus.common.util.player.input.InputUtils;
import com.chorus.core.listener.Listener;
import com.chorus.impl.events.input.KeyPressEvent;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

public class KeyPressEventListener implements Listener, QuickImports {

    @Setter
    private static Module moduleToBindTo;

    @RegisterEvent
    private void keyPressEventListener(KeyPressEvent event) {
        if (moduleToBindTo != null && event.getAction() == GLFW.GLFW_PRESS) {
            moduleToBindTo.setKey(event.getKey());
            ChatUtils.sendFormattedMessage("Bound " + moduleToBindTo.getName() + " to key: " + InputUtils.getKeyName(event.getKey()));
            moduleToBindTo = null;
        }
    }
}
