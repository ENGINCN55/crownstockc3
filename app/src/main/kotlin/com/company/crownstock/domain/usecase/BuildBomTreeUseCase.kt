package com.company.crownstock.domain.usecase

import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode

/**
 * Bölüm 15.1 — BOM Ağacı Derleme Algoritması (Ortak Alt Rutin).
 * Girdi: targetItemId (kök ürün, örn. Crown)
 * Çıktı: BomTreeNode (kök düğüm, tüm alt ağacı içerir)
 *
 * Bu use case, diğer tüm hesaplama algoritmalarının (Bölüm 18, 20, 21, 22, 23)
 * ortak ön koşuludur.
 *
 * Not: BomTreeNode (Bölüm 9) yalnızca itemId, itemType, requiredQuantityForOneParent
 * ve children alanlarını içerir — currentStock/itemName gibi alanlar dokümanda
 * BomTreeNode'a tanımlanmamıştır (bu alanlar Item entity'sinde zaten var, Bölüm
 * 4.3.1). Bu yüzden ağaca yeni alan eklenmedi; hesaplama use case'leri, ağaçla
 * birlikte ayrıca fetchInvolvedItems() ile alınan bir Map<String, Item> kullanır.
 */
class BuildBomTreeUseCase(
    private val itemDataSource: ItemDataSource,
    private val bomDataSource: BomDataSource
) {

    class CircularBomReferenceException(val itemId: String) :
        IllegalStateException("Dairesel BOM referansı tespit edildi: $itemId")

    suspend fun execute(targetItemId: String): BomTreeNode {
        val rootItem = itemDataSource.getItemById(targetItemId)
            ?: throw NoSuchElementException("Item bulunamadı: $targetItemId")
        return buildNode(
            item = rootItem,
            requiredQuantityForOneParent = 1.0,
            ancestors = emptySet()
        )
    }

    private suspend fun buildNode(
        item: Item,
        requiredQuantityForOneParent: Double,
        ancestors: Set<String>
    ): BomTreeNode {
        // Adım 6: Sonsuz döngü koruması (dairesel referans).
        if (item.itemId in ancestors) {
            throw CircularBomReferenceException(item.itemId)
        }

        // Adım 5: HAM_MADDE düğümlerinde derinleşme durur (yaprak düğüm).
        if (item.itemType == ItemType.HAM_MADDE) {
            return BomTreeNode(
                itemId = item.itemId,
                itemType = item.itemType,
                requiredQuantityForOneParent = requiredQuantityForOneParent,
                children = emptyList()
            )
        }

        // Adım 2: parentItemId == item.itemId olan tüm bomComponents kayıtları getirilir.
        val components = bomDataSource.getComponentsByParent(item.itemId)
        val nextAncestors = ancestors + item.itemId

        // Adım 3-4: her kayıt için child düğüm eklenir; YARI_MAMUL ise recursive devam eder.
        val children = components.mapNotNull { component ->
            val childItem = itemDataSource.getItemById(component.childItemId) ?: return@mapNotNull null
            buildNode(
                item = childItem,
                requiredQuantityForOneParent = component.quantityPerUnit,
                ancestors = nextAncestors
            )
        }

        return BomTreeNode(
            itemId = item.itemId,
            itemType = item.itemType,
            requiredQuantityForOneParent = requiredQuantityForOneParent,
            children = children
        )
    }

    companion object {
        /**
         * Yardımcı: Ağaçtaki tüm itemId'leri (kök dahil) toplar. Hesaplama use case'lerinin
         * ihtiyaç duyduğu currentStock/name bilgisini tek seferde (Item entity üzerinden)
         * çekebilmek için kullanılır. Saf/statik bir fonksiyondur (Firestore'a bağımlı
         * değildir) — bu yüzden companion object'e taşındı; CalculationRepository gibi
         * çağıranların ayrı bir BuildBomTreeUseCase örneği tutmasına gerek kalmaz
         * (Bölüm 29.2 sekans diyagramıyla tutarlılık — bkz. CalculationRepository notu).
         */
        fun collectItemIds(node: BomTreeNode): Set<String> {
            return setOf(node.itemId) + node.children.flatMap { collectItemIds(it) }.toSet()
        }
    }
}
