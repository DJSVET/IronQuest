package com.example.ironquest

import androidx.compose.ui.graphics.Color

/** Цвета редкости используются в карточках предметов и рамках инвентаря. */
enum class ItemRarity(val label: String, val color: Color) {
    COMMON("ОБЫЧНЫЙ", Color(0xFFB7C0CC)),
    UNCOMMON("НЕОБЫЧНЫЙ", Color(0xFF65D98A)),
    RARE("РЕДКИЙ", Color(0xFF5CE1FF)),
    EPIC("ЭПИЧЕСКИЙ", Color(0xFFC49BFF)),
    LEGENDARY("ЛЕГЕНДАРНЫЙ", Color(0xFFFFC84A)),
    MYTHIC("МИФИЧЕСКИЙ", Color(0xFFFF6B8A))
}

data class ItemDefinition(
    val id: String,
    val name: String,
    val iconRes: Int,
    val rarity: ItemRarity,
    val description: String,
    /** Слот экипировки: head, body, hands, feet. null означает коллекционный предмет. */
    val slot: String? = null,
    /** Прозрачный PNG-слой, который отображается поверх героя. */
    val overlayRes: Int? = null
) {
    /** Цена продажи одной копии предмета. */
    val sellPrice: Int
        get() = when (rarity) {
            ItemRarity.COMMON -> 5
            ItemRarity.UNCOMMON -> 12
            ItemRarity.RARE -> 25
            ItemRarity.EPIC -> 60
            ItemRarity.LEGENDARY -> 150
            ItemRarity.MYTHIC -> 350
        }
}

data class InventoryEntry(
    val item: ItemDefinition,
    val count: Int,
    val locked: Boolean = false
)

val ITEM_CATALOG = listOf(
    ItemDefinition("worn_gloves", "Перчатки бойца", R.drawable.item_gloves, ItemRarity.COMMON,
        "Базовые перчатки. Первый предмет экипировки.", slot = "hands", overlayRes = R.drawable.gear_gloves),
    ItemDefinition("iron_token", "Железный жетон", R.drawable.item_token, ItemRarity.COMMON,
        "Обычный жетон, найденный после тренировки."),
    ItemDefinition("energy_drink", "Энергетик", R.drawable.item_energy, ItemRarity.UNCOMMON,
        "Редкая находка. Пока коллекционный предмет."),
    ItemDefinition("lucky_charm", "Талисман удачи", R.drawable.item_charm, ItemRarity.UNCOMMON,
        "Маленький талисман для будущих приключений."),
    ItemDefinition("steel_bracer", "Стальной наруч", R.drawable.item_bracer, ItemRarity.RARE,
        "Наруч опытного бойца.", slot = "hands", overlayRes = R.drawable.gear_gloves),
    ItemDefinition("crystal_core", "Кристальное ядро", R.drawable.item_crystal, ItemRarity.EPIC,
        "Очень редкий артефакт с таинственной энергией."),
    ItemDefinition("forest_armor", "Броня лесного стража", R.drawable.item_armor, ItemRarity.EPIC,
        "Лёгкая броня, пропитанная энергией леса.", slot = "body", overlayRes = R.drawable.gear_armor),
    ItemDefinition("traveler_boots", "Сапоги странника", R.drawable.item_boots, ItemRarity.RARE,
        "Удобные сапоги для долгого пути.", slot = "feet", overlayRes = R.drawable.gear_boots),
    ItemDefinition("iron_helmet", "Железный шлем", R.drawable.item_helmet, ItemRarity.EPIC,
        "Надёжный шлем с защитным забралом.", slot = "head", overlayRes = R.drawable.gear_helmet),
    ItemDefinition("legendary_crown", "Корона чемпиона", R.drawable.item_legendary_crown, ItemRarity.LEGENDARY,
        "Легендарная награда для тех, кто не сдаётся.", slot = "head", overlayRes = R.drawable.gear_helmet),
    ItemDefinition("ember_gloves", "Перчатки искры", R.drawable.item_gloves, ItemRarity.RARE,
        "Усиленные перчатки для взрывных подходов.", slot = "hands", overlayRes = R.drawable.gear_gloves),
    ItemDefinition("guardian_helm", "Шлем стража", R.drawable.item_helmet, ItemRarity.RARE,
        "Шлем для тех, кто держит темп до конца.", slot = "head", overlayRes = R.drawable.gear_helmet),
    ItemDefinition("night_armor", "Ночная броня", R.drawable.item_armor, ItemRarity.LEGENDARY,
        "Тяжёлая броня с редким тёмным сплавом.", slot = "body", overlayRes = R.drawable.gear_armor),
    ItemDefinition("swift_boots", "Быстрые сапоги", R.drawable.item_boots, ItemRarity.UNCOMMON,
        "Лёгкие сапоги для ежедневных тренировок.", slot = "feet", overlayRes = R.drawable.gear_boots),
    ItemDefinition("ancient_crystal", "Древний кристалл", R.drawable.item_crystal, ItemRarity.MYTHIC,
        "Мифический кристалл, который почти невозможно найти."),
    ItemDefinition("champion_charm", "Талисман чемпиона", R.drawable.item_charm, ItemRarity.EPIC,
        "Редкий талисман для длинных серий."),
    ItemDefinition("golden_token", "Золотой жетон", R.drawable.item_token, ItemRarity.LEGENDARY,
        "Жетон за упорство, достойный коллекции."),
    ItemDefinition("training_gloves", "Перчатки спартанца", R.drawable.item_gloves, ItemRarity.UNCOMMON,
        "Надёжные перчатки для тяжёлых подходов.", slot = "hands", overlayRes = R.drawable.gear_gloves),
    ItemDefinition("vanguard_armor", "Доспех авангарда", R.drawable.item_armor, ItemRarity.RARE,
        "Лёгкая броня для тех, кто всегда идёт первым.", slot = "body", overlayRes = R.drawable.gear_armor),
    ItemDefinition("storm_boots", "Сапоги грозы", R.drawable.item_boots, ItemRarity.EPIC,
        "Редкие сапоги, символ скорости и дисциплины.", slot = "feet", overlayRes = R.drawable.gear_boots),
    ItemDefinition("ranger_hood", "Капюшон следопыта", R.drawable.item_helmet, ItemRarity.UNCOMMON,
        "Капюшон опытного охотника за трофеями.", slot = "head", overlayRes = R.drawable.gear_helmet),
    ItemDefinition("sun_crystal", "Солнечный кристалл", R.drawable.item_crystal, ItemRarity.RARE,
        "Кристалл, хранящий тепло сотни тренировок."),
    ItemDefinition("champion_badge", "Знак чемпиона", R.drawable.item_token, ItemRarity.EPIC,
        "Знак за постоянство и характер."),
    ItemDefinition("phoenix_charm", "Амулет феникса", R.drawable.item_charm, ItemRarity.LEGENDARY,
        "Напоминает, что после каждой неудачи можно подняться."),
    ItemDefinition("iron_greaves", "Железные поножи", R.drawable.item_boots, ItemRarity.RARE,
        "Защита для долгих испытаний.", slot = "feet", overlayRes = R.drawable.gear_boots),
    ItemDefinition("void_core", "Ядро пустоты", R.drawable.item_crystal, ItemRarity.MYTHIC,
        "Мифический артефакт из неизвестного мира."),
    ItemDefinition("royal_gauntlets", "Королевские перчатки", R.drawable.item_gloves, ItemRarity.LEGENDARY,
        "Элитная экипировка настоящего чемпиона.", slot = "hands", overlayRes = R.drawable.gear_gloves)
)


/** Виды кейсов и их стоимость в магазине. */
enum class ShopCase(val title: String, val price: Int, val description: String) {
    BASIC("КЕЙС НОВИЧКА", 30, "Чаще обычные и необычные предметы"),
    RARE("КЕЙС ИСКАТЕЛЯ", 75, "Повышенный шанс редких и эпических вещей"),
    EPIC("ЭПИЧЕСКИЙ КЕЙС", 150, "Высокий шанс эпической и легендарной экипировки")
}

sealed class CaseOpenResult {
    data class Success(val item: ItemDefinition) : CaseOpenResult()
    object NotEnoughCoins : CaseOpenResult()
    object EmptyCatalog : CaseOpenResult()
}
