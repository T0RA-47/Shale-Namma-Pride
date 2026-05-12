package com.shalenammapride.ui.screens.meal

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.MealUpdate
import com.shalenammapride.data.repository.MealRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MealUiState(
    val meals: List<MealUpdate> = emptyList(),
    val isLoading: Boolean = true,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

class MealViewModel : ViewModel() {
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

    fun uploadMeal(imageUri: Uri, menuDescription: String, mealType: String, mealTime: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            runCatching {
                val imageUrl = repo.uploadMealImage(imageUri)
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

    fun deleteMeal(id: String) {
        viewModelScope.launch { repo.deleteMeal(id) }
    }

    fun clearSuccess() { _uiState.update { it.copy(uploadSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
