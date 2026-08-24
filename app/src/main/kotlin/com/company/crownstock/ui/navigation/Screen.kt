package com.company.crownstock.ui.navigation

/**
 * Bölüm 12 — Navigation Yapısı + Bölüm 13-14 Ekran Listesi.
 * Route bazlı, kavramsal graph doğrudan burada somutlaştırıldı.
 */
sealed class Screen(val route: String) {

    // MainGraph
    object Dashboard : Screen("dashboard")

    // ItemsGraph
    object ItemList : Screen("items")
    object ItemDetail : Screen("items/{itemId}") {
        fun createRoute(itemId: String) = "items/$itemId"
    }
    object ItemEdit : Screen("items/edit?itemId={itemId}") {
        fun createRoute(itemId: String? = null) = if (itemId != null) "items/edit?itemId=$itemId" else "items/edit"
    }
    object BomEditor : Screen("items/{itemId}/bom") {
        fun createRoute(itemId: String) = "items/$itemId/bom"
    }

    // StockGraph
    object StockOverview : Screen("stock")
    object ManualStockEntry : Screen("stock/manual-entry?itemId={itemId}") {
        fun createRoute(itemId: String? = null) =
            if (itemId != null) "stock/manual-entry?itemId=$itemId" else "stock/manual-entry"
    }

    // ProductionGraph
    object ProductionOrderCreate : Screen("production/create")
    object MaxProducibleCalculator : Screen("production/max-producible?itemId={itemId}") {
        fun createRoute(itemId: String? = null) =
            if (itemId != null) "production/max-producible?itemId=$itemId" else "production/max-producible"
    }
    object CapacityAnalysis : Screen("production/capacity-analysis")
    object CapacityAnalysisResult : Screen("production/capacity-analysis/result?itemId={itemId}&quantity={quantity}") {
        fun createRoute(itemId: String, quantity: Double) =
            "production/capacity-analysis/result?itemId=$itemId&quantity=$quantity"
    }
    object ProductionOrderConfirm : Screen("production/{orderId}/confirm") {
        fun createRoute(orderId: String) = "production/$orderId/confirm"
    }
    object ProductionOrderResult : Screen("production/{orderId}/result") {
        fun createRoute(orderId: String) = "production/$orderId/result"
    }

    // ShortageGraph
    object ShortageOverview : Screen("shortage?itemId={itemId}&quantity={quantity}") {
        fun createRoute(itemId: String? = null, quantity: Double? = null): String {
            val itemPart = itemId?.let { "itemId=$it" }
            val qtyPart = quantity?.let { "quantity=$it" }
            val query = listOfNotNull(itemPart, qtyPart).joinToString("&")
            return if (query.isEmpty()) "shortage" else "shortage?$query"
        }
    }
    object MultiLevelShortage : Screen("shortage/multi-level?itemId={itemId}&quantity={quantity}") {
        fun createRoute(itemId: String, quantity: Double) =
            "shortage/multi-level?itemId=$itemId&quantity=$quantity"
    }

    // ReportsGraph
    object PrintPreview : Screen("reports/print?reportType={reportType}&itemId={itemId}&quantity={quantity}&orderId={orderId}") {
        fun createRoute(reportType: String, itemId: String? = null, quantity: Double? = null, orderId: String? = null): String {
            val base = "reports/print?reportType=$reportType"
            val itemPart = itemId?.let { "&itemId=$it" } ?: ""
            val qtyPart = quantity?.let { "&quantity=$it" } ?: ""
            val orderPart = orderId?.let { "&orderId=$it" } ?: ""
            return base + itemPart + qtyPart + orderPart
        }
    }

    // HistoryGraph
    object StockMovementHistory : Screen("history?itemId={itemId}") {
        fun createRoute(itemId: String? = null) = if (itemId != null) "history?itemId=$itemId" else "history"
    }

    // Settings (Bölüm 13-14, ekran #16)
    object Settings : Screen("settings")
}
