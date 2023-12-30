package nl.hva.chatstone.ui.windows

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.hva.chatstone.ui.screens.AddUserScreen
import nl.hva.chatstone.ui.screens.conversation.ConversationScreen
import nl.hva.chatstone.ui.screens.home.HomeScreen
import nl.hva.chatstone.ui.screens.LoginScreen
import nl.hva.chatstone.ui.screens.SignupScreen
import nl.hva.chatstone.viewmodel.SessionState
import nl.hva.chatstone.viewmodel.SessionViewModel

@Composable
fun ChatstoneAppWindow(sessionVM: SessionViewModel) {
  val navController = rememberNavController()
  val conversationsVM = sessionVM.conversationsVM

  var targetURL by sessionVM.targetURL

  LaunchedEffect(Unit) {
    sessionVM.listenForEvents()
    if (targetURL != null) {
      navController.navigate(targetURL!!)
      targetURL = null
    }
  }

  val state by sessionVM.state.observeAsState()

  val startDestination = when (state) {
    SessionState.INITIALISING -> return
    SessionState.READY -> "/conversations"
    else -> "/login"
  }

  NavHost(
    navController,
    startDestination = startDestination,
    modifier = Modifier.fillMaxSize().safeDrawingPadding()
  ) {
    composable("/login") {
      LoginScreen(navController, sessionVM)
    }

    composable("/signup") {
      SignupScreen(navController, sessionVM)
    }

    composable("/conversations") {
      HomeScreen(navController, conversationsVM)
    }

    composable("/conversations/add") {
      AddUserScreen(navController, conversationsVM)
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
        conversationsVM
      )
    }
  }
}