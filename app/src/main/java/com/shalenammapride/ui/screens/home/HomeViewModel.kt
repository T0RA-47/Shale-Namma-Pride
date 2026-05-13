package com.shalenammapride.ui.screens.home

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shalenammapride.data.model.Announcement
import com.shalenammapride.data.model.MealUpdate
import com.shalenammapride.data.model.StudentAchievement
import com.shalenammapride.data.repository.AnnouncementRepository
import com.shalenammapride.data.repository.AchievementRepository
import com.shalenammapride.data.repository.MealRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AnnouncementReactionSummary(
    val likes: Int = 0, val laughs: Int = 0, val sads: Int = 0, val checks: Int = 0,
    val myVote: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val mealRepo = MealRepository()
    private val achievementRepo = AchievementRepository()
    private val announcementRepo = AnnouncementRepository()

    private val deviceId: String = Settings.Secure.getString(
        application.contentResolver, Settings.Secure.ANDROID_ID
    )

    val latestMeal: StateFlow<MealUpdate?> = mealRepo.getMeals()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestAchievement: StateFlow<StudentAchievement?> = achievementRepo.getAchievements()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val announcements: StateFlow<List<Announcement>> = announcementRepo.getAnnouncements()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _announcementReactions = MutableStateFlow<Map<String, AnnouncementReactionSummary>>(emptyMap())
    val announcementReactions: StateFlow<Map<String, AnnouncementReactionSummary>> = _announcementReactions.asStateFlow()

    init {
        viewModelScope.launch {
            announcementRepo.getReactions().catch { }.collect { allReactions ->
                val summaries = allReactions.mapValues { (_, votes) ->
                    AnnouncementReactionSummary(
                        likes = votes.values.count { it == "like" },
                        laughs = votes.values.count { it == "laugh" },
                        sads = votes.values.count { it == "sad" },
                        checks = votes.values.count { it == "check" },
                        myVote = votes[deviceId]
                    )
                }
                _announcementReactions.value = summaries
            }
        }
    }

    fun reactToAnnouncement(announcementId: String, vote: String) {
        viewModelScope.launch {
            val current = _announcementReactions.value[announcementId]?.myVote
            if (current == vote) announcementRepo.removeReaction(announcementId, deviceId)
            else announcementRepo.setReaction(announcementId, deviceId, vote)
        }
    }
}
