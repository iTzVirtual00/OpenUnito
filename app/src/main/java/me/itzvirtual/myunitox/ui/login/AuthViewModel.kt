package me.itzvirtual.myunitox.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.repository.AppConfigRepository
import me.itzvirtual.myunitox.data.repository.StudentDataRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appConfigRepo: AppConfigRepository,
    private val studentDataRepo: StudentDataRepository,
) : ViewModel() {

    /** Reactive auth state: null = still loading from DB, true/false = resolved. */
    val isAuthenticated = appConfigRepo.config
        .map { it?.authToken != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    suspend fun checkAuth(): Boolean {
        return studentDataRepo.checkAuth()
    }

    fun login(token: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            appConfigRepo.saveToken(token)
            onComplete()
        }
    }

    fun clearToken(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            appConfigRepo.clearAuth()
            onComplete()
        }
    }
}