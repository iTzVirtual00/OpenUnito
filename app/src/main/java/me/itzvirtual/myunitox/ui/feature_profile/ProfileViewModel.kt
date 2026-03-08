package me.itzvirtual.myunitox.ui.feature_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.dashboard.DashboardRepository
import me.itzvirtual.myunitox.data.student.StudentRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val dashboardRepo: DashboardRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(ProfileScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _screenState.update { it.copy(isGlobalLoading = true) }

            val studentDeferred = async {
                try {
                    studentRepo.getStudentData()
                } catch (_: Exception) {
                    null
                }
            }
            val dashboardDeferred = async {
                val cached = dashboardRepo.getCached()
                val isStale = dashboardRepo.isCacheStale()
                if (cached != null) {
                    _screenState.update { s ->
                        s.copy(
                            dashboardState = ProfileDashboardUiState(
                                data = cached,
                                isStale = isStale
                            )
                        )
                    }
                }
                dashboardRepo.refresh()
            }

            _screenState.update {
                it.copy(
                    profileState = ProfileUiState(
                        data = studentDeferred.await(),
                        isLoading = false
                    )
                )
            }

            dashboardDeferred.await().fold(
                onSuccess = {
                    val fresh = dashboardRepo.getCached()
                    _screenState.update { s -> s.copy(dashboardState = ProfileDashboardUiState(data = fresh)) }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            dashboardState = s.dashboardState.copy(
                                error = e.message,
                                isLoading = false
                            )
                        )
                    }
                }
            )

            _screenState.update { it.copy(isGlobalLoading = false) }
        }
    }
}
