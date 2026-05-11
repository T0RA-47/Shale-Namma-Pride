package com.shalenammapride.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shalenammapride.ui.components.NetworkImage
import com.shalenammapride.ui.components.ShaleCard
import com.shalenammapride.ui.navigation.Screen
import com.shalenammapride.ui.theme.*
import com.shalenammapride.util.AppStrings

@Composable
fun HomeScreen(
    strings: AppStrings,
    onNavigate: (Screen) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val latestMeal by viewModel.latestMeal.collectAsState()
    val latestAchievement by viewModel.latestAchievement.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(LightGray),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WelcomeBanner(strings = strings)
        }

        item {
            Text(
                text = strings.recentUpdates,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            QuickAccessGrid(strings = strings, onNavigate = onNavigate)
        }

        if (latestMeal != null) {
            item {
                ShaleCard(modifier = Modifier.clickable { onNavigate(Screen.Meals) }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.todaysMeal,
                            style = MaterialTheme.typography.titleLarge,
                            color = Saffron
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (latestMeal!!.photoUrl.isNotEmpty()) {
                            NetworkImage(
                                url = latestMeal!!.photoUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = latestMeal!!.menuDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumText
                        )
                        Text(
                            text = latestMeal!!.uploadDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = LightText
                        )
                    }
                }
            }
        }

        if (latestAchievement != null) {
            item {
                ShaleCard(modifier = Modifier.clickable { onNavigate(Screen.Achievements) }) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (latestAchievement!!.photoUrl.isNotEmpty()) {
                            NetworkImage(
                                url = latestAchievement!!.photoUrl,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(36.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Saffron,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.studentStar,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Saffron
                                )
                            }
                            Text(
                                text = latestAchievement!!.studentName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = latestAchievement!!.achievementTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumText
                            )
                        }
                    }
                }
            }
        }

        if (announcements.isNotEmpty()) {
            item {
                Text(
                    text = strings.announcements,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(count = announcements.size) { idx ->
                val ann = announcements[idx]
                ShaleCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = ann.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (ann.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ann.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumText
                            )
                        }
                        Text(
                            text = ann.postedDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = LightText
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun WelcomeBanner(strings: AppStrings) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Saffron, SaffronDark)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = strings.welcomeTitle,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.buildingTrust,
                style = MaterialTheme.typography.bodyMedium.copy(color = PureWhite.copy(alpha = 0.85f))
            )
        }
    }
}

@Composable
private fun QuickAccessGrid(strings: AppStrings, onNavigate: (Screen) -> Unit) {
    val items = listOf(
        Triple(Icons.Filled.Restaurant, strings.meals, Screen.Meals),
        Triple(Icons.Filled.School, strings.facilities, Screen.Facilities),
        Triple(Icons.Filled.Star, strings.achievements, Screen.Achievements),
        Triple(Icons.Filled.Feedback, strings.feedback, Screen.Feedback),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { (icon, label, screen) ->
            QuickAccessCard(
                icon = icon,
                label = label,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(screen) }
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Saffron,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DarkText,
                maxLines = 1
            )
        }
    }
}
