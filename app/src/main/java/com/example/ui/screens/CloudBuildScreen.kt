package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BuildLog
import com.example.data.BuildStatus
import com.example.data.LogLevel
import com.example.data.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBuildScreen(
    project: Project,
    buildLogs: List<BuildLog>,
    onBack: () -> Unit,
    onTriggerBuild: (String) -> Unit
) {
    val context = LocalContext.current
    var isBuilding by remember { mutableStateOf(project.status == BuildStatus.BUILDING) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cloud Build Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("build_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Project Status & Build Trigger Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Package: ${project.packageName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(status = project.status)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isBuilding) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Compiling cloud APK binaries & signing release...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                isBuilding = true
                                onTriggerBuild(project.id)
                            },
                            enabled = !isBuilding,
                            modifier = Modifier.weight(1f).testTag("trigger_build_button")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBuilding) "Compiling..." else "Trigger Cloud Build")
                        }
                    }
                }
            }

            // Built Binaries & Download Links
            Text("Built Binaries & Download Links", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = Color(0xFF0F9D58), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("app-release.apk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Signed Production Binary • 14.2 MB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = { Toast.makeText(context, "Downloading app-release.apk (14.2 MB)...", Toast.LENGTH_LONG).show() },
                            modifier = Modifier.testTag("download_release_apk")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("app-debug.apk", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Debug Binary with Profiler • 12.8 MB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "Downloading app-debug.apk (12.8 MB)...", Toast.LENGTH_LONG).show() },
                            modifier = Modifier.testTag("download_debug_apk")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }
                    }
                }
            }

            // Real-time Build Logs Stream
            Text("Real-Time Cloud Build Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(buildLogs) { log ->
                        val color = when (log.level) {
                            LogLevel.SUCCESS -> Color(0xFF81C995)
                            LogLevel.ERROR -> Color(0xFFF28B82)
                            LogLevel.WARN -> Color(0xFFFDD663)
                            LogLevel.INFO -> Color(0xFFE8EAED)
                        }
                        Text(
                            text = "[${log.timestamp}] ${log.message}",
                            color = color,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
