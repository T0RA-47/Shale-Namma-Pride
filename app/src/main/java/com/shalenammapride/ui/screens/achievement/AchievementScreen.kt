package com.shalenammapride.ui.screens.achievement

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shalenammapride.ui.components.*
import com.shalenammapride.ui.theme.*
import com.shalenammapride.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    strings: AppStrings,
    isAdmin: Boolean,
    viewModel: AchievementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) { showAddDialog = false; viewModel.clearSuccess() }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = "Upload failed: $error", duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Saffron) {
                    Icon(Icons.Filled.Add, contentDescription = strings.addAchievement)
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (uiState.achievements.isEmpty()) {
            EmptyState(message = strings.noAchievements, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(LightGray).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.achievements) { achievement ->
                    ShaleCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (achievement.photoUrl.isNotEmpty()) {
                                    NetworkImage(
                                        url = achievement.photoUrl,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(SaffronLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Saffron, modifier = Modifier.size(36.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = achievement.studentName,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = DarkText
                                            )
                                            Text(
                                                text = achievement.achievementTitle,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Saffron
                                            )
                                        }
                                        if (isAdmin) {
                                            IconButton(onClick = { viewModel.deleteAchievement(achievement.id) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = ErrorRed)
                                            }
                                        }
                                    }
                                    if (achievement.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = achievement.description, style = MaterialTheme.typography.bodyMedium, color = MediumText)
                                    }
                                    Text(text = achievement.achievementDate, style = MaterialTheme.typography.labelMedium, color = LightText)
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddAchievementDialog(
            strings = strings,
            isUploading = uiState.isUploading,
            onDismiss = { showAddDialog = false },
            onSubmit = { uri, name, title, desc -> viewModel.addAchievement(uri, name, title, desc) }
        )
    }
}

@Composable
private fun AddAchievementDialog(
    strings: AppStrings,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Uri?, String, String, String) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedUri = uri }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text(strings.addAchievement) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = studentName, onValueChange = { studentName = it },
                    label = { Text(strings.studentName) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text(strings.achievementTitle) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text(strings.achievementDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                ShaleOutlinedButton(
                    text = if (selectedUri != null) "✓ ${strings.uploadPhoto}" else strings.uploadPhoto,
                    onClick = { imagePicker.launch("image/*") }
                )
                if (isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Saffron)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.uploadingImage)
                    }
                }
            }
        },
        confirmButton = {
            ShaleButton(
                text = strings.submit,
                onClick = { onSubmit(selectedUri, studentName, title, description) },
                enabled = studentName.isNotBlank() && title.isNotBlank() && !isUploading
            )
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isUploading) { Text(strings.cancel) } }
    )
}
