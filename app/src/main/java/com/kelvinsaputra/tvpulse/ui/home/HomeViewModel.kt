package com.kelvinsaputra.tvpulse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.usecase.GetTopShowsUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTopShowsUseCase: GetTopShowsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadShows()
    }

    fun retry() {
        loadShows()
    }

    private fun loadShows() {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            try {
                val shows = getTopShowsUseCase()
                _uiState.value = if (shows.isEmpty()) {
                    HomeUiState.Empty
                } else {
                    HomeUiState.Success(shows)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = HomeUiState.Error(exception.toUserMessage())
            }
        }
    }
}
