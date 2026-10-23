package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.google.gson.Gson


class SimpleItemFragment : BaseBindingFragment<FragmentSimpleItemBinding?>() {
    private var recyclerAdapter: RecyclerAdapter? = null

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentSimpleItemBinding {
        return FragmentSimpleItemBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        RecyclerViewMethed.init_VERTICAL(
            getActivity(), getBinding()!!.recyclerView,
            true, 0, CommonUtils.getColorByVERSION(R.color.white), true, true
        )
        val list: MutableList<RecyclerData> = ArrayList<RecyclerData>()
        for (i in 0..49) {
            val recyclerData: RecyclerData = RecyclerData()
            recyclerData.setId(i)
            recyclerData.setName("A" + i)
            list.add(recyclerData)
        }
        recyclerAdapter = RecyclerAdapter(getContext(), list)
        recyclerAdapter!!.setOnItemClickListener(object :RecyclerAdapter.OnItemClickListener{
            override fun OnItemClick(data: RecyclerData?, position: Int) {
                showToast(Gson().toJson(data))
            }
        })

        getBinding()!!.recyclerView.setAdapter(recyclerAdapter)
        val itemTouchHelper: SimpleItemTouchHelper =
            SimpleItemTouchHelper(SimpleItemTouchHelperCallback(object :
                SimpleItemTouchHelperCallback.Sthc_Movement {
                override fun onMove(
                    viewHolder: RecyclerView.ViewHolder?,
                    target: RecyclerView.ViewHolder?
                ) {
                    val fromPos: Int = viewHolder!!.getAdapterPosition()
                    val toPos: Int = target!!.getAdapterPosition()

                    var dataIndexFrom = fromPos
                    var dataIndexTo = toPos

                    //                Log.d(TAG, "onItemMove: from: " + fromPos + ", to: " + toPos + ", size: " + routeSectionList.size());
                    if (recyclerAdapter!!.isReverse) {
                        val index = list.size - 1
                        dataIndexFrom = index - fromPos
                        dataIndexTo = index - toPos
                        //                    Log.d(TAG, "com.example.lin_new_project_kotlin.data index: from: " + dataIndexFrom + ", to: " + dataIndexTo + ", index: " + index);
                    }

                    /**< 禁止拖动到新增菜单的底部 */
                    if (toPos >= (list.size) || toPos < 0) {
                        return
                    }

                    val holder1 = viewHolder as RecyclerAdapter.MyRecyclerViewHolder
                    val holder2 = target as RecyclerAdapter.MyRecyclerViewHolder

                    //                holder1.binding.tvOrder.setText(String.valueOf(toPos + 1));
//                holder2.binding.tvOrder.setText(String.valueOf(fromPos + 1));
//
//                RouteSection data1 = routeSectionList.get(fromPos);
//                RouteSection data2 = routeSectionList.get(toPos);
//                int o1 = data1.getOrder();
//                int o2 = data2.getOrder();
//                data1.setOrder(o2);
//                data2.setOrder(o1);
//                routeSectionList.set(fromPos, data2);
//                routeSectionList.set(toPos, data1);
                    holder1.binding.tvId.setText(dataIndexTo.toString())
                    holder2.binding.tvId.setText(dataIndexFrom.toString())

                    val data1: RecyclerData = list.get(dataIndexFrom)
                    val data2: RecyclerData = list.get(dataIndexTo)
                    val o1: Int = data1.getId()
                    val o2: Int = data2.getId()
                    data1.setId(o2)
                    data2.setId(o1)
                    list.set(dataIndexFrom, data2)
                    list.set(dataIndexTo, data1)


                    //                Collections.swap(paragraphsDataList, fromPos, toPos);
                    recyclerAdapter!!.notifyItemMoved(fromPos, toPos)
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                    return
                }

                /**
                 * 控制每个条目的可操作状态 - 滑动，拖拽等  对应->getMovementFlags
                 * @param position
                 * @return
                 */
                override fun getFlag(position: Int): IntArray? {
                    if (position == (list.size)) {
                        return intArrayOf(0, 0)
                    } else {
                        return intArrayOf(
                            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                            ItemTouchHelper.UP
                        )
                    }
                }
            }))
        itemTouchHelper.attachToRecyclerView(getBinding()!!.recyclerView)
    }
}