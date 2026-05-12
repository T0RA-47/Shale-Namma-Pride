package com.shalenammapride.ui.screens.meal

import android.app.Application
import android.net.Uri
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

data class MealUiState(
    val meals: List<MealUpdate> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

class MealViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MealRepository()
    private val _uiState = MutableStateFlow(MealUiState())
    val uiState: StateFlow<MealUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMeals()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { meals -> _uiState.update { it.copy(meals = meals, isLoading = false) } }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(800)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun uploadMeal(imageUri: Uri, menuDescription: String, mealType: String, mealTime: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            runCatching {
                val imageBytes = ImageUtils.compress(getApplication(), imageUri)
                val imageUrl = repo.uploadMealImage(imageBytes)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val meal = MealUpdate(
                    photoUrl = imageUrl,
                    menuDescription = menuDescription,
                    mealType = mealType,
                    mealTime = mealTime,
                    uploadDate = today,
                    uploadedBy = "Admin"
                )
                repo.addMeal(meal).getOrThrow()
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
            val meal = MealUpdate(
                menuDescription = menuDescription,
                mealType = mealType,
                mealTime = mealTime,
                uploadDate = today,
                uploadedBy = "Admin"
            )
            repo.addMeal(meal)
                .onSuccess { _uiState.update { it.copy(isUploading = false, uploadSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isUploading = false, error = e.message) } }
        }
    }

    fun reactToMeal(id: String) {
        viewModelScope.launch { repo.incrementLikes(id) }
    }

    fun deleteMeal(id: String) {
        viewModelScope.launch { repo.deleteMeal(id) }
    }

    fun clearSuccess() { _uiState.update { it.copy(uploadSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
