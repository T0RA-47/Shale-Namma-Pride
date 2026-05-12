package com.shalenammapride.ui.screens.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shalenammapride.R
import com.shalenammapride.ui.components.FullScreenImageDialog
import com.shalenammapride.ui.theme.*
import com.shalenammapride.util.AppStrings

@Composable
fun SchoolInfoScreen(strings: AppStrings) {
    val context = LocalContext.current
    var fullScreenUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Saffron, SaffronDark))).padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.school_logo),
                        contentDescription = "School Logo",
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Vijayanagara Vivekananda\nEducation Society",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "DICIMUS CONSEQUIMUR", style = MaterialTheme.typography.labelMedium, color = PureWhite.copy(alpha = 0.75f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = strings.buildingTrust, style = MaterialTheme.typography.bodyMedium, color = PureWhite.copy(alpha = 0.85f))
                }
            }
        }

        item { InfoRow(icon = Icons.Filled.History, label = strings.founded, value = "1990 — Estd. by Shri Hanumanthappa & Shri Nagarathna") }
        item { InfoRow(icon = Icons.Filled.Person, label = strings.principal, value = "Jyothi S (Headmistress)") }
        item {
            InfoRow(
                icon = Icons.Filled.AccountBalance,
                label = strings.schoolType,
                value = "Kannada Medium — Govt. Aided\nEnglish Medium — Private"
            )
        }
        item { InfoRow(icon = Icons.Filled.MenuBook, label = strings.grades, value = "Montessori to 10th Standard") }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/rdBGcwjhDcEb2dRi9"))
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(SaffronLight),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Saffron, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = strings.address, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "2008, 1st A Main Rd, Govindaraja Nagar Ward,\nMC Layout, Vijayanagar, Bengaluru — 560079",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to open in Maps", style = MaterialTheme.typography.labelSmall, color = Saffron)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:9696265860")))
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(IndiaGreenLight),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = IndiaGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = strings.phone, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "9696265860", style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold, color = IndiaGreen)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    fullScreenUrl?.let { FullScreenImageDialog(url = it, onDismiss = { fullScreenUrl = null }) }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(SaffronLight),
                contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Saffron, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
