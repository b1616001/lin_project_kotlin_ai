package com.example.lin_new_project_kotlin_ai.fragment

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import com.example.lin_new_project_kotlin_ai.databinding.FragmentSignatureBinding
import com.example.lin_new_project_kotlin_ai.`fun`.AndroidQStorageSaveUtils
import com.example.lin_new_project_kotlin_ai.`fun`.ImageMethod
import com.example.lin_new_project_kotlin_ai.`fun`.ToastMethod
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import com.github.gcacace.signaturepad.views.SignaturePad


class SignatureFragment : BaseBindingFragment<FragmentSignatureBinding?>() {
    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentSignatureBinding {
        return FragmentSignatureBinding.inflate(layoutInflater)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        getBinding()!!.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                //Event triggered when the pad is touched
            }

            override fun onSigned() {
                //Event triggered when the pad is signed
            }

            override fun onClear() {
                //Event triggered when the pad is cleared
            }
        })
        getBinding()!!.btnSave.setOnClickListener { view: View? -> Save() }
        getBinding()!!.btnClear.setOnClickListener { view: View? -> getBinding()!!.signaturePad.clear() }
    }

    private fun Save() {
        try {
            val bmp = getBinding()!!.signaturePad.getSignatureBitmap()
            setLog("圖片大小" + "bmp.getWidth" + bmp.getWidth())
            val bb: Bitmap? = ImageMethod.reSize(bmp, 512) //縮放圖片大小
            //            ImageMethod.saveBitmap(bb);
            val bitmap_uri: Uri? = AndroidQStorageSaveUtils.saveBitmap(requireContext(), bb!!,
                Environment.DIRECTORY_DCIM, "Lin")
            if (AndroidQStorageSaveUtils.fileUriIsExists(requireContext(), bitmap_uri!!)) {
                ToastMethod.showToast(requireContext(), "儲存成功")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}