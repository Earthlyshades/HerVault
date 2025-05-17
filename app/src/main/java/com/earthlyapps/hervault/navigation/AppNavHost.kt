package com.earthlyapps.hervault.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earthlyapps.hervault.screens.WelcomeScreen
import com.earthlyapps.hervault.screens.auth.LoginScreen
import com.earthlyapps.hervault.screens.ladies.GenerateCodeScreen
import com.earthlyapps.hervault.screens.ladies.LadiesDashboardScreen
import com.earthlyapps.hervault.screens.ladies.LadiesRegisterScreen
import com.earthlyapps.hervault.screens.men.MenDashboardScreen
import com.earthlyapps.hervault.screens.men.MenRegisterScreen
import com.earthlyapps.hervault.viewmodels.AuthRepository


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
    startDestination: String = "welcome"
){
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(navHostController,context) }

    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(WELCOME_SCREEN) { WelcomeScreen(navHostController) }

        composable(LOGIN_SCREEN) { LoginScreen(navHostController, authRepository = authRepository) }


        composable(LADIES_REGISTER_SCREEN) { LadiesRegisterScreen(navHostController, authRepository = authRepository) }
        composable(LADIES_DASHBOARD_SCREEN) { LadiesDashboardScreen(navHostController) }

        composable(GENERATE_CODE) { GenerateCodeScreen(navHostController, authRepository = authRepository) }

        composable(MEN_REGISTER_SCREEN) { MenRegisterScreen(navHostController, authRepository = authRepository) }
        composable(MEN_DASHBOARD_SCREEN) { MenDashboardScreen(navHostController) }

    }
}
