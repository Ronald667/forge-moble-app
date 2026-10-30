package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

sealed class Screen {
    object Projects : Screen()
    data class Editor(val projectId: String) : Screen()
    data class CloudBuild(val projectId: String) : Screen()
    data class VersionControl(val projectId: String) : Screen()
    object Monitoring : Screen()
}

class MainActivity : ComponentActivity() {
    private val repository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Projects) }

                    val projects by repository.projects.collectAsStateWithLifecycle()
                    val buildLogs by repository.buildLogs.collectAsStateWithLifecycle()
                    val crashes by repository.crashes.collectAsStateWithLifecycle()
                    val analytics by repository.analytics.collectAsStateWithLifecycle()

                    when (val screen = currentScreen) {
                        is Screen.Projects -> {
                            ProjectsScreen(
                                projects = projects,
                                onSelectProject = { currentScreen = Screen.Editor(it) },
                                onOpenCloudBuild = { currentScreen = Screen.CloudBuild(it) },
                                onOpenVersionControl = { currentScreen = Screen.VersionControl(it) },
                                onOpenMonitoring = { currentScreen = Screen.Monitoring },
                                onCreateProject = { name, pkg, desc, cat ->
                                    repository.addProject(name, pkg, desc, cat)
                                }
                            )
                        }
                        is Screen.Editor -> {
                            BackHandler { currentScreen = Screen.Projects }
                            val project = projects.find { it.id == screen.projectId } ?: projects.first()
                            EditorScreen(
                                project = project,
                                onBack = { currentScreen = Screen.Projects },
                                onUpdateComponents = { comps -> repository.updateProjectComponents(project.id, comps) },
                                onAddComponent = { type, label -> repository.addComponentToProject(project.id, type, label) },
                                onGenerateAi = { prompt -> repository.generateAiComponent(prompt) }
                            )
                        }
                        is Screen.CloudBuild -> {
                            BackHandler { currentScreen = Screen.Projects }
                            val project = projects.find { it.id == screen.projectId } ?: projects.first()
                            CloudBuildScreen(
                                project = project,
                                buildLogs = buildLogs,
                                onBack = { currentScreen = Screen.Projects },
                                onTriggerBuild = { id ->
                                    repository.triggerCloudBuild(id) {}
                                }
                            )
                        }
                        is Screen.VersionControl -> {
                            BackHandler { currentScreen = Screen.Projects }
                            val project = projects.find { it.id == screen.projectId } ?: projects.first()
                            val githubUser by repository.githubUser.collectAsStateWithLifecycle()
                            val githubToken by repository.githubToken.collectAsStateWithLifecycle()
                            VersionControlScreen(
                                project = project,
                                githubUser = githubUser,
                                githubToken = githubToken,
                                onAuthenticateGitHub = { token, cb -> repository.authenticateGitHub(token, cb) },
                                onPush = { id, repo, cb -> repository.pushToGitHub(id, repo, cb) },
                                onPull = { id, repo, cb -> repository.pullFromGitHub(id, repo, cb) },
                                onBack = { currentScreen = Screen.Projects }
                            )
                        }
                        is Screen.Monitoring -> {
                            BackHandler { currentScreen = Screen.Projects }
                            MonitoringScreen(
                                analytics = analytics,
                                crashes = crashes,
                                onBack = { currentScreen = Screen.Projects }
                            )
                        }
                    }
                }
            }
        }
    }
}
