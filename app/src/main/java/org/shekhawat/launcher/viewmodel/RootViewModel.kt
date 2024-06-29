package org.shekhawat.launcher.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.shekhawat.launcher.SharedPrefManager

class RootViewModel(private val sharedPrefManager: SharedPrefManager) : ViewModel() {

}