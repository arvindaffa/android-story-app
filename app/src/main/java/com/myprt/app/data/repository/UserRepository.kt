package com.myprt.app.data.repository

import android.util.Log
import com.myprt.app.data.Resource
import com.myprt.app.data.model.User
import com.myprt.app.data.source.local.DataStorePreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UserRepository(
    private val dataStore: DataStorePreferences,
    private val coroutineDispatcher: CoroutineDispatcher
) {
    suspend fun isUserLoggedIn(): Flow<Resource<Boolean>> {
        return flow<Resource<Boolean>> {
            dataStore.user.collect {
                emit(Resource.Success(it?.token?.isNotEmpty() ?: false))
            }
        }.catch { exception ->
            val message = exception.localizedMessage.orEmpty()
            Log.e("UserRepository", message, exception)
            emit(Resource.Error(-1, message))
        }.flowOn(coroutineDispatcher)
    }

    suspend fun storeUser(user: User) {
        dataStore.storeUser(user)
    }

    fun getUser(): Flow<User?> {
        return dataStore.user
    }

    suspend fun logout() {
        dataStore.clear()
    }
}