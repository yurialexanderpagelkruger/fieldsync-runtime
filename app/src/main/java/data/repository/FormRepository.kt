package com.fieldsync.app.data.repository

import android.content.Context
import com.fieldsync.app.R
import com.fieldsync.app.data.database.FormDao
import com.fieldsync.app.data.database.FormEntity
import com.fieldsync.app.data.network.ApiService
import com.fieldsync.app.data.network.SyncRecord
import com.fieldsync.app.data.network.SyncRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class FormRepository(
    private val dao: FormDao,
    private val api: ApiService
) {

    private val gson = Gson()

    fun observeAll(): Flow<List<FormEntity>> = dao.observeAll()

    suspend fun getAll(): List<FormEntity> = dao.getAll()

    suspend fun getPending(): List<FormEntity> = dao.getPending()

    suspend fun countPending(): Int = dao.countPending()

    suspend fun getById(id: String): FormEntity? = dao.getById(id)

    suspend fun saveLocal(
        deviceId: String,
        agentName: String,
        clientName: String,
        formType: String,
        fields: Map<String, Any>
    ): String {
        val id = UUID.randomUUID().toString()
        val now = nowIso()
        val entity = FormEntity(
            id = id,
            deviceId = deviceId,
            agentName = agentName,
            clientName = clientName,
            formType = formType,
            payload = gson.toJson(fields),
            status = "pending",
            createdAt = now,
            syncedAt = null,
            updatedAt = now
        )
        dao.insert(entity)
        return id
    }

    suspend fun delete(id: String) = dao.deleteById(id)

    suspend fun syncPending(context: Context? = null): SyncResult {
        val pending = dao.getPending()
        if (pending.isEmpty()) {
            val msg = context?.getString(R.string.msg_sync_none) ?: "Sin registros pendientes"
            return SyncResult(0, 0, 0, msg)
        }

        val records = pending.map { entity ->
            @Suppress("UNCHECKED_CAST")
            val payload = gson.fromJson(entity.payload, Map::class.java) as Map<String, Any>
            SyncRecord(
                id = entity.id,
                device_id = entity.deviceId,
                agent_name = entity.agentName,
                client_name = entity.clientName,
                form_type = entity.formType,
                payload = payload,
                status = "synced",
                created_at = entity.createdAt
            )
        }

        return try {
            val response = api.sync(SyncRequest(records))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val now = nowIso()
                body.synced.forEach { id ->
                    dao.updateStatus(id, "synced", now, now)
                }
                body.failed.forEach { item ->
                    val id = item["id"]?.toString()
                    if (id != null) {
                        dao.incrementAttempts(id)
                        dao.updateStatus(id, "error", null, now)
                    }
                }
                val msg = context?.getString(R.string.msg_sync_success) ?: "Sincronizado"
                SyncResult(body.syncedCount, body.failedCount, pending.size, msg)
            } else {
                pending.forEach { dao.incrementAttempts(it.id) }
                val msg = context?.getString(R.string.msg_sync_server_error, response.code())
                    ?: "Error servidor ${response.code()}"
                SyncResult(0, pending.size, pending.size, msg)
            }
        } catch (e: Exception) {
            pending.forEach { dao.incrementAttempts(it.id) }
            val msg = context?.getString(R.string.msg_sync_error, e.message ?: "")
                ?: "Error: ${e.message}"
            SyncResult(0, pending.size, pending.size, msg)
        }
    }

    suspend fun testConnection(): Boolean = try {
        api.ping().isSuccessful
    } catch (e: Exception) {
        false
    }

    private fun nowIso(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }
}

data class SyncResult(
    val synced: Int,
    val failed: Int,
    val total: Int,
    val message: String
)
