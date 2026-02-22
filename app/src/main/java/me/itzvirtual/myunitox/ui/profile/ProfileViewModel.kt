package me.itzvirtual.myunitox.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.models.StudentData
import me.itzvirtual.myunitox.data.repository.DashboardDataRepository
import me.itzvirtual.myunitox.data.repository.StudentDataRepository
import javax.inject.Inject

data class StudentDataState(
    val data: StudentData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class DashboardDataState(
    val data: DashboardDataResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class UiState(
    val studentDataState: StudentDataState = StudentDataState(),
    val dashboardDataState: DashboardDataState = DashboardDataState()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studentDataRepo: StudentDataRepository,
    private val dashboardDataRepo: DashboardDataRepository

) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // set all data to loading state
            _uiState.update{
                it.copy(
                    studentDataState = StudentDataState(
                        isLoading = true,
                        errorMessage = null
                    ),
                    dashboardDataState = DashboardDataState(
                        isLoading = true,
                        errorMessage = null
                    )
                )
            }

            // student info state
            val studentData = studentDataRepo.getStudentData()
            _uiState.update{
                it.copy(
                    studentDataState = StudentDataState(
                        data = studentData,
                        isLoading = false,
                        errorMessage = null
                    )
                )
            }

            // dashboard data state
            // Section B: Dashboard/Dates
            val dashboardDataJob = launch {
                dashboardDataRepo.fetchDashboardData().onSuccess { data ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            dashboardDataState = currentState.dashboardDataState.copy(
                                data = data,
                                isLoading = false
                            )
                        )
                    }
                }.onFailure { error ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            dashboardDataState = currentState.dashboardDataState.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        )
                    }
                    Log.e("ProfileViewModel", "Error fetching dashboardData", error)
                }
            }

            joinAll(dashboardDataJob)


        }
    }
}