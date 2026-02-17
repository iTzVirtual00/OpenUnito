package me.itzvirtual.myunitox.ui.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.itzvirtual.myunitox.data.remote.AuthStorage
import javax.inject.Inject


// May be redudant for now
// but this is where we can put any future auth-related logic (e.g. token refresh) and keep it separate from the UI
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authStorage: AuthStorage
) : ViewModel() {
    fun isUserLoggedIn(): Boolean {
        return authStorage.getToken() != null
    }

    fun login(token: String) {
        authStorage.saveToken(token)
    }

    fun clearToken() {
        authStorage.clearToken()
    }
}