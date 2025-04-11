
/**
 * Created: 12/10/2024
 */
package com.chorus.impl.modules.other;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.asm.accessors.ChatMessageC2SPacketAccessor;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.common.QuickImports;
import com.chorus.impl.events.network.PacketSendEvent;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

@ModuleInfo(
        name        = "ChatBypass",
        description = "Attempts To Bypass Chat Filters",
        category    = ModuleCategory.OTHER
)
public class ChatBypass extends BaseModule implements QuickImports {

    @RegisterEvent
    private void PacketSendEventListener(PacketSendEvent event) {
        if (event.getMode().equals(PacketSendEvent.Mode.PRE)) {
            if (event.getPacket() instanceof ChatMessageC2SPacket chat) {
                String message = chat.chatMessage();
                if (!message.startsWith("/") && !message.startsWith("!") && !message.startsWith(".")) {
                    String bypassed = bypassString(message);
                    ((ChatMessageC2SPacketAccessor) (Object) chat).setChatMessage(bypassed);
                }
            }
        }
    }

    public String bypassString(String text) {
        return text.replace("a", "\u00E1").replace("e", "\u00E9").replace("i", "\u00A1")
                .replace("o", "\u00F3").replace("u", "\u00FA").replace("y", "\u00FF")
                .replace("A", "\u00C1").replace("E", "\u00C9").replace("I", "\u00A1")
                .replace("O", "\u00D3").replace("U", "\u00DA").replace("Y", "\u00FF");
    }
}
