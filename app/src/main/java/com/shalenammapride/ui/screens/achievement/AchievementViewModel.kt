package com.shalenammapride.ui.screens.achievement

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.StudentAchievement
import com.shalenammapride.data.repository.AchievementRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AchievementUiState(
    val achievements: List<StudentAchievement> = emptyList(),
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

class AchievementViewModel : ViewModel() {
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

    fun addAchievement(imageUri: Uri?, studentName: String, title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            runCatching {
                val imageUrl = if (imageUri != null) repo.uploadImage(imageUri) else ""
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val achievement = StudentAchievement(
                    studentName = studentName,
                    achievementTitle = title,
                    description = description,
                    photoUrl = imageUrl,
                    achievementDate = today
                )
                repo.addAchievement(achievement).getOrThrow()
            }.onSuccess {
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun deleteAchievement(id: String) {
        viewModelScope.launch { repo.deleteAchievement(id) }
    }

    fun clearSuccess() { _uiState.update { it.copy(uploadSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
