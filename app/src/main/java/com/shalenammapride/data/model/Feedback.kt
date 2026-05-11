package com.shalenammapride.data.model

data class Feedback(
    val id: String = "",
    val userName: String = "",
    val message: String = "",
    val isAnonymous: Boolean = false,
    val submittedDate: String = ""
)
