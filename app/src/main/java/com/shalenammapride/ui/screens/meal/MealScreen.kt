package com.shalenammapride.ui.screens.meal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
fun MealScreen(
    strings: AppStrings,
    isAdmin: Boolean,
    viewModel: MealViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) {
            showAddDialog = false
            viewModel.clearSuccess()
        }
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
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Saffron
                ) {
                    Icon(Icons.Filled.Add, contentDescription = strings.addMeal)
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (uiState.meals.isEmpty()) {
            EmptyState(message = strings.noMealsToday, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightGray)
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.meals) { meal ->
                    ShaleCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = strings.todaysMeal,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Saffron
                                    )
                                    if (meal.mealType.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                color = SaffronLight,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = meal.mealType,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Saffron,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            if (meal.mealTime.isNotEmpty()) {
                                                Text(
                                                    text = meal.mealTime,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = LightText
                                                )
                                            }
                                        }
                                    }
                                }
                                if (isAdmin) {
                                    IconButton(onClick = { viewModel.deleteMeal(meal.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = ErrorRed)
                                    }
                                }
                            }
                            if (meal.photoUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                NetworkImage(
                                    url = meal.photoUrl,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = meal.menuDescription,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${strings.date}: ${meal.uploadDate}",
                                style = MaterialTheme.typography.labelMedium,
                                color = LightText
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddMealDialog(
            strings = strings,
            isUploading = uiState.isUploading,
            onDismiss = { showAddDialog = false },
            onSubmit = { uri, description, mealType, mealTime ->
                if (uri != null) viewModel.uploadMeal(uri, description, mealType, mealTime)
                else viewModel.addMealWithoutImage(description, mealType, mealTime)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMealDialog(
    strings: AppStrings,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Uri?, String, String, String) -> Unit
) {
    val mealTypes = listOf(strings.breakfast, strings.lunch, strings.snack, strings.dinner)
    var description by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMealType by remember { mutableStateOf(mealTypes[1]) }
    var mealTime by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedUri = uri }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text(strings.addMeal) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = strings.mealType, style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mealTypes.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Saffron,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = mealTime,
                    onValueChange = { mealTime = it },
                    label = { Text(strings.mealTimeOptional) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.mealMenu) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
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
                onClick = { onSubmit(selectedUri, description, selectedMealType, mealTime.trim()) },
                enabled = description.isNotBlank() && !isUploading
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text(strings.cancel)
            }
        }
    )
}
