package com.example.sosjibon.ui.vault

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosjibon.data.vault.HealthReading
import com.example.sosjibon.data.vault.Medication
import com.example.sosjibon.data.vault.VaultDocument
import com.example.sosjibon.ui.components.GuestAccessWarningCard
import com.example.sosjibon.ui.settings.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val EmergencyRed = Color(0xFFD92D20)

@Composable
fun VaultScreen(
    onNavigateToAuth: () -> Unit = {},
    vm: VaultViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsState by settingsViewModel.state.collectAsState()
    val isGuest = !settingsState.isLoggedIn || settingsState.profile.fullName == "Guest Member" || settingsState.profile.email.isBlank()

    if (isGuest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            GuestAccessWarningCard(
                pageName = "Health Vault & Records",
                onLoginRegisterClick = onNavigateToAuth
            )
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer
    val backgroundBg = MaterialTheme.colorScheme.background
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = lightGreen, shape = CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = primaryGreen.copy(alpha = 0.35f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Vault",
                    tint = primaryGreen,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Medical Vault & History",
                    color = textDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Encrypted records, vitals history & medications",
                    color = textGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = cardBg,
            contentColor = primaryGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = primaryGreen
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Health Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = primaryGreen,
                unselectedContentColor = textGray
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activity History", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = primaryGreen,
                unselectedContentColor = textGray
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Medications", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                selectedContentColor = primaryGreen,
                unselectedContentColor = textGray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> VaultDocumentsTab(vm = vm)
                1 -> ActivityHistoryTab(vm = vm)
                2 -> MedicationsTab(vm = vm)
            }
        }
    }
}

// ============================================================
// HEALTH DATA VAULT TAB
// ============================================================

@Composable
fun VaultDocumentsTab(vm: VaultViewModel) {
    val list by vm.vault.collectAsState()
    var chosenCategory by remember { mutableStateOf<String?>(null) }
    var chooserOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val primaryGreen = MaterialTheme.colorScheme.primary
    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && chosenCategory != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            vm.addVault(
                chosenCategory!!,
                uri.toString(),
                fileName(context, uri) ?: "Medical document"
            )
            chosenCategory = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = primaryGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Long-Term Health Records",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Store disease pictures, lab reports and PDFs offline on this device.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { chooserOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = primaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = primaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            "Disease Categories",
            color = textDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        listOf("Heart Disease", "Respiratory", "Diabetes", "Other").forEach { category ->
            DiseaseCategoryCard(
                name = category,
                count = list.count { it.disease == category },
                onAdd = {
                    chosenCategory = category
                    picker.launch(arrayOf("image/*", "application/pdf"))
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Stored Records (${list.size})",
            color = textDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        if (list.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No documents added yet. Select a category above to add.",
                        color = textGray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            list.forEach { doc ->
                VaultRow(
                    doc = doc,
                    onDelete = { vm.deleteVault(doc) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (chooserOpen) {
        AlertDialog(
            onDismissRequest = { chooserOpen = false },
            title = { Text("Choose Category", color = textDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Heart Disease", "Respiratory", "Diabetes", "Other").forEach { category ->
                        OutlinedButton(
                            onClick = {
                                chooserOpen = false
                                chosenCategory = category
                                picker.launch(arrayOf("image/*", "application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(category, color = primaryGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { chooserOpen = false }) {
                    Text("Cancel", color = textGray)
                }
            }
        )
    }
}

@Composable
fun DiseaseCategoryCard(
    name: String,
    count: Int,
    onAdd: () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(lightGreen, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = textDark, fontSize = 15.sp)
                Text("$count file(s)", style = MaterialTheme.typography.bodySmall, color = textGray)
            }

            FilledTonalButton(
                onClick = onAdd,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = lightGreen,
                    contentColor = primaryGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VaultRow(
    doc: VaultDocument,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(doc.disease, fontWeight = FontWeight.Bold, color = primaryGreen, fontSize = 13.sp)
                Text(doc.displayName, fontWeight = FontWeight.SemiBold, color = textDark, fontSize = 14.sp)
                Text(formatDate(doc.dateMillis), style = MaterialTheme.typography.labelSmall, color = textGray)
            }

            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            Uri.parse(doc.uri),
                            if (doc.displayName.lowercase(Locale.ROOT).endsWith(".pdf")) "application/pdf" else "image/*"
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open", tint = primaryGreen)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed)
            }
        }
    }
}

private fun fileName(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = cursor.getString(index)
            }
        }
    }
    return name ?: uri.lastPathSegment
}

// ============================================================
// ACTIVITY HISTORY TAB
// ============================================================

@Composable
fun ActivityHistoryTab(vm: VaultViewModel) {
    val readings by vm.readings.collectAsState()
    var subTab by remember { mutableIntStateOf(0) }
    var activeDialogType by remember { mutableStateOf<String?>(null) }

    val primaryGreen = MaterialTheme.colorScheme.primary
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = cardBg,
            contentColor = primaryGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]),
                    color = primaryGreen
                )
            }
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Standard Reference", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = primaryGreen,
                unselectedContentColor = textGray
            )

            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("My Vitals & Trends", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                selectedContentColor = primaryGreen,
                unselectedContentColor = textGray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (subTab == 0) {
            StandardTabContent { type -> activeDialogType = type }
        } else {
            MyStandardTabContent(
                readings = readings,
                onAddReading = { type -> activeDialogType = type },
                onDeleteReading = vm::deleteReading
            )
        }
    }

    activeDialogType?.let { type ->
        if (type.startsWith("Standard:")) {
            StandardInfoDialog(
                metric = type.removePrefix("Standard:"),
                onDismiss = { activeDialogType = null }
            )
        } else {
            AddReadingDialog(
                type = type,
                onCancel = { activeDialogType = null },
                onSave = { value ->
                    vm.addReading(type, value)
                    activeDialogType = null
                }
            )
        }
    }
}

@Composable
fun StandardTabContent(onOpenStandard: (String) -> Unit) {
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Global Adult Healthy Reference Ranges",
            color = textGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        MetricCard(
            title = "Blood Sugar – Fasting",
            value = "70–99 mg/dL",
            sub = "Adult Healthy Range",
            icon = Icons.Default.HealthAndSafety,
            onClick = { onOpenStandard("Standard:Blood Sugar – Fasting") }
        )

        MetricCard(
            title = "Blood Sugar – 2 hrs after meal",
            value = "<140 mg/dL",
            sub = "Adult Healthy Range",
            icon = Icons.Default.HealthAndSafety,
            onClick = { onOpenStandard("Standard:Blood Sugar – 2 hrs after meal") }
        )

        MetricCard(
            title = "Blood Pressure – Systolic",
            value = "<120 mmHg",
            sub = "Adult Healthy Range",
            icon = Icons.Default.Favorite,
            onClick = { onOpenStandard("Standard:Blood Pressure – Systolic") }
        )

        MetricCard(
            title = "Blood Pressure – Diastolic",
            value = "<80 mmHg",
            sub = "Adult Healthy Range",
            icon = Icons.Default.Favorite,
            onClick = { onOpenStandard("Standard:Blood Pressure – Diastolic") }
        )

        MetricCard(
            title = "Resting Heart Rate",
            value = "60–100 BPM",
            sub = "Adult Healthy Range",
            icon = Icons.Default.Favorite,
            onClick = { onOpenStandard("Standard:Resting Heart Rate") }
        )

        MetricCard(
            title = "Weight",
            value = "Depends on BMI & height",
            sub = "Adult Healthy Range",
            icon = Icons.Default.MonitorWeight,
            onClick = { onOpenStandard("Standard:Weight") }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(lightGreen, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = textDark, fontSize = 14.sp)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = primaryGreen)
                Text(sub, fontSize = 11.sp, color = textGray)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = textGray)
        }
    }
}

@Composable
fun StandardInfoDialog(metric: String, onDismiss: () -> Unit) {
    val textDark = MaterialTheme.colorScheme.onBackground
    val primaryGreen = MaterialTheme.colorScheme.primary

    val message = when (metric) {
        "Blood Sugar – Fasting" -> "Male Adult: Same\nFemale Adult: Same\nHealthy range: 70–99 mg/dL"
        "Blood Sugar – 2 hrs after meal" -> "Male Adult: Same\nFemale Adult: Same\nHealthy range: <140 mg/dL"
        "Blood Pressure – Systolic" -> "Male Adult: Same\nFemale Adult: Same\nHealthy range: <120 mmHg"
        "Blood Pressure – Diastolic" -> "Male Adult: Same\nFemale Adult: Same\nHealthy range: <80 mmHg"
        "Resting Heart Rate" -> "Male Adult: Same\nFemale Adult: Same\nHealthy range: 60–100 BPM"
        "Weight" -> "Male Adult: Depends on height\nFemale Adult: Depends on height\nHealthy range: No single standard weight"
        else -> ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(metric, color = textDark, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = textDark) },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("OK", color = Color.White)
            }
        }
    )
}

@Composable
fun MyStandardTabContent(
    readings: List<HealthReading>,
    onAddReading: (String) -> Unit,
    onDeleteReading: (HealthReading) -> Unit
) {
    val types = listOf(
        "Blood Sugar – Fasting",
        "Blood Sugar – 2 hrs after meal",
        "Blood Pressure – Systolic",
        "Blood Pressure – Diastolic",
        "Resting Heart Rate",
        "Weight"
    )

    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary
    val cardBg = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Log your health metrics. When 2 or more readings are recorded, a trend graph will appear.",
            color = textGray,
            fontSize = 12.sp
        )

        types.forEach { type ->
            val data = readings
                .filter { it.type == type }
                .sortedBy { it.dateMillis }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(type, fontWeight = FontWeight.Bold, color = textDark, fontSize = 14.sp)
                            Text(
                                data.lastOrNull()?.value?.let { "Latest: $it" } ?: "No data logged",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (data.isNotEmpty()) primaryGreen else textGray
                            )
                        }

                        Button(
                            onClick = { onAddReading(type) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp)
                        }
                    }

                    if (data.size >= 2) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TrendGraph(
                            data = data,
                            unit = trendUnit(type)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "All Logged Readings (${readings.size})",
            color = textDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        readings
            .sortedByDescending { it.dateMillis }
            .take(50)
            .forEach { r ->
                ReadingRow(r = r, onDelete = onDeleteReading)
            }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TrendGraph(
    data: List<HealthReading>,
    unit: String
) {
    val points = data.mapNotNull { r ->
        r.value
            .replace(",", "")
            .trim()
            .toDoubleOrNull()
            ?.let { r.dateMillis to it }
    }

    if (points.size < 2) return

    val minValue = points.minOf { it.second }
    val maxValue = points.maxOf { it.second }
    val range = max(1.0, maxValue - minValue)

    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Trend Graph", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = textDark)
            Text("${points.size} readings • $unit", style = MaterialTheme.typography.labelSmall, color = primaryGreen)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 4.dp)
        ) {
            val left = 12f
            val right = size.width - 12f
            val top = 12f
            val bottom = size.height - 12f

            val width = right - left
            val height = bottom - top

            for (i in 0..4) {
                val y = top + height * i / 4f
                drawLine(
                    color = Color.Gray.copy(alpha = 0.35f),
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1f
                )
            }

            val path = Path()

            points.forEachIndexed { index, point ->
                val x = left + index.toFloat() / (points.size - 1) * width
                val normalized = ((point.second - minValue) / range).toFloat()
                val y = bottom - normalized * height

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = primaryGreen,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            points.forEachIndexed { index, point ->
                val x = left + index.toFloat() / (points.size - 1) * width
                val normalized = ((point.second - minValue) / range).toFloat()
                val y = bottom - normalized * height

                drawCircle(
                    color = primaryGreen,
                    radius = 5f,
                    center = Offset(x, y)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDate(points.first().first), style = MaterialTheme.typography.labelSmall, color = textGray)
            Text(formatDate(points.last().first), style = MaterialTheme.typography.labelSmall, color = textGray)
        }
    }
}

@Composable
fun ReadingRow(
    r: HealthReading,
    onDelete: (HealthReading) -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(r.type, fontWeight = FontWeight.SemiBold, color = textDark, fontSize = 13.sp)
                Text("${r.value} • ${formatDate(r.dateMillis)}", style = MaterialTheme.typography.bodySmall, color = textGray)
            }

            IconButton(onClick = { onDelete(r) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed)
            }
        }
    }
}

@Composable
fun AddReadingDialog(
    type: String,
    onCancel: () -> Unit,
    onSave: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }

    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary

    val label = when (type) {
        "Blood Sugar – Fasting", "Blood Sugar – 2 hrs after meal" -> "Value in mg/dL"
        "Blood Pressure – Systolic", "Blood Pressure – Diastolic" -> "Value in mmHg"
        "Resting Heart Rate" -> "Value in BPM"
        "Weight" -> "Value in kg"
        else -> "Value"
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Add Reading", color = textDark, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(type, color = primaryGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(label) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        focusedLabelColor = primaryGreen
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel", color = textGray) }
        },
        confirmButton = {
            Button(
                enabled = value.isNotBlank(),
                onClick = { onSave(value.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("Save", color = Color.White)
            }
        }
    )
}

// ============================================================
// MEDICATIONS TAB
// ============================================================

@Composable
fun MedicationsTab(vm: VaultViewModel) {
    val meds by vm.medications.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val primaryGreen = MaterialTheme.colorScheme.primary
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = primaryGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Daily & Required Medications",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Track your daily dosage, timings and required prescription notes.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = primaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = primaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (meds.isEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No medication added yet. Tap Add above to record.",
                        color = textGray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            meds.forEach { med ->
                MedicationRow(
                    m = med,
                    onDelete = { vm.deleteMedication(med) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showAddDialog) {
        AddMedicationDialog(
            onCancel = { showAddDialog = false },
            onSave = { name, dose, timing, required, note ->
                vm.addMedication(name, dose, timing, required, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MedicationRow(
    m: Medication,
    onDelete: () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary
    val lightGreen = MaterialTheme.colorScheme.primaryContainer

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(lightGreen, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Medication, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(m.name, fontWeight = FontWeight.Bold, color = textDark, fontSize = 15.sp)
                Text("${m.dose} • ${m.timing}", style = MaterialTheme.typography.bodySmall, color = textGray)

                if (m.required) {
                    Text(
                        "Required Medication",
                        color = primaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                if (m.note.isNotBlank()) {
                    Text(m.note, style = MaterialTheme.typography.bodySmall, color = textGray)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed)
            }
        }
    }
}

@Composable
fun AddMedicationDialog(
    onCancel: () -> Unit,
    onSave: (String, String, String, Boolean, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var timing by remember { mutableStateOf("Daily - Morning") }
    var required by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    val textDark = MaterialTheme.colorScheme.onBackground
    val textGray = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryGreen = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Add Medication", color = textDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        focusedLabelColor = primaryGreen
                    )
                )

                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("Dose (e.g. 500mg, 1 tablet)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        focusedLabelColor = primaryGreen
                    )
                )

                OutlinedTextField(
                    value = timing,
                    onValueChange = { timing = it },
                    label = { Text("Timing (e.g. Morning / Night)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        focusedLabelColor = primaryGreen
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = required,
                        onCheckedChange = { required = it },
                        colors = CheckboxDefaults.colors(checkedColor = primaryGreen)
                    )
                    Text("Required Daily Medicine", fontSize = 13.sp, color = textDark)
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Prescription details") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryGreen,
                        focusedLabelColor = primaryGreen
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel", color = textGray) }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), dose.trim(), timing.trim(), required, note.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text("Save", color = Color.White)
            }
        }
    )
}

private fun trendUnit(type: String): String = when (type) {
    "Blood Sugar – Fasting", "Blood Sugar – 2 hrs after meal" -> "mg/dL"
    "Blood Pressure – Systolic", "Blood Pressure – Diastolic" -> "mmHg"
    "Resting Heart Rate" -> "BPM"
    "Weight" -> "kg"
    else -> ""
}

private fun formatDate(ms: Long): String {
    if (ms <= 0) return ""
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(ms))
}
