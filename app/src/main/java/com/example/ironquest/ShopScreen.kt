package com.example.ironquest

import androidx.compose.foundation.Image
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShopScreen(
    coins: Int,
    onOpenCase: (ShopCase) -> Unit,
    rewardItem: ItemDefinition?,
    onDismissReward: () -> Unit,
    onBack: () -> Unit
) {
    val background = Color(0xFF080F1B)
    val panel = Color(0xFF111E30)
    val text = Color(0xFFE7F0FF)
    val muted = Color(0xFF8194AD)
    val accent = Color(0xFFB8FF5C)

    Column(
        modifier = Modifier.fillMaxSize().background(background).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("МАГАЗИН", color = text, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("🪙 $coins", color = Color(0xFFFFC84A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text("Покупай кейсы и собирай коллекцию экипировки", color = muted, fontSize = 12.sp)
        Spacer(Modifier.height(20.dp))

        ShopCase.values().forEach { shopCase ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    .background(panel, RoundedCornerShape(10.dp)).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(54.dp).background(Color(0xFF223148), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("▣", color = when (shopCase) {
                            ShopCase.BASIC -> Color(0xFFB7C0CC)
                            ShopCase.RARE -> Color(0xFF5CE1FF)
                            ShopCase.EPIC -> Color(0xFFC49BFF)
                        }, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(shopCase.title, color = text, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(shopCase.description, color = muted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onOpenCase(shopCase) },
                    enabled = coins >= shopCase.price,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, disabledContainerColor = Color(0xFF30394A)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("ОТКРЫТЬ · 🪙 ${shopCase.price}", color = if (coins >= shopCase.price) Color(0xFF080F1B) else muted, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF223148))) {
            Text("← НАЗАД", color = text, fontWeight = FontWeight.Bold)
        }
    }

    if (rewardItem != null) {
        val item = rewardItem
        AlertDialog(
            onDismissRequest = onDismissReward,
            containerColor = panel,
            title = { Text("ПРЕДМЕТ ПОЛУЧЕН!", color = item.rarity.color, fontWeight = FontWeight.Black) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(painter = painterResource(item.iconRes), contentDescription = item.name, modifier = Modifier.size(72.dp))
                    Text(item.name, color = text, fontWeight = FontWeight.Bold)
                    Text(item.rarity.label, color = item.rarity.color, fontSize = 12.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(item.description, color = muted, fontSize = 12.sp)
                }
            },
            confirmButton = { TextButton(onClick = onDismissReward) { Text("ОТЛИЧНО", color = accent) } }
        )
    }
}
