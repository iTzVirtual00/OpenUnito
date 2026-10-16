package me.itzvirtual.openunito

import android.app.Application
import org.koin.android.ext.koin.androidContext

class OpenUnitoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@OpenUnitoApp) }
    }
}
