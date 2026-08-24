package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateMultiLevelShortageUseCaseTest {

    private val useCase = CalculateMultiLevelShortageUseCase()

    private fun item(id: String, stock: Double, type: ItemType = ItemType.HAM_MADDE) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = stock, minStockThreshold = 0.0)

    private val diyot = BomTreeNode("diyot", ItemType.HAM_MADDE, 8.0, emptyList())
    private val kart = BomTreeNode("kart", ItemType.YARI_MAMUL, 1.0, listOf(diyot))
    private val crown = BomTreeNode("crown", ItemType.NIHAI_URUN, 1.0, listOf(kart))

    @Test
    fun `yari mamul stogu yeterliyse eksik listesi bos doner (S1)`() {
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 10.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 0.0)
        )
        val shortages = useCase.executeAsFlatList(crown, requestedQuantity = 5.0, itemsById = itemsById)
        assertTrue("Kart yeterliyken hiçbir eksik olmamalı", shortages.isEmpty())
    }

    @Test
    fun `yari mamul stogu kismen yeterliyse sadece eksik kisim icin ham madde eksigi hesaplanir`() {
        // Kart gereken=5, stok=2 => eksik=3 => diyot gereken = 3*8=24, mevcut=10 => eksik diyot=14
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 2.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 10.0)
        )
        val shortages = useCase.executeAsFlatList(crown, requestedQuantity = 5.0, itemsById = itemsById)
        assertEquals(1, shortages.size)
        assertEquals("diyot", shortages[0].itemId)
        assertEquals(14.0, shortages[0].missingQty, 0.0001)
        assertEquals(2, shortages[0].level) // kart=seviye 1, diyot (kart'ın çocuğu)=seviye 2
    }

    @Test
    fun `agac gorunumu (tree) sadece eksik olan dallari icerir`() {
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val tree = useCase.executeAsTree(crown, requestedQuantity = 1.0, itemsById = itemsById)
        assertTrue("Diyot stoğu yeterliyken (100 >= 8) ağaç boş olmalı", tree.isEmpty())
    }

    @Test
    fun `agac gorunumu eksik oldugunda hiyerarsiyi korur`() {
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 0.0)
        )
        val tree = useCase.executeAsTree(crown, requestedQuantity = 1.0, itemsById = itemsById)
        assertEquals(1, tree.size)
        assertEquals("kart", tree[0].itemId)
        assertEquals(1, tree[0].children.size)
        assertEquals("diyot", tree[0].children[0].itemId)
        assertEquals(8.0, tree[0].children[0].missingQty, 0.0001)
    }

    @Test
    fun `TUTARLILIK regresyonu - BOM u olmayan yari mamulun kendisi terminal eksik olarak raporlanir`() {
        // "kart2" adında, hiç bomComponents kaydı olmayan (children=emptyList) bir
        // yarı mamül; stoğu yetersiz. Önceden bu sessizce kaybolurdu (bkz. bug fix).
        val kartSizBom = BomTreeNode("kart2", ItemType.YARI_MAMUL, 1.0, emptyList())
        val crownWithBomless = BomTreeNode("crown2", ItemType.NIHAI_URUN, 1.0, listOf(kartSizBom))
        val itemsById = mapOf(
            "crown2" to item("crown2", 0.0, ItemType.NIHAI_URUN),
            "kart2" to item("kart2", 2.0, ItemType.YARI_MAMUL)
        )
        val shortages = useCase.executeAsFlatList(crownWithBomless, requestedQuantity = 5.0, itemsById = itemsById)
        assertEquals(1, shortages.size)
        assertEquals("kart2", shortages[0].itemId)
        assertEquals(3.0, shortages[0].missingQty, 0.0001) // gereken 5, mevcut 2, eksik 3
    }
}
