package com.company.crownstock.domain.usecase

import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.model.BomComponent
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeItemDs(private val items: Map<String, Item>) : ItemDataSource {
    override suspend fun addItem(item: Item) = item.itemId
    override suspend fun updateItem(item: Item) {}
    override suspend fun deactivateItem(itemId: String) {}
    override suspend fun getItemById(itemId: String): Item? = items[itemId]
    override suspend fun getItemsByType(itemType: ItemType): List<Item> = items.values.filter { it.itemType == itemType }
    override suspend fun searchItemsByName(query: String): List<Item> = emptyList()
    override suspend fun getLowStockItems(): List<Item> = emptyList()
    override fun observeItem(itemId: String): Flow<Item?> = flowOf(items[itemId])
    override fun observeItemsByType(itemType: ItemType): Flow<List<Item>> = flowOf(emptyList())
}

private class FakeBomDs(private val components: List<BomComponent>) : BomDataSource {
    override suspend fun getComponentsByParent(parentItemId: String): List<BomComponent> =
        components.filter { it.parentItemId == parentItemId && it.isActive }
    override suspend fun getParentsByChild(childItemId: String): List<BomComponent> = emptyList()
    override suspend fun addBomComponent(bomComponent: BomComponent) = bomComponent.bomId
    override suspend fun updateBomComponent(bomComponent: BomComponent) {}
    override suspend fun deactivateBomComponent(bomId: String) {}
}

/** Bölüm 22 — tek seviye (recursive olmayan) eksik hesaplama testleri. */
class CalculateDirectShortageUseCaseTest {

    private fun item(id: String, stock: Double, type: ItemType = ItemType.HAM_MADDE) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = stock, minStockThreshold = 0.0)

    @Test
    fun `sadece dogrudan bilesenler dikkate alinir, alt seviyeye inilmez`() = runBlocking {
        // Crown -> Kart (x1, stok=0) -> Diyot (bu seviyeye HİÇ bakılmamalı)
        val items = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL)
        )
        val components = listOf(
            BomComponent(bomId = "b1", parentItemId = "crown", childItemId = "kart", quantityPerUnit = 1.0, unit = "adet")
        )
        val useCase = CalculateDirectShortageUseCase(FakeBomDs(components), FakeItemDs(items))

        val result = useCase.execute("crown", requestedQuantity = 3.0)
        assertEquals(1, result.size)
        assertEquals("kart", result[0].itemId)
        assertEquals(3.0, result[0].requiredQty, 0.0001) // 1 * 3
        assertEquals(3.0, result[0].missingQty, 0.0001) // stok 0 olduğu için tamamı eksik
        assertEquals(1, result[0].level)
    }

    @Test
    fun `yeterli stoklu bilesenler listede yer almaz`() = runBlocking {
        val items = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 100.0, ItemType.YARI_MAMUL)
        )
        val components = listOf(
            BomComponent(bomId = "b1", parentItemId = "crown", childItemId = "kart", quantityPerUnit = 1.0, unit = "adet")
        )
        val useCase = CalculateDirectShortageUseCase(FakeBomDs(components), FakeItemDs(items))

        val result = useCase.execute("crown", requestedQuantity = 3.0)
        assertTrue(result.isEmpty())
    }
}
