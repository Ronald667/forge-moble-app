package com.example.data

data class Project(
    val id: String,
    val name: String,
    val packageName: String,
    val description: String,
    val category: String,
    val version: String = "1.0.0",
    val status: BuildStatus = BuildStatus.SUCCESS,
    val updatedAt: String = "Just now",
    val components: List<UIComponent> = emptyList(),
    val dependencies: List<String> = listOf("androidx.core:core-ktx", "androidx.compose.material3", "io.coil-kt:coil-compose"),
    val commits: List<Commit> = emptyList(),
    val collaborators: List<Collaborator> = emptyList()
)

enum class BuildStatus {
    SUCCESS, BUILDING, FAILED, QUEUED
}

enum class ComponentType {
    BUTTON, TEXT, TEXT_FIELD, CHECKBOX, RADIO_BUTTON, LIST, IMAGE_VIEW, SWITCH, CARD, SLIDER, SPINNER
}

data class UIComponent(
    val id: String,
    val type: ComponentType,
    val label: String,
    val value: String = "",
    val hint: String = "Enter value...",
    val colorHex: String = "#6750A4",
    val textColorHex: String = "#1C1B1F",
    val textSize: Int = 16,
    val isChecked: Boolean = false,
    val sliderValue: Float = 50f,
    val jsLogic: String = "function run(context) {\n  console.log('Event triggered on " + "component');\n  toast('Action executed successfully!');\n}",
    val order: Int = 0
)

data class Commit(
    val id: String,
    val message: String,
    val author: String,
    val timestamp: String,
    val branch: String = "main"
)

data class Collaborator(
    val id: String,
    val name: String,
    val role: String,
    val email: String
)

data class BuildLog(
    val id: String,
    val timestamp: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

enum class LogLevel {
    INFO, SUCCESS, WARN, ERROR
}

data class CrashReport(
    val id: String,
    val timestamp: String,
    val exception: String,
    val message: String,
    val stackTrace: String,
    val device: String,
    val resolved: Boolean = false
)

data class AnalyticsData(
    val dailyActiveUsers: Int,
    val screenViews: Int,
    val crashFreeRate: Float,
    val avgSessionDuration: String
)
