package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.OpenClawSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<OpenClawSettings>
    suspend fun save(settings: OpenClawSettings)
}
