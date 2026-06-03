package Smart.Campus.PWR.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

const val GENERAL_CHANNEL_ID = "smartcampus_general"

object FcmTokenRegistrar {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "General",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Messages, assignments and deadlines" }
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun register(uid: String, firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
        val token = FirebaseMessaging.getInstance().token.await()
        firestore.collection("users").document(uid)
            .update("fcmTokens", FieldValue.arrayUnion(token), "updatedAt", FieldValue.serverTimestamp())
    }

    suspend fun unregister(uid: String, firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
        val token = FirebaseMessaging.getInstance().token.await()
        firestore.collection("users").document(uid)
            .update("fcmTokens", FieldValue.arrayRemove(token))
    }
}
