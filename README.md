# Bluetooth Chat App

A modern Android application for real-time text communication between devices via Bluetooth. Built using Jetpack Compose, Clean Architecture, and MVVM principles.

## 🚀 Features

- **Device Discovery**: Scan for nearby Bluetooth-enabled devices.
- **Device Pairing**: View and connect to already paired devices.
- **Server/Client Modes**: 
  - Act as a **Server** to listen for incoming connections.
  - Act as a **Client** to initiate connections with discovered devices.
- **Real-time Chat**: Exchange text messages instantly once a connection is established.
- **Visual Feedback**: Real-time status updates for discovery and connection progress.
- **Modern UI**: Fully built with Jetpack Compose using Material 3 components.

## 🏗️ Architecture

The project follows **Clean Architecture** principles and **MVVM** pattern:

- **Domain Layer**: Contains business logic, entity models (`BluetoothMessage`), and interface abstractions (`BluetoothController`).
- **Data Layer**: Concrete implementations of domain interfaces using Android Bluetooth APIs, handling low-level socket communication and broadcast receivers.
- **Presentation Layer**: 
  - **ViewModels**: Manage UI state using `StateFlow` and handle user interactions.
  - **Screens**: Declarative UI components built with Jetpack Compose.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Navigation**: Jetpack Compose Navigation (Type-safe)
- **Dependency Injection**: Hilt
- **Concurrency**: Coroutines & Flow
- **Bluetooth**: RFCOMM Sockets (Classic Bluetooth)

## 📡 Bluetooth Implementation

The application leverages the **Android Bluetooth API** for device communication:

- **BluetoothAdapter**: The entry point for all Bluetooth interactions, used for discovery and retrieving paired devices.
- **BluetoothServerSocket & BluetoothSocket**: Implements the **RFCOMM** (Radio Frequency Communication) protocol for reliable, stream-based data transfer.
- **UUID-based Service**: Uses a specific **UUID** (`00001101-0000-1000-8000-00805F9B34FB`) for the Serial Port Profile (SPP) to ensure compatibility between devices.
- **BroadcastReceivers**: Actively monitors system events like `ACTION_FOUND` for discovery, and `ACTION_ACL_CONNECTED`/`DISCONNECTED` for connection lifecycle management.
- **Permissions (API 31+)**: Robust handling of modern permissions including `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, and `BLUETOOTH_ADVERTISE`.

## 📋 Requirements

- Two Android devices.
- Android 8.0 (API level 26) or higher.
- Bluetooth and Location permissions (handled at runtime).

## 📖 How to Use

1. **Permissions**: Grant the requested Bluetooth and Location permissions on startup.
2. **Setup**:
   - **Device A (Server)**: Tap **"Start Server"**. It will enter a listening state.
   - **Device B (Client)**: Tap **"Start Discovery"** to find Device A.
3. **Connect**: On **Device B**, find **Device A** in the "New Devices" or "Paired Devices" list and tap **"Connect"**.
4. **Chat**: Once the "Connecting..." overlay disappears, both devices will be transitioned to the chat screen.
5. **Disconnect**: Tap the "X" in the top bar of the chat screen to close the connection and return to the home screen.

## ⚙️ Development

### Project Structure
- `com.ms.bluetoothchatapp.common`: Utility classes like `PermissionManager`.
- `com.ms.bluetoothchatapp.data`: Implementation of Bluetooth communication and data transfer services.
- `com.ms.bluetoothchatapp.domain`: Models and core logic abstractions.
- `com.ms.bluetoothchatapp.presentation`: UI screens, ViewModels, and state management.

### Build
Run the following command to compile the project:
```bash
./gradlew assembleDebug
```
