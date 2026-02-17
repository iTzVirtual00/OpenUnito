package me.itzvirtual.myunitox.data.remote

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val PREF_NAME = "secure_auth_prefs"
        private const val KEY_BASIC_AUTH = "basic_auth_token"
    }

    // We initialize this lazily so it doesn't slow down app startup
    private val sharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        Log.i("AuthStorage", "Saving token")
        sharedPreferences.edit(commit = true) { putString(KEY_BASIC_AUTH, token.trim()) }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_BASIC_AUTH, null)
    }

    fun clearToken() {
        Log.i("AuthStorage", "Clearing stored token")
        sharedPreferences.edit(commit = true) { remove(KEY_BASIC_AUTH) }
    }
}