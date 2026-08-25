package com.kelvinsaputra.tvpulse.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.usecase.SearchShowsUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchShowsUseCase: SearchShowsUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val retryToken = MutableStateFlow(0L)

    private val normalizedQuery = _query
        .map { it.trim() }
        .distinctUntilChanged()

    val uiState: StateFlow<SearchUiState> = combine(
        normalizedQuery,
        retryToken,
    ) { query, token -> query to token }
        .debounce { (query, _) -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
        .flatMapLatest { (query, _) ->
            if (query.isBlank()) {
                flow { emit(SearchUiState.Empty()) }
            } else {
                flow<SearchUiState> {
                    emit(SearchUiState.Loading(query))
                    val shows = searchShowsUseCase(query)
                    emit(
                        if (shows.isEmpty()) {
                            SearchUiState.Empty(
                                query = query,
                                message = "No shows found for “$query”.",
                            )
                        } else {
                            SearchUiState.Success(query, shows)
                        }
                    )
                }.catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(SearchUiState.Error(query, throwable.toUserMessage()))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SearchUiState.Empty(),
        )

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun retry() {
        retryToken.value += 1
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
