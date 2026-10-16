package me.itzvirtual.openunito.ui.feature_login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.student.StudentRepository
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AuthViewModel(
    private val appConfigRepo: AppConfigRepository,
    private val studentRepo: StudentRepository,
) : ViewModel() {

    val isAuthenticated = appConfigRepo.config
        .map { it?.authToken != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun login(token: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            appConfigRepo.saveToken(token)
            onComplete()
        }
    }

    fun clearToken(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            appConfigRepo.clearAuth()
            studentRepo.clearCache()
            onComplete()
        }
    }
}
