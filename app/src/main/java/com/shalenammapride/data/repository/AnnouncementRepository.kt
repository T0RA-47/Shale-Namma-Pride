package com.shalenammapride.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shalenammapride.data.model.Announcement
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AnnouncementRepository {
    private val db = Firebase.database.reference.child("announcements")

    fun getAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull {
                    it.getValue(Announcement::class.java)
                }.sortedByDescending { it.postedDate }
                trySend(items)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        db.addValueEventListener(listener)
        awaitClose { db.removeEventListener(listener) }
    }

    suspend fun addAnnouncement(announcement: Announcement): Result<Unit> = runCatching {
        val key = db.push().key ?: UUID.randomUUID().toString()
        db.child(key).setValue(announcement.copy(id = key)).await()
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> = runCatching {
        db.child(id).removeValue().await()
    }
}
