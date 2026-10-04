package com.fieldsync.app.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fieldsync.app.R
import com.fieldsync.app.data.database.FormEntity
import com.fieldsync.app.databinding.ItemFormBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ListAdapter(
    private val onDelete: (FormEntity) -> Unit
) : RecyclerView.Adapter<ListAdapter.FormViewHolder>() {

    private var items: List<FormEntity> = emptyList()

    fun submit(list: List<FormEntity>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val binding = ItemFormBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FormViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FormViewHolder(private val binding: ItemFormBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormEntity) {
            binding.txtClient.text = item.clientName
            binding.txtAgent.text = item.agentName
            binding.txtType.text = item.formType
            binding.txtDate.text = formatDate(item.createdAt)
            binding.txtStatus.text = item.status

            val colorRes = when (item.status) {
                "synced" -> R.color.status_synced
                "error" -> R.color.status_error
                else -> R.color.status_pending
            }
            binding.txtStatus.setTextColor(binding.root.context.getColor(colorRes))

            binding.btnDelete.setOnClickListener { onDelete(item) }
        }

        private fun formatDate(iso: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(iso)
                val out = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                if (date != null) out.format(date) else iso
            } catch (e: Exception) {
                iso
            }
        }
    }
}
