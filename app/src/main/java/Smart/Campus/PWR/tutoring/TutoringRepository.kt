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
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
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
        endHour: String,
        durationMinutes: Int = 60,
        format: String = "ONLINE",
        location: String = "",
        meetingUrl: String = "",
        topic: String = ""
    ) {
        val normalizedSubject = subject.trim()
        val normalizedDate = date.trim()
        val normalizedStart = startHour.trim()
        val normalizedEnd = endHour.trim()

        require(normalizedSubject.isNotEmpty()) { "Subject is required." }
        require(normalizedDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Date must use YYYY-MM-DD format." }
        require(normalizedStart.isNotEmpty() && normalizedEnd.isNotEmpty()) { "Start and end hours are required." }
        val (startAt, endAt) = parseRequiredLessonWindow(normalizedDate, normalizedStart, normalizedEnd)
        requireFutureWindow(startAt)

        val payload = mapOf(
            "tutorId" to tutor.uid,
            "tutorDisplayName" to tutor.displayName,
            "subject" to normalizedSubject,
            "date" to normalizedDate,
            "startHour" to normalizedStart,
            "endHour" to normalizedEnd,
            "startAt" to startAt.toFirebaseTimestamp(),
            "endAt" to endAt.toFirebaseTimestamp(),
            "isBooked" to false,
            "durationMinutes" to durationMinutes,
            "format" to format,
            "location" to location,
            "meetingUrl" to meetingUrl,
            "topic" to topic,
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
        user: AppUser,
        durationMinutes: Int = 60,
        format: String = "ONLINE",
        location: String = "",
        meetingUrl: String = "",
        topic: String = ""
    ) {
        val normalizedSubject = newSubject.trim()
        val normalizedDate = newDate.trim()
        val normalizedStart = newStartHour.trim()
        val normalizedEnd = newEndHour.trim()

        require(normalizedSubject.isNotEmpty()) { "Subject is required." }
        require(normalizedDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Date must use YYYY-MM-DD format." }
        require(normalizedStart.isNotEmpty() && normalizedEnd.isNotEmpty()) { "Start and end hours are required." }
        val (startAt, endAt) = parseRequiredLessonWindow(normalizedDate, normalizedStart, normalizedEnd)
        requireFutureWindow(startAt)

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
                    "endHour" to normalizedEnd,
                    "startAt" to startAt.toFirebaseTimestamp(),
                    "endAt" to endAt.toFirebaseTimestamp(),
                    "durationMinutes" to durationMinutes,
                    "format" to format,
                    "location" to location,
                    "meetingUrl" to meetingUrl,
                    "topic" to topic,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.await()
    }

    suspend fun getAllAvailability(): List<TutorAvailabilityUi> {
        return firestore.collection("tutor_availability")
            .get()
            .await()
            .documents
            .mapNotNull(::toAvailability)
            .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
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
            if (tutorId == student.uid) {
                throw IllegalStateException("You cannot book your own tutoring slot.")
            }
            val tutorDisplayName = slotSnapshot.getString("tutorDisplayName").orEmpty()
            val subject = slotSnapshot.getString("subject").orEmpty()
            val date = slotSnapshot.getString("date").orEmpty()


            val startHour = slotSnapshot.get("startHour")?.toString().orEmpty()
            val endHour = slotSnapshot.get("endHour")?.toString().orEmpty()
            val (lessonStartsAt, lessonEndsAt) = parseRequiredLessonWindow(date, startHour, endHour)
            requireFutureWindow(lessonStartsAt)

            transaction.update(
                slotRef,
                mapOf(
                    "isBooked" to true,
                    "bookingId" to bookingRef.id,
                    "bookedBy" to student.uid,
                    "bookedAt" to FieldValue.serverTimestamp(),
                    "startAt" to lessonStartsAt.toFirebaseTimestamp(),
                    "endAt" to lessonEndsAt.toFirebaseTimestamp()
                )
            )

            val bookingPayload = mutableMapOf<String, Any>(
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
                "lessonStartsAt" to lessonStartsAt.toFirebaseTimestamp(),
                "lessonEndsAt" to lessonEndsAt.toFirebaseTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            transaction.set(bookingRef, bookingPayload)
        }.await()
    }

    suspend fun cancelBooking(bookingId: String, slotId: String, reason: String, user: AppUser) {
        require(bookingId.isNotEmpty()) { "Booking ID cannot be empty." }
        val normalizedReason = reason.trim()
        require(normalizedReason.isNotEmpty()) { "Cancellation reason is required." }
        require(normalizedReason.length <= 500) { "Cancellation reason is too long." }

        val bookingRef = firestore.collection("bookings").document(bookingId)

        firestore.runTransaction { transaction ->
            val bookingSnapshot = transaction.get(bookingRef)
            if (!bookingSnapshot.exists()) {
                throw IllegalStateException("Booking not found.")
            }

            val studentId = bookingSnapshot.getString("studentId").orEmpty()
            val tutorId = bookingSnapshot.getString("tutorId").orEmpty()
            val isParticipant = studentId == user.uid || tutorId == user.uid
            if (!isParticipant && !user.hasRole(UserRole.ADMIN)) {
                throw IllegalStateException("Permission denied.")
            }

            if (bookingSnapshot.getString("status") != "booked") {
                throw IllegalStateException("Only booked lessons can be cancelled.")
            }

            val availabilityId = bookingSnapshot.getString("availabilityId").orEmpty().ifBlank { slotId }
            if (availabilityId.isBlank()) {
                throw IllegalStateException("Booking is missing its availability slot.")
            }
            val slotRef = firestore.collection("tutor_availability").document(availabilityId)
            val slotSnapshot = transaction.get(slotRef)
            if (!slotSnapshot.exists()) {
                throw IllegalStateException("Availability slot not found.")
            }

            transaction.update(
                bookingRef,
                mapOf(
                    "status" to "cancelled",
                    "cancelReason" to normalizedReason,
                    "cancelledBy" to user.uid,
                    "cancelledAt" to FieldValue.serverTimestamp()
                )
            )

            transaction.update(
                slotRef,
                mapOf(
                    "isBooked" to false,
                    "bookingId" to FieldValue.delete(),
                    "bookedBy" to FieldValue.delete(),
                    "bookedAt" to FieldValue.delete()
                )
            )
        }.await()
    }

    // -------------------------------------------------------------------------
    // Booking lifecycle: request → accept/decline → reschedule → complete/no-show
    // -------------------------------------------------------------------------

    /**
     * Student requests a tutoring slot that is currently `open`.
     * Slot status transitions: open → pending.
     * Booking is created with status PENDING.
     */
    suspend fun requestBooking(
        slotId: String,
        student: AppUser,
        message: String,
        topic: String
    ) {
        val slotRef = firestore.collection("tutor_availability").document(slotId)
        val bookingRef = firestore.collection("bookings").document()

        firestore.runTransaction { transaction ->
            val slotSnapshot = transaction.get(slotRef)
            if (!slotSnapshot.exists()) {
                throw IllegalStateException("This availability slot no longer exists.")
            }

            val slotStatus = slotSnapshot.getString("slotStatus") ?: "open"
            if (slotStatus != "open") {
                throw IllegalStateException("This slot is not available for booking (status: $slotStatus).")
            }

            val tutorId = slotSnapshot.getString("tutorId").orEmpty()
            if (tutorId == student.uid) {
                throw IllegalStateException("You cannot book your own tutoring slot.")
            }

            val tutorDisplayName = slotSnapshot.getString("tutorDisplayName").orEmpty()
            val subject = slotSnapshot.getString("subject").orEmpty()
            val date = slotSnapshot.getString("date").orEmpty()
            val startHour = slotSnapshot.get("startHour")?.toString().orEmpty()
            val endHour = slotSnapshot.get("endHour")?.toString().orEmpty()
            val format = slotSnapshot.getString("format") ?: "ONLINE"
            val location = slotSnapshot.getString("location") ?: ""
            val meetingUrl = slotSnapshot.getString("meetingUrl") ?: ""
            val durationMinutes = slotSnapshot.getLong("durationMinutes")?.toInt() ?: 60
            val slotTopic = topic.ifBlank { slotSnapshot.getString("topic") ?: "" }

            val (lessonStartsAt, lessonEndsAt) = parseRequiredLessonWindow(date, startHour, endHour)
            requireFutureWindow(lessonStartsAt)

            transaction.update(
                slotRef,
                mapOf(
                    "slotStatus" to "pending",
                    "pendingBookingId" to bookingRef.id,
                    "pendingStudentId" to student.uid
                )
            )

            val bookingPayload = mutableMapOf<String, Any>(
                "availabilityId" to slotId,
                "tutorId" to tutorId,
                "tutorDisplayName" to tutorDisplayName,
                "studentId" to student.uid,
                "studentDisplayName" to student.displayName,
                "subject" to subject,
                "date" to date,
                "startHour" to startHour,
                "endHour" to endHour,
                "format" to format,
                "location" to location,
                "meetingUrl" to meetingUrl,
                "durationMinutes" to durationMinutes,
                "topic" to slotTopic,
                "requestMessage" to message.trim(),
                "status" to "PENDING",
                "lessonStartsAt" to lessonStartsAt.toFirebaseTimestamp(),
                "lessonEndsAt" to lessonEndsAt.toFirebaseTimestamp(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            transaction.set(bookingRef, bookingPayload)
        }.await()
    }

    /**
     * Tutor (or admin) accepts a PENDING booking.
     * Booking status: PENDING → CONFIRMED.
     * Slot status: pending → booked.
     */
    suspend fun acceptBooking(bookingId: String, tutor: AppUser) {
        val bookingRef = firestore.collection("bookings").document(bookingId)

        firestore.runTransaction { transaction ->
            val bookingSnapshot = transaction.get(bookingRef)
            if (!bookingSnapshot.exists()) {
                throw IllegalStateException("Booking not found.")
            }

            val tutorId = bookingSnapshot.getString("tutorId").orEmpty()
            val isAdmin = tutor.hasRole(UserRole.ADMIN)
            if (!isAdmin && tutorId != tutor.uid) {
                throw IllegalStateException("Permission denied.")
            }

            val currentStatus = bookingSnapshot.getString("status").orEmpty()
            if (!BookingRules.isPending(currentStatus)) {
                throw IllegalStateException("Only PENDING bookings can be accepted (current: $currentStatus).")
            }

            val availabilityId = bookingSnapshot.getString("availabilityId").orEmpty()
            val slotRef = firestore.collection("tutor_availability").document(availabilityId)

            transaction.update(
                bookingRef,
                mapOf(
                    "status" to "CONFIRMED",
                    "confirmedAt" to FieldValue.serverTimestamp()
                )
            )

            if (availabilityId.isNotBlank()) {
                transaction.update(
                    slotRef,
                    mapOf(
                        "slotStatus" to "booked",
                        "isBooked" to true,
                        "bookingId" to bookingId,
                        "bookedBy" to bookingSnapshot.getString("studentId").orEmpty()
                    )
                )
            }
        }.await()
    }

    /**
     * Tutor (or admin) declines a PENDING booking.
     * Booking status: PENDING → DECLINED.
     * Slot status: pending → open.
     */
    suspend fun declineBooking(bookingId: String, tutor: AppUser, reason: String) {
        val normalizedReason = reason.trim()
        require(normalizedReason.isNotEmpty()) { "Decline reason is required." }

        val bookingRef = firestore.collection("bookings").document(bookingId)

        firestore.runTransaction { transaction ->
            val bookingSnapshot = transaction.get(bookingRef)
            if (!bookingSnapshot.exists()) {
                throw IllegalStateException("Booking not found.")
            }

            val tutorId = bookingSnapshot.getString("tutorId").orEmpty()
            val isAdmin = tutor.hasRole(UserRole.ADMIN)
            if (!isAdmin && tutorId != tutor.uid) {
                throw IllegalStateException("Permission denied.")
            }

            val currentStatus = bookingSnapshot.getString("status").orEmpty()
            if (!BookingRules.isPending(currentStatus)) {
                throw IllegalStateException("Only PENDING bookings can be declined (current: $currentStatus).")
            }

            val availabilityId = bookingSnapshot.getString("availabilityId").orEmpty()
            val slotRef = firestore.collection("tutor_availability").document(availabilityId)

            transaction.update(
                bookingRef,
                mapOf(
                    "status" to "DECLINED",
                    "cancelReason" to normalizedReason,
                    "cancelledBy" to tutor.uid,
                    "cancelledAt" to FieldValue.serverTimestamp()
                )
            )

            if (availabilityId.isNotBlank()) {
                transaction.update(
                    slotRef,
                    mapOf(
                        "slotStatus" to "open",
                        "pendingBookingId" to FieldValue.delete(),
                        "pendingStudentId" to FieldValue.delete()
                    )
                )
            }
        }.await()
    }

    /**
     * Either participant (or admin) reschedules a CONFIRMED booking to a different open slot.
     * Old slot: booked → open.
     * New slot: open → booked.
     * Booking's slot reference and time fields are updated; status stays CONFIRMED.
     */
    suspend fun rescheduleBooking(bookingId: String, newSlotId: String, user: AppUser) {
        require(bookingId.isNotEmpty()) { "Booking ID cannot be empty." }
        require(newSlotId.isNotEmpty()) { "New slot ID cannot be empty." }

        val bookingRef = firestore.collection("bookings").document(bookingId)
        val newSlotRef = firestore.collection("tutor_availability").document(newSlotId)

        firestore.runTransaction { transaction ->
            val bookingSnapshot = transaction.get(bookingRef)
            if (!bookingSnapshot.exists()) {
                throw IllegalStateException("Booking not found.")
            }

            val studentId = bookingSnapshot.getString("studentId").orEmpty()
            val tutorId = bookingSnapshot.getString("tutorId").orEmpty()
            val isParticipant = studentId == user.uid || tutorId == user.uid
            if (!isParticipant && !user.hasRole(UserRole.ADMIN)) {
                throw IllegalStateException("Permission denied.")
            }

            val newSlotSnapshot = transaction.get(newSlotRef)
            if (!newSlotSnapshot.exists()) {
                throw IllegalStateException("New slot not found.")
            }

            val newSlotStatus = newSlotSnapshot.getString("slotStatus") ?: "open"
            if (newSlotStatus != "open") {
                throw IllegalStateException("New slot is not available (status: $newSlotStatus).")
            }

            // Release the old slot
            val oldAvailabilityId = bookingSnapshot.getString("availabilityId").orEmpty()
            if (oldAvailabilityId.isNotBlank() && oldAvailabilityId != newSlotId) {
                val oldSlotRef = firestore.collection("tutor_availability").document(oldAvailabilityId)
                transaction.update(
                    oldSlotRef,
                    mapOf(
                        "slotStatus" to "open",
                        "isBooked" to false,
                        "bookingId" to FieldValue.delete(),
                        "bookedBy" to FieldValue.delete(),
                        "bookedAt" to FieldValue.delete(),
                        "pendingBookingId" to FieldValue.delete(),
                        "pendingStudentId" to FieldValue.delete()
                    )
                )
            }

            // Claim the new slot
            transaction.update(
                newSlotRef,
                mapOf(
                    "slotStatus" to "booked",
                    "isBooked" to true,
                    "bookingId" to bookingId,
                    "bookedBy" to studentId,
                    "bookedAt" to FieldValue.serverTimestamp()
                )
            )

            val newDate = newSlotSnapshot.getString("date").orEmpty()
            val newStartHour = newSlotSnapshot.get("startHour")?.toString().orEmpty()
            val newEndHour = newSlotSnapshot.get("endHour")?.toString().orEmpty()
            val (newLessonStart, newLessonEnd) = parseRequiredLessonWindow(newDate, newStartHour, newEndHour)
            requireFutureWindow(newLessonStart)

            transaction.update(
                bookingRef,
                mapOf(
                    "availabilityId" to newSlotId,
                    "date" to newDate,
                    "startHour" to newStartHour,
                    "endHour" to newEndHour,
                    "format" to (newSlotSnapshot.getString("format") ?: "ONLINE"),
                    "location" to (newSlotSnapshot.getString("location") ?: ""),
                    "meetingUrl" to (newSlotSnapshot.getString("meetingUrl") ?: ""),
                    "lessonStartsAt" to newLessonStart.toFirebaseTimestamp(),
                    "lessonEndsAt" to newLessonEnd.toFirebaseTimestamp(),
                    "rescheduledAt" to FieldValue.serverTimestamp(),
                    "rescheduledBy" to user.uid
                )
            )
        }.await()
    }

    /**
     * Marks a CONFIRMED booking as COMPLETED (tutor, admin, or student).
     */
    suspend fun markLessonCompleted(bookingId: String, user: AppUser) {
        updateLessonOutcome(bookingId, user, newStatus = "COMPLETED")
    }

    /**
     * Marks a CONFIRMED booking as NO_SHOW (tutor, admin, or student).
     */
    suspend fun markNoShow(bookingId: String, user: AppUser) {
        updateLessonOutcome(bookingId, user, newStatus = "NO_SHOW")
    }

    private suspend fun updateLessonOutcome(bookingId: String, user: AppUser, newStatus: String) {
        require(bookingId.isNotEmpty()) { "Booking ID cannot be empty." }

        val bookingRef = firestore.collection("bookings").document(bookingId)

        firestore.runTransaction { transaction ->
            val bookingSnapshot = transaction.get(bookingRef)
            if (!bookingSnapshot.exists()) {
                throw IllegalStateException("Booking not found.")
            }

            val studentId = bookingSnapshot.getString("studentId").orEmpty()
            val tutorId = bookingSnapshot.getString("tutorId").orEmpty()
            val isParticipant = studentId == user.uid || tutorId == user.uid
            if (!isParticipant && !user.hasRole(UserRole.ADMIN)) {
                throw IllegalStateException("Permission denied.")
            }

            val currentStatus = bookingSnapshot.getString("status").orEmpty()
            if (currentStatus != "CONFIRMED") {
                throw IllegalStateException("Only CONFIRMED bookings can be marked $newStatus (current: $currentStatus).")
            }

            transaction.update(
                bookingRef,
                mapOf(
                    "status" to newStatus,
                    "resolvedAt" to FieldValue.serverTimestamp(),
                    "resolvedBy" to user.uid
                )
            )
        }.await()
    }

    // -------------------------------------------------------------------------

    suspend fun createReview(
        student: AppUser,
        tutorUid: String,
        bookingId: String,
        rating: Int,
        comment: String,
        tags: List<String> = emptyList(),
        anonymous: Boolean = false
    ) {
        val normalizedComment = comment.trim()
        val normalizedTutorUid = tutorUid.trim()
        val normalizedBookingId = bookingId.trim()

        require(rating in 1..5) { "Rating must be between 1 and 5." }
        require(normalizedComment.isNotEmpty()) { "Review comment is required." }

        val payload = if (normalizedBookingId.isNotEmpty()) {
            buildLessonReviewPayload(
                student = student,
                bookingId = normalizedBookingId,
                rating = rating,
                comment = normalizedComment,
                tags = tags,
                anonymous = anonymous
            )
        } else {
            require(normalizedTutorUid.isNotEmpty()) { "Select tutor." }
            val tutor = getUserByUid(normalizedTutorUid)
                ?: throw IllegalStateException("Tutor account does not exist.")

            mapOf(
                "reviewType" to "general",
                "tutorId" to tutor.uid,
                "tutorDisplayName" to tutor.displayName,
                "studentId" to student.uid,
                "studentDisplayName" to student.displayName,
                "rating" to rating,
                "comment" to normalizedComment,
                "tags" to tags,
                "anonymous" to anonymous,
                "createdAt" to FieldValue.serverTimestamp()
            )
        }

        if (normalizedBookingId.isNotEmpty()) {
            firestore.collection("reviews")
                .document("lesson_${normalizedBookingId}_${student.uid}")
                .set(payload)
                .await()
        } else {
            firestore.collection("reviews").add(payload).await()
        }
    }

    private suspend fun buildLessonReviewPayload(
        student: AppUser,
        bookingId: String,
        rating: Int,
        comment: String,
        tags: List<String> = emptyList(),
        anonymous: Boolean = false
    ): Map<String, Any> {
        val bookingSnapshot = firestore.collection("bookings").document(bookingId).get().await()
        if (!bookingSnapshot.exists()) {
            throw IllegalStateException("Lesson not found.")
        }

        val studentId = bookingSnapshot.getString("studentId").orEmpty()
        if (studentId != student.uid) {
            throw IllegalStateException("You can review only your own lessons.")
        }

        if (bookingSnapshot.getString("status") != "booked") {
            throw IllegalStateException("Only booked lessons can be reviewed after they end.")
        }

        val lessonEnd = bookingSnapshot.getTimestamp("lessonEndsAt")
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDateTime()
            ?: throw IllegalStateException("Lesson end timestamp is missing.")
        if (lessonEnd.isAfter(LocalDateTime.now())) {
            throw IllegalStateException("You can review a lesson after it ends.")
        }

        val date = bookingSnapshot.getString("date").orEmpty()
        val startHour = bookingSnapshot.get("startHour")?.toString().orEmpty()
        val endHour = bookingSnapshot.get("endHour")?.toString().orEmpty()

        val alreadyReviewed = firestore.collection("reviews")
            .whereEqualTo("bookingId", bookingId)
            .get()
            .await()
            .documents
            .any { it.getString("studentId") == student.uid }

        if (alreadyReviewed) {
            throw IllegalStateException("This lesson has already been reviewed.")
        }

        val tutorId = bookingSnapshot.getString("tutorId").orEmpty()
        val tutorDisplayName = bookingSnapshot.getString("tutorDisplayName").orEmpty().ifBlank { tutorId }
        val subject = bookingSnapshot.getString("subject").orEmpty()

        return mapOf(
            "reviewType" to "lesson",
            "bookingId" to bookingId,
            "tutorId" to tutorId,
            "tutorDisplayName" to tutorDisplayName,
            "studentId" to student.uid,
            "studentDisplayName" to student.displayName,
            "rating" to rating,
            "comment" to comment,
            "subject" to subject,
            "lessonDate" to date,
            "lessonTime" to "$startHour - $endHour",
            "tags" to tags,
            "anonymous" to anonymous,
            "createdAt" to FieldValue.serverTimestamp()
        )
    }

    fun listenDashboard(
        user: AppUser,
        onUpdate: (DashboardUiState) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        val registrations = mutableListOf<ListenerRegistration>()
        var current = DashboardUiState(isLoading = false)

        fun emit(next: DashboardUiState) {
            current = next.copy(isLoading = false)
            onUpdate(current)
        }

        if (user.hasRole(UserRole.STUDENT)) {
            registrations += firestore.collection("bookings")
                .whereEqualTo("studentId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        val bookings = snapshot?.documents.orEmpty()
                            .mapNotNull(::toBooking)
                            .sortedWith(compareBy<LessonBookingUi> { it.dateLabel }.thenBy { it.startHour })
                        emit(current.copy(myStudentBookings = bookings))
                    }
                }

            registrations += firestore.collection("tutor_availability")
                .whereEqualTo("isBooked", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        val slots = snapshot?.documents.orEmpty()
                            .mapNotNull(::toAvailability)
                            .filter { it.tutorId != user.uid }
                            .filter(::isFutureSlot)
                            .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
                        emit(current.copy(availableTutorSlots = slots))
                    }
                }

            registrations += firestore.collection("reviews")
                .whereEqualTo("studentId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        val reviews = snapshot?.documents.orEmpty()
                            .mapNotNull(::toReview)
                            .sortedByDescending { it.createdAtLabel }
                        emit(current.copy(reviewsByMe = reviews))
                    }
                }

            registrations += firestore.collection("reports")
                .whereEqualTo("studentId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        val reports = snapshot?.documents.orEmpty()
                            .mapNotNull(::toReport)
                            .sortedByDescending { it.createdAtLabel }
                        emit(current.copy(reportsByMe = reports))
                    }
                }

            var tutorUsers = emptyList<AppUser>()
            var lecturerUsers = emptyList<AppUser>()
            var cachedReviews = emptyList<TutorReviewUi>()
            fun emitTutors() {
                val ratingStats = TutorRatings.aggregateRatings(cachedReviews)
                val tutors = (tutorUsers + lecturerUsers)
                    .distinctBy { it.uid }
                    .map { appUser ->
                        val stats = ratingStats[appUser.uid]
                        TutorSummaryUi(
                            uid = appUser.uid,
                            displayName = appUser.displayName,
                            subjects = appUser.subjects.joinToString(", ").ifBlank { "Set in availability" },
                            bio = appUser.bio.orEmpty(),
                            subjectsList = appUser.subjects,
                            verified = appUser.verified,
                            experienceYears = appUser.experienceYears,
                            ratingAvg = stats?.avg ?: 0.0,
                            ratingCount = stats?.count ?: 0
                        )
                    }
                    .sortedBy { it.displayName.lowercase(Locale.getDefault()) }
                emit(current.copy(tutors = tutors))
            }

            registrations += firestore.collection("reviews")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        cachedReviews = snapshot?.documents.orEmpty().mapNotNull(::toReview)
                        emitTutors()
                    }
                }

            registrations += firestore.collection("users")
                .whereArrayContains("roles", "tutor")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        tutorUsers = snapshot?.documents.orEmpty().mapNotNull(::toUser)
                        emitTutors()
                    }
                }

            registrations += firestore.collection("users")
                .whereArrayContains("roles", "lecturer")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        lecturerUsers = snapshot?.documents.orEmpty().mapNotNull(::toUser)
                        emitTutors()
                    }
                }
        }

        if (user.hasRole(UserRole.TUTOR)) {
            registrations += firestore.collection("bookings")
                .whereEqualTo("tutorId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        val bookings = snapshot?.documents.orEmpty()
                            .mapNotNull(::toBooking)
                            .sortedWith(compareBy<LessonBookingUi> { it.dateLabel }.thenBy { it.startHour })
                        emit(current.copy(myTutorBookings = bookings))
                    }
                }

            registrations += firestore.collection("tutor_availability")
                .whereEqualTo("tutorId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        val availability = snapshot?.documents.orEmpty()
                            .mapNotNull(::toAvailability)
                            .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
                        emit(current.copy(myAvailability = availability))
                    }
                }

            registrations += firestore.collection("reviews")
                .whereEqualTo("tutorId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                    } else {
                        val reviews = snapshot?.documents.orEmpty()
                            .mapNotNull(::toReview)
                            .sortedByDescending { it.createdAtLabel }
                        emit(current.copy(reviewsForMe = reviews))
                    }
                }
        }

        return object : ListenerRegistration {
            override fun remove() {
                registrations.forEach { it.remove() }
                registrations.clear()
            }
        }
    }

    suspend fun createReport(
        student: AppUser,
        tutorUid: String,
        reason: String,
        details: String,
        severity: String = "MEDIUM"
    ) {
        val normalizedTutorUid = tutorUid.trim()
        val normalizedReason = reason.trim()
        val normalizedDetails = details.trim()
        val normalizedSeverity = severity.trim().uppercase(Locale.getDefault())

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
                "severity" to normalizedSeverity,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun loadAdminReports(): List<TutorReportUi> {
        return firestore.collection("reports").get().await().documents.mapNotNull(::toReport)
            .sortedByDescending { it.createdAtLabel }
    }

    suspend fun updateReportStatus(
        reportId: String,
        status: String,
        user: AppUser,
        note: String = ""
    ) {
        require(user.hasRole(UserRole.ADMIN)) { "Only admins can moderate reports." }
        require(reportId.isNotBlank()) { "Report ID cannot be empty." }
        val normalizedStatus = status.trim().lowercase(Locale.getDefault())
        require(normalizedStatus in setOf("open", "in_review", "resolved", "dismissed")) {
            "Unsupported report status."
        }

        val update = mutableMapOf<String, Any>(
            "status" to normalizedStatus,
            "reviewedBy" to user.uid,
            "reviewedAt" to FieldValue.serverTimestamp()
        )
        val normalizedNote = note.trim()
        if (normalizedNote.isNotBlank()) {
            update["moderatorNote"] = normalizedNote
        }

        firestore.collection("reports").document(reportId).update(update).await()
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
        return firestore.collection("tutor_availability")
            .whereEqualTo("isBooked", false)
            .get()
            .await()
            .documents
            .mapNotNull(::toAvailability)
            .filter(::isFutureSlot)
            .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
    }

    private suspend fun getAvailabilityForTutor(tutorId: String): List<TutorAvailabilityUi> {
        return firestore.collection("tutor_availability")
            .whereEqualTo("tutorId", tutorId)
            .get()
            .await()
            .documents
            .mapNotNull(::toAvailability)
            .sortedWith(compareBy<TutorAvailabilityUi> { it.dateLabel }.thenBy { it.startHour })
    }

    private suspend fun getBookingsForStudent(studentId: String): List<LessonBookingUi> {
        return firestore.collection("bookings")
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .mapNotNull(::toBooking)
            .sortedWith(compareBy<LessonBookingUi> { it.dateLabel }.thenBy { it.startHour })
    }

    private suspend fun getBookingsForTutor(tutorId: String): List<LessonBookingUi> {
        return firestore.collection("bookings")
            .whereEqualTo("tutorId", tutorId)
            .get()
            .await()
            .documents
            .mapNotNull(::toBooking)
            .sortedWith(compareBy<LessonBookingUi> { it.dateLabel }.thenBy { it.startHour })
    }

    private suspend fun getReviewsWrittenByStudent(studentId: String): List<TutorReviewUi> {
        return firestore.collection("reviews")
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .mapNotNull(::toReview)
            .sortedByDescending { it.createdAtLabel }
    }

    private suspend fun getReviewsForTutor(tutorId: String): List<TutorReviewUi> {
        return firestore.collection("reviews")
            .whereEqualTo("tutorId", tutorId)
            .get()
            .await()
            .documents
            .mapNotNull(::toReview)
            .sortedByDescending { it.createdAtLabel }
    }

    private suspend fun getReportsWrittenByStudent(studentId: String): List<TutorReportUi> {
        return firestore.collection("reports")
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .mapNotNull(::toReport)
            .sortedByDescending { it.createdAtLabel }
    }

    private suspend fun getReportsForTutor(tutorId: String): List<TutorReportUi> {
        return firestore.collection("reports")
            .whereEqualTo("tutorId", tutorId)
            .get()
            .await()
            .documents
            .mapNotNull(::toReport)
            .sortedByDescending { it.createdAtLabel }
    }

    private suspend fun getTutors(): List<TutorSummaryUi> {
        val tutorDocs = firestore.collection("users")
            .whereArrayContains("roles", "tutor")
            .get()
            .await()
            .documents
        val lecturerDocs = firestore.collection("users")
            .whereArrayContains("roles", "lecturer")
            .get()
            .await()
            .documents

        val users = (tutorDocs + lecturerDocs)
            .distinctBy { it.id }
            .mapNotNull(::toUser)

        val allReviews = firestore.collection("reviews").get().await().documents.mapNotNull(::toReview)
        val ratingStats = TutorRatings.aggregateRatings(allReviews)

        return users.map { appUser ->
            val stats = ratingStats[appUser.uid]
            TutorSummaryUi(
                uid = appUser.uid,
                displayName = appUser.displayName,
                subjects = appUser.subjects.joinToString(", ").ifBlank { "Set in availability" },
                bio = appUser.bio.orEmpty(),
                subjectsList = appUser.subjects,
                verified = appUser.verified,
                experienceYears = appUser.experienceYears,
                ratingAvg = stats?.avg ?: 0.0,
                ratingCount = stats?.count ?: 0
            )
        }.sortedBy { it.displayName.lowercase(Locale.getDefault()) }
    }

    private suspend fun getUserByUid(uid: String): AppUser? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        return if (!snapshot.exists()) null else toUser(snapshot)
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
            isBooked = document.getBoolean("isBooked") == true,
            durationMinutes = document.getLong("durationMinutes")?.toInt() ?: 60,
            format = document.getString("format") ?: "ONLINE",
            location = document.getString("location") ?: "",
            meetingUrl = document.getString("meetingUrl") ?: "",
            topic = document.getString("topic") ?: ""
        )
    }

    private fun String?.toBookingStatus(): String = BookingRules.normalizeLegacyStatus(this)

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
            status = document.getString("status").toBookingStatus(),
            cancelReason = document.getString("cancelReason").orEmpty(),
            cancelledBy = document.getString("cancelledBy").orEmpty(),
            cancelledAtLabel = formatTimestamp(document.get("cancelledAt")),
            format = document.getString("format") ?: "ONLINE",
            location = document.getString("location") ?: "",
            meetingUrl = document.getString("meetingUrl") ?: "",
            topic = document.getString("topic") ?: "",
            requestMessage = document.getString("requestMessage") ?: ""
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
            createdAtLabel = formatTimestamp(document.get("createdAt")),
            bookingId = document.getString("bookingId").orEmpty(),
            reviewType = document.getString("reviewType").orEmpty().ifBlank { "general" },
            subject = document.getString("subject").orEmpty(),
            lessonDateLabel = document.getString("lessonDate").orEmpty(),
            lessonTimeLabel = document.getString("lessonTime").orEmpty(),
            tags = (document.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            anonymous = document.getBoolean("anonymous") ?: false
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
            createdAtLabel = formatTimestamp(document.get("createdAt")),
            severity = document.getString("severity") ?: "MEDIUM",
            moderatorNote = document.getString("moderatorNote") ?: ""
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
            avatarUrl = data["avatarUrl"] as? String,
            bio = data["bio"] as? String,
            subjects = (data["subjects"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            experienceYears = (data["experienceYears"] as? Long)?.toInt(),
            verified = data["verified"] as? Boolean ?: false
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

    private fun parseLessonDateTime(date: String, hour: String): LocalDateTime? {
        val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
        val parsedTime = runCatching {
            LocalTime.parse(hour, DateTimeFormatter.ofPattern("HH:mm"))
        }.getOrNull() ?: return null
        return LocalDateTime.of(parsedDate, parsedTime)
    }

    private fun parseRequiredLessonWindow(date: String, startHour: String, endHour: String): Pair<LocalDateTime, LocalDateTime> {
        val start = parseLessonDateTime(date, startHour)
            ?: throw IllegalArgumentException("Start date/time is invalid.")
        val end = parseLessonDateTime(date, endHour)
            ?: throw IllegalArgumentException("End date/time is invalid.")
        require(end.isAfter(start)) { "End time must be after start time." }
        return start to end
    }

    private fun requireFutureWindow(startAt: LocalDateTime) {
        require(startAt.isAfter(LocalDateTime.now())) {
            "Lessons cannot be planned or booked in the past."
        }
    }

    private fun LocalDateTime.toFirebaseTimestamp(): Timestamp {
        val instant = atZone(ZoneId.systemDefault()).toInstant()
        return Timestamp(Date.from(instant))
    }

    private fun isFutureSlot(slot: TutorAvailabilityUi): Boolean {
        val start = parseLessonDateTime(slot.dateLabel, slot.startHour) ?: return false
        return start.isAfter(LocalDateTime.now())
    }
}
