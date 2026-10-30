package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ComponentType
import com.example.data.Project
import com.example.data.UIComponent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: Project,
    onBack: () -> Unit,
    onUpdateComponents: (List<UIComponent>) -> Unit,
    onAddComponent: (ComponentType, String) -> Unit,
    onGenerateAi: suspend (String) -> String
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Preview, 1: Library, 2: Inspector, 3: JS Logic, 4: AI Assist
    var selectedComponentId by remember { mutableStateOf<String?>(project.components.firstOrNull()?.id) }
    var showAddDialog by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }
    var aiResult by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var consoleLogs by remember { mutableStateOf(listOf("[System] JavaScript engine initialized successfully.")) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedComponent = project.components.find { it.id == selectedComponentId } ?: project.components.firstOrNull()

    fun logToConsole(message: String) {
        consoleLogs = listOf("[${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}] $message") + consoleLogs
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Visual UI Builder & JS Logic Studio", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("add_component_button")) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Component", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                    label = { Text("Live Preview") },
                    modifier = Modifier.testTag("nav_preview")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Widgets, contentDescription = null) },
                    label = { Text("Components") },
                    modifier = Modifier.testTag("nav_components")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                    label = { Text("Inspector") },
                    modifier = Modifier.testTag("nav_inspector")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Code, contentDescription = null) },
                    label = { Text("JS Logic") },
                    modifier = Modifier.testTag("nav_js_logic")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("AI Assist") },
                    modifier = Modifier.testTag("nav_ai")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> LivePreviewTab(
                    project = project,
                    onLog = { msg -> logToConsole(msg) },
                    onToast = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                )
                1 -> ComponentLibraryTab(
                    components = project.components,
                    selectedId = selectedComponentId,
                    onSelect = { selectedComponentId = it },
                    onDelete = { id ->
                        val updated = project.components.filter { it.id != id }
                        onUpdateComponents(updated)
                    }
                )
                2 -> InspectorTab(
                    component = selectedComponent,
                    onUpdate = { updatedComp ->
                        val updatedList = project.components.map { if (it.id == updatedComp.id) updatedComp else it }
                        onUpdateComponents(updatedList)
                    }
                )
                3 -> AdvancedJsLogicTab(
                    component = selectedComponent,
                    consoleLogs = consoleLogs,
                    onUpdate = { updatedComp ->
                        val updatedList = project.components.map { if (it.id == updatedComp.id) updatedComp else it }
                        onUpdateComponents(updatedList)
                    },
                    onClearConsole = { consoleLogs = emptyList() },
                    onTestRun = { snippet ->
                        logToConsole("Executing JS: $snippet")
                        Toast.makeText(context, "JS Executed Successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
                4 -> AiAssistantTab(
                    prompt = aiPrompt,
                    onPromptChange = { aiPrompt = it },
                    result = aiResult,
                    isGenerating = isGenerating,
                    onGenerate = {
                        isGenerating = true
                        coroutineScope.launch {
                            val res = onGenerateAi(aiPrompt)
                            aiResult = res
                            isGenerating = false
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddComponentLibraryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { type, label ->
                onAddComponent(type, label)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LivePreviewTab(project: Project, onLog: (String) -> Unit, onToast: (String) -> Unit) {
    var selectedDevice by remember { mutableStateOf("Phone") } // Phone, Foldable, Tablet
    var isLandscape by remember { mutableStateOf(false) }

    val (width, height) = when (selectedDevice) {
        "Phone" -> if (isLandscape) 560.dp to 340.dp else 320.dp to 640.dp
        "Foldable" -> if (isLandscape) 600.dp to 380.dp else 380.dp to 600.dp
        "Tablet" -> if (isLandscape) 680.dp to 420.dp else 420.dp to 680.dp
        else -> 320.dp to 640.dp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Virtual Emulator Shell Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Phone", "Foldable", "Tablet").forEach { device ->
                        FilterChip(
                            selected = selectedDevice == device,
                            onClick = { selectedDevice = device },
                            label = { Text(device, fontSize = 11.sp) }
                        )
                    }
                }
                IconButton(onClick = { isLandscape = !isLandscape }) {
                    Icon(
                        imageVector = if (isLandscape) Icons.Default.PhoneAndroid else Icons.Default.ScreenRotation,
                        contentDescription = "Toggle Orientation",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Virtual Emulator Frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Status Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("9:41", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Text("5G", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Render dynamic component library
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(project.components) { comp ->
                            when (comp.type) {
                                ComponentType.TEXT -> {
                                    Text(
                                        text = comp.label,
                                        fontSize = comp.textSize.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (comp.value.isNotBlank()) {
                                        Text(text = comp.value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                ComponentType.BUTTON -> {
                                    Button(
                                        onClick = {
                                            onLog("Clicked button [${comp.label}]")
                                            onToast("Triggered: ${comp.label}")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(comp.label)
                                    }
                                }
                                ComponentType.TEXT_FIELD -> {
                                    OutlinedTextField(
                                        value = comp.value,
                                        onValueChange = {},
                                        label = { Text(comp.label) },
                                        placeholder = { Text(comp.hint) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                ComponentType.CHECKBOX -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(checked = comp.isChecked, onCheckedChange = { onLog("Checkbox [${comp.label}] toggled: $it") })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(comp.label, fontSize = 14.sp)
                                    }
                                }
                                ComponentType.RADIO_BUTTON -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = comp.isChecked, onClick = { onLog("Radio [${comp.label}] selected") })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(comp.label, fontSize = 14.sp)
                                    }
                                }
                                ComponentType.SWITCH -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(comp.label, fontSize = 14.sp)
                                        Switch(checked = comp.isChecked, onCheckedChange = { onLog("Switch [${comp.label}] changed: $it") })
                                    }
                                }
                                ComponentType.SLIDER -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("${comp.label}: ${comp.sliderValue.toInt()}", fontSize = 12.sp)
                                        Slider(value = comp.sliderValue, onValueChange = { onLog("Slider [${comp.label}] value: $it") }, valueRange = 0f..100f)
                                    }
                                }
                                ComponentType.CARD -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = comp.label, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = comp.value, fontSize = 13.sp)
                                        }
                                    }
                                }
                                ComponentType.LIST -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = "List View: ${comp.label}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Text(text = "• Item 1\n• Item 2\n• Item 3", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }
                                ComponentType.IMAGE_VIEW -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text(comp.label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                else -> {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Text(text = comp.label, modifier = Modifier.padding(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentLibraryTab(
    components: List<UIComponent>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(components) { comp ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (comp.id == selectedId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(comp.id) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (comp.type) {
                                ComponentType.BUTTON -> Icons.Default.SmartButton
                                ComponentType.TEXT -> Icons.Default.TextFields
                                ComponentType.TEXT_FIELD -> Icons.Default.Input
                                ComponentType.CHECKBOX -> Icons.Default.CheckBox
                                ComponentType.RADIO_BUTTON -> Icons.Default.RadioButtonChecked
                                ComponentType.SWITCH -> Icons.Default.ToggleOn
                                ComponentType.SLIDER -> Icons.Default.Tune
                                ComponentType.CARD -> Icons.Default.Dashboard
                                ComponentType.LIST -> Icons.Default.List
                                ComponentType.IMAGE_VIEW -> Icons.Default.Image
                                else -> Icons.Default.Widgets
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = comp.label, fontWeight = FontWeight.Bold)
                            Text(text = "Type: ${comp.type}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { onDelete(comp.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun InspectorTab(component: UIComponent?, onUpdate: (UIComponent) -> Unit) {
    if (component == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a component from Components tab to inspect and customize.")
        }
        return
    }

    var label by remember(component.id) { mutableStateOf(component.label) }
    var value by remember(component.id) { mutableStateOf(component.value) }
    var hint by remember(component.id) { mutableStateOf(component.hint) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Component Property Inspector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = label,
            onValueChange = {
                label = it
                onUpdate(component.copy(label = it))
            },
            label = { Text("Component Label / Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                onUpdate(component.copy(value = it))
            },
            label = { Text("Default Value / Content") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = hint,
            onValueChange = {
                hint = it
                onUpdate(component.copy(hint = it))
            },
            label = { Text("Input Placeholder / Hint") },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Component Type: ${component.type}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AdvancedJsLogicTab(
    component: UIComponent?,
    consoleLogs: List<String>,
    onUpdate: (UIComponent) -> Unit,
    onClearConsole: () -> Unit,
    onTestRun: (String) -> Unit
) {
    if (component == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a component to edit custom JavaScript logic.")
        }
        return
    }

    var jsCode by remember(component.id) { mutableStateOf(component.jsLogic) }
    var syntaxError by remember { mutableStateOf<String?>(null) }

    fun validateSyntax(code: String) {
        syntaxError = if (!code.contains("function") && !code.contains("=>")) {
            "Warning: Missing function declaration (e.g., function onClick() { ... })"
        } else {
            null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Advanced JavaScript Logic Editor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Button(onClick = {
                validateSyntax(jsCode)
                if (syntaxError == null) onTestRun(jsCode)
            }) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run Test")
            }
        }

        // IntelliSense / Snippets Bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(onClick = { jsCode += "\n\ntoast('Hello World!');" }, label = { Text("toast()") })
            AssistChip(onClick = { jsCode += "\n\nconsole.log('Event fired');" }, label = { Text("console.log()") })
            AssistChip(onClick = { jsCode += "\n\nif (state.active) { ... }" }, label = { Text("if/else") })
        }

        OutlinedTextField(
            value = jsCode,
            onValueChange = {
                jsCode = it
                validateSyntax(it)
                onUpdate(component.copy(jsLogic = it))
            },
            label = { Text("JavaScript Snippet (IntelliSense Active)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        if (syntaxError != null) {
            Text(text = syntaxError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        // Debugger Console Drawer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("JS Debugger & Console", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            TextButton(onClick = onClearConsole) {
                Text("Clear", fontSize = 12.sp)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(consoleLogs) { log ->
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF81C995)
                    )
                }
            }
        }
    }
}

@Composable
fun AiAssistantTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    result: String,
    isGenerating: Boolean,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gemini AI App Builder Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            label = { Text("Describe UI screen or JS logic you want...") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
        Button(
            onClick = onGenerate,
            enabled = !isGenerating && prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating with Gemini...")
            } else {
                Text("Generate with Gemini AI")
            }
        }

        if (result.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AI Output:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = result, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun AddComponentLibraryDialog(onDismiss: () -> Unit, onAdd: (ComponentType, String) -> Unit) {
    var label by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ComponentType.BUTTON) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add UI Component from Library") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Component Name / Label") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Select Component Type:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(ComponentType.values()) { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { type = t }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = type == t, onClick = { type = t })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(t.name, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (label.isNotBlank()) onAdd(type, label) }
            ) {
                Text("Add to App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
