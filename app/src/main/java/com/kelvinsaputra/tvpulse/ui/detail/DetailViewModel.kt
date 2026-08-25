package com.kelvinsaputra.tvpulse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.usecase.GetShowDetailUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUserMessage
import com.kelvinsaputra.tvpulse.ui.navigation.DetailDestination
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val destination: DetailDestination,
    private val getShowDetailUseCase: GetShowDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init { loadDetail() }

    fun retry() { loadDetail() }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                _uiState.value = DetailUiState.Success(getShowDetailUseCase(destination.showId))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = DetailUiState.Error(exception.toUserMessage())
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(destination: DetailDestination): DetailViewModel
    }
}
