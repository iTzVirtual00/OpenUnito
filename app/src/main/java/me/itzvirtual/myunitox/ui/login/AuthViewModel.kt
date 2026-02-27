package me.itzvirtual.myunitox.ui.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.itzvirtual.myunitox.data.local.SharedPrefStorage
import me.itzvirtual.myunitox.data.repository.StudentDataRepository
import javax.inject.Inject


// May be redudant for now
// but this is where we can put any future auth-related logic (e.g. token refresh) and keep it separate from the UI
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sharedPrefStorage: SharedPrefStorage,
    private val studentDataRepo: StudentDataRepository
) : ViewModel() {
    suspend fun isUserLoggedIn(): Boolean {
        return sharedPrefStorage.getToken() != null && studentDataRepo.checkAuth()
    }

    fun login(token: String) {
        sharedPrefStorage.saveToken(token)
    }

    fun clearToken() {
        sharedPrefStorage.clearToken()
    }
}