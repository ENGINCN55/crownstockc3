package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode
import com.company.crownstock.domain.model.ShortageDetail
import kotlin.math.max

/**
 * Bölüm 23 — Çok Seviyeli Eksik Hesaplama Algoritması.
 * Girdi: root (targetItemId için derlenmiş BomTreeNode), requestedQuantity, itemsById
 * Çıktı: List<ShortageDetail> — yalnızca eksik (missingQty > 0) ham maddeler.
 *
 * Bu, Bölüm 18/20'de bahsedilen S1 tasarım kararının somut/uygulanmış halidir:
 * bir yarı mamül stokta yeterliyse alt dala inilmez; yetersizse yalnızca EKSİK
 * kısım için alt ham maddelere inilir (Adım 3c/3d).
 *
 * NOT (eksik alan): Bölüm 23 Adım 3e, her eksik kaydın "hangi üst yarı mamül
 * üzerinden geldiği" (parentChain) bilgisiyle birlikte tutulmasını istiyor, ancak
 * ShortageDetail modeli (Bölüm 9) böyle bir alan içermiyor. Model değiştirilmediği
 * için (kural: dokümanda yazmayan alan eklenmez) bu bilgi, aşağıdaki
 * executeAsTree() ile ayrı bir ağaç yapısında sağlanıyor; executeAsFlatList()
 * ise Bölüm 23 Adım 4'teki "düzleştirilmiş liste" görünümünü, ShortageDetail'in
 * mevcut alanlarıyla üretir.
 */
class CalculateMultiLevelShortageUseCase {

    /** Adım 4 — "düzleştirilmiş liste" görünümü: tüm eksik ham maddeler tek listede. */
    fun executeAsFlatList(
        root: BomTreeNode,
        requestedQuantity: Double,
        itemsById: Map<String, Item>
    ): List<ShortageDetail> {
        val results = mutableListOf<ShortageDetail>()
        for (child in root.children) {
            val childRequired = requestedQuantity * child.requiredQuantityForOneParent
            traverse(child, childRequired, level = 1, itemsById = itemsById, results = results)
        }
        return results
    }

    /**
     * Adım 4 — "ağaç görünümü": hangi yarı mamül eksik → onun hangi ham maddesi
     * eksik, parentChain bilgisi korunarak. ShortageTreeNode, Bölüm 9'da tanımlı
     * olmayan yardımcı bir sunum yapısıdır (Firestore'a yazılmaz, yalnızca UI içindir).
     */
    fun executeAsTree(
        root: BomTreeNode,
        requestedQuantity: Double,
        itemsById: Map<String, Item>
    ): List<ShortageTreeNode> {
        return root.children.mapNotNull { child ->
            val childRequired = requestedQuantity * child.requiredQuantityForOneParent
            buildTreeNode(child, childRequired, level = 1, itemsById = itemsById)
        }
    }

    private fun traverse(
        node: BomTreeNode,
        requiredQuantity: Double,
        level: Int,
        itemsById: Map<String, Item>,
        results: MutableList<ShortageDetail>
    ) {
        val item = itemsById[node.itemId]
        val availableQuantity = item?.currentStock ?: 0.0

        when (node.itemType) {
            ItemType.YARI_MAMUL -> {
                // 3c: stokta yeterliyse dal burada durur, alt ham maddeler tüketilmez.
                if (availableQuantity >= requiredQuantity) return
                // 3d: eksik miktar, alt ham maddelerden karşılanacak varsayılır.
                val missing = requiredQuantity - availableQuantity
                // TUTARLILIK DÜZELTMESİ: BOM'u tanımlanmamış (children boş) bir yarı
                // mamülün eksikliği, alt ham maddeye inilemediği için sessizce
                // kaybolmamalı — kendisi terminal eksik olarak kaydedilir
                // (bkz. BomStockAwareRequirementCalculator.isTerminal ile aynı kural).
                if (node.children.isEmpty()) {
                    results.add(
                        ShortageDetail(
                            itemId = node.itemId,
                            level = level,
                            requiredQty = requiredQuantity,
                            availableQty = availableQuantity,
                            missingQty = missing,
                            itemName = item?.name
                        )
                    )
                    return
                }
                for (child in node.children) {
                    val childRequired = missing * child.requiredQuantityForOneParent
                    traverse(child, childRequired, level + 1, itemsById, results)
                }
            }
            ItemType.HAM_MADDE -> {
                // 3e: doğrudan eksik hesabı.
                val missingQuantity = max(0.0, requiredQuantity - availableQuantity)
                if (missingQuantity > 0) {
                    results.add(
                        ShortageDetail(
                            itemId = node.itemId,
                            level = level,
                            requiredQty = requiredQuantity,
                            availableQty = availableQuantity,
                            missingQty = missingQuantity,
                            itemName = item?.name
                        )
                    )
                }
            }
            ItemType.NIHAI_URUN -> {
                // BOM ağacında NIHAI_URUN yalnızca kök olabilir (Bölüm 16); alt düğümlerde
                // beklenmez. Güvenlik amaçlı: yarı mamül gibi ele alınır (aynı tutarlılık
                // düzeltmesi burada da uygulanır).
                if (availableQuantity >= requiredQuantity) return
                val missing = requiredQuantity - availableQuantity
                if (node.children.isEmpty()) {
                    results.add(
                        ShortageDetail(
                            itemId = node.itemId,
                            level = level,
                            requiredQty = requiredQuantity,
                            availableQty = availableQuantity,
                            missingQty = missing,
                            itemName = item?.name
                        )
                    )
                    return
                }
                for (child in node.children) {
                    traverse(child, missing * child.requiredQuantityForOneParent, level + 1, itemsById, results)
                }
            }
        }
    }

    private fun buildTreeNode(
        node: BomTreeNode,
        requiredQuantity: Double,
        level: Int,
        itemsById: Map<String, Item>
    ): ShortageTreeNode? {
        val item = itemsById[node.itemId]
        val availableQuantity = item?.currentStock ?: 0.0

        if (node.itemType == ItemType.HAM_MADDE) {
            val missingQuantity = max(0.0, requiredQuantity - availableQuantity)
            if (missingQuantity <= 0) return null
            return ShortageTreeNode(
                itemId = node.itemId,
                itemName = item?.name,
                level = level,
                requiredQty = requiredQuantity,
                availableQty = availableQuantity,
                missingQty = missingQuantity,
                children = emptyList()
            )
        }

        // YARI_MAMUL / NIHAI_URUN
        if (availableQuantity >= requiredQuantity) return null
        val missing = requiredQuantity - availableQuantity

        // TUTARLILIK DÜZELTMESİ: BOM'u tanımlanmamış (children boş) düğüm, alt ham
        // maddeye inilemediği için kendisi yaprak-eksik düğümü olarak döner (traverse()
        // ile aynı kural — bkz. yukarısı).
        if (node.children.isEmpty()) {
            return ShortageTreeNode(
                itemId = node.itemId,
                itemName = item?.name,
                level = level,
                requiredQty = requiredQuantity,
                availableQty = availableQuantity,
                missingQty = missing,
                children = emptyList()
            )
        }

        val childNodes = node.children.mapNotNull { child ->
            buildTreeNode(child, missing * child.requiredQuantityForOneParent, level + 1, itemsById)
        }
        if (childNodes.isEmpty()) return null
        return ShortageTreeNode(
            itemId = node.itemId,
            itemName = item?.name,
            level = level,
            requiredQty = requiredQuantity,
            availableQty = availableQuantity,
            missingQty = missing,
            children = childNodes
        )
    }
}

/**
 * UI-yardımcı ağaç sunum modeli (Bölüm 23 Adım 4 — "ağaç görünümü" için).
 * Firestore'a karşılık gelmez; Bölüm 9'daki resmi model listesine ek değildir,
 * yalnızca MultiLevelShortageScreen'in ağaç görünümünü render edebilmesi içindir.
 */
data class ShortageTreeNode(
    val itemId: String,
    val itemName: String?,
    val level: Int,
    val requiredQty: Double,
    val availableQty: Double,
    val missingQty: Double,
    val children: List<ShortageTreeNode>
)
