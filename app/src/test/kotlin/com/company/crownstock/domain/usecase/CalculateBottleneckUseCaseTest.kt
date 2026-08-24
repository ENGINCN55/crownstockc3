package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateBottleneckUseCaseTest {

    private val useCase = CalculateBottleneckUseCase()

    private fun item(id: String, stock: Double, type: ItemType = ItemType.HAM_MADDE) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = stock, minStockThreshold = 0.0)

    // Crown → Kart (x1) → {Diyot (x8), Kondansator (x2)}
    private val diyot = BomTreeNode("diyot", ItemType.HAM_MADDE, 8.0, emptyList())
    private val kondansator = BomTreeNode("kondansator", ItemType.HAM_MADDE, 2.0, emptyList())
    private val kart = BomTreeNode("kart", ItemType.YARI_MAMUL, 1.0, listOf(diyot, kondansator))
    private val crown = BomTreeNode("crown", ItemType.NIHAI_URUN, 1.0, listOf(kart))

    @Test
    fun `en kisitlayici bileşen listenin basinda yer alir (kucukten buyuge siralama)`() {
        // Diyot: 80/8 = 10 adet destekler. Kondansator: 100/2 = 50 adet destekler.
        // Diyot daha kısıtlayıcı olmalı (10 < 50).
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 80.0),
            "kondansator" to item("kondansator", 100.0)
        )
        val result = useCase.execute(crown, itemsById)
        assertEquals(2, result.size)
        assertEquals("diyot", result.first().itemId)
        assertEquals(10.0, result.first().maxUnitsSupportedByThisItem, 0.0001)
        assertEquals("kondansator", result.last().itemId)
        assertEquals(50.0, result.last().maxUnitsSupportedByThisItem, 0.0001)
    }

    @Test
    fun `bilesensiz urun icin bos liste doner`() {
        val standalone = BomTreeNode("standalone", ItemType.NIHAI_URUN, 1.0, emptyList())
        assertTrue(useCase.execute(standalone, emptyMap()).isEmpty())
    }
}
