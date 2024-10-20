package com.myprt.app.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.myprt.app.data.model.Story
import com.myprt.app.data.repository.ApiRepository
import com.myprt.app.view.MyApplication

class MainViewModel(
    private val apiRepository: ApiRepository
) : ViewModel() {

    fun getListStory(): LiveData<PagingData<Story>> =
        apiRepository.getPagingListStory().cachedIn(viewModelScope)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                MainViewModel(application.injection.apiRepository)
            }
        }
    }
}