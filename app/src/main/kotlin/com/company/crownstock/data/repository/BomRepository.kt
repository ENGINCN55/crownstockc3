package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.model.BomComponent
import com.company.crownstock.data.model.ItemType
import com.company.crownstock.domain.model.BomTreeNode
import com.company.crownstock.domain.usecase.BuildBomTreeUseCase

class BomValidationException(message: String) : IllegalArgumentException(message)

/**
 * Bölüm 8 — BomRepository: BOM ağacı okuma/yazma, recursive BOM ağacı oluşturma.
 * Bölüm 16 — BOM Doğrulama Kuralları burada uygulanır (DataSource katmanı iş
 * kuralı içermez, Bölüm 8 ilkesi gereği).
 */
class BomRepository(
    private val bomDataSource: BomDataSource,
    private val itemDataSource: ItemDataSource
) {
    private val buildBomTreeUseCase = BuildBomTreeUseCase(itemDataSource, bomDataSource)

    /** Bir ürünün tüm alt ağacını tek seferde derler (Bölüm 15.1). */
    suspend fun getBomTree(rootItemId: String): BomTreeNode = buildBomTreeUseCase.execute(rootItemId)

    suspend fun getComponentsByParent(parentItemId: String): List<BomComponent> =
        bomDataSource.getComponentsByParent(parentItemId)

    /** "Nerede kullanılıyor" analizi (Bölüm 7 / 16). */
    suspend fun getParentsByChild(childItemId: String): List<BomComponent> =
        bomDataSource.getParentsByChild(childItemId)

    /**
     * Bölüm 16 — BOM Doğrulama Kuralları:
     * - HAM_MADDE tipi bir ürün asla parentItemId olarak yer alamaz.
     * - quantityPerUnit her zaman > 0 olmalıdır.
     * - Dairesel referans (A → B → A) oluşturulamaz.
     */
    suspend fun addBomComponent(bomComponent: BomComponent): String {
        val parent = itemDataSource.getItemById(bomComponent.parentItemId)
            ?: throw BomValidationException("Üst ürün bulunamadı: ${bomComponent.parentItemId}")
        if (parent.itemType == ItemType.HAM_MADDE) {
            throw BomValidationException("HAM_MADDE tipi bir ürün BOM'da üst ürün (parent) olamaz: ${parent.itemId}")
        }
        if (bomComponent.quantityPerUnit <= 0) {
            throw BomValidationException("quantityPerUnit > 0 olmalıdır: ${bomComponent.quantityPerUnit}")
        }
        if (wouldCreateCircularReference(bomComponent.parentItemId, bomComponent.childItemId)) {
            throw BomValidationException(
                "Dairesel BOM referansı: ${bomComponent.parentItemId} -> ${bomComponent.childItemId}"
            )
        }
        return bomDataSource.addBomComponent(bomComponent)
    }

    suspend fun updateBomComponent(bomComponent: BomComponent) {
        if (bomComponent.quantityPerUnit <= 0) {
            throw BomValidationException("quantityPerUnit > 0 olmalıdır: ${bomComponent.quantityPerUnit}")
        }
        bomDataSource.updateBomComponent(bomComponent)
    }

    suspend fun deactivateBomComponent(bomId: String) = bomDataSource.deactivateBomComponent(bomId)

    /**
     * childItemId'nin, eklenmek istenen parentItemId'nin zaten bir atası olup
     * olmadığını kontrol eder (A → B eklenirken B'nin altında zaten A varsa, döngü oluşur).
     */
    private suspend fun wouldCreateCircularReference(
        newParentId: String,
        newChildId: String,
        visited: MutableSet<String> = mutableSetOf()
    ): Boolean {
        if (newChildId == newParentId) return true
        if (!visited.add(newChildId)) return false
        val childOfNewChild = bomDataSource.getComponentsByParent(newChildId)
        for (component in childOfNewChild) {
            if (component.childItemId == newParentId) return true
            if (wouldCreateCircularReference(newParentId, component.childItemId, visited)) return true
        }
        return false
    }
}
