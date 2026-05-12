package com.shalenammapride.data.repository

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.shalenammapride.data.model.Facility
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FacilityRepository {
    private val db = Firebase.database.reference.child("facilities")
    private val storage = Firebase.storage.reference.child("facilities")

    fun getFacilities(): Flow<List<Facility>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull {
                    it.getValue(Facility::class.java)
                }
                trySend(items)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        db.addValueEventListener(listener)
        awaitClose { db.removeEventListener(listener) }
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

    suspend fun addFacility(facility: Facility): Result<Unit> = runCatching {
        val key = db.push().key ?: UUID.randomUUID().toString()
        db.child(key).setValue(facility.copy(id = key)).await()
    }

    suspend fun deleteFacility(id: String): Result<Unit> = runCatching {
        db.child(id).removeValue().await()
    }
}
