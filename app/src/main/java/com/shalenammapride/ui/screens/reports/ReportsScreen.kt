package com.shalenammapride.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shalenammapride.data.repository.AchievementRepository
import com.shalenammapride.data.repository.FacilityRepository
import com.shalenammapride.data.repository.FeedbackRepository
import com.shalenammapride.data.repository.MealRepository
import com.shalenammapride.ui.components.ShaleCard
import com.shalenammapride.ui.theme.*
import com.shalenammapride.util.AppStrings

@Composable
fun ReportsScreen(strings: AppStrings) {
    val context = LocalContext.current
    val mealRepo = remember { MealRepository() }
    val facilityRepo = remember { FacilityRepository() }
    val achievementRepo = remember { AchievementRepository() }
    val feedbackRepo = remember { FeedbackRepository() }

    var mealCount by remember { mutableStateOf(0) }
    var facilityCount by remember { mutableStateOf(0) }
    var achievementCount by remember { mutableStateOf(0) }
    var feedbackCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { mealRepo.getMeals().collect { mealCount = it.size } }
    LaunchedEffect(Unit) { facilityRepo.getFacilities().collect { facilityCount = it.size } }
    LaunchedEffect(Unit) { achievementRepo.getAchievements().collect { achievementCount = it.size } }
    LaunchedEffect(Unit) { feedbackRepo.getFeedback().collect { feedbackCount = it.size } }

    val reportText = """
*Shale - Namma Pride School Report*
Generated: ${java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())}

📋 *Summary*
• Meal Updates: $mealCount
• School Facilities: $facilityCount
• Student Achievements: $achievementCount
• Feedback Received: $feedbackCount

_Shared via Shale - Namma Pride App_
    """.trimIndent()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = strings.reports, style = MaterialTheme.typography.headlineMedium, color = Saffron)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), label = strings.meals, count = mealCount, icon = Icons.Filled.Restaurant)
                StatCard(modifier = Modifier.weight(1f), label = strings.facilities, count = facilityCount, icon = Icons.Filled.School)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), label = strings.achievements, count = achievementCount, icon = Icons.Filled.Star)
                StatCard(modifier = Modifier.weight(1f), label = strings.feedback, count = feedbackCount, icon = Icons.Filled.Feedback)
            }
        }

        item {
            ShaleCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = strings.shareViaWhatsApp, style = MaterialTheme.typography.titleLarge, color = Saffron)
                    Text(
                        text = "Share a summary of school activities with parents and community via WhatsApp.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                setPackage("com.whatsapp")
                            }
                            runCatching { context.startActivity(intent) }.onFailure {
                                val fallback = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, reportText)
                                }
                                context.startActivity(Intent.createChooser(fallback, strings.share))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndiaGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.shareViaWhatsApp, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ShaleCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Saffron, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.headlineLarge, color = Saffron)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
