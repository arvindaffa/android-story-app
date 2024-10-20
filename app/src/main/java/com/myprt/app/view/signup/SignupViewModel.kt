package com.myprt.app.view.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myprt.app.data.Resource
import com.myprt.app.data.repository.ApiRepository
import com.myprt.app.view.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel(
    private val apiRepository: ApiRepository
) : ViewModel() {

    private val _signupUiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val signupUiState: StateFlow<SignupUiState> get() = _signupUiState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            apiRepository.register(name, email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> _signupUiState.update { SignupUiState.Loading }
                    is Resource.Success -> _signupUiState.update { SignupUiState.Success }
                    is Resource.Error -> _signupUiState.update { SignupUiState.Error(result.message) }
                    else -> Unit
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                SignupViewModel(application.injection.apiRepository)
            }
        }
    }
}

sealed class SignupUiState {
    data object Idle : SignupUiState()
    data object Loading : SignupUiState()
    data object Success : SignupUiState()
    data class Error(val errorMessage: String) : SignupUiState()
}