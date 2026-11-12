package com.example.lin_new_project_kotlin_ai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.databinding.ItemNotebookNoteBinding
import com.example.lin_new_project_kotlin_ai.`fun`.NotebookFun
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotebookAdapter(
    private var list: MutableList<NotebookFun.NoteItem> = mutableListOf()
) : RecyclerView.Adapter<NotebookAdapter.NoteViewHolder>() {

    private var onItemClickListener: ((NotebookFun.NoteItem) -> Unit)? = null
    private var onDeleteClickListener: ((NotebookFun.NoteItem, Int) -> Unit)? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    inner class NoteViewHolder(val binding: ItemNotebookNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotebookFun.NoteItem, position: Int) {
            binding.tvItemName.text = item.name
            val sizeFormatted = formatFileSize(item.size)
            val dateFormatted = dateFormat.format(Date(item.lastModified))
            binding.tvItemInfo.text = "$sizeFormatted • $dateFormatted"

            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }

            binding.btnItemDelete.setOnClickListener {
                onDeleteClickListener?.invoke(item, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNotebookNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<NotebookFun.NoteItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (NotebookFun.NoteItem) -> Unit) {
        this.onItemClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (NotebookFun.NoteItem, Int) -> Unit) {
        this.onDeleteClickListener = listener
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", size / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", size / (1024.0 * 1024.0))
        }
    }
}
