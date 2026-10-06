package com.fieldsync.app.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fieldsync.app.FieldSyncApplication
import com.fieldsync.app.R
import com.fieldsync.app.data.network.RetrofitClient
import com.fieldsync.app.data.repository.FormRepository
import com.fieldsync.app.data.sync.SyncScheduler
import com.fieldsync.app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etUrl.setText(RetrofitClient.getBaseUrl())

        binding.btnSaveUrl.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_url_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            RetrofitClient.setBaseUrl(url)
            Toast.makeText(this, getString(R.string.msg_url_saved), Toast.LENGTH_SHORT).show()
        }

        binding.btnTest.setOnClickListener {
            val app = application as FieldSyncApplication
            lifecycleScope.launch {
                val repo = FormRepository(
                    app.database.formDao(),
                    RetrofitClient.create()
                )
                val ok = repo.testConnection()
                Toast.makeText(
                    this@SettingsActivity,
                    if (ok) getString(R.string.msg_connection_ok) else getString(R.string.msg_connection_fail),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.btnSyncNow.setOnClickListener {
            SyncScheduler.runNow(this)
            Toast.makeText(this, getString(R.string.msg_sync_scheduled), Toast.LENGTH_SHORT).show()
        }
    }
}
