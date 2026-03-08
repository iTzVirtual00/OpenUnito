package me.itzvirtual.myunitox.ui.feature_examdates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.examdate.ExamDateRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import javax.inject.Inject

@HiltViewModel
class ExamDatesViewModel @Inject constructor(
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(ExamDatesScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _screenState.update { it.copy(isGlobalLoading = true) }

            val examDatesDeferred = async {
                val cached = examDateRepo.getCached()
                val isStale = examDateRepo.isCacheStale()
                if (cached != null) {
                    _screenState.update { s ->
                        s.copy(examDatesState = ExamDatesUiState(dates = cached, isStale = isStale))
                    }
                }
                examDateRepo.refresh()
            }

            val examResultDeferred = async {
                val cached = examResultRepo.getCached()
                val isStale = examResultRepo.isCacheStale()
                if (cached != null) {
                    _screenState.update { s ->
                        s.copy(
                            examResultsState = ExamResultsForDatesUiState(
                                results = cached,
                                isStale = isStale
                            )
                        )
                    }
                }
                examResultRepo.refresh()
            }

            examDatesDeferred.await().fold(
                onSuccess = {
                    val fresh = examDateRepo.getCached()
                    _screenState.update { s ->
                        s.copy(examDatesState = ExamDatesUiState(dates = fresh ?: emptyList()))
                    }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            examDatesState = s.examDatesState.copy(
                                error = e.message,
                                isLoading = false
                            )
                        )
                    }
                }
            )

            examResultDeferred.await().fold(
                onSuccess = {
                    val fresh = examResultRepo.getCached()
                    _screenState.update { s ->
                        s.copy(
                            examResultsState = ExamResultsForDatesUiState(
                                results = fresh ?: emptyList()
                            )
                        )
                    }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            examResultsState = s.examResultsState.copy(
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
