package Smart.Campus.PWR.notifications

import Smart.Campus.PWR.ui.state.NotificationUi
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationCountTest {
    @Test
    fun countsUnread() {
        val list = listOf(
            NotificationUi("1", "chat", "t", "b", emptyMap(), false, ""),
            NotificationUi("2", "chat", "t", "b", emptyMap(), true, ""),
            NotificationUi("3", "chat", "t", "b", emptyMap(), false, "")
        )
        assertEquals(2, NotificationUtil.unreadCount(list))
    }
}
