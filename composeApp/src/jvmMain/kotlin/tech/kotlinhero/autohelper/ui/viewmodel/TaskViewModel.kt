package tech.kotlinhero.autohelper.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val _hasTaskExecuting = mutableStateOf(false)

    val hasTaskExecuting: State<Boolean> = _hasTaskExecuting

    fun executeTask() {
        viewModelScope.launch {
            _hasTaskExecuting.value = true

            delay(5000)

            _hasTaskExecuting.value = false
        }
    }
}