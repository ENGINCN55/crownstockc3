package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.AppSetting

/**
 * NOT: Bölüm 7 (DataSource katmanı), Bölüm 4.3.6'da tanımlanan "appSettings"
 * collection'ı için bir DataSource arayüzü içermiyor (dokümanın kendi eksiği).
 * Bölüm 4.3.6'nın alan tanımına sadık kalınarak, diğer DataSource'larla aynı
 * desende (yalnızca CRUD, iş kuralı yok) minimal olarak tamamlandı — yeni bir
 * özellik eklenmedi, yalnızca zaten var olan collection için eksik erişim katmanı
 * oluşturuldu.
 */
interface AppSettingDataSource {
    suspend fun getSetting(settingKey: String): AppSetting?
    suspend fun setSetting(setting: AppSetting)
}
