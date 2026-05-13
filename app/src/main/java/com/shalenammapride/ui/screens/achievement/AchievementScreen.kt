package com.shalenammapride.ui.screens.achievement

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var fullScreenUrl by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) { showAddDialog = false; viewModel.clearSuccess() }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = "Upload failed: $it", duration = SnackbarDuration.Long)
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
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
                uiState.achievements.isEmpty() -> EmptyState(message = strings.noAchievements, modifier = Modifier.fillMaxSize())
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.achievements) { achievement ->
                        val reaction = uiState.reactionData[achievement.id] ?: AchievementLikeSummary()
                        val commentCount = if (uiState.commentsState.achievementId == achievement.id)
                            uiState.commentsState.comments.size else 0

                        ShaleCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header: profile photo + name + title + actions
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                    if (achievement.photoUrl.isNotEmpty()) {
                                        NetworkImage(
                                            url = achievement.photoUrl,
                                            modifier = Modifier.size(64.dp).clip(CircleShape)
                                                .clickable { fullScreenUrl = achievement.photoUrl }
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(64.dp).clip(CircleShape).background(SaffronLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = Saffron, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = achievement.studentName, style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = achievement.achievementTitle, style = MaterialTheme.typography.bodyLarge, color = Saffron)
                                        Text(text = achievement.achievementDate, style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            val text = "${achievement.studentName} — ${achievement.achievementTitle}\n${strings.date}: ${achievement.achievementDate}\n— ${strings.appName}"
                                            context.startActivity(Intent.createChooser(
                                                Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) },
                                                strings.share
                                            ))
                                        }) {
                                            Icon(Icons.Filled.Share, contentDescription = strings.share, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (isAdmin) {
                                            IconButton(onClick = { viewModel.deleteAchievement(achievement.id) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = strings.delete, tint = ErrorRed)
                                            }
                                        }
                                    }
                                }

                                // Achievement showcase image (full-width)
                                if (achievement.achievementImageUrl.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    NetworkImage(
                                        url = achievement.achievementImageUrl,
                                        modifier = Modifier.fillMaxWidth().height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { fullScreenUrl = achievement.achievementImageUrl }
                                    )
                                }

                                if (achievement.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = achievement.description, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(4.dp))

                                // Reaction + Comment row
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.react(achievement.id) }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            if (reaction.myVoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                            contentDescription = strings.likes,
                                            tint = if (reaction.myVoted) IndiaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    if (reaction.count > 0) {
                                        Text("${reaction.count}", style = MaterialTheme.typography.labelMedium,
                                            color = if (reaction.myVoted) IndiaGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(onClick = { viewModel.openComments(achievement.id) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Filled.ChatBubbleOutline, contentDescription = strings.comments,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    }
                                    Text(strings.comments, style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    fullScreenUrl?.let { FullScreenImageDialog(url = it, onDismiss = { fullScreenUrl = null }) }

    uiState.commentsState.achievementId?.let {
        CommentsDialog(
            strings = strings,
            commentsState = uiState.commentsState,
            isAdmin = isAdmin,
            onDismiss = { viewModel.closeComments() },
            onAddComment = { name, msg -> viewModel.addComment(name, msg) },
            onDeleteComment = { id -> viewModel.deleteComment(id) }
        )
    }

    if (showAddDialog) {
        AddAchievementDialog(
            strings = strings,
            isUploading = uiState.isUploading,
            onDismiss = { showAddDialog = false },
            onSubmit = { profileUri, achievementUri, name, title, desc ->
                viewModel.addAchievement(profileUri, achievementUri, name, title, desc)
            }
        )
    }
}

@Composable
private fun CommentsDialog(
    strings: AppStrings,
    commentsState: CommentsState,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onAddComment: (String, String) -> Unit,
    onDeleteComment: (String) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.comments, style = MaterialTheme.typography.titleLarge, color = Saffron)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = strings.cancel, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                HorizontalDivider()

                if (commentsState.comments.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(strings.noComments, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(commentsState.comments) { comment ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(SaffronLight),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = Saffron, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = comment.userName, style = MaterialTheme.typography.labelLarge,
                                        color = Saffron, fontWeight = FontWeight.SemiBold)
                                    Text(text = comment.message, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface)
                                }
                                if (isAdmin) {
                                    IconButton(onClick = { onDeleteComment(comment.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Delete, contentDescription = strings.delete,
                                            tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = userName, onValueChange = { userName = it },
                        label = { Text(strings.yourName) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = message, onValueChange = { message = it },
                            label = { Text(strings.typeComment) }, modifier = Modifier.weight(1f), maxLines = 3)
                        IconButton(
                            onClick = {
                                if (message.isNotBlank()) {
                                    onAddComment(userName, message)
                                    message = ""
                                }
                            },
                            modifier = Modifier.size(48.dp).background(Saffron, CircleShape)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = strings.submit, tint = PureWhite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAchievementDialog(
    strings: AppStrings,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Uri?, Uri?, String, String, String) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var achievementUri by remember { mutableStateOf<Uri?>(null) }
    val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> profileUri = uri }
    val achievementPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> achievementUri = uri }

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
                    text = if (profileUri != null) "✓ ${strings.profilePhoto}" else strings.profilePhoto,
                    onClick = { profilePicker.launch("image/*") }
                )
                ShaleOutlinedButton(
                    text = if (achievementUri != null) "✓ ${strings.achievementPhoto}" else strings.achievementPhoto,
                    onClick = { achievementPicker.launch("image/*") }
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
            ShaleButton(text = strings.submit,
                onClick = { onSubmit(profileUri, achievementUri, studentName, title, description) },
                enabled = studentName.isNotBlank() && title.isNotBlank() && !isUploading)
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isUploading) { Text(strings.cancel) } }
    )
}
