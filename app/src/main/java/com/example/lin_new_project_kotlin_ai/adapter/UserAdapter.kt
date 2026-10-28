package com.example.lin_new_project_kotlin_ai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.databinding.ItemuserBinding
import com.example.lin_new_project_kotlin_ai.room.entity.UserProfileEntity
import com.google.gson.Gson

class UserAdapter(
    private val context: Context?,
    private var array: MutableList<UserProfileEntity?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    var isReverse: Boolean = false

    private var onItemClickListener: OnItemClickListener? = null
    private var onDeleteClickListener: OnDeleteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemuserBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return MyRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1 = holder as MyRecyclerViewHolder
        val ii = position + 1
        val item = array[position]
        val user = item?.userData
        val typeStr = when (user?.userType) {
            1 -> "使用者"
            0 -> "訪客"
            else -> "類型: ${user?.userType ?: 0}"
        }
        val noteStr = if (!item?.note.isNullOrEmpty()) " | ${item?.note}" else ""
        val textNameStr = if (!item?.textname.isNullOrEmpty()) " | textname: ${item?.textname}" else ""
        val titleText = "$ii. [ID: ${item?.uid ?: 0}] ${user?.userName ?: "未命名"} ($typeStr$noteStr$textNameStr)"
        holder1.binding.tvText.text = titleText

        // 顯示整體資料的 JSON
        val jsonText = if (item != null) Gson().toJson(item) else "{}"
        holder1.binding.tvJson.text = jsonText
    }

    override fun getItemCount(): Int {
        return array.size
    }

    inner class MyRecyclerViewHolder internal constructor(var binding: ItemuserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (onItemClickListener != null) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos >= 0 && pos < array.size) {
                        onItemClickListener!!.OnItemClick(array[pos])
                    }
                }
            }
            binding.btnItemDelete.setOnClickListener {
                if (onDeleteClickListener != null) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos >= 0 && pos < array.size) {
                        onDeleteClickListener!!.onDeleteClick(array[pos], pos)
                    }
                }
            }
        }
    }

    fun setArray(array: MutableList<UserProfileEntity?>) {
        this.array = array
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnDeleteClickListener(onDeleteClickListener: OnDeleteClickListener?) {
        this.onDeleteClickListener = onDeleteClickListener
    }

    interface OnItemClickListener {
        fun OnItemClick(userProfileEntity: UserProfileEntity?)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(userProfileEntity: UserProfileEntity?, position: Int)
    }
}
