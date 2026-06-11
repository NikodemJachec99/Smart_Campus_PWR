package Smart.Campus.PWR.chat

import java.text.Normalizer

/**
 * Shared contract for user-uploaded files (chat attachments and course materials).
 * Must stay in sync with `firestore.rules` and `storage.rules`.
 */
object AttachmentContract {
    const val MAX_BYTES: Long = 10L * 1024 * 1024
    const val MAX_FILE_NAME_LENGTH: Int = 180

    val allowedMimeTypes: Set<String> = setOf(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
        "image/heic",
        "image/heif"
    )

    fun validationError(fileName: String, mimeType: String, sizeBytes: Long): String? = when {
        fileName.isBlank() -> "File name is required."
        fileName.length > MAX_FILE_NAME_LENGTH -> "File name must be at most $MAX_FILE_NAME_LENGTH characters."
        mimeType !in allowedMimeTypes -> "Use PDF, JPEG, PNG, WebP, HEIC or HEIF."
        sizeBytes <= 0L -> "File is empty."
        sizeBytes >= MAX_BYTES -> "File must be smaller than 10 MB."
        else -> null
    }

    fun sanitizeFileName(fileName: String): String {
        val withoutDiacritics = Normalizer.normalize(fileName, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return withoutDiacritics
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "attachment" }
    }
}
