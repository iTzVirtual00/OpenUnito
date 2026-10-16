@file:OptIn(ExperimentalTime::class)

package me.itzvirtual.openunito.domain.grade

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class ExamGrade(
    val id: String,
    val courseName: String,
    val score: Score?,
    val teaching: String,
    val status: String,
    val passYear: Int,
    val yearFreq: String,
    val ordAdCod: String,
    val ordAdDes: String,
    val yearIdx: Int,
    val weight: String,
)

data class Score(
    val date: Instant,
    val value: String,
    val laude: Boolean,
)
