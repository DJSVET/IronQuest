package com.example.ironquest

import androidx.compose.foundation.Image
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkoutScreen(
    onFinish: (
        xp: Int,
        armsReps: Int,
        shouldersReps: Int,
        backReps: Int,
        chestReps: Int,
        pullUps: Int,
        dips: Int,
        pushUps: Int
    ) -> Unit,
    onBack: () -> Unit
) {

    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondaryText = Color(0xFF8194AD)

    val pullUps = remember { mutableStateListOf("") }
    val dips = remember { mutableStateListOf("") }
    val pushUps = remember { mutableStateListOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                    containerColor = Color(0xFF223148)
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(R.drawable.icon_workout), contentDescription = null, modifier = Modifier.height(28.dp))
                Text(text = "ТРЕНИРОВКА", color = text, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(1.dp))
        }


        Spacer(modifier = Modifier.height(20.dp))

        ExerciseBlock(
            title = "ПОДТЯГИВАНИЯ",
            sets = pullUps,
            onAddSet = {
                pullUps.add("")
            },
            panel = panel,
            text = text,
            secondaryText = secondaryText
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExerciseBlock(
            title = "БРУСЬЯ",
            sets = dips,
            onAddSet = {
                dips.add("")
            },
            panel = panel,
            text = text,
            secondaryText = secondaryText
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExerciseBlock(
            title = "ОТЖИМАНИЯ",
            sets = pushUps,
            onAddSet = {
                pushUps.add("")
            },
            panel = panel,
            text = text,
            secondaryText = secondaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        val finishTransition = rememberInfiniteTransition(label = "finish_workout")
        val finishScale by finishTransition.animateFloat(
            initialValue = 1f, targetValue = 1.02f,
            animationSpec = infiniteRepeatable(tween(750), repeatMode = RepeatMode.Reverse),
            label = "finish_scale"
        )
        Button(
            onClick = {
                val pullUpsTotal =
                    pullUps.sumOf { it.toIntOrNull() ?: 0 }

                val dipsTotal =
                    dips.sumOf { it.toIntOrNull() ?: 0 }

                val pushUpsTotal =
                    pushUps.sumOf { it.toIntOrNull() ?: 0 }

                val totalReps =
                    pullUpsTotal + dipsTotal + pushUpsTotal

                val xp = totalReps

                val armsReps =
                    pullUpsTotal + dipsTotal + pushUpsTotal

                val shouldersReps =
                    dipsTotal + pushUpsTotal

                val backReps =
                    pullUpsTotal

                val chestReps =
                    dipsTotal + pushUpsTotal

                onFinish(
                    xp,
                    armsReps,
                    shouldersReps,
                    backReps,
                    chestReps,
                    pullUpsTotal,
                    dipsTotal,
                    pushUpsTotal
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .graphicsLayer { scaleX = finishScale; scaleY = finishScale },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent
            )
        ) {
            Text(
                text = "ЗАВЕРШИТЬ ТРЕНИРОВКУ",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ExerciseBlock(
    title: String,
    sets: MutableList<String>,
    onAddSet: () -> Unit,
    panel: Color,
    text: Color,
    secondaryText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                panel,
                RoundedCornerShape(6.dp)
            )
            .padding(15.dp)
    ) {

        val exerciseIcon = when {
            title.contains("ПОДТЯГИВАНИЯ") -> R.drawable.icon_pullups
            title.contains("БРУСЬЯ") -> R.drawable.icon_dips
            else -> R.drawable.icon_pushups
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(exerciseIcon), contentDescription = null, modifier = Modifier.height(30.dp))
            Text(text = title, color = text, fontSize = 16.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 10.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        sets.forEachIndexed { index, value ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Подход ${index + 1}",
                    color = secondaryText,
                    fontSize = 14.sp
                )

                TextField(
                    value = value,
                    onValueChange = {
                        sets[index] = it
                    },
                    modifier = Modifier.fillMaxWidth(0.45f),
                    singleLine = true,
                    placeholder = {
                        Text("0")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAddSet,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF223148)
            )
        ) {
            Text(
                text = "+ ДОБАВИТЬ ПОДХОД",
                color = text,
                fontSize = 13.sp
            )
        }
    }
}
