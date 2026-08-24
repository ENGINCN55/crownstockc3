package com.company.crownstock.domain.usecase

import com.company.crownstock.data.model.ItemType
import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.domain.model.CapacityAnalysisResult

/**
 * Bölüm 35.3 — AnalyzeProductionCapacityUseCase.
 * Girdi: targetItemId, requestedQuantity Çıktı: CapacityAnalysisResult
 *
 * "Bu UseCase, hiçbir Firestore yazma işlemi tetiklemez. Yalnızca okuma tabanlı
 * Repository metodları çağrılır." Mevcut Bölüm 20/21/22 algoritmaları aynen
 * çağrılır; yeni bir hesaplama mantığı eklenmez.
 */
class AnalyzeProductionCapacityUseCase(
    private val calculationRepository: CalculationRepository,
    private val itemRepository: ItemRepository
) {

    suspend fun execute(targetItemId: String, requestedQuantity: Double): CapacityAnalysisResult {
        // Adım 1: Bölüm 20 — aynen çağrılır.
        val maxProducible = calculationRepository.calculateMaxProducible(targetItemId)

        // Adım 2.
        val isFullyFulfillable = maxProducible.maxProducibleQuantity >= requestedQuantity

        // Adım 3: Bölüm 21 — aynen çağrılır.
        val bottleneckRanking = calculationRepository.calculateBottleneckRanking(targetItemId)

        // Adım 4: Bölüm 22 — aynen çağrılır; yalnızca itemType = YARI_MAMUL olanlar filtrelenir.
        // Not: ShortageDetail (Bölüm 9) itemType alanı içermediği için, filtreleme burada
        // Item entity'sinden ayrıca sorgulanarak yapılır (model değiştirilmedi).
        val directShortages = calculationRepository.calculateDirectShortage(targetItemId, requestedQuantity)
        val missingSemiFinishedItems = directShortages.filter { detail ->
            itemRepository.getItemById(detail.itemId)?.itemType == ItemType.YARI_MAMUL
        }

        // Adım 6: birleştirilir.
        return CapacityAnalysisResult(
            targetItemId = targetItemId,
            requestedQuantity = requestedQuantity,
            isFullyFulfillable = isFullyFulfillable,
            maxProducibleQuantity = maxProducible.maxProducibleQuantity,
            bottleneckRanking = bottleneckRanking,
            missingSemiFinishedItems = missingSemiFinishedItems
        )
    }
}
