package me.itzvirtual.myunitox.ui.login

import android.annotation.SuppressLint
import android.util.Base64
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun UniToLoginScreen(
    startUrl: String,
    onAuthSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()

            WebView(context).apply {
                WebStorage.getInstance().deleteAllData()
                clearHistory()
                clearFormData()

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val url = request?.url?.toString() ?: return false
                        if (url.startsWith("unito-prod://")) {
                            // Regex?
                            val token = url.
                                replace("unito-prod://", "").
                                replace("?username=", "").
                                replace("&password=", ":")
                            val tokenb64 = Base64.encodeToString(token.toByteArray(), Base64.NO_WRAP)
                            onAuthSuccess("Basic $tokenb64")
                            return true
                        }
                        return false
                    }
                }
                loadUrl(startUrl)
            }
        },
        onRelease = { it.destroy() } // Ensure total destruction on finish
    )
}