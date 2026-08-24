package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val COLLECTION_ITEMS = "items"

class ItemDataSourceImpl(
    private val firestore: FirebaseFirestore
) : ItemDataSource {

    private val collection = firestore.collection(COLLECTION_ITEMS)

    override suspend fun addItem(item: Item): String {
        val docRef = collection.document()
        val toSave = item.copy(itemId = docRef.id)
        docRef.set(toSave).await()
        return docRef.id
    }

    override suspend fun updateItem(item: Item) {
        collection.document(item.itemId).set(item).await()
    }

    override suspend fun deactivateItem(itemId: String) {
        collection.document(itemId).update("isActive", false).await()
    }

    override suspend fun getItemById(itemId: String): Item? {
        return collection.document(itemId).get().await().toObject(Item::class.java)
    }

    override suspend fun getItemsByType(itemType: ItemType): List<Item> {
        return collection
            .whereEqualTo("itemType", itemType.name)
            .get()
            .await()
            .toObjects(Item::class.java)
    }

    override suspend fun searchItemsByName(query: String): List<Item> {
        // Firestore prefix araması: ad alanında büyük/küçük harf duyarlı prefix eşleşmesi.
        return collection
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .await()
            .toObjects(Item::class.java)
    }

    override suspend fun getLowStockItems(): List<Item> {
        // Firestore, iki farklı alanı (currentStock < minStockThreshold) doğrudan
        // sunucu tarafında karşılaştıramaz; bu yüzden tüm aktif ürünler çekilip
        // istemci tarafında filtrelenir (Bölüm 4.3.1 tanımına göre).
        return collection
            .whereEqualTo("isActive", true)
            .get()
            .await()
            .toObjects(Item::class.java)
            .filter { it.currentStock < it.minStockThreshold }
    }

    override fun observeItem(itemId: String): Flow<Item?> = callbackFlow {
        val registration: ListenerRegistration = collection.document(itemId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Item::class.java))
            }
        awaitClose { registration.remove() }
    }

    override fun observeItemsByType(itemType: ItemType): Flow<List<Item>> = callbackFlow {
        val registration: ListenerRegistration = collection
            .whereEqualTo("itemType", itemType.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Item::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }
}
