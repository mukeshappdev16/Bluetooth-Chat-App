package com.ms.bluetoothchatapp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ms.bluetoothchatapp.domain.model.BluetoothDeviceUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBluetoothDevices(
    modifier: Modifier = Modifier,
    isDiscovering: Boolean = false,
    isConnecting: Boolean = false,
    pairedDevices: List<BluetoothDeviceUI> = emptyList(),
    newDevices: List<BluetoothDeviceUI> = emptyList(),
    onStartDiscovery: () -> Unit = {},
    onStopDiscovery: () -> Unit = {},
    onStartServer: () -> Unit = {},
    onDeviceClick: (BluetoothDeviceUI) -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Bluetooth Chat") },
                    actions = {
                        if (isDiscovering) {
                            IconButton(onClick = onStopDiscovery) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop Discovery")
                            }
                        } else {
                            IconButton(onClick = onStartDiscovery) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isDiscovering) "Searching for devices..." else "Ready to connect",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (isDiscovering) onStopDiscovery() else onStartDiscovery()
                            },
                            enabled = !isConnecting
                        ) {
                            Text(if (isDiscovering) "Stop Discovery" else "Start Discovery")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onStartServer,
                            enabled = !isConnecting
                        ) {
                            Text("Start Server")
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Paired Devices Section
                if (pairedDevices.isNotEmpty()) {
                    item {
                        SectionHeader("Paired Devices")
                    }
                    items(pairedDevices) { device ->
                        DeviceListItem(
                            device = device,
                            onClick = { onDeviceClick(device) }
                        )
                    }
                }

                // New Devices Section
                item {
                    SectionHeader("New Devices")
                }

                if (newDevices.isEmpty() && !isDiscovering) {
                    item {
                        EmptyStatePlaceholder("No new devices found")
                    }
                } else {
                    items(newDevices) { device ->
                        DeviceListItem(
                            device = device,
                            onClick = { onDeviceClick(device) }
                        )
                    }
                }
            }
        }

        if (isConnecting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Connecting...", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DeviceListItem(
    device: BluetoothDeviceUI,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(device.name ?: "Unknown Device") },
        supportingContent = { Text(device.address) },
        leadingContent = {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Text(
                "Connect",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
