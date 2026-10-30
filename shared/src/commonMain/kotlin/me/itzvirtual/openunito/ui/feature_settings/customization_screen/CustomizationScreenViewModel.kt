package me.itzvirtual.openunito.ui.feature_settings.customization_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.config.AppConfigRepository
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CustomizationScreenViewModel(
    private val appConfigRepo: AppConfigRepository,
): ViewModel() {
    val screenState: StateFlow<CustomizationScreenState> = combine(
        appConfigRepo.showAttendance
    ) { showAttendance ->
        CustomizationScreenState(
            showAttendance = showAttendance[0],
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CustomizationScreenState()
    )

    fun setShowAttendance(value: Boolean) {
        viewModelScope.launch {
            appConfigRepo.setShowAttendance(value)
        }
    }
}