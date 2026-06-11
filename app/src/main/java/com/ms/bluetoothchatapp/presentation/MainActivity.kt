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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ms.bluetoothchatapp.common.PermissionManager
import com.ms.bluetoothchatapp.presentation.screens.ChatScreen
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
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissionsMap ->
                    val isGranted = permissionsMap.values.all { it }
                    if (isGranted) {
                        println("Permissions granted")
                    }
                }

                LaunchedEffect(key1 = true) {
                    if (!permissionManager.hasAllBluetoothPermissions()) {
                        permissionLauncher.launch(permissionManager.getRequiredBluetoothPermissions())
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = NavDestination.Search
                    ) {
                        composable<NavDestination.Search> {
                            val searchViewModel = hiltViewModel<SearchViewModel>()
                            val pairedDevices by searchViewModel.pairedDevices.collectAsState()
                            val scannedDevices by searchViewModel.scannedDevices.collectAsState()
                            val isDiscovering by searchViewModel.isDiscovering.collectAsState()
                            SearchBluetoothDevices(
                                modifier = Modifier.padding(innerPadding),
                                isDiscovering = isDiscovering,
                                pairedDevices = pairedDevices,
                                newDevices = scannedDevices,
                                onStartDiscovery = {
                                    searchViewModel.startDiscovery()
                                },
                                onStopDiscovery = {
                                    searchViewModel.stopDiscovery()
                                }) {
                                // Handle device click
                            }
                        }
                        composable<NavDestination.Chat> {
                            ChatScreen(modifier = Modifier.padding(innerPadding))
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
    object Search : NavDestination()

    @Serializable
    class Chat : NavDestination()
}