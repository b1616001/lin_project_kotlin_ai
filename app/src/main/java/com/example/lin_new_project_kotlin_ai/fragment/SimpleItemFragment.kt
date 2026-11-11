package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.adapter.RecyclerAdapter
import com.example.lin_new_project_kotlin_ai.data.RecyclerData
import com.example.lin_new_project_kotlin_ai.databinding.FragmentSimpleItemBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils
import com.example.lin_new_project_kotlin_ai.`fun`.RecyclerViewMethed
import com.example.lin_new_project_kotlin_ai.`fun`.SimpleItemTouchHelper
import com.example.lin_new_project_kotlin_ai.`fun`.SimpleItemTouchHelperCallback
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment

class SimpleItemFragment : BaseBindingFragment<FragmentSimpleItemBinding?>() {
    private var recyclerAdapter: RecyclerAdapter? = null
    private var itemTouchHelper: SimpleItemTouchHelper? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentSimpleItemBinding {
        return FragmentSimpleItemBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val binding = getBinding() ?: return

        // 頂部返回導覽
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 初始化 RecyclerView
        RecyclerViewMethed.init_VERTICAL(
            activity, binding.recyclerView,
            true, 0, CommonUtils.getColorByVERSION(R.color.white), true, true
        )

        // 產生列表資料
        val list: MutableList<RecyclerData> = ArrayList()
        for (i in 0..49) {
            val recyclerData = RecyclerData()
            recyclerData.id = i
            recyclerData.name = "項目 A$i"
            list.add(recyclerData)
        }
        binding.tvItemCount.text = "${list.size} 項"

        val adapter = RecyclerAdapter(context, list)
        recyclerAdapter = adapter

        adapter.setOnItemClickListener(object : RecyclerAdapter.OnItemClickListener {
            override fun OnItemClick(data: RecyclerData?, position: Int) {
                showToast("點擊: ${data?.name ?: ""} (序號: ${data?.id})")
            }
        })

        // 建立長按上下拖曳 Callback
        val callback = SimpleItemTouchHelperCallback(object :
            SimpleItemTouchHelperCallback.Sthc_Movement {
            override fun onMove(
                viewHolder: RecyclerView.ViewHolder?,
                target: RecyclerView.ViewHolder?
            ) {
                if (viewHolder == null || target == null) return
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition

                if (fromPos < 0 || toPos < 0 || fromPos >= list.size || toPos >= list.size) {
                    return
                }

                val holder1 = viewHolder as? RecyclerAdapter.MyRecyclerViewHolder
                val holder2 = target as? RecyclerAdapter.MyRecyclerViewHolder

                holder1?.binding?.tvId?.text = toPos.toString()
                holder2?.binding?.tvId?.text = fromPos.toString()

                val data1 = list[fromPos]
                val data2 = list[toPos]
                val o1 = data1.id
                val o2 = data2.id
                data1.id = o2
                data2.id = o1
                list[fromPos] = data2
                list[toPos] = data1

                adapter.notifyItemMoved(fromPos, toPos)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                // 停用滑動刪除，僅允許長按上下拖曳
            }

            override fun getFlag(position: Int): IntArray {
                return if (position in list.indices) {
                    // dragFlags: 僅允許 UP 與 DOWN，swipeFlags: 0 (無滑動)
                    intArrayOf(
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                        0
                    )
                } else {
                    intArrayOf(0, 0)
                }
            }
        })

        val touchHelper = SimpleItemTouchHelper(callback)
        itemTouchHelper = touchHelper
        touchHelper.attachToRecyclerView(binding.recyclerView)

        // 綁定長按拖曳手柄圖示事件
        adapter.setOnStartDragListener(object : RecyclerAdapter.OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        })

        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        itemTouchHelper = null
        recyclerAdapter = null
        super.onDestroyView()
    }
}