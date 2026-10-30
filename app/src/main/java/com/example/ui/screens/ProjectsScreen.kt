package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BuildStatus
import com.example.data.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projects: List<Project>,
    onSelectProject: (String) -> Unit,
    onOpenCloudBuild: (String) -> Unit,
    onOpenVersionControl: (String) -> Unit,
    onOpenMonitoring: () -> Unit,
    onCreateProject: (String, String, String, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageIconsStandardPhoneOrBuild(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("AppForge Mobile", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Mobile Android App Studio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenMonitoring,
                        modifier = Modifier.testTag("monitoring_button")
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = "Monitoring & Analytics")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("create_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New App Project", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Your App Projects (${projects.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (projects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No projects yet. Tap + to create your first mobile app!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(projects) { project ->
                        ProjectCard(
                            project = project,
                            onOpenEditor = { onSelectProject(project.id) },
                            onOpenBuild = { onOpenCloudBuild(project.id) },
                            onOpenGit = { onOpenVersionControl(project.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, pkg, desc, cat ->
                onCreateProject(name, pkg, desc, cat)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun imageIconsStandardPhoneOrBuild() = Icons.Default.PhoneAndroid

@Composable
fun ProjectCard(
    project: Project,
    onOpenEditor: () -> Unit,
    onOpenBuild: () -> Unit,
    onOpenGit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenEditor)
            .testTag("project_card_${project.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.name.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = project.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                StatusBadge(status = project.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = onOpenEditor,
                        label = { Text("Visual Editor") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = onOpenBuild,
                        label = { Text("Cloud Build") },
                        leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                IconButton(onClick = onOpenGit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Source, contentDescription = "Version Control", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: BuildStatus) {
    val (text, color, containerColor) = when (status) {
        BuildStatus.SUCCESS -> Triple("Ready", Color(0xFF0F9D58), Color(0xFFE6F4EA))
        BuildStatus.BUILDING -> Triple("Building", Color(0xFF1A73E8), Color(0xFFE8F0FE))
        BuildStatus.FAILED -> Triple("Failed", Color(0xFFD93025), Color(0xFFFCE8E6))
        BuildStatus.QUEUED -> Triple("Queued", Color(0xFFF29900), Color(0xFFFEF7E0))
    }

    Surface(
        color = containerColor,
        shape = CircleShape
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("com.ai.appforge.") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Utility") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Android App") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App Name") },
                    modifier = Modifier.fillMaxWidth().testTag("app_name_input")
                )
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name") },
                    modifier = Modifier.fillMaxWidth().testTag("package_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().testTag("description_input")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, packageName, description, category) },
                modifier = Modifier.testTag("confirm_create_button")
            ) {
                Text("Create App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
