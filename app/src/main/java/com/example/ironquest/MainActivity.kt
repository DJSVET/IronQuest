package com.example.ironquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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

    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondaryText = Color(0xFF8194AD)

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

    val inventory by PlayerData
        .getInventory(context)
        .collectAsState(initial = emptyList())

    val equippedItems by PlayerData
        .getEquipped(context)
        .collectAsState(initial = emptyMap())

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

    // Автоматически выдаём XP за новые достижения, сохраняя защиту от повторной награды.
    LaunchedEffect(achievements.map { it.id to it.unlocked }) {
        PlayerData.awardUnlockedAchievements(
            context = context,
            achievements = achievements.filter { it.unlocked }
        )
    }



    var showWorkout by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var showInventory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var lastXp by remember { mutableStateOf(0) }
    var lastTotalXp by remember { mutableStateOf(0) }
    var isSavingWorkout by remember { mutableStateOf(false) }
    var lastDroppedItem by remember { mutableStateOf<ItemDefinition?>(null) }

    if (showWorkout) {

        WorkoutScreen(
            isSaving = isSavingWorkout,
            onFinish = { xp, armsReps, shouldersReps, backReps, chestReps, pullUps, dips, pushUps ->
                if (!isSavingWorkout) {
                    isSavingWorkout = true
                    lastXp = xp
                    lastDroppedItem = null

                    scope.launch {
                        try {
                            // Сначала полностью сохраняем тренировку и награды, и только потом открываем результат.
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

                            val totalReps = pullUps + dips + pushUps
                            lastDroppedItem = PlayerData.rollAndSaveDrop(context, totalReps)
                            lastTotalXp = playerStats.xp + xp
                            showWorkout = false
                            showResult = true
                        } finally {
                            isSavingWorkout = false
                        }
                    }
                }
            },
            onBack = {
                if (!isSavingWorkout) showWorkout = false
            }
        )

        return
    }

    if (showResult) {

        WorkoutResultScreen(
            xp = lastXp,
            totalXp = lastTotalXp,
            droppedItem = lastDroppedItem,
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

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    if (showInventory) {
        InventoryScreen(
            items = inventory,
            equippedItemIds = equippedItems,
            onEquip = { item -> scope.launch { PlayerData.equipItem(context, item) } },
            onBack = { showInventory = false }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF080F1B))
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "IRONQUEST",
                        color = Color(0xFFE7F0FF),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "ТВОЙ ПУТЬ СИЛЫ",
                        color = Color(0xFF7E8AA3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1A2A40), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "СЕРИЯ $currentStreak ДН.",
                            color = Color(0xFFB8FF5C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "⚙ НАСТРОЙКИ",
                        color = Color(0xFF8194AD),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showSettings = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TopGameIcon(R.drawable.icon_history, "ИСТОРИЯ", Modifier.weight(1f)) { showHistory = true }
                TopGameIcon(R.drawable.icon_achievements, "НАГРАДЫ $unlockedAchievements/${achievements.size}", Modifier.weight(1f)) { showAchievements = true }
                TopGameIcon(R.drawable.icon_inventory, "ИНВЕНТАРЬ ${inventory.sumOf { it.count }}", Modifier.weight(1f)) { showInventory = true }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val ctaTransition = rememberInfiniteTransition(label = "workout_cta")
            val ctaScale by ctaTransition.animateFloat(
                initialValue = 1f, targetValue = 1.025f,
                animationSpec = infiniteRepeatable(tween(800), repeatMode = RepeatMode.Reverse),
                label = "cta_scale"
            )
            Button(
                onClick = { showWorkout = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .graphicsLayer { scaleX = ctaScale; scaleY = ctaScale },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB8FF5C)
                )
            ) {
                Text(
                    text = "▶  НАЧАТЬ ТРЕНИРОВКУ",
                    color = Color(0xFF080F1B),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "КАЖДАЯ ТРЕНИРОВКА — ШАГ К НОВОМУ УРОВНЮ",
                color = Color(0xFF566178),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111E30), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ТВОЙ ГЕРОЙ",
                            color = Color(0xFF7E8AA3),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Боец",
                            color = Color(0xFFE7F0FF),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "УРОВЕНЬ $level",
                            color = Color(0xFFB8FF5C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    PixelHero(equippedItemIds = equippedItems.values.toSet())
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ОПЫТ",
                        color = Color(0xFF7E8AA3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$levelXp / $levelRequiredXp XP",
                        color = Color(0xFFE7F0FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(7.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(Color(0xFF30394A), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                (levelXp.toFloat() / levelRequiredXp.toFloat()).coerceIn(0f, 1f)
                            )
                            .height(12.dp)
                            .background(Color(0xFFB8FF5C), RoundedCornerShape(3.dp))
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Всего заработано: ${playerStats.xp} XP",
                    color = Color(0xFF7E8AA3),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            WorkoutComparisonCard(history = workoutHistory)

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ХАРАКТЕРИСТИКИ",
                    color = Color(0xFFE7F0FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "ПОКАЗАТЕЛИ",
                    color = Color(0xFF7E8AA3),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            PixelStatCard(
                title = "РУКИ",
                subtitle = "Сила и выносливость",
                value = playerStats.armsReps,
                iconRes = R.drawable.icon_arms,
                accent = Color(0xFFB8FF5C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PixelStatCard(
                title = "ПЛЕЧИ",
                subtitle = "Стабильность и мощь",
                value = playerStats.shouldersReps,
                iconRes = R.drawable.icon_shoulders,
                accent = Color(0xFF5CE1FF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PixelStatCard(
                title = "СПИНА",
                subtitle = "Подтягивания",
                value = playerStats.backReps,
                iconRes = R.drawable.icon_back,
                accent = Color(0xFFC49BFF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PixelStatCard(
                title = "ГРУДЬ",
                subtitle = "Жимовые упражнения",
                value = playerStats.chestReps,
                iconRes = R.drawable.icon_chest,
                accent = Color(0xFFFF8A65)
            )

            Spacer(modifier = Modifier.height(18.dp))

        }
    }
}



@Composable
private fun WorkoutComparisonCard(history: List<WorkoutRecord>) {
    val panel = Color(0xFF111E30)
    val text = Color(0xFFE7F0FF)
    val secondary = Color(0xFF8194AD)
    val accent = Color(0xFFB8FF5C)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(panel, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = "СРАВНЕНИЕ ТРЕНИРОВОК",
            color = secondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Text(
                text = "Заверши первую тренировку, чтобы начать отслеживать прогресс",
                color = text,
                fontSize = 12.sp
            )
        } else {
            val latest = history[0]
            val latestTotal = latest.pullUps + latest.dips + latest.pushUps
            val previous = history.getOrNull(1)
            val previousTotal = previous?.let { it.pullUps + it.dips + it.pushUps }
            val difference = if (previousTotal != null) latestTotal - previousTotal else 0
            val differenceColor = when {
                previousTotal == null -> secondary
                difference > 0 -> Color(0xFF5CE1A0)
                difference < 0 -> Color(0xFFFF6B78)
                else -> secondary
            }
            val changeText = when {
                previousTotal == null -> "НОВАЯ СТАТИСТИКА"
                difference > 0 -> "↑ +$difference"
                difference < 0 -> "↓ $difference"
                else -> "→ 0"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ПОСЛЕДНЯЯ ТРЕНИРОВКА",
                        color = secondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$latestTotal повторений",
                        color = text,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (previousTotal != null) {
                        Text(
                            text = "Предыдущая: $previousTotal",
                            color = secondary,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Следующая тренировка покажет разницу",
                            color = secondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = changeText,
                        color = differenceColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (previousTotal == null) "" else "к прошлой",
                        color = secondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}


@Composable
fun PixelHero(equippedItemIds: Set<String> = emptySet()) {
    val transition = rememberInfiniteTransition(label = "hero_idle")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_bob"
    )

    Box(
        modifier = Modifier
            .height(176.dp)
            .fillMaxWidth(0.48f)
            .graphicsLayer {
                translationY = bob
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.hero_base),
            contentDescription = "Пиксельный персонаж IronQuest",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        val overlayResources = buildList {
            if (equippedItemIds.contains("forest_armor")) {
                add(R.drawable.gear_armor)
            }
            if (equippedItemIds.contains("worn_gloves") ||
                equippedItemIds.contains("steel_bracer")
            ) {
                add(R.drawable.gear_gloves)
            }
            if (equippedItemIds.contains("traveler_boots")) {
                add(R.drawable.gear_boots)
            }
            if (equippedItemIds.contains("iron_helmet") ||
                equippedItemIds.contains("legendary_crown")
            ) {
                add(R.drawable.gear_helmet)
            }
        }

        overlayResources.forEach { resource ->
            Image(
                painter = painterResource(id = resource),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}


@Composable
private fun TopGameIcon(iconRes: Int, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(82.dp).background(Color(0xFF111E30), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(iconRes), contentDescription = label, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color(0xFFB8C7DD), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun PixelStatCard(
    title: String,
    subtitle: String,
    value: Int,
    iconRes: Int,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111E30), RoundedCornerShape(8.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.13f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(35.dp)
                )
            }
            Spacer(modifier = Modifier.padding(start = 4.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = title,
                    color = Color(0xFFE7F0FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF7E8AA3),
                    fontSize = 10.sp
                )
            }
            Text(
                text = value.toString(),
                color = accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
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
    droppedItem: ItemDefinition?,
    onContinue: () -> Unit
) {

    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondaryText = Color(0xFF8194AD)

    // Use the same progression calculation as the home screen.
    val levelInfo = getLevelInfo(totalXp)
    val level = levelInfo.level
    val currentLevelXp = levelInfo.currentXp
    val requiredLevelXp = levelInfo.requiredXp

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
            text = "ТРЕНИРОВКА",
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

        if (droppedItem != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A2A40), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ПРЕДМЕТ НАЙДЕН!",
                    color = droppedItem.rarity.color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(droppedItem.iconRes), contentDescription = droppedItem.name, modifier = Modifier.size(42.dp))
                    Text(droppedItem.name, color = Color(0xFFE7F0FF), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = droppedItem.rarity.label,
                    color = droppedItem.rarity.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "В этот раз предмет не выпал. Повезёт в следующий раз!",
                color = secondaryText,
                fontSize = 12.sp
            )
        }

        Spacer(
            modifier = Modifier.height(25.dp)
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
                text = "$currentLevelXp / $requiredLevelXp XP",
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
                        Color(0xFF223148),
                        RoundedCornerShape(4.dp)
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            (currentLevelXp.toFloat() / requiredLevelXp.toFloat()).coerceIn(0f, 1f)
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
