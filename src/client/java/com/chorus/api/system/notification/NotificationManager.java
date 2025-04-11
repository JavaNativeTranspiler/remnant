package com.chorus.api.system.notification;

import com.chorus.common.QuickImports;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements QuickImports {

    @Getter
    @Setter
    private CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    @Getter
    @Setter
    private Map<Notification, float[]> notificationProgress = new HashMap<>();

    @Getter
    @Setter
    public float[] startingPos = new float[]{0, 0};
    /**
     * @param title Title of Notification
     * @param content Content of notification
     * @param time time in milliseconds to show notification
     * @param startingPosition starting position of notification
     */
    public void addNotification(String title, String content, int time, float[] startingPosition) {
        notifications.add(new Notification(title, content, time, System.currentTimeMillis()));
        notificationProgress.put(new Notification(title, content, time, System.currentTimeMillis()), startingPosition);
    }
    /**
     * @param title Title of Notification
     * @param content Content of notification
     * @param time time in milliseconds to show notification
     */
    public void addNotification(String title, String content, int time) {
        notifications.add(new Notification(title, content, time, System.currentTimeMillis()));
        notificationProgress.put(new Notification(title, content, time, System.currentTimeMillis()), new float[]{
                startingPos[0],
                startingPos[1]
        });
    }

    public void clearAllNotifications() {
        notifications.clear();
        notificationProgress.clear();
    }


    public record Notification(String title, String content, int time, long currentTimeMillis) {
    }
}