package com.shalenammapride.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
fun FeedbackScreen(
    strings: AppStrings,
    isAdmin: Boolean,
    viewModel: FeedbackViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            name = ""; message = ""; isAnonymous = false
            showSuccess = true
            viewModel.clearSuccess()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ShaleCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = strings.feedback, style = MaterialTheme.typography.headlineSmall, color = Saffron)

                    if (!isAnonymous) {
                        OutlinedTextField(value = name, onValueChange = { name = it },
                            label = { Text(strings.yourName) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }

                    OutlinedTextField(value = message, onValueChange = { message = it },
                        label = { Text(strings.yourMessage) }, modifier = Modifier.fillMaxWidth(), minLines = 4)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAnonymous, onCheckedChange = { isAnonymous = it },
                            colors = CheckboxDefaults.colors(checkedColor = Saffron))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = strings.submitAnonymously, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                    }

                    if (showSuccess) {
                        Text(text = strings.feedbackSubmitted, color = IndiaGreen, style = MaterialTheme.typography.bodyMedium)
                    }

                    ShaleButton(
                        text = if (uiState.isSubmitting) strings.loading else strings.submit,
                        onClick = { viewModel.submitFeedback(name, message, isAnonymous); showSuccess = false },
                        enabled = message.isNotBlank() && !uiState.isSubmitting
                    )
                }
            }
        }

        if (isAdmin && uiState.feedbackList.isNotEmpty()) {
            item {
                Text(text = "Submitted Feedback (${uiState.feedbackList.size})",
                    style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            }
            items(uiState.feedbackList) { fb ->
                ShaleCard {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(SaffronLight),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Saffron)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (fb.isAnonymous || fb.userName.isEmpty()) "Anonymous" else fb.userName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = fb.message, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = fb.submittedDate, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.deleteFeedback(fb.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = ErrorRed)
                        }
                    }
                }
            }
        } else if (!isAdmin && uiState.feedbackList.isEmpty() && !uiState.isLoading) {
            item { EmptyState(message = strings.noFeedback) }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
