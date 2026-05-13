package com.shalenammapride.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun AdminScreen(
    strings: AppStrings,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) { showAddAnnouncementDialog = false; viewModel.clearSuccess() }
    }
    LaunchedEffect(uiState.notificationSent) {
        if (uiState.notificationSent) {
            snackbarHostState.showSnackbar("Announcement posted & notification sent!")
            viewModel.clearNotification()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ShaleCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(text = strings.adminPanel, style = MaterialTheme.typography.headlineSmall, color = Saffron)
                            TextButton(onClick = onLogout) {
                                Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = ErrorRed)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Logout", color = ErrorRed)
                            }
                        }
                        Text(text = "Manage school announcements below. A push notification is sent to all users when you post.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Text(text = strings.announcements, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground) }
            item { ShaleButton(text = strings.addAnnouncement, onClick = { showAddAnnouncementDialog = true }) }

            if (uiState.isLoading) {
                item { LoadingState() }
            } else if (uiState.announcements.isEmpty()) {
                item { EmptyState(message = strings.noAnnouncements) }
            } else {
                items(uiState.announcements) { ann ->
                    ShaleCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = ann.title, style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface)
                                    if (ann.description.isNotEmpty()) {
                                        Text(text = ann.description, style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(text = ann.postedDate, style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { viewModel.deleteAnnouncement(ann.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = ErrorRed)
                                }
                            }
                            if (ann.imageUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                NetworkImage(url = ann.imageUrl,
                                    modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showAddAnnouncementDialog) {
        AddAnnouncementDialog(
            strings = strings,
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showAddAnnouncementDialog = false },
            onSubmit = { title, desc, imageUri -> viewModel.addAnnouncement(title, desc, imageUri) }
        )
    }
}

@Composable
private fun AddAnnouncementDialog(
    strings: AppStrings,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageUri = uri }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(strings.addAnnouncement) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text(strings.title) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text(strings.description) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                ShaleOutlinedButton(
                    text = if (imageUri != null) "✓ ${strings.uploadPhoto}" else strings.uploadPhoto,
                    onClick = { imagePicker.launch("image/*") }
                )
                if (isSubmitting) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Saffron)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.loading)
                    }
                }
            }
        },
        confirmButton = {
            ShaleButton(text = strings.submit, onClick = { onSubmit(title, description, imageUri) },
                enabled = title.isNotBlank() && !isSubmitting)
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text(strings.cancel) } }
    )
}
