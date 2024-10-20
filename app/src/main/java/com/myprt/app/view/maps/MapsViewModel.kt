package com.myprt.app.view.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myprt.app.data.Resource
import com.myprt.app.data.model.Story
import com.myprt.app.data.repository.ApiRepository
import com.myprt.app.view.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapsViewModel(
    private val repository: ApiRepository
) : ViewModel() {

    private val _mapsUiState = MutableStateFlow<MapsUiState>(MapsUiState.Loading)
    val mapsUiState = _mapsUiState.asStateFlow()

    init {
        getListStory()
    }

    private fun getListStory() {
        viewModelScope.launch {
            repository.getStories(location = 1).collect { result ->
                when (result) {
                    is Resource.Success -> _mapsUiState.update { MapsUiState.Success(result.data) }
                    is Resource.Error -> _mapsUiState.update { MapsUiState.Error(result.message) }
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
                MapsViewModel(application.injection.apiRepository)
            }
        }
    }
}

sealed class MapsUiState {
    data object Loading : MapsUiState()
    data class Success(val listStory: List<Story>) : MapsUiState()
    data class Error(val errorMessage: String) : MapsUiState()
}