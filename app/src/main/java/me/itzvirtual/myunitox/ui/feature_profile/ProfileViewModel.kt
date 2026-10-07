package me.itzvirtual.myunitox.ui.feature_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.student.StudentRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(ProfileScreenState(isLoading = true))
    val screenState = _screenState.asStateFlow()

    init {
        observeStudentData()
    }

    /**
     * Subscribes to the cache flow. Shows local Room data immediately
     * and performs a background revalidation if data is missing or stale.
     */
    private fun observeStudentData() {
        viewModelScope.launch {
            studentRepo.studentCacheFlow.collectLatest { cached ->
                _screenState.update { state ->
                    state.copy(
                        data = cached.data,
                        // Only display the loading indicator if we don't have cached data yet
                        isLoading = cached.data == null
                    )
                }

                if (cached.data == null || cached.isStale) {
                    refresh()
                }
            }
        }
    }

    /**
     * Replaces the one-shot `load()`.
     * Triggered automatically on stale cache, or manually via pull-to-refresh.
     */
    fun refresh() {
        viewModelScope.launch {
            val result = studentRepo.refresh()
            result.onFailure {
                _screenState.update { state ->
                    state.copy(isLoading = false)
                }
            }
        }
    }
}