package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.StockMovementDataSource
import com.company.crownstock.data.model.Item
import com.company.crownstock.data.model.MovementType
import com.company.crownstock.data.model.StockMovement
import com.company.crownstock.domain.usecase.BomStockAwareRequirementCalculator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

private const val COLLECTION_ITEMS = "items"
private const val COLLECTION_STOCK_MOVEMENTS = "stockMovements"

class InsufficientStockException(val itemId: String, val available: Double, val required: Double) :
    IllegalStateException("Yetersiz stok: $itemId (mevcut=$available, gereken=$required)")

/**
 * Bölüm 8 — StockRepository: Stok hareketi oluşturma, stok düşüm/artış işlemleri
 * (Firestore Transaction yönetimi burada yapılır — Bölüm 18, 19).
 *
 * S1 KESİN KARARI (kullanıcı onayı ile, Bölüm 33 kapatıldı): confirmProduction()
 * artık BomStockAwareRequirementCalculator.computeConsumptionPlan kullanır —
 * yarı mamül stoğu önce tüketilir, yalnızca eksik kalan miktar için ham
 * maddelere inilir. Bu nedenle transaction artık yalnızca ham madde
 * dokümanlarını değil, ağaçtaki TÜM düğümlerin (yarı mamül dahil) Item
 * dokümanlarını okur/günceller.
 */
class StockRepository(
    private val firestore: FirebaseFirestore,
    private val calculationRepository: CalculationRepository,
    private val stockMovementDataSource: StockMovementDataSource
) {

    /**
     * Bölüm 25 — StockMovementHistoryScreen ve ItemDetailScreen (Bölüm 13-14, #3, #15)
     * için okuma metodu. Bölüm 8, StockRepository'nin "stok hareketi" sorumluluğu
     * taşıdığını belirtiyor; DataSource'a doğrudan delege eder, iş kuralı içermez.
     */
    suspend fun getMovementHistory(
        itemId: String,
        startDate: Date? = null,
        endDate: Date? = null,
        movementType: MovementType? = null
    ): List<StockMovement> = stockMovementDataSource.getMovementsByItem(itemId, startDate, endDate, movementType)

    /** Ekran #11 (Bölüm 13-14) — ProductionOrderResultScreen ihtiyacı. */
    suspend fun getMovementsForProductionOrder(productionOrderId: String): List<StockMovement> =
        stockMovementDataSource.getMovementsByProductionOrder(productionOrderId)

    /**
     * Bölüm 18 — Stok Düşüm Algoritması (S1 kesin kararına göre yeniden yazıldı).
     * Ön koşul: productionOrder CONFIRMED durumuna geçmiş olmalı (çağıran taraf —
     * ProductionRepository — bunu garanti eder).
     *
     * Adımlar:
     * 1. BOM ağacı derlenir (transaction DIŞINDA — topoloji nadiren değişir, Bölüm 27 R2).
     * 2. Ağaçtaki TÜM düğüm id'leri toplanır (yarı mamül + ham madde — S1 nedeniyle
     *    artık yalnızca ham maddeler değil, çünkü yarı mamül stoğu da tüketilebilir).
     * 3. Transaction içinde: bu id'lerin GÜNCEL stokları okunur (race condition koruması).
     * 4. S1 tüketim planı (computeConsumptionPlan) bu güncel stoklarla hesaplanır.
     * 5. Yeterlilik kontrolü: plandaki her miktar, o öğenin güncel stoğunu aşıyorsa iptal.
     * 6. Plandaki her öğe için stok düşülür + PRODUCTION_CONSUME hareketi yazılır
     *    (bu, hem kısmen/tamamen tüketilen yarı mamülleri HEM DE türetilen ham
     *    maddeleri kapsar).
     * 7. Hedef ürünün stoğu confirmedQuantity kadar artırılır + PRODUCTION_OUTPUT hareketi.
     */
    suspend fun confirmProduction(
        targetItemId: String,
        confirmedQuantity: Double,
        relatedProductionOrderId: String
    ) {
        // Adım 1.
        val root = calculationRepository.buildBomTree(targetItemId)

        // Adım 2: ağaçtaki tüm düğüm id'leri (kök = targetItemId dahil).
        val allTreeItemIds = calculationRepository.collectTreeItemIds(root)

        firestore.runTransaction { transaction ->
            val itemRefs = allTreeItemIds.associateWith { itemId ->
                firestore.collection(COLLECTION_ITEMS).document(itemId)
            }

            // Adım 3: güncel currentStock'lar transaction içinde okunur.
            val currentItems = itemRefs.mapValues { (id, ref) ->
                transaction.get(ref).toObject(Item::class.java)
                    ?: throw NoSuchElementException("Item bulunamadı: $id")
            }

            // Adım 4: S1 tüketim planı — yarı mamül stoğu önce kullanılır, yalnızca
            // eksik kalan miktar için alt ham maddelere inilir.
            val consumptionPlan = BomStockAwareRequirementCalculator.computeConsumptionPlan(
                root = root,
                quantity = confirmedQuantity,
                itemsById = currentItems
            )

            // Adım 5: yeterlilik kontrolü (S2 kararı: negatif stoğa asla izin verilmez).
            for ((itemId, required) in consumptionPlan) {
                val available = currentItems.getValue(itemId).currentStock
                if (available < required) {
                    throw InsufficientStockException(itemId, available, required)
                }
            }

            // Adım 6: her tüketilen öğe için stok düşülür + PRODUCTION_CONSUME hareketi.
            for ((itemId, consumedAmount) in consumptionPlan) {
                if (consumedAmount <= 0.0) continue
                val ref = itemRefs.getValue(itemId)
                val current = currentItems.getValue(itemId)
                val newStock = current.currentStock - consumedAmount
                transaction.update(ref, "currentStock", newStock, "updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())

                val movementRef = firestore.collection(COLLECTION_STOCK_MOVEMENTS).document()
                transaction.set(
                    movementRef,
                    mapOf(
                        "movementId" to movementRef.id,
                        "itemId" to itemId,
                        "movementType" to MovementType.PRODUCTION_CONSUME.name,
                        "quantity" to consumedAmount,
                        "resultingStock" to newStock,
                        "relatedProductionOrderId" to relatedProductionOrderId,
                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
            }

            // Adım 7: hedef ürünün stoğu artırılır + PRODUCTION_OUTPUT hareketi.
            // Not: targetItemId, consumptionPlan'da yer ALMAZ (kendisi tüketilmiyor,
            // üretiliyor) — bu yüzden currentItems'tan (transaction'da zaten okunmuştu,
            // ağacın kökü olduğu için allTreeItemIds içinde mevcut) ayrı okunur.
            val targetRef = itemRefs.getValue(targetItemId)
            val targetItem = currentItems.getValue(targetItemId)
            // Eğer targetItemId aynı zamanda consumptionPlan'da başka bir daldan
            // tüketilmiş olsaydı (teorik olarak imkansız — bir ürün kendi BOM'unun
            // parçası olamaz, Bölüm 16 dairesel referans koruması), stok tutarsız
            // olurdu; bu durum zaten BuildBomTreeUseCase'de engellenmiştir.
            val newTargetStock = targetItem.currentStock + confirmedQuantity
            transaction.update(targetRef, "currentStock", newTargetStock, "updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())

            val outputMovementRef = firestore.collection(COLLECTION_STOCK_MOVEMENTS).document()
            transaction.set(
                outputMovementRef,
                mapOf(
                    "movementId" to outputMovementRef.id,
                    "itemId" to targetItemId,
                    "movementType" to MovementType.PRODUCTION_OUTPUT.name,
                    "quantity" to confirmedQuantity,
                    "resultingStock" to newTargetStock,
                    "relatedProductionOrderId" to relatedProductionOrderId,
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )

            null
        }.await()

        // Adım 8: productionOrders kaydı status = COMPLETED (ProductionRepository sorumluluğunda).
    }

    /**
     * Bölüm 19 — Manuel Stok Giriş Algoritması.
     * movementType yalnızca MANUAL_IN / MANUAL_OUT olabilir.
     */
    suspend fun manualStockEntry(
        itemId: String,
        quantity: Double,
        movementType: MovementType,
        reason: String?,
        performedBy: String?
    ) {
        require(movementType == MovementType.MANUAL_IN || movementType == MovementType.MANUAL_OUT) {
            "manualStockEntry yalnızca MANUAL_IN / MANUAL_OUT ile çağrılabilir"
        }

        firestore.runTransaction { transaction ->
            val itemRef = firestore.collection(COLLECTION_ITEMS).document(itemId)
            // Adım 1 + 3a: item var mı / aktif mi kontrolü + currentStock okunur.
            val item = transaction.get(itemRef).toObject(Item::class.java)
                ?: throw NoSuchElementException("Item bulunamadı: $itemId")
            if (!item.isActive) {
                throw IllegalStateException("Item aktif değil: $itemId")
            }

            // Adım 2: MANUAL_OUT için quantity <= currentStock doğrulanır
            // (S2 kesin kararı: negatif stoğa asla izin verilmez).
            if (movementType == MovementType.MANUAL_OUT && quantity > item.currentStock) {
                throw InsufficientStockException(itemId, item.currentStock, quantity)
            }

            // 3b: yeni stok hesaplanır.
            val newStock = if (movementType == MovementType.MANUAL_IN) {
                item.currentStock + quantity
            } else {
                item.currentStock - quantity
            }
            transaction.update(itemRef, "currentStock", newStock, "updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())

            // 3c: yeni stockMovements kaydı (resultingStock ile).
            val movementRef = firestore.collection(COLLECTION_STOCK_MOVEMENTS).document()
            transaction.set(
                movementRef,
                mapOf(
                    "movementId" to movementRef.id,
                    "itemId" to itemId,
                    "movementType" to movementType.name,
                    "quantity" to quantity,
                    "resultingStock" to newStock,
                    "reason" to reason,
                    "performedBy" to performedBy,
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
            null
        }.await()

        // Adım 4 (düşük stok uyarısı gösterimi) UI/ViewModel katmanının sorumluluğundadır;
        // Repository yalnızca veri döndürür (Bölüm 8 ilkesi).
    }
}
