@file:OptIn(ExperimentalTime::class)

package me.itzvirtual.openunito.domain.student

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class StudentData(
    val userId: Int,
    val username: String,
    val firstname: String,
    val lastname: String,
    val careers: List<StudentCareer>,
    val type: Int,
)

data class StudentCareer(
    val id: Int,
    val title: String,
    val description: String,
    val dateStart: Instant,
    val dateEnd: String?,
    val studentId: String,
    val registrationId: Int,
    val notes: String,
    val regulationYear: String,
    val normativa: String,
    val className: String,
    val cohort: String,
    val percorsoDes: String,
    val sedeDes: String,
    val iscrDes: String,
    val staStuDes: String,
    val courseYear: Int,
) {
    fun isActive(): Boolean = dateEnd == null
}


private val now = Clock.System.now()
val studentDataSample = StudentData(
    userId = 12345,
    username = "mario.rossi",
    firstname = "Mario",
    lastname = "Rossi",
    careers = listOf(
        StudentCareer(
            id = 1,
            title = "Attivo in Informatica",
            description = "Corso di Laurea - Informatica",
            dateStart = now,
            dateEnd = null,
            studentId = "1639205",
            registrationId = 1926481,
            notes = "Attivo per Trasf. in ingresso",
            regulationYear = "2025",
            normativa = "D.M. 270/2004",
            className = "L-31 R",
            cohort = "2025",
            percorsoDes = "PERCORSO GENERICO",
            sedeDes = "Torino",
            iscrDes = "Anno corso: 1°",
            staStuDes = "Attivo",
            courseYear = 1,
        ),
        StudentCareer(
            id = 1,
            title = "Attivo in Informatica",
            description = "Corso di Laurea - Informatica",
            dateStart = now,
            dateEnd = null,
            studentId = "1639205",
            registrationId = 1926481,
            notes = "Attivo per Trasf. in ingresso",
            regulationYear = "2025",
            normativa = "D.M. 270/2004",
            className = "L-31 R",
            cohort = "2025",
            percorsoDes = "PERCORSO GENERICO",
            sedeDes = "Torino",
            iscrDes = "Anno corso: 1°",
            staStuDes = "Attivo",
            courseYear = 1,
        ),
        StudentCareer(
            id = 1,
            title = "Attivo in Informatica",
            description = "Corso di Laurea - Informatica",
            dateStart = now,
            dateEnd = null,
            studentId = "1639205",
            registrationId = 1926481,
            notes = "Attivo per Trasf. in ingresso",
            regulationYear = "2025",
            normativa = "D.M. 270/2004",
            className = "L-31 R",
            cohort = "2025",
            percorsoDes = "PERCORSO GENERICO",
            sedeDes = "Torino",
            iscrDes = "Anno corso: 1°",
            staStuDes = "Attivo",
            courseYear = 1,
        )
    ),
    type = 1,
)
