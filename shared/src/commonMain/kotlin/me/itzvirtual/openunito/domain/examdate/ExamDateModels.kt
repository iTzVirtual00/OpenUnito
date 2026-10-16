@file:OptIn(ExperimentalTime::class)

package me.itzvirtual.openunito.domain.examdate

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ExamDate(
    val adsceId: Long,
    val adId: Long,
    val activityCode: String,
    val title: String,
    val description: String,
    val note: String,

    val date: Instant,
    val bookingStartDate: Instant,
    val bookingEndDate: Instant,

    val bookableExam: Boolean,
    val bookingEnabled: Boolean,
    val unbookingEnabled: Boolean,

    val cdsAppId: Long,
    val adAppId: Long,

    val subscribedStudents: Int,

    val president: String,

    val questionnaire: Int
)

val examDateSample: HashMap<String, List<ExamDate>> = hashMapOf(
    "INF0328" to listOf(
        ExamDate(
            adsceId = 23948175L,
            adId = 95183L,
            activityCode = "INF0328",
            title = "MATEMATICA DISCRETA, ALGEBRA E GEOMETRIA",
            description = "M.D.A.G- PARTE DI MATEMATICA DISCRETA",
            note = "aule A+B+C+D+F",
            date = Instant.fromEpochMilliseconds(1800313200000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1798585200000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1799708400000L),
            bookableExam = true,
            bookingEnabled = false,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 95183L,
            subscribedStudents = 0,
            president = "PAOLA CONTI",
            questionnaire = 1
        ),
        ExamDate(
            adsceId = 23948176L,
            adId = 95183L,
            activityCode = "INF0328",
            title = "MATEMATICA DISCRETA, ALGEBRA E GEOMETRIA",
            description = "M.D.A.G- PARTE DI ALGEBRA LINEARE",
            note = "aula G",
            date = Instant.fromEpochMilliseconds(1802905200000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1801177200000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1802300400000L),
            bookableExam = true,
            bookingEnabled = true,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 95183L,
            subscribedStudents = 24,
            president = "PAOLA CONTI",
            questionnaire = 1
        ),
        ExamDate(
            adsceId = 23948177L,
            adId = 95183L,
            activityCode = "INF0328",
            title = "MATEMATICA DISCRETA, ALGEBRA E GEOMETRIA",
            description = "Appello di Recupero / Sessione Autunnale",
            note = "Aula Magna",
            date = Instant.fromEpochMilliseconds(1807442400000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1805714400000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1806837600000L),
            bookableExam = true,
            bookingEnabled = false,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 95183L,
            subscribedStudents = 5,
            president = "PAOLA CONTI",
            questionnaire = 0
        )
    ),
    "INF0084" to listOf(
        ExamDate(
            adsceId = 24183920L,
            adId = 91385L,
            activityCode = "INF0084",
            title = "PROGRAMMAZIONE I E LABORATORIO",
            description = "Appello Ordinario - Scritto e Orale",
            note = "Laboratorio Turing",
            date = Instant.fromEpochMilliseconds(1800918000000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1799190000000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1800313200000L),
            bookableExam = true,
            bookingEnabled = true,
            unbookingEnabled = true,
            cdsAppId = 14928L,
            adAppId = 91385L,
            subscribedStudents = 58,
            president = "MARIO ROSSI",
            questionnaire = 1
        ),
        ExamDate(
            adsceId = 24183921L,
            adId = 91385L,
            activityCode = "INF0084",
            title = "PROGRAMMAZIONE I E LABORATORIO",
            description = "Secondo Appello Invernale",
            note = "Laboratorio Lovelace - Postazioni A-Z",
            date = Instant.fromEpochMilliseconds(1803510000000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1801782000000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1802905200000L),
            bookableExam = true,
            bookingEnabled = true,
            unbookingEnabled = true,
            cdsAppId = 14928L,
            adAppId = 91385L,
            subscribedStudents = 42,
            president = "MARIO ROSSI",
            questionnaire = 1
        )
    ),
    "INF0102" to listOf(
        ExamDate(
            adsceId = 25839104L,
            adId = 93847L,
            activityCode = "INF0102",
            title = "ARCHITETTURA DEGLI ELABORATORI",
            description = "Prova Scritta e Progetto Assembly",
            note = "Aule 1 e 2 - Consegna progetto entro le 12:00",
            date = Instant.fromEpochMilliseconds(1801522800000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1799794800000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1800918000000L),
            bookableExam = true,
            bookingEnabled = false,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 93847L,
            subscribedStudents = 77,
            president = "PAOLO BIANCHI",
            questionnaire = 1
        ),
        ExamDate(
            adsceId = 25839105L,
            adId = 93847L,
            activityCode = "INF0102",
            title = "ARCHITETTURA DEGLI ELABORATORI",
            description = "Discussione Orale Progetto",
            note = "Ufficio Docente - 3° Piano",
            date = Instant.fromEpochMilliseconds(1801954800000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1800140400000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1801349900000L),
            bookableExam = false,
            bookingEnabled = false,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 93847L,
            subscribedStudents = 19,
            president = "PAOLO BIANCHI",
            questionnaire = 1
        )
    ),
    "INF0555" to listOf(
        ExamDate(
            adsceId = 26194832L,
            adId = 97421L,
            activityCode = "INF0555",
            title = "BASI DI DATI E SISTEMI INFORMATIVI",
            description = "Appello Unico Integrato",
            note = "Aula Informatica Beta",
            date = Instant.fromEpochMilliseconds(1804114800000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1802386800000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1803510000000L),
            bookableExam = true,
            bookingEnabled = true,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 97421L,
            subscribedStudents = 112,
            president = "GIUSEPPE VERDI",
            questionnaire = 1
        )
    ),
    "MAT0110" to listOf(
        ExamDate(
            adsceId = 27391845L,
            adId = 98519L,
            activityCode = "MAT0110",
            title = "ANALISI MATEMATICA I",
            description = "Prova Scritta Preselettiva",
            note = "Aula Magna e Aula B",
            date = Instant.fromEpochMilliseconds(1799708400000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1797980400000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1799103600000L),
            bookableExam = false,
            bookingEnabled = false,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 98519L,
            subscribedStudents = 145,
            president = "ELENA FERRARI",
            questionnaire = 1
        ),
        ExamDate(
            adsceId = 27391846L,
            adId = 98519L,
            activityCode = "MAT0110",
            title = "ANALISI MATEMATICA I",
            description = "Prova Orale",
            note = "Solo per ammessi allo scritto - Aula Seminari",
            date = Instant.fromEpochMilliseconds(1800140400000L),
            bookingStartDate = Instant.fromEpochMilliseconds(1798412400000L),
            bookingEndDate = Instant.fromEpochMilliseconds(1799535600000L),
            bookableExam = false,
            bookingEnabled = false,
            unbookingEnabled = false,
            cdsAppId = 14928L,
            adAppId = 98519L,
            subscribedStudents = 38,
            president = "ELENA FERRARI",
            questionnaire = 1
        )
    )
)