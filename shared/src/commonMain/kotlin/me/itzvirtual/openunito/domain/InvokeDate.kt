package me.itzvirtual.openunito.domain

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class InvokeDate(
    val title: String,
    val description: String,
    val note: String,
    val adCode: String, //ad_cod
    val adDescription: String,
    val appDescription: String,
    val date: Instant,
    val bookingStartDate: Instant,
    val bookingEndDate: Instant,
    val bookableExam: Boolean,
    val bookingEnabled: Boolean,
    val unbookingEnabled: Boolean,
    val students: Int,
    val president: String,
)