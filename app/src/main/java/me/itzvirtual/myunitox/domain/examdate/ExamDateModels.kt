package me.itzvirtual.myunitox.domain.examdate

import java.util.Date

data class ExamDate(
    val title: String,
    val description: String,
    val note: String,
    val adCode: String,
    val adDescription: String,
    val appDescription: String,
    val date: Date,
    val bookingStartDate: Date,
    val bookingEndDate: Date,
    val bookableExam: Boolean,
    val bookingEnabled: Boolean,
    val unbookingEnabled: Boolean,
    val students: Int,
    val president: String,
)
