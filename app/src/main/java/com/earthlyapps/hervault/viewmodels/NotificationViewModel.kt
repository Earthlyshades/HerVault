package com.earthlyapps.hervault.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.earthlyapps.hervault.models.Notification
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {
    private val _currentNotification = MutableStateFlow<Notification?>(null)
    val currentNotification: StateFlow<Notification?> = _currentNotification.asStateFlow()

    fun clearNotification() {
        _currentNotification.value = null
    }

    fun showRandomNotification() {
        _currentNotification.value = Notification.supportMessages.random()
        viewModelScope.launch {
            delay(5000)
            clearNotification()
        }
    }
}