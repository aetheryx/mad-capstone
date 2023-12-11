package nl.hva.capstone

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nl.hva.capstone.ui.screens.HomeScreen
import nl.hva.capstone.ui.screens.LoadingScreen
import nl.hva.capstone.ui.screens.LoginScreen
import nl.hva.capstone.viewmodels.SessionViewModel

@Composable
fun CapstoneApp(sessionViewModel: SessionViewModel) {
  val navController = rememberNavController()

  NavHost(
    navController,
    startDestination = "/login",
    modifier = Modifier.fillMaxSize()
  ) {
    composable("/login") {
      LoginScreen(navController, sessionViewModel)
    }

    composable("/home") {
      HomeScreen(sessionViewModel)
    }
  }
}