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
