package com.shalenammapride.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.Feedback
import com.shalenammapride.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FeedbackUiState(
    val feedbackList: List<Feedback> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)

class FeedbackViewModel : ViewModel() {
    private val repo = FeedbackRepository()
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getFeedback()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { items -> _uiState.update { it.copy(feedbackList = items, isLoading = false) } }
        }
    }

    fun submitFeedback(name: String, message: String, isAnonymous: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val today = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val feedback = Feedback(
                userName = if (isAnonymous) "" else name,
                message = message,
                isAnonymous = isAnonymous,
                submittedDate = today
            )
            repo.addFeedback(feedback)
                .onSuccess { _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isSubmitting = false, error = e.message) } }
        }
    }

    fun deleteFeedback(id: String) {
        viewModelScope.launch { repo.deleteFeedback(id) }
    }

    fun clearSuccess() { _uiState.update { it.copy(submitSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
