package com.fieldsync.app.ui.form

import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fieldsync.app.FieldSyncApplication
import com.fieldsync.app.databinding.ActivityFormBinding

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private lateinit var viewModel: FormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = FormViewModel()
        viewModel.attach(application as FieldSyncApplication)

        binding.btnSave.setOnClickListener { saveForm() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun saveForm() {
        val agent = binding.etAgent.text.toString().trim()
        val client = binding.etClient.text.toString().trim()
        val product = binding.etProduct.text.toString().trim()
        val quantity = binding.etQuantity.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (agent.isEmpty() || client.isEmpty() || product.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val fields = mapOf(
            "product" to product,
            "quantity" to (quantity.toIntOrNull() ?: 0),
            "notes" to notes,
            "location" to binding.etLocation.text.toString().trim()
        )

        binding.btnSave.isEnabled = false
        viewModel.saveForm(deviceId, agent, client, "inventory", fields) {
            Toast.makeText(this, "Guardado localmente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
