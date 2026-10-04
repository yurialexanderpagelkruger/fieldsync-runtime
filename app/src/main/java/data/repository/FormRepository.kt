package com.fieldsync.app.data.repository

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

    suspend fun syncPending(): SyncResult {
        val pending = dao.getPending()
        if (pending.isEmpty()) return SyncResult(0, 0, 0, "Sin registros pendientes")

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
                SyncResult(body.syncedCount, body.failedCount, pending.size, "Sincronizado")
            } else {
                pending.forEach { dao.incrementAttempts(it.id) }
                SyncResult(0, pending.size, pending.size, "Error servidor ${response.code()}")
            }
        } catch (e: Exception) {
            pending.forEach { dao.incrementAttempts(it.id) }
            SyncResult(0, pending.size, pending.size, "Error: ${e.message}")
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
