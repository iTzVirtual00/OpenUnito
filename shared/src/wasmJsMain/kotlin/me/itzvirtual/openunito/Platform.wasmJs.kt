package me.itzvirtual.openunito

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import me.itzvirtual.openunito.core.database.AppDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.w3c.dom.Worker

actual fun platform(): String = "wasmjs"

@Module
class WasmPlatformModule {
    @Single
    fun databaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        return Room.databaseBuilder<AppDatabase>(
            name = "app.db",
        ).setDriver(
            WebWorkerSQLiteDriver(
                Worker("sqlite.worker.js")
            )
        )
    }
}