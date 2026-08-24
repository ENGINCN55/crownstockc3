package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import kotlinx.coroutines.flow.Flow

/**
 * Bölüm 8 — ItemRepository: Ürün CRUD, stok sorgulama, düşük stok tespiti.
 * ItemDataSource'u sarmalar; şu an ek iş kuralı gerektirmediği için ince bir
 * (thin) katmandır — ileride doğrulama eklenirse burada yer alacaktır.
 */
class ItemRepository(private val itemDataSource: ItemDataSource) {

    suspend fun addItem(item: Item): String = itemDataSource.addItem(item)
    suspend fun updateItem(item: Item) = itemDataSource.updateItem(item)
    suspend fun deactivateItem(itemId: String) = itemDataSource.deactivateItem(itemId)
    suspend fun getItemById(itemId: String): Item? = itemDataSource.getItemById(itemId)
    suspend fun getItemsByType(itemType: ItemType): List<Item> = itemDataSource.getItemsByType(itemType)
    suspend fun searchItemsByName(query: String): List<Item> = itemDataSource.searchItemsByName(query)
    suspend fun getLowStockItems(): List<Item> = itemDataSource.getLowStockItems()
    fun observeItem(itemId: String): Flow<Item?> = itemDataSource.observeItem(itemId)
    fun observeItemsByType(itemType: ItemType): Flow<List<Item>> = itemDataSource.observeItemsByType(itemType)
}
