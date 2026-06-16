package com.ms.bluetoothchatapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ms.bluetoothchatapp.common.PermissionManager
import com.ms.bluetoothchatapp.presentation.screens.ChatScreen
import com.ms.bluetoothchatapp.presentation.screens.ChatViewModel
import com.ms.bluetoothchatapp.presentation.screens.SearchBluetoothDevices
import com.ms.bluetoothchatapp.presentation.screens.SearchViewModel
import com.ms.bluetoothchatapp.ui.theme.BluetoothChatAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothChatAppTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissionsMap ->
                    val isGranted = permissionsMap.values.all { it }
                    if (!isGranted) {
                        // Handle permission denied
                    }
                }

                LaunchedEffect(Unit) {
                    if (!permissionManager.hasAllBluetoothPermissions()) {
                        permissionLauncher.launch(permissionManager.getRequiredBluetoothPermissions())
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = NavDestination.Search,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<NavDestination.Search> {
                            val viewModel = hiltViewModel<SearchViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(state.isConnected) {
                                if (state.isConnected) {
                                    navController.navigate(NavDestination.Chat)
                                }
                            }

                            SearchBluetoothDevices(
                                isDiscovering = state.isDiscovering,
                                isConnecting = state.isConnecting,
                                pairedDevices = state.pairedDevices,
                                newDevices = state.scannedDevices,
                                onStartDiscovery = viewModel::startDiscovery,
                                onStopDiscovery = viewModel::stopDiscovery,
                                onStartServer = viewModel::startServer,
                                onDeviceClick = viewModel::connectToDevice
                            )
                        }
                        composable<NavDestination.Chat> {
                            val viewModel = hiltViewModel<ChatViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(state.isConnected) {
                                if (!state.isConnected) {
                                    navController.popBackStack()
                                }
                            }

                            ChatScreen(
                                state = state,
                                onDisconnect = viewModel::disconnect,
                                onSendMessage = viewModel::sendMessage,
                                onMessageChange = viewModel::onMessageChange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Serializable
sealed class NavDestination {
    @Serializable
    data object Search : NavDestination()

    @Serializable
    data object Chat : NavDestination()
}
