package me.itzvirtual.openunito

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import me.itzvirtual.openunito.core.database.AppDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

actual fun platform() = "Android"

@Module
actual class PlatformModule {
    @Single
    fun databaseBuilder(@Provided context: Context): RoomDatabase.Builder<AppDatabase>{
        val dbFile = context.getDatabasePath("app.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        ).setDriver(BundledSQLiteDriver())
    }
}