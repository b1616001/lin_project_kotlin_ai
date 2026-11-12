package com.example.lin_new_project_kotlin_ai.`fun`

import android.app.Activity
import android.content.Context
import android.util.Log
import java.io.File

object NotebookFun {
    var fileName: String = "Fiction" // 存放記事本的檔案夾名稱

    data class NoteItem(
        val name: String,
        val size: Long,
        val lastModified: Long,
        val file: File
    )

    fun internalReadnnn(activity: Activity, dirName: String): String {
        return readNote(activity, dirName)
    }

    fun extelnalPrivateCreateFoler(activity: Activity, dirName: String, sb: String): File {
        return saveNote(activity, dirName, sb)
    }

    fun readNote(context: Context, dirName: String): String {
        return try {
            val dir = getExternalStoragePrivateDir(context, fileName) ?: return "無資料"
            val f = File(dir, dirName)
            if (!f.exists() || !f.isFile) {
                return "無資料"
            }
            f.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("NotebookFun", "readNote error: ${e.message}", e)
            "無資料"
        }
    }

    fun saveNote(context: Context, dirName: String, content: String): File {
        val dir = getExternalStoragePrivateDir(context, fileName)
        if (dir != null && !dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, dirName)
        try {
            file.writeText(content, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("NotebookFun", "saveNote error: ${e.message}", e)
        }
        return file
    }

    fun deleteNote(context: Context, dirName: String): Boolean {
        return try {
            val dir = getExternalStoragePrivateDir(context, fileName) ?: return false
            val file = File(dir, dirName)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            Log.e("NotebookFun", "deleteNote error: ${e.message}", e)
            false
        }
    }

    fun getNotesList(context: Context): List<NoteItem> {
        val list = mutableListOf<NoteItem>()
        try {
            val dir = getExternalStoragePrivateDir(context, fileName)
            if (dir != null && dir.exists() && dir.isDirectory) {
                val files = dir.listFiles()
                if (files != null) {
                    for (f in files) {
                        if (f.isFile && !f.name.startsWith(".")) {
                            list.add(NoteItem(f.name, f.length(), f.lastModified(), f))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NotebookFun", "getNotesList error: ${e.message}", e)
        }
        list.sortByDescending { it.lastModified }
        return list
    }

    fun getStorageDirectoryPath(context: Context): String {
        val dir = getExternalStoragePrivateDir(context, fileName)
        return dir?.absolutePath ?: "內部儲存空間"
    }

    fun getExternalStoragePrivateDir(context: Context, subFolder: String?): File? {
        val dirs = context.getExternalFilesDirs(subFolder)
        var primaryDir: File? = null
        if (dirs != null && dirs.isNotEmpty() && dirs[0] != null) {
            primaryDir = dirs[0]
        }
        if (primaryDir == null) {
            primaryDir = File(context.filesDir, subFolder ?: "")
        }
        if (!primaryDir.exists()) {
            primaryDir.mkdirs()
        }
        Log.d("NotebookFun", "getExternalStoragePrivateDir->$primaryDir")
        return primaryDir
    }
}
