package com.example.lin_new_project_kotlin_ai.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.example.lin_new_project_kotlin_ai.view.viewpager.ViewPagerView


class ViewPagerAdapter(private val arrayList: ArrayList<ViewPagerView<*>?>) : PagerAdapter() {

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view === any
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(arrayList.get(position)!!.binding!!.getRoot())
        return arrayList.get(position)!!.binding!!.getRoot()
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(arrayList.get(position)!!.binding!!.getRoot())
    }
}
