package com.earthlyapps.hervault.screens.men

import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.earthlyapps.hervault.models.CycleData
import com.earthlyapps.hervault.models.Symptom
import com.earthlyapps.hervault.models.SymptomType
import com.earthlyapps.hervault.viewmodels.menViewmodels.MenViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenDashboardScreen(navController: NavHostController) {
    val menViewModel: MenViewModel = viewModel()
    val partnerSymptoms by menViewModel.partnerSymptoms.collectAsState()
    val partnerCycles by menViewModel.partnerCycles.collectAsState()
    val partnerInfo by menViewModel.partnerInfo.collectAsState()
    val loading by menViewModel.loading.collectAsState()
    val error by menViewModel.error.collectAsState()

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Error, contentDescription = "Error")
                Text(text = error!!)
                Button(onClick = { menViewModel.loadPartnerData() }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    Column (modifier = Modifier.fillMaxSize().background(color = Color(0xFF7C37FF))) {
        LazyColumn (modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 16.dp)) {
            item {
                LazyRow (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    item { IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFE91E63)
                        )
                    } }

                    item { Text(text = "Vault",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color(0xFFE91E63),
                            fontWeight = FontWeight.Bold
                        )
                    ) }

                    item { IconButton(onClick = { /* TODO: Open settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFE91E63)
                        )
                    } }
                }
            }

            item {
                partnerInfo?.let { partner ->
                    Column {
                        Text("Partner: ${partner.name}", color = Color.White)
                        Text("Cycle Sharing: ${partner.shareCycleData}", color = Color.White)
                        Text("Symptom Sharing: ${partner.shareSymptoms}", color = Color.White)
                    }
                }
            }

            if (partnerInfo?.shareSymptoms == true) {
                item {
                    Text("Partner's Symptoms", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    if (partnerSymptoms.isEmpty()) {
                        Text("No symptoms shared yet")
                    }
                }
                items(partnerSymptoms) { symptom ->
                    SymptomRow(symptom)
                }
            }

            if (partnerInfo?.shareCycleData == true) {
                item {
                    Text("Partner's Cycles", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    if (partnerCycles.isEmpty()) {
                        Text("No cycles shared yet")
                    }
                }
                items(partnerCycles) { cycle ->
                    CycleCard(cycle)
                }
            }
        }
    }

}

@Composable
fun SymptomRow(symptom: Symptom) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp)
            .background(color = when (symptom.type) {
                SymptomType.CRAMPS -> Color(0xFFFFCDD2)
                SymptomType.HEADACHE -> Color(0xFFD1C4E9)
                else -> Color(0xFFC8E6C9) },
                shape = CircleShape
            ).padding(8.dp), contentAlignment = Alignment.Center
        ) {
            Text(text = when (symptom.type) {
                SymptomType.CRAMPS -> "🤕"
                SymptomType.HEADACHE -> "🤯"
                SymptomType.MOOD_SWINGS -> "😤"
                else -> "😕"
            },fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = symptom.type.name.replace("_", " ").uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

            Text(text = symptom.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Text(text = "★".repeat(symptom.intensity), color = Color(0xFFFFA000), style = MaterialTheme.typography.bodyLarge
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleCard(cycle: CycleData) {
    Card(modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Started: ${cycle.startDate}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                Text(text = "${cycle.cycleLength} days", color = Color(0xFFE91E63))
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (cycle.startDate.isNotBlank()) {
                CycleProgressIndicator(cycle)
            }

            if (cycle.periodDays.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Period days: ${cycle.periodDays.size}", style = MaterialTheme.typography.bodySmall)
            }

            if (cycle.symptoms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Symptoms logged: ${cycle.symptoms.size}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleProgressIndicator(cycle: CycleData) {
    val currentDate = LocalDate.now()
    val startDate = try {
        LocalDate.parse(cycle.startDate)
    } catch (e: Exception) {
        Log.e("DATE_PARSE", "Error parsing date ${cycle.startDate}", e)
        return
    }

    val cycleDay = ChronoUnit.DAYS.between(startDate, currentDate).toInt() + 1
    val progress = (cycleDay.toFloat() / cycle.cycleLength).coerceIn(0f, 1f)
    val cyclePhase = when {
        cycleDay <= 5 -> "Menstrual"
        cycleDay <= 14 -> "Follicular"
        cycleDay <= 21 -> "Ovulation"
        else -> "Luteal"
    }

    Column {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Color(0xFFE91E63),
            trackColor = Color.LightGray
        )

        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Day $cycleDay/${cycle.cycleLength}", style = MaterialTheme.typography.labelSmall)

            Text(text = cyclePhase, style = MaterialTheme.typography.labelSmall, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
        }
    }
}