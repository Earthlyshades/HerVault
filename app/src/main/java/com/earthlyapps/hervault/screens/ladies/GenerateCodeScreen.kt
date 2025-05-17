package com.earthlyapps.hervault.screens.ladies

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.earthlyapps.hervault.viewmodels.AuthRepository

@Composable
fun GenerateCodeScreen(navController: NavHostController, authRepository: AuthRepository) {
    val context = LocalContext.current
    var partnerCode by remember { mutableStateOf("") }

    val softPink = Color(0xFFFFC1CC)
    val darkPink = Color(0xFFB00F46)

    Box(modifier = Modifier.fillMaxSize().background(softPink)) {
        Column(modifier = Modifier.padding(24.dp).align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Partner Code", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = partnerCode.ifEmpty { "Tap Generate New Code Button" }, style = MaterialTheme.typography.displayLarge.copy(color = darkPink))

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { partnerCode = authRepository.generateAndSavePartnerCode() },
                colors = ButtonDefaults.buttonColors(containerColor = darkPink)
            ) {
                Text("Generate New Code")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("ladies-dashboard") }) {
                Text("Continue to Dashboard")
            }

            TextButton(onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Use code $partnerCode to join me on HerVault!")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Code"))
            }) {
                Text("Share Code", color = darkPink)
            }
        }
    }
}