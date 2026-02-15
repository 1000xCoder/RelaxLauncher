package org.shekhawat.launcher.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.ThemeType

class SettingsViewModel(private val sharedPrefManager: SharedPrefManager) : ViewModel() {

    private val _theme = MutableStateFlow(getTheme())
    val theme: Flow<String> = _theme.asStateFlow()

    fun getTheme(): String {
        return getString("theme", ThemeType.LIGHT.name)
    }

    fun setTheme(theme: String) {
        saveString("theme", theme)
        _theme.value = theme
    }

    fun saveString(key: String, value: String) {
        sharedPrefManager.saveString(key, value)
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPrefManager.getString(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPrefManager.getBoolean(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPrefManager.saveBoolean(key, value)
    }
}