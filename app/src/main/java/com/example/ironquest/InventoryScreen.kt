package com.example.ironquest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private val IqBackground = Color(0xFF080F1B)
private val IqPanel = Color(0xFF111E30)
private val IqText = Color(0xFFE7F0FF)
private val IqMuted = Color(0xFF8194AD)
private val IqGreen = Color(0xFFB8FF5C)

@Composable
fun InventoryScreen(
    items: List<InventoryEntry>,
    equippedItemIds: Map<String, String>,
    onEquip: (ItemDefinition) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(IqBackground)
            .verticalScroll(rememberScrollState()).padding(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.icon_inventory), null, Modifier.size(36.dp))
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text("ИНВЕНТАРЬ", color = IqText, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("ПРЕДМЕТОВ: ${items.sumOf { it.count }}", color = IqMuted, fontSize = 10.sp, letterSpacing = 1.sp)
            }
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF223148)),
                shape = RoundedCornerShape(8.dp)) { Text("НАЗАД", color = IqText, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(18.dp))

        if (items.isEmpty()) {
            Column(Modifier.fillMaxWidth().background(IqPanel, RoundedCornerShape(12.dp)).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.icon_inventory), null, Modifier.size(58.dp))
                Spacer(Modifier.height(12.dp))
                Text("Инвентарь пуст", color = IqText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Завершай тренировки — после них могут выпасть предметы.", color = IqMuted, fontSize = 13.sp)
            }
        } else {
            items.forEach { entry ->
                val item = entry.item
                val isEquipped = item.slot != null && equippedItemIds[item.slot] == item.id
                Column(Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    .border(1.dp, item.rarity.color.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .background(IqPanel, RoundedCornerShape(12.dp)).padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(68.dp).background(item.rarity.color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center) {
                            Image(painterResource(item.iconRes), null, Modifier.size(54.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(item.name, color = IqText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(item.rarity.label, color = item.rarity.color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Количество: ${entry.count}", color = IqGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(item.description, color = IqMuted, fontSize = 12.sp)
                    if (item.slot != null) {
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { onEquip(item) }, modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isEquipped) Color(0xFF25452F) else IqGreen),
                            shape = RoundedCornerShape(8.dp)) {
                            Text(if (isEquipped) "НАДЕТО" else "НАДЕТЬ НА ПЕРСОНАЖА",
                                color = if (isEquipped) IqGreen else Color(0xFF0B1420), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
