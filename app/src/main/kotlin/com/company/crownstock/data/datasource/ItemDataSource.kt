package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import kotlinx.coroutines.flow.Flow

/**
 * Bölüm 7 — ItemDataSource: DAO benzeri, yalnızca Firestore "items" collection
 * erişimi. İş kuralı / hesaplama İÇERMEZ (bkz. Bölüm 8 — Repository katmanı kuralı).
 */
interface ItemDataSource {

    // Ürün ekleme / güncelleme / pasife alma
    suspend fun addItem(item: Item): String
    suspend fun updateItem(item: Item)
    suspend fun deactivateItem(itemId: String)

    // ID ile tekil ürün getirme
    suspend fun getItemById(itemId: String): Item?

    // Tipe göre (HAM_MADDE / YARI_MAMUL / NIHAI_URUN) listeleme
    suspend fun getItemsByType(itemType: ItemType): List<Item>

    // İsme göre arama
    suspend fun searchItemsByName(query: String): List<Item>

    // Düşük stoklu ürünleri listeleme (currentStock < minStockThreshold)
    suspend fun getLowStockItems(): List<Item>

    // Gerçek zamanlı stok değişikliği dinleme (snapshot listener)
    fun observeItem(itemId: String): Flow<Item?>
    fun observeItemsByType(itemType: ItemType): Flow<List<Item>>
}
