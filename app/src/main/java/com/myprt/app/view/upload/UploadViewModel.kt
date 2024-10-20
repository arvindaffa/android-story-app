package com.myprt.app.view.upload

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myprt.app.data.Resource
import com.myprt.app.data.repository.ApiRepository
import com.myprt.app.view.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel(
    private val apiRepository: ApiRepository
) : ViewModel() {

    private val _uploadUiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadUiState: StateFlow<UploadUiState> get() = _uploadUiState.asStateFlow()

    fun uploadImage(imageFile: File, description: String, location: Location? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            apiRepository.uploadImage(imageFile, description, location).collect { result ->
                when (result) {
                    is Resource.Loading -> _uploadUiState.update { UploadUiState.Loading }
                    is Resource.Success -> _uploadUiState.update { UploadUiState.Success(result.data) }
                    is Resource.Error -> _uploadUiState.update { UploadUiState.Error(result.message) }
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
                UploadViewModel(application.injection.apiRepository)
            }
        }
    }
}

sealed class UploadUiState {
    data object Idle : UploadUiState()
    data object Loading : UploadUiState()
    data class Success(val message: String) : UploadUiState()
    data class Error(val errorMessage: String) : UploadUiState()
}