package me.itzvirtual.myunitox

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class MyUnitoXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyUnitoXApp)
            modules(AppModule().module)
        }
    }
}
