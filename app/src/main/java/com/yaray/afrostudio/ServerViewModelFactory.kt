package com.yaray.afrostudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ServerViewModelFactory(private val serverRepository: ServerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServerViewModel(serverRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
