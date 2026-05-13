package com.shalenammapride.ui.screens.achievement

import android.app.Application
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.AchievementComment
import com.shalenammapride.data.model.StudentAchievement
import com.shalenammapride.data.repository.AchievementRepository
import com.shalenammapride.util.ImageUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AchievementLikeSummary(val count: Int = 0, val myVoted: Boolean = false)

data class CommentsState(
    val achievementId: String? = null,
    val comments: List<AchievementComment> = emptyList()
)

data class AchievementUiState(
    val achievements: List<StudentAchievement> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val reactionData: Map<String, AchievementLikeSummary> = emptyMap(),
    val commentsState: CommentsState = CommentsState(),
    val error: String? = null
)

class AchievementViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AchievementRepository()
    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    private val deviceId: String = Settings.Secure.getString(
        application.contentResolver, Settings.Secure.ANDROID_ID
    )
    private var commentsJob: Job? = null

    init {
        viewModelScope.launch {
            repo.getAchievements()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { items -> _uiState.update { it.copy(achievements = items, isLoading = false) } }
        }
        viewModelScope.launch {
            repo.getReactions().catch { }.collect { allReactions ->
                val summaries = allReactions.mapValues { (_, votes) ->
                    AchievementLikeSummary(
                        count = votes.values.count { it == "like" },
                        myVoted = votes[deviceId] == "like"
                    )
                }
                _uiState.update { it.copy(reactionData = summaries) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(800)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun react(achievementId: String) {
        viewModelScope.launch {
            val alreadyLiked = _uiState.value.reactionData[achievementId]?.myVoted == true
            if (alreadyLiked) repo.removeReaction(achievementId, deviceId)
            else repo.setReaction(achievementId, deviceId)
        }
    }

    fun openComments(achievementId: String) {
        commentsJob?.cancel()
        _uiState.update { it.copy(commentsState = CommentsState(achievementId = achievementId)) }
        commentsJob = viewModelScope.launch {
            repo.getComments(achievementId).catch { }.collect { comments ->
                _uiState.update { it.copy(commentsState = it.commentsState.copy(comments = comments)) }
            }
        }
    }

    fun closeComments() {
        commentsJob?.cancel()
        commentsJob = null
        _uiState.update { it.copy(commentsState = CommentsState()) }
    }

    fun addComment(userName: String, message: String) {
        val achievementId = _uiState.value.commentsState.achievementId ?: return
        viewModelScope.launch {
            val comment = AchievementComment(
                userName = userName.trim().ifEmpty { "Anonymous" },
                message = message.trim(),
                timestamp = System.currentTimeMillis()
            )
            repo.addComment(achievementId, comment)
        }
    }

    fun deleteComment(commentId: String) {
        val achievementId = _uiState.value.commentsState.achievementId ?: return
        viewModelScope.launch { repo.deleteComment(achievementId, commentId) }
    }

    fun addAchievement(profileUri: Uri?, achievementImageUri: Uri?, studentName: String, title: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            runCatching {
                val profileUrl = if (profileUri != null) repo.uploadImage(ImageUtils.compress(getApplication(), profileUri)) else ""
                val achievementImageUrl = if (achievementImageUri != null) repo.uploadImage(ImageUtils.compress(getApplication(), achievementImageUri)) else ""
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val achievement = StudentAchievement(
                    studentName = studentName, achievementTitle = title,
                    description = description, photoUrl = profileUrl,
                    achievementImageUrl = achievementImageUrl, achievementDate = today
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
