package me.itzvirtual.myunitox.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.itzvirtual.myunitox.data.local.AppDatabase
import me.itzvirtual.myunitox.data.local.dao.AppConfigDao
import javax.inject.Singleton

fun dbInitialize(db: SupportSQLiteDatabase) {
    db.execSQL("INSERT OR IGNORE INTO app_config (id, calendarClientId) VALUES (0, '5852fd1ab7305612a8354d51')")
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_unitox_db"
        )
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.i("Database", "Pre-populating database with initial data")
                        dbInitialize(db)
                    }
                }
            )
            .build()
    }

    @Provides
    fun provideConfigDao(db: AppDatabase): AppConfigDao {
        return db.configDao()
    }
}