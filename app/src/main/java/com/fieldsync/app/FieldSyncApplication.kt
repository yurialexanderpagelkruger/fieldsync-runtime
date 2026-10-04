package com.fieldsync.app

import android.app.Application
import com.fieldsync.app.data.database.AppDatabase
import com.fieldsync.app.data.network.RetrofitClient
import com.fieldsync.app.data.repository.FormRepository
import com.fieldsync.app.data.sync.SyncScheduler

class FieldSyncApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val api by lazy { RetrofitClient.create() }
    val repository by lazy { FormRepository(database.formDao(), api) }

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        SyncScheduler.schedulePeriodic(this)
    }
}
