package Smart.Campus.PWR.chat

import Smart.Campus.PWR.ui.state.ChatAttachmentUi
import Smart.Campus.PWR.ui.state.ChatReplyPreviewUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatReadStateTest {
    @Test
    fun unreadCountIgnoresMessagesSentByViewer() {
        val count = ChatReadState.unreadCount(
            viewerUid = "student",
            lastMessageSenderUid = "student",
            lastMessageAtMillis = 2000L,
            readAtMillis = 0L
        )

        assertEquals(0, count)
    }

    @Test
    fun unreadCountRequiresNewerMessageThanReadReceipt() {
        val unread = ChatReadState.unreadCount(
            viewerUid = "student",
            lastMessageSenderUid = "tutor",
            lastMessageAtMillis = 2000L,
            readAtMillis = 1000L
        )
        val read = ChatReadState.unreadCount(
            viewerUid = "student",
            lastMessageSenderUid = "tutor",
            lastMessageAtMillis = 2000L,
            readAtMillis = 2000L
        )

        assertEquals(1, unread)
        assertEquals(0, read)
    }

    @Test
    fun readStateForMineUsesOtherReadReceipt() {
        assertTrue(ChatReadState.isReadByOther(sentAtMillis = 2000L, otherReadAtMillis = 2500L))
        assertFalse(ChatReadState.isReadByOther(sentAtMillis = 2000L, otherReadAtMillis = 1000L))
    }

    @Test
    fun attachmentMetadataExposesImageAndSizeLabel() {
        val attachment = ChatAttachmentUi(
            fileName = "notes.webp",
            mimeType = "image/webp",
            sizeBytes = 2 * 1024 * 1024,
            storagePath = "chat/direct/a/b/c/notes.webp",
            downloadUrl = "https://example.test/notes.webp"
        )

        assertTrue(attachment.isImage)
        assertEquals("2 MB", attachment.sizeLabel)
    }

    @Test
    fun replyPreviewFallsBackToAttachmentName() {
        val reply = ChatReplyPreviewUi(
            messageId = "message-1",
            senderName = "Tutor",
            textPreview = "",
            attachmentName = "lecture.pdf"
        )

        assertEquals("lecture.pdf", reply.previewLabel)
    }
}
