package com.chorus.impl.modules.visual;

import cc.polymorphism.eventbus.RegisterEvent;
import chorus0.Chorus;
import com.chorus.api.module.BaseModule;
import com.chorus.api.module.ModuleCategory;
import com.chorus.api.module.ModuleInfo;
import com.chorus.api.module.setting.implement.BooleanSetting;
import com.chorus.api.module.setting.implement.ModeSetting;
import com.chorus.api.module.setting.implement.SettingCategory;
import com.chorus.api.system.notification.NotificationManager;
import com.chorus.api.system.render.Render2DEngine;
import com.chorus.api.system.render.font.FontAtlas;
import com.chorus.common.QuickImports;
import com.chorus.common.util.math.MathUtils;
import com.chorus.impl.events.render.Render2DEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

@ModuleInfo(
        name = "Notifications",
        description = "sigma",
        category = ModuleCategory.VISUAL)
public class Notifications extends BaseModule implements QuickImports {

    private final SettingCategory general = new SettingCategory("General");
    public final ModeSetting mode = new ModeSetting(general, "Mode", "Choose style", "Chorus", "Chorus", "Remnant");
    public final BooleanSetting enableNotifications = new BooleanSetting("Module Toggle", "Shows a notification when a module is toggled", false);

    @RegisterEvent
    private void render2DListener(Render2DEvent event) {
        DrawContext context = event.getContext();
        MatrixStack matrices = context.getMatrices();
        for (NotificationManager.Notification notification : notificationManager.getNotifications()) {
            switch (Chorus.getInstance().getModuleManager().getModule(Notifications.class).mode.getValue()) {
                case "Chorus":
                    notificationManager.setStartingPos(new float[]{mc.getWindow().getScaledWidth() + 500, 0});
                    renderAdjust(matrices, context, notification);
                    break;
                case "Remnant":
                    renderRemnant(matrices, context, notification);
                    break;
            }
        }
    }

    private void renderAdjust(MatrixStack matrices, DrawContext context, NotificationManager.Notification notification) {
        FontAtlas interBold = Chorus.getInstance().getFonts().getInterSemiBold();
        FontAtlas interMedium = Chorus.getInstance().getFonts().getInterMedium();
        var notificationProgress = notificationManager.getNotificationProgress();

        Color themeColor = new Color(184, 112, 242, 255);
        Color background = new Color(0, 0, 0, 100);

        float height = 27.5f, length = 40;
        float[] position = notificationProgress.getOrDefault(notification, new float[]{
                mc.getWindow().getScaledWidth() + 100f,
                mc.getWindow().getScaledHeight()
        });

        int notificationIndex = notificationManager.getNotifications().indexOf(notification);

        float fullWidth = Math.max(interBold.getWidth(notification.title(), 8f), interMedium.getWidth(notification.content())) + length;
        boolean isFinished = (System.currentTimeMillis() - notification.currentTimeMillis() > notification.time());
        float targetX = mc.getWindow().getScaledWidth() - fullWidth - 5;
        float smoothX = MathUtils.lerp(position[0], isFinished ? targetX + 500 : targetX, 3f / mc.getCurrentFps());

        float targetY = (mc.getWindow().getScaledHeight() / 1.07f) - (notificationIndex * (height + 5));
        float smoothY = MathUtils.lerp(position[1], isFinished ? targetY + 100 : targetY, 3f / mc.getCurrentFps());

        float padding = 3;

        Render2DEngine.drawGradientRect(matrices, smoothX, smoothY, fullWidth, 1, themeColor, themeColor.brighter().brighter());
        Render2DEngine.drawRect(matrices, smoothX, smoothY, fullWidth, height, background);
        interBold.renderWithShadow(matrices,
                notification.title(),
                smoothX + padding,
                smoothY + padding + 1,
                8f,
                themeColor.getRGB());
        interMedium.renderWithShadow(matrices,
                notification.content(),
                smoothX + padding,
                smoothY + padding + interBold.getLineHeight(8f) + 1,
                7f,
                new Color(197, 197, 197, 255).getRGB());
        if (smoothY > targetY + 50 && isFinished) {
            notificationManager.getNotificationProgress().remove(notification);
            notificationManager.getNotifications().remove(notification);
        } else {
            notificationManager.getNotificationProgress().put(notification, new float[]{smoothX, smoothY});
        }
    }

    private void renderRemnant(MatrixStack matrices, DrawContext context, NotificationManager.Notification notification) {
        FontAtlas poppins = Chorus.getInstance().getFonts().getPoppins();
        var notificationProgress = notificationManager.getNotificationProgress();

        float padding = 3;
        float height = poppins.getLineHeight(8f), length = 35;

        float[] position = notificationProgress.getOrDefault(notification, new float[]{
                mc.getWindow().getScaledWidth() / 2f,
                mc.getWindow().getScaledHeight() - 300
        });

        int notificationIndex = notificationManager.getNotifications().indexOf(notification);
        float fullWidth = Math.max(poppins.getWidth(notification.title() + " " + notification.content(), 8f), 8f);
        notificationManager.setStartingPos(new float[]{mc.getWindow().getScaledWidth() / 2f - fullWidth / 2, mc.getWindow().getScaledHeight() - 500});
        boolean isFinished = (System.currentTimeMillis() - notification.currentTimeMillis() > notification.time() / 5);
        float targetX = mc.getWindow().getScaledWidth() / 2f - fullWidth / 2;
        float smoothX = MathUtils.lerp(position[0], targetX, 3f / mc.getCurrentFps());

        float targetY = (50) - (notificationIndex * (height + 5));
        float smoothY = MathUtils.lerp(position[1], isFinished ? targetY + 300 : targetY, 3f / mc.getCurrentFps());


        Render2DEngine.drawBlurredRoundedRect(matrices, smoothX - padding, smoothY + (poppins.getLineHeight(8f) / 2), fullWidth + padding, height, 4, 8, new Color(255, 255, 255, 10));
        Render2DEngine.drawRoundedOutline(matrices, smoothX - padding, smoothY + (poppins.getLineHeight(8f) / 2), fullWidth + padding, height, 4, 1, new Color(200, 200, 200, 75));


        poppins.render(matrices,
                notification.title().toLowerCase(),
                smoothX,
                smoothY + (poppins.getLineHeight(8f) / 2),
                8f,
                Color.WHITE.getRGB());
        poppins.render(matrices,
                notification.content().toLowerCase(),
                smoothX + poppins.getWidth(notification.title().toLowerCase() + " ", 8f),
                smoothY + (poppins.getLineHeight(8f) / 2),
                8f,
                Color.GREEN.getRGB());
        if (smoothY > targetY + 50 && isFinished) {
            notificationManager.getNotificationProgress().remove(notification);
            notificationManager.getNotifications().remove(notification);
        } else {
            notificationManager.getNotificationProgress().put(notification, new float[]{smoothX, smoothY});
        }
    }

    public Notifications() {
        getSettingRepository().registerSettings(general, mode, enableNotifications);
    }
}