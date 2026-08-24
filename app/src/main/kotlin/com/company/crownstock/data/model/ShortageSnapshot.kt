package com.company.crownstock.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore "shortageSnapshots" collection doküman modeli (opsiyonel — performans cache'i).
 * Bölüm 4.3.5.
 *
 * !!! ÇELİŞKİ NOTU (kural: "Çelişki görürsen kod yazma, beni durdur"):
 * Bölüm 4.3.5, shortageDetails alanını ham Map listesi olarak şu key'lerle tanımlıyor:
 *   { itemId, requiredQty, availableQty, missingQty, level }
 * Bölüm 9 ise aynı yapıyı "ShortageDetail" adlı ayrı bir domain modeli olarak, FARKLI
 * alan isimleriyle tanımlıyor:
 *   { itemId, itemName, level, requiredQuantity, availableQuantity, missingQuantity }
 * (hem isimler kısaltılmış/tam hali farklı, hem de itemName Bölüm 4.3.5'te yok.)
 *
 * Bu dosyada Bölüm 4.3.5'in birebir tanımı (Map<String, Any>) kullanıldı çünkü o,
 * doğrudan bu collection'ın alan tanımı. domain/model/ShortageDetail.kt dosyası ise
 * Bölüm 9'daki adıyla ayrıca oluşturuldu. Firestore'a yazarken hangi şemanın esas
 * alınacağına (ve isimlerin birleştirilip birleştirilmeyeceğine) karar verilmeden
 * bu iki yapı arasında dönüştürme kodu YAZILMAYACAK.
 */
data class ShortageSnapshot(
    @DocumentId
    val snapshotId: String = "",
    val targetItemId: String = "",
    val requestedQuantity: Double = 0.0,
    // Bölüm 4.3.5'te tanımlandığı gibi ham Map listesi: {itemId, requiredQty, availableQty, missingQty, level}
    val shortageDetails: List<Map<String, Any>> = emptyList(),
    @ServerTimestamp
    val calculatedAt: Date? = null
)
