package com.flo.readinglog.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flo.readinglog.domain.model.OpenClawSettings
import com.flo.readinglog.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named

class SettingsRepositoryImpl @Inject constructor(
    @Named("settings") private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    companion object {
        private val BASE_URL = stringPreferencesKey("open_claw_base_url")
        private val API_TOKEN = stringPreferencesKey("open_claw_api_token")
        private val PARENT_NUMBER = stringPreferencesKey("parent_whatsapp_number")
    }

    override val settings: Flow<OpenClawSettings> = dataStore.data.map { prefs ->
        OpenClawSettings(
            baseUrl = prefs[BASE_URL] ?: "",
            apiToken = prefs[API_TOKEN] ?: "",
            parentNumber = prefs[PARENT_NUMBER] ?: "",
        )
    }

    override suspend fun save(settings: OpenClawSettings) {
        dataStore.edit { prefs ->
            prefs[BASE_URL] = settings.baseUrl
            prefs[API_TOKEN] = settings.apiToken
            prefs[PARENT_NUMBER] = settings.parentNumber
        }
    }
}
