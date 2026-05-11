package com.shalenammapride.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.Announcement
import com.shalenammapride.data.model.MealUpdate
import com.shalenammapride.data.model.StudentAchievement
import com.shalenammapride.data.repository.AnnouncementRepository
import com.shalenammapride.data.repository.AchievementRepository
import com.shalenammapride.data.repository.MealRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val mealRepo = MealRepository()
    private val achievementRepo = AchievementRepository()
    private val announcementRepo = AnnouncementRepository()

    val latestMeal: StateFlow<MealUpdate?> = mealRepo.getMeals()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestAchievement: StateFlow<StudentAchievement?> = achievementRepo.getAchievements()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val announcements: StateFlow<List<Announcement>> = announcementRepo.getAnnouncements()
        .map { it.take(3) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
