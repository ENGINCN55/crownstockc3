package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.ProductionOrder
import com.company.crownstock.data.model.ProductionOrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val COLLECTION_PRODUCTION_ORDERS = "productionOrders"

class ProductionOrderDataSourceImpl(
    private val firestore: FirebaseFirestore
) : ProductionOrderDataSource {

    private val collection = firestore.collection(COLLECTION_PRODUCTION_ORDERS)

    override suspend fun createOrder(order: ProductionOrder): String {
        val docRef = collection.document()
        val toSave = order.copy(orderId = docRef.id)
        docRef.set(toSave).await()
        return docRef.id
    }

    override suspend fun updateOrderStatus(orderId: String, status: ProductionOrderStatus) {
        collection.document(orderId).update("status", status.name).await()
    }

    override suspend fun getOrderById(orderId: String): ProductionOrder? {
        return collection.document(orderId).get().await().toObject(ProductionOrder::class.java)
    }

    override suspend fun getOrdersByStatus(status: ProductionOrderStatus): List<ProductionOrder> {
        return collection
            .whereEqualTo("status", status.name)
            .get()
            .await()
            .toObjects(ProductionOrder::class.java)
    }

    override suspend fun getRecentOrders(limit: Int): List<ProductionOrder> {
        return collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(ProductionOrder::class.java)
    }
}
