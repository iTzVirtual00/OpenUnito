package me.itzvirtual.myunitox.domain.examresult

import java.util.Date
import kotlin.time.ExperimentalTime

data class ExamResult(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val date: Date,
    val weight: Int,
    val editableUntil: String,
    val maxGrade: String,
    val status: GradeStatus,
    val gradeResult: GradeResult?,
)

data class GradeResult(
    val passed: Boolean,
    val grade: String,
    val isAbsent: Boolean,
    val isWithdrawn: Boolean,
    val canAccept: Boolean,
    val canReject: Boolean,
    val laude: Boolean,
)

enum class GradeStatus {
    VIEWED, ACCEPTED, REJECTED, NOT_TOUCHED, UNKNOWN;

    override fun toString(): String = when (this) {
        VIEWED -> "V"
        ACCEPTED -> "A"
        REJECTED -> "R"
        NOT_TOUCHED -> "N"
        UNKNOWN -> "UNKNOWN"
    }
}

@OptIn(ExperimentalTime::class)
val examResultsDataSample = listOf(
    ExamResult(
        id = "1",
        courseName = "Analisi Matematica",
        courseCode = "ANMAT",
        date = Date(System.currentTimeMillis() - (60 * 60 * 1000L)),
        weight = 9,
        editableUntil = "29/09/2027 23:59:59",
        maxGrade = "30",
        status = GradeStatus.NOT_TOUCHED,
        gradeResult = GradeResult(
            passed = true,
            grade = "29",
            isAbsent = false,
            isWithdrawn = false,
            canAccept = true,
            canReject = true,
            laude = false
        )
    ),
    ExamResult(
        id = "2",
        courseName = "Analisi Matematica II",
        courseCode = "ANMAT2",
        date = Date(System.currentTimeMillis() - (60 * 60 * 1000L)),
        weight = 9,
        editableUntil = "29/09/2027 23:59:59",
        maxGrade = "30",
        status = GradeStatus.NOT_TOUCHED,
        gradeResult = GradeResult(
            passed = false,
            grade = "X",
            isAbsent = false,
            isWithdrawn = false,
            canAccept = false,
            canReject = false,
            laude = false
        )
    ),
    ExamResult(
        id = "3",
        courseName = "Ricerca Operativa",
        courseCode = "ROP",
        date = Date(System.currentTimeMillis() - (60 * 60 * 1000L)),
        weight = 9,
        editableUntil = "29/09/2026 23:59:59",
        maxGrade = "30",
        status = GradeStatus.ACCEPTED,
        gradeResult = GradeResult(
            passed = true,
            grade = "29",
            isAbsent = false,
            isWithdrawn = false,
            canAccept = false,
            canReject = false,
            laude = false
        )
    ),
    ExamResult(
        id = "4",
        courseName = "Analisi Matematica",
        courseCode = "ANMAT",
        date = Date(System.currentTimeMillis() - (60 * 60 * 1000L)),
        weight = 9,
        editableUntil = "29/09/2027 23:59:59",
        maxGrade = "30",
        status = GradeStatus.NOT_TOUCHED,
        gradeResult = GradeResult(
            passed = true,
            grade = "29",
            isAbsent = false,
            isWithdrawn = false,
            canAccept = true,
            canReject = true,
            laude = false
        )
    ),
    ExamResult(
        id = "5",
        courseName = "Analisi Matematica",
        courseCode = "ANMAT",
        date = Date(System.currentTimeMillis() - (60 * 60 * 1000L)),
        weight = 9,
        editableUntil = "29/09/2027 23:59:59",
        maxGrade = "30",
        status = GradeStatus.NOT_TOUCHED,
        gradeResult = GradeResult(
            passed = true,
            grade = "29",
            isAbsent = false,
            isWithdrawn = false,
            canAccept = true,
            canReject = true,
            laude = false
        )
    ),
    ExamResult(
        id = "6",
        courseName = "Analisi Matematica",
        courseCode = "ANMAT",
        date = Date(System.currentTimeMillis() - (60 * 60 * 1000L)),
        weight = 9,
        editableUntil = "29/09/2027 23:59:59",
        maxGrade = "30",
        status = GradeStatus.NOT_TOUCHED,
        gradeResult = GradeResult(
            passed = true,
            grade = "29",
            isAbsent = false,
            isWithdrawn = false,
            canAccept = true,
            canReject = true,
            laude = false
        )
    ),
)