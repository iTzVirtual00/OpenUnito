package me.itzvirtual.openunito

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import me.itzvirtual.openunito.core.network.NetworkMonitor
import me.itzvirtual.openunito.data.Prefetcher
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val prefetcher: Prefetcher,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    val isOnline = networkMonitor.isOnline
    fun prefetchData(scope: CoroutineScope) = prefetcher.prefetchData(scope)
}