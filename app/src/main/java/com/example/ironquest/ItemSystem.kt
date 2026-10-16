package com.example.ironquest

import androidx.compose.ui.graphics.Color

/* ============================== ХАРАКТЕРИСТИКИ ============================== */

enum class Stat(val title: LText, val short: LText, val icon: String, val color: Color, val hint: LText) {
    STR(LText("СИЛА", "STRENGTH"), LText("СИЛ", "STR"), "str", Color(0xFFFF6B5C),
        LText("Тяговые и жимовые упражнения", "Pulling and pushing")),
    AGI(LText("ЛОВКОСТЬ", "AGILITY"), LText("ЛОВ", "AGI"), "agi", Color(0xFF7BE36A),
        LText("Прыжки, выпады, динамика", "Jumps, lunges, explosiveness")),
    END(LText("ВЫНОСЛИВОСТЬ", "ENDURANCE"), LText("ВЫН", "END"), "end", Color(0xFFFF8AB0),
        LText("Много повторений и кардио", "High reps and cardio")),
    VIT(LText("СТОЙКОСТЬ", "VITALITY"), LText("СТК", "VIT"), "vit", Color(0xFF5CA8FF),
        LText("Кор, статика и баланс", "Core, holds and balance"));
}

enum class Muscle(val title: LText, val icon: String) {
    ARMS(LText("Руки", "Arms"), "arms"),
    SHOULDERS(LText("Плечи", "Shoulders"), "shoulders"),
    CHEST(LText("Грудь", "Chest"), "chest"),
    BACK(LText("Спина", "Back"), "back"),
    CORE(LText("Пресс", "Core"), "core"),
    LEGS(LText("Ноги", "Legs"), "legs");
}

/* ================================= ПРЕДМЕТЫ ================================= */

enum class ItemRarity(val title: LText, val color: Color, val sellPrice: Int, val weight: Int) {
    COMMON(LText("ОБЫЧНЫЙ", "COMMON"), Color(0xFFB7C0CC), 5, 0),
    UNCOMMON(LText("НЕОБЫЧНЫЙ", "UNCOMMON"), Color(0xFF65D98A), 12, 1),
    RARE(LText("РЕДКИЙ", "RARE"), Color(0xFF5CB8FF), 25, 2),
    EPIC(LText("ЭПИЧЕСКИЙ", "EPIC"), Color(0xFFC49BFF), 60, 3),
    LEGENDARY(LText("ЛЕГЕНДАРНЫЙ", "LEGENDARY"), Color(0xFFFFC84A), 150, 4),
    MYTHIC(LText("МИФИЧЕСКИЙ", "MYTHIC"), Color(0xFFFF5C9A), 350, 5);

    fun label(language: AppLanguage) = title.get(language)
}

enum class ItemSlot(val key: String, val title: LText, val emptyIcon: String) {
    HEAD("head", LText("Голова", "Head"), "head_iron"),
    BODY("body", LText("Тело", "Body"), "body_tunic"),
    HANDS("hands", LText("Руки", "Hands"), "hands_gloves"),
    FEET("feet", LText("Ноги", "Feet"), "feet_boots"),
    BACK("back", LText("Плащ", "Cape"), "back_cape"),
    WEAPON("weapon", LText("Оружие", "Weapon"), "weapon_sword"),
    TRINKET("trinket", LText("Амулет", "Trinket"), "trinket_amulet");

    companion object {
        fun fromKey(key: String?): ItemSlot? = values().firstOrNull { it.key == key }
    }
}

/** Пассивные бонусы экипировки, проценты. */
data class Perks(val xp: Int = 0, val coins: Int = 0, val luck: Int = 0) {
    operator fun plus(o: Perks) = Perks(xp + o.xp, coins + o.coins, luck + o.luck)
    val isEmpty get() = xp == 0 && coins == 0 && luck == 0
}

data class ItemDefinition(
    val id: String,
    val title: LText,
    val rarity: ItemRarity,
    val desc: LText,
    val slot: ItemSlot? = null,
    /** Ключ формы из PixelArtData.ITEMS (иконка) — также определяет слой на персонаже. */
    val shape: String,
    /** Ключ материала из PixelArtData.MATERIALS. */
    val material: String,
    val stats: Map<Stat, Int> = emptyMap(),
    val perks: Perks = Perks(),
    val setId: String? = null,
) {
    val sellPrice: Int get() = rarity.sellPrice
    fun name(language: AppLanguage) = title.get(language)
    fun description(language: AppLanguage) = desc.get(language)
    val isEquipment get() = slot != null

    /** Ключ слоя на спрайте героя. */
    val heroLayer: String?
        get() = when (slot) {
            null -> null
            ItemSlot.BACK -> "back_cape"
            ItemSlot.TRINKET -> "trinket_amulet"
            else -> shape
        }
}

data class InventoryEntry(val item: ItemDefinition, val count: Int, val locked: Boolean = false)

private fun t(ru: String, en: String) = LText(ru, en)

private fun gear(
    id: String, ru: String, en: String, slot: ItemSlot, shape: String, mat: String, rarity: ItemRarity,
    dru: String, den: String, stats: Map<Stat, Int> = emptyMap(), perks: Perks = Perks(), set: String? = null,
) = ItemDefinition(id, t(ru, en), rarity, t(dru, den), slot, shape, mat, stats, perks, set)

private fun loot(id: String, ru: String, en: String, shape: String, mat: String, rarity: ItemRarity, dru: String, den: String) =
    ItemDefinition(id, t(ru, en), rarity, t(dru, den), null, shape, mat)

private val Cmn = ItemRarity.COMMON
private val Unc = ItemRarity.UNCOMMON
private val Rar = ItemRarity.RARE
private val Epc = ItemRarity.EPIC
private val Leg = ItemRarity.LEGENDARY
private val Myt = ItemRarity.MYTHIC

/**
 * Каталог предметов. ID старых версий сохранены, чтобы инвентарь игроков не пропал.
 */
val ITEM_CATALOG: List<ItemDefinition> = listOf(
    // --- Сет «Новобранец»
    gear("rookie_band", "Повязка новобранца", "Rookie Headband", ItemSlot.HEAD, "head_band", "crimson", Cmn,
        "Красная повязка. С неё начинается каждый герой.", "A red headband. Every hero starts here.", mapOf(Stat.END to 1), set = "rookie"),
    gear("rookie_tunic", "Рубаха новобранца", "Rookie Tunic", ItemSlot.BODY, "body_tunic", "cloth", Cmn,
        "Простая, но удобная тренировочная рубаха.", "Simple but comfy training shirt.", mapOf(Stat.VIT to 1), set = "rookie"),
    gear("worn_gloves", "Перчатки бойца", "Fighter Gloves", ItemSlot.HANDS, "hands_gloves", "leather", Cmn,
        "Базовые перчатки. Первый предмет экипировки.", "Basic gloves. Your first equipment item.", mapOf(Stat.STR to 1), set = "rookie"),
    gear("rookie_shoes", "Кеды новобранца", "Rookie Sneakers", ItemSlot.FEET, "feet_shoes", "leather", Cmn,
        "Стоптанные, но надёжные.", "Worn out, but reliable.", mapOf(Stat.AGI to 1), set = "rookie"),
    gear("wooden_sword", "Деревянный меч", "Wooden Sword", ItemSlot.WEAPON, "weapon_sword", "wood", Cmn,
        "Тренировочный меч из дуба.", "An oak training sword.", mapOf(Stat.STR to 1), set = "rookie"),

    // --- Сет «Страж леса» (исторический)
    gear("iron_helmet", "Железный шлем", "Iron Helmet", ItemSlot.HEAD, "head_iron", "iron", Epc,
        "Надёжный шлем с защитным забралом.", "A reliable helmet with a protective visor.", mapOf(Stat.VIT to 3), set = "forest_guardian"),
    gear("forest_armor", "Броня лесного стража", "Forest Guardian Armor", ItemSlot.BODY, "body_tunic", "forest", Epc,
        "Лёгкая броня, пропитанная энергией леса.", "Light armor infused with forest energy.", mapOf(Stat.VIT to 2, Stat.END to 2), set = "forest_guardian"),
    gear("steel_bracer", "Стальной наруч", "Steel Bracer", ItemSlot.HANDS, "hands_bracer", "steel", Rar,
        "Наруч опытного бойца.", "A bracer for an experienced fighter.", mapOf(Stat.STR to 2), set = "forest_guardian"),
    gear("traveler_boots", "Сапоги странника", "Traveler Boots", ItemSlot.FEET, "feet_boots", "leather", Rar,
        "Удобные сапоги для долгого пути.", "Comfortable boots for a long road.", mapOf(Stat.AGI to 2), set = "forest_guardian"),
    gear("forest_cloak", "Плащ лесного стража", "Forest Guardian Cloak", ItemSlot.BACK, "back_cape", "forest", Rar,
        "Листья шепчут: «ещё один подход».", "The leaves whisper: \"one more set\".", mapOf(Stat.END to 2), set = "forest_guardian"),

    // --- Сет «Ночной чемпион» (исторический)
    gear("legendary_crown", "Корона чемпиона", "Champion Crown", ItemSlot.HEAD, "head_crown", "gold", Leg,
        "Легендарная награда для тех, кто не сдаётся.", "A legendary reward for those who never quit.",
        mapOf(Stat.STR to 2, Stat.AGI to 2, Stat.END to 2, Stat.VIT to 2), Perks(xp = 5), set = "night_champion"),
    gear("night_armor", "Ночная броня", "Night Armor", ItemSlot.BODY, "body_plate", "night", Leg,
        "Тяжёлая броня с редким тёмным сплавом.", "Heavy armor of a rare dark alloy.", mapOf(Stat.VIT to 3, Stat.STR to 2), set = "night_champion"),
    gear("ember_gloves", "Перчатки искры", "Ember Gloves", ItemSlot.HANDS, "hands_gloves", "ember", Rar,
        "Усиленные перчатки для взрывных подходов.", "Reinforced gloves for explosive sets.", mapOf(Stat.STR to 2, Stat.AGI to 1), set = "night_champion"),
    gear("swift_boots", "Быстрые сапоги", "Swift Boots", ItemSlot.FEET, "feet_shoes", "storm", Unc,
        "Лёгкие сапоги для ежедневных тренировок.", "Light boots for daily training.", mapOf(Stat.AGI to 2), set = "night_champion"),
    gear("night_blade", "Клинок полуночи", "Midnight Blade", ItemSlot.WEAPON, "weapon_sword", "night", Epc,
        "Светится, когда все остальные спят.", "Glows while everyone else sleeps.", mapOf(Stat.STR to 3), set = "night_champion"),

    // --- Сет «Железный легион»
    gear("legion_helm", "Шлем легионера", "Legionnaire Helm", ItemSlot.HEAD, "head_iron", "steel", Unc,
        "Стандартный шлем железного легиона.", "Standard iron legion helmet.", mapOf(Stat.VIT to 1), set = "iron_legion"),
    gear("vanguard_armor", "Доспех авангарда", "Vanguard Armor", ItemSlot.BODY, "body_plate", "steel", Rar,
        "Лёгкая броня для тех, кто всегда идёт первым.", "Light armor for those who always lead.", mapOf(Stat.VIT to 2), set = "iron_legion"),
    gear("iron_gauntlets", "Железные рукавицы", "Iron Gauntlets", ItemSlot.HANDS, "hands_gauntlets", "iron", Unc,
        "Тяжёлые, как последний повтор.", "Heavy as the last rep.", mapOf(Stat.STR to 1), set = "iron_legion"),
    gear("iron_greaves", "Железные поножи", "Iron Greaves", ItemSlot.FEET, "feet_greaves", "iron", Rar,
        "Защита для долгих испытаний.", "Protection for long trials.", mapOf(Stat.VIT to 1, Stat.STR to 1), set = "iron_legion"),
    gear("legion_hammer", "Молот легиона", "Legion Hammer", ItemSlot.WEAPON, "weapon_hammer", "iron", Rar,
        "Каждый удар — как становая тяга.", "Every swing feels like a deadlift.", mapOf(Stat.STR to 2), set = "iron_legion"),

    // --- Сет «Пламя феникса»
    gear("ember_helm", "Рогатый шлем пламени", "Horned Flame Helm", ItemSlot.HEAD, "head_horned", "ember", Epc,
        "Горячая голова — горячий подход.", "Hot head, hot set.", mapOf(Stat.STR to 2, Stat.VIT to 1), set = "phoenix"),
    gear("ember_plate", "Пламенный доспех", "Flame Plate", ItemSlot.BODY, "body_plate", "ember", Epc,
        "Раскаляется с каждым повтором.", "Heats up with every rep.", mapOf(Stat.VIT to 2, Stat.STR to 2), set = "phoenix"),
    gear("ember_bracers", "Наручи жара", "Heat Bracers", ItemSlot.HANDS, "hands_bracer", "ember", Epc,
        "Сжигают лень на подлёте.", "Burn laziness on approach.", mapOf(Stat.STR to 2, Stat.AGI to 1), set = "phoenix"),
    gear("ember_boots", "Сапоги углей", "Cinder Boots", ItemSlot.FEET, "feet_boots", "ember", Epc,
        "Оставляют дымящиеся следы.", "Leave smoking footprints.", mapOf(Stat.AGI to 3), set = "phoenix"),
    gear("phoenix_cape", "Крылья феникса", "Phoenix Wings", ItemSlot.BACK, "back_cape", "ember", Leg,
        "Подняться после каждой неудачи.", "Rise after every failure.", mapOf(Stat.END to 3), Perks(coins = 5), set = "phoenix"),
    gear("phoenix_axe", "Топор феникса", "Phoenix Axe", ItemSlot.WEAPON, "weapon_axe", "ember", Leg,
        "Рубит рекорды пополам.", "Splits records in half.", mapOf(Stat.STR to 4), set = "phoenix"),

    // --- Сет «Ледяной странник»
    gear("frost_hood", "Ледяной капюшон", "Frost Hood", ItemSlot.HEAD, "head_hood", "frost", Rar,
        "Хладнокровие в любой серии.", "Cool-headed through any streak.", mapOf(Stat.END to 2), set = "frost"),
    gear("frost_robe", "Мантия инея", "Rime Robe", ItemSlot.BODY, "body_robe", "frost", Rar,
        "Мерцает, как утренний иней.", "Shimmers like morning frost.", mapOf(Stat.END to 2, Stat.VIT to 1), set = "frost"),
    gear("frost_bracer", "Наруч вьюги", "Blizzard Bracer", ItemSlot.HANDS, "hands_bracer", "frost", Rar,
        "Холоден снаружи, горяч внутри.", "Cold outside, fire inside.", mapOf(Stat.AGI to 2), set = "frost"),
    gear("frost_boots", "Сапоги стужи", "Chill Boots", ItemSlot.FEET, "feet_boots", "frost", Rar,
        "Не скользят даже по льду.", "Never slip, even on ice.", mapOf(Stat.AGI to 1, Stat.END to 1), set = "frost"),
    gear("frost_staff", "Посох метели", "Snowstorm Staff", ItemSlot.WEAPON, "weapon_staff", "frost", Epc,
        "Призывает холодный душ после тренировки.", "Summons a cold shower post-workout.", mapOf(Stat.END to 3), Perks(luck = 3), set = "frost"),

    // --- Сет «Атлет»
    gear("champion_band", "Повязка атлета", "Athlete Headband", ItemSlot.HEAD, "head_band", "jade", Rar,
        "Впитывает пот и сомнения.", "Absorbs sweat and doubt.", mapOf(Stat.END to 2), set = "athlete"),
    gear("athlete_tunic", "Майка атлета", "Athlete Tank", ItemSlot.BODY, "body_tunic", "jade", Rar,
        "Дышащая ткань для долгих сетов.", "Breathable fabric for long sets.", mapOf(Stat.AGI to 1, Stat.END to 1), set = "athlete"),
    gear("training_gloves", "Перчатки спартанца", "Spartan Gloves", ItemSlot.HANDS, "hands_gauntlets", "crimson", Unc,
        "Надёжные перчатки для тяжёлых подходов.", "Reliable gloves for heavy sets.", mapOf(Stat.STR to 1, Stat.END to 1), set = "athlete"),
    gear("athlete_shoes", "Кроссовки атлета", "Athlete Sneakers", ItemSlot.FEET, "feet_shoes", "jade", Rar,
        "Пружинят на каждом прыжке.", "Bounce on every jump.", mapOf(Stat.AGI to 2), set = "athlete"),
    gear("golden_dumbbell", "Золотая гантель", "Golden Dumbbell", ItemSlot.WEAPON, "weapon_dumbbell", "gold", Leg,
        "Весит как мечта, блестит как цель.", "Weighs like a dream, shines like a goal.", mapOf(Stat.STR to 3, Stat.END to 2), Perks(xp = 5), set = "athlete"),

    // --- Сет «Королевский»
    gear("royal_helm", "Венец короля", "King's Circlet", ItemSlot.HEAD, "head_crown", "royal", Leg,
        "Корону носит тот, кто не пропускает тренировки.", "Worn by those who never skip.", mapOf(Stat.VIT to 2, Stat.END to 2), set = "royal"),
    gear("royal_armor", "Королевский доспех", "Royal Armor", ItemSlot.BODY, "body_plate", "royal", Leg,
        "Пурпур и золото дисциплины.", "Purple and gold of discipline.", mapOf(Stat.VIT to 3, Stat.STR to 2), set = "royal"),
    gear("royal_gauntlets", "Королевские перчатки", "Royal Gauntlets", ItemSlot.HANDS, "hands_gauntlets", "gold", Leg,
        "Элитная экипировка настоящего чемпиона.", "Elite gear of a true champion.", mapOf(Stat.STR to 3), set = "royal"),
    gear("royal_greaves", "Королевские поножи", "Royal Greaves", ItemSlot.FEET, "feet_greaves", "gold", Leg,
        "Шаг правителя — уверенный и твёрдый.", "A ruler's stride — steady and firm.", mapOf(Stat.AGI to 2, Stat.VIT to 1), set = "royal"),
    gear("royal_cape", "Королевская мантия", "Royal Mantle", ItemSlot.BACK, "back_cape", "royal", Leg,
        "Развевается даже без ветра.", "Flows even without wind.", mapOf(Stat.END to 2), Perks(coins = 10), set = "royal"),
    gear("royal_sword", "Меч короля", "King's Sword", ItemSlot.WEAPON, "weapon_sword", "gold", Leg,
        "Посвящает в рыцари каждого, кто сделал 100 отжиманий.", "Knights anyone who did 100 push-ups.", mapOf(Stat.STR to 4), set = "royal"),

    // --- Сет «Пустота» (мифический)
    gear("void_hat", "Шляпа пустоты", "Void Hat", ItemSlot.HEAD, "head_wizard", "void", Myt,
        "Внутри — бесконечное количество подходов.", "Holds infinite sets inside.", mapOf(Stat.VIT to 3, Stat.END to 3), Perks(luck = 5), set = "void"),
    gear("void_robe", "Мантия пустоты", "Void Robe", ItemSlot.BODY, "body_robe", "void", Myt,
        "Соткана из несделанных отговорок.", "Woven from unmade excuses.", mapOf(Stat.VIT to 4, Stat.STR to 2), Perks(xp = 5), set = "void"),
    gear("void_cape", "Плащ пустоты", "Void Cloak", ItemSlot.BACK, "back_cape", "void", Myt,
        "Поглощает усталость.", "Absorbs fatigue.", mapOf(Stat.END to 4), Perks(coins = 10), set = "void"),
    gear("void_staff", "Посох пустоты", "Void Staff", ItemSlot.WEAPON, "weapon_staff", "void", Myt,
        "Искривляет время отдыха.", "Bends rest time.", mapOf(Stat.STR to 3, Stat.AGI to 3), Perks(xp = 5), set = "void"),

    // --- Одиночная экипировка
    gear("guardian_helm", "Шлем стража", "Guardian Helm", ItemSlot.HEAD, "head_horned", "steel", Rar,
        "Шлем для тех, кто держит темп до конца.", "For those who keep the pace to the end.", mapOf(Stat.VIT to 2)),
    gear("ranger_hood", "Капюшон следопыта", "Ranger Hood", ItemSlot.HEAD, "head_hood", "forest", Unc,
        "Капюшон опытного охотника за трофеями.", "Hood of a seasoned trophy hunter.", mapOf(Stat.AGI to 1)),
    gear("bone_helm", "Костяной шлем", "Bone Helm", ItemSlot.HEAD, "head_horned", "bone", Epc,
        "Трофей с босса «Понедельник».", "A trophy from the boss \"Monday\".", mapOf(Stat.VIT to 2, Stat.STR to 1)),
    gear("storm_boots", "Сапоги грозы", "Storm Boots", ItemSlot.FEET, "feet_greaves", "storm", Epc,
        "Редкие сапоги, символ скорости и дисциплины.", "Rare boots of speed and discipline.", mapOf(Stat.AGI to 2, Stat.END to 1)),
    gear("crimson_cape", "Алый плащ", "Crimson Cape", ItemSlot.BACK, "back_cape", "crimson", Unc,
        "Классика героического стиля.", "Classic heroic style.", mapOf(Stat.END to 1)),
    gear("storm_cape", "Плащ бури", "Storm Cloak", ItemSlot.BACK, "back_cape", "storm", Rar,
        "Пахнет озоном и победой.", "Smells of ozone and victory.", mapOf(Stat.AGI to 2)),
    gear("iron_dumbbell", "Железная гантель", "Iron Dumbbell", ItemSlot.WEAPON, "weapon_dumbbell", "iron", Unc,
        "Лучшее оружие против слабости.", "The best weapon against weakness.", mapOf(Stat.STR to 1, Stat.END to 1)),
    gear("steel_axe", "Стальной топор", "Steel Axe", ItemSlot.WEAPON, "weapon_axe", "steel", Rar,
        "Колет дрова и рекорды.", "Chops wood and records.", mapOf(Stat.STR to 2)),
    gear("storm_hammer", "Молот грозы", "Storm Hammer", ItemSlot.WEAPON, "weapon_hammer", "storm", Epc,
        "Гремит при каждом повторе.", "Thunders with every rep.", mapOf(Stat.STR to 3)),
    gear("plain_robe", "Мантия ученика", "Apprentice Robe", ItemSlot.BODY, "body_robe", "cloth", Unc,
        "Для медитации между подходами.", "For meditation between sets.", mapOf(Stat.VIT to 1, Stat.END to 1)),

    // --- Амулеты и кольца
    gear("lucky_charm", "Талисман удачи", "Lucky Charm", ItemSlot.TRINKET, "coll_charm", "forest", Unc,
        "Немного повышает шанс добычи.", "Slightly raises loot chance.", perks = Perks(luck = 3)),
    gear("champion_charm", "Талисман чемпиона", "Champion Charm", ItemSlot.TRINKET, "trinket_amulet", "gold", Epc,
        "Даёт больше опыта за тренировки.", "Grants more workout XP.", perks = Perks(xp = 8)),
    gear("phoenix_charm", "Амулет феникса", "Phoenix Amulet", ItemSlot.TRINKET, "trinket_amulet", "ember", Leg,
        "Напоминает, что после каждой неудачи можно подняться.", "After every failure you can rise again.",
        mapOf(Stat.END to 2), Perks(coins = 15)),
    gear("jade_ring", "Нефритовое кольцо", "Jade Ring", ItemSlot.TRINKET, "trinket_ring", "jade", Rar,
        "Притягивает удачу.", "Attracts fortune.", perks = Perks(luck = 5)),
    gear("gold_ring", "Золотое кольцо", "Gold Ring", ItemSlot.TRINKET, "trinket_ring", "gold", Epc,
        "Монеты сами катятся в карман.", "Coins roll into your pocket.", perks = Perks(coins = 12)),
    gear("void_ring", "Кольцо бездны", "Abyss Ring", ItemSlot.TRINKET, "trinket_ring", "void", Myt,
        "Всё и сразу.", "Everything at once.", perks = Perks(xp = 10, coins = 10, luck = 5)),

    // --- Коллекционные предметы
    loot("iron_token", "Железный жетон", "Iron Token", "coll_token", "iron", Cmn,
        "Обычный жетон, найденный после тренировки.", "A common token found after training."),
    loot("energy_drink", "Энергетик", "Energy Drink", "coll_potion", "jade", Unc,
        "Бодрящая находка для коллекции.", "An energizing collectible."),
    loot("sage_scroll", "Свиток мудреца", "Sage Scroll", "coll_scroll", "cloth", Unc,
        "«Техника важнее веса».", "\"Form beats weight.\""),
    loot("lucky_feather", "Счастливое перо", "Lucky Feather", "coll_feather", "storm", Unc,
        "Лёгкое, как первая тренировка.", "Light as your first workout."),
    loot("sun_crystal", "Солнечный кристалл", "Sun Crystal", "coll_crystal", "ember", Rar,
        "Кристалл, хранящий тепло сотни тренировок.", "Holds the warmth of a hundred workouts."),
    loot("ruby_gem", "Рубин силы", "Ruby of Might", "coll_gem", "crimson", Rar,
        "Пульсирует в такт сердцу.", "Pulses with your heartbeat."),
    loot("crystal_core", "Кристальное ядро", "Crystal Core", "coll_crystal", "frost", Epc,
        "Очень редкий артефакт с таинственной энергией.", "A very rare artifact of mysterious energy."),
    loot("champion_badge", "Знак чемпиона", "Champion Badge", "coll_badge", "royal", Epc,
        "Знак за постоянство и характер.", "A badge of consistency and grit."),
    loot("amethyst", "Аметист терпения", "Amethyst of Patience", "coll_gem", "royal", Epc,
        "Растёт медленно, но верно.", "Grows slowly but surely."),
    loot("golden_token", "Золотой жетон", "Golden Token", "coll_token", "gold", Leg,
        "Жетон за упорство, достойный коллекции.", "A token of persistence."),
    loot("dragon_scroll", "Свиток дракона", "Dragon Scroll", "coll_scroll", "gold", Leg,
        "Древний план тренировок драконов.", "An ancient dragon training plan."),
    loot("ancient_crystal", "Древний кристалл", "Ancient Crystal", "coll_crystal", "void", Myt,
        "Мифический кристалл, который почти невозможно найти.", "A mythic crystal nearly impossible to find."),
    loot("void_core", "Ядро пустоты", "Void Core", "coll_orb", "void", Myt,
        "Мифический артефакт из неизвестного мира.", "A mythic artifact from an unknown world."),
)

val ITEMS_BY_ID: Map<String, ItemDefinition> = ITEM_CATALOG.associateBy { it.id }

fun itemById(id: String?): ItemDefinition? = id?.let { ITEMS_BY_ID[it] }

/* =================================== СЕТЫ =================================== */

data class GearSet(
    val id: String,
    val title: LText,
    val bonusStats: Map<Stat, Int>,
    val bonusPerks: Perks,
    val icon: String,
) {
    val pieces: List<ItemDefinition> get() = ITEM_CATALOG.filter { it.setId == id }
}

val GEAR_SETS: List<GearSet> = listOf(
    GearSet("rookie", t("Новобранец", "Rookie"), mapOf(Stat.END to 1, Stat.VIT to 1), Perks(xp = 5), "head_band"),
    GearSet("iron_legion", t("Железный легион", "Iron Legion"), mapOf(Stat.VIT to 3, Stat.STR to 1), Perks(), "head_iron"),
    GearSet("forest_guardian", t("Страж леса", "Forest Guardian"), mapOf(Stat.VIT to 2, Stat.END to 2), Perks(luck = 5), "body_tunic"),
    GearSet("athlete", t("Атлет", "Athlete"), mapOf(Stat.AGI to 2, Stat.END to 2), Perks(coins = 10), "weapon_dumbbell"),
    GearSet("frost", t("Ледяной странник", "Frost Wanderer"), mapOf(Stat.END to 3), Perks(luck = 8), "head_hood"),
    GearSet("night_champion", t("Ночной чемпион", "Night Champion"), mapOf(Stat.STR to 3), Perks(xp = 10), "head_crown"),
    GearSet("phoenix", t("Пламя феникса", "Phoenix Flame"), mapOf(Stat.STR to 2, Stat.AGI to 2), Perks(coins = 15), "head_horned"),
    GearSet("royal", t("Королевский", "Royal"), mapOf(Stat.STR to 2, Stat.AGI to 2, Stat.END to 2, Stat.VIT to 2), Perks(xp = 10, coins = 10), "head_crown"),
    GearSet("void", t("Пустота", "The Void"), mapOf(Stat.STR to 3, Stat.AGI to 3, Stat.END to 3, Stat.VIT to 3), Perks(xp = 15, luck = 10), "head_wizard"),
)

fun gearSetById(id: String?): GearSet? = GEAR_SETS.firstOrNull { it.id == id }

/* ================================== КЕЙСЫ ================================== */

/** Шансы редкостей в процентах: COMMON..MYTHIC. */
enum class ShopCase(val title: LText, val price: Int, val desc: LText, val odds: IntArray, val material: String) {
    BASIC(t("СУНДУК НОВИЧКА", "ROOKIE CHEST"), 30, t("Чаще обычные и необычные вещи", "Mostly common and uncommon"),
        intArrayOf(55, 27, 13, 4, 1, 0), "leather"),
    RARE(t("СУНДУК ИСКАТЕЛЯ", "SEEKER CHEST"), 75, t("Повышенный шанс редких и эпических", "Better odds for rare and epic"),
        intArrayOf(20, 30, 28, 17, 5, 0), "steel"),
    EPIC(t("ЭПИЧЕСКИЙ СУНДУК", "EPIC CHEST"), 150, t("Эпическая и легендарная экипировка", "Epic and legendary gear"),
        intArrayOf(0, 10, 25, 40, 22, 3), "royal"),
    MYTHIC(t("СУНДУК ПУСТОТЫ", "VOID CHEST"), 400, t("Лучший шанс на мифические вещи", "Best odds for mythic items"),
        intArrayOf(0, 0, 10, 35, 40, 15), "void");

    fun title(language: AppLanguage) = title.get(language)
    fun description(language: AppLanguage) = desc.get(language)

    fun rollRarity(random: kotlin.random.Random = kotlin.random.Random): ItemRarity {
        var roll = random.nextInt(100)
        ItemRarity.values().forEachIndexed { index, rarity ->
            if (roll < odds[index]) return rarity
            roll -= odds[index]
        }
        return ItemRarity.COMMON
    }
}

sealed class CaseOpenResult {
    data class Success(val item: ItemDefinition, val case: ShopCase?) : CaseOpenResult()
    object NotEnoughCoins : CaseOpenResult()
    object EmptyCatalog : CaseOpenResult()
}

fun randomItemOf(rarity: ItemRarity, random: kotlin.random.Random = kotlin.random.Random): ItemDefinition {
    var r = rarity
    while (true) {
        val pool = ITEM_CATALOG.filter { it.rarity == r }
        if (pool.isNotEmpty()) return pool.random(random)
        r = ItemRarity.values()[(r.ordinal - 1).coerceAtLeast(0)]
        if (r == ItemRarity.COMMON && ITEM_CATALOG.none { it.rarity == r }) return ITEM_CATALOG.first()
    }
}

/* ============================= ТОВАРЫ МАГАЗИНА ============================== */

enum class Consumable(val title: LText, val desc: LText, val icon: String, val price: Int, val maxStack: Int) {
    XP_POTION(t("Зелье опыта", "XP Potion"), t("×2 опыта за следующую тренировку", "×2 XP on your next workout"), "potion", 60, 5),
    LUCK_CLOVER(t("Клевер удачи", "Luck Clover"), t("Гарантированная добыча и выше редкость в следующей тренировке", "Guaranteed loot with better rarity next workout"), "clover", 70, 5),
    COIN_MAGNET(t("Монетный магнит", "Coin Magnet"), t("×3 монет за следующую тренировку", "×3 coins on your next workout"), "coin", 40, 5),
    STREAK_FREEZE(t("Заморозка серии", "Streak Freeze"), t("Спасёт серию, если пропустишь день. Срабатывает сама", "Saves your streak if you miss a day. Auto-used"), "snowflake", 90, 3);
}

/** Стоимость разблокировки следующего выходного (индекс = уже открыто). */
val REST_DAY_PRICES = mapOf(1 to 150, 2 to 250, 3 to 400, 4 to 600)
const val MAX_REST_DAYS = 5
