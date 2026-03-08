package me.itzvirtual.myunitox.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import me.itzvirtual.myunitox.data.local.dao.AppConfigDao
import me.itzvirtual.myunitox.data.models.tables.AppConfig

@Database(
    entities = [AppConfig::class], version = 1, exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): AppConfigDao
}