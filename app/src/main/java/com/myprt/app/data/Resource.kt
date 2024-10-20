package com.myprt.app.data

sealed class Resource<out T> {
    data class Success<T>(val data: T): Resource<T>()
    data class Error(val errorCode: Int, val message: String): Resource<Nothing>()
    data object Loading: Resource<Nothing>()
    data object Empty: Resource<Nothing>()
}