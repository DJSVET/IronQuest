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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun WorkoutScreen(
    language: AppLanguage = AppLanguage.RU,
    isSaving: Boolean = false,
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
    val s = strings(language)

    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val accent = Color(0xFFB8FF5C)
    val text = Color(0xFFE7F0FF)
    val secondaryText = Color(0xFF8194AD)

    val pullUps = remember { mutableStateListOf("") }
    val dips = remember { mutableStateListOf("") }
    val pushUps = remember { mutableStateListOf("") }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showFinishConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = !isSaving) { showExitConfirmation = true }

    fun finishWorkoutNow() {
        val pullUpsTotal = pullUps.sumOf { it.toIntOrNull() ?: 0 }
        val dipsTotal = dips.sumOf { it.toIntOrNull() ?: 0 }
        val pushUpsTotal = pushUps.sumOf { it.toIntOrNull() ?: 0 }
        val totalReps = pullUpsTotal + dipsTotal + pushUpsTotal
        onFinish(
            totalReps,
            totalReps,
            dipsTotal + pushUpsTotal,
            pullUpsTotal,
            dipsTotal + pushUpsTotal,
            pullUpsTotal,
            dipsTotal,
            pushUpsTotal
        )
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text(s.workoutExitTitle) },
            text = { Text(s.workoutExitText) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmation = false
                    onBack()
                }) { Text(s.exit) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text(s.continueAction) }
            },
            containerColor = Color(0xFF111E30),
            titleContentColor = text,
            textContentColor = secondaryText
        )
    }

    if (showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            title = { Text(s.finishWorkoutTitle) },
            text = { Text(s.finishWorkoutText) },
            confirmButton = {
                TextButton(onClick = {
                    showFinishConfirmation = false
                    finishWorkoutNow()
                }) { Text(s.finish) }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) { Text(s.cancel) }
            },
            containerColor = Color(0xFF111E30),
            titleContentColor = text,
            textContentColor = secondaryText
        )
    }

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
                onClick = { showExitConfirmation = true },
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
                Image(painter = painterResource(R.drawable.icon_workout), contentDescription = null, modifier = Modifier.size(32.dp))
                Text(text = s.workout, color = text, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(1.dp))
        }


        Spacer(modifier = Modifier.height(20.dp))

        ExerciseBlock(
            title = s.pullUps,
            iconRes = R.drawable.icon_pullups,
            language = language,
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
            title = s.dips,
            iconRes = R.drawable.icon_dips,
            language = language,
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
            title = s.pushUps,
            iconRes = R.drawable.icon_pushups,
            language = language,
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
            onClick = { showFinishConfirmation = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .graphicsLayer { scaleX = finishScale; scaleY = finishScale },
            shape = RoundedCornerShape(6.dp),
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                disabledContainerColor = Color(0xFF64705A)
            )
        ) {
            Text(
                text = if (isSaving) s.saving else s.finishWorkout,
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
    iconRes: Int,
    language: AppLanguage = AppLanguage.RU,
    sets: MutableList<String>,
    onAddSet: () -> Unit,
    panel: Color,
    text: Color,
    secondaryText: Color
) {
    val s = strings(language)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                panel,
                RoundedCornerShape(6.dp)
            )
            .padding(15.dp)
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(34.dp))
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
                    text = "${index + 1}.",
                    color = secondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                TextField(
                    value = value,
                    onValueChange = { newValue ->
                        // Только целые неотрицательные повторения.
                        if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                            sets[index] = newValue
                        }
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text(s.repetitionsPlaceholder) },
                    label = { Text(s.repsLabel) }
                )
                TextButton(
                    onClick = { if (sets.size > 1) sets.removeAt(index) },
                    enabled = sets.size > 1
                ) { Text("−", color = if (sets.size > 1) Color(0xFFFF7777) else secondaryText, fontSize = 20.sp) }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1A2A40), RoundedCornerShape(5.dp)).padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(s.totalReps, color = secondaryText, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(sets.sumOf { it.toIntOrNull() ?: 0 }.toString(), color = Color(0xFFB8FF5C), fontSize = 17.sp, fontWeight = FontWeight.Black)
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
                text = s.addSet,
                color = text,
                fontSize = 13.sp
            )
        }
    }
}
