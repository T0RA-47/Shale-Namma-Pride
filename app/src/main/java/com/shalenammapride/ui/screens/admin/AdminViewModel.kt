package com.shalenammapride.ui.screens.admin

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.Announcement
import com.shalenammapride.data.repository.AnnouncementRepository
import com.shalenammapride.util.ImageUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AdminUiState(
    val announcements: List<Announcement> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val notificationSent: Boolean = false,
    val error: String? = null
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
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

    fun addAnnouncement(title: String, description: String, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            runCatching {
                val imageUrl = if (imageUri != null) {
                    val bytes = ImageUtils.compress(getApplication(), imageUri)
                    announcementRepo.uploadImage(bytes)
                } else ""
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val ann = Announcement(title = title, description = description, imageUrl = imageUrl, postedDate = today)
                announcementRepo.addAnnouncement(ann).getOrThrow()
                announcementRepo.sendNotification(title, description)
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true, notificationSent = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, error = e.message) }
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch { announcementRepo.deleteAnnouncement(id) }
    }

    fun clearSuccess() { _uiState.update { it.copy(submitSuccess = false) } }
    fun clearNotification() { _uiState.update { it.copy(notificationSent = false) } }
}
