package me.itzvirtual.myunitox.data.local

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val PREF_NAME = "secure_auth_prefs"
        private const val KEY_BASIC_AUTH = "basic_auth_token"

        private const val KEY_CALENDAR_LINK_ID = "calendar_link_id"
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
    /*
    * BASIC AUTH
    * */
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


    /*
    * CALENDAR
    * */
    fun setCalendarLinkId(linkId: String) {
        sharedPreferences.edit(commit = true) { putString(KEY_CALENDAR_LINK_ID, linkId.trim()) }
    }

    fun getCalendarLinkId(): String? {
        return sharedPreferences.getString(KEY_CALENDAR_LINK_ID, null)
    }

    fun getCalendarClientId(): String {
        return "5852fd1ab7305612a8354d51" // hardcoded for now, seems to work with any calendar...
    }

}