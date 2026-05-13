package Smart.Campus.PWR.tutoring

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.AuthMapping
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.AdminUserInspectorUi
import Smart.Campus.PWR.ui.state.DashboardUiState
import Smart.Campus.PWR.ui.state.LessonBookingUi
import Smart.Campus.PWR.ui.state.TutorAvailabilityUi
import Smart.Campus.PWR.ui.state.TutorReportUi
import Smart.Campus.PWR.ui.state.TutorReviewUi
import Smart.Campus.PWR.ui.state.TutorSummaryUi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TutoringRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun loadDashboard(user: AppUser): DashboardUiState {
        val myStudentBookings = if (user.hasRole(UserRole.STUDENT)) {
            getBookingsForStudent(user.uid)
        } else {
            emptyList()
        }

        val myTutorBookings = if (user.hasRole(UserRole.TUTOR)) {
            getBookingsForTutor(user.uid)
        } else {
            emptyList()
        }

        val myAvailability = if (user.hasRole(UserRole.TUTOR)) {
            getAvailabilityForTutor(user.uid)
        } else {
            emptyList()
        }

        val availableTutorSlots = if (user.hasRole(UserRole.STUDENT)) {
            getOpenAvailability().filter { it.tutorId != user.uid }
        } else {
            emptyList()
        }

        val tutors = if (user.hasRole(UserRole.STUDENT)) {
            getTutors()
        } else {
            emptyList()
        }

        val reviewsByMe = if (user.hasRole(UserRole.STUDENT)) {
            getReviewsWrittenByStudent(user.uid)
        } else {
            emptyList()
        }

        val reviewsForMe = if (user.hasRole(UserRole.TUTOR)) {
            getReviewsForTutor(user.uid)
        } else {
            emptyList()
        }

        val reportsByMe = if (user.hasRole(UserRole.STUDENT)) {
            getReportsWrittenByStudent(user.uid)
        } else {
            emptyList()
        }

        return DashboardUiState(
            myStudentBookings = myStudentBookings,
            myTutorBookings = myTutorBookings,
            myAvailability = myAvailability,
            availableTutorSlots = availableTutorSlots,
            tutors = tutors,
            reviewsByMe = reviewsByMe,
            reviewsForMe = reviewsForMe,
            reportsByMe = reportsByMe,
            adminReports = emptyList()
        )
    }

    suspend fun createAvailability(
        tutor: AppUser,
        subject: String,
        date: String,
        startHour: String,
        endHour: String
    ) {
        val normalizedSubject = subject.trim()
        val normalizedDate = date.trim()
        val normalizedStart = startHour.trim()
        val normalizedEnd = endHour.trim()

        require(normalizedSubject.isNotEmpty()) { "Subject is required." }
        require(normalizedDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Date must use YYYY-MM-DD format." }
        require(normalizedStart.isNotEmpty() && normalizedEnd.isNotEmpty()) { "Start and end hours are required." }

        val payload = mapOf(
            "tutorId" to tutor.uid,
            "tutorDisplayName" to tutor.displayName,
            "subject" to normalizedSubject,
            "date" to normalizedDate,
            "startHour" to normalizedStart,
            "endHour" to normalizedEnd,
            "isBooked" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("tutor_availability").add(payload).await()
    }

    suspend fun deleteAvailability(slotId: String, user: AppUser) {
        require(slotId.isNotEmpty()) { "Slot ID cannot be empty." }

        val docRef = firestore.collection("tutor_availability").document(slotId)
        val snapshot = docRef.get().await()

        if (snapshot.exists()) {
            val ownerId = snapshot.getString("tutorId")
            val isAdmin = user.hasRole(UserRole.ADMIN)

            if (!isAdmin && ownerId != user.uid) {
                throw IllegalStateException("Permission denied.")
            }
            if (!isAdmin && snapshot.getBoolean("isBooked") == true) {
                throw IllegalStateException("Tutors cannot delete a booked slot.")
            }

            docRef.delete().await()
        }
    }

    suspend fun updateAvailability(
        slotId: String,
        newSubject: String,
        newDate: String,
        newStartHour: String,
        newEndHour: String,
        user: AppUser
    ) {
        val normalizedSubject = newSubject.trim()
        val normalizedDate = newDate.trim()
        val normalizedStart = newStartHour.trim()
        val normalizedEnd = newEndHour.trim()

        require(normalizedSubject.isNotEmpty()) { "Subject is required." }
        require(normalizedDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Date must use YYYY-MM-DD format." }
        require(normalizedStart.isNotEmpty() && normalizedEnd.isNotEmpty()) { "Start and end hours are required." }

        val docRef = firestore.collection("tutor_availability").document(slotId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (!snapshot.exists()) {
                throw IllegalStateException("Slot not found.")
            }

            val ownerId = snapshot.getString("tutorId")
            val isAdmin = user.hasRole(UserRole.ADMIN)

            if (!isAdmin && ownerId != user.uid) {
                throw IllegalStateException("Permission denied.")
            }

            if (!isAdmin && snapshot.getBoolean("isBooked") == true) {
                throw IllegalStateException("Tutors cannot edit a booked slot.")
            }

            transaction.update(
                docRef,
                mapOf(
                    "subject" to normalizedSubject,
                    "date" to normalizedDate,
                    "startHour" to normalizedStart,
                    "endHour" to normalizedEnd
                )
            )
        }.await()
    }

    suspend fun getAllAvailability(): List<TutorAvailabilityUi> {
        return try {
            firestore.collection("tutor_availability")
                .get()
                .await()
                .documents
                .mapNotNull(::toAvailability)
                .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun bookAvailability(slotId: String, student: AppUser) {
        val slotRef = firestore.collection("tutor_availability").document(slotId)
        val bookingRef = firestore.collection("bookings").document()

        firestore.runTransaction { transaction ->
            val slotSnapshot = transaction.get(slotRef)
            if (!slotSnapshot.exists()) {
                throw IllegalStateException("This availability slot no longer exists.")
            }

            val isBooked = slotSnapshot.getBoolean("isBooked") == true
            if (isBooked) {
                throw IllegalStateException("This slot is already booked.")
            }

            val tutorId = slotSnapshot.getString("tutorId").orEmpty()
            val tutorDisplayName = slotSnapshot.getString("tutorDisplayName").orEmpty()
            val subject = slotSnapshot.getString("subject").orEmpty()
            val date = slotSnapshot.getString("date").orEmpty()


            val startHour = slotSnapshot.get("startHour")?.toString().orEmpty()
            val endHour = slotSnapshot.get("endHour")?.toString().orEmpty()

            transaction.update(
                slotRef,
                mapOf(
                    "isBooked" to true,
                    "bookedBy" to student.uid,
                    "bookedAt" to FieldValue.serverTimestamp()
                )
            )

            transaction.set(
                bookingRef,
                mapOf(
                    "availabilityId" to slotId,
                    "tutorId" to tutorId,
                    "tutorDisplayName" to tutorDisplayName,
                    "studentId" to student.uid,
                    "studentDisplayName" to student.displayName,
                    "subject" to subject,
                    "date" to date,
                    "startHour" to startHour,
                    "endHour" to endHour,
                    "status" to "booked",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
        }.await()
    }

    suspend fun cancelBooking(bookingId: String, slotId: String) {
        require(bookingId.isNotEmpty() && slotId.isNotEmpty()) { "IDs cannot be empty." }

        val bookingRef = firestore.collection("bookings").document(bookingId)
        val slotRef = firestore.collection("tutor_availability").document(slotId)

        firestore.runTransaction { transaction ->
            transaction.update(bookingRef, "status", "cancelled")

            transaction.update(
                slotRef,
                mapOf(
                    "isBooked" to false,
                    "bookedBy" to FieldValue.delete(),
                    "bookedAt" to FieldValue.delete()
                )
            )
        }.await()
    }

    suspend fun createReview(student: AppUser, tutorUid: String, rating: Int, comment: String) {
        val normalizedComment = comment.trim()
        val normalizedTutorUid = tutorUid.trim()

        require(normalizedTutorUid.isNotEmpty()) { "Select tutor." }
        require(rating in 1..5) { "Rating must be between 1 and 5." }
        require(normalizedComment.isNotEmpty()) { "Review comment is required." }

        val tutor = getUserByUid(normalizedTutorUid)
            ?: throw IllegalStateException("Tutor account does not exist.")

        firestore.collection("reviews").add(
            mapOf(
                "tutorId" to tutor.uid,
                "tutorDisplayName" to tutor.displayName,
                "studentId" to student.uid,
                "studentDisplayName" to student.displayName,
                "rating" to rating,
                "comment" to normalizedComment,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun createReport(student: AppUser, tutorUid: String, reason: String, details: String) {
        val normalizedTutorUid = tutorUid.trim()
        val normalizedReason = reason.trim()
        val normalizedDetails = details.trim()

        require(normalizedTutorUid.isNotEmpty()) { "Select tutor." }
        require(normalizedReason.isNotEmpty()) { "Reason is required." }

        val tutor = getUserByUid(normalizedTutorUid)
            ?: throw IllegalStateException("Tutor account does not exist.")

        firestore.collection("reports").add(
            mapOf(
                "tutorId" to tutor.uid,
                "tutorDisplayName" to tutor.displayName,
                "studentId" to student.uid,
                "studentDisplayName" to student.displayName,
                "reason" to normalizedReason,
                "details" to normalizedDetails,
                "status" to "open",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun loadAdminReports(): List<TutorReportUi> {
        return try {
            firestore.collection("reports").get().await().documents.mapNotNull(::toReport)
                .sortedByDescending { it.createdAtLabel }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun loadAdminUserInspector(user: AppUser): AdminUserInspectorUi {
        val availability = if (user.hasRole(UserRole.TUTOR)) {
            getAvailabilityForTutor(user.uid)
        } else {
            emptyList()
        }

        return AdminUserInspectorUi(
            user = user,
            availability = availability,
            bookingsAsTutor = getBookingsForTutor(user.uid),
            bookingsAsStudent = getBookingsForStudent(user.uid),
            reviewsReceived = getReviewsForTutor(user.uid),
            reviewsWritten = getReviewsWrittenByStudent(user.uid),
            reportsReceived = getReportsForTutor(user.uid),
            reportsWritten = getReportsWrittenByStudent(user.uid),
            isLoading = false
        )
    }

    suspend fun getOpenAvailability(): List<TutorAvailabilityUi> {
        return try {
            firestore.collection("tutor_availability")
                .whereEqualTo("isBooked", false)
                .get()
                .await()
                .documents
                .mapNotNull(::toAvailability)
                .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getAvailabilityForTutor(tutorId: String): List<TutorAvailabilityUi> {
        return try {
            firestore.collection("tutor_availability")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .await()
                .documents
                .mapNotNull(::toAvailability)
                .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getBookingsForStudent(studentId: String): List<LessonBookingUi> {
        return try {
            firestore.collection("bookings")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
                .documents
                .mapNotNull(::toBooking)
                .sortedWith(compareBy<LessonBookingUi> { it.dateLabel }.thenBy { it.startHour })
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getBookingsForTutor(tutorId: String): List<LessonBookingUi> {
        return try {
            firestore.collection("bookings")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .await()
                .documents
                .mapNotNull(::toBooking)
                .sortedWith(compareBy<LessonBookingUi> { it.dateLabel }.thenBy { it.startHour })
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getReviewsWrittenByStudent(studentId: String): List<TutorReviewUi> {
        return try {
            firestore.collection("reviews")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
                .documents
                .mapNotNull(::toReview)
                .sortedByDescending { it.createdAtLabel }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getReviewsForTutor(tutorId: String): List<TutorReviewUi> {
        return try {
            firestore.collection("reviews")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .await()
                .documents
                .mapNotNull(::toReview)
                .sortedByDescending { it.createdAtLabel }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getReportsWrittenByStudent(studentId: String): List<TutorReportUi> {
        return try {
            firestore.collection("reports")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
                .documents
                .mapNotNull(::toReport)
                .sortedByDescending { it.createdAtLabel }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getReportsForTutor(tutorId: String): List<TutorReportUi> {
        return try {
            firestore.collection("reports")
                .whereEqualTo("tutorId", tutorId)
                .get()
                .await()
                .documents
                .mapNotNull(::toReport)
                .sortedByDescending { it.createdAtLabel }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getTutors(): List<TutorSummaryUi> {
        val users = try {
            firestore.collection("users")
                .whereArrayContains("roles", "tutor")
                .get()
                .await()
                .documents
                .mapNotNull(::toUser)
        } catch (_: Exception) {
            try {
                firestore.collection("users")
                    .get()
                    .await()
                    .documents
                    .mapNotNull(::toUser)
                    .filter { appUser -> appUser.hasRole(UserRole.TUTOR) }
            } catch (_: Exception) {
                emptyList()
            }
        }

        return users.map { appUser ->
            TutorSummaryUi(
                uid = appUser.uid,
                displayName = appUser.displayName,
                subjects = "Set in availability"
            )
        }.sortedBy { it.displayName.lowercase(Locale.getDefault()) }
    }

    private suspend fun getUserByUid(uid: String): AppUser? {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            if (!snapshot.exists()) {
                null
            } else {
                toUser(snapshot)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun toAvailability(document: DocumentSnapshot): TutorAvailabilityUi? {
        val tutorId = document.getString("tutorId") ?: return null
        val subject = document.getString("subject") ?: return null
        val date = document.getString("date") ?: return null

        val startHour = document.get("startHour")?.toString() ?: return null
        val endHour = document.get("endHour")?.toString() ?: return null

        val tutorDisplayName = document.getString("tutorDisplayName").orEmpty().ifBlank { tutorId }

        return TutorAvailabilityUi(
            id = document.id,
            tutorId = tutorId,
            tutorDisplayName = tutorDisplayName,
            subject = subject,
            dateLabel = date,
            startHour = startHour,
            endHour = endHour,
            isBooked = document.getBoolean("isBooked") == true
        )
    }

    private fun toBooking(document: DocumentSnapshot): LessonBookingUi? {
        val tutorId = document.getString("tutorId") ?: return null
        val studentId = document.getString("studentId") ?: return null
        val subject = document.getString("subject") ?: return null
        val date = document.getString("date") ?: return null

        val startHour = document.get("startHour")?.toString() ?: return null
        val endHour = document.get("endHour")?.toString() ?: return null
        val availabilityId = document.getString("availabilityId").orEmpty()

        return LessonBookingUi(
            id = document.id,
            availabilityId = availabilityId,
            tutorId = tutorId,
            tutorDisplayName = document.getString("tutorDisplayName").orEmpty().ifBlank { tutorId },
            studentId = studentId,
            studentDisplayName = document.getString("studentDisplayName").orEmpty().ifBlank { studentId },
            subject = subject,
            dateLabel = date,
            startHour = startHour,
            endHour = endHour,
            status = document.getString("status") ?: "booked"
        )
    }

    private fun toReview(document: DocumentSnapshot): TutorReviewUi? {
        val tutorId = document.getString("tutorId") ?: return null
        val studentId = document.getString("studentId") ?: return null
        val rating = (document.getLong("rating") ?: return null).toInt()

        return TutorReviewUi(
            id = document.id,
            tutorId = tutorId,
            tutorDisplayName = document.getString("tutorDisplayName").orEmpty().ifBlank { tutorId },
            studentId = studentId,
            studentDisplayName = document.getString("studentDisplayName").orEmpty().ifBlank { studentId },
            rating = rating,
            comment = document.getString("comment").orEmpty(),
            createdAtLabel = formatTimestamp(document.get("createdAt"))
        )
    }

    private fun toReport(document: DocumentSnapshot): TutorReportUi? {
        val tutorId = document.getString("tutorId") ?: return null
        val studentId = document.getString("studentId") ?: return null

        return TutorReportUi(
            id = document.id,
            tutorId = tutorId,
            tutorDisplayName = document.getString("tutorDisplayName").orEmpty().ifBlank { tutorId },
            studentId = studentId,
            studentDisplayName = document.getString("studentDisplayName").orEmpty().ifBlank { studentId },
            reason = document.getString("reason").orEmpty(),
            details = document.getString("details").orEmpty(),
            status = document.getString("status") ?: "open",
            createdAtLabel = formatTimestamp(document.get("createdAt"))
        )
    }

    private fun toUser(document: DocumentSnapshot): AppUser? {
        val data = document.data ?: return null
        val email = (data["email"] as? String).orEmpty()
        val login = (data["login"] as? String)
            ?: (data["loginLowercase"] as? String)
            ?: if (email.isNotBlank()) AuthMapping.loginFromEmail(email) else document.id

        return AppUser(
            uid = document.id,
            login = login,
            email = email,
            displayName = (data["displayName"] as? String).orEmpty().ifBlank { login },
            roles = AuthMapping.parseRoles(data["roles"], data["role"] as? String),
            isActive = data["isActive"] as? Boolean ?: true,
            avatarUrl = data["avatarUrl"] as? String
        )
    }

    private fun formatTimestamp(value: Any?): String {
        val millis = when (value) {
            is Timestamp -> value.toDate().time
            is Long -> value
            is Int -> value.toLong()
            else -> null
        } ?: return "Unknown"

        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault()))
    }
}