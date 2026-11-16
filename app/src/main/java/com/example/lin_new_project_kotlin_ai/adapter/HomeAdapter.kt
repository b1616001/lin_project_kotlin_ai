package com.example.lin_new_project_kotlin_ai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.ItemhomeBinding

class HomeAdapter(private val context: Context?, private var array_home: Array<String?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemhomeBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return MyRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1 = holder as MyRecyclerViewHolder
        val rawText = array_home[position].orEmpty()
        val dotIndex = rawText.indexOf('.')
        val key = if (dotIndex != -1) rawText.substring(0, dotIndex) else ""
        val displayTitle = if (dotIndex != -1) rawText.substring(dotIndex + 1) else rawText

        holder1.binding.tvLanguage.text = displayTitle
        holder1.binding.ivIcon.setImageResource(getIconResForKey(key))
    }

    private fun getIconResForKey(key: String): Int {
        return when (key) {
            "1" -> R.drawable.ic_home
            "2" -> R.drawable.ic_language
            "3" -> R.drawable.ic_location
            "4" -> R.drawable.ic_cloud
            "5" -> R.drawable.ic_volume_up
            "6" -> R.drawable.ic_signature
            "7" -> R.drawable.ic_history
            "8" -> R.drawable.ic_calendar
            "9" -> R.drawable.ic_shuffle
            "10" -> R.drawable.ic_chart
            "11" -> R.drawable.ic_touch_app
            "12" -> R.drawable.ic_mic
            "13" -> R.drawable.ic_code
            "14" -> R.drawable.ic_format_list_bulleted
            "15" -> R.drawable.ic_cloud
            "16", "19", "26" -> R.drawable.ic_notifications
            "17" -> R.drawable.ic_note_file
            "18", "25" -> R.drawable.ic_link
            "20" -> R.drawable.ic_qr_code
            "21" -> R.drawable.ic_vibration
            "22", "23" -> R.drawable.ic_screen_display
            "24", "30" -> R.drawable.ic_image
            "27" -> R.drawable.ic_history
            "28" -> R.drawable.ic_note_file
            "29" -> R.drawable.ic_palette
            else -> R.drawable.ic_home
        }
    }

    override fun getItemCount(): Int {
        return array_home.size
    }

    inner class MyRecyclerViewHolder internal constructor(var binding: ItemhomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (onItemClickListener != null && pos != RecyclerView.NO_POSITION && pos in array_home.indices) {
                    onItemClickListener?.OnItemClick(array_home[pos])
                }
            }
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun OnItemClick(string: String?)
    }
}
