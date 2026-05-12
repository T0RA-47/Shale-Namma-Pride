package com.shalenammapride.data.repository

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.shalenammapride.data.model.MealUpdate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MealRepository {
    private val db = Firebase.database.reference.child("meals")
    private val storage = Firebase.storage.reference.child("meals")
    private val reactionsRef = Firebase.database.reference.child("meal_reactions")

    fun getMeals(): Flow<List<MealUpdate>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meals = snapshot.children.mapNotNull {
                    it.getValue(MealUpdate::class.java)
                }.sortedByDescending { it.uploadDate }
                trySend(meals)
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
                snapshot.children.forEach { mealSnap ->
                    val mealId = mealSnap.key ?: return@forEach
                    val votes = mutableMapOf<String, String>()
                    mealSnap.children.forEach { deviceSnap ->
                        val deviceId = deviceSnap.key ?: return@forEach
                        val vote = deviceSnap.getValue(String::class.java) ?: return@forEach
                        votes[deviceId] = vote
                    }
                    result[mealId] = votes
                }
                trySend(result)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        reactionsRef.addValueEventListener(listener)
        awaitClose { reactionsRef.removeEventListener(listener) }
    }

    suspend fun setReaction(mealId: String, deviceId: String, vote: String) {
        reactionsRef.child(mealId).child(deviceId).setValue(vote).await()
    }

    suspend fun removeReaction(mealId: String, deviceId: String) {
        reactionsRef.child(mealId).child(deviceId).removeValue().await()
    }

    suspend fun uploadMealImage(imageBytes: ByteArray): String {
        val ref = storage.child("${UUID.randomUUID()}.jpg")
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadMealImage(uri: Uri): String {
        val ref = storage.child("${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun addMeal(meal: MealUpdate): Result<Unit> = runCatching {
        val key = db.push().key ?: UUID.randomUUID().toString()
        db.child(key).setValue(meal.copy(id = key)).await()
    }

    suspend fun deleteMeal(id: String): Result<Unit> = runCatching {
        db.child(id).removeValue().await()
    }
}
