package com.myprt.app.data.repository

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.myprt.app.data.Resource
import com.myprt.app.data.model.ErrorResponse
import com.myprt.app.data.model.Story
import com.myprt.app.data.model.User
import com.myprt.app.data.source.remote.ApiService
import com.myprt.app.data.source.remote.PagingDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class ApiRepository(
    private val apiService: ApiService, private val coroutineDispatcher: CoroutineDispatcher
) {
    suspend fun register(name: String, email: String, password: String): Flow<Resource<String>> =
        flow {
            val response = apiService.register(name, email, password)

            if (!response.error) {
                emit(Resource.Success(response.message))
            } else {
                emit(Resource.Error(-1, response.message))
            }
        }.catch { exception ->
            val errorMessage = exception.getErrorMessage()
            Log.e(ApiRepository::class.java.simpleName, errorMessage, exception)
            emit(Resource.Error(-1, errorMessage))
        }.onStart {
            emit(Resource.Loading)
        }.flowOn(coroutineDispatcher)

    suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        val response = apiService.login(email, password)

        if (!response.error && response.loginResult != null) {
            emit(Resource.Success(response.loginResult))
        } else {
            emit(Resource.Error(-1, response.message))
        }
    }.catch { exception ->
        val errorMessage = exception.getErrorMessage()
        Log.e(ApiRepository::class.java.simpleName, errorMessage, exception)
        emit(Resource.Error(-1, errorMessage))
    }.onStart {
        emit(Resource.Loading)
    }.flowOn(coroutineDispatcher)

    suspend fun getStories(location: Int? = null): Flow<Resource<List<Story>>> = flow {
        val response = apiService.getStories(location = location)

        if (!response.error && response.listStory != null) {
            emit(Resource.Success(response.listStory))
        } else {
            emit(Resource.Error(-1, response.message))
        }
    }.catch { exception ->
        val errorMessage = exception.getErrorMessage()
        Log.e(ApiRepository::class.java.simpleName, errorMessage, exception)
        emit(Resource.Error(-1, errorMessage))
    }.onStart {
        emit(Resource.Loading)
    }.flowOn(coroutineDispatcher)

    suspend fun uploadImage(imageFile: File, description: String, location: Location? = null): Flow<Resource<String>> = flow {
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo", imageFile.name, requestImageFile
        )
        val latitude = location?.latitude?.toString()?.toRequestBody("text/plain".toMediaType())
        val longitude = location?.longitude?.toString()?.toRequestBody("text/plain".toMediaType())

        val response = apiService.uploadImage(multipartBody, requestBody, latitude, longitude)

        if (!response.error) {
            emit(Resource.Success(response.message))
        } else {
            emit(Resource.Error(-1, response.message))
        }
    }.catch { exception ->
        val errorMessage = exception.getErrorMessage()
        Log.e(ApiRepository::class.java.simpleName, errorMessage, exception)
        emit(Resource.Error(-1, errorMessage))
    }.onStart {
        emit(Resource.Loading)
    }.flowOn(coroutineDispatcher)

    fun getPagingListStory(): LiveData<PagingData<Story>> {
        return Pager(config = PagingConfig(
            pageSize = 10, enablePlaceholders = false
        ), pagingSourceFactory = {
            PagingDataSource(
                apiService = apiService
            )
        }).liveData
    }

    private fun Throwable.getErrorMessage(): String {
        return when (this) {
            is HttpException -> this.response()?.errorBody()?.let { responseBody ->
                try {
                    val body = Gson().fromJson(responseBody.string(), ErrorResponse::class.java)
                    val message = body?.message ?: "Internal Server Error"
                    message.replace("\"", "").replaceFirstChar { it.uppercase() }
                } catch (e: JsonParseException) {
                    "Internal Server Error"
                }
            } ?: "Internal Server Error"

            else -> this.localizedMessage.orEmpty()
        }
    }
}