package me.itzvirtual.myunitox.ui.feature_profile

import me.itzvirtual.myunitox.domain.student.StudentData

data class ProfileScreenState(
    val data: StudentData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)
