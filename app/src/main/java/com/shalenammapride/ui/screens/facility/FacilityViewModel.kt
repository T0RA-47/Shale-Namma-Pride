package com.shalenammapride.ui.screens.facility

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.Facility
import com.shalenammapride.data.repository.FacilityRepository
import com.shalenammapride.util.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FacilityUiState(
    val facilities: List<Facility> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

class FacilityViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FacilityRepository()
    private val _uiState = MutableStateFlow(FacilityUiState())
    val uiState: StateFlow<FacilityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getFacilities()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { items -> _uiState.update { it.copy(facilities = items, isLoading = false) } }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(800)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun addFacility(imageUri: Uri?, name: String, description: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }
            runCatching {
                val imageUrl = if (imageUri != null) {
                    val bytes = ImageUtils.compress(getApplication(), imageUri)
                    repo.uploadImage(bytes)
                } else ""
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val facility = Facility(facilityName = name, description = description, imageUrl = imageUrl, uploadDate = today)
                repo.addFacility(facility).getOrThrow()
            }.onSuccess {
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun deleteFacility(id: String) { viewModelScope.launch { repo.deleteFacility(id) } }
    fun clearSuccess() { _uiState.update { it.copy(uploadSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
