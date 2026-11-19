package com.example.lin_new_project_kotlin_ai.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.databinding.FragmentTimezoneBinding
import com.example.lin_new_project_kotlin_ai.databinding.ItemTimezoneWorldBinding
import com.example.lin_new_project_kotlin_ai.`fun`.ConvertedTimeResult
import com.example.lin_new_project_kotlin_ai.`fun`.TimeZoneHelper
import com.example.lin_new_project_kotlin_ai.`fun`.TimeZonePreset
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TimeZoneFragment : BaseBindingFragment<FragmentTimezoneBinding?>() {

    private var sourceZoneId: String = TimeZone.getDefault().id
    private var targetZoneId: String = if (sourceZoneId == "Asia/Tokyo") "Asia/Taipei" else "Asia/Tokyo"
    private var isLiveMode: Boolean = true
    private var referenceMillis: Long = System.currentTimeMillis()

    private val worldClockZoneIds = listOf(
        "UTC",
        "Asia/Taipei",
        "Asia/Tokyo",
        "Asia/Seoul",
        "Asia/Singapore",
        "Europe/London",
        "Europe/Paris",
        "America/New_York",
        "America/Los_Angeles",
        "Australia/Sydney"
    )

    private lateinit var worldClockAdapter: WorldClockAdapter
    private val tickHandler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isLiveMode && getBinding() != null) {
                referenceMillis = System.currentTimeMillis()
                refreshConversionUi()
                tickHandler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentTimezoneBinding {
        return FragmentTimezoneBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initHeader()
        initConverterControls()
        initTimestampParser()
        initWorldClockList()
        refreshConversionUi()
    }

    override fun onResume() {
        super.onResume()
        startLiveTickerIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        tickHandler.removeCallbacks(tickRunnable)
    }

    override fun onDestroyView() {
        tickHandler.removeCallbacks(tickRunnable)
        super.onDestroyView()
    }

    private fun startLiveTickerIfNeeded() {
        tickHandler.removeCallbacks(tickRunnable)
        if (isLiveMode) {
            referenceMillis = System.currentTimeMillis()
            refreshConversionUi()
            tickHandler.postDelayed(tickRunnable, 1000L)
        }
    }

    private fun initHeader() {
        val b = getBinding() ?: return
        b.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initConverterControls() {
        val b = getBinding() ?: return

        b.btnBackToLive.setOnClickListener {
            isLiveMode = true
            referenceMillis = System.currentTimeMillis()
            startLiveTickerIfNeeded()
            showToast("已切換回即時同步時間")
        }

        b.btnSourceZone.setOnClickListener {
            showZonePickerDialog(title = "選擇來源時區", currentZoneId = sourceZoneId) { selected ->
                sourceZoneId = selected.zoneId
                refreshConversionUi()
            }
        }

        b.btnTargetZone.setOnClickListener {
            showZonePickerDialog(title = "選擇目標時區", currentZoneId = targetZoneId) { selected ->
                targetZoneId = selected.zoneId
                refreshConversionUi()
            }
        }

        b.btnSwapZones.setOnClickListener {
            val prevSource = sourceZoneId
            sourceZoneId = targetZoneId
            targetZoneId = prevSource
            refreshConversionUi()
            showToast("已互換來源與目標時區")
        }

        b.btnPickDate.setOnClickListener {
            openSourceDatePicker()
        }

        b.btnPickTime.setOnClickListener {
            openSourceTimePicker()
        }

        b.btnShiftMinus1h.setOnClickListener { shiftReferenceMillis(-3_600_000L) }
        b.btnShiftPlus1h.setOnClickListener { shiftReferenceMillis(3_600_000L) }
        b.btnShiftPlus8h.setOnClickListener { shiftReferenceMillis(8 * 3_600_000L) }
        b.btnShiftPlus24h.setOnClickListener { shiftReferenceMillis(24 * 3_600_000L) }

        b.btnCopyResult.setOnClickListener {
            copyConvertedResult()
        }
    }

    private fun initTimestampParser() {
        val b = getBinding() ?: return

        b.btnFillCurrentEpoch.setOnClickListener {
            val seconds = System.currentTimeMillis() / 1000L
            b.etTimestampInput.setText(seconds.toString())
        }

        b.btnParseTimestamp.setOnClickListener {
            val raw = b.etTimestampInput.text?.toString().orEmpty()
            val parsedMillis = TimeZoneHelper.parseFlexibleInput(raw, sourceZoneId)
            if (parsedMillis == null) {
                showToast("無法解析格式，請輸入 Unix 時間戳或 yyyy-MM-dd HH:mm:ss")
            } else {
                isLiveMode = false
                tickHandler.removeCallbacks(tickRunnable)
                referenceMillis = parsedMillis
                refreshConversionUi()
                showToast("已套用解析時間並完成換算")
            }
        }
    }

    private fun initWorldClockList() {
        val b = getBinding() ?: return
        worldClockAdapter = WorldClockAdapter(
            onItemClick = { zoneId ->
                targetZoneId = zoneId
                refreshConversionUi()
                showToast("已將目標時區設為 ${TimeZoneHelper.findDisplayName(zoneId)}")
            }
        )
        b.rvWorldClocks.layoutManager = LinearLayoutManager(requireContext())
        b.rvWorldClocks.adapter = worldClockAdapter
    }

    private fun shiftReferenceMillis(deltaMillis: Long) {
        isLiveMode = false
        tickHandler.removeCallbacks(tickRunnable)
        referenceMillis += deltaMillis
        refreshConversionUi()
    }

    private fun openSourceDatePicker() {
        val ctx = context ?: return
        val cal = TimeZoneHelper.extractZoneFields(referenceMillis, sourceZoneId)
        DatePickerDialog(
            ctx,
            { _, year, month, dayOfMonth ->
                val current = TimeZoneHelper.extractZoneFields(referenceMillis, sourceZoneId)
                isLiveMode = false
                tickHandler.removeCallbacks(tickRunnable)
                referenceMillis = TimeZoneHelper.buildInstantFromZoneFields(
                    year = year,
                    monthZeroBased = month,
                    dayOfMonth = dayOfMonth,
                    hourOfDay = current.get(Calendar.HOUR_OF_DAY),
                    minute = current.get(Calendar.MINUTE),
                    second = current.get(Calendar.SECOND),
                    sourceZoneId = sourceZoneId
                )
                refreshConversionUi()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openSourceTimePicker() {
        val ctx = context ?: return
        val cal = TimeZoneHelper.extractZoneFields(referenceMillis, sourceZoneId)
        TimePickerDialog(
            ctx,
            { _, hourOfDay, minute ->
                val current = TimeZoneHelper.extractZoneFields(referenceMillis, sourceZoneId)
                isLiveMode = false
                tickHandler.removeCallbacks(tickRunnable)
                referenceMillis = TimeZoneHelper.buildInstantFromZoneFields(
                    year = current.get(Calendar.YEAR),
                    monthZeroBased = current.get(Calendar.MONTH),
                    dayOfMonth = current.get(Calendar.DAY_OF_MONTH),
                    hourOfDay = hourOfDay,
                    minute = minute,
                    second = 0,
                    sourceZoneId = sourceZoneId
                )
                refreshConversionUi()
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showZonePickerDialog(
        title: String,
        currentZoneId: String,
        onSelected: (TimeZonePreset) -> Unit
    ) {
        val ctx = context ?: return
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_timezone_picker, null)
        val etSearch = dialogView.findViewById<EditText>(R.id.etSearchZone)
        val lvZones = dialogView.findViewById<ListView>(R.id.lvZones)

        val allZones = TimeZoneHelper.getAllSelectableZones()
        val filtered = allZones.toMutableList()
        val now = referenceMillis

        val adapter = object : ArrayAdapter<TimeZonePreset>(
            ctx,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            filtered
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val item = filtered[position]
                val tz = TimeZoneHelper.resolveTimeZone(item.zoneId)
                val offset = TimeZoneHelper.formatOffsetString(tz, now)
                val selectedTag = if (item.zoneId == currentZoneId) "  [目前]" else ""

                val text1 = view.findViewById<TextView>(android.R.id.text1)
                val text2 = view.findViewById<TextView>(android.R.id.text2)
                text1.text = "${item.displayName}$selectedTag"
                text1.setTextColor(if (item.zoneId == currentZoneId) 0xFF019889.toInt() else 0xFF212121.toInt())
                text2.text = "${item.zoneId} • $offset (${item.regionLabel})"
                text2.setTextColor(0xFF607D8B.toInt())
                return view
            }
        }

        lvZones.adapter = adapter
        val dialog = AlertDialog.Builder(ctx)
            .setTitle(title)
            .setView(dialogView)
            .setNegativeButton("取消", null)
            .create()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim()?.lowercase(Locale.US).orEmpty()
                filtered.clear()
                if (q.isEmpty()) {
                    filtered.addAll(allZones)
                } else {
                    filtered.addAll(
                        allZones.filter {
                            it.displayName.lowercase(Locale.US).contains(q) ||
                                it.zoneId.lowercase(Locale.US).contains(q) ||
                                it.regionLabel.lowercase(Locale.US).contains(q)
                        }
                    )
                }
                adapter.notifyDataSetChanged()
            }
        })

        lvZones.setOnItemClickListener { _, _, position, _ ->
            val picked = filtered.getOrNull(position) ?: return@setOnItemClickListener
            onSelected(picked)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun refreshConversionUi() {
        val b = getBinding() ?: return

        val sourceResult = TimeZoneHelper.convertTimestamp(referenceMillis, sourceZoneId, sourceZoneId)
        val targetResult = TimeZoneHelper.convertTimestamp(referenceMillis, targetZoneId, sourceZoneId)
        val diffText = TimeZoneHelper.calculateOffsetDiffText(sourceZoneId, targetZoneId, referenceMillis)

        // Header Mode Badge
        b.tvModeBadge.text = if (isLiveMode) "即時同步" else "自訂時間"
        b.tvModeBadge.setTextColor(if (isLiveMode) 0xFF019889.toInt() else 0xFFEF6C00.toInt())
        b.btnBackToLive.visibility = if (isLiveMode) View.GONE else View.VISIBLE

        // Source & Target Cards
        val srcDstTag = if (sourceResult.isDaylightSaving) " (DST)" else ""
        val dstDstTag = if (targetResult.isDaylightSaving) " (DST)" else ""
        b.tvSourceZoneName.text = sourceResult.displayName
        b.tvSourceZoneOffset.text = "${sourceResult.offsetText}$srcDstTag"
        b.tvTargetZoneName.text = targetResult.displayName
        b.tvTargetZoneOffset.text = "${targetResult.offsetText}$dstDstTag"

        b.btnPickDate.text = sourceResult.formattedDate
        b.btnPickTime.text = sourceResult.formattedTime

        // Highlighted Result Panel
        b.tvResultHeader.text = "目標轉換結果 • ${targetResult.displayName} (${targetResult.offsetText})"
        b.tvDayDeltaBadge.text = targetResult.dayDeltaText
        b.tvResultTime.text = targetResult.formattedTime
        b.tvResultDateAndDiff.text = "${targetResult.formattedDate} ${targetResult.weekdayText} • $diffText"
        val epochSec = referenceMillis / 1000L
        b.tvResultIsoAndEpoch.text = "ISO: ${targetResult.iso8601}\nUnix: $epochSec s (${referenceMillis} ms)"

        // World Clock List
        val worldResults = worldClockZoneIds.map { zoneId ->
            TimeZoneHelper.convertTimestamp(referenceMillis, zoneId, sourceZoneId)
        }
        worldClockAdapter.submitList(worldResults, targetZoneId)
    }

    private fun copyConvertedResult() {
        val ctx = context ?: return
        val sourceResult = TimeZoneHelper.convertTimestamp(referenceMillis, sourceZoneId, sourceZoneId)
        val targetResult = TimeZoneHelper.convertTimestamp(referenceMillis, targetZoneId, sourceZoneId)
        val summary = buildString {
            append("${sourceResult.displayName} (${sourceResult.offsetText}): ${sourceResult.formattedDateTime}\n")
            append("→ ${targetResult.displayName} (${targetResult.offsetText}): ${targetResult.formattedDateTime}\n")
            append("ISO-8601: ${targetResult.iso8601}\n")
            append("Unix: ${referenceMillis / 1000L}")
        }
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("TimeZoneConversion", summary))
        showToast("已複製時區轉換結果到剪貼簿")
    }

    private class WorldClockAdapter(
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<WorldClockAdapter.Holder>() {

        private var items: List<ConvertedTimeResult> = emptyList()
        private var selectedTargetId: String = ""

        fun submitList(newItems: List<ConvertedTimeResult>, targetId: String) {
            items = newItems
            selectedTargetId = targetId
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val binding = ItemTimezoneWorldBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return Holder(binding)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = items[position]
            val b = holder.binding
            b.llWorldItemRoot.isSelected = item.zoneId == selectedTargetId
            b.tvCityName.text = item.displayName
            b.tvDstTag.visibility = if (item.isDaylightSaving) View.VISIBLE else View.GONE
            b.tvZoneMeta.text = "${item.zoneId} • ${item.offsetText}"
            b.tvWorldTime.text = item.formattedTime
            b.tvWorldDate.text = "${item.formattedDate} ${item.weekdayText} • ${item.dayDeltaText}"
            b.llWorldItemRoot.setOnClickListener {
                onItemClick(item.zoneId)
            }
        }

        override fun getItemCount(): Int = items.size

        class Holder(val binding: ItemTimezoneWorldBinding) : RecyclerView.ViewHolder(binding.root)
    }
}
