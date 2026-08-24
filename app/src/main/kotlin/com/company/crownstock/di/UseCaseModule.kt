package com.company.crownstock.di

import com.company.crownstock.data.repository.CalculationRepository
import com.company.crownstock.data.repository.ItemRepository
import com.company.crownstock.domain.usecase.AnalyzeProductionCapacityUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Bölüm 35.3 — yeni tek orkestrasyon use case'i. */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideAnalyzeProductionCapacityUseCase(
        calculationRepository: CalculationRepository,
        itemRepository: ItemRepository
    ): AnalyzeProductionCapacityUseCase = AnalyzeProductionCapacityUseCase(calculationRepository, itemRepository)
}
