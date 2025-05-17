package com.earthlyapps.hervault.screens.men

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.earthlyapps.hervault.viewmodels.AuthRepository
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenRegisterScreen(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var partnerCode by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()


    val oceanBlue = Color(0xFF64B5F6)
    val deepPurple = Color(0xFF7E57C2)
    val white = Color.White

//    var showCodeHelp by remember { mutableStateOf(false) }
//    if (showCodeHelp) {
//        AlertDialog(
//            onDismissRequest = { showCodeHelp = false },
//            title = { Text("Partner Code Help") },
//            text = {
//                Text("Your partner can find their 6-digit code in:\n\n" +
//                        "1. Their app profile\n" +
//                        "2. Settings → Partner Access\n\n" +
//                        "Ask them to share it with you!")
//            },
//            confirmButton = {
//                Button(
//                    onClick = { showCodeHelp = false },
//                    colors = ButtonDefaults.buttonColors(containerColor = oceanBlue)
//                ) {
//                    Text("Got it!")
//                }
//            }
//        )
//    }

    Box(modifier = Modifier.fillMaxSize().background(deepPurple).padding(24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "HerVault 💙", style = MaterialTheme.typography.headlineMedium, color = oceanBlue, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Join with partner code", style = MaterialTheme.typography.bodyLarge, color = white)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors(oceanBlue, white)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = textFieldColors(oceanBlue, white)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = textFieldColors(oceanBlue, white)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = partnerCode,
                onValueChange = { partnerCode = it },
                label = { Text("Partner Code (6 digits)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = textFieldColors(oceanBlue, white)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { coroutineScope.launch { authRepository.registerAsMan(name, email, password, partnerCode)}},
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = oceanBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Register with Partner", color = white, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

//            // Helper text
//            Text(text = "Where to find your code?",
//                color = oceanBlue,
//                modifier = Modifier.clickable {showCodeHelp = true}.padding(4.dp),
//                style = MaterialTheme.typography.labelSmall,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Already Registered? Tap here to login",
                color = white, fontSize = 22.sp,
                modifier = Modifier.clickable { navController.navigate("login") })
        }
    }
}

// Reusable text field styling
@Composable
private fun textFieldColors(
    focusedColor: Color,
    unfocusedColor: Color
) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = focusedColor,
    unfocusedBorderColor = unfocusedColor,
    cursorColor = focusedColor,
    focusedLabelColor = focusedColor,
    unfocusedLabelColor = unfocusedColor,
    focusedTextColor = unfocusedColor,
    unfocusedTextColor = unfocusedColor
)
