package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.ProductionOrder
import com.company.crownstock.data.model.ProductionOrderStatus

/**
 * Bölüm 7 — ProductionOrderDataSource: Firestore "productionOrders" collection erişimi.
 */
interface ProductionOrderDataSource {

    // Üretim emri oluşturma / durum güncelleme
    suspend fun createOrder(order: ProductionOrder): String
    suspend fun updateOrderStatus(orderId: String, status: ProductionOrderStatus)

    // ID ile tekil üretim emri getirme
    suspend fun getOrderById(orderId: String): ProductionOrder?

    // Duruma göre listeleme (bekleyen, tamamlanan, iptal edilen)
    suspend fun getOrdersByStatus(status: ProductionOrderStatus): List<ProductionOrder>

    // NOT: Bölüm 7'de tanımlı değildi; DashboardScreen'in (Bölüm 13-14, #1)
    // "son üretim emirleri" gereksinimini karşılamak için eklendi.
    suspend fun getRecentOrders(limit: Int): List<ProductionOrder>
}
