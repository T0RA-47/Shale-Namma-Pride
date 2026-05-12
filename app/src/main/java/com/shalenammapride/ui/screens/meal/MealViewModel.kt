package com.shalenammapride.ui.screens.meal

import android.app.Application
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.MealUpdate
import com.shalenammapride.data.repository.MealRepository
import com.shalenammapride.util.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ReactionSummary(val ups: Int = 0, val downs: Int = 0, val myVote: String? = null)

data class MealUiState(
    val meals: List<MealUpdate> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val reactionData: Map<String, ReactionSummary> = emptyMap(),
    val error: String? = null
)

class MealViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MealRepository()
    private val _uiState = MutableStateFlow(MealUiState())
    val uiState: StateFlow<MealUiState> = _uiState.asStateFlow()

    private val deviceId: String = Settings.Secure.getString(
        application.contentResolver, Settings.Secure.ANDROID_ID
    )

    init {
        viewModelScope.launch {
            repo.getMeals()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { meals -> _uiState.update { it.copy(meals = meals, isLoading = false) } }
        }
        viewModelScope.launch {
            repo.getReactions()
                .catch { }
                .collect { allReactions ->
                    val summaries = allReactions.mapValues { (_, votes) ->
                        ReactionSummary(
                            ups = votes.values.count { it == "up" },
                            downs = votes.values.count { it == "down" },
                            myVote = votes[deviceId]
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

    fun react(mealId: String, vote: String) {
        viewModelScope.launch {
            val currentVote = _uiState.value.reactionData[mealId]?.myVote
            if (currentVote == vote) {
                repo.removeReaction(mealId, deviceId)
            } else {
                repo.setReaction(mealId, deviceId, vote)
            }
        }
    }

    fun uploadMeal(imageUri: Uri, menuDescription: String, mealType: String, mealTime: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            runCatching {
                val imageBytes = ImageUtils.compress(getApplication(), imageUri)
                val imageUrl = repo.uploadMealImage(imageBytes)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                repo.addMeal(MealUpdate(photoUrl = imageUrl, menuDescription = menuDescription, mealType = mealType, mealTime = mealTime, uploadDate = today, uploadedBy = "Admin")).getOrThrow()
            }.onSuccess {
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun addMealWithoutImage(menuDescription: String, mealType: String, mealTime: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repo.addMeal(MealUpdate(menuDescription = menuDescription, mealType = mealType, mealTime = mealTime, uploadDate = today, uploadedBy = "Admin"))
                .onSuccess { _uiState.update { it.copy(isUploading = false, uploadSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isUploading = false, error = e.message) } }
        }
    }

    fun deleteMeal(id: String) { viewModelScope.launch { repo.deleteMeal(id) } }
    fun clearSuccess() { _uiState.update { it.copy(uploadSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
