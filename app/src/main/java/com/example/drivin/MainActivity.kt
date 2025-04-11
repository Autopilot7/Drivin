package com.example.drivin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.drivin.ui.theme.DrivinTheme
import com.example.drivin.ui.DriverBehaviorAnalysisScreen
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drivin.ui.theme.DrivinTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SensorHandler.init(this)
        setContent {
            DrivinTheme {
                DriverBehaviorAnalysisScreen(
                    safetyScore = SensorHandler.safeScoreState.value,
                    scoreMessage = "Room for improvement",
                    thisWeekScoreChange = 3,
                    thisMonthScoreChange = 12,
                    //suddenBrakesCount = SensorHandler.suddenBrakesCount.value,
                    //suddenAccelerationCount = SensorHandler.suddenAccelerationCount.value,
                    //suddenDirectionChangesCount = SensorHandler.suddenDirectionChangesCount.value
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        SensorHandler.register()
    }

    override fun onPause() {
        super.onPause()
        SensorHandler.unregister()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DrivinTheme {
        Greeting("Android")
    }
}
