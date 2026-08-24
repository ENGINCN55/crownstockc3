package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.domain.model.BomTreeNode
import com.company.crownstock.domain.model.BottleneckResult
import kotlin.math.floor

/**
 * Bölüm 21 — Darboğaz (Bottleneck) Hesaplama Algoritması.
 *
 * S1 KESİN KARARI SONRASI YENİDEN YAZILDI: Her terminal öğe (ham madde veya
 * BOM'suz yarı mamül) için "yalnızca bu öğe kısıtlıyor olsaydı en fazla kaç
 * adet üretilebilirdi" sorusu, BomStockAwareRequirementCalculator ile ikili
 * arama kullanılarak ayrı ayrı çözülür (S1 nedeniyle artık basit
 * floor(stok/birim) yeterli değil — bkz. CalculateMaxProducibleUseCase notu).
 */
class CalculateBottleneckUseCase {

    fun execute(root: BomTreeNode, itemsById: Map<String, Item>): List<BottleneckResult> {
        if (root.children.isEmpty()) return emptyList()

        // Ağaçta görünebilecek tüm terminal öğe id'lerini toplamak için yeterince
        // büyük bir Q'da bir kez hesapla (hangi öğelerin potansiyel terminal
        // olduğunu keşfetmek amacıyla; Q büyüdükçe yeni terminal öğe eklenmez,
        // yalnızca miktarlar büyür — ağacın yapısı Q'dan bağımsızdır).
        val allTerminalIds = BomStockAwareRequirementCalculator
            .computeTerminalRequirements(root, 1.0, itemsById)
            .keys

        fun maxQuantityConstrainedByItem(itemId: String): Double {
            fun feasibleForItem(quantity: Double): Boolean {
                val requirement = BomStockAwareRequirementCalculator.computeTerminalRequirements(root, quantity, itemsById)
                return BomStockAwareRequirementCalculator.missingAt(itemId, requirement, itemsById) <= 0.0
            }

            var hi = 1.0
            var iterations = 0
            while (feasibleForItem(hi) && iterations < 60) {
                hi *= 2
                iterations++
            }
            if (iterations >= 60) return floor(hi)

            var lo = 0.0
            repeat(100) {
                val mid = (lo + hi) / 2
                if (feasibleForItem(mid)) lo = mid else hi = mid
            }
            return floor(lo)
        }

        val results = allTerminalIds.map { itemId ->
            val item = itemsById[itemId]
            BottleneckResult(
                itemId = itemId,
                itemName = item?.name ?: "",
                availableStock = item?.currentStock ?: 0.0,
                // NOT: "requiredPerUnit" alanı (Bölüm 9) sabit bir orana dayanır; ancak S1
                // sonrası ihtiyaç artık Q'ya bağlı (doğrusal olmayan) olduğundan, burada
                // yalnızca gösterge amaçlı "1 adet için terminal ihtiyaç" değeri kullanılır
                // (yarı mamül stokları 1 adette genellikle tüketilmediği için gerçek eğilimi
                // yansıtmayabilir — asıl karşılaştırma maxUnitsSupportedByThisItem'dır).
                requiredPerUnit = BomStockAwareRequirementCalculator
                    .computeTerminalRequirements(root, 1.0, itemsById)[itemId] ?: 0.0,
                maxUnitsSupportedByThisItem = maxQuantityConstrainedByItem(itemId)
            )
        }

        return results.sortedBy { it.maxUnitsSupportedByThisItem }
    }
}
