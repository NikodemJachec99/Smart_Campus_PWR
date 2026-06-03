package Smart.Campus.PWR.notifications

import Smart.Campus.PWR.ui.state.NotificationUi

object NotificationUtil {
    fun unreadCount(list: List<NotificationUi>): Int = list.count { !it.read }
}
