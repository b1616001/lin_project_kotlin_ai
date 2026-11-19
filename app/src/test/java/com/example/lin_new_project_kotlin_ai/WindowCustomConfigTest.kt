package com.example.lin_new_project_kotlin_ai

import com.example.lin_new_project_kotlin_ai.window.WindowConfigStore
import com.example.lin_new_project_kotlin_ai.window.WindowCustomConfig
import com.example.lin_new_project_kotlin_ai.window.WindowMode
import com.example.lin_new_project_kotlin_ai.window.WindowSizePreset
import com.example.lin_new_project_kotlin_ai.window.WindowThemePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowCustomConfigTest {

    @Test
    fun testDefaultConfigValues() {
        val config = WindowCustomConfig()
        assertEquals(WindowMode.IN_APP, config.mode)
        assertEquals(WindowSizePreset.MEDIUM, config.sizePreset)
        assertEquals(WindowThemePreset.DARK, config.themePreset)
        assertEquals(95, config.alphaPercent)
        assertEquals(18, config.cornerRadiusDp)
        assertTrue(config.autoSnapEdge)
        assertFalse(config.lockPosition)
        assertFalse(config.keepScreenOn)
        assertFalse(config.secureFlag)
        assertFalse(config.collapsed)
    }

    @Test
    fun testNextThemeCyclesCorrectly() {
        val dark = WindowCustomConfig(themePreset = WindowThemePreset.DARK)
        assertEquals(WindowThemePreset.TEAL, dark.nextTheme())

        val teal = WindowCustomConfig(themePreset = WindowThemePreset.TEAL)
        assertEquals(WindowThemePreset.LIGHT, teal.nextTheme())

        val light = WindowCustomConfig(themePreset = WindowThemePreset.LIGHT)
        assertEquals(WindowThemePreset.DARK, light.nextTheme())
    }

    @Test
    fun testStoreFallbackWhenMmkvUnavailable() {
        val loaded = WindowConfigStore.load()
        assertEquals(WindowMode.IN_APP, loaded.mode)
        assertEquals(WindowSizePreset.MEDIUM, loaded.sizePreset)
    }
}
