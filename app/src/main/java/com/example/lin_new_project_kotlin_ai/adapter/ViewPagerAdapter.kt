package com.example.lin_new_project_kotlin_ai.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.example.lin_new_project_kotlin_ai.view.viewpager.ViewPagerView

class ViewPagerAdapter(
    private val arrayList: List<ViewPagerView<*>?>,
    private val pageTitles: List<String> = emptyList()
) : PagerAdapter() {

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun isViewFromObject(view: View, any: Any): Boolean {
        return view === any
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = arrayList.getOrNull(position)?.binding?.root ?: View(container.context)
        (view.parent as? ViewGroup)?.removeView(view)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        (any as? View)?.let { container.removeView(it) }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position in pageTitles.indices) {
            pageTitles[position]
        } else {
            "頁面 ${position + 1}"
        }
    }
}
