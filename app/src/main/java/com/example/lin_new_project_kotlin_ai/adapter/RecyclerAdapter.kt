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
    private var onStartDragListener: OnStartDragListener? = null
    var isReverse: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ItemRacyerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1 = holder as MyRecyclerViewHolder
        val data = list[position]

        if (this.isReverse) {
            holder1.binding.tvId.text = (list.size - position).toString()
        } else {
            holder1.binding.tvId.text = position.toString()
        }

        holder1.binding.tvName.text = data.getName()

        // 僅限長按圖示（拖曳圖示 ivDragHandle 或項目圖示 ivIcon）才可以觸發上下拖曳
        holder1.binding.ivDragHandle.setOnLongClickListener {
            onStartDragListener?.onStartDrag(holder1)
            true
        }
        holder1.binding.ivIcon.setOnLongClickListener {
            onStartDragListener?.onStartDrag(holder1)
            true
        }

        // item 本身長按不能拖曳
        holder1.binding.root.setOnLongClickListener(null)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: MutableList<RecyclerData>) {
        this.list = list
        if (this.isReverse) {
            this.list = ArrayList(list)
            Collections.sort(
                this.list, Collections.reverseOrder(
                    Comparator.comparingInt(ToIntFunction { section: RecyclerData? -> section?.getId() ?: 0 })
                )
            )
        }
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnStartDragListener(onStartDragListener: OnStartDragListener?) {
        this.onStartDragListener = onStartDragListener
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(this.list, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class MyRecyclerViewHolder internal constructor(@JvmField var binding: ItemRacyerBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        init {
            binding.root.setOnClickListener { v: View? ->
                if (onItemClickListener != null) {
                    val pos = adapterPosition
                    if (pos > RecyclerView.NO_POSITION && pos < list.size) {
                        if (isReverse) {
                            onItemClickListener!!.OnItemClick(
                                list[pos],
                                list.size - pos - 1
                            )
                        } else {
                            onItemClickListener!!.OnItemClick(
                                list[pos],
                                pos
                            )
                        }
                    }
                }
            }
        }
    }

    interface OnItemClickListener {
        fun OnItemClick(data: RecyclerData?, position: Int)
    }

    interface OnStartDragListener {
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }

    companion object {
        private const val TAG = "RecyclerViewAdapter"
    }
}
