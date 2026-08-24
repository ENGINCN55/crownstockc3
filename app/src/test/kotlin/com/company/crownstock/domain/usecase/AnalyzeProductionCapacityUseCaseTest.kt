package com.company.crownstock.domain.usecase

import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.model.BomComponent
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.BomRepository
import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.data.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeItemDs(private val items: MutableMap<String, Item>) : ItemDataSource {
    override suspend fun addItem(item: Item): String { items[item.itemId] = item; return item.itemId }
    override suspend fun updateItem(item: Item) { items[item.itemId] = item }
    override suspend fun deactivateItem(itemId: String) {}
    override suspend fun getItemById(itemId: String): Item? = items[itemId]
    override suspend fun getItemsByType(itemType: ItemType): List<Item> = items.values.filter { it.itemType == itemType }
    override suspend fun searchItemsByName(query: String): List<Item> = emptyList()
    override suspend fun getLowStockItems(): List<Item> = items.values.filter { it.currentStock < it.minStockThreshold }
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

/**
 * Bölüm 35.3 — AnalyzeProductionCapacityUseCase uçtan uca testi.
 * "Bu UseCase hiçbir Firestore yazma işlemi tetiklemez" — bu nedenle gerçek
 * Firestore olmadan, yalnızca fake DataSource'larla tam akış test edilebilir.
 */
class AnalyzeProductionCapacityUseCaseTest {

    private fun item(id: String, stock: Double, type: ItemType, threshold: Double = 0.0) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = stock, minStockThreshold = threshold)

    @Test
    fun `tam karsilanabilir senaryoda isFullyFulfillable true doner`() = runBlocking {
        val items = mutableMapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0, ItemType.HAM_MADDE)
        )
        val components = listOf(
            BomComponent(bomId = "b1", parentItemId = "crown", childItemId = "kart", quantityPerUnit = 1.0, unit = "adet"),
            BomComponent(bomId = "b2", parentItemId = "kart", childItemId = "diyot", quantityPerUnit = 8.0, unit = "adet")
        )
        val itemDs = FakeItemDs(items)
        val bomDs = FakeBomDs(components)
        val bomRepository = BomRepository(bomDs, itemDs)
        val calculationRepository = CalculationRepository(bomRepository, itemDs, bomDs)
        val itemRepository = ItemRepository(itemDs)
        val useCase = AnalyzeProductionCapacityUseCase(calculationRepository, itemRepository)

        // 5 Crown için 40 diyot gerekir, 100 mevcut => tam karşılanabilir.
        val result = useCase.execute("crown", 5.0)

        assertTrue(result.isFullyFulfillable)
        assertEquals(12.0, result.maxProducibleQuantity, 0.0001) // 100/8=12.5 -> floor 12
        assertTrue(result.missingSemiFinishedItems.isEmpty())
    }

    @Test
    fun `karsilanamayan senaryoda darbogaz siralamasi ve eksik yari mamuller dolu doner`() = runBlocking {
        val items = mutableMapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 10.0, ItemType.HAM_MADDE)
        )
        val components = listOf(
            BomComponent(bomId = "b1", parentItemId = "crown", childItemId = "kart", quantityPerUnit = 1.0, unit = "adet"),
            BomComponent(bomId = "b2", parentItemId = "kart", childItemId = "diyot", quantityPerUnit = 8.0, unit = "adet")
        )
        val itemDs = FakeItemDs(items)
        val bomDs = FakeBomDs(components)
        val bomRepository = BomRepository(bomDs, itemDs)
        val calculationRepository = CalculationRepository(bomRepository, itemDs, bomDs)
        val itemRepository = ItemRepository(itemDs)
        val useCase = AnalyzeProductionCapacityUseCase(calculationRepository, itemRepository)

        // 5 Crown için 40 diyot gerekir, yalnızca 10 mevcut => karşılanamaz.
        val result = useCase.execute("crown", 5.0)

        assertFalse(result.isFullyFulfillable)
        assertEquals(1.0, result.maxProducibleQuantity, 0.0001) // 10/8=1.25 -> floor 1
        assertEquals(1, result.bottleneckRanking.size)
        assertEquals("diyot", result.bottleneckRanking.first().itemId)
        // Bölüm 22 (tek seviye) bakış açısıyla: crown'un DOĞRUDAN bileşeni olan
        // "kart" (YARI_MAMUL, stok=0) eksik listede yer almalı.
        assertEquals(1, result.missingSemiFinishedItems.size)
        assertEquals("kart", result.missingSemiFinishedItems.first().itemId)
    }
}
