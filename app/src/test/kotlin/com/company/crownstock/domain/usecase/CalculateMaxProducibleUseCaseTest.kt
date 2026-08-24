package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateMaxProducibleUseCaseTest {

    private val useCase = CalculateMaxProducibleUseCase()

    private fun item(id: String, stock: Double, type: ItemType = ItemType.HAM_MADDE) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = stock, minStockThreshold = 0.0)

    // Crown → Kart (x1) → Diyot (x8) — Bölüm 15.1 örneği.
    private val diyot = BomTreeNode("diyot", ItemType.HAM_MADDE, 8.0, emptyList())
    private val kart = BomTreeNode("kart", ItemType.YARI_MAMUL, 1.0, listOf(diyot))
    private val crown = BomTreeNode("crown", ItemType.NIHAI_URUN, 1.0, listOf(kart))

    @Test
    fun `yari mamul stogu yokken saf ham madde sinirina gore hesaplanir`() {
        // Kart stok=0, Diyot stok=100 => 100/8 = 12.5 => floor = 12
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val result = useCase.execute(crown, itemsById)
        assertEquals(12.0, result.maxProducibleQuantity, 0.0001)
        assertEquals("diyot", result.limitingItemId)
    }

    @Test
    fun `yari mamul stogu varken S1 geregi daha fazla adet uretilebilir`() {
        // Kart stok=5 (5 adet hazır Kart var, ham madde gerektirmez).
        // Diyot stok=100 => kalan (Q-5) adet Kart için 8*(Q-5) diyot gerekir.
        // 100 >= 8*(Q-5)  =>  Q-5 <= 12.5  =>  Q <= 17.5 => floor = 17
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 5.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val result = useCase.execute(crown, itemsById)
        assertEquals(17.0, result.maxProducibleQuantity, 0.0001)
    }

    @Test
    fun `bilesensiz urun sinirsiz kabul edilir`() {
        val standalone = BomTreeNode("standalone", ItemType.NIHAI_URUN, 1.0, emptyList())
        val result = useCase.execute(standalone, emptyMap())
        assertTrue(result.maxProducibleQuantity.isInfinite())
        assertNull(result.limitingItemId)
    }

    @Test
    fun `hicbir stok yoksa sifir uretilebilir`() {
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 0.0)
        )
        val result = useCase.execute(crown, itemsById)
        assertEquals(0.0, result.maxProducibleQuantity, 0.0001)
    }
}
