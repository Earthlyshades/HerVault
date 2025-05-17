package com.earthlyapps.hervault.screens.ladies

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CycleCalendarScreen(startDate: String, cycleLength: Int = 28, onDayClicked: (String) -> Unit = {}, ) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val start = remember(startDate) {
        runCatching { LocalDate.parse(startDate, dateFormatter) }.getOrDefault(LocalDate.now())
    }
    val today = remember { LocalDate.now() }

    val dates = remember(today) {
        (0..41).map { start.plusDays(it.toLong()) }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = "${start.month} ${start.year}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(dates) { date ->
                val isPeriodDay = remember(date, start, cycleLength) {
                    date.isAfter(start.minusDays(1)) && date.isBefore(start.plusDays(cycleLength.toLong()))
                }

                val isToday = remember(date) { date == today }

                DayCell(date = date.dayOfMonth.toString(),
                    isPeriod = isPeriodDay,
                    isToday = isToday,
                    onClick = { onDayClicked(date.toString()) }
                )
            }
        }
    }
}

@Composable
private fun DayCell(date: String, isPeriod: Boolean, isToday: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f).padding(4.dp).clip(CircleShape)
        .background(
                when {
                    isToday -> Color(0xFFA13D52) // Highlight today
                    isPeriod -> Color(0xFFF8BBD0) // Period days
                    else -> Color.Transparent
                }
            ).clickable(onClick = onClick)) {
        Text(
            text = date,
            color = when {
                isPeriod -> Color.White
                isToday -> Color.Black
                else -> MaterialTheme.colorScheme.onBackground
            },
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}