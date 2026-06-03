package Smart.Campus.PWR.notifications

import Smart.Campus.PWR.ui.state.NotificationUi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val fmt = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault())

    fun listenMine(
        uid: String,
        onUpdate: (List<NotificationUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        firestore.collection("notifications")
            .whereEqualTo("recipientUid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUpdate(snapshot?.documents.orEmpty().map { doc ->
                        @Suppress("UNCHECKED_CAST")
                        NotificationUi(
                            id = doc.id,
                            type = doc.getString("type").orEmpty(),
                            title = doc.getString("title").orEmpty(),
                            body = doc.getString("body").orEmpty(),
                            data = (doc.get("data") as? Map<String, String>).orEmpty(),
                            read = doc.getBoolean("read") ?: false,
                            createdAtLabel = (doc.get("createdAt") as? Timestamp)
                                ?.let { fmt.format(Instant.ofEpochSecond(it.seconds)) } ?: ""
                        )
                    })
                }
            }

    suspend fun markRead(id: String) {
        firestore.collection("notifications").document(id).update("read", true).await()
    }

    suspend fun markAllRead(ids: List<String>) {
        if (ids.isEmpty()) return
        val batch = firestore.batch()
        ids.forEach { batch.update(firestore.collection("notifications").document(it), "read", true) }
        batch.commit().await()
    }
}
