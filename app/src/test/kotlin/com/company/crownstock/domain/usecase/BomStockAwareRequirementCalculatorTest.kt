package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bölüm 34, öneri 8: "Hesaplama çekirdeği (BOM ağacı, eksik hesaplama, darboğaz)
 * Firestore bağımlılığından arındırılmış, saf fonksiyonlar olarak yazılmalı ve
 * birim testlerle doğrulanmalıdır."
 *
 * S1 KESİN KARARI senaryoları (kullanıcı onayı ile):
 * "Sistem her zaman önce mevcut yarı mamül stokunu kullanır."
 *
 * Örnek ağaç (Bölüm 15.1'deki örnekle birebir): Crown → Kart (x1) → M7 Diyot (x8)
 */
class BomStockAwareRequirementCalculatorTest {

    private val diyot = BomTreeNode(
        itemId = "diyot",
        itemType = ItemType.HAM_MADDE,
        requiredQuantityForOneParent = 8.0,
        children = emptyList()
    )
    private val kart = BomTreeNode(
        itemId = "kart",
        itemType = ItemType.YARI_MAMUL,
        requiredQuantityForOneParent = 1.0,
        children = listOf(diyot)
    )
    private val crown = BomTreeNode(
        itemId = "crown",
        itemType = ItemType.NIHAI_URUN,
        requiredQuantityForOneParent = 1.0,
        children = listOf(kart)
    )

    private fun item(id: String, stock: Double, type: ItemType = ItemType.HAM_MADDE) =
        Item(itemId = id, name = id, itemType = type, unit = "adet", currentStock = stock, minStockThreshold = 0.0)

    @Test
    fun `yari mamul stogu sifirsa tum ihtiyac ham maddeden turetilir`() {
        // Bölüm 15.1 örneği: 1 Crown için 8 diyot gerekir (Kart stoğu yok).
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 0.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val result = BomStockAwareRequirementCalculator.computeTerminalRequirements(crown, quantity = 1.0, itemsById = itemsById)
        assertEquals(8.0, result["diyot"]!!, 0.0001)
    }

    @Test
    fun `yari mamul stogu yeterliyse alt ham maddeye inilmez (S1)`() {
        // Kart stoğu (5) >= gereken (1) => diyot hiç tüketilmemeli, alt dala inilmemeli.
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 5.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val result = BomStockAwareRequirementCalculator.computeTerminalRequirements(crown, quantity = 1.0, itemsById = itemsById)
        assertTrue("Kart stoğu yeterliyken diyot ihtiyacı olmamalı", result.isEmpty() || result["diyot"] == null)
    }

    @Test
    fun `yari mamul stogu kismen yeterliyse sadece eksik kismin ham maddesi hesaplanir (S1)`() {
        // Kart gereken=3, stok=1 => eksik=2 => diyot ihtiyacı = 2 * 8 = 16.
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 1.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val result = BomStockAwareRequirementCalculator.computeTerminalRequirements(crown, quantity = 3.0, itemsById = itemsById)
        assertEquals(16.0, result["diyot"]!!, 0.0001)
    }

    @Test
    fun `computeConsumptionPlan hem yari mamul hem ham madde tuketimini iceriyor (Bolum 18 S1)`() {
        // Kart gereken=3, stok=1 => plan: kart'tan 1 (tamamı) + diyot'tan 16 (eksik * 8).
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 1.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val plan = BomStockAwareRequirementCalculator.computeConsumptionPlan(crown, quantity = 3.0, itemsById = itemsById)
        assertEquals(1.0, plan["kart"]!!, 0.0001)
        assertEquals(16.0, plan["diyot"]!!, 0.0001)
    }

    @Test
    fun `computeConsumptionPlan kart tamamen stoktan karsilaniyorsa diyot tuketilmez`() {
        val itemsById = mapOf(
            "crown" to item("crown", 0.0, ItemType.NIHAI_URUN),
            "kart" to item("kart", 10.0, ItemType.YARI_MAMUL),
            "diyot" to item("diyot", 100.0)
        )
        val plan = BomStockAwareRequirementCalculator.computeConsumptionPlan(crown, quantity = 2.0, itemsById = itemsById)
        assertEquals(2.0, plan["kart"]!!, 0.0001) // yalnızca gereken kadarı (2), stoktan.
        assertTrue("Kart yeterliyken diyot planında olmamalı", plan["diyot"] == null)
    }
}
