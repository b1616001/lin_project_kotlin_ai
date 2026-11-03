package com.example.lin_new_project_kotlin_ai

import android.graphics.BitmapFactory
import com.example.lin_new_project_kotlin_ai.`fun`.ImageMethod
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageMethodTest {

    @Test
    fun testCalculateInSampleSize_smallerImage_returnsOne() {
        val options = BitmapFactory.Options().apply {
            outWidth = 100
            outHeight = 100
        }
        val sampleSize = ImageMethod.calculateInSampleSize(options, 200, 200)
        assertEquals(1, sampleSize)
    }

    @Test
    fun testCalculateInSampleSize_largerImage_returnsPowerOfTwo() {
        val options = BitmapFactory.Options().apply {
            outWidth = 1000
            outHeight = 1000
        }
        val sampleSize = ImageMethod.calculateInSampleSize(options, 250, 250)
        assertEquals(4, sampleSize)
    }

    @Test
    fun testCalculateInSampleSize_zeroDimensions_returnsAtLeastOne() {
        val options = BitmapFactory.Options().apply {
            outWidth = 0
            outHeight = 0
        }
        val sampleSize = ImageMethod.calculateInSampleSize(options, 100, 100)
        assertEquals(1, sampleSize)
    }
}
