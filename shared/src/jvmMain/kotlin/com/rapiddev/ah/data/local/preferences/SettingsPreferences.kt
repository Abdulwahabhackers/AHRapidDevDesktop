package com.rapiddev.ah.data.local.preferences

import com.rapiddev.ah.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.prefs.Preferences

class SettingsPreferences {

    private val prefs: Preferences = Preferences.userRoot().node("com/rapiddev/ah/settings")

    private val _themeMode = MutableStateFlow(
        ThemeMode.fromString(prefs.get(KEY_THEME, ThemeMode.SYSTEM.name))
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private val _onboardingSeen = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING, false)
    )
    val onboardingSeen: StateFlow<Boolean> = _onboardingSeen

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.put(KEY_THEME, mode.name)
    }

    fun markOnboardingSeen() {
        _onboardingSeen.value = true
        prefs.putBoolean(KEY_ONBOARDING, true)
    }

    fun resetOnboarding() {
        _onboardingSeen.value = false
        prefs.putBoolean(KEY_ONBOARDING, false)
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_ONBOARDING = "onboarding_seen"
    }
}