package Smart.Campus.PWR.course

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.chat.AttachmentContract
import Smart.Campus.PWR.chat.ChatReadState
import Smart.Campus.PWR.ui.state.CourseMaterialUi
import Smart.Campus.PWR.ui.state.CourseMemberUi
import Smart.Campus.PWR.ui.state.CourseUi
import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CoursePaths {
    fun courseIdsFromMemberPaths(paths: List<String>): List<String> =
        paths.mapNotNull { path ->
            path.split("/").let { parts -> if (parts.size >= 2 && parts[0] == "courses") parts[1] else null }
        }.distinct()
}

class CourseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).withZone(ZoneId.systemDefault())
    private val chatFmt = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault())

    private fun label(timestamp: Any?): String =
        (timestamp as? Timestamp)?.let { dateFmt.format(Instant.ofEpochSecond(it.seconds)) } ?: ""

    private fun chatLabel(timestamp: Any?): String =
        (timestamp as? Timestamp)?.let { chatFmt.format(Instant.ofEpochSecond(it.seconds)) } ?: ""

    private fun millis(timestamp: Any?): Long = when (timestamp) {
        is Timestamp -> timestamp.seconds * 1000 + timestamp.nanoseconds / 1_000_000
        is Long -> timestamp
        is Int -> timestamp.toLong()
        else -> 0L
    }

    private fun readAtMillis(data: Map<*, *>, uid: String): Long =
        millis((data["lastReadAtBy"] as? Map<*, *>)?.get(uid))

    suspend fun createCourse(tutor: AppUser, name: String, subject: String, description: String): String {
        require(name.trim().isNotEmpty()) { "Course name is required." }
        val ref = firestore.collection("courses").add(
            mapOf(
                "name" to name.trim(),
                "subject" to subject.trim(),
                "description" to description.trim(),
                "tutorUid" to tutor.uid,
                "tutorDisplayName" to tutor.displayName,
                "memberCount" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        return ref.id
    }

    suspend fun updateCourse(courseId: String, name: String, subject: String, description: String) {
        firestore.collection("courses").document(courseId).update(
            mapOf(
                "name" to name.trim(),
                "subject" to subject.trim(),
                "description" to description.trim()
            )
        ).await()
    }

    suspend fun deleteCourse(courseId: String) {
        firestore.collection("courses").document(courseId).delete().await()
    }

    suspend fun enroll(student: AppUser, courseId: String) {
        firestore.collection("courses").document(courseId)
            .collection("members").document(student.uid)
            .set(
                mapOf(
                    "studentUid" to student.uid,
                    "displayName" to student.displayName,
                    "enrolledAt" to FieldValue.serverTimestamp()
                )
            ).await()
    }

    suspend fun leave(uid: String, courseId: String) {
        firestore.collection("courses").document(courseId)
            .collection("members").document(uid).delete().await()
    }

    suspend fun markCourseRead(courseId: String, uid: String) {
        if (courseId.isBlank() || uid.isBlank()) return
        firestore.collection("courses")
            .document(courseId)
            .update("lastReadAtBy.$uid", FieldValue.serverTimestamp())
            .await()
    }

    suspend fun loadRoster(courseId: String): List<CourseMemberUi> {
        val snapshot = firestore.collection("courses").document(courseId)
            .collection("members").get().await()
        return snapshot.documents.map {
            CourseMemberUi(
                studentUid = it.getString("studentUid").orEmpty(),
                displayName = it.getString("displayName").orEmpty(),
                enrolledAtLabel = label(it.get("enrolledAt"))
            )
        }
    }

    /** Course ids the student is enrolled in (one-shot). */
    suspend fun myEnrolledCourseIds(uid: String): List<String> {
        val courses = firestore.collection("courses").get().await()
        return courses.documents.mapNotNull { course ->
            val memberSnapshot = course.reference
                .collection("members")
                .whereEqualTo("studentUid", uid)
                .limit(1)
                .get()
                .await()

            course.id.takeIf { !memberSnapshot.isEmpty }
        }
    }

    /** Course ids the tutor owns (one-shot). */
    suspend fun myOwnedCourseIds(uid: String): List<String> {
        val snapshot = firestore.collection("courses")
            .whereEqualTo("tutorUid", uid).get().await()
        return snapshot.documents.map { it.id }
    }

    fun listenCourseCatalog(
        uid: String,
        onUpdate: (List<CourseUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        firestore.collection("courses").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUpdate(snapshot?.documents.orEmpty().mapNotNull { mapCourse(it, uid) })
                }
            }

    private fun materialsCollection(courseId: String) =
        firestore.collection("courses").document(courseId).collection("materials")

    fun listenCourseMaterials(
        courseId: String,
        onUpdate: (List<CourseMaterialUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        materialsCollection(courseId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUpdate(snapshot?.documents.orEmpty().mapNotNull { mapMaterial(it) })
                }
            }

    suspend fun uploadCourseMaterial(
        courseId: String,
        uploader: AppUser,
        fileName: String,
        mimeType: String,
        sizeBytes: Long,
        uri: Uri,
        onProgress: (Float) -> Unit
    ) {
        AttachmentContract.validationError(fileName, mimeType, sizeBytes)?.let { error ->
            throw IllegalArgumentException(error)
        }
        val materialRef = materialsCollection(courseId).document()
        val safeName = AttachmentContract.sanitizeFileName(fileName)
        val storagePath = "course_materials/$courseId/${uploader.uid}/${materialRef.id}/$safeName"
        val reference = storage.reference.child(storagePath)
        val metadata = StorageMetadata.Builder()
            .setContentType(mimeType)
            .setCustomMetadata("uploaderUid", uploader.uid)
            .build()
        reference.putFile(uri, metadata)
            .addOnProgressListener { snapshot ->
                val total = snapshot.totalByteCount.takeIf { it > 0L } ?: sizeBytes
                onProgress((snapshot.bytesTransferred.toFloat() / total.toFloat()).coerceIn(0f, 1f))
            }
            .await()
        val downloadUrl = reference.downloadUrl.await().toString()
        onProgress(1f)
        materialRef.set(
            mapOf(
                "fileName" to fileName,
                "mimeType" to mimeType,
                "sizeBytes" to sizeBytes,
                "storagePath" to storagePath,
                "downloadUrl" to downloadUrl,
                "uploaderUid" to uploader.uid,
                "uploaderName" to uploader.displayName,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun deleteCourseMaterial(courseId: String, material: CourseMaterialUi) {
        materialsCollection(courseId).document(material.id).delete().await()
        if (material.storagePath.isNotBlank()) {
            runCatching { storage.reference.child(material.storagePath).delete().await() }
        }
    }

    private fun mapMaterial(doc: DocumentSnapshot): CourseMaterialUi? {
        val fileName = doc.getString("fileName") ?: return null
        return CourseMaterialUi(
            id = doc.id,
            fileName = fileName,
            mimeType = doc.getString("mimeType").orEmpty().ifBlank { "application/octet-stream" },
            sizeBytes = doc.getLong("sizeBytes") ?: 0L,
            storagePath = doc.getString("storagePath").orEmpty(),
            downloadUrl = doc.getString("downloadUrl").orEmpty(),
            uploaderUid = doc.getString("uploaderUid").orEmpty(),
            uploaderName = doc.getString("uploaderName").orEmpty(),
            createdAtLabel = chatLabel(doc.get("createdAt")),
            createdAtMillis = millis(doc.get("createdAt"))
        )
    }

    private fun mapCourse(doc: DocumentSnapshot, uid: String): CourseUi? {
        val name = doc.getString("name") ?: return null
        val data = doc.data.orEmpty()
        val lastMessageAtMillis = millis(doc.get("lastMessageAt"))
        val lastMessageSenderUid = doc.getString("lastMessageSenderUid").orEmpty()
        val currentUserReadAtMillis = readAtMillis(data, uid)
        return CourseUi(
            id = doc.id,
            name = name,
            subject = doc.getString("subject").orEmpty(),
            description = doc.getString("description").orEmpty(),
            tutorUid = doc.getString("tutorUid").orEmpty(),
            tutorDisplayName = doc.getString("tutorDisplayName").orEmpty(),
            memberCount = (doc.getLong("memberCount") ?: 0L).toInt(),
            lastMessageText = doc.getString("lastMessageText").orEmpty(),
            lastMessageAtLabel = chatLabel(doc.get("lastMessageAt")),
            lastMessageAtMillis = lastMessageAtMillis,
            lastMessageSenderName = doc.getString("lastMessageSenderName").orEmpty(),
            lastMessageSenderUid = lastMessageSenderUid,
            currentUserReadAtMillis = currentUserReadAtMillis,
            unreadCount = ChatReadState.unreadCount(
                viewerUid = uid,
                lastMessageSenderUid = lastMessageSenderUid,
                lastMessageAtMillis = lastMessageAtMillis,
                readAtMillis = currentUserReadAtMillis
            ),
            isOwner = doc.getString("tutorUid") == uid
        )
    }
}
