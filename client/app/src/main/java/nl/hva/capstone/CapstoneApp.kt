package nl.hva.capstone

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nl.hva.capstone.ui.screens.AddUserScreen
import nl.hva.capstone.ui.screens.HomeScreen
import nl.hva.capstone.ui.screens.LoginScreen
import nl.hva.capstone.ui.screens.SignupScreen
import nl.hva.capstone.viewmodel.SessionState
import nl.hva.capstone.viewmodel.SessionViewModel

@Composable
fun CapstoneApp(sessionViewModel: SessionViewModel) {
  val navController = rememberNavController()
  val state by sessionViewModel.state.observeAsState()

  if (state == SessionState.INITIALISING) return

  NavHost(
    navController,
    startDestination = if (state == SessionState.READY) "/home" else "/login",
    modifier = Modifier.fillMaxSize()
  ) {
    composable("/login") {
      LoginScreen(navController, sessionViewModel)
    }

    composable("/signup") {
      SignupScreen(navController, sessionViewModel)
    }

    composable("/home") {
      HomeScreen(navController, sessionViewModel)
    }

    composable("/home/add-user") {
      AddUserScreen(navController, sessionViewModel)
    }
  }
}