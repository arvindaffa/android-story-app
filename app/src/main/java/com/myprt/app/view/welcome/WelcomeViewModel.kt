package com.myprt.app.view.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myprt.app.data.Resource
import com.myprt.app.data.repository.UserRepository
import com.myprt.app.view.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _welcomeUiState = MutableStateFlow<WelcomeUiState>(WelcomeUiState.Idle)
    val welcomeUiState: StateFlow<WelcomeUiState> get() = _welcomeUiState.asStateFlow()

    init {
        getIsUserLoggedIn()
    }

    private fun getIsUserLoggedIn() {
        viewModelScope.launch {
            val result = userRepository.isUserLoggedIn().first()
            if (result is Resource.Success && result.data) {
                _welcomeUiState.update {
                    WelcomeUiState.Authenticated
                }
            } else {
                _welcomeUiState.update {
                    WelcomeUiState.Unauthenticated
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                WelcomeViewModel(application.injection.userRepository)
            }
        }
    }
}

sealed class WelcomeUiState {
    data object Idle : WelcomeUiState()
    data object Authenticated : WelcomeUiState()
    data object Unauthenticated : WelcomeUiState()
}