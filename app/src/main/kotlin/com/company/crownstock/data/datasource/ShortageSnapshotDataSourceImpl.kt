package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.ShortageSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val COLLECTION_SHORTAGE_SNAPSHOTS = "shortageSnapshots"

class ShortageSnapshotDataSourceImpl(
    private val firestore: FirebaseFirestore
) : ShortageSnapshotDataSource {

    private val collection = firestore.collection(COLLECTION_SHORTAGE_SNAPSHOTS)

    override suspend fun saveSnapshot(snapshot: ShortageSnapshot): String {
        val docRef = collection.document()
        val toSave = snapshot.copy(snapshotId = docRef.id)
        docRef.set(toSave).await()
        return docRef.id
    }

    override suspend fun getLatestSnapshot(targetItemId: String): ShortageSnapshot? {
        return collection
            .whereEqualTo("targetItemId", targetItemId)
            .orderBy("calculatedAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
            .toObjects(ShortageSnapshot::class.java)
            .firstOrNull()
    }
}
