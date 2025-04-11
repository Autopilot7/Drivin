package com.example.drivin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.drivin.R
import androidx.compose.material.icons.filled.Check
/**
 * Mô phỏng màn hình "Driver Behavior Analysis"
 * với Safety Score, logs, v.v., cộng thêm màu sắc và icon để UI trực quan hơn.
 */

@Composable
fun DriverBehaviorAnalysisScreen(
    safetyScore: Int = 78,
    scoreMessage: String = "Room for improvement",
    thisWeekScoreChange: Int = 3,
    thisMonthScoreChange: Int = 12
) {
    // Màu chính & phụ có thể thay đổi tùy ý
    val primaryColor = Color(0xFF4CAF50)       // Xanh lá đậm
    val backgroundColor = Color(0xFFF9F9F9)    // Màu nền nhẹ
    val secondaryTextColor = Color.Gray

    // Toàn màn hình background
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
            // Tiêu đề
            Text(
                text = "Driver Behavior Analysis",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Khu vực Safety Score
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
                    // Vẽ vòng tròn có số điểm
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .drawBehind {
                                // Vẽ vòng tròn bao quanh
                                drawCircle(
                                    brush = SolidColor(primaryColor.copy(alpha = 0.2f)),
                                    radius = size.minDimension / 2
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Tô vòng cung thứ 2 (giống progress) - tùy biến
                        val sweepAngle = (safetyScore / 100f) * 360f
                        CanvasProgressCircle(
                            sweepAngle = sweepAngle,
                            strokeWidth = 16f,
                            color = primaryColor
                        )
                        // Hiển thị số điểm ở chính giữa
                        Text(
                            text = "$safetyScore",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.Black
                        )
                    }
                    // Nhãn "Safety Score"
                    Text(
                        text = "Safety Score",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    // Thông điệp
                    Text(
                        text = scoreMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Khu vực thể hiện thay đổi điểm
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // This Week
                ScoreChangeCard(
                    title = "This Week",
                    scoreChange = thisWeekScoreChange,
                    primaryColor = primaryColor
                )
                // This Month
                ScoreChangeCard(
                    title = "This Month",
                    scoreChange = thisMonthScoreChange,
                    primaryColor = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Behavior Logs
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
                    // Tiêu đề
                    Text(
                        text = "Behavior Logs",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Thông báo khi chưa có logs
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

                    DrivingAdviceCard()
                }
            }
        }
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
            style = androidx.compose.ui.graphics.drawscope.Stroke(
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
    adviceList: List<String> = listOf(
        "Maintain a safe following distance",
        "Reduce speed in adverse weather conditions",
        "Avoid using phone while driving",
        "Take regular breaks on long journeys"
    )
) {
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

            adviceList.forEach { advice ->
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