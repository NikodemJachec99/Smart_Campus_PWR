package Smart.Campus.PWR.tutoring

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.chat.AttachmentContract
import Smart.Campus.PWR.ui.state.CourseMaterialUi
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

/**
 * Files shared on a single lesson (booking), stored under
 * `bookings/{bookingId}/materials` + Storage `lesson_materials/...`.
 * Either booking participant (student or tutor) may upload and read; mirrors
 * the course-materials implementation in [Smart.Campus.PWR.course.CourseRepository].
 */
class LessonMaterialRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())

    private fun materialsCollection(bookingId: String) =
        firestore.collection("bookings").document(bookingId).collection("materials")

    fun listenLessonMaterials(
        bookingId: String,
        onUpdate: (List<CourseMaterialUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration =
        materialsCollection(bookingId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUpdate(snapshot?.documents.orEmpty().mapNotNull { mapMaterial(it) })
                }
            }

    suspend fun uploadLessonMaterial(
        bookingId: String,
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
        val materialRef = materialsCollection(bookingId).document()
        val safeName = AttachmentContract.sanitizeFileName(fileName)
        val storagePath = "lesson_materials/$bookingId/${uploader.uid}/${materialRef.id}/$safeName"
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

    suspend fun deleteLessonMaterial(bookingId: String, material: CourseMaterialUi) {
        materialsCollection(bookingId).document(material.id).delete().await()
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
            createdAtLabel = formatTimestamp(doc.get("createdAt")),
            createdAtMillis = (doc.get("createdAt") as? Timestamp)?.let { it.seconds * 1000 } ?: 0L
        )
    }

    private fun formatTimestamp(value: Any?): String = when (value) {
        is Timestamp -> dateFormatter.format(Instant.ofEpochSecond(value.seconds, value.nanoseconds.toLong()))
        else -> ""
    }
}
