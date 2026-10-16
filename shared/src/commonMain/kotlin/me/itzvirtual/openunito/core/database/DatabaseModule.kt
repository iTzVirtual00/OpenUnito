package me.itzvirtual.openunito.core.database

import co.touchlab.kermit.Logger
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import me.itzvirtual.openunito.data.agenda.local.AgendaDao
import me.itzvirtual.openunito.data.calendar.local.CalendarConfigDao
import me.itzvirtual.openunito.data.config.local.AppConfigDao
import me.itzvirtual.openunito.data.dashboard.local.DashboardDao
import me.itzvirtual.openunito.data.examdate.local.ExamDateDao
import me.itzvirtual.openunito.data.examresult.local.ExamResultDao
import me.itzvirtual.openunito.data.grade.local.ExamGradeDao
import me.itzvirtual.openunito.data.student.local.StudentDao
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

private fun dbInitialize(db: SQLiteConnection) {
    db.execSQL("INSERT OR IGNORE INTO app_config (id) VALUES (0)")
}

@Module
class DatabaseModule {

    @Single
    fun provideDatabase(@Provided builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
        builder
            .addCallback(object : RoomDatabase.Callback() {
                override suspend fun onCreate(db: SQLiteConnection) {
                    super.onCreate(db)
                    Logger.i(tag = "Database") { "Pre-populating database with initial data" }
                    dbInitialize(db)
                }
            })
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Factory
    fun provideConfigDao(db: AppDatabase): AppConfigDao = db.configDao()

    @Factory
    fun provideAgendaDao(db: AppDatabase): AgendaDao = db.agendaDao()

    @Factory
    fun provideCalendarConfigDao(db: AppDatabase): CalendarConfigDao = db.calendarConfigDao()

    @Factory
    fun provideDashboardDao(db: AppDatabase): DashboardDao = db.dashboardDao()

    @Factory
    fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()

    @Factory
    fun provideExamResultDao(db: AppDatabase): ExamResultDao = db.examResultDao()

    @Factory
    fun provideExamDateDao(db: AppDatabase): ExamDateDao = db.examDateDao()

    @Factory
    fun provideExamGradeDao(db: AppDatabase): ExamGradeDao = db.examGradeDao()
}
