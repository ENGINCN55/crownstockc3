package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.BomComponent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val COLLECTION_BOM_COMPONENTS = "bomComponents"

class BomDataSourceImpl(
    private val firestore: FirebaseFirestore
) : BomDataSource {

    private val collection = firestore.collection(COLLECTION_BOM_COMPONENTS)

    override suspend fun getComponentsByParent(parentItemId: String): List<BomComponent> {
        return collection
            .whereEqualTo("parentItemId", parentItemId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .toObjects(BomComponent::class.java)
    }

    override suspend fun getParentsByChild(childItemId: String): List<BomComponent> {
        return collection
            .whereEqualTo("childItemId", childItemId)
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .toObjects(BomComponent::class.java)
    }

    override suspend fun addBomComponent(bomComponent: BomComponent): String {
        val docRef = collection.document()
        val toSave = bomComponent.copy(bomId = docRef.id)
        docRef.set(toSave).await()
        return docRef.id
    }

    override suspend fun updateBomComponent(bomComponent: BomComponent) {
        collection.document(bomComponent.bomId).set(bomComponent).await()
    }

    override suspend fun deactivateBomComponent(bomId: String) {
        collection.document(bomId).update("isActive", false).await()
    }
}
