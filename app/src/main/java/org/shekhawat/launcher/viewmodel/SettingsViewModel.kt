package org.shekhawat.launcher.viewmodel

import androidx.lifecycle.ViewModel
import org.shekhawat.launcher.SharedPrefManager

class SettingsViewModel(private val sharedPrefManager: SharedPrefManager): ViewModel() {
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