package Smart.Campus.PWR.ui.state

data class NotificationUi(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val data: Map<String, String>,
    val read: Boolean,
    val createdAtLabel: String
)
