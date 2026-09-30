package me.itzvirtual.myunitox.ui.feature_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.student.StudentRepository
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(ProfileScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _screenState.update { it.copy(isLoading = true) }

            val studentDeferred = async {
                try {
                    studentRepo.getStudentData()
                } catch (_: Exception) {
                    null
                }
            }

            _screenState.update {
                it.copy(
                    data = studentDeferred.await(),
                    isLoading = false
                )
            }
        }
    }
}
