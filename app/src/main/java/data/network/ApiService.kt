package com.fieldsync.app.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class SyncRecord(
    val id: String,
    val device_id: String,
    val agent_name: String,
    val client_name: String,
    val form_type: String,
    val payload: Map<String, Any>,
    val status: String,
    val created_at: String
)

data class SyncRequest(val records: List<SyncRecord>)

data class SyncResponse(
    val success: Boolean,
    val syncedCount: Int,
    val failedCount: Int,
    val synced: List<String>,
    val failed: List<Map<String, Any>>,
    val serverTime: String
)

data class PingResponse(val ok: Boolean, val serverTime: String)

interface ApiService {
    @GET("api/forms/ping")
    suspend fun ping(): Response<PingResponse>

    @POST("api/forms/sync")
    suspend fun sync(@Body request: SyncRequest): Response<SyncResponse>
}
