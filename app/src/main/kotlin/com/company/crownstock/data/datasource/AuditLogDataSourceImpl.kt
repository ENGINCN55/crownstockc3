package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.AuditEntityType
import com.company.crownstock.data.model.AuditLogEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val COLLECTION_AUDIT_LOG = "auditLog"

class AuditLogDataSourceImpl(
    private val firestore: FirebaseFirestore
) : AuditLogDataSource {

    private val collection = firestore.collection(COLLECTION_AUDIT_LOG)

    override suspend fun addLogEntry(entry: AuditLogEntry): String {
        val docRef = collection.document()
        val toSave = entry.copy(logId = docRef.id)
        docRef.set(toSave).await()
        return docRef.id
    }

    override suspend fun getLogsByEntity(entityType: AuditEntityType, entityId: String): List<AuditLogEntry> {
        return collection
            .whereEqualTo("entityType", entityType.name)
            .whereEqualTo("entityId", entityId)
            .get()
            .await()
            .toObjects(AuditLogEntry::class.java)
    }
}
