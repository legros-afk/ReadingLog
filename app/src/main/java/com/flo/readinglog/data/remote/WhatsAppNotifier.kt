package com.flo.readinglog.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppNotifier @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {
    @Serializable
    private data class SendRequest(val to: String, val message: String)

    suspend fun send(baseUrl: String, apiToken: String, to: String, message: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val body = json.encodeToString(SendRequest(to, message))
                    .toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("${baseUrl.trimEnd('/')}/send")
                    .post(body)
                    .header("Authorization", "Bearer $apiToken")
                    .build()
                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    error("HTTP ${response.code}: ${response.body?.string()}")
                }
            }
        }
}
