package com.example.subversionMavenIncrement.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

import javax.annotation.Nullable;

/**
 * 右下角通知气泡工具类
 *
 * @author 蚕豆的生活
 */
public class NotifyUtil {

    /**
     * 普通信息通知
     *
     * @param project IntelliJ 项目对象
     * @param content 通知内容
     */
    public static void notifyInfo(@Nullable Project project, String content) {
        sendNotify(content, project, NotificationType.INFORMATION);
    }

    /**
     * 警告信息通知
     *
     * @param project IntelliJ 项目对象
     * @param content 通知内容
     */
    public static void notifyWarn(@Nullable Project project, String content) {
        sendNotify(content, project, NotificationType.WARNING);
    }

    /**
     * 错误信息通知
     *
     * @param project IntelliJ 项目对象
     * @param content 通知内容
     */
    public static void notifyError(@Nullable Project project, String content) {
        sendNotify(content, project, NotificationType.ERROR);
    }

    /**
     * 普通信息通知
     *
     * @param project IntelliJ 项目对象
     * @param content 通知内容
     * @param type 通知类型
     */
    private static void sendNotify(String content, @Nullable Project project, NotificationType type) {
        NotificationGroupManager.getInstance().getNotificationGroup("Subversion Maven Increment")
                .createNotification(content, type).setTitle("Subversion maven increment")
                .notify(project);
    }

    private NotifyUtil() {}

}
