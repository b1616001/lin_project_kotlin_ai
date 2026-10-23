package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.FragmentNotebookBinding
import com.example.lin_new_project_kotlin_ai.`fun`.AndroidQStorageSaveUtils
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils
import com.example.lin_new_project_kotlin_ai.`fun`.FileMethod
import com.example.lin_new_project_kotlin_ai.`fun`.NotebookFun
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import org.json.JSONArray
import org.json.JSONException
import java.util.concurrent.Executors


class NotebookFragment : BaseBindingFragment<FragmentNotebookBinding?>(), View.OnClickListener {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentNotebookBinding {
        return FragmentNotebookBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        getBinding()!!.btnRead.setOnClickListener(this)
        getBinding()!!.btnDeposit.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val dirName = getBinding()!!.edText.getText().toString()
        if (!CommonUtils.checkStr(dirName)) {
            showToast("檔名請勿空白")
            return
        }
        if (view == getBinding()!!.btnRead) {
            val cachedThreadPool = Executors.newCachedThreadPool()
            cachedThreadPool.execute(Runnable {
                val txt = NotebookFun.internalReadnnn(requireActivity(), dirName)
                Handler(Looper.getMainLooper()).post(object : Runnable {
                    override fun run() {
                        getBinding()!!.tvTxt.setText(txt)
                        //                        System.out.println("序UI" + threadName + " thread");
//                        Toast.makeText(ExecutorsActivity.this, threadName, Toast.LENGTH_LONG).show();
                    }
                })
            })
        }
        if (view == getBinding()!!.btnDeposit) {
            val aa = ArrayList<HashMap<*, *>?>()
            for (i in 0..1) {
                val hh = HashMap<String?, String?>()
                hh.put("x", "x" + i)
                hh.put("x1", "x1" + i)
                aa.add(hh)
            }
            try {
                val jsonArray = JSONArray(aa.toString())
                setLog("存入資料->" + jsonArray)
                val file = NotebookFun.extelnalPrivateCreateFoler(
                    requireActivity(),
                    dirName,
                    jsonArray.toString()
                )
                if (AndroidQStorageSaveUtils.fileUriIsExists(
                        requireContext(),
                        FileMethod.getFile_uri(getContext(), file)
                    )
                ) {
                    showToast("儲存成功")
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}