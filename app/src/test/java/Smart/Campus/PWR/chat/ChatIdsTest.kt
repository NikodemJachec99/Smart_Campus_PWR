package Smart.Campus.PWR.chat

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatIdsTest {
    @Test
    fun directIdIsDeterministicAndSorted() {
        assertEquals("dm_a_b", ChatIds.directId("b", "a"))
        assertEquals("dm_a_b", ChatIds.directId("a", "b"))
    }
}
