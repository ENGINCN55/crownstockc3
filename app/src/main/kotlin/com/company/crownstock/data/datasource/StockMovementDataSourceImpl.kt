package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.MovementType
import com.company.crownstock.data.model.StockMovement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

private const val COLLECTION_STOCK_MOVEMENTS = "stockMovements"

class StockMovementDataSourceImpl(
    private val firestore: FirebaseFirestore
) : StockMovementDataSource {

    private val collection = firestore.collection(COLLECTION_STOCK_MOVEMENTS)

    override suspend fun addMovement(movement: StockMovement): String {
        // IK-9: append-only — yalnızca ekleme, güncelleme/silme yok.
        val docRef = collection.document()
        val toSave = movement.copy(movementId = docRef.id)
        docRef.set(toSave).await()
        return docRef.id
    }

    override suspend fun getMovementsByItem(
        itemId: String,
        startDate: Date?,
        endDate: Date?,
        movementType: MovementType?
    ): List<StockMovement> {
        var query: Query = collection.whereEqualTo("itemId", itemId)
        if (movementType != null) {
            query = query.whereEqualTo("movementType", movementType.name)
        }
        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("timestamp", startDate)
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("timestamp", endDate)
        }
        return query.orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .toObjects(StockMovement::class.java)
    }

    override suspend fun getMovementsByProductionOrder(productionOrderId: String): List<StockMovement> {
        return collection
            .whereEqualTo("relatedProductionOrderId", productionOrderId)
            .get()
            .await()
            .toObjects(StockMovement::class.java)
    }
}
