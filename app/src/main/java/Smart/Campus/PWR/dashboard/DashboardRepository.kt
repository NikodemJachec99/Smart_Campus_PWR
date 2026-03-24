package Smart.Campus.PWR.dashboard

import Smart.Campus.PWR.auth.AppUser
import Smart.Campus.PWR.auth.UserRole
import Smart.Campus.PWR.ui.state.UpcomingClassUi
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DashboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getUpcomingClasses(user: AppUser, activeRole: UserRole): List<UpcomingClassUi> {
        val documentsById = LinkedHashMap<String, DocumentSnapshot>()

        val queryFields = linkedSetOf("assignedUserIds")
        when (activeRole) {
            UserRole.STUDENT -> queryFields.add("studentIds")
            UserRole.LECTURER -> queryFields.add("lecturerIds")
            UserRole.ADMIN -> Unit
        }

        for (field in queryFields) {
            try {
                firestore.collection("classes")
                    .whereArrayContains(field, user.uid)
                    .get()
                    .await()
                    .documents
                    .forEach { document -> documentsById[document.id] = document }
            } catch (_: Exception) {
                // Missing collection, permissions, or schema mismatch should not crash the dashboard.
            }
        }

        if (documentsById.isEmpty()) {
            try {
                firestore.collection("classes")
                    .limit(40)
                    .get()
                    .await()
                    .documents
                    .filter { document -> isAssignedToUser(document, user.uid, activeRole) }
                    .forEach { document -> documentsById[document.id] = document }
            } catch (_: Exception) {
                return emptyList()
            }
        }

        val now = System.currentTimeMillis() - 15 * 60 * 1000L

        return documentsById.values
            .mapNotNull(::toUpcomingClassUi)
            .filter { classItem -> classItem.startsAtMillis >= now }
            .sortedBy { classItem -> classItem.startsAtMillis }
            .take(6)
    }

    private fun isAssignedToUser(document: DocumentSnapshot, uid: String, activeRole: UserRole): Boolean {
        val data = document.data ?: return false
        val listMatches = listOf(
            data["assignedUserIds"],
            data["userIds"],
            data["attendeeIds"],
            when (activeRole) {
                UserRole.STUDENT -> data["studentIds"]
                UserRole.LECTURER -> data["lecturerIds"]
                UserRole.ADMIN -> null
            }
        ).filterNotNull().any { fieldValue ->
            fieldValue is List<*> && fieldValue.any { entry -> entry == uid }
        }

        if (listMatches) {
            return true
        }

        val singleFieldMatches = listOf(
            data["assignedUserId"],
            data["studentId"],
            data["lecturerId"],
            data["ownerId"]
        ).any { fieldValue -> fieldValue == uid }

        return singleFieldMatches
    }

    private fun toUpcomingClassUi(document: DocumentSnapshot): UpcomingClassUi? {
        val data = document.data ?: return null
        val title = firstString(data, "title", "subject", "name") ?: return null
        val typeLabel = firstString(data, "type", "classType", "kind") ?: "Zajecia"
        val roomLabel = firstString(data, "room", "location", "classroom", "hall") ?: "Lokalizacja TBD"
        val startsAtMillis = firstTimestampMillis(data["startTime"], data["startAt"], data["startsAt"]) ?: return null
        val durationMinutes = firstInt(data["durationMinutes"], data["duration"], data["durationMin"])
        val timeLabel = Instant.ofEpochMilli(startsAtMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))

        return UpcomingClassUi(
            id = document.id,
            timeLabel = timeLabel,
            durationLabel = durationMinutes?.let { "$it min" } ?: "Czas TBD",
            title = title,
            typeLabel = typeLabel,
            roomLabel = roomLabel,
            startsAtMillis = startsAtMillis
        )
    }

    private fun firstString(data: Map<String, Any?>, vararg keys: String): String? {
        return keys.asSequence()
            .mapNotNull { key -> data[key] as? String }
            .map { value -> value.trim() }
            .firstOrNull { value -> value.isNotEmpty() }
    }

    private fun firstInt(vararg values: Any?): Int? {
        return values.asSequence()
            .mapNotNull { value ->
                when (value) {
                    is Int -> value
                    is Long -> value.toInt()
                    is Double -> value.toInt()
                    is String -> value.toIntOrNull()
                    else -> null
                }
            }
            .firstOrNull()
    }

    private fun firstTimestampMillis(vararg values: Any?): Long? {
        return values.asSequence()
            .mapNotNull { value -> parseTimestampMillis(value) }
            .firstOrNull()
    }

    private fun parseTimestampMillis(value: Any?): Long? {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Long -> if (value < 100000000000L) value * 1000 else value
            is Int -> value.toLong() * 1000
            is Double -> {
                val longValue = value.toLong()
                if (longValue < 100000000000L) longValue * 1000 else longValue
            }
            is String -> parseStringDate(value)
            else -> null
        }
    }

    private fun parseStringDate(rawValue: String): Long? {
        return try {
            Instant.parse(rawValue).toEpochMilli()
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(rawValue).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }
    }
}
