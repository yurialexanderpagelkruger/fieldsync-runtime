package com.fieldsync.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fieldsync.app.FieldSyncApplication

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as FieldSyncApplication
        return try {
            val pending = app.repository.countPending()
            if (pending == 0) return Result.success()
            val connected = app.repository.testConnection()
            if (!connected) return Result.retry()
            val result = app.repository.syncPending()
            if (result.failed > 0 && result.synced == 0) Result.retry() else Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
