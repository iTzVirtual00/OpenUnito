package me.itzvirtual.myunitox.ui.devtools

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.itzvirtual.myunitox.data.local.SharedPrefStorage
import javax.inject.Inject

@HiltViewModel
class DevtoolsViewModel @Inject constructor(
    private val sharedPrefStorage: SharedPrefStorage
) : ViewModel() {

    fun login(token: String) {
        sharedPrefStorage.saveToken(token)
    }

    fun clearToken() {
        sharedPrefStorage.clearToken()
    }

    fun copyTokenToClipboard(context: Context) {
        val token = sharedPrefStorage.getToken()
        if (token != null) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("UnitoX Token", token)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun setCalendarLinkId(linkId: String) {
        sharedPrefStorage.setCalendarLinkId(linkId)
    }
}