package me.itzvirtual.openunito.ui.feature_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.student.StudentRepository
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val studentRepo: StudentRepository,
    private val appConfigRepo: AppConfigRepository,
) : ViewModel() {

    private val isManualRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<ProfileScreenState> = combine(
        studentRepo.studentCacheFlow,
        appConfigRepo.selectedCareerIdState,
        isManualRefreshing
    ) { cached, selectedCareerId, manualRefreshing ->
        val isLoading = cached == null || manualRefreshing

        ProfileScreenState(
            data = cached?.data,
            selectedCareerId = selectedCareerId,
            isLoading = isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileScreenState(isLoading = true)
    )

    fun refresh() {
        viewModelScope.launch {
            isManualRefreshing.value = true
            try {
                studentRepo.refresh()
            } finally {
                isManualRefreshing.value = false
            }
        }
    }

    fun setCareer(careerId: Int) {
        viewModelScope.launch {
            appConfigRepo.setCareerId(careerId)
        }
    }
}
