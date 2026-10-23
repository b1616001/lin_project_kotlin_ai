package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import com.example.lin_new_project_kotlin_ai.databinding.FragmentTimeBinding
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment


class TimeFragment : BaseBindingFragment<FragmentTimeBinding?>() {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentTimeBinding {
        return FragmentTimeBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val hashMap = HashMap<String?, Boolean?>()
        hashMap.put("2026年05月06日", true)
        hashMap.put("2026年05月17日", true)
        hashMap.put("2026年05月12日", true)
        hashMap.put("2026年05月01日", true)
        hashMap.put("2026年05月08日", true)
        hashMap.put("2026年05月09日", true)
        hashMap.put("2026年05月25日", true)

        getBinding()!!.timeView.hashMap = hashMap
        //        timeView.setTime(getTime());
        getBinding()!!.timeView.setTime("2026年5月07日")
        //        timeView.setValue(hashMap);//額外變動
        getBinding()!!.timeView.getBinding()!!.gvTime.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int,
                                                                               id: Long ->
            if (getBinding()!!.timeView.arrayList!!.get(position)!!.trim().length > 0) {
                val tt = getBinding()!!.timeView.str_years + getBinding()!!.timeView.arrayList!!.get(position) + "日"
                ToastMethod.showToast(requireContext(), tt)
                getBinding()!!.timeView.setValue(tt)
            }
        }
    }
}