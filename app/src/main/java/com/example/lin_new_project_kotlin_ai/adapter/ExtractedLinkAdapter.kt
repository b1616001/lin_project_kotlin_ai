package com.example.lin_new_project_kotlin_ai.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.databinding.ItemTextUrlLinkBinding
import com.example.lin_new_project_kotlin_ai.`fun`.UrlUtilsMethod

class ExtractedLinkAdapter(
    private var list: MutableList<UrlUtilsMethod.ExtractedLink> = mutableListOf()
) : RecyclerView.Adapter<ExtractedLinkAdapter.LinkViewHolder>() {

    private var onOpenClickListener: ((UrlUtilsMethod.ExtractedLink) -> Unit)? = null
    private var onCopyClickListener: ((UrlUtilsMethod.ExtractedLink) -> Unit)? = null

    inner class LinkViewHolder(val binding: ItemTextUrlLinkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UrlUtilsMethod.ExtractedLink) {
            binding.tvLinkUrl.text = item.originalText
            binding.tvLinkTarget.text = "目標: ${item.targetUrl}"

            // 設定標籤顏色與文字
            val (badgeText, badgeColor) = when (item.type) {
                UrlUtilsMethod.LinkType.WEB -> "WEB" to Color.parseColor("#019889")
                UrlUtilsMethod.LinkType.EMAIL -> "EMAIL" to Color.parseColor("#7C4DFF")
                UrlUtilsMethod.LinkType.PHONE -> "PHONE" to Color.parseColor("#0288D1")
                UrlUtilsMethod.LinkType.OTHER -> "LINK" to Color.parseColor("#E65100")
            }

            binding.tvLinkType.text = badgeText
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(badgeColor)
            }
            binding.tvLinkType.background = bg

            binding.btnOpenLink.setOnClickListener {
                onOpenClickListener?.invoke(item)
            }

            binding.btnCopyLink.setOnClickListener {
                onCopyClickListener?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder {
        val binding = ItemTextUrlLinkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LinkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<UrlUtilsMethod.ExtractedLink>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun setOnOpenClickListener(listener: (UrlUtilsMethod.ExtractedLink) -> Unit) {
        this.onOpenClickListener = listener
    }

    fun setOnCopyClickListener(listener: (UrlUtilsMethod.ExtractedLink) -> Unit) {
        this.onCopyClickListener = listener
    }
}
