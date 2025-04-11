package com.example.openweather.ui.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.openweather.network.DirectionsApiService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MapScreen(
    context: Context,
    directionsApiService: DirectionsApiService,
    apiKey: String
) {
    val routeColor = MaterialTheme.colorScheme.primary.toArgb()
    val mapView = rememberMapViewWithLifecycle()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    val scope = rememberCoroutineScope()

    // State for search field
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Hardcoded route information
    val distance = "764 km"
    val duration = "15 hours 30 minutes"

    Box(modifier = Modifier.fillMaxSize()) {
        // Map View
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { view ->
            view.getMapAsync { gMap ->
                setupMap(gMap)
                googleMap = gMap
            }
        }

        // Search Bar
        if (isSearchExpanded) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp)),
                placeholder = { Text("Search location") },
                trailingIcon = {
                    IconButton(onClick = { isSearchExpanded = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search")
                    }
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
            )
        } else {
            // Search Button (when search is not expanded)
            FloatingActionButton(
                onClick = { isSearchExpanded = true },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }

        // Enhanced Zoom Controls
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
        ) {
            // Zoom In Button
            IconButton(
                onClick = {
                    googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
                },
                modifier = Modifier
                    .padding(4.dp)
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Zoom in",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Zoom Out Button
            IconButton(
                onClick = {
                    googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
                },
                modifier = Modifier
                    .padding(4.dp)
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Zoom out",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Current Zoom Level Indicator
        var currentZoom by remember { mutableFloatStateOf(6f) } // Default zoom level

        LaunchedEffect(googleMap) {
            googleMap?.setOnCameraIdleListener {
                currentZoom = googleMap?.cameraPosition?.zoom ?: 6f
            }
        }

        // Zoom Level Display
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Zoom: ${String.format("%.1f", currentZoom)}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Route Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Hanoi to Da Nang",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Icon(
                        Icons.Outlined.Send,
                        contentDescription = "Driving",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.DateRange,
                            contentDescription = "Distance",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Distance: $distance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = "Duration",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Duration: $duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))



                Button(
                    onClick = {
                        scope.launch {
                            getAndDisplayRoute(
                                googleMap = googleMap!!,
                                context = context,
                                directionsApiService = directionsApiService,
                                apiKey = apiKey,
                                polylineColor = routeColor
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Start Navigation"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Start Navigation",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    // Load route when map is ready
    LaunchedEffect(googleMap) {
        googleMap?.let { gMap ->
            getAndDisplayRoute(
                googleMap = gMap,
                context = context,
                directionsApiService = directionsApiService,
                apiKey = apiKey,
                polylineColor = routeColor
            )
        }
    }
}

private fun setupMap(googleMap: GoogleMap) {
    val hanoi = LatLng(21.0285, 105.8542)
    val danang = LatLng(16.0544, 108.2022)

    googleMap.apply {
        clear()
        addMarker(MarkerOptions().position(hanoi).title("Hanoi"))
        addMarker(MarkerOptions().position(danang).title("Da Nang"))
        moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 6f))
        uiSettings.apply {
            isZoomControlsEnabled = false // We're providing our own zoom controls
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = true
        }
    }
}

private suspend fun getAndDisplayRoute(
    googleMap: GoogleMap,
    context: Context,
    directionsApiService: DirectionsApiService,
    apiKey: String,
    polylineColor: Int // <-- now accepts a plain color
) {
    try {
        val routeResponse = withContext(Dispatchers.IO) {
            directionsApiService.getDirections(
                origin = "21.0285,105.8542",
                destination = "16.0544,108.2022",
                apiKey = apiKey
            )
        }

        if (routeResponse.routes.isNotEmpty()) {
            val route = routeResponse.routes.first()
            val polylinePoints = PolyUtil.decode(route.overviewPolyline.points)

            googleMap.addPolyline(
                PolylineOptions()
                    .addAll(polylinePoints)
                    .color(polylineColor) // <-- uses the passed-in color
                    .width(10f)
            )
        }
    } catch (e: Exception) {
        // Silent fail — log if needed
    }
}
