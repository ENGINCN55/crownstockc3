package com.company.crownstock.data.datasource

import com.company.crownstock.data.model.AppSetting
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val COLLECTION_APP_SETTINGS = "appSettings"

class AppSettingDataSourceImpl(
    private val firestore: FirebaseFirestore
) : AppSettingDataSource {

    private val collection = firestore.collection(COLLECTION_APP_SETTINGS)

    override suspend fun getSetting(settingKey: String): AppSetting? {
        return collection.document(settingKey).get().await().toObject(AppSetting::class.java)
    }

    override suspend fun setSetting(setting: AppSetting) {
        collection.document(setting.settingKey).set(setting).await()
    }
}
