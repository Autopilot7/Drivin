package com.example.openweather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Mô phỏng màn hình "Driver Behavior Analysis"
 * với Safety Score, logs, v.v., cộng thêm màu sắc và icon để UI trực quan hơn.
 */
@Composable
fun DriverBehaviorAnalysisScreen(
    safetyScore: Int = 78,
    scoreMessage: String = "Room for improvement",
    thisWeekScoreChange: Int = 3,
    thisMonthScoreChange: Int = 12,
    suddenBrakesCount: Int = 0,
    suddenAccelerationCount: Int = 0,
    suddenDirectionChangesCount: Int = 0
) {
    // Existing colors and background setup...
    val primaryColor = Color(0xFF4CAF50)
    val backgroundColor = Color(0xFFF9F9F9)
    val secondaryTextColor = Color.Gray
    val warningColor = Color(0xFFF57C00) // Orange for warnings

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title section remains the same
            Text(
                text = "Driver Behavior Analysis",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Safety Score card with added counters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Safety score circle remains the same
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .drawBehind {
                                drawCircle(
                                    brush = SolidColor(primaryColor.copy(alpha = 0.2f)),
                                    radius = size.minDimension / 2
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val sweepAngle = (safetyScore / 100f) * 360f
                        CanvasProgressCircle(
                            sweepAngle = sweepAngle,
                            strokeWidth = 16f,
                            color = primaryColor
                        )
                        Text(
                            text = "$safetyScore",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.Black
                        )
                    }

                    Text(
                        text = "Safety Score",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = scoreMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )

                    // New section: Driving Behavior Counters
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Driving Behaviors Detected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BehaviorCountItem(
                            count = suddenBrakesCount,
                            label = "Sudden Brakes",
                            iconVector = Icons.Default.Warning,
                            tint = warningColor
                        )

                        BehaviorCountItem(
                            count = suddenAccelerationCount,
                            label = "Sudden Accelerations",
                            iconVector = Icons.Default.Warning,
                            tint = warningColor
                        )

                        BehaviorCountItem(
                            count = suddenDirectionChangesCount,
                            label = "Direction Changes",
                            iconVector = Icons.Default.Warning,
                            tint = warningColor
                        )
                    }
                }
            }

            // Rest of the existing UI components...
            Spacer(modifier = Modifier.height(16.dp))

            // Score change cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreChangeCard(
                    title = "This Week",
                    scoreChange = thisWeekScoreChange,
                    primaryColor = primaryColor
                )
                ScoreChangeCard(
                    title = "This Month",
                    scoreChange = thisMonthScoreChange,
                    primaryColor = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Behavior Logs card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Behavior Logs",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "No logs",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No behavior logs found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // In the DriverBehaviorAnalysisScreen composable, change the DrivingAdviceCard call:
                    DrivingAdviceCard(
                        suddenBrakesCount = suddenBrakesCount,
                        suddenAccelerationCount = suddenAccelerationCount,
                        suddenDirectionChangesCount = suddenDirectionChangesCount
                    )
                }
            }
        }
    }
}

@Composable
fun BehaviorCountItem(
    count: Int,
    label: String,
    iconVector: ImageVector,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (count > 0) tint else Color.Gray
        )

        Icon(
            imageVector = iconVector,
            contentDescription = label,
            tint = if (count > 0) tint else Color.Gray,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            fontSize = 10.sp
        )
    }
}
/**
 * Hàm vẽ vòng cung (progress) dạng circle
 * sweepAngle là góc quét (0..360).
 * strokeWidth là độ dày.
 */
@Composable
fun CanvasProgressCircle(sweepAngle: Float, strokeWidth: Float, color: Color) {
    Canvas(modifier = Modifier.size(120.dp)) {
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth
            ),
        )
    }
}

/**
 * Thẻ hiển thị thay đổi điểm (+3, +12, v.v.)
 */
@Composable
fun ScoreChangeCard(
    title: String,
    scoreChange: Int,
    primaryColor: Color
) {
    Card(
        modifier = Modifier
            .width(150.dp) // tùy chỉnh
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (scoreChange >= 0) "+$scoreChange" else "$scoreChange",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (scoreChange >= 0) primaryColor else Color.Red
                )
            )
        }
    }
}
@Composable
fun DrivingAdviceCard(
    suddenBrakesCount: Int = 0,
    suddenAccelerationCount: Int = 0,
    suddenDirectionChangesCount: Int = 0
) {
    val adviceList = remember { AdviceGenerator.adviceList }
    val isLoading = remember { AdviceGenerator.isLoading }

    // Generate advice when component is first composed
    LaunchedEffect(suddenBrakesCount, suddenAccelerationCount, suddenDirectionChangesCount) {
        AdviceGenerator.generateAdvice(
            suddenBrakesCount,
            suddenAccelerationCount,
            suddenDirectionChangesCount
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Driving Advice",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else {
                adviceList.value.forEach { advice ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Advice item",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = advice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}