package com.example.lin_new_project_kotlin_ai.window

import com.tencent.mmkv.MMKV

enum class WindowMode(val label: String) {
    IN_APP("應用內懸浮"),
    SYSTEM_OVERLAY("全局懸浮")
}

enum class WindowSizePreset(val widthDp: Int, val label: String) {
    COMPACT(188, "精巧"),
    MEDIUM(228, "標準"),
    LARGE(268, "寬大")
}

enum class WindowThemePreset(val label: String) {
    DARK("深邃曜石"),
    TEAL("科技青綠"),
    LIGHT("極簡亮白")
}

enum class DockCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

data class WindowCustomConfig(
    val mode: WindowMode = WindowMode.IN_APP,
    val sizePreset: WindowSizePreset = WindowSizePreset.MEDIUM,
    val themePreset: WindowThemePreset = WindowThemePreset.DARK,
    val alphaPercent: Int = 95,
    val cornerRadiusDp: Int = 18,
    val autoSnapEdge: Boolean = true,
    val lockPosition: Boolean = false,
    val keepScreenOn: Boolean = false,
    val secureFlag: Boolean = false,
    val collapsed: Boolean = false,
    val posX: Int = -1,
    val posY: Int = -1
) {
    fun nextTheme(): WindowThemePreset {
        val values = WindowThemePreset.entries
        return values[(themePreset.ordinal + 1) % values.size]
    }
}

object WindowConfigStore {
    private const val KEY_MODE = "win_custom_mode"
    private const val KEY_SIZE = "win_custom_size"
    private const val KEY_THEME = "win_custom_theme"
    private const val KEY_ALPHA = "win_custom_alpha"
    private const val KEY_RADIUS = "win_custom_radius"
    private const val KEY_AUTO_SNAP = "win_custom_auto_snap"
    private const val KEY_LOCK_POS = "win_custom_lock_pos"
    private const val KEY_KEEP_SCREEN = "win_custom_keep_screen"
    private const val KEY_SECURE = "win_custom_secure"
    private const val KEY_COLLAPSED = "win_custom_collapsed"
    private const val KEY_POS_X = "win_custom_pos_x"
    private const val KEY_POS_Y = "win_custom_pos_y"

    fun load(): WindowCustomConfig {
        return try {
            val mmkv = MMKV.defaultMMKV()
            val modeName = mmkv.decodeString(KEY_MODE, WindowMode.IN_APP.name)
            val sizeName = mmkv.decodeString(KEY_SIZE, WindowSizePreset.MEDIUM.name)
            val themeName = mmkv.decodeString(KEY_THEME, WindowThemePreset.DARK.name)

            WindowCustomConfig(
                mode = WindowMode.entries.firstOrNull { it.name == modeName } ?: WindowMode.IN_APP,
                sizePreset = WindowSizePreset.entries.firstOrNull { it.name == sizeName } ?: WindowSizePreset.MEDIUM,
                themePreset = WindowThemePreset.entries.firstOrNull { it.name == themeName } ?: WindowThemePreset.DARK,
                alphaPercent = mmkv.decodeInt(KEY_ALPHA, 95).coerceIn(40, 100),
                cornerRadiusDp = mmkv.decodeInt(KEY_RADIUS, 18).coerceIn(8, 28),
                autoSnapEdge = mmkv.decodeBool(KEY_AUTO_SNAP, true),
                lockPosition = mmkv.decodeBool(KEY_LOCK_POS, false),
                keepScreenOn = mmkv.decodeBool(KEY_KEEP_SCREEN, false),
                secureFlag = mmkv.decodeBool(KEY_SECURE, false),
                collapsed = mmkv.decodeBool(KEY_COLLAPSED, false),
                posX = mmkv.decodeInt(KEY_POS_X, -1),
                posY = mmkv.decodeInt(KEY_POS_Y, -1)
            )
        } catch (_: Throwable) {
            WindowCustomConfig()
        }
    }

    fun save(config: WindowCustomConfig) {
        try {
            val mmkv = MMKV.defaultMMKV()
            mmkv.encode(KEY_MODE, config.mode.name)
            mmkv.encode(KEY_SIZE, config.sizePreset.name)
            mmkv.encode(KEY_THEME, config.themePreset.name)
            mmkv.encode(KEY_ALPHA, config.alphaPercent.coerceIn(40, 100))
            mmkv.encode(KEY_RADIUS, config.cornerRadiusDp.coerceIn(8, 28))
            mmkv.encode(KEY_AUTO_SNAP, config.autoSnapEdge)
            mmkv.encode(KEY_LOCK_POS, config.lockPosition)
            mmkv.encode(KEY_KEEP_SCREEN, config.keepScreenOn)
            mmkv.encode(KEY_SECURE, config.secureFlag)
            mmkv.encode(KEY_COLLAPSED, config.collapsed)
            mmkv.encode(KEY_POS_X, config.posX)
            mmkv.encode(KEY_POS_Y, config.posY)
        } catch (_: Throwable) {
            // Ignore when MMKV is unavailable in tests
        }
    }

    fun reset(): WindowCustomConfig {
        val defaults = WindowCustomConfig()
        save(defaults)
        return defaults
    }
}
