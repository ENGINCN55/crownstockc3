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
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Bölüm 15.1 birim testleri — BOM ağacı derleme + Bölüm 16 dairesel referans koruması
 * (Adım 6). Gerçek Firestore olmadan test edilebilmesi için (Bölüm 34, öneri 8)
 * bellek-içi sahte (fake) DataSource implementasyonları kullanılır.
 */
private class FakeItemDataSource(private val items: MutableMap<String, Item>) : ItemDataSource {
    override suspend fun addItem(item: Item): String { items[item.itemId] = item; return item.itemId }
    override suspend fun updateItem(item: Item) { items[item.itemId] = item }
    override suspend fun deactivateItem(itemId: String) { items[itemId] = items.getValue(itemId).copy(isActive = false) }
    override suspend fun getItemById(itemId: String): Item? = items[itemId]
    override suspend fun getItemsByType(itemType: ItemType): List<Item> = items.values.filter { it.itemType == itemType }
    override suspend fun searchItemsByName(query: String): List<Item> = items.values.filter { it.name.contains(query) }
    override suspend fun getLowStockItems(): List<Item> = items.values.filter { it.currentStock < it.minStockThreshold }
    override fun observeItem(itemId: String): Flow<Item?> = flowOf(items[itemId])
    override fun observeItemsByType(itemType: ItemType): Flow<List<Item>> = flowOf(items.values.filter { it.itemType == itemType })
}

private class FakeBomDataSource(private val components: MutableList<BomComponent>) : BomDataSource {
    override suspend fun getComponentsByParent(parentItemId: String): List<BomComponent> =
        components.filter { it.parentItemId == parentItemId && it.isActive }
    override suspend fun getParentsByChild(childItemId: String): List<BomComponent> =
        components.filter { it.childItemId == childItemId && it.isActive }
    override suspend fun addBomComponent(bomComponent: BomComponent): String { components.add(bomComponent); return bomComponent.bomId }
    override suspend fun updateBomComponent(bomComponent: BomComponent) {
        components.removeAll { it.bomId == bomComponent.bomId }; components.add(bomComponent)
    }
    override suspend fun deactivateBomComponent(bomId: String) {
        val idx = components.indexOfFirst { it.bomId == bomId }
        if (idx >= 0) components[idx] = components[idx].copy(isActive = false)
    }
}

class BuildBomTreeUseCaseTest {

    private fun item(id: String, type: ItemType, stock: Double = 0.0) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = stock, minStockThreshold = 0.0)

    @Test
    fun `basit agac dogru derlenir (Bolum 15_1 ornegi)`() = runBlocking {
        val items = mutableMapOf(
            "crown" to item("crown", ItemType.NIHAI_URUN),
            "kart" to item("kart", ItemType.YARI_MAMUL),
            "diyot" to item("diyot", ItemType.HAM_MADDE)
        )
        val components = mutableListOf(
            BomComponent(bomId = "b1", parentItemId = "crown", childItemId = "kart", quantityPerUnit = 1.0, unit = "adet"),
            BomComponent(bomId = "b2", parentItemId = "kart", childItemId = "diyot", quantityPerUnit = 8.0, unit = "adet")
        )
        val useCase = BuildBomTreeUseCase(FakeItemDataSource(items), FakeBomDataSource(components))

        val root = useCase.execute("crown")

        assertEquals("crown", root.itemId)
        assertEquals(1, root.children.size)
        assertEquals("kart", root.children[0].itemId)
        assertEquals(1, root.children[0].children.size)
        assertEquals("diyot", root.children[0].children[0].itemId)
        assertEquals(8.0, root.children[0].children[0].requiredQuantityForOneParent, 0.0001)
    }

    @Test
    fun `dairesel referans tespit edilirse istisna firlatilir (Bolum 16 Adim 6)`() = runBlocking {
        val items = mutableMapOf(
            "a" to item("a", ItemType.YARI_MAMUL),
            "b" to item("b", ItemType.YARI_MAMUL)
        )
        // A -> B -> A (dairesel)
        val components = mutableListOf(
            BomComponent(bomId = "b1", parentItemId = "a", childItemId = "b", quantityPerUnit = 1.0, unit = "adet"),
            BomComponent(bomId = "b2", parentItemId = "b", childItemId = "a", quantityPerUnit = 1.0, unit = "adet")
        )
        val useCase = BuildBomTreeUseCase(FakeItemDataSource(items), FakeBomDataSource(components))

        assertThrows(BuildBomTreeUseCase.CircularBomReferenceException::class.java) {
            runBlocking { useCase.execute("a") }
        }
    }

    @Test
    fun `ham madde yaprak dugumde derinlesme durur`() = runBlocking {
        val items = mutableMapOf("diyot" to item("diyot", ItemType.HAM_MADDE))
        val useCase = BuildBomTreeUseCase(FakeItemDataSource(items), FakeBomDataSource(mutableListOf()))

        val root = useCase.execute("diyot")
        assertEquals(0, root.children.size)
    }

    @Test
    fun `collectItemIds agactaki tum id leri toplar`() = runBlocking {
        val items = mutableMapOf(
            "crown" to item("crown", ItemType.NIHAI_URUN),
            "kart" to item("kart", ItemType.YARI_MAMUL),
            "diyot" to item("diyot", ItemType.HAM_MADDE)
        )
        val components = mutableListOf(
            BomComponent(bomId = "b1", parentItemId = "crown", childItemId = "kart", quantityPerUnit = 1.0, unit = "adet"),
            BomComponent(bomId = "b2", parentItemId = "kart", childItemId = "diyot", quantityPerUnit = 8.0, unit = "adet")
        )
        val useCase = BuildBomTreeUseCase(FakeItemDataSource(items), FakeBomDataSource(components))
        val root = useCase.execute("crown")

        val ids = BuildBomTreeUseCase.collectItemIds(root)
        assertEquals(setOf("crown", "kart", "diyot"), ids)
    }
}
