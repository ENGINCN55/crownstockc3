package com.company.crownstock.domain.model

/**
 * Domain-only model. Bölüm 9 — ProductionOrder/ShortageSnapshot içinde alt yapı
 * olarak kullanılır (hangi bileşenin, hangi BOM seviyesinde, ne kadar eksik olduğu).
 *
 * ÇELİŞKİ ÇÖZÜMÜ: Bölüm 4.3.5, "items" gibi bir collection'ın DEĞİL, doğrudan
 * shortageSnapshots.shortageDetails alanının şema tanımıdır (Kurallar: "dokümanın
 * tek doğruluk kaynağı" ve Firestore alan tanımları en spesifik kaynaktır) — bu
 * yüzden alan isimleri (requiredQty, availableQty, missingQty) 4.3.5'e göre esas
 * alındı. itemName, 4.3.5'te yer almadığı için Firestore'a YAZILMAYACAK, yalnızca
 * bellek içinde (UI gösterimi için) doldurulacak opsiyonel bir alan olarak eklendi
 * (mevcut alanlarla çakışmıyor, yeni bir iş kuralı getirmiyor).
 */
data class ShortageDetail(
    val itemId: String,
    val level: Int,
    val requiredQty: Double,
    val availableQty: Double,
    val missingQty: Double,
    val itemName: String? = null
)
