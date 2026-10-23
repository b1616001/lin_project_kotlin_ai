package com.example.lin_new_project_kotlin_ai.`fun`

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/*
 *@Description: 侧滑删除辅助类
 *@Author: hl
 *@Time: 2019/1/4 15:52
 */
class SimpleItemTouchHelperCallback(private val sthc_movement: Sthc_Movement) :
    ItemTouchHelper.Callback() {
    /**
     * 控制每个条目的可操作状态 - 滑动，拖拽等  来源->getFlag
     * @param recyclerView
     * @param viewHolder
     * @return
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        //int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        //int swipeFlags = ItemTouchHelper.LEFT;
        val flags = sthc_movement.getFlag(viewHolder.getLayoutPosition())
        return ItemTouchHelper.Callback.makeMovementFlags(flags!![0], flags[1])
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

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val alpha = 1 - abs(dX) / viewHolder.itemView.getWidth().toFloat()
            viewHolder.itemView.setAlpha(alpha)
            viewHolder.itemView.setTranslationX(dX)
        }
    }

    interface Sthc_Movement {
        fun onMove(viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?)
        fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int)
        fun getFlag(postion: Int): IntArray?
    }
}

