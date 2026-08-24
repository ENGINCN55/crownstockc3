package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.ShortageSnapshot

/**
 * Bölüm 7 — ShortageSnapshotDataSource: Firestore "shortageSnapshots" collection
 * erişimi (opsiyonel — performans amaçlı cache, Bölüm 4.3.5).
 */
interface ShortageSnapshotDataSource {

    // Hesaplama sonucunu cache'leme
    suspend fun saveSnapshot(snapshot: ShortageSnapshot): String

    // Belirli ürün için son snapshot'ı getirme
    suspend fun getLatestSnapshot(targetItemId: String): ShortageSnapshot?
}
