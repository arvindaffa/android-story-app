package com.myprt.app.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.myprt.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStorePreferences constructor(
    private val context: Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

    val user: Flow<User?>
        get() = context.dataStore.data.map { pref ->
            val userJson = pref[USER_KEY].orEmpty()
            Gson().fromJson(userJson, User::class.java)
        }

    suspend fun storeUser(user: User) {
        context.dataStore.edit { pref ->
            pref[USER_KEY] = Gson().toJson(user)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { pref ->
            pref.clear()
        }
    }

    companion object {
        val USER_KEY = stringPreferencesKey("USER")
        private const val DATA_STORE_NAME = "myPRT"
    }
}