package com.example.lin_new_project_kotlin_ai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.databinding.ItemhomeBinding

class HomeAdapter(private val context: Context?, private var array_home: Array<String?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemhomeBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent,
            false
        )
        return MyRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1 = holder as MyRecyclerViewHolder
        holder1.binding.tvLanguage.setText(array_home[position])
    }

    override fun getItemCount(): Int {
        return array_home.size
    }

//    inner  class MyRecyclerViewHolder ( binding: ItemhomeBinding) : RecyclerView.ViewHolder(binding.getRoot()) {
//        init {
//            val binding: ItemhomeBinding= binding
//            //            getBindingAdapterPosition() 得到的是元素位於當前綁定Adapter的位置，
////            而getAbsoluteAdapterPosition()方法得到的是元素位於合併後Adapter的絕對位置。
//            binding.getRoot().setOnClickListener(View.OnClickListener { v: View? ->
//                if (onItemClickListener != null) {
//                    if (getAdapterPosition() >= RecyclerView.NO_POSITION) {
//                        onItemClickListener!!.OnItemClick(array_home[getAdapterPosition()])
//                    }
//                }
//            })
//        }
//    }
    inner class MyRecyclerViewHolder internal constructor(binding: ItemhomeBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        var binding: ItemhomeBinding

        init {
            this.binding = binding
            //            getBindingAdapterPosition() 得到的是元素位於當前綁定Adapter的位置，
//            而getAbsoluteAdapterPosition()方法得到的是元素位於合併後Adapter的絕對位置。
            binding.getRoot().setOnClickListener({ v ->
                if (onItemClickListener != null) {
                    if (getAdapterPosition() >= RecyclerView.NO_POSITION) {
                        onItemClickListener!!.OnItemClick(array_home[getAdapterPosition()])
                    }
                }
            })
        }
    }


    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        /**
         * 介面中的點選每一項的實現方法，引數自己定義
         */
        fun OnItemClick(string: String?)
    }
}
