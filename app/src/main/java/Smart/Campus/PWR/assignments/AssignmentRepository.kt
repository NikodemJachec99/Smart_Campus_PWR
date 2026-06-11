package Smart.Campus.PWR.assignments

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.ui.state.AssignmentUi
import Smart.Campus.PWR.ui.state.SubmissionUi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AssignmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("europe-west1")
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())

    private fun formatTimestamp(timestamp: Any?): String = when (timestamp) {
        is Timestamp -> dateFormatter.format(Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong()))
        is Long -> if (timestamp > 0) dateFormatter.format(Instant.ofEpochMilli(timestamp)) else ""
        else -> ""
    }

    suspend fun createAssignment(
        tutor: AppUser,
        title: String,
        description: String,
        subject: String,
        courseId: String,
        courseName: String,
        dueDate: String,
        dueAtMillis: Long?
    ) {
        val normalizedTitle = title.trim()
        val normalizedSubject = subject.trim()
        val normalizedDescription = description.trim()
        val normalizedDueDate = dueDate.trim()

        require(normalizedTitle.isNotEmpty()) { "Title is required." }
        require(courseId.isNotEmpty()) { "Course is required." }

        val payload = mutableMapOf<String, Any>(
            "title" to normalizedTitle,
            "description" to normalizedDescription,
            "subject" to normalizedSubject,
            "courseId" to courseId,
            "courseName" to courseName,
            "dueDate" to normalizedDueDate,
            "tutorUid" to tutor.uid,
            "tutorDisplayName" to tutor.displayName,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (dueAtMillis != null) {
            payload["dueAt"] = Timestamp(java.util.Date(dueAtMillis))
        }

        firestore.collection("assignments").add(payload).await()
    }

    suspend fun deleteAssignment(assignmentId: String) {
        require(assignmentId.isNotEmpty()) { "Assignment ID is required." }
        functions.getHttpsCallable("deleteAssignment")
            .call(mapOf("assignmentId" to assignmentId))
            .await()
    }

    suspend fun loadAssignmentsForCourses(courseIds: List<String>): List<AssignmentUi> {
        if (courseIds.isEmpty()) return emptyList()
        val snapshot = firestore
            .collection("assignments")
            .whereIn("courseId", courseIds.take(30))
            .get()
            .await()
        return snapshot.documents.mapNotNull { mapAssignment(it) }
            .sortedByDescending { it.dueAtMillis ?: 0L }
    }

    fun listenAssignmentsForCourses(
        courseIds: List<String>,
        onUpdate: (List<AssignmentUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration? {
        if (courseIds.isEmpty()) {
            onUpdate(emptyList())
            return null
        }
        return firestore
            .collection("assignments")
            .whereIn("courseId", courseIds.take(30))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                } else {
                    onUpdate(
                        snapshot?.documents.orEmpty().mapNotNull { mapAssignment(it) }
                            .sortedByDescending { it.dueAtMillis ?: 0L }
                    )
                }
            }
    }

    private fun mapAssignment(doc: DocumentSnapshot): AssignmentUi? {
        val title = doc.getString("title") ?: return null
        return AssignmentUi(
            id = doc.id,
            title = title,
            description = doc.getString("description").orEmpty(),
            subject = doc.getString("subject").orEmpty(),
            courseId = doc.getString("courseId").orEmpty(),
            courseName = doc.getString("courseName").orEmpty(),
            dueDateLabel = doc.getString("dueDate").orEmpty(),
            dueAtMillis = (doc.get("dueAt") as? Timestamp)?.let { it.seconds * 1000 },
            tutorUid = doc.getString("tutorUid").orEmpty(),
            tutorDisplayName = doc.getString("tutorDisplayName").orEmpty(),
            createdAtLabel = formatTimestamp(doc.get("createdAt"))
        )
    }

    suspend fun uploadSubmission(
        student: AppUser,
        assignmentId: String,
        fileName: String,
        bytes: ByteArray
    ): SubmissionUi {
        require(assignmentId.isNotEmpty()) { "Assignment ID is required." }
        require(fileName.isNotEmpty()) { "File name is required." }
        require(bytes.size <= 10 * 1024 * 1024) { "File must be smaller than 10 MB." }

        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val storagePath = "submissions/$assignmentId/${student.uid}/$safeName"
        val ref = storage.reference.child(storagePath)
        ref.putBytes(bytes).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        val submissionRef = firestore
            .collection("assignments")
            .document(assignmentId)
            .collection("submissions")
            .document(student.uid)

        val payload = mapOf(
            "assignmentId" to assignmentId,
            "studentUid" to student.uid,
            "studentDisplayName" to student.displayName,
            "fileName" to safeName,
            "storagePath" to storagePath,
            "downloadUrl" to downloadUrl,
            "submittedAt" to FieldValue.serverTimestamp(),
            "status" to "submitted"
        )

        submissionRef.set(payload).await()

        return SubmissionUi(
            id = student.uid,
            assignmentId = assignmentId,
            studentUid = student.uid,
            studentDisplayName = student.displayName,
            fileName = safeName,
            downloadUrl = downloadUrl,
            submittedAtLabel = formatTimestamp(System.currentTimeMillis()),
            status = "submitted"
        )
    }

    suspend fun loadMySubmissions(studentUid: String, assignmentIds: List<String>): List<SubmissionUi> {
        return assignmentIds.distinct().flatMap { assignmentId ->
            val snapshot = firestore
                .collection("assignments")
                .document(assignmentId)
                .collection("submissions")
                .whereEqualTo("studentUid", studentUid)
                .get()
                .await()

            snapshot.documents.mapNotNull { mapSubmission(it) }
        }
    }

    fun listenMySubmissions(
        studentUid: String,
        assignmentIds: List<String>,
        onUpdate: (List<SubmissionUi>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration? {
        val uniqueAssignmentIds = assignmentIds.distinct().filter { it.isNotBlank() }
        if (uniqueAssignmentIds.isEmpty()) {
            onUpdate(emptyList())
            return null
        }

        val registrations = mutableListOf<ListenerRegistration>()
        val submissionsByAssignment = mutableMapOf<String, SubmissionUi>()
        val pendingInitialSnapshots = uniqueAssignmentIds.toMutableSet()
        val lock = Any()

        uniqueAssignmentIds.forEach { assignmentId ->
            val registration = firestore
                .collection("assignments")
                .document(assignmentId)
                .collection("submissions")
                .whereEqualTo("studentUid", studentUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                        return@addSnapshotListener
                    }

                    val submission = snapshot
                        ?.documents
                        .orEmpty()
                        .mapNotNull { mapSubmission(it) }
                        .firstOrNull()

                    synchronized(lock) {
                        if (submission == null) {
                            submissionsByAssignment.remove(assignmentId)
                        } else {
                            submissionsByAssignment[assignmentId] = submission
                        }
                        pendingInitialSnapshots.remove(assignmentId)
                        if (pendingInitialSnapshots.isEmpty()) {
                            onUpdate(submissionsByAssignment.values.toList())
                        }
                    }
                }
            registrations += registration
        }

        return ListenerRegistration {
            registrations.forEach { it.remove() }
        }
    }

    suspend fun loadSubmissionsForAssignment(assignmentId: String): List<SubmissionUi> {
        require(assignmentId.isNotEmpty()) { "Assignment ID is required." }
        val snapshot = firestore
            .collection("assignments")
            .document(assignmentId)
            .collection("submissions")
            .get()
            .await()
        return snapshot.documents.mapNotNull { mapSubmission(it) }
    }

    private fun mapSubmission(doc: DocumentSnapshot): SubmissionUi? {
        val assignmentId = doc.getString("assignmentId") ?: return null
        return SubmissionUi(
            id = doc.id,
            assignmentId = assignmentId,
            studentUid = doc.getString("studentUid").orEmpty(),
            studentDisplayName = doc.getString("studentDisplayName").orEmpty(),
            fileName = doc.getString("fileName").orEmpty(),
            downloadUrl = doc.getString("downloadUrl").orEmpty(),
            submittedAtLabel = formatTimestamp(doc.get("submittedAt")),
            status = doc.getString("status") ?: "submitted"
        )
    }
}
