package com.myprt.app.data

import android.content.Context
import com.myprt.app.BuildConfig
import com.myprt.app.data.repository.ApiRepository
import com.myprt.app.data.repository.UserRepository
import com.myprt.app.data.source.local.DataStorePreferences
import com.myprt.app.data.source.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Injection(
    private val context: Context
) {
    private val coroutineDispatcher by lazy {
        Dispatchers.IO
    }

    private val dataStorePreferences by lazy {
        DataStorePreferences(context)
    }

    val userRepository by lazy {
        UserRepository(dataStorePreferences, coroutineDispatcher)
    }

    private fun client(): OkHttpClient {
        val loggingInterceptor = if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        } else {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { dataStorePreferences.user.first()?.token.orEmpty() }
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://story-api.dicoding.dev/v1/")
        .client(client())
        .build()

    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val apiRepository by lazy {
        ApiRepository(apiService, coroutineDispatcher)
    }
}