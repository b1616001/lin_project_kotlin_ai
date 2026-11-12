package com.example.lin_new_project_kotlin_ai.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lin_new_project_kotlin_ai.adapter.NotebookAdapter
import com.example.lin_new_project_kotlin_ai.databinding.FragmentNotebookBinding
import com.example.lin_new_project_kotlin_ai.`fun`.CommonUtils
import com.example.lin_new_project_kotlin_ai.`fun`.NotebookFun
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class NotebookFragment : BaseBindingFragment<FragmentNotebookBinding?>(), View.OnClickListener {

    private lateinit var notebookAdapter: NotebookAdapter

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentNotebookBinding {
        return FragmentNotebookBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupRecyclerView()
        setupListeners()
        loadNotesList()
    }

    private fun initView() {
        val binding = getBinding() ?: return

        // 顯示儲存路徑資訊
        val path = NotebookFun.getStorageDirectoryPath(requireContext())
        binding.tvStoragePath.text = "目錄: $path"

        // 預設檔案名稱
        if (binding.edText.text.isNullOrEmpty()) {
            binding.edText.setText("my_note.txt")
        }

        // 字數統計監聽
        binding.edContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.tvCharCount.text = "$length 字元"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        val binding = getBinding() ?: return
        notebookAdapter = NotebookAdapter()

        notebookAdapter.setOnItemClickListener { noteItem ->
            binding.edText.setText(noteItem.name)
            readFile(noteItem.name)
            showToast("已選取: ${noteItem.name}")
        }

        notebookAdapter.setOnDeleteClickListener { noteItem, _ ->
            confirmDeleteNote(noteItem)
        }

        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notebookAdapter
        }
    }

    private fun setupListeners() {
        val binding = getBinding() ?: return

        binding.btnRead.setOnClickListener(this)
        binding.btnDeposit.setOnClickListener(this)
        binding.btnClear.setOnClickListener(this)
        binding.btnCopy.setOnClickListener(this)
        binding.btnRefreshList.setOnClickListener(this)

        // 範本快速填入
        binding.chipPlain.setOnClickListener {
            binding.edText.setText("daily_note.txt")
            binding.edContent.setText(
                "【日常記事】\n" +
                "日期：2026-11-11\n" +
                "主題：專案進度追蹤\n" +
                "內容：\n" +
                "1. 完成 NotebookFragment 效能與介面最佳化\n" +
                "2. 支援即時筆記瀏覽與快速儲存\n" +
                "3. 備註：使用內部獨立儲存，安全可靠。"
            )
        }

        binding.chipTodo.setOnClickListener {
            binding.edText.setText("todo_list.txt")
            binding.edContent.setText(
                "【待辦事項清單】\n" +
                "[x] 整理筆記模組核心架構\n" +
                "[x] 加入清單管理與點擊讀取功能\n" +
                "[ ] 測試檔案讀寫邊界條件\n" +
                "[ ] 審查 Material 3 深色主題外觀"
            )
        }

        binding.chipJson.setOnClickListener {
            binding.edText.setText("sample_data.json")
            try {
                val jsonArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", 1)
                        put("title", "Kotlin Coroutines")
                        put("status", "completed")
                        put("priority", "high")
                    })
                    put(JSONObject().apply {
                        put("id", 2)
                        put("title", "Storage Access Framework")
                        put("status", "in_progress")
                        put("priority", "medium")
                    })
                }
                binding.edContent.setText(jsonArray.toString(2))
            } catch (e: Exception) {
                binding.edContent.setText("[{\"id\": 1, \"title\": \"Note 1\"}]")
            }
        }
    }

    override fun onClick(view: View) {
        val binding = getBinding() ?: return

        when (view) {
            binding.btnRead -> {
                val fileName = binding.edText.text.toString().trim()
                if (!CommonUtils.checkStr(fileName)) {
                    showToast("檔名請勿空白")
                    return
                }
                readFile(fileName)
            }

            binding.btnDeposit -> {
                val fileName = binding.edText.text.toString().trim()
                if (!CommonUtils.checkStr(fileName)) {
                    showToast("檔名請勿空白")
                    return
                }

                var content = binding.edContent.text.toString()
                if (content.isEmpty()) {
                    // 若無輸入內容，自動以 JSON 陣列範本儲存（維持原有相容性與展示效果）
                    val aa = ArrayList<HashMap<String, String>>()
                    for (i in 0..1) {
                        val hh = HashMap<String, String>()
                        hh["x"] = "x$i"
                        hh["x1"] = "x1$i"
                        aa.add(hh)
                    }
                    val jsonArray = JSONArray(aa)
                    content = jsonArray.toString()
                    binding.edContent.setText(content)
                }

                saveFile(fileName, content)
            }

            binding.btnClear -> {
                binding.edText.setText("")
                binding.edContent.setText("")
                binding.tvTxt.text = "已清空編輯內容"
                showToast("已清空")
            }

            binding.btnCopy -> {
                val textToCopy = binding.tvTxt.text.toString()
                if (textToCopy.isEmpty() || textToCopy == "尚未載入檔案內容") {
                    showToast("無內容可複製")
                    return
                }
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Notebook Content", textToCopy)
                clipboard.setPrimaryClip(clip)
                showToast("已複製到剪貼簿")
            }

            binding.btnRefreshList -> {
                loadNotesList()
                showToast("已重新整理清單")
            }
        }
    }

    private fun readFile(fileName: String) {
        val binding = getBinding() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val content = withContext(Dispatchers.IO) {
                NotebookFun.readNote(requireContext(), fileName)
            }

            if (content == "無資料") {
                binding.tvTxt.text = "無資料 (檔案可能不存在或為空)"
                showToast("找不到檔案或無資料: $fileName")
            } else {
                binding.tvTxt.text = content
                binding.edContent.setText(content)
                showToast("讀取成功: $fileName")
            }
        }
    }

    private fun saveFile(fileName: String, content: String) {
        val binding = getBinding() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) {
                NotebookFun.saveNote(requireContext(), fileName, content)
            }

            if (file.exists() && file.length() >= 0) {
                binding.tvTxt.text = content
                showToast("儲存成功 (${file.length()} 位元組)")
                setLog("存入資料成功 -> ${file.absolutePath}")
                loadNotesList()
            } else {
                showToast("儲存失敗")
            }
        }
    }

    private fun loadNotesList() {
        val binding = getBinding() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val notes = withContext(Dispatchers.IO) {
                NotebookFun.getNotesList(requireContext())
            }

            binding.tvNotesTitle.text = "📁 已儲存筆記清單 (${notes.size})"
            if (notes.isEmpty()) {
                binding.tvEmptyNotes.visibility = View.VISIBLE
                binding.rvNotes.visibility = View.GONE
            } else {
                binding.tvEmptyNotes.visibility = View.GONE
                binding.rvNotes.visibility = View.VISIBLE
                notebookAdapter.updateList(notes)
            }
        }
    }

    private fun confirmDeleteNote(noteItem: NotebookFun.NoteItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("刪除確認")
            .setMessage("確定要刪除「${noteItem.name}」嗎？此操作無法復原。")
            .setPositiveButton("確定刪除") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val deleted = withContext(Dispatchers.IO) {
                        NotebookFun.deleteNote(requireContext(), noteItem.name)
                    }
                    if (deleted) {
                        showToast("已刪除: ${noteItem.name}")
                        loadNotesList()
                    } else {
                        showToast("刪除失敗")
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}