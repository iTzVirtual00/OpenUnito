package me.itzvirtual.openunito.ui.feature_profile

import me.itzvirtual.openunito.domain.student.StudentData

data class ProfileScreenState(
    val data: StudentData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)
