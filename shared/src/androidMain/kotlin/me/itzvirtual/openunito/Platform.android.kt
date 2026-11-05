package me.itzvirtual.openunito

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import me.itzvirtual.openunito.core.database.AppDatabase
import me.itzvirtual.openunito.data.localization.PlatformLanguageManager
import me.itzvirtual.openunito.data.localization.SupportedLanguage
import me.itzvirtual.openunito.domain.AndroidClipboardManagerImpl
import me.itzvirtual.openunito.domain.AndroidLanguageManagerImpl
import me.itzvirtual.openunito.domain.PlatformClipboardManager
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

actual fun platform() = "Android"

@Module
class AndroidPlatformModule {
    @Single
    fun databaseBuilder(@Provided context: Context): RoomDatabase.Builder<AppDatabase>{
        val dbFile = context.getDatabasePath("app.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        )
    }

    @Single
    fun clipboardManager(@Provided context: Context): PlatformClipboardManager =
        AndroidClipboardManagerImpl(context)

    @Single
    fun platformLanguageManager(@Provided context: Context): PlatformLanguageManager =
        AndroidLanguageManagerImpl(context)
}