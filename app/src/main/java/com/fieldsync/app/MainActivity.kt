package com.fieldsync.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fieldsync.app.databinding.ActivityMainBinding
import com.fieldsync.app.ui.form.FormActivity
import com.fieldsync.app.ui.form.FormViewModel
import com.fieldsync.app.ui.list.ListActivity
import com.fieldsync.app.ui.settings.SettingsActivity
import com.fieldsync.app.util.NetworkMonitor
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var viewModel: FormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = FormViewModel()
        viewModel.attach(application as FieldSyncApplication)

        binding.btnNewForm.setOnClickListener {
            startActivity(Intent(this, FormActivity::class.java))
        }

        binding.btnViewRecords.setOnClickListener {
            startActivity(Intent(this, ListActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnSync.setOnClickListener {
            lifecycleScope.launch {
                binding.progressSync.visibility = View.VISIBLE
                binding.btnSync.isEnabled = false
                val result = viewModel.syncNow()
                binding.progressSync.visibility = View.GONE
                binding.btnSync.isEnabled = true
                Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_LONG).show()
                updatePending()
            }
        }

        networkMonitor = NetworkMonitor(this)
        networkMonitor.start { online ->
            runOnUiThread {
                binding.txtStatus.text = if (online) "En línea" else "Sin conexión"
                binding.statusDot.setBackgroundResource(
                    if (online) R.drawable.dot_online else R.drawable.dot_offline
                )
            }
        }

        updatePending()
    }

    override fun onResume() {
        super.onResume()
        updatePending()
    }

    private fun updatePending() {
        lifecycleScope.launch {
            val count = viewModel.pendingCount()
            binding.txtPending.text = "Pendientes: $count"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.stop()
    }
}
