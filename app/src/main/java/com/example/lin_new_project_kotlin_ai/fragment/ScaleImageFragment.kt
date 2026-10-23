package com.example.lin_new_project_kotlin_ai.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentScaleImageBinding
import com.example.lin_new_project_kotlin_ai.view.ScaleImageView
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment


class ScaleImageFragment : BaseBindingFragment<FragmentScaleImageBinding?>() {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentScaleImageBinding {
        return FragmentScaleImageBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        getBinding()!!.frameLayout.post  {
            val location = IntArray(2)
            getBinding()!!.frameLayout.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + getBinding()!!.frameLayout.getMeasuredWidth()
            val bottom = top + getBinding()!!.frameLayout.getMeasuredHeight()
            Log.d(
                "資料長寬-座標", left.toString() + "\n" +
                        top + "\n" +
                        right + "\n" +
                        bottom
            )
            Log.d(
                "資料長寬-寬", getBinding()!!.frameLayout.getWidth().toString() + "\n" +
                        getBinding()!!.frameLayout.getMeasuredWidth() + "\n" +
                        getBinding()!!.frameLayout.getMinimumWidth() + "\n" +
                        getBinding()!!.frameLayout.getVerticalScrollbarWidth()
            )
            Log.d(
                "資料長寬-長", getBinding()!!.frameLayout.getHeight().toString() + "\n" +
                        getBinding()!!.frameLayout.getMeasuredHeight() + "\n" +
                        getBinding()!!.frameLayout.getMinimumHeight()
            )
            val bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_red)
            val scaleImageView = ScaleImageView(requireContext())
            scaleImageView.setImageBitmap(bitmap)
            scaleImageView.setData(
                getBinding()!!.frameLayout.getWidth(),
                getBinding()!!.frameLayout.getHeight(),
                left,
                top,
                right,
                bottom
            )
            getBinding()!!.frameLayout.addView(scaleImageView)
        }
    }
}