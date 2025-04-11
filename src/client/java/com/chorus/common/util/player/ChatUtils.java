package com.chorus.common.util.player;

import com.chorus.api.system.render.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.apache.commons.codec.binary.Base32;

import java.awt.*;

public class ChatUtils {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    public static void addChatMessage(String message) {
        if (mc.player == null || mc.world == null) return;
        mc.inGameHud.getChatHud().addMessage(Text.literal(message));
    }
    public static void printCrashReason(String message) {
        String saltedData = message + "remnantpathosionrealmoofdyrk";

        Base32 base32 = new Base32();
        String encoded = base32.encodeToString(saltedData.getBytes());
        
        System.out.println("Remnant Security » " + encoded);
    }

    public static void sendFormattedMessage(String message) {
        if (mc.player == null || mc.world == null) return;

        MutableText arrow = Text.empty();
        String prefix = "Remnant » ";
        Color primaryColor = new Color(184, 112, 242);
        Color secondaryColor = Color.white;

        for (int i = 0; i < prefix.length(); i++) {
            int interpolatedColor = ColorUtils.interpolateColor(primaryColor, secondaryColor, 5, i * (prefix.length() * 3)).getRGB();
            TextColor textColor = TextColor.fromRgb(interpolatedColor);
            arrow.append(Text.literal(String.valueOf(prefix.charAt(i))).setStyle(
                    Style.EMPTY.withColor(textColor).withBold(true)
            ));
        }

        MutableText fullMessage = arrow.append(Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.WHITE)));
        mc.inGameHud.getChatHud().addMessage(fullMessage);
    }

}
