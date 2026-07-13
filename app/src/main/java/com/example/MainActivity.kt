package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AppViewModel
import com.example.ui.DashboardScreen
import com.example.ui.LockScreen
import com.example.ui.NoteDetailScreen
import com.example.ui.PasswordDetailScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()
    private var shakeListener: android.hardware.SensorEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val isLocked by viewModel.isLocked.collectAsState()
                val preventScreenshot by viewModel.preventScreenshot.collectAsState(false)
                val navController = rememberNavController()

                // Security Shield: Prevent screenshot & recents thumbnail if policy enabled
                LaunchedEffect(preventScreenshot) {
                    if (preventScreenshot) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                // App Lock: Redirect immediately on lock trigger
                LaunchedEffect(isLocked) {
                    if (isLocked) {
                        navController.navigate("lock") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = if (isLocked) "lock" else "dashboard",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("lock") {
                        LockScreen(
                            viewModel = viewModel,
                            onUnlocked = {
                                navController.navigate("dashboard") {
                                    popUpTo("lock") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToNote = { id ->
                                navController.navigate("note/$id")
                            },
                            onNavigateToPassword = { id ->
                                navController.navigate("password/$id")
                            }
                        )
                    }

                    composable(
                        route = "note/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                        NoteDetailScreen(
                            noteId = noteId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "password/{passwordId}",
                        arguments = listOf(navArgument("passwordId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val passwordId = backStackEntry.arguments?.getInt("passwordId") ?: 0
                        PasswordDetailScreen(
                            passwordId = passwordId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register sensor for physical Shake panic triggers
        shakeListener = viewModel.registerShakePanic {
            // Panic triggered: App locks immediately and updates compose stream
        }
    }

    override fun onPause() {
        super.onPause()
        shakeListener?.let {
            viewModel.unregisterShakePanic(it)
        }
    }
}
