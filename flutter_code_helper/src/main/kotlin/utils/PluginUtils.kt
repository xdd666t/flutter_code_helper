package utils

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager

object PluginUtils {
    @JvmStatic
    fun showNotify(message: String?) {
        val notificationGroup = NotificationGroup("FlutterAssetsGenerator", NotificationDisplayType.BALLOON, true)
        ApplicationManager.getApplication().invokeLater {
            val notification = notificationGroup.createNotification(message!!, NotificationType.INFORMATION)
            Notifications.Bus.notify(notification)
        }
    }

    @JvmStatic
    fun showError(message: String?) {
        val notificationGroup = NotificationGroup("FlutterAssetsGenerator", NotificationDisplayType.BALLOON, true)
        ApplicationManager.getApplication().invokeLater {
            val notification = notificationGroup.createNotification(message!!, NotificationType.ERROR)
            Notifications.Bus.notify(notification)
        }
    }

    fun String.lowerCaseFirst(): String {
        return if (this.isEmpty()) {
            this
        } else {
            "${this[0].lowercase()}${this.subSequence(1, this.length)}"
        }
    }

    /**
     * 首字母大写
     */
    fun String.upperCaseFirst(): String {
        return if (this.isEmpty()) {
            this
        } else {
            "${this[0].uppercase()}${this.subSequence(1, this.length)}"
        }
    }
}