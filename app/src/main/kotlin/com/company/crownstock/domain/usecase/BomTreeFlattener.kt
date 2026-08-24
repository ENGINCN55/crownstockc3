package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode

/**
 * ⚠️ SÜPERSEDE EDİLDİ (S1 kesin kararı, kullanıcı onayı ile — Bölüm 33 kapatıldı):
 * Bu dosya, yarı mamül stoğunu HİÇ dikkate almadan saf ham maddeye düzleştirme
 * yapıyordu. S1 kararı sonrası Bölüm 18/20/21 artık bu fonksiyonu KULLANMIYOR;
 * onun yerine BomStockAwareRequirementCalculator (stok-öncelikli türetme)
 * kullanılıyor. Bu dosya yalnızca tasarım geçmişinin izlenebilirliği için
 * (ve olası ileride "stoksuz teorik ihtiyaç" hesaplaması gerekirse) bırakıldı;
 * aktif olarak hiçbir yerden çağrılmıyor.
 *
 * Ortak alt rutin: Bölüm 18 (Adım 2), Bölüm 20 (Adım 1) ve Bölüm 21 (Adım 1) aynı
 * "ağacı ham maddelere kadar düzleştirme" mantığını tanımlıyor — bu yüzden tek yerde
 * toplanmıştır (kod tekrarından kaçınmak amacıyla, DAO/Repository kuralları dışında
 * bir mimari değişiklik değildir).
 *
 * "Örnek: Crown → Kart (x1) → M7 diyot (x8) ise, 1 Crown için M7 diyot ihtiyacı = 8."
 *
 * ÖNEMLİ SINIR: Bu fonksiyon, Bölüm 20 Adım 5 / Bölüm 33 Soru S1'de bahsedilen
 * "yarı mamülün kendi stoğunun kısmen karşılaması" durumunu HESABA KATMAZ — saf,
 * doğrudan ham maddeye düzleştirme yapar (Bölüm 18 Adım 2 ve Bölüm 20 Adım 1-4 ile
 * birebir). Yarı mamül stok durumunu dikkate alan algoritma Bölüm 23'te
 * (CalculateMultiLevelShortageUseCase) tam olarak tanımlanmıştı ve S1 kararı
 * sonrası artık BomStockAwareRequirementCalculator olarak tüm algoritmalarda
 * ortak kullanılıyor.
 */
object BomTreeFlattener {

    /**
     * itemId -> 1 adet kök ürün (root) üretmek için gereken toplam ham madde miktarı.
     * Aynı ham madde birden fazla dalda geçiyorsa miktarlar toplanır.
     */
    fun flattenToRawMaterialRequirementsPerUnit(root: BomTreeNode): Map<String, Double> {
        val totals = mutableMapOf<String, Double>()
        collect(root, cumulativeMultiplier = 1.0, totals = totals)
        return totals
    }

    private fun collect(node: BomTreeNode, cumulativeMultiplier: Double, totals: MutableMap<String, Double>) {
        if (node.itemType == ItemType.HAM_MADDE) {
            totals[node.itemId] = (totals[node.itemId] ?: 0.0) + cumulativeMultiplier
            return
        }
        for (child in node.children) {
            val childCumulative = cumulativeMultiplier * child.requiredQuantityForOneParent
            collect(child, childCumulative, totals)
        }
    }
}
