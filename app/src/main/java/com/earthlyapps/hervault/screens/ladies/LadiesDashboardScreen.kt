package com.earthlyapps.hervault.screens.ladies

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.earthlyapps.hervault.models.Symptom
import com.earthlyapps.hervault.models.SymptomType
import com.earthlyapps.hervault.utilities.SessionManager
import com.earthlyapps.hervault.viewmodels.ladiesViewmodels.CycleViewModel
import com.earthlyapps.hervault.viewmodels.ladiesViewmodels.LadiesDashboardViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadiesDashboardScreen(
    navController: NavHostController
) {
    val dashboardViewModel: LadiesDashboardViewModel = viewModel()
    val cycleViewModel: CycleViewModel = viewModel()
    val sessionManager = SessionManager(navController.context)
    val currentUser = sessionManager.getUser()
    val context = LocalContext.current

    var selectedStartDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var cycleLength by remember { mutableIntStateOf(0) }
    var periodDays by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLogging by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var selectedSymptom by remember { mutableStateOf<SymptomType?>(null) }
    var intensity by remember { mutableIntStateOf(3) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

    val men by dashboardViewModel.menData.collectAsState()
    var showSharingDialog by remember { mutableStateOf(false) }
    var shareCycle by remember { mutableStateOf(true) }
    var shareSymptoms by remember { mutableStateOf(true) }



    LaunchedEffect(Unit) {
        cycleViewModel.loadCycleData()
        dashboardViewModel.currentUser.value?.let { user ->
            shareCycle = user.shareCycleData
            shareSymptoms = user.shareSymptoms
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFDF2F5))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFFE91E63))
            }

            Text(text = "Vault", style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFE91E63), fontWeight = FontWeight.Bold))

            IconButton(onClick = { /* TODO: Open settings */ }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFFE91E63))
            }
        }

        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC1CC))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Welcome, ${currentUser?.name ?: "Queen"}!", style = MaterialTheme.typography.headlineSmall, color = White)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Track your cycle and wellness", style = MaterialTheme.typography.bodyMedium, color = White)
                    }
                }

                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Cycle Tracker", style = MaterialTheme.typography.titleLarge, color = Color(0xFFE91E63))

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(value = selectedStartDate,
                            onValueChange = { selectedStartDate = it },
                            label = { Text("Start Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(value = cycleLength.toString(),
                            onValueChange = { cycleLength = it.toIntOrNull()?: 28 },
                            placeholder = { Text("Cycle Length (days)") },
                            label = { Text("Cycle Length (days)") },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(modifier = Modifier.height(300.dp)) {
                            CycleCalendarScreen(startDate = selectedStartDate, cycleLength = cycleLength, onDayClicked = { date ->
                                    selectedDate = date
                                    Log.d("CALENDAR", "Selected date: $date")
                                }
                            )
                        }

                        Text(text = "Log Symptoms", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))

                        LazyRow(modifier = Modifier.padding(bottom = 8.dp)) {
                            items(SymptomType.entries.toTypedArray()) { symptom ->
                                FilterChip(selected = selectedSymptom == symptom,
                                    onClick = { selectedSymptom = symptom },
                                    label = { Text(symptom.name.replace("_", " ")) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }

                        Text("Intensity: $intensity")
                        Slider(value = intensity.toFloat(),
                            onValueChange = { intensity = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 4,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                if (selectedSymptom != null) {
                                    dashboardViewModel.logSymptom(
                                        date = selectedDate,
                                        type = selectedSymptom!!,
                                        intensity = intensity
                                    )
                                } else {
                                    Toast.makeText(context, "Please select a symptom type", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = selectedSymptom != null, modifier = Modifier.fillMaxWidth())
                        {
                            Text("Log Symptom")
                        }

                        val symptoms by dashboardViewModel.symptoms.collectAsState()

                        if (symptoms.isNotEmpty()) {
                            Text("Symptom History", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

                            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                                items(symptoms) { symptom -> SymptomItem(symptom) }
                            }
                        }

                        Button(
                            onClick = {
                                isLogging = true
                                cycleViewModel.logCycle(
                                    startDate = selectedStartDate,
                                    cycleLength = cycleLength,
                                    periodDays = periodDays
                                )
                                isLogging = false
                                showSuccess = true
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            enabled = !isLogging
                        ) {
                            if (isLogging) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                            } else {
                                Text("Log Cycle Data")
                            }
                        }

                        if (showSuccess) {
                            LaunchedEffect(Unit) {
                                delay(2000)
                                showSuccess = false
                            }

                            Text(text = "Cycle logged successfully!", color = Color.Green, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                        }
                    }
                }

                Card {
                    Row (modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Partner Settings", style = MaterialTheme.typography.titleMedium)

                            men?.let {
                                Text("Linked with: ${it.name}", style = MaterialTheme.typography.bodyMedium)
                            } ?: Text("No partner linked", style = MaterialTheme.typography.bodyMedium)

                        }
                        Button(onClick = { showSharingDialog = true }) {
                            Text("Manage Sharing")
                        }
                    }
                }

                if (showSharingDialog) {
                    AlertDialog(
                        onDismissRequest = { showSharingDialog = false },
                        title = { Text("Share Data with Partner") },
                        text = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = shareCycle,
                                        onCheckedChange = { shareCycle = it }
                                    )
                                    Text("Share Cycle Data")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = shareSymptoms,
                                        onCheckedChange = { shareSymptoms = it }
                                    )
                                    Text("Share Symptoms")
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                dashboardViewModel.updateSharingPreferences(shareCycle, shareSymptoms)
                                showSharingDialog = false
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSharingDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SymptomItem(symptom: Symptom) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(symptom.type.name.replace("_", " "), modifier = Modifier.weight(1f))
            Text(symptom.date, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("Intensity: ${"★".repeat(symptom.intensity)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}