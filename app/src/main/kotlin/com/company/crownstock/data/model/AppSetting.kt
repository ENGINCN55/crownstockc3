package com.company.crownstock.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore "appSettings" collection doküman modeli.
 * Bölüm 4.3.6 — Sistem geneli ayarlar (örn. global_low_stock_threshold).
 * settingKey doküman id'sidir (örn. "global_low_stock_threshold").
 *
 * Not: value alanı dokümanda "Any" olarak tanımlı. Firestore'da tip-güvenli
 * karşılığı yoktur; String/Number/Boolean gibi farklı ayar tiplerini tutabilmek
 * için Any olarak bırakıldı (ekstra tip eklenmedi).
 */
data class AppSetting(
    @DocumentId
    val settingKey: String = "",
    val value: Any? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
