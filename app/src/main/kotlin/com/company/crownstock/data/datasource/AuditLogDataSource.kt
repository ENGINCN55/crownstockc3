package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.AuditEntityType
import com.company.crownstock.data.model.AuditLogEntry

/**
 * Bölüm 7 — AuditLogDataSource: Firestore "auditLog" collection erişimi.
 */
interface AuditLogDataSource {

    // Yapısal değişiklik kaydı ekleme
    suspend fun addLogEntry(entry: AuditLogEntry): String

    // Entity bazlı geçmiş getirme
    suspend fun getLogsByEntity(entityType: AuditEntityType, entityId: String): List<AuditLogEntry>
}
