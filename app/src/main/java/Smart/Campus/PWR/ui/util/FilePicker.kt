package Smart.Campus.PWR.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class PickedFile(
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long
)

fun Context.readPickedFile(uri: Uri): PickedFile? {
    val resolver = contentResolver
    var fileName = uri.lastPathSegment ?: "attachment"
    var size = 0L
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
            if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
        }
    }
    val mimeType = resolver.getType(uri) ?: inferMimeType(fileName)
    if (size <= 0L) {
        size = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
    }
    return PickedFile(fileName = fileName, mimeType = mimeType, sizeBytes = size)
}

fun inferMimeType(fileName: String): String = when (fileName.substringAfterLast(".", "").lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "webp" -> "image/webp"
    "heic" -> "image/heic"
    "heif" -> "image/heif"
    "pdf" -> "application/pdf"
    else -> "application/octet-stream"
}
