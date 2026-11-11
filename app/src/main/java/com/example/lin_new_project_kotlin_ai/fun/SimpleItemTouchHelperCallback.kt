package com.example.lin_new_project_kotlin_ai.`fun`

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class SimpleItemTouchHelperCallback(private val sthc_movement: Sthc_Movement) :
    ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean {
        // 停用整行長按拖曳，只有長按 item 的圖示才可以拖曳
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        // 停用滑動刪除，專注於上下拖曳排序
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val flags = sthc_movement.getFlag(viewHolder.layoutPosition)
        val dragFlags = flags?.getOrNull(0) ?: (ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        val swipeFlags = flags?.getOrNull(1) ?: 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        sthc_movement.onMove(viewHolder, target)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
        sthc_movement.onSwiped(viewHolder, i)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        // 拖曳狀態視覺回饋
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.let {
                it.animate().scaleX(1.03f).scaleY(1.03f).setDuration(120).start()
                it.alpha = 0.88f
                it.elevation = 16f
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        // 釋放拖曳後還原視圖
        viewHolder.itemView.let {
            it.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
            it.alpha = 1.0f
            it.elevation = 0f
        }
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val alpha = 1 - abs(dX) / viewHolder.itemView.width.toFloat()
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX
        }
    }

    interface Sthc_Movement {
        fun onMove(viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?)
        fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int)
        fun getFlag(position: Int): IntArray?
    }
}
