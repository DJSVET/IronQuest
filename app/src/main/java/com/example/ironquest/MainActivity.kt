package com.example.ironquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IronQuestApp()
        }
    }
}

@Composable
fun IronQuestApp() {
    val background = Color(0xFF17151C)
    val panel = Color(0xFF25212D)
    val accent = Color(0xFFE6A23C)
    val text = Color(0xFFF3E8D0)
    val secondaryText = Color(0xFFA99FB2)

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "⚔ IRON QUEST",
                    color = accent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "LEVEL 1",
                    color = text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "0 / 100 XP",
                    color = secondaryText,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(
                            Color(0xFF3A3442),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.01f)
                            .height(14.dp)
                            .background(
                                accent,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                ProgressCard(
                    title = "💪 Руки",
                    value = "0%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProgressCard(
                    title = "🏔 Плечи",
                    value = "0%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProgressCard(
                    title = "🪽 Спина",
                    value = "0%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProgressCard(
                    title = "🫀 Грудь",
                    value = "0%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        // Пока ничего не делаем
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent
                    )
                ) {
                    Text(
                        text = "⚔ НАЧАТЬ ТРЕНИРОВКУ",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "🔥 Серия: 0",
                        color = secondaryText,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "🏆 Достижения: 0",
                        color = secondaryText,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressCard(
    title: String,
    value: String,
    panel: Color,
    text: Color,
    secondaryText: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                panel,
                RoundedCornerShape(6.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = secondaryText,
            fontSize = 15.sp
        )
    }
}