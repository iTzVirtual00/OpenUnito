package me.itzvirtual.myunitox.ui.devtools

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.itzvirtual.myunitox.data.remote.AuthStorage
import javax.inject.Inject

@HiltViewModel
class DevtoolsViewModel @Inject constructor(
    private val authStorage: AuthStorage
) : ViewModel() {

    fun login(token: String) {
        authStorage.saveToken(token)
    }

    fun clearToken() {
        authStorage.clearToken()
    }

    fun copyTokenToClipboard(context: Context) {
        val token = authStorage.getToken()
        if (token != null) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("UnitoX Token", token)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}