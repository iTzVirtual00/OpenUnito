package me.itzvirtual.openunito

import androidx.room3.Room
import androidx.room3.RoomDatabase
import me.itzvirtual.openunito.core.database.AppDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

expect fun platform(): String


@Module
expect class PlatformModule()