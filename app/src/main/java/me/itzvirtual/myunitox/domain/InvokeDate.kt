package me.itzvirtual.myunitox.domain

import java.util.Date

data class InvokeDate(
    val title: String,
    val description: String,
    val note: String,
    val adCode: String, //ad_cod
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