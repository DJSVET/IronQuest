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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AchievementsScreen(
    language: AppLanguage = AppLanguage.RU,
    achievements: List<Achievement>,
    inventory: List<InventoryEntry> = emptyList(),
    favoriteAchievementIds: Set<String> = emptySet(),
    onToggleFavorite: (Achievement) -> Unit = {},
    onBack: () -> Unit
) {
    val s = strings(language)
    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val text = Color(0xFFE7F0FF)
    val muted = Color(0xFF8194AD)
    val green = Color(0xFFB8FF5C)
    var selectedFilter by remember { mutableStateOf("all") }

    Column(Modifier.fillMaxSize().background(background).verticalScroll(rememberScrollState()).padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_achievements), null, Modifier.size(38.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(s.achievementsTitle, color = text, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(s.unlockedCount(achievements.count { it.unlocked }, achievements.size), color = muted, fontSize = 10.sp, letterSpacing = 1.sp)
            }
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF223148)), shape = RoundedCornerShape(8.dp)) {
                Text(s.backButton, color = text, fontWeight = FontWeight.Bold)
            }
        }
        Text("★ Выбери до 3 любимых — они появятся рядом с персонажем", color = muted, fontSize = 10.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("all" to "ВСЁ", "set" to "СЕТЫ", "achievement" to "ДОСТИЖЕНИЯ").forEach { (key, label) ->
                TextButton(
                    onClick = { selectedFilter = key },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(contentColor = if (selectedFilter == key) green else muted)
                ) {
                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        val forestSet = listOf(
            "forest_armor" to ITEM_CATALOG.first { it.id == "forest_armor" }.name(language),
            "traveler_boots" to ITEM_CATALOG.first { it.id == "traveler_boots" }.name(language),
            "iron_helmet" to ITEM_CATALOG.first { it.id == "iron_helmet" }.name(language),
            "steel_bracer" to ITEM_CATALOG.first { it.id == "steel_bracer" }.name(language)
        )
        val collectedForestPieces = forestSet.count { (id, _) -> inventory.any { it.item.id == id && it.count > 0 } }
        if (selectedFilter != "achievement") Column(
            Modifier.fillMaxWidth().padding(bottom = 14.dp)
                .border(1.dp, Color(0xFFC49BFF).copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .background(panel, RoundedCornerShape(12.dp)).padding(14.dp)
        ) {
            Text(s.setCollection, color = Color(0xFFC49BFF), fontSize = 13.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Text(s.forestGuardian(collectedForestPieces, forestSet.size), color = text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            forestSet.forEach { (id, name) ->
                val collected = inventory.any { it.item.id == id && it.count > 0 }
                Text("${if (collected) "✓" else "○"} $name", color = if (collected) green else muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(5.dp))
            Text(s.forestSetHint, color = muted, fontSize = 11.sp)
        }

        achievements.filter { achievement ->
            selectedFilter == "all" ||
                (selectedFilter == "set" && achievement.category == AchievementCategory.SET) ||
                (selectedFilter == "achievement" && achievement.category == AchievementCategory.ACHIEVEMENT)
        }.forEach { achievement ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)
                .border(1.dp, if (achievement.unlocked) green.copy(alpha = 0.55f) else Color(0xFF293950), RoundedCornerShape(12.dp))
                .background(panel, RoundedCornerShape(12.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(painterResource(achievement.iconRes), achievement.title(language), Modifier.size(42.dp))
                Column(Modifier.weight(1f)) {
                    Text(achievement.title(language), color = if (achievement.unlocked) green else text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(achievement.description(language), color = muted, fontSize = 12.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(if (achievement.unlocked) s.receivedReward(achievement.rewardXp) else s.reward(achievement.rewardXp),
                        color = if (achievement.unlocked) green else Color(0xFFFFC84A), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (achievement.unlocked) "✓" else "—", color = if (achievement.unlocked) green else muted, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    TextButton(onClick = { onToggleFavorite(achievement) }) {
                        Text(if (favoriteAchievementIds.contains(achievement.id)) "★" else "☆", color = if (favoriteAchievementIds.contains(achievement.id)) Color(0xFFFFC84A) else muted, fontSize = 22.sp)
                    }
                }
            }
        }
    }
}
