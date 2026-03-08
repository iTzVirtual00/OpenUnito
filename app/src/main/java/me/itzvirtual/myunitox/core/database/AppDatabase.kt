package me.itzvirtual.myunitox.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.itzvirtual.myunitox.data.agenda.local.AgendaDao
import me.itzvirtual.myunitox.data.agenda.local.AgendaEntity
import me.itzvirtual.myunitox.data.config.local.AppConfig
import me.itzvirtual.myunitox.data.config.local.AppConfigDao
import me.itzvirtual.myunitox.data.dashboard.local.DashboardDao
import me.itzvirtual.myunitox.data.dashboard.local.DashboardEntity
import me.itzvirtual.myunitox.data.examdate.local.ExamDateDao
import me.itzvirtual.myunitox.data.examdate.local.ExamDateEntity
import me.itzvirtual.myunitox.data.examresult.local.ExamResultDao
import me.itzvirtual.myunitox.data.examresult.local.ExamResultEntity
import me.itzvirtual.myunitox.data.grade.local.ExamGradeDao
import me.itzvirtual.myunitox.data.grade.local.ExamGradeEntity
import me.itzvirtual.myunitox.data.student.local.StudentDao
import me.itzvirtual.myunitox.data.student.local.StudentEntity

@Database(
    entities = [
        AppConfig::class,
        AgendaEntity::class,
        DashboardEntity::class,
        StudentEntity::class,
        ExamResultEntity::class,
        ExamDateEntity::class,
        ExamGradeEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): AppConfigDao
    abstract fun agendaDao(): AgendaDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun studentDao(): StudentDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun examDateDao(): ExamDateDao
    abstract fun examGradeDao(): ExamGradeDao
}

/** Preserves app_config (and thus the auth token) while adding new cache tables. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `agenda_cache` (
                `dateRangeKey` TEXT NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `dashboard_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `student_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `exam_result_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `exam_date_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `exam_grade_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
    }
}
