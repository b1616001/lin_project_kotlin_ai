package com.example.lin_new_project_kotlin_ai.`fun`

import android.app.Activity
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object NotebookFun {
    var fileName: String = "Fiction" //存放記事本的檔案夾名稱
    fun internalReadnnn(activity: Activity, dirName: String): String {
        var ss = ""
        try {
            val dir = getExtermalStoragePrivateDir(activity, fileName)
            val f = File(dir, dirName) //dirName記事本名稱
            val inputStream = FileInputStream(f)
            val bytes = ByteArray(10240)
            val sb = StringBuffer()
            while (inputStream.read(bytes) != -1) {
                sb.append(String(bytes))
            }
            ss = sb.toString()
            inputStream.close()
            return ss
        } catch (e: Exception) {
            e.printStackTrace()
            return "無資料"
        }
    }

    fun extelnalPrivateCreateFoler(activity: Activity, dirName: String, sb: String): File {
        val dir = getExtermalStoragePrivateDir(activity, fileName)
        val file = File(dir, dirName)
        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(sb.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    private fun getExtermalStoragePrivateDir(activity: Activity, fileName: String?): File? {
        val dirs = activity.getExternalFilesDirs(fileName) //name 資料夾名稱
        var primaryDir: File? = null
        if (dirs != null && dirs.size > 0) {
            primaryDir = dirs[0]
        }
        Log.d("NotebookFun", "getExtermalStoragePrivateDir->" + primaryDir)
        return primaryDir
    }
}
