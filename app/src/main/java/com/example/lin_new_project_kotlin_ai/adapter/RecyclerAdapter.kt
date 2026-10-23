package com.example.lin_new_project_kotlin_ai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.data.RecyclerData
import com.example.lin_new_project_kotlin_ai.databinding.ItemRacyerBinding
import com.example.lin_new_project_kotlin_ai.`fun`.DragInterface
import java.util.Collections
import java.util.function.ToIntFunction


class RecyclerAdapter(private val context: Context?, private var list: MutableList<RecyclerData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>(), DragInterface {
    private var onItemClickListener: OnItemClickListener? = null

    var isReverse: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ItemRacyerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        return MyRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1 = holder as MyRecyclerViewHolder

        val data = list.get(position)

        if (this.isReverse) {
            holder1.binding.tvId.setText((list.size - position).toString())
        } else {
            holder1.binding.tvId.setText(position.toString())
        }

        holder1.binding.tvName.setText(data.getName())
    }


    override fun getItemCount(): Int {
        return list.size
    }


    fun setList(list: MutableList<RecyclerData>) {
        this.list = list
        if (this.isReverse) {
            this.list = ArrayList<RecyclerData>(list)
            Collections.sort<RecyclerData?>(
                this.list, Collections.reverseOrder<RecyclerData?>(
                    Comparator.comparingInt<RecyclerData?>(ToIntFunction { section: RecyclerData? -> section!!.getId() })
                )
            )
        }
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(this.list, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class MyRecyclerViewHolder internal constructor(@JvmField var binding: ItemRacyerBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        ) {
        init {
            binding.getRoot().setOnClickListener { v: View? ->
                //此處回傳點選監聽事件
                if (onItemClickListener != null) {
                    if (getAdapterPosition() > RecyclerView.NO_POSITION) {
//                        onItemClickListener.OnItemClick(routeSectionList.get(getAdapterPosition()),getAdapterPosition());
                        if (isReverse) {
                            onItemClickListener!!.OnItemClick(
                                list.get(getAdapterPosition()),
                                list.size - getAdapterPosition() - 1
                            )
                        } else {
                            onItemClickListener!!.OnItemClick(
                                list.get(getAdapterPosition()),
                                getAdapterPosition()
                            )
                        }
                    }
                }
            }
        }
    }


    interface OnItemClickListener {
        /**
         * 介面中的點選每一項的實現方法，引數自己定義
         * 
         * @param data 點選的item的資料
         */
        fun OnItemClick(data: RecyclerData?, position: Int)
    }

    companion object {
        private const val TAG = "RecyclerViewAdapter"
    }
}
