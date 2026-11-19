package com.example.lin_new_project_kotlin_ai

import com.example.lin_new_project_kotlin_ai.view.ScaleImageView
import org.junit.Assert.assertEquals
import org.junit.Test

class ScaleImageViewBoundsTest {

    @Test
    fun computeClampedScaleFactor_withinBounds_keepsRawFactor() {
        val factor = ScaleImageView.computeClampedScaleFactor(
            startScale = 2.0f,
            rawScaleFactor = 1.5f,
            minScale = 1.0f,
            maxScale = 5.0f
        )
        assertEquals(1.5f, factor, 0.0001f)
    }

    @Test
    fun computeClampedScaleFactor_belowMin_clampsToMinScale() {
        val factor = ScaleImageView.computeClampedScaleFactor(
            startScale = 2.0f,
            rawScaleFactor = 0.25f,
            minScale = 1.0f,
            maxScale = 5.0f
        )
        assertEquals(0.5f, factor, 0.0001f)
    }

    @Test
    fun computeClampedScaleFactor_aboveMax_clampsToMaxScale() {
        val factor = ScaleImageView.computeClampedScaleFactor(
            startScale = 2.5f,
            rawScaleFactor = 4.0f,
            minScale = 1.0f,
            maxScale = 5.0f
        )
        assertEquals(2.0f, factor, 0.0001f)
    }

    @Test
    fun computeTranslationDelta_smallerThanView_centersOnBothAxes() {
        val (dx, dy) = ScaleImageView.computeTranslationDelta(
            rectLeft = 200f,
            rectTop = 100f,
            rectRight = 700f,
            rectBottom = 600f,
            viewWidth = 1000f,
            viewHeight = 1600f
        )
        // Target left = (1000 - 500)/2 = 250 -> dx = 50
        // Target top = (1600 - 500)/2 = 550 -> dy = 450
        assertEquals(50f, dx, 0.0001f)
        assertEquals(450f, dy, 0.0001f)
    }

    @Test
    fun computeTranslationDelta_largerThanView_clampsLeftAndTopGap() {
        val (dx, dy) = ScaleImageView.computeTranslationDelta(
            rectLeft = 120f,
            rectTop = 80f,
            rectRight = 2120f,
            rectBottom = 2080f,
            viewWidth = 1000f,
            viewHeight = 1500f
        )
        assertEquals(-120f, dx, 0.0001f)
        assertEquals(-80f, dy, 0.0001f)
    }

    @Test
    fun computeTranslationDelta_largerThanView_clampsRightAndBottomGap() {
        val (dx, dy) = ScaleImageView.computeTranslationDelta(
            rectLeft = -1200f,
            rectTop = -800f,
            rectRight = 800f,
            rectBottom = 1200f,
            viewWidth = 1000f,
            viewHeight = 1500f
        )
        assertEquals(200f, dx, 0.0001f)
        assertEquals(300f, dy, 0.0001f)
    }

    @Test
    fun computeTranslationDelta_largerThanViewWithinBounds_returnsZero() {
        val (dx, dy) = ScaleImageView.computeTranslationDelta(
            rectLeft = -300f,
            rectTop = -200f,
            rectRight = 1700f,
            rectBottom = 1800f,
            viewWidth = 1000f,
            viewHeight = 1500f
        )
        assertEquals(0f, dx, 0.0001f)
        assertEquals(0f, dy, 0.0001f)
    }
}
