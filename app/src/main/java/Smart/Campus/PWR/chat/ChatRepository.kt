package Smart.Campus.PWR.chat

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.ui.state.ConversationUi
import Smart.Campus.PWR.ui.state.MessageUi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ChatIds {
    fun directId(uidA: String, uidB: String): String {
        val (lo, hi) = listOf(uidA, uidB).sorted()
        return "dm_${lo}_${hi}"
    }
}

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val timeFmt = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault())

    private fun label(timestamp: Any?): String =
        (timestamp as? Timestamp)?.let { timeFmt.format(Instant.ofEpochSecond(it.seconds)) } ?: ""

    private fun millis(timestamp: Any?): Long = (timestamp as? Timestamp)?.let { it.seconds * 1000 } ?: 0L

    suspend fun openOrCreateDirect(me: AppUser, otherUid: String, otherName: String): String {
        val id = ChatIds.directId(me.uid, otherUid)
        val ref = firestore.collection("conversations").document(id)
        if (!ref.get().await().exists()) {
            ref.set(
                mapOf(
                    "type" to "direct",
                    "participants" to listOf(me.uid, otherUid),
                    "participantNames" to mapOf(me.uid to me.displayName, otherUid to otherName),
                    "lastMessageText" to "",
                    "lastMessageSenderName" to "",
                    "lastMessageAt" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        return id
    }

    suspend fun sendDirectMessage(conversationId: String, sender: AppUser, text: String) {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty() && trimmed.length <= 2000) { "Message must be 1-2000 characters." }
        firestore.collection("conversations").document(conversationId).collection("messages").add(
            mapOf(
                "senderUid" to sender.uid,
                "senderName" to sender.displayName,
                "text" to trimmed,
                "sentAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun sendCourseMessage(courseId: String, sender: AppUser, text: String, isAnnouncement: Boolean) {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty() && trimmed.length <= 2000) { "Message must be 1-2000 characters." }
        firestore.collection("courses").document(courseId).collection("messages").add(
            mapOf(
                "senderUid" to sender.uid,
                "senderName" to sender.displayName,
                "text" to trimmed,
                "isAnnouncement" to isAnnouncement,
                "sentAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun listenDirectConversations(
        uid: String,
        onUpdate: (List<ConversationUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        firestore.collection("conversations")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUpdate(
                        snapshot?.documents.orEmpty().map { mapConversation(it, uid) }
                            .sortedByDescending { it.lastMessageAtMillis }
                    )
                }
            }

    @Suppress("UNCHECKED_CAST")
    private fun mapConversation(doc: DocumentSnapshot, uid: String): ConversationUi {
        val names = (doc.get("participantNames") as? Map<String, String>).orEmpty()
        val otherUid = (doc.get("participants") as? List<String>).orEmpty().firstOrNull { it != uid }.orEmpty()
        return ConversationUi(
            id = doc.id,
            title = names[otherUid] ?: "Conversation",
            lastMessageText = doc.getString("lastMessageText").orEmpty(),
            lastMessageAtLabel = label(doc.get("lastMessageAt")),
            lastMessageAtMillis = millis(doc.get("lastMessageAt")),
            otherUid = otherUid
        )
    }

    fun listenDirectMessages(
        conversationId: String,
        onUpdate: (List<MessageUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        messagesListener(
            firestore.collection("conversations").document(conversationId).collection("messages"),
            onUpdate,
            onError
        )

    fun listenCourseMessages(
        courseId: String,
        onUpdate: (List<MessageUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        messagesListener(
            firestore.collection("courses").document(courseId).collection("messages"),
            onUpdate,
            onError
        )

    private fun messagesListener(
        collection: CollectionReference,
        onUpdate: (List<MessageUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        collection.orderBy("sentAt", Query.Direction.ASCENDING).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
            } else {
                onUpdate(
                    snapshot?.documents.orEmpty().map { doc ->
                        MessageUi(
                            id = doc.id,
                            senderUid = doc.getString("senderUid").orEmpty(),
                            senderName = doc.getString("senderName").orEmpty(),
                            text = doc.getString("text").orEmpty(),
                            isAnnouncement = doc.getBoolean("isAnnouncement") ?: false,
                            sentAtLabel = label(doc.get("sentAt"))
                        )
                    }
                )
            }
        }
}
