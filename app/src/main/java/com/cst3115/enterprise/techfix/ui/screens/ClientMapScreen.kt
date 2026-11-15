package com.cst3115.enterprise.techfix.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cst3115.enterprise.techfix.Data.local.TaskEntity
import com.cst3115.enterprise.techfix.location.getTechnicianLocation
import com.cst3115.enterprise.techfix.location.hasLocationPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CancellationException

/**
 * Shows daily client locations and an optional technician location fix.
 */
@Composable
fun ClientMapScreen(
    tasks: List<TaskEntity>
) {
    val context = LocalContext.current

    var selectedTaskId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    var technicianPosition by remember {
        mutableStateOf<LatLng?>(null)
    }

    var locationRequest by remember { mutableIntStateOf(0) }
    var isLocating by remember { mutableStateOf(false) }
    var mapLoaded by remember { mutableStateOf(false) }

    var locationMessage by remember {
        mutableStateOf(
            "Tap a client marker for details, or select Find me."
        )
    }

    val selectedTask = tasks.firstOrNull {
        it.id == selectedTaskId
    }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(45.4215, -75.6972),
            13f
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            locationRequest++
        } else {
            locationMessage =
                "Location permission was declined. Client markers remain available. " +
                        "You can enable location in the app's system settings."
        }
    }

    /**
     * Runs only after the technician requests their location.
     * Leaving the screen cancels this coroutine and its location request.
     */
    LaunchedEffect(locationRequest) {
        if (locationRequest == 0) return@LaunchedEffect

        isLocating = true
        locationMessage = "Finding your location..."

        try {
            val position = getTechnicianLocation(context)

            technicianPosition = position

            locationMessage = if (position != null) {
                "Your location is shown by the blue marker. " +
                        "Accuracy depends on your location settings."
            } else {
                "Location is unavailable. Turn on device location and try again."
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: SecurityException) {
            technicianPosition = null
            locationMessage =
                "Location permission is unavailable. Select Find me to try again."
        } catch (exception: Exception) {
            locationMessage =
                "Unable to get your location. Check device location and Google Play services."
        } finally {
            isLocating = false
        }
    }

    /**
     * Waits for the map before moving its camera to the technician.
     */
    LaunchedEffect(mapLoaded, technicianPosition) {
        val position = technicianPosition

        if (mapLoaded && position != null) {
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(position, 15f)
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TECHFIX / CLIENT LOCATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Your service area.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${tasks.size} scheduled client locations",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (hasLocationPermission(context)) {
                            locationRequest++
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    enabled = !isLocating
                ) {
                    Text(
                        text = if (isLocating) {
                            "Locating..."
                        } else {
                            "Find me"
                        }
                    )
                }

                OutlinedButton(
                    onClick = {
                        selectedTaskId = null
                        technicianPosition = null
                        locationMessage =
                            "Showing Ottawa. Tap a client marker for details."

                        cameraState.position =
                            CameraPosition.fromLatLngZoom(
                                LatLng(45.4215, -75.6972),
                                13f
                            )
                    },
                    enabled = mapLoaded && !isLocating
                ) {
                    Text("Ottawa view")
                }
            }

            Text(
                text = locationMessage,
                style = MaterialTheme.typography.bodySmall,
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
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false
                    ),
                    onMapLoaded = {
                        mapLoaded = true
                    },
                    onMapClick = {
                        selectedTaskId = null
                    }
                ) {
                    tasks.forEach { task ->
                        key(task.id) {
                            val position = LatLng(
                                task.latitude,
                                task.longitude
                            )

                            val markerState = remember(position) {
                                MarkerState(position = position)
                            }

                            Marker(
                                state = markerState,
                                title = task.clientName,
                                snippet = if (task.isCompleted) {
                                    "Completed"
                                } else {
                                    "${task.priority} priority"
                                },
                                icon = BitmapDescriptorFactory.defaultMarker(
                                    if (task.isCompleted) {
                                        BitmapDescriptorFactory.HUE_GREEN
                                    } else {
                                        BitmapDescriptorFactory.HUE_RED
                                    }
                                ),
                                onClick = {
                                    selectedTaskId = task.id
                                    false
                                }
                            )
                        }
                    }

                    technicianPosition?.let { position ->
                        val markerState = remember(position) {
                            MarkerState(position = position)
                        }

                        Marker(
                            state = markerState,
                            title = "Your location",
                            snippet = "Location when Find me was selected",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_AZURE
                            )
                        )
                    }
                }
            }

            if (selectedTask != null) {
                MapClientCard(
                    task = selectedTask,
                    onDirections = {
                        openClientDirections(context, selectedTask)
                    }
                )
            } else {
                Text(
                    text = if (tasks.isEmpty()) {
                        "Add a task to place a client on the map."
                    } else {
                        "Red: pending · Green: completed · Blue: your location"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Shows the client selected on the map and provides a directions action.
 */
@Composable
private fun MapClientCard(
    task: TaskEntity,
    onDirections: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = task.clientName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = task.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (task.isCompleted) {
                    "Completed"
                } else {
                    "Pending · ${task.priority} priority"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = onDirections,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get directions")
            }
        }
    }
}

/**
 * Opens Google Maps or a browser with driving directions to the client.
 */
private fun openClientDirections(
    context: Context,
    task: TaskEntity
) {
    val uri = Uri.parse("https://www.google.com/maps/dir/")
        .buildUpon()
        .appendQueryParameter("api", "1")
        .appendQueryParameter(
            "destination",
            "${task.latitude},${task.longitude}"
        )
        .appendQueryParameter("travelmode", "driving")
        .build()

    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri)
        )
    } catch (exception: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No application is available to open directions.",
            Toast.LENGTH_LONG
        ).show()
    }
}