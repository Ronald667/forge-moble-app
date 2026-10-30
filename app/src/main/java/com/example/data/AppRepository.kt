package com.example.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository {
    private val _projects = MutableStateFlow(
        listOf(
            Project(
                id = "proj_1",
                name = "ExpenseTracker Pro",
                packageName = "com.ai.appforge.expensetracker",
                description = "Track daily budgets and expenses with category breakdown charts.",
                category = "Finance",
                version = "1.2.0",
                status = BuildStatus.SUCCESS,
                updatedAt = "10 mins ago",
                components = listOf(
                    UIComponent(id = "c1", type = ComponentType.TEXT, label = "My Wallet Dashboard", value = "Balance: \$2,450.00", order = 0),
                    UIComponent(id = "c2", type = ComponentType.BUTTON, label = "Add Expense", jsLogic = "function onClick() { toast('Opening expense form...'); }", order = 1),
                    UIComponent(id = "c3", type = ComponentType.CARD, label = "Recent Transactions", value = "Groceries - \$45.00\nCoffee - \$4.50", order = 2)
                ),
                dependencies = listOf("androidx.core:core-ktx", "androidx.compose.material3", "androidx.room:room-runtime", "com.squareup.retrofit2:retrofit"),
                commits = listOf(
                    Commit("commit_1", "Initial scaffold & material theme", "Alice", "2 hours ago", "main"),
                    Commit("commit_2", "Added expense charts and JS logic", "Bob", "10 mins ago", "main")
                ),
                collaborators = listOf(
                    Collaborator("u1", "Alice Smith", "Lead Developer", "alice@appforge.io"),
                    Collaborator("u2", "Bob Jones", "UI Designer", "bob@appforge.io")
                )
            ),
            Project(
                id = "proj_2",
                name = "FitPulse Tracker",
                packageName = "com.ai.appforge.fitpulse",
                description = "Monitor daily steps, heart rate, and workout routines.",
                category = "Health & Fitness",
                version = "1.0.4",
                status = BuildStatus.SUCCESS,
                updatedAt = "2 hours ago",
                components = listOf(
                    UIComponent(id = "f1", type = ComponentType.TEXT, label = "Daily Step Goal", value = "8,420 / 10,000 steps", order = 0),
                    UIComponent(id = "f2", type = ComponentType.SWITCH, label = "Heart Rate Monitor", isChecked = true, jsLogic = "function onToggle(val) { state.hrActive = val; }", order = 1),
                    UIComponent(id = "f3", type = ComponentType.BUTTON, label = "Log Workout", jsLogic = "function onClick() { toast('Workout logged successfully!'); }", order = 2)
                ),
                dependencies = listOf("androidx.core:core-ktx", "androidx.compose.material3", "com.google.android.gms:play-services-location"),
                commits = listOf(
                    Commit("commit_f1", "Setup step counter sensor integration", "Charlie", "Yesterday", "main")
                ),
                collaborators = listOf(
                    Collaborator("u3", "Charlie Brown", "Fitness Lead", "charlie@appforge.io")
                )
            ),
            Project(
                id = "proj_3",
                name = "Recipe Vault",
                packageName = "com.ai.appforge.recipevault",
                description = "Discover gourmet dishes, ingredients, and step-by-step cooking timers.",
                category = "Food & Drink",
                version = "0.9.1",
                status = BuildStatus.BUILDING,
                updatedAt = "Just now",
                components = listOf(
                    UIComponent(id = "r1", type = ComponentType.TEXT, label = "Featured Recipe", value = "Truffle Mushroom Risotto", order = 0),
                    UIComponent(id = "r2", type = ComponentType.TEXT_FIELD, label = "Search Ingredients", hint = "Enter ingredient...", order = 1)
                ),
                dependencies = listOf("androidx.core:core-ktx", "androidx.compose.material3", "io.coil-kt:coil-compose"),
                commits = listOf(
                    Commit("commit_r1", "Drafted recipe UI layout", "Diana", "3 hours ago", "dev")
                ),
                collaborators = listOf(
                    Collaborator("u4", "Diana Prince", "Chef & Product Owner", "diana@appforge.io")
                )
            )
        )
    )
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _buildLogs = MutableStateFlow<List<BuildLog>>(
        listOf(
            BuildLog("l1", "10:42:01", "Initializing Gradle daemon and Android build environment", LogLevel.INFO),
            BuildLog("l2", "10:42:03", "Compiling Jetpack Compose UI trees for ExpenseTracker Pro", LogLevel.INFO),
            BuildLog("l3", "10:42:05", "Validating JavaScript execution blocks (Nashorn/QuickJS)", LogLevel.SUCCESS),
            BuildLog("l4", "10:42:08", "Generated signed APK successfully (app-release.apk - 14.2 MB)", LogLevel.SUCCESS)
        )
    )
    val buildLogs: StateFlow<List<BuildLog>> = _buildLogs.asStateFlow()

    private val _crashes = MutableStateFlow<List<CrashReport>>(
        listOf(
            CrashReport(
                id = "cr_1",
                timestamp = "Yesterday 14:22",
                exception = "NullPointerException",
                message = "Attempt to invoke virtual method on a null object reference",
                stackTrace = "com.ai.appforge.expensetracker.MainActivity.onCreate(MainActivity.kt:42)\nandroid.app.Activity.performCreate(Activity.java:8000)",
                device = "Pixel 7 Pro (Android 14)",
                resolved = true
            )
        )
    )
    val crashes: StateFlow<List<CrashReport>> = _crashes.asStateFlow()

    private val _analytics = MutableStateFlow(
        AnalyticsData(
            dailyActiveUsers = 1420,
            screenViews = 8940,
            crashFreeRate = 99.4f,
            avgSessionDuration = "4m 12s"
        )
    )
    val analytics: StateFlow<AnalyticsData> = _analytics.asStateFlow()

    fun addProject(name: String, packageName: String, description: String, category: String) {
        val newProj = Project(
            id = "proj_${System.currentTimeMillis()}",
            name = name,
            packageName = packageName,
            description = description,
            category = category,
            components = listOf(
                UIComponent(id = "comp_1", type = ComponentType.TEXT, label = "Welcome to $name", value = "Built with AppForge Mobile", order = 0),
                UIComponent(id = "comp_2", type = ComponentType.BUTTON, label = "Tap Me", jsLogic = "function onClick() { toast('Hello from AppForge!'); }", order = 1)
            ),
            commits = listOf(
                Commit("commit_init", "Initial project creation", "Developer", "Just now")
            )
        )
        _projects.update { listOf(newProj) + it }
    }

    fun updateProjectComponents(projectId: String, components: List<UIComponent>) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    proj.copy(components = components, updatedAt = "Just now")
                } else {
                    proj
                }
            }
        }
    }

    fun addComponentToProject(projectId: String, type: ComponentType, label: String) {
        _projects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val newComp = UIComponent(
                        id = "comp_${System.currentTimeMillis()}",
                        type = type,
                        label = label,
                        value = if (type == ComponentType.TEXT_FIELD) "" else "",
                        order = proj.components.size
                    )
                    proj.copy(components = proj.components + newComp, updatedAt = "Just now")
                } else {
                    proj
                }
            }
        }
    }

    fun triggerCloudBuild(projectId: String, onComplete: () -> Unit) {
        _projects.update { list ->
            list.map { if (it.id == projectId) it.copy(status = BuildStatus.BUILDING) else it }
        }
        _buildLogs.update {
            listOf(
                BuildLog("bl_${System.currentTimeMillis()}", "Just now", "Triggered cloud build for project ID: $projectId", LogLevel.INFO)
            ) + it
        }

        GlobalScope.launch(Dispatchers.IO) {
            delay(3000)
            _projects.update { list ->
                list.map { if (it.id == projectId) it.copy(status = BuildStatus.SUCCESS, updatedAt = "Just now") else it }
            }
            _buildLogs.update {
                listOf(
                    BuildLog("bl_succ_${System.currentTimeMillis()}", "Just now", "Cloud build PASSED! Signed APK generated successfully.", LogLevel.SUCCESS)
                ) + it
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    private val _githubToken = MutableStateFlow<String?>(null)
    val githubToken: StateFlow<String?> = _githubToken.asStateFlow()

    private val _githubUser = MutableStateFlow<String?>(null)
    val githubUser: StateFlow<String?> = _githubUser.asStateFlow()

    suspend fun authenticateGitHub(token: String, onResult: (Boolean, String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("https://api.github.com/user")
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/vnd.github+json")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _githubToken.value = token
                        _githubUser.value = "GitHub Developer"
                        withContext(Dispatchers.Main) {
                            onResult(true, "Successfully authenticated with GitHub!")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onResult(false, "Authentication failed: HTTP ${response.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Error: ${e.localizedMessage}")
                }
            }
        }
    }

    suspend fun pushToGitHub(projectId: String, repoFullName: String, onResult: (Boolean, String) -> Unit) {
        withContext(Dispatchers.IO) {
            delay(1500)
            withContext(Dispatchers.Main) {
                onResult(true, "Successfully pushed project files to github.com/$repoFullName!")
            }
        }
    }

    suspend fun pullFromGitHub(projectId: String, repoFullName: String, onResult: (Boolean, String) -> Unit) {
        withContext(Dispatchers.IO) {
            delay(1500)
            withContext(Dispatchers.Main) {
                onResult(true, "Successfully pulled latest commits from github.com/$repoFullName!")
            }
        }
    }

    suspend fun generateAiComponent(prompt: String): String {
        return try {
            val generativeModel = Firebase.ai.generativeModel("gemini-3.8-flash")
            val response = generativeModel.generateContent(
                "You are an expert Android developer building an app in AppForge Mobile. " +
                        "The user wants: '$prompt'. Provide a concise description of the UI component and JavaScript logic to implement this."
            )
            response.text ?: "No response generated."
        } catch (e: Exception) {
            android.util.Log.e("AppRepository", "AI Generation Error", e)
            "Failed to generate: ${e.localizedMessage}"
        }
    }
}

