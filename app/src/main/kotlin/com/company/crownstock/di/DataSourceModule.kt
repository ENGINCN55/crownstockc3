package com.company.crownstock.di

import com.company.crownstock.data.datasource.AppSettingDataSource
import com.company.crownstock.data.datasource.AppSettingDataSourceImpl
import com.company.crownstock.data.datasource.AuditLogDataSource
import com.company.crownstock.data.datasource.AuditLogDataSourceImpl
import com.company.crownstock.data.datasource.BomDataSource
import com.company.crownstock.data.datasource.BomDataSourceImpl
import com.company.crownstock.data.datasource.ItemDataSource
import com.company.crownstock.data.datasource.ItemDataSourceImpl
import com.company.crownstock.data.datasource.ProductionOrderDataSource
import com.company.crownstock.data.datasource.ProductionOrderDataSourceImpl
import com.company.crownstock.data.datasource.ShortageSnapshotDataSource
import com.company.crownstock.data.datasource.ShortageSnapshotDataSourceImpl
import com.company.crownstock.data.datasource.StockMovementDataSource
import com.company.crownstock.data.datasource.StockMovementDataSourceImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Bölüm 7 — DataSource katmanı: arayüz -> Firestore implementasyonu bağlanır. */
@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideItemDataSource(firestore: FirebaseFirestore): ItemDataSource = ItemDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideBomDataSource(firestore: FirebaseFirestore): BomDataSource = BomDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideStockMovementDataSource(firestore: FirebaseFirestore): StockMovementDataSource =
        StockMovementDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideProductionOrderDataSource(firestore: FirebaseFirestore): ProductionOrderDataSource =
        ProductionOrderDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideShortageSnapshotDataSource(firestore: FirebaseFirestore): ShortageSnapshotDataSource =
        ShortageSnapshotDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideAuditLogDataSource(firestore: FirebaseFirestore): AuditLogDataSource =
        AuditLogDataSourceImpl(firestore)

    @Provides
    @Singleton
    fun provideAppSettingDataSource(firestore: FirebaseFirestore): AppSettingDataSource =
        AppSettingDataSourceImpl(firestore)
}
