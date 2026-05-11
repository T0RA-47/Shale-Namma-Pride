package com.shalenammapride.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.Announcement
import com.shalenammapride.data.repository.AnnouncementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AdminUiState(
    val announcements: List<Announcement> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)

class AdminViewModel : ViewModel() {
    private val announcementRepo = AnnouncementRepository()
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            announcementRepo.getAnnouncements()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { items -> _uiState.update { it.copy(announcements = items, isLoading = false) } }
        }
    }

    fun addAnnouncement(title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val ann = Announcement(title = title, description = description, postedDate = today)
            announcementRepo.addAnnouncement(ann)
                .onSuccess { _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isSubmitting = false, error = e.message) } }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch { announcementRepo.deleteAnnouncement(id) }
    }

    fun clearSuccess() { _uiState.update { it.copy(submitSuccess = false) } }
}
