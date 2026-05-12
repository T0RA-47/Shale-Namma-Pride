package com.shalenammapride.data.model

data class MealUpdate(
    val id: String = "",
    val photoUrl: String = "",
    val menuDescription: String = "",
    val mealType: String = "",
    val mealTime: String = "",
    val likes: Long = 0L,
    val uploadDate: String = "",
    val uploadedBy: String = ""
)
