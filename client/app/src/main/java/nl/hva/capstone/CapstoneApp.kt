package nl.hva.capstone

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.hva.capstone.ui.screens.AddUserScreen
import nl.hva.capstone.ui.screens.conversation.ConversationScreen
import nl.hva.capstone.ui.screens.home.HomeScreen
import nl.hva.capstone.ui.screens.LoginScreen
import nl.hva.capstone.ui.screens.SignupScreen
import nl.hva.capstone.viewmodel.SessionState
import nl.hva.capstone.viewmodel.SessionViewModel

@Composable
fun CapstoneApp(sessionVM: SessionViewModel) {
  val navController = rememberNavController()
  val state by sessionVM.state.observeAsState()

  if (state == SessionState.INITIALISING) return

  NavHost(
    navController,
    startDestination = if (state == SessionState.READY) "/conversations" else "/login",
    modifier = Modifier.fillMaxSize().safeDrawingPadding()
  ) {
    composable("/login") {
      LoginScreen(navController, sessionVM)
    }

    composable("/signup") {
      SignupScreen(navController, sessionVM)
    }

    composable("/conversations") {
      HomeScreen(navController, sessionVM)
    }

    composable("/conversations/add") {
      AddUserScreen(navController, sessionVM)
    }

    composable(
      route = "/conversations/{id}",
      arguments = listOf(
        navArgument("id") {
          type = NavType.IntType
        }
      )
    ) { entry ->
      val id = entry.arguments?.getInt("id")
      ConversationScreen(
        navController,
        conversationID = id!!,
        sessionVM.conversationsVM
      )
    }
  }
}