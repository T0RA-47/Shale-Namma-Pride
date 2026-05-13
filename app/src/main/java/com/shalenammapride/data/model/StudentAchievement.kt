package com.shalenammapride.data.model

data class StudentAchievement(
    val id: String = "",
    val studentName: String = "",
    val achievementTitle: String = "",
    val description: String = "",
    val photoUrl: String = "",            // profile photo (circle)
    val achievementImageUrl: String = "", // showcase image (full-width)
    val achievementDate: String = ""
)
