package com.company.crownstock.data.repository

import com.company.crownstock.data.datasource.AppSettingDataSource
import com.company.crownstock.data.model.AppSetting

/**
 * NOT: Bölüm 8'in repository tablosunda ayrıca listelenmemiş (dokümanın eksiği,
 * AppSettingDataSource gibi). Ancak Bölüm 10'daki mimari kural açık:
 * "ViewModel asla doğrudan Firestore'a erişmez, her zaman Repository üzerinden
 * geçer." SettingsScreen (Bölüm 13-14, #16) appSettings collection'ına
 * (Bölüm 4.3.6) erişmek zorunda olduğundan, bu ince (thin) repository — diğer
 * repository'lerle aynı desende — eklendi.
 */
class SettingsRepository(private val appSettingDataSource: AppSettingDataSource) {
    suspend fun getSetting(settingKey: String): AppSetting? = appSettingDataSource.getSetting(settingKey)
    suspend fun setSetting(setting: AppSetting) = appSettingDataSource.setSetting(setting)
}
