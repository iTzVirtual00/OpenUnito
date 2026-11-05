package me.itzvirtual.openunito.core.database

import androidx.room3.AutoMigration
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL
import me.itzvirtual.openunito.data.agenda.local.AgendaDao
import me.itzvirtual.openunito.data.agenda.local.AgendaEntity
import me.itzvirtual.openunito.data.calendar.local.CalendarConfig
import me.itzvirtual.openunito.data.calendar.local.CalendarConfigDao
import me.itzvirtual.openunito.data.config.local.AppConfig
import me.itzvirtual.openunito.data.config.local.AppConfigDao
import me.itzvirtual.openunito.data.dashboard.local.DashboardDao
import me.itzvirtual.openunito.data.dashboard.local.DashboardEntity
import me.itzvirtual.openunito.data.examdate.local.ExamDateDao
import me.itzvirtual.openunito.data.examdate.local.ExamDateEntity
import me.itzvirtual.openunito.data.examresult.local.ExamResultDao
import me.itzvirtual.openunito.data.examresult.local.ExamResultEntity
import me.itzvirtual.openunito.data.grade.local.ExamGradeDao
import me.itzvirtual.openunito.data.grade.local.ExamGradeEntity
import me.itzvirtual.openunito.data.student.local.StudentDao
import me.itzvirtual.openunito.data.student.local.StudentEntity

// RoomKSP populates this
// https://developer.android.com/kotlin/multiplatform/room#defining-database
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}


@Database(
    entities = [
        AppConfig::class,
        AgendaEntity::class,
        CalendarConfig::class,
        DashboardEntity::class,
        StudentEntity::class,
        ExamResultEntity::class,
        ExamDateEntity::class,
        ExamGradeEntity::class,
    ],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(3, 4),
        AutoMigration(4, 5),
        AutoMigration(5, 6),
        AutoMigration(6, 7),
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configDao(): AppConfigDao
    abstract fun agendaDao(): AgendaDao
    abstract fun calendarConfigDao(): CalendarConfigDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun studentDao(): StudentDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun examDateDao(): ExamDateDao
    abstract fun examGradeDao(): ExamGradeDao
}

/** Preserves app_config (and thus the auth token) while adding new cache tables. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.inTransaction()
        connection.executeSQL(
            """CREATE TABLE IF NOT EXISTS `agenda_cache` (
                `dateRangeKey` TEXT NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        connection.executeSQL(
            """CREATE TABLE IF NOT EXISTS `dashboard_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        connection.executeSQL(
            """CREATE TABLE IF NOT EXISTS `student_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        connection.executeSQL(
            """CREATE TABLE IF NOT EXISTS `exam_result_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        connection.executeSQL(
            """CREATE TABLE IF NOT EXISTS `exam_date_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
        connection.executeSQL(
            """CREATE TABLE IF NOT EXISTS `exam_grade_cache` (
                `id` INTEGER NOT NULL PRIMARY KEY,
                `data` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL)"""
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.executeSQL(
            """CREATE TABLE IF NOT EXISTS `calendar_config` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL,
            `calendarLinkId` TEXT NOT NULL)"""
        )
        connection.executeSQL("ALTER TABLE `app_config` ADD COLUMN `selectedCalendarId` INTEGER")
        connection.executeSQL("ALTER TABLE `app_config` DROP COLUMN `calendarClientId` INTEGER")
        connection.executeSQL("ALTER TABLE `app_config` DROP COLUMN `calendarLinkId` INTEGER")
    }
}

