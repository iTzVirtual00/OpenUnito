package me.itzvirtual.myunitox.ui.devtools

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.repository.AppConfigRepository
import javax.inject.Inject

@HiltViewModel
class DevtoolsViewModel @Inject constructor(
    private val appConfigRepo: AppConfigRepository
) : ViewModel() {

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

    fun copyTokenToClipboard(context: Context) {
        viewModelScope.launch {
            val token = appConfigRepo.getToken()
            if (token != null) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("UnitoX Token", token)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setCalendarLinkId(linkId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            appConfigRepo.setCalendarLinkId(linkId)
            onComplete()
        }
    }
}