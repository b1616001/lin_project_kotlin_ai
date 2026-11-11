package com.example.lin_new_project_kotlin_ai.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.lin_new_project_kotlin_ai.R
import com.example.lin_new_project_kotlin_ai.adapter.ViewPagerAdapter
import com.example.lin_new_project_kotlin_ai.databinding.FragmentViewPagerBinding
import com.example.lin_new_project_kotlin_ai.view.viewpager.BlackView
import com.example.lin_new_project_kotlin_ai.view.viewpager.BlueView
import com.example.lin_new_project_kotlin_ai.view.viewpager.RedView
import com.example.lin_new_project_kotlin_ai.view.viewpager.ViewPagerView
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment

class ViewPagerFragment : BaseBindingFragment<FragmentViewPagerBinding?>() {

    private val pages = ArrayList<ViewPagerView<*>>()
    private val titles = listOf("紅色主題", "藍色主題", "黑色主題")
    private var isAutoPlaying = false
    private val autoPlayHandler = Handler(Looper.getMainLooper())

    private val autoPlayRunnable = object : Runnable {
        override fun run() {
            if (!isAutoPlaying) return
            val binding = getBinding() ?: return
            val count = pages.size
            if (count > 0) {
                val next = (binding.viewPager.currentItem + 1) % count
                binding.viewPager.setCurrentItem(next, true)
                autoPlayHandler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreateViewBinding(layoutInflater: LayoutInflater): FragmentViewPagerBinding {
        return FragmentViewPagerBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        setupListeners()
    }

    private fun initViewPager() {
        val binding = getBinding() ?: return
        val context = requireContext()

        val redView = RedView(context).apply {
            onNextAction = { selectPage(1) }
        }
        val blueView = BlueView(context).apply {
            onNextAction = { selectPage(2) }
        }
        val blackView = BlackView(context).apply {
            onNextAction = { selectPage(0) }
        }

        pages.clear()
        pages.add(redView)
        pages.add(blueView)
        pages.add(blackView)

        val adapter = ViewPagerAdapter(pages, titles)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        // Smooth Page Transition Animation
        binding.viewPager.setPageTransformer(true, ZoomOutPageTransformer())

        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                updatePageUI(position)
                for (i in pages.indices) {
                    if (i == position) {
                        pages[i].initData()
                    } else {
                        pages[i].dismiss()
                    }
                }
            }
        })

        // Initial setup for the first page
        updatePageUI(0)
        pages[0].initData()
    }

    private fun setupListeners() {
        val binding = getBinding() ?: return

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnPrev.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current > 0) {
                selectPage(current - 1)
            }
        }

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.size - 1) {
                selectPage(current + 1)
            }
        }

        binding.btnAutoPlay.setOnClickListener {
            toggleAutoPlay()
        }
    }

    private fun selectPage(position: Int) {
        val binding = getBinding() ?: return
        if (position in pages.indices) {
            binding.viewPager.setCurrentItem(position, true)
        }
    }

    private fun updatePageUI(position: Int) {
        val binding = getBinding() ?: return
        binding.tvPageIndicator.text = "${position + 1} / ${pages.size}"
        binding.tvCurrentStep.text = titles.getOrElse(position) { "" }

        // Update button states
        binding.btnPrev.isEnabled = position > 0
        binding.btnPrev.alpha = if (position > 0) 1.0f else 0.4f

        binding.btnNext.isEnabled = position < pages.size - 1
        binding.btnNext.alpha = if (position < pages.size - 1) 1.0f else 0.4f
    }

    private fun toggleAutoPlay() {
        val binding = getBinding() ?: return
        isAutoPlaying = !isAutoPlaying
        if (isAutoPlaying) {
            binding.btnAutoPlay.setImageResource(R.drawable.ic_pause)
            autoPlayHandler.postDelayed(autoPlayRunnable, 3000)
            showToast("已開啟自動輪播 (每 3 秒切換)")
        } else {
            binding.btnAutoPlay.setImageResource(R.drawable.ic_play_arrow)
            autoPlayHandler.removeCallbacks(autoPlayRunnable)
            showToast("已暫停自動輪播")
        }
    }

    override fun onDestroyView() {
        isAutoPlaying = false
        autoPlayHandler.removeCallbacks(autoPlayRunnable)
        pages.clear()
        super.onDestroyView()
    }

    /**
     * Page transformer providing a smooth scaling and fading transition.
     */
    private class ZoomOutPageTransformer : ViewPager.PageTransformer {
        private val minScale = 0.88f
        private val minAlpha = 0.65f

        override fun transformPage(view: View, position: Float) {
            val pageWidth = view.width
            val pageHeight = view.height

            when {
                position < -1 -> {
                    view.alpha = 0f
                }
                position <= 1 -> {
                    val scaleFactor = Math.max(minScale, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    if (position < 0) {
                        view.translationX = horzMargin - vertMargin / 2
                    } else {
                        view.translationX = -horzMargin + vertMargin / 2
                    }
                    view.scaleX = scaleFactor
                    view.scaleY = scaleFactor
                    view.alpha = minAlpha + (scaleFactor - minScale) / (1 - minScale) * (1 - minAlpha)
                }
                else -> {
                    view.alpha = 0f
                }
            }
        }
    }
}