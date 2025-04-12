package com.example.openweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.openweather.ui.map.MapScreen
import com.example.openweather.ui.weather.WeatherScreen
import com.example.openweather.ui.theme.OpenWeatherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var directionsApiService: com.example.openweather.network.DirectionsApiService

    private val mapsApiKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TabbedApp(mapsApiKey, directionsApiService)
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedApp(
    mapsApiKey: String,
    directionsApiService: com.example.openweather.network.DirectionsApiService
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // Default to Weather tab
    val context = LocalContext.current

    // Initialize SensorHandler for the Driver Behavior screen
    LaunchedEffect(Unit) {
        SensorHandler.init(context)
        SensorHandler.register()
    }

    // Clean up sensor listener when the activity is destroyed
    DisposableEffect(Unit) {
        onDispose {
            SensorHandler.unregister()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DriVin'",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> WeatherScreen()
                    1 -> MapScreen(context, directionsApiService, mapsApiKey)
                    2 -> DriverBehaviorAnalysisScreen(
                        safetyScore = SensorHandler.safeScoreState.value,
                        //suddenBrakesCount = SensorHandler.suddenBrakesCount.value,
                        //suddenAccelerationCount = SensorHandler.suddenAccelerationCount.value,
                        //suddenDirectionChangesCount = SensorHandler.suddenDirectionChangesCount.value
                        suddenBrakesCount = 1,
                        suddenAccelerationCount = 2,
                        suddenDirectionChangesCount = 0
                    )
                }
            }

            // Bottom navigation
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(
                            if (selectedTabIndex == 0) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = "Weather"
                        )
                    },
                    label = { Text("Weather") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Icon(
                            if (selectedTabIndex == 1) Icons.Filled.Send else Icons.Outlined.Send,
                            contentDescription = "Map"
                        )
                    },
                    label = { Text("Map") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Driver Advice"
                        )
                    },
                    label = { Text("Driver Advice") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
