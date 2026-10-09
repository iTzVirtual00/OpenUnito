package me.itzvirtual.myunitox.ui.feature_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.student.StudentRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
) : ViewModel() {

    private val isManualRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<ProfileScreenState> = combine(
        studentRepo.studentCacheFlow,
        isManualRefreshing
    ) { cached, manualRefreshing ->
        val isLoading = cached == null || manualRefreshing

        ProfileScreenState(
            data = cached?.data,
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
}