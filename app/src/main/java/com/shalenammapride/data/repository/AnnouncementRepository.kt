package com.shalenammapride.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.shalenammapride.data.model.Announcement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class AnnouncementRepository {
    private val db = Firebase.database.reference.child("announcements")
    private val storage = Firebase.storage.reference.child("announcements")
    private val reactionsRef = Firebase.database.reference.child("announcement_reactions")

    // Get from Firebase Console → Project Settings → Cloud Messaging → Server key (Legacy)
    private companion object {
        const val FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE"
    }

    fun getAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(Announcement::class.java) }
                    .sortedByDescending { it.postedDate }
                trySend(items)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        db.addValueEventListener(listener)
        awaitClose { db.removeEventListener(listener) }
    }

    fun getReactions(): Flow<Map<String, Map<String, String>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = mutableMapOf<String, Map<String, String>>()
                snapshot.children.forEach { annSnap ->
                    val annId = annSnap.key ?: return@forEach
                    val votes = mutableMapOf<String, String>()
                    annSnap.children.forEach { d ->
                        d.key?.let { k -> d.getValue(String::class.java)?.let { v -> votes[k] = v } }
                    }
                    result[annId] = votes
                }
                trySend(result)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        reactionsRef.addValueEventListener(listener)
        awaitClose { reactionsRef.removeEventListener(listener) }
    }

    suspend fun setReaction(announcementId: String, deviceId: String, vote: String) {
        reactionsRef.child(announcementId).child(deviceId).setValue(vote).await()
    }

    suspend fun removeReaction(announcementId: String, deviceId: String) {
        reactionsRef.child(announcementId).child(deviceId).removeValue().await()
    }

    suspend fun uploadImage(imageBytes: ByteArray): String {
        val ref = storage.child("${UUID.randomUUID()}.jpg")
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun addAnnouncement(announcement: Announcement): Result<Unit> = runCatching {
        val key = db.push().key ?: UUID.randomUUID().toString()
        db.child(key).setValue(announcement.copy(id = key)).await()
    }

    suspend fun sendNotification(title: String, body: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL("https://fcm.googleapis.com/fcm/send")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "key=$FCM_SERVER_KEY")
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true
                val safeTitle = title.replace("\"", "'")
                val safeBody = body.replace("\"", "'").ifEmpty { "New announcement from school" }
                val payload = """{"to":"/topics/school_updates","notification":{"title":"$safeTitle","body":"$safeBody","sound":"default"},"priority":"high"}"""
                conn.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload) }
                conn.responseCode
                conn.disconnect()
            }
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> = runCatching {
        db.child(id).removeValue().await()
    }
}
