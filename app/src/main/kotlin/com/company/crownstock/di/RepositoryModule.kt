package com.company.crownstock.di

import com.company.crownstock.data.datasource.AppSettingDataSource
import com.company.crownstock.data.datasource.AuditLogDataSource
import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.datasource.ProductionOrderDataSource
import com.company.crownstock.data.datasource.StockMovementDataSource
import com.company.crownstock.data.repository.AuditRepository
import com.company.crownstock.data.repository.BomRepository
import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.data.repository.PrintRepository
import com.company.crownstock.data.repository.ProductionRepository
import com.company.crownstock.data.repository.SettingsRepository
import com.company.crownstock.data.repository.StockRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Bölüm 8 — Repository Katmanı Planı. */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideItemRepository(itemDataSource: ItemDataSource): ItemRepository = ItemRepository(itemDataSource)

    @Provides
    @Singleton
    fun provideBomRepository(
        bomDataSource: BomDataSource,
        itemDataSource: ItemDataSource
    ): BomRepository = BomRepository(bomDataSource, itemDataSource)

    @Provides
    @Singleton
    fun provideCalculationRepository(
        bomRepository: BomRepository,
        itemDataSource: ItemDataSource,
        bomDataSource: BomDataSource
    ): CalculationRepository = CalculationRepository(bomRepository, itemDataSource, bomDataSource)

    @Provides
    @Singleton
    fun provideStockRepository(
        firestore: FirebaseFirestore,
        calculationRepository: CalculationRepository,
        stockMovementDataSource: StockMovementDataSource
    ): StockRepository = StockRepository(firestore, calculationRepository, stockMovementDataSource)

    @Provides
    @Singleton
    fun provideProductionRepository(
        productionOrderDataSource: ProductionOrderDataSource,
        calculationRepository: CalculationRepository,
        stockRepository: StockRepository
    ): ProductionRepository = ProductionRepository(productionOrderDataSource, calculationRepository, stockRepository)

    @Provides
    @Singleton
    fun provideAuditRepository(auditLogDataSource: AuditLogDataSource): AuditRepository =
        AuditRepository(auditLogDataSource)

    @Provides
    @Singleton
    fun providePrintRepository(
        itemRepository: ItemRepository,
        calculationRepository: CalculationRepository
    ): PrintRepository = PrintRepository(itemRepository, calculationRepository)

    @Provides
    @Singleton
    fun provideSettingsRepository(appSettingDataSource: AppSettingDataSource): SettingsRepository =
        SettingsRepository(appSettingDataSource)
}
