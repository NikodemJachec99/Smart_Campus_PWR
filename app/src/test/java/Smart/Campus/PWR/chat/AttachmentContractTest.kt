package Smart.Campus.PWR.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AttachmentContractTest {

    @Test
    fun `accepts pdf and common image types`() {
        listOf(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/heic",
            "image/heif"
        ).forEach { mime ->
            assertNull(
                "expected $mime to be allowed",
                AttachmentContract.validationError("file.bin", mime, 1024L)
            )
        }
    }

    @Test
    fun `rejects disallowed mime type`() {
        assertNotNull(AttachmentContract.validationError("a.exe", "application/x-msdownload", 1024L))
        assertNotNull(AttachmentContract.validationError("a.mp4", "video/mp4", 1024L))
    }

    @Test
    fun `rejects empty and oversized files`() {
        assertNotNull(AttachmentContract.validationError("a.pdf", "application/pdf", 0L))
        assertNotNull(AttachmentContract.validationError("a.pdf", "application/pdf", -5L))
        assertNotNull(AttachmentContract.validationError("a.pdf", "application/pdf", AttachmentContract.MAX_BYTES))
        assertNull(AttachmentContract.validationError("a.pdf", "application/pdf", AttachmentContract.MAX_BYTES - 1))
    }

    @Test
    fun `rejects blank or overlong file name`() {
        assertNotNull(AttachmentContract.validationError("", "application/pdf", 1024L))
        assertNotNull(AttachmentContract.validationError("   ", "application/pdf", 1024L))
        assertNotNull(AttachmentContract.validationError("x".repeat(181), "application/pdf", 1024L))
        assertNull(AttachmentContract.validationError("x".repeat(180), "application/pdf", 1024L))
    }

    @Test
    fun `sanitizes file name to storage-safe characters`() {
        assertEquals("notatki_z_zajec.pdf", AttachmentContract.sanitizeFileName("notatki z zajęć.pdf"))
        assertEquals("report-v2.1_final.pdf", AttachmentContract.sanitizeFileName("report-v2.1_final.pdf"))
        assertEquals("attachment", AttachmentContract.sanitizeFileName(""))
    }
}
