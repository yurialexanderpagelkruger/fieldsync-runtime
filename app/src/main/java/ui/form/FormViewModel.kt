package com.fieldsync.app.ui.form

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldsync.app.FieldSyncApplication
import com.fieldsync.app.data.network.RetrofitClient
import com.fieldsync.app.data.repository.FormRepository
import com.fieldsync.app.data.repository.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FormViewModel : ViewModel() {

    private var repository: FormRepository? = null

    fun attach(app: FieldSyncApplication) {
        if (repository == null) {
            repository = FormRepository(app.database.formDao(), RetrofitClient.create())
        }
    }

    private fun repo(): FormRepository = repository
        ?: throw IllegalStateException("ViewModel no inicializado")

    fun saveForm(
        deviceId: String,
        agentName: String,
        clientName: String,
        formType: String,
        fields: Map<String, Any>,
        onDone: (String) -> Unit
    ) {
        viewModelScope.launch {
            val id = repo().saveLocal(deviceId, agentName, clientName, formType, fields)
            withContext(Dispatchers.Main) { onDone(id) }
        }
    }

    suspend fun pendingCount(): Int = repo().countPending()

    suspend fun syncNow(context: Context): SyncResult = repo().syncPending(context)
}
