package com.islami.core.state

import com.islami.core.error.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base class for UI state holders using MVI/MVVM pattern
 * Manages state as a StateFlow
 *
 * Example:
 * ```
 * class UserListStateHolder(
 *     private val getUsersUseCase: GetUsersUseCase
 * ) : StateHolder<UserListState>(
 *     initialState = UserListState()
 * ) {
 *     init {
 *         loadUsers()
 *     }
 *
 *     fun loadUsers() = viewModelScope.launch {
 *         updateState { it.copy(isLoading = true) }
 *         getUsersUseCase().collect { result ->
 *             updateState {
 *                 when (result) {
 *                     is Result.Success -> it.copy(users = result.data, isLoading = false)
 *                     is Result.Error -> it.copy(error = result.exception, isLoading = false)
 *                     is Result.Loading -> it
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 * // In UI
 * val state by viewModel.state.collectAsState()
 * ```
 */
abstract class StateHolder<State>(initialState: State) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * Get current state value
     */
    protected fun currentState(): State = _state.value

    /**
     * Update state with a new value
     */
    protected fun updateState(block: (State) -> State) {
        _state.update { currentState ->
            block(currentState)
        }
    }

    /**
     * Set state to a completely new value
     */
    protected fun setState(newState: State) {
        _state.value = newState
    }
}

/**
 * Sealed class for UI events/side effects
 */
sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    data class ShowError(val error: String) : UiEvent()
    data class Navigate(val destination: String, val args: Map<String, Any> = emptyMap()) : UiEvent()
    data object NavigateBack : UiEvent()
    data class ShowLoading(val isLoading: Boolean) : UiEvent()
}

/**
 * Generic UI state wrapper for handling loading, success, error states
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Exception? = null) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()

    fun getDataOrNull(): T? = (this as? Success)?.data

    fun getErrorOrNull(): String? = (this as? Error)?.message

    fun isLoading(): Boolean = this is Loading

    fun mapSuccess(transform: (T) -> UiState<T>): UiState<T> = when (this) {
        is Success -> transform(data)
        else -> this
    }
}

/**
 * Helper extension to convert Result<T> to UiState<T>
 */
fun <T> Result<T>.toUiState(): UiState<T> = when (this) {
    is Result.Loading -> UiState.Loading
    is Result.Success -> UiState.Success(data)
    is Result.Error -> UiState.Error(exception.message ?: "Unknown error", exception)
}

/**
 * Base class combining StateHolder with event handling for side effects
 */
abstract class EventStateHolder<State, Event>(initialState: State) : StateHolder<State>(initialState) {
    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    /**
     * Emit an event to be consumed by UI
     */
    protected fun emitEvent(event: Event) {
        _event.value = event
    }

    /**
     * Consumed event (call after handling)
     */
    protected fun eventConsumed() {
        _event.value = null
    }
}
