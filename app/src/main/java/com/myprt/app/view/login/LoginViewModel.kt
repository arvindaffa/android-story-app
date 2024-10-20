package com.myprt.app.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myprt.app.data.Resource
import com.myprt.app.data.model.User
import com.myprt.app.data.repository.ApiRepository
import com.myprt.app.data.repository.UserRepository
import com.myprt.app.view.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val apiRepository: ApiRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> get() = _loginUiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            apiRepository.login(email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> _loginUiState.update { LoginUiState.Loading }
                    is Resource.Success -> _loginUiState.update { LoginUiState.Success(result.data) }
                    is Resource.Error -> _loginUiState.update { LoginUiState.Error(result.message) }
                    else -> Unit
                }
            }
        }
    }

    fun storeUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.storeUser(user)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                LoginViewModel(application.injection.apiRepository, application.injection.userRepository)
            }
        }
    }
}

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val loginResult: User) : LoginUiState()
    data class Error(val errorMessage: String) : LoginUiState()
}