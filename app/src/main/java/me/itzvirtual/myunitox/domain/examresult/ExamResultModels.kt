package me.itzvirtual.myunitox.domain.examresult

data class ExamResult(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val date: String,
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
