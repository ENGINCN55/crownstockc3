package com.company.crownstock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.company.crownstock.ui.screens.bom.BomEditorScreen
import com.company.crownstock.ui.screens.capacity.CapacityAnalysisResultScreen
import com.company.crownstock.ui.screens.capacity.CapacityAnalysisScreen
import com.company.crownstock.ui.screens.dashboard.DashboardScreen
import com.company.crownstock.ui.screens.history.StockMovementHistoryScreen
import com.company.crownstock.ui.screens.items.ItemDetailScreen
import com.company.crownstock.ui.screens.items.ItemEditScreen
import com.company.crownstock.ui.screens.items.ItemListScreen
import com.company.crownstock.ui.screens.production.MaxProducibleCalculatorScreen
import com.company.crownstock.ui.screens.production.ProductionOrderConfirmScreen
import com.company.crownstock.ui.screens.production.ProductionOrderCreateScreen
import com.company.crownstock.ui.screens.production.ProductionOrderResultScreen
import com.company.crownstock.ui.screens.reports.PrintPreviewScreen
import com.company.crownstock.ui.screens.settings.SettingsScreen
import com.company.crownstock.ui.screens.shortage.MultiLevelShortageScreen
import com.company.crownstock.ui.screens.shortage.ShortageOverviewScreen
import com.company.crownstock.ui.screens.stock.ManualStockEntryScreen
import com.company.crownstock.ui.screens.stock.StockOverviewScreen

/**
 * Bölüm 12 — Navigation Yapısı: MainGraph (AuthGraph yok, Bölüm 26 — MVP'de
 * Authentication bulunmuyor). "Her ekran arası geçişte gerekli parametreler
 * (itemId, orderId vb.) navigation argümanı olarak taşınır."
 */
@Composable
fun CrownStockNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToItems = { navController.navigate(Screen.ItemList.route) },
                onNavigateToStock = { navController.navigate(Screen.StockOverview.route) },
                onNavigateToProductionCreate = { navController.navigate(Screen.ProductionOrderCreate.route) },
                onNavigateToCapacityAnalysis = { navController.navigate(Screen.CapacityAnalysis.route) },
                onNavigateToMaxProducible = { navController.navigate(Screen.MaxProducibleCalculator.createRoute()) },
                onNavigateToShortageOverview = { navController.navigate(Screen.ShortageOverview.createRoute()) },
                onNavigateToHistory = { navController.navigate(Screen.StockMovementHistory.createRoute()) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // ItemsGraph
        composable(Screen.ItemList.route) {
            ItemListScreen(
                onItemClick = { itemId -> navController.navigate(Screen.ItemDetail.createRoute(itemId)) },
                onAddItemClick = { navController.navigate(Screen.ItemEdit.createRoute()) }
            )
        }
        composable(
            Screen.ItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            ItemDetailScreen(
                onEditClick = { itemId -> navController.navigate(Screen.ItemEdit.createRoute(itemId)) },
                onBomEditorClick = { itemId -> navController.navigate(Screen.BomEditor.createRoute(itemId)) },
                onManualStockEntryClick = { itemId -> navController.navigate(Screen.ManualStockEntry.createRoute(itemId)) },
                onHistoryClick = { itemId -> navController.navigate(Screen.StockMovementHistory.createRoute(itemId)) },
                onMaxProducibleClick = { itemId -> navController.navigate(Screen.MaxProducibleCalculator.createRoute(itemId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.ItemEdit.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            ItemEditScreen(onSaved = { navController.popBackStack() })
        }
        composable(
            Screen.BomEditor.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            BomEditorScreen(onBack = { navController.popBackStack() })
        }

        // StockGraph
        composable(Screen.StockOverview.route) {
            StockOverviewScreen(
                onManualEntryClick = { navController.navigate(Screen.ManualStockEntry.createRoute()) },
                onPrintClick = { navController.navigate(Screen.PrintPreview.createRoute("STOCK_REPORT")) }
            )
        }
        composable(
            Screen.ManualStockEntry.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            ManualStockEntryScreen(onSaved = { navController.popBackStack() })
        }

        // ProductionGraph
        composable(Screen.ProductionOrderCreate.route) {
            ProductionOrderCreateScreen(
                onDraftCreated = { orderId -> navController.navigate(Screen.ProductionOrderConfirm.createRoute(orderId)) },
                onCheckMaxProducible = { itemId -> navController.navigate(Screen.MaxProducibleCalculator.createRoute(itemId)) },
                onCheckCapacityAnalysis = { navController.navigate(Screen.CapacityAnalysis.route) }
            )
        }
        composable(
            Screen.MaxProducibleCalculator.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            MaxProducibleCalculatorScreen(
                onOrderCreated = { orderId -> navController.navigate(Screen.ProductionOrderConfirm.createRoute(orderId)) }
            )
        }
        composable(Screen.CapacityAnalysis.route) {
            CapacityAnalysisScreen(
                onCheckCapacity = { itemId, qty -> navController.navigate(Screen.CapacityAnalysisResult.createRoute(itemId, qty)) },
                onViewDirectShortages = { itemId, qty -> navController.navigate(Screen.ShortageOverview.createRoute(itemId, qty)) }
            )
        }
        composable(
            Screen.CapacityAnalysisResult.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = checkNotNull(backStackEntry.arguments?.getString("itemId"))
            CapacityAnalysisResultScreen(
                onMissingSemiFinishedClick = { missingItemId, missingQty ->
                    navController.navigate(Screen.MultiLevelShortage.createRoute(missingItemId, missingQty))
                },
                onPrintClick = { navController.navigate(Screen.PrintPreview.createRoute("CAPACITY_ANALYSIS", itemId = itemId)) }
            )
        }
        composable(
            Screen.ProductionOrderConfirm.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = checkNotNull(backStackEntry.arguments?.getString("orderId"))
            ProductionOrderConfirmScreen(
                onConfirmed = { navController.navigate(Screen.ProductionOrderResult.createRoute(orderId)) }
            )
        }
        composable(
            Screen.ProductionOrderResult.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = checkNotNull(backStackEntry.arguments?.getString("orderId"))
            ProductionOrderResultScreen(
                onPrintClick = { navController.navigate(Screen.PrintPreview.createRoute("PRODUCTION_SUMMARY", orderId = orderId)) }
            )
        }

        // ShortageGraph
        composable(
            Screen.ShortageOverview.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("quantity") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            ShortageOverviewScreen(
                onComponentClick = { itemId, missingQty ->
                    navController.navigate(Screen.MultiLevelShortage.createRoute(itemId, missingQty))
                }
            )
        }
        composable(
            Screen.MultiLevelShortage.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = checkNotNull(backStackEntry.arguments?.getString("itemId"))
            val quantity = backStackEntry.arguments?.getString("quantity")?.toDoubleOrNull() ?: 0.0
            MultiLevelShortageScreen(
                onPrintClick = { navController.navigate(Screen.PrintPreview.createRoute("SHORTAGE_LIST", itemId = itemId, quantity = quantity)) }
            )
        }

        // ReportsGraph
        composable(
            Screen.PrintPreview.route,
            arguments = listOf(
                navArgument("reportType") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("quantity") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("orderId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            PrintPreviewScreen()
        }

        // HistoryGraph
        composable(
            Screen.StockMovementHistory.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            StockMovementHistoryScreen()
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
