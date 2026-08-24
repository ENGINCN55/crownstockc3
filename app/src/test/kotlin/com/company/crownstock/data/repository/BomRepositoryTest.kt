package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.model.BomComponent
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
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

private class FakeBomDs(private val components: MutableList<BomComponent>) : BomDataSource {
    override suspend fun getComponentsByParent(parentItemId: String): List<BomComponent> =
        components.filter { it.parentItemId == parentItemId && it.isActive }
    override suspend fun getParentsByChild(childItemId: String): List<BomComponent> =
        components.filter { it.childItemId == childItemId && it.isActive }
    override suspend fun addBomComponent(bomComponent: BomComponent): String { components.add(bomComponent); return bomComponent.bomId }
    override suspend fun updateBomComponent(bomComponent: BomComponent) {}
    override suspend fun deactivateBomComponent(bomId: String) {}
}

/** Bölüm 16 — BOM Doğrulama Kuralları testleri. */
class BomRepositoryTest {

    private fun item(id: String, type: ItemType) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = 0.0, minStockThreshold = 0.0)

    @Test
    fun `ham madde parent olarak eklenemez`() = runBlocking {
        val items = mapOf(
            "diyot" to item("diyot", ItemType.HAM_MADDE),
            "kart" to item("kart", ItemType.YARI_MAMUL)
        )
        val repo = BomRepository(FakeBomDs(mutableListOf()), FakeItemDs(items))

        assertThrows(BomValidationException::class.java) {
            runBlocking {
                repo.addBomComponent(BomComponent(parentItemId = "diyot", childItemId = "kart", quantityPerUnit = 1.0, unit = "adet"))
            }
        }
    }

    @Test
    fun `quantityPerUnit sifir veya negatifse reddedilir`() = runBlocking {
        val items = mapOf(
            "crown" to item("crown", ItemType.NIHAI_URUN),
            "kart" to item("kart", ItemType.YARI_MAMUL)
        )
        val repo = BomRepository(FakeBomDs(mutableListOf()), FakeItemDs(items))

        assertThrows(BomValidationException::class.java) {
            runBlocking {
                repo.addBomComponent(BomComponent(parentItemId = "crown", childItemId = "kart", quantityPerUnit = 0.0, unit = "adet"))
            }
        }
    }

    @Test
    fun `dairesel referans reddedilir (A already child of B, B parent eklenmek isteniyor)`() = runBlocking {
        // Mevcut: A -> B (A'nın altında B var). Şimdi B -> A eklenmek isteniyor (döngü).
        val items = mapOf(
            "a" to item("a", ItemType.YARI_MAMUL),
            "b" to item("b", ItemType.YARI_MAMUL)
        )
        val components = mutableListOf(
            BomComponent(bomId = "b1", parentItemId = "a", childItemId = "b", quantityPerUnit = 1.0, unit = "adet")
        )
        val repo = BomRepository(FakeBomDs(components), FakeItemDs(items))

        assertThrows(BomValidationException::class.java) {
            runBlocking {
                repo.addBomComponent(BomComponent(parentItemId = "b", childItemId = "a", quantityPerUnit = 1.0, unit = "adet"))
            }
        }
    }

    @Test
    fun `gecerli bilesen basariyla eklenir`() = runBlocking {
        val items = mapOf(
            "crown" to item("crown", ItemType.NIHAI_URUN),
            "kart" to item("kart", ItemType.YARI_MAMUL)
        )
        val repo = BomRepository(FakeBomDs(mutableListOf()), FakeItemDs(items))

        val id = repo.addBomComponent(BomComponent(parentItemId = "crown", childItemId = "kart", quantityPerUnit = 2.0, unit = "adet"))
        org.junit.Assert.assertNotNull(id)
    }
}
