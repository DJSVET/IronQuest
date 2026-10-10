package com.example.ironquest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AchievementsScreen(achievements: List<Achievement>, onBack: () -> Unit) {
    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val text = Color(0xFFE7F0FF)
    val muted = Color(0xFF8194AD)
    val green = Color(0xFFB8FF5C)

    Column(Modifier.fillMaxSize().background(background).verticalScroll(rememberScrollState()).padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_achievements), null, Modifier.size(38.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text("ДОСТИЖЕНИЯ", color = text, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("ОТКРЫТО ${achievements.count { it.unlocked }} ИЗ ${achievements.size}", color = muted, fontSize = 10.sp, letterSpacing = 1.sp)
            }
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF223148)), shape = RoundedCornerShape(8.dp)) {
                Text("НАЗАД", color = text, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
        achievements.forEachIndexed { index, achievement ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)
                .border(1.dp, if (achievement.unlocked) green.copy(alpha = 0.55f) else Color(0xFF293950), RoundedCornerShape(12.dp))
                .background(panel, RoundedCornerShape(12.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(R.drawable.icon_achievements), null, Modifier.size(42.dp))
                Column(Modifier.weight(1f)) {
                    Text(achievement.title, color = if (achievement.unlocked) green else text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(achievement.description, color = muted, fontSize = 12.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(if (achievement.unlocked) "ПОЛУЧЕНО · +${achievement.rewardXp} XP" else "НАГРАДА · +${achievement.rewardXp} XP",
                        color = if (achievement.unlocked) green else Color(0xFFFFC84A), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Text(if (achievement.unlocked) "✓" else "—", color = if (achievement.unlocked) green else muted, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
