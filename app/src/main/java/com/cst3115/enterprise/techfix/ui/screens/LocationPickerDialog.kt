package com.cst3115.enterprise.techfix.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

/**
 * Allows the technician to choose client coordinates by tapping the map.
 * Coordinates are returned only when the technician confirms.
 */
@Composable
fun LocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onDismiss: () -> Unit,
    onLocationSelected: (Double, Double) -> Unit
) {
    val validInitialLocation =
        initialLatitude != null &&
                initialLongitude != null &&
                initialLatitude.isFinite() &&
                initialLongitude.isFinite() &&
                initialLatitude in -90.0..90.0 &&
                initialLongitude in -180.0..180.0

    var selectedLatitude by rememberSaveable {
        mutableStateOf(
            if (validInitialLocation) initialLatitude else null
        )
    }

    var selectedLongitude by rememberSaveable {
        mutableStateOf(
            if (validInitialLocation) initialLongitude else null
        )
    }

    val selectedPosition = selectedLatitude?.let { latitude ->
        selectedLongitude?.let { longitude ->
            LatLng(latitude, longitude)
        }
    }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedPosition ?: LatLng(45.4215, -75.6972),
            if (selectedPosition != null) 15f else 13f
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose client location",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Tap the service location to place a pin. " +
                            "Tap again to move it.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            mapToolbarEnabled = false
                        ),
                        onMapClick = { position ->
                            selectedLatitude = position.latitude
                            selectedLongitude = position.longitude
                        }
                    ) {
                        selectedPosition?.let { position ->
                            val markerState = remember(position) {
                                MarkerState(position = position)
                            }

                            Marker(
                                state = markerState,
                                title = "Selected client location"
                            )
                        }
                    }
                }

                Text(
                    text = if (selectedPosition == null) {
                        "No location selected."
                    } else {
                        String.format(
                            Locale.US,
                            "Latitude: %.6f\nLongitude: %.6f",
                            selectedPosition.latitude,
                            selectedPosition.longitude
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "This sets coordinates only. " +
                            "Enter the matching service address in the form.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        selectedPosition?.let { position ->
                            onLocationSelected(
                                position.latitude,
                                position.longitude
                            )
                        }
                    },
                    enabled = selectedPosition != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                ) {
                    Text("Use this location")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}