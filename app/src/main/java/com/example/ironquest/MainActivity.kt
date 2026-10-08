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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val playerStats by PlayerData
        .getStats(context)
        .collectAsState(
            initial = PlayerStats(
                xp = 0,
                armsReps = 0,
                shouldersReps = 0,
                backReps = 0,
                chestReps = 0
            )
        )

    val levelInfo = getLevelInfo(playerStats.xp)

    val level = levelInfo.level
    val levelXp = levelInfo.currentXp
    val levelRequiredXp = levelInfo.requiredXp

    val workoutHistory by WorkoutHistoryData
        .getHistory(context)
        .collectAsState(initial = emptyList())

    val currentStreak = calculateStreak(workoutHistory)

    val achievements = getAchievements(
        history = workoutHistory,
        totalXp = playerStats.xp,
        streak = currentStreak
    )

    val unlockedAchievements = achievements.count {
        it.unlocked
    }



    var showWorkout by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var lastXp by remember { mutableStateOf(0) }

    if (showWorkout) {

        WorkoutScreen(
            onFinish = { xp, armsReps, shouldersReps, backReps, chestReps, pullUps, dips, pushUps ->
                lastXp = xp

                scope.launch {
                    PlayerData.addWorkout(
                        context = context,
                        xp = xp,
                        armsReps = armsReps,
                        shouldersReps = shouldersReps,
                        backReps = backReps,
                        chestReps = chestReps
                    )

                    WorkoutHistoryData.addWorkout(
                        context = context,
                        xp = xp,
                        pullUps = pullUps,
                        dips = dips,
                        pushUps = pushUps
                    )
                }

                showWorkout = false
                showResult = true
            },
            onBack = {
                showWorkout = false
            }
        )

        return
    }

    if (showResult) {

        WorkoutResultScreen(
            xp = lastXp,
            totalXp = playerStats.xp,
            onContinue = {
                showResult = false
            }
        )

        return
    }

    if (showHistory) {

        WorkoutHistoryScreen(
            context = context,
            onBack = {
                showHistory = false
            }
        )

        return
    }

    if (showAchievements) {

        AchievementsScreen(
            achievements = achievements,
            onBack = {
                showAchievements = false
            }
        )

        return
    }

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

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "LEVEL $level",
                    color = text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "$levelXp / $levelRequiredXp XP",
                    color = secondaryText,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

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
                            .fillMaxWidth(
                                (levelXp.toFloat() / levelRequiredXp.toFloat())
                                    .coerceIn(0f, 1f)
                            )
                            .height(14.dp)
                            .background(
                                accent,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(
                    modifier = Modifier.height(25.dp)
                )

                ProgressCard(
                    title = "💪 Руки",
                    value = "${(playerStats.armsReps / 10).coerceAtMost(100)}%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                ProgressCard(
                    title = "🏔 Плечи",
                    value = "${(playerStats.shouldersReps / 10).coerceAtMost(100)}%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                ProgressCard(
                    title = "🪽 Спина",
                    value = "${(playerStats.backReps / 10).coerceAtMost(100)}%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                ProgressCard(
                    title = "🫀 Грудь",
                    value = "${(playerStats.chestReps / 10).coerceAtMost(100)}%",
                    panel = panel,
                    text = text,
                    secondaryText = secondaryText
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        showWorkout = true
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

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Text(
                        text = "🔥 Серия: $currentStreak",
                        color = secondaryText,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "🏆 Достижения: $unlockedAchievements/${achievements.size}",
                        color = secondaryText,
                        fontSize = 14.sp
                    )
                }
                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {
                        showHistory = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A3442)
                    )
                ) {
                    Text(
                        text = "📜 ИСТОРИЯ ТРЕНИРОВОК",
                        color = text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Button(
                    onClick = {
                        showAchievements = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A3442)
                    )
                ) {
                    Text(
                        text = "🏆 ДОСТИЖЕНИЯ",
                        color = text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
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

@Composable
fun WorkoutResultScreen(
    xp: Int,
    totalXp: Int,
    onContinue: () -> Unit
) {

    val background = Color(0xFF17151C)
    val panel = Color(0xFF25212D)
    val accent = Color(0xFFE6A23C)
    val text = Color(0xFFF3E8D0)
    val secondaryText = Color(0xFFA99FB2)

    val level = (totalXp / 100) + 1
    val currentLevelXp = totalXp % 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(50.dp)
        )

        Text(
            text = "★ ТРЕНИРОВКА",
            color = accent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "ЗАВЕРШЕНА",
            color = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(45.dp)
        )

        Text(
            text = "+$xp XP",
            color = accent,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Text(
            text = "Отличная работа!",
            color = secondaryText,
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(45.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    panel,
                    RoundedCornerShape(6.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "УРОВЕНЬ $level",
                color = text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "$currentLevelXp / 100 XP",
                color = secondaryText,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

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
                        .fillMaxWidth(
                            currentLevelXp / 100f
                        )
                        .height(14.dp)
                        .background(
                            accent,
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent
            )
        ) {

            Text(
                text = "← ВЕРНУТЬСЯ В МЕНЮ",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
