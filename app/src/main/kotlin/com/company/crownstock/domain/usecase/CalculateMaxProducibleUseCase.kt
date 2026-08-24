package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.domain.model.BomTreeNode
import com.company.crownstock.domain.model.MaxProducibleResult
import kotlin.math.floor

/**
 * Bölüm 20 — Üretilebilir Maksimum Adet Hesaplama Algoritması.
 *
 * S1 KESİN KARARI SONRASI YENİDEN YAZILDI: Artık basit "floor(stok/birim başına
 * ihtiyaç)" YETERLİ DEĞİL, çünkü yarı mamül stoğu S1 kararı gereği önce
 * kullanılıyor ve tükendiği noktada ihtiyaç eğrisi kırılıyor (parçalı-doğrusal,
 * ama artık düz bir doğru değil). Bu yüzden ikili arama (binary search) ile
 * "hangi Q değerinde herhangi bir terminal (ham madde veya BOM'suz yarı mamül)
 * öğesi yetersiz kalıyor" sorusu çözülüyor — BomStockAwareRequirementCalculator
 * üzerinden (S1 çekirdek algoritması, Bölüm 18/21/23 ile paylaşılıyor).
 */
class CalculateMaxProducibleUseCase {

    fun execute(root: BomTreeNode, itemsById: Map<String, Item>): MaxProducibleResult {
        if (root.children.isEmpty()) {
            // BOM'u olmayan (bileşensiz) bir ürün — dokümanda tanımlanmamış özel durum;
            // önceki davranışla tutarlı: sınırsız kabul edilir.
            return MaxProducibleResult(root.itemId, Double.POSITIVE_INFINITY, null)
        }

        fun isFeasible(quantity: Double): Boolean {
            val requirement = BomStockAwareRequirementCalculator.computeTerminalRequirements(root, quantity, itemsById)
            return requirement.keys.all { itemId ->
                BomStockAwareRequirementCalculator.missingAt(itemId, requirement, itemsById) <= 0.0
            }
        }

        // Üst sınırı bul (ikiye katlayarak) — infeasible olduğu noktaya kadar.
        var hi = 1.0
        var iterations = 0
        while (isFeasible(hi) && iterations < 60) {
            hi *= 2
            iterations++
        }
        if (iterations >= 60) {
            // Güvenlik sınırı: 60 katlamadan sonra hâlâ karşılanabiliyorsa (örn. tüm
            // terminal öğelerin stoğu pratikte tükenmiyor), pratik bir üst sınır olarak
            // bu değer döndürülür — dokümanda bu uç durum tanımlanmamıştır.
            return MaxProducibleResult(root.itemId, floor(hi), null)
        }

        // İkili arama: lo her zaman feasible, hi her zaman infeasible.
        var lo = 0.0
        repeat(100) {
            val mid = (lo + hi) / 2
            if (isFeasible(mid)) lo = mid else hi = mid
        }

        val maxProducibleQuantity = floor(lo)

        // Sınırlayıcı (limiting) öğe: bir sonraki birim için hangi terminal öğe(ler)
        // eksik kalıyor (Bölüm 20 Adım 3-4 ile aynı amaç: darboğaz adayını bulmak).
        val probeQuantity = maxProducibleQuantity + 1
        val probeRequirement = BomStockAwareRequirementCalculator.computeTerminalRequirements(root, probeQuantity, itemsById)
        val limitingItemId = probeRequirement.keys
            .map { it to BomStockAwareRequirementCalculator.missingAt(it, probeRequirement, itemsById) }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }
            ?.first

        return MaxProducibleResult(
            targetItemId = root.itemId,
            maxProducibleQuantity = maxProducibleQuantity,
            limitingItemId = limitingItemId
        )
    }
}
