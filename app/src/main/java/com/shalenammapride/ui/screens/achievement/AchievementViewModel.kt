package com.shalenammapride.ui.screens.achievement

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.StudentAchievement
import com.shalenammapride.data.repository.AchievementRepository
import com.shalenammapride.util.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AchievementUiState(
    val achievements: List<StudentAchievement> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

class AchievementViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AchievementRepository()
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAchievements()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { items -> _uiState.update { it.copy(achievements = items, isLoading = false) } }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(800)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun addAchievement(imageUri: Uri?, studentName: String, title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            runCatching {
                val imageUrl = if (imageUri != null) {
                    val bytes = ImageUtils.compress(getApplication(), imageUri)
                    repo.uploadImage(bytes)
                } else ""
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val achievement = StudentAchievement(
                    studentName = studentName, achievementTitle = title,
                    description = description, photoUrl = imageUrl, achievementDate = today
                )
                repo.addAchievement(achievement).getOrThrow()
            }.onSuccess {
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun deleteAchievement(id: String) { viewModelScope.launch { repo.deleteAchievement(id) } }
    fun clearSuccess() { _uiState.update { it.copy(uploadSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
