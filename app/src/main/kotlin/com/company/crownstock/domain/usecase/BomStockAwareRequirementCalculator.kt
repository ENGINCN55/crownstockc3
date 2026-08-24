package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode

/**
 * S1 — KESİN TASARIM KARARI (kullanıcı onayı ile, Bölüm 33 açık sorusu kapatıldı):
 *
 * "Sistem her zaman önce mevcut yarı mamül stokunu kullanır. Gereken miktar
 * stoktan tamamen karşılanabiliyorsa alt ham maddelere inilmez. Yetersizse,
 * yalnızca EKSİK kalan miktar için alt BOM açılır ve ham madde hesaplanır."
 *
 * Bu davranış Bölüm 18 (Stock Consume), Bölüm 20 (Max Producible), Bölüm 21
 * (Bottleneck) ve Bölüm 23'te (Multi-Level Shortage — zaten bu mantıkla
 * yazılmıştı) TUTARLI şekilde uygulanır. Bu dosya, o dört bölümün paylaştığı
 * ortak çekirdek algoritmadır (kod tekrarını önlemek için).
 *
 * NOT: Bu, BomTreeFlattener'ın (saf, stoktan bağımsız düzleştirme) yerini
 * ALIR — bkz. BomTreeFlattener.kt üzerindeki güncellenmiş "SÜPERSEDE" notu.
 */
object BomStockAwareRequirementCalculator {

    /**
     * "Terminal" düğüm: ya gerçek bir HAM_MADDE, ya da BOM'u tanımlanmamış
     * (children boş) bir YARI_MAMUL/NIHAI_URUN — bu durumda o düğümün kendi
     * stoğu aşılırsa daha derine inilemez, kendisi darboğaz noktası olur.
     */
    private fun isTerminal(node: BomTreeNode): Boolean =
        node.itemType == ItemType.HAM_MADDE || node.children.isEmpty()

    /**
     * itemId -> terminal düğümün, verilen quantity (kök için istenen adet) için
     * GEREKEN BRÜT miktarı (stoktan karşılanan kısım dahil, henüz "eksik" değil).
     * Aynı terminal öğe birden fazla dalda geçiyorsa toplanır.
     *
     * Kullanım: Bölüm 20 (Max Producible) ve Bölüm 21 (Bottleneck) — bu fonksiyon
     * ile stok karşılaştırılarak "missing" hesaplanır (bkz. missingAt).
     */
    fun computeTerminalRequirements(
        root: BomTreeNode,
        quantity: Double,
        itemsById: Map<String, Item>
    ): Map<String, Double> {
        val totals = mutableMapOf<String, Double>()
        for (child in root.children) {
            val childRequired = quantity * child.requiredQuantityForOneParent
            collectTerminalRequirements(child, childRequired, itemsById, totals)
        }
        return totals
    }

    private fun collectTerminalRequirements(
        node: BomTreeNode,
        requiredQuantity: Double,
        itemsById: Map<String, Item>,
        totals: MutableMap<String, Double>
    ) {
        if (isTerminal(node)) {
            totals[node.itemId] = (totals[node.itemId] ?: 0.0) + requiredQuantity
            return
        }
        // YARI_MAMUL/NIHAI_URUN, BOM'u var: stok-öncelikli düşüş.
        val available = itemsById[node.itemId]?.currentStock ?: 0.0
        if (available >= requiredQuantity) return // tamamen stoktan karşılanıyor, alta inilmez.
        val missing = requiredQuantity - available
        for (child in node.children) {
            collectTerminalRequirements(child, missing * child.requiredQuantityForOneParent, itemsById, totals)
        }
    }

    fun missingAt(itemId: String, requirement: Map<String, Double>, itemsById: Map<String, Item>): Double {
        val required = requirement[itemId] ?: return 0.0
        val available = itemsById[itemId]?.currentStock ?: 0.0
        return (required - available).coerceAtLeast(0.0)
    }

    /**
     * itemId -> O DÜĞÜMÜN KENDİ STOĞUNDAN çekilecek miktar (hem yarı mamül
     * kısmi/tam stok tüketimi, hem de türetilen ham madde miktarları dahil).
     * Bölüm 18 (Stock Consume) tarafından, transaction içinde currentStock
     * düşürmek ve PRODUCTION_CONSUME hareketleri yazmak için kullanılır.
     */
    fun computeConsumptionPlan(
        root: BomTreeNode,
        quantity: Double,
        itemsById: Map<String, Item>
    ): Map<String, Double> {
        val plan = mutableMapOf<String, Double>()
        for (child in root.children) {
            val childRequired = quantity * child.requiredQuantityForOneParent
            collectConsumptionPlan(child, childRequired, itemsById, plan)
        }
        return plan
    }

    private fun collectConsumptionPlan(
        node: BomTreeNode,
        requiredQuantity: Double,
        itemsById: Map<String, Item>,
        plan: MutableMap<String, Double>
    ) {
        if (isTerminal(node)) {
            // Terminal düğüm: kendi stoğundan (yeterliyse tamamı, değilse elindeki kadarı +
            // fiziksel olarak yetersiz kalan kısım) — Adım 4b'deki genel yeterlilik kontrolü
            // (Bölüm 18) bu planın toplamını gerçek stokla ayrıca karşılaştırır.
            plan[node.itemId] = (plan[node.itemId] ?: 0.0) + requiredQuantity
            return
        }
        val available = itemsById[node.itemId]?.currentStock ?: 0.0
        val usedFromStock = minOf(available, requiredQuantity)
        if (usedFromStock > 0) {
            plan[node.itemId] = (plan[node.itemId] ?: 0.0) + usedFromStock
        }
        val missing = requiredQuantity - usedFromStock
        if (missing > 0) {
            for (child in node.children) {
                collectConsumptionPlan(child, missing * child.requiredQuantityForOneParent, itemsById, plan)
            }
        }
    }
}
