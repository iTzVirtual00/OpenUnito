package me.itzvirtual.myunitox.core.database

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import me.itzvirtual.myunitox.data.agenda.local.AgendaDao
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfigDao
import me.itzvirtual.myunitox.data.config.local.AppConfigDao
import me.itzvirtual.myunitox.data.dashboard.local.DashboardDao
import me.itzvirtual.myunitox.data.examdate.local.ExamDateDao
import me.itzvirtual.myunitox.data.examresult.local.ExamResultDao
import me.itzvirtual.myunitox.data.grade.local.ExamGradeDao
import me.itzvirtual.myunitox.data.student.local.StudentDao
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

private fun dbInitialize(db: SupportSQLiteDatabase) {
    db.execSQL("INSERT OR IGNORE INTO app_config (id) VALUES (0)")
}

@Module
class DatabaseModule {

    @Single
    fun provideDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "my_unitox_db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.i("Database", "Pre-populating database with initial data")
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
