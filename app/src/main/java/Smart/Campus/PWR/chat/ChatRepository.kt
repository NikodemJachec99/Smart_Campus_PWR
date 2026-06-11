package Smart.Campus.PWR.chat

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object ChatIds {
    fun directId(uidA: String, uidB: String): String {
        val (lo, hi) = listOf(uidA, uidB).sorted()
        return "dm_${lo}_${hi}"
    }
}

data class ChatConversation(
    val id: String,
    val title: String,
    val lastMessageText: String,
    val lastMessageAtLabel: String,
    val lastMessageAtMillis: Long,
    val lastMessageSenderName: String = "",
    val lastMessageSenderUid: String = "",
    val currentUserReadAtMillis: Long = 0L,
    val otherReadAtMillis: Long = 0L,
    val unreadCount: Int = 0,
    val otherUid: String = ""
)

data class ChatMessage(
    val id: String,
    val senderUid: String,
    val senderName: String,
    val text: String,
    val isAnnouncement: Boolean,
    val sentAtLabel: String,
    val sentAtMillis: Long = 0L,
    val clientMessageId: String = "",
    val messageType: String = "text",
    val attachment: ChatAttachment? = null,
    val replyTo: ChatReplyPreview? = null,
    val editedAtLabel: String = "",
    val deletedAtLabel: String = "",
    val reactionCounts: Map<String, Int> = emptyMap(),
    val myReaction: String = ""
)

data class ChatAttachment(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val storagePath: String,
    val downloadUrl: String,
    val width: Int? = null,
    val height: Int? = null
)

data class ChatReplyPreview(
    val messageId: String,
    val senderName: String,
    val textPreview: String,
    val attachmentName: String = ""
)

data class OutgoingChatAttachment(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val storagePath: String,
    val downloadUrl: String,
    val width: Int? = null,
    val height: Int? = null
)

data class ChatThreadTarget(
    val kind: String,
    val id: String
) {
    val isCourse: Boolean
        get() = kind == KIND_COURSE

    companion object {
        const val KIND_DIRECT = "direct"
        const val KIND_COURSE = "course"

        fun direct(conversationId: String): ChatThreadTarget = ChatThreadTarget(KIND_DIRECT, conversationId)
        fun course(courseId: String): ChatThreadTarget = ChatThreadTarget(KIND_COURSE, courseId)
    }
}

object ChatReadState {
    fun unreadCount(
        viewerUid: String,
        lastMessageSenderUid: String,
        lastMessageAtMillis: Long,
        readAtMillis: Long
    ): Int {
        return if (
            viewerUid.isNotBlank() &&
            lastMessageSenderUid.isNotBlank() &&
            lastMessageSenderUid != viewerUid &&
            lastMessageAtMillis > readAtMillis
        ) 1 else 0
    }

    fun isReadByOther(sentAtMillis: Long, otherReadAtMillis: Long): Boolean =
        sentAtMillis > 0L && otherReadAtMillis >= sentAtMillis
}

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val timeFmt = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault())

    private fun label(timestamp: Any?): String =
        (timestamp as? Timestamp)?.let { timeFmt.format(Instant.ofEpochSecond(it.seconds)) } ?: ""

    private fun millis(timestamp: Any?): Long = when (timestamp) {
        is Timestamp -> timestamp.seconds * 1000 + timestamp.nanoseconds / 1_000_000
        is Long -> timestamp
        is Int -> timestamp.toLong()
        else -> 0L
    }

    private fun readAtMillis(data: Map<*, *>, uid: String): Long =
        millis((data["lastReadAtBy"] as? Map<*, *>)?.get(uid))

    private fun messagesCollection(target: ChatThreadTarget): CollectionReference =
        if (target.isCourse) {
            firestore.collection("courses").document(target.id).collection("messages")
        } else {
            firestore.collection("conversations").document(target.id).collection("messages")
        }

    private fun parentCollection(target: ChatThreadTarget) =
        if (target.isCourse) firestore.collection("courses") else firestore.collection("conversations")

    private fun parentDocument(target: ChatThreadTarget) = parentCollection(target).document(target.id)

    private fun typingField(uid: String) = FieldPath.of("typing", uid)

    private fun messageType(text: String, attachment: OutgoingChatAttachment?): String = when {
        attachment == null -> "text"
        attachment.mimeType.startsWith("image/") -> "image"
        else -> "file"
    }

    suspend fun openOrCreateDirect(meUid: String, meDisplayName: String, otherUid: String, otherName: String): String {
        val id = ChatIds.directId(meUid, otherUid)
        val ref = firestore.collection("conversations").document(id)
        try {
            if (ref.get().await().exists()) return id
        } catch (error: FirebaseFirestoreException) {
            if (error.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) throw error
        }
        ref.set(
            mapOf(
                "type" to "direct",
                "participants" to listOf(meUid, otherUid),
                "participantNames" to mapOf(meUid to meDisplayName, otherUid to otherName),
                "lastReadAtBy" to mapOf(meUid to FieldValue.serverTimestamp())
            ),
            SetOptions.merge()
        ).await()
        return id
    }

    suspend fun sendMessage(
        target: ChatThreadTarget,
        senderUid: String,
        senderName: String,
        text: String,
        isAnnouncement: Boolean,
        clientMessageId: String,
        attachment: OutgoingChatAttachment? = null,
        replyTo: ChatReplyPreview? = null
    ) {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty() || attachment != null) { "Message or attachment is required." }
        require(trimmed.length <= 2000) { "Message must be 1-2000 characters." }
        val payload = mutableMapOf<String, Any>(
            "senderUid" to senderUid,
            "senderName" to senderName,
            "text" to trimmed,
            "isAnnouncement" to (target.isCourse && isAnnouncement),
            "clientMessageId" to clientMessageId,
            "messageType" to messageType(trimmed, attachment),
            "sentAt" to FieldValue.serverTimestamp()
        )
        attachment?.let {
            payload["attachment"] = mapOf(
                "fileName" to it.fileName,
                "mimeType" to it.mimeType,
                "sizeBytes" to it.sizeBytes,
                "storagePath" to it.storagePath,
                "downloadUrl" to it.downloadUrl,
                "width" to (it.width ?: 0),
                "height" to (it.height ?: 0)
            )
        }
        replyTo?.let {
            payload["replyTo"] = mapOf(
                "messageId" to it.messageId,
                "senderName" to it.senderName,
                "textPreview" to it.textPreview,
                "attachmentName" to it.attachmentName
            )
        }
        messagesCollection(target).add(payload).await()
    }

    suspend fun sendDirectMessage(
        conversationId: String,
        senderUid: String,
        senderName: String,
        text: String,
        clientMessageId: String
    ) {
        sendMessage(
            target = ChatThreadTarget.direct(conversationId),
            senderUid = senderUid,
            senderName = senderName,
            text = text,
            isAnnouncement = false,
            clientMessageId = clientMessageId
        )
    }

    suspend fun sendCourseMessage(
        courseId: String,
        senderUid: String,
        senderName: String,
        text: String,
        isAnnouncement: Boolean,
        clientMessageId: String
    ) {
        sendMessage(
            target = ChatThreadTarget.course(courseId),
            senderUid = senderUid,
            senderName = senderName,
            text = text,
            isAnnouncement = isAnnouncement,
            clientMessageId = clientMessageId
        )
    }

    suspend fun markDirectRead(conversationId: String, uid: String) {
        if (conversationId.isBlank() || uid.isBlank()) return
        firestore.collection("conversations")
            .document(conversationId)
            .update(FieldPath.of("lastReadAtBy", uid), FieldValue.serverTimestamp())
            .await()
    }

    suspend fun uploadChatAttachment(
        target: ChatThreadTarget,
        senderUid: String,
        clientMessageId: String,
        fileName: String,
        mimeType: String,
        sizeBytes: Long,
        uri: Uri,
        onProgress: (Float) -> Unit
    ): OutgoingChatAttachment {
        AttachmentContract.validationError(fileName, mimeType, sizeBytes)?.let { error ->
            throw IllegalArgumentException(error)
        }
        val safeName = AttachmentContract.sanitizeFileName(fileName)
        val storagePath = if (target.isCourse) {
            "chat/courses/${target.id}/$senderUid/$clientMessageId/$safeName"
        } else {
            "chat/direct/${target.id}/$senderUid/$clientMessageId/$safeName"
        }
        val reference = storage.reference.child(storagePath)
        val metadata = StorageMetadata.Builder()
            .setContentType(mimeType)
            .setCustomMetadata("senderUid", senderUid)
            .setCustomMetadata("clientMessageId", clientMessageId)
            .build()
        reference.putFile(uri, metadata)
            .addOnProgressListener { snapshot ->
                val total = snapshot.totalByteCount.takeIf { it > 0L } ?: sizeBytes
                onProgress((snapshot.bytesTransferred.toFloat() / total.toFloat()).coerceIn(0f, 1f))
            }
            .await()
        val downloadUrl = reference.downloadUrl.await().toString()
        onProgress(1f)
        return OutgoingChatAttachment(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            storagePath = storagePath,
            downloadUrl = downloadUrl
        )
    }

    fun listenLatestMessages(
        target: ChatThreadTarget,
        viewerUid: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit,
        limit: Long = 50
    ): ListenerRegistration =
        messagesCollection(target)
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUpdate(snapshot?.documents.orEmpty().map { doc -> mapMessage(doc, viewerUid) }.sortedBy { it.sentAtMillis })
                }
            }

    suspend fun loadOlderMessages(
        target: ChatThreadTarget,
        viewerUid: String,
        beforeMillis: Long,
        limit: Long = 50
    ): List<ChatMessage> {
        if (beforeMillis <= 0L) return emptyList()
        val snapshot = messagesCollection(target)
            .whereLessThan("sentAt", Timestamp(Date(beforeMillis)))
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        return snapshot.documents.map { mapMessage(it, viewerUid) }.sortedBy { it.sentAtMillis }
    }

    suspend fun setTyping(target: ChatThreadTarget, uid: String, displayName: String, isTyping: Boolean) {
        if (uid.isBlank()) return
        parentDocument(target)
            .set(
                mapOf(
                    "typing" to mapOf(
                        uid to mapOf(
                            "displayName" to displayName,
                            "isTyping" to isTyping,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                ),
                SetOptions.merge()
            )
            .await()
    }

    fun listenTyping(
        target: ChatThreadTarget,
        viewerUid: String,
        onUpdate: (String) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        parentDocument(target).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
            } else {
                val typing = snapshot?.get("typing") as? Map<*, *> ?: emptyMap<Any, Any>()
                val activeNames = typing.entries.mapNotNull { entry ->
                    val uid = entry.key as? String ?: return@mapNotNull null
                    val data = entry.value as? Map<*, *> ?: return@mapNotNull null
                    val updatedAt = millis(data["updatedAt"])
                    val fresh = updatedAt > 0L && System.currentTimeMillis() - updatedAt < 10_000L
                    val isTyping = data["isTyping"] as? Boolean ?: false
                    if (uid != viewerUid && isTyping && fresh) data["displayName"] as? String else null
                }.distinct()
                onUpdate(
                    when (activeNames.size) {
                        0 -> ""
                        1 -> "${activeNames.first()} is typing"
                        else -> "${activeNames.size} people are typing"
                    }
                )
            }
        }

    suspend fun setReaction(target: ChatThreadTarget, messageId: String, uid: String, reactionKey: String) {
        if (messageId.isBlank() || uid.isBlank()) return
        val messageRef = messagesCollection(target).document(messageId)
        val reactionRef = messageRef.collection("reactions").document(uid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(messageRef)
            val reactionBy = (snapshot.get("reactionBy") as? Map<*, *>)
                .orEmpty()
                .mapNotNull { (key, value) -> (key as? String)?.let { it to (value as? String).orEmpty() } }
                .toMap()
                .toMutableMap()
            val counts = (snapshot.get("reactionCounts") as? Map<*, *>)
                .orEmpty()
                .mapNotNull { (key, value) -> (key as? String)?.let { it to ((value as? Number)?.toInt() ?: 0) } }
                .toMap()
                .toMutableMap()
            val oldReaction = reactionBy[uid].orEmpty()
            if (oldReaction.isNotBlank()) counts[oldReaction] = ((counts[oldReaction] ?: 1) - 1).coerceAtLeast(0)
            if (reactionKey.isBlank() || oldReaction == reactionKey) {
                reactionBy.remove(uid)
                transaction.delete(reactionRef)
            } else {
                reactionBy[uid] = reactionKey
                counts[reactionKey] = (counts[reactionKey] ?: 0) + 1
                transaction.set(reactionRef, mapOf("reactionKey" to reactionKey, "createdAt" to FieldValue.serverTimestamp()))
            }
            transaction.update(messageRef, mapOf("reactionBy" to reactionBy, "reactionCounts" to counts.filterValues { it > 0 }))
        }.await()
    }

    suspend fun deleteOwnMessage(target: ChatThreadTarget, messageId: String, uid: String) {
        if (messageId.isBlank() || uid.isBlank()) return
        messagesCollection(target).document(messageId)
            .update(
                mapOf(
                    "text" to "",
                    "messageType" to "text",
                    "attachment" to FieldValue.delete(),
                    "deletedAt" to FieldValue.serverTimestamp(),
                    "deletedBy" to uid
                )
            ).await()
    }

    suspend fun reportMessage(target: ChatThreadTarget, message: ChatMessage, reporterUid: String) {
        if (message.id.isBlank() || reporterUid.isBlank()) return
        firestore.collection("message_reports").add(
            mapOf(
                "threadKind" to target.kind,
                "threadId" to target.id,
                "messageId" to message.id,
                "messageSenderUid" to message.senderUid,
                "reporterUid" to reporterUid,
                "textPreview" to message.text.take(300),
                "attachmentName" to message.attachment?.fileName.orEmpty(),
                "status" to "open",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    fun listenDirectConversations(
        uid: String,
        onUpdate: (List<ChatConversation>) -> Unit,
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
    private fun mapConversation(doc: DocumentSnapshot, uid: String): ChatConversation {
        val data = doc.data.orEmpty()
        val names = (doc.get("participantNames") as? Map<String, String>).orEmpty()
        val otherUid = (doc.get("participants") as? List<String>).orEmpty().firstOrNull { it != uid }.orEmpty()
        val lastMessageAtMillis = millis(doc.get("lastMessageAt"))
        val lastMessageSenderUid = doc.getString("lastMessageSenderUid").orEmpty()
        val currentUserReadAtMillis = readAtMillis(data, uid)
        val otherReadAtMillis = readAtMillis(data, otherUid)
        return ChatConversation(
            id = doc.id,
            title = names[otherUid] ?: "Conversation",
            lastMessageText = doc.getString("lastMessageText").orEmpty(),
            lastMessageAtLabel = label(doc.get("lastMessageAt")),
            lastMessageAtMillis = lastMessageAtMillis,
            lastMessageSenderName = doc.getString("lastMessageSenderName").orEmpty(),
            lastMessageSenderUid = lastMessageSenderUid,
            currentUserReadAtMillis = currentUserReadAtMillis,
            otherReadAtMillis = otherReadAtMillis,
            unreadCount = ChatReadState.unreadCount(
                viewerUid = uid,
                lastMessageSenderUid = lastMessageSenderUid,
                lastMessageAtMillis = lastMessageAtMillis,
                readAtMillis = currentUserReadAtMillis
            ),
            otherUid = otherUid
        )
    }

    private fun mapAttachment(value: Any?): ChatAttachment? {
        val data = value as? Map<*, *> ?: return null
        val fileName = data["fileName"] as? String ?: return null
        val mimeType = data["mimeType"] as? String ?: "application/octet-stream"
        return ChatAttachment(
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = (data["sizeBytes"] as? Number)?.toLong() ?: 0L,
            storagePath = data["storagePath"] as? String ?: "",
            downloadUrl = data["downloadUrl"] as? String ?: "",
            width = (data["width"] as? Number)?.toInt()?.takeIf { it > 0 },
            height = (data["height"] as? Number)?.toInt()?.takeIf { it > 0 }
        )
    }

    private fun mapReply(value: Any?): ChatReplyPreview? {
        val data = value as? Map<*, *> ?: return null
        val messageId = data["messageId"] as? String ?: return null
        return ChatReplyPreview(
            messageId = messageId,
            senderName = data["senderName"] as? String ?: "",
            textPreview = data["textPreview"] as? String ?: "",
            attachmentName = data["attachmentName"] as? String ?: ""
        )
    }

    private fun mapReactionCounts(value: Any?): Map<String, Int> =
        (value as? Map<*, *>).orEmpty().mapNotNull { (key, count) ->
            (key as? String)?.let { it to ((count as? Number)?.toInt() ?: 0) }
        }.filter { it.second > 0 }.toMap()

    private fun mapMessage(doc: DocumentSnapshot, viewerUid: String): ChatMessage {
        val reactionBy = (doc.get("reactionBy") as? Map<*, *>).orEmpty()
        return ChatMessage(
            id = doc.id,
            senderUid = doc.getString("senderUid").orEmpty(),
            senderName = doc.getString("senderName").orEmpty(),
            text = doc.getString("text").orEmpty(),
            isAnnouncement = doc.getBoolean("isAnnouncement") ?: false,
            sentAtLabel = label(doc.get("sentAt")),
            sentAtMillis = millis(doc.get("sentAt")),
            clientMessageId = doc.getString("clientMessageId").orEmpty(),
            messageType = doc.getString("messageType").orEmpty().ifBlank { "text" },
            attachment = mapAttachment(doc.get("attachment")),
            replyTo = mapReply(doc.get("replyTo")),
            editedAtLabel = label(doc.get("editedAt")),
            deletedAtLabel = label(doc.get("deletedAt")),
            reactionCounts = mapReactionCounts(doc.get("reactionCounts")),
            myReaction = (reactionBy[viewerUid] as? String).orEmpty()
        )
    }

    fun listenDirectMessages(
        conversationId: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        listenLatestMessages(ChatThreadTarget.direct(conversationId), "", onUpdate, onError)

    fun listenCourseMessages(
        courseId: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        listenLatestMessages(ChatThreadTarget.course(courseId), "", onUpdate, onError)

    private fun messagesListener(
        collection: CollectionReference,
        onUpdate: (List<ChatMessage>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        collection.orderBy("sentAt", Query.Direction.ASCENDING).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
            } else {
                onUpdate(
                    snapshot?.documents.orEmpty().map { doc -> mapMessage(doc, "") }
                )
            }
        }
}
