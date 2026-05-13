package com.shalenammapride.data.repository

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.shalenammapride.data.model.AchievementComment
import com.shalenammapride.data.model.StudentAchievement
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AchievementRepository {
    private val db = Firebase.database.reference.child("achievements")
    private val storage = Firebase.storage.reference.child("achievements")
    private val reactionsRef = Firebase.database.reference.child("achievement_reactions")
    private val commentsRef = Firebase.database.reference.child("achievement_comments")

    fun getAchievements(): Flow<List<StudentAchievement>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(StudentAchievement::class.java) }
                    .sortedByDescending { it.achievementDate }
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
                snapshot.children.forEach { achSnap ->
                    val achId = achSnap.key ?: return@forEach
                    val votes = mutableMapOf<String, String>()
                    achSnap.children.forEach { d -> d.key?.let { k -> d.getValue(String::class.java)?.let { v -> votes[k] = v } } }
                    result[achId] = votes
                }
                trySend(result)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        reactionsRef.addValueEventListener(listener)
        awaitClose { reactionsRef.removeEventListener(listener) }
    }

    suspend fun setReaction(achievementId: String, deviceId: String) {
        reactionsRef.child(achievementId).child(deviceId).setValue("like").await()
    }

    suspend fun removeReaction(achievementId: String, deviceId: String) {
        reactionsRef.child(achievementId).child(deviceId).removeValue().await()
    }

    fun getComments(achievementId: String): Flow<List<AchievementComment>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = snapshot.children.mapNotNull { it.getValue(AchievementComment::class.java) }
                    .sortedBy { it.timestamp }
                trySend(comments)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        commentsRef.child(achievementId).addValueEventListener(listener)
        awaitClose { commentsRef.child(achievementId).removeEventListener(listener) }
    }

    suspend fun addComment(achievementId: String, comment: AchievementComment): Result<Unit> = runCatching {
        val key = commentsRef.child(achievementId).push().key ?: UUID.randomUUID().toString()
        commentsRef.child(achievementId).child(key).setValue(comment.copy(id = key)).await()
    }

    suspend fun deleteComment(achievementId: String, commentId: String): Result<Unit> = runCatching {
        commentsRef.child(achievementId).child(commentId).removeValue().await()
    }

    suspend fun uploadImage(imageBytes: ByteArray): String {
        val ref = storage.child("${UUID.randomUUID()}.jpg")
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadImage(uri: Uri): String {
        val ref = storage.child("${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun addAchievement(achievement: StudentAchievement): Result<Unit> = runCatching {
        val key = db.push().key ?: UUID.randomUUID().toString()
        db.child(key).setValue(achievement.copy(id = key)).await()
    }

    suspend fun deleteAchievement(id: String): Result<Unit> = runCatching {
        db.child(id).removeValue().await()
    }
}
