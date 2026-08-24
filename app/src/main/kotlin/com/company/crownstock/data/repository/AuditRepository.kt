package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.AuditLogDataSource
import com.company.crownstock.data.model.AuditAction
import com.company.crownstock.data.model.AuditEntityType
import com.company.crownstock.data.model.AuditLogEntry

/**
 * Bölüm 8 — AuditRepository: Yapısal değişiklik loglama (Bölüm 25 — auditLog,
 * stockMovements'tan ayrı bir "yapısal" değişiklik logu).
 */
class AuditRepository(private val auditLogDataSource: AuditLogDataSource) {

    suspend fun logChange(
        entityType: AuditEntityType,
        entityId: String,
        action: AuditAction,
        changedFields: Map<String, Any?> = emptyMap()
    ) {
        auditLogDataSource.addLogEntry(
            AuditLogEntry(
                entityType = entityType,
                entityId = entityId,
                action = action,
                changedFields = changedFields
                // performedBy: MVP'de kullanılmaz (Bölüm 4.3.7)
            )
        )
    }

    suspend fun getLogsByEntity(entityType: AuditEntityType, entityId: String): List<AuditLogEntry> =
        auditLogDataSource.getLogsByEntity(entityType, entityId)
}
