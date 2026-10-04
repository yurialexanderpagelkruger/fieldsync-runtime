package com.fieldsync.app.ui.list

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fieldsync.app.FieldSyncApplication
import com.fieldsync.app.data.repository.FormRepository
import com.fieldsync.app.databinding.ActivityListBinding
import com.fieldsync.app.util.CsvExporter
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var repository: FormRepository
    private lateinit var adapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as FieldSyncApplication
        repository = FormRepository(app.database.formDao(), app.api)

        adapter = ListAdapter { form ->
            lifecycleScope.launch {
                repository.delete(form.id)
                Toast.makeText(this@ListActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
            }
        }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.btnExport.setOnClickListener { exportCsv() }

        lifecycleScope.launch {
            repository.observeAll().collect { list ->
                adapter.submit(list)
                binding.txtEmpty.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun exportCsv() {
        lifecycleScope.launch {
            val all = repository.getAll()
            if (all.isEmpty()) {
                Toast.makeText(this@ListActivity, "No hay registros", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val file = CsvExporter.export(this@ListActivity, all)
            CsvExporter.share(this@ListActivity, file)
        }
    }
}
