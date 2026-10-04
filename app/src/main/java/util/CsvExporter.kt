package com.fieldsync.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.fieldsync.app.data.database.FormEntity
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

object CsvExporter {

    private val gson = Gson()

    fun export(context: Context, forms: List<FormEntity>): File {
        val dir = File(context.getExternalFilesDir(null), "exports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "fieldsync_export_${System.currentTimeMillis()}.csv")
        val sb = StringBuilder()
        sb.append("id,device_id,agent_name,client_name,form_type,status,created_at,synced_at,payload\n")
        forms.forEach { f ->
            sb.append(escape(f.id)).append(",")
            sb.append(escape(f.deviceId)).append(",")
            sb.append(escape(f.agentName)).append(",")
            sb.append(escape(f.clientName)).append(",")
            sb.append(escape(f.formType)).append(",")
            sb.append(escape(f.status)).append(",")
            sb.append(escape(f.createdAt)).append(",")
            sb.append(escape(f.syncedAt ?: "")).append(",")
            sb.append(escape(f.payload)).append("\n")
        }
        FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "com.fieldsync.app.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir CSV"))
    }

    private fun escape(value: String): String {
        val v = value.replace("\"", "\"\"")
        return "\"$v\""
    }
}
