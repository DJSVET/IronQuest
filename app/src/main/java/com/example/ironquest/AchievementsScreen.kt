package com.example.ironquest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AchievementsScreen(
    achievements: List<Achievement>,
    onBack: () -> Unit
) {
    val background = Color(0xFF17151C)
    val panel = Color(0xFF25212D)
    val accent = Color(0xFFE6A23C)
    val text = Color(0xFFF3E8D0)
    val secondaryText = Color(0xFFA99FB2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3A3442)
                )
            ) {
                Text(
                    text = "←",
                    color = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "🏆 ДОСТИЖЕНИЯ",
                color = accent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(1.dp))
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        achievements.forEachIndexed { index, achievement ->

            AchievementCard(
                achievement = achievement,
                panel = panel,
                accent = accent,
                text = text,
                secondaryText = secondaryText
            )

            if (index < achievements.lastIndex) {
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    panel: Color,
    accent: Color,
    text: Color,
    secondaryText: Color
) {
    val cardColor =
        if (achievement.unlocked) {
            panel
        } else {
            Color(0xFF1E1B23)
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                cardColor,
                RoundedCornerShape(6.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = if (achievement.unlocked) {
                achievement.title
            } else {
                "🔒 ${achievement.title}"
            },
            color = if (achievement.unlocked) {
                accent
            } else {
                secondaryText
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = achievement.description,
            color = secondaryText,
            fontSize = 12.sp
        )
    }
}