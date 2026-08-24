package com.company.crownstock.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class AuditEntityType {
    ITEM,
    BOM_COMPONENT
}

enum class AuditAction {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Firestore "auditLog" collection doküman modeli.
 * Bölüm 4.3.7 — Ürün/BOM tanım değişikliklerinin (silme, düzenleme) izlenmesi.
 * Stok hareketinden (stockMovements) ayrı, "yapısal" değişiklik logudur.
 */
data class AuditLogEntry(
    @DocumentId
    val logId: String = "",
    val entityType: AuditEntityType = AuditEntityType.ITEM,
    val entityId: String = "",
    val action: AuditAction = AuditAction.CREATE,
    // changedFields: değişen alanlar (eski/yeni değer) — dokümanda "Map" olarak
    // tanımlı, alt yapı detaylandırılmamış; genel Map<String, Any?> olarak bırakıldı.
    val changedFields: Map<String, Any?> = emptyMap(),
    // MVP'de kullanılmaz; V2.0'da aktif olur (Bölüm 4.3.7)
    val performedBy: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)
