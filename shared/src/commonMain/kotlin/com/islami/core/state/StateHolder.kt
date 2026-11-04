package com.islami.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base class for screen state holders: exposes immutable [state] for the UI to collect and lets
 * subclasses update it atomically.
 */
abstract class StateHolder<State>(initialState: State) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    protected fun currentState(): State = _state.value

    protected fun updateState(block: (State) -> State) {
        _state.update(block)
    }

    protected fun setState(newState: State) {
        _state.value = newState
    }
}
