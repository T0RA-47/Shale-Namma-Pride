package com.shalenammapride.ui.screens.facility

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
fun FacilityScreen(
    strings: AppStrings,
    isAdmin: Boolean,
    viewModel: FacilityViewModel = viewModel()
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
                FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Saffron) {
                    Icon(Icons.Filled.Add, contentDescription = strings.addFacility)
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else if (uiState.facilities.isEmpty()) {
            EmptyState(message = strings.noFacilities, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(LightGray).padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val withImages = uiState.facilities.filter { it.imageUrl.isNotEmpty() }
                if (withImages.isNotEmpty()) {
                    item {
                        FacilityGalleryPager(facilities = withImages.map { it.imageUrl })
                    }
                }

                item {
                    Text(
                        text = strings.schoolFacilities,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                items(uiState.facilities) { facility ->
                    ShaleCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = facility.facilityName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Saffron,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isAdmin) {
                                    IconButton(onClick = { viewModel.deleteFacility(facility.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = ErrorRed)
                                    }
                                }
                            }
                            if (facility.imageUrl.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                NetworkImage(
                                    url = facility.imageUrl,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            if (facility.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = facility.description, style = MaterialTheme.typography.bodyMedium, color = MediumText)
                            }
                            Text(text = "${strings.date}: ${facility.uploadDate}", style = MaterialTheme.typography.labelMedium, color = LightText)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddFacilityDialog(
            strings = strings,
            isUploading = uiState.isUploading,
            onDismiss = { showAddDialog = false },
            onSubmit = { uri, name, desc -> viewModel.addFacility(uri, name, desc) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FacilityGalleryPager(facilities: List<String>) {
    val pagerState = rememberPagerState(pageCount = { facilities.size })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) { page ->
            NetworkImage(
                url = facilities[page],
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(facilities.size) { idx ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == idx) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (pagerState.currentPage == idx) Saffron else LightText)
                )
            }
        }
    }
}

@Composable
private fun AddFacilityDialog(
    strings: AppStrings,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Uri?, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text(strings.addFacility) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(strings.facilityName) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text(strings.facilityDescription) }, modifier = Modifier.fillMaxWidth(), minLines = 2)
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
                onClick = { onSubmit(selectedUri, name, description) },
                enabled = name.isNotBlank() && !isUploading
            )
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isUploading) { Text(strings.cancel) } }
    )
}
