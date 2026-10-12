package com.example.ironquest

import android.content.Context

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    RU("ru", "Russian", "Русский"),
    EN("en", "English", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage = values().firstOrNull { it.code == code } ?: RU
    }
}

object AppPreferences {
    private const val PREFS = "ironquest_app_settings"
    private const val KEY_LANGUAGE = "language"

    fun getLanguage(context: Context): AppLanguage =
        AppLanguage.fromCode(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANGUAGE, AppLanguage.RU.code))

    fun setLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }
}

data class AppStrings(
    val tagline: String,
    val streak: (Int) -> String,
    val lastWorkoutToday: String,
    val lastWorkoutYesterday: String,
    val lastWorkoutDate: (String) -> String,
    val lastWorkoutNone: String,
    val history: String,
    val achievements: String,
    val rewardsCount: (Int, Int) -> String,
    val inventoryWithCount: (Int) -> String,
    val caseShop: String,
    val startWorkout: String,
    val workoutMotivation: String,
    val yourHero: String,
    val fighter: String,
    val level: (Int) -> String,
    val experience: String,
    val totalEarned: (Int) -> String,
    val attributes: String,
    val stats: String,
    val arms: String,
    val armsSubtitle: String,
    val shoulders: String,
    val shouldersSubtitle: String,
    val back: String,
    val backSubtitle: String,
    val chest: String,
    val chestSubtitle: String,
    val workoutComparison: String,
    val comparisonEmpty: String,
    val newStats: String,
    val lastWorkout: String,
    val reps: (Int) -> String,
    val previous: (Int) -> String,
    val nextWorkoutDifference: String,
    val comparedToPrevious: String,
    val workout: String,
    val completed: String,
    val greatWork: String,
    val coinsForWorkout: (Int) -> String,
    val itemFound: String,
    val noItemDropped: String,
    val backToMenu: String,
    val shop: String,
    val shopSubtitle: String,
    val openCase: (Int) -> String,
    val openingCase: String,
    val caseOpeningHint: String,
    val itemReceived: String,
    val great: String,
    val backButton: String,
    val settings: String,
    val language: String,
    val chooseLanguage: String,
    val notifications: String,
    val remindWorkout: String,
    val remindWorkoutSub: String,
    val protectStreak: String,
    val protectStreakSub: String,
    val reminderTime: String,
    val reminderTimeSub: String,
    val updates: String,
    val updateNotifications: String,
    val updateNotificationsSub: String,
    val checkUpdates: String,
    val checking: String,
    val aboutApp: String,
    val appVersion: String,
    val projectGithub: String,
    val open: String,
    val dangerZone: String,
    val resetAllData: String,
    val settingsNote: String,
    val updatePrompt: String,
    val updateChecking: String,
    val updateLatest: String,
    val updateAvailable: (String) -> String,
    val updateFailed: String,
    val notificationGranted: String,
    val notificationDenied: String,
    val resetTitle: String,
    val resetText: String,
    val resetConfirm: String,
    val cancel: String,
    val later: String,
    val downloadApk: String,
    val openRelease: String,
    val updateDialogTitle: String,
    val updateDialogText: (String) -> String,
    val workoutExitTitle: String,
    val workoutExitText: String,
    val exit: String,
    val continueAction: String,
    val finishWorkoutTitle: String,
    val finishWorkoutText: String,
    val finish: String,
    val pullUps: String,
    val dips: String,
    val pushUps: String,
    val saving: String,
    val finishWorkout: String,
    val repetitionsPlaceholder: String,
    val repsLabel: String,
    val totalReps: String,
    val addSet: String,
    val historyEmptyTitle: String,
    val historyEmptyText: String,
    val progress: String,
    val sevenDays: String,
    val thirtyDays: String,
    val allTime: String,
    val workouts: String,
    val exerciseFilter: String,
    val allExercises: String,
    val progressSummary: String,
    val workoutCount: String,
    val repetitions: String,
    val average: String,
    val record: String,
    val noWorkoutsPeriod: String,
    val repsByWorkout: String,
    val lastWorkoutsMax: (Int, Int) -> String,
    val graphEmpty: String,
    val totalRepsLine: (Int) -> String,
    val pullUpsLine: (Int) -> String,
    val dipsLine: (Int) -> String,
    val pushUpsLine: (Int) -> String,
    val inventory: String,
    val itemsCount: (Int) -> String,
    val coinsCount: (Int) -> String,
    val emptyInventory: String,
    val emptyInventoryHint: String,
    val quantity: (Int) -> String,
    val unequip: String,
    val equip: String,
    val sellFor: (Int) -> String,
    val achievementsTitle: String,
    val unlockedCount: (Int, Int) -> String,
    val setCollection: String,
    val forestGuardian: (Int, Int) -> String,
    val forestSetHint: String,
    val receivedReward: (Int) -> String,
    val reward: (Int) -> String,
    val streakReminderTitle: String,
    val streakReminderMessage: (Int) -> String,
    val workoutReminderTitle: String,
    val workoutReminderMessage: String,
    val reminderChannelName: String,
    val reminderChannelDescription: String,
    val updateNotificationTitle: String,
    val updateNotificationApkMessage: (String) -> String,
    val updateNotificationReleaseMessage: (String) -> String,
    val updateChannelName: String,
    val updateChannelDescription: String
)

fun strings(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.RU -> ruStrings
    AppLanguage.EN -> enStrings
}

fun ItemRarity.label(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> label
    AppLanguage.EN -> when (this) {
        ItemRarity.COMMON -> "COMMON"
        ItemRarity.UNCOMMON -> "UNCOMMON"
        ItemRarity.RARE -> "RARE"
        ItemRarity.EPIC -> "EPIC"
        ItemRarity.LEGENDARY -> "LEGENDARY"
        ItemRarity.MYTHIC -> "MYTHIC"
    }
}

fun ItemDefinition.name(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> name
    AppLanguage.EN -> when (id) {
        "worn_gloves" -> "Fighter Gloves"
        "iron_token" -> "Iron Token"
        "energy_drink" -> "Energy Drink"
        "lucky_charm" -> "Lucky Charm"
        "steel_bracer" -> "Steel Bracer"
        "crystal_core" -> "Crystal Core"
        "forest_armor" -> "Forest Guardian Armor"
        "traveler_boots" -> "Traveler Boots"
        "iron_helmet" -> "Iron Helmet"
        "legendary_crown" -> "Champion Crown"
        else -> name
    }
}

fun ItemDefinition.description(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> description
    AppLanguage.EN -> when (id) {
        "worn_gloves" -> "Basic gloves. Your first equipment item."
        "iron_token" -> "A common token found after training."
        "energy_drink" -> "A rare find. For now, a collectible item."
        "lucky_charm" -> "A small charm for future adventures."
        "steel_bracer" -> "A bracer for an experienced fighter."
        "crystal_core" -> "A very rare artifact with mysterious energy."
        "forest_armor" -> "Light armor infused with forest energy."
        "traveler_boots" -> "Comfortable boots for a long road."
        "iron_helmet" -> "A reliable helmet with a protective visor."
        "legendary_crown" -> "A legendary reward for those who do not quit."
        else -> description
    }
}

fun ShopCase.title(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> title
    AppLanguage.EN -> when (this) {
        ShopCase.BASIC -> "ROOKIE CASE"
        ShopCase.RARE -> "SEEKER CASE"
        ShopCase.EPIC -> "EPIC CASE"
    }
}

fun ShopCase.description(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> description
    AppLanguage.EN -> when (this) {
        ShopCase.BASIC -> "Mostly common and uncommon items"
        ShopCase.RARE -> "Better chance for rare and epic items"
        ShopCase.EPIC -> "High chance for epic and legendary gear"
    }
}

fun Achievement.title(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> title
    AppLanguage.EN -> when (id) {
        "first_workout" -> "First Step"
        "hundred_reps" -> "One Hundred"
        "streak_3" -> "Warm-Up"
        "streak_7" -> "Iron Will"
        "level_5" -> "Level 5"
        "xp_1000" -> "1000 XP"
        "pullups_100" -> "Pull-Up Master"
        "reps_500" -> "Iron Arms"
        "set_forest_guardian" -> "Forest Guardian"
        else -> title
    }
}

fun Achievement.description(language: AppLanguage): String = when (language) {
    AppLanguage.RU -> description
    AppLanguage.EN -> when (id) {
        "first_workout" -> "Complete your first workout"
        "hundred_reps" -> "Do 100 reps"
        "streak_3" -> "Reach a 3-day streak"
        "streak_7" -> "Reach a 7-day streak"
        "level_5" -> "Reach level 5"
        "xp_1000" -> "Earn 1000 XP"
        "pullups_100" -> "Do 100 pull-ups"
        "reps_500" -> "Do 500 reps"
        "set_forest_guardian" -> "Collect the full set: forest guardian armor, traveler boots, iron helmet, and steel bracer"
        else -> description
    }
}

private val ruStrings = AppStrings(
    tagline = "ТВОЙ ПУТЬ СИЛЫ",
    streak = { "СЕРИЯ $it ДН." },
    lastWorkoutToday = "Последняя тренировка: сегодня",
    lastWorkoutYesterday = "Последняя тренировка: вчера",
    lastWorkoutDate = { "Последняя тренировка: $it" },
    lastWorkoutNone = "Последняя тренировка: пока нет",
    history = "ИСТОРИЯ",
    achievements = "НАГРАДЫ",
    rewardsCount = { unlocked, total -> "НАГРАДЫ $unlocked/$total" },
    inventoryWithCount = { "ИНВЕНТАРЬ $it" },
    caseShop = "▣  МАГАЗИН КЕЙСОВ",
    startWorkout = "▶  НАЧАТЬ ТРЕНИРОВКУ",
    workoutMotivation = "КАЖДАЯ ТРЕНИРОВКА - ШАГ К НОВОМУ УРОВНЮ",
    yourHero = "ТВОЙ ГЕРОЙ",
    fighter = "Боец",
    level = { "УРОВЕНЬ $it" },
    experience = "ОПЫТ",
    totalEarned = { "Всего заработано: $it XP" },
    attributes = "ХАРАКТЕРИСТИКИ",
    stats = "ПОКАЗАТЕЛИ",
    arms = "РУКИ",
    armsSubtitle = "Сила и выносливость",
    shoulders = "ПЛЕЧИ",
    shouldersSubtitle = "Стабильность и мощь",
    back = "СПИНА",
    backSubtitle = "Подтягивания",
    chest = "ГРУДЬ",
    chestSubtitle = "Жимовые упражнения",
    workoutComparison = "СРАВНЕНИЕ ТРЕНИРОВОК",
    comparisonEmpty = "Заверши первую тренировку, чтобы начать отслеживать прогресс",
    newStats = "НОВАЯ СТАТИСТИКА",
    lastWorkout = "ПОСЛЕДНЯЯ ТРЕНИРОВКА",
    reps = { "$it повторений" },
    previous = { "Предыдущая: $it" },
    nextWorkoutDifference = "Следующая тренировка покажет разницу",
    comparedToPrevious = "к прошлой",
    workout = "ТРЕНИРОВКА",
    completed = "ЗАВЕРШЕНА",
    greatWork = "Отличная работа!",
    coinsForWorkout = { "+$it МОНЕТ ЗА ТРЕНИРОВКУ" },
    itemFound = "ПРЕДМЕТ НАЙДЕН!",
    noItemDropped = "В этот раз предмет не выпал. Повезёт в следующий раз!",
    backToMenu = "← ВЕРНУТЬСЯ В МЕНЮ",
    shop = "МАГАЗИН",
    shopSubtitle = "Покупай кейсы и собирай коллекцию экипировки",
    openCase = { "ОТКРЫТЬ · $it монет" },
    openingCase = "ОТКРЫВАЕМ КЕЙС",
    caseOpeningHint = "Редкость определяется...",
    itemReceived = "ПРЕДМЕТ ПОЛУЧЕН!",
    great = "ОТЛИЧНО",
    backButton = "НАЗАД",
    settings = "НАСТРОЙКИ",
    language = "ЯЗЫК",
    chooseLanguage = "Выбери язык приложения",
    notifications = "УВЕДОМЛЕНИЯ",
    remindWorkout = "Напоминать о тренировке",
    remindWorkoutSub = "Ежедневное напоминание в выбранное время",
    protectStreak = "Беречь тренировочную серию",
    protectStreakSub = "Если сегодня ещё не было тренировки, напомнит о серии",
    reminderTime = "Время напоминания",
    reminderTimeSub = "Каждый день, если тренировка ещё не выполнена",
    updates = "ОБНОВЛЕНИЯ",
    updateNotifications = "Уведомлять о новых версиях",
    updateNotificationsSub = "Проверять GitHub Releases в фоне примерно раз в сутки",
    checkUpdates = "ПРОВЕРИТЬ ОБНОВЛЕНИЯ",
    checking = "ПРОВЕРКА...",
    aboutApp = "О ПРИЛОЖЕНИИ",
    appVersion = "Версия приложения",
    projectGithub = "GitHub проекта",
    open = "Открыть",
    dangerZone = "ОПАСНАЯ ЗОНА",
    resetAllData = "СБРОСИТЬ ВСЕ ДАННЫЕ",
    settingsNote = "Настройки сохраняются на устройстве. Android может ограничивать точное время доставки уведомлений.",
    updatePrompt = "Нажми, чтобы проверить последнюю версию на GitHub",
    updateChecking = "Проверяем GitHub Releases...",
    updateLatest = "Установлена последняя доступная версия или релизы пока не опубликованы",
    updateAvailable = { "Доступно обновление: v$it" },
    updateFailed = "Не удалось проверить обновления. Проверь интернет и попробуй ещё раз.",
    notificationGranted = "Уведомления разрешены",
    notificationDenied = "Разреши уведомления в настройках Android, чтобы получать напоминания",
    resetTitle = "Сбросить весь прогресс?",
    resetText = "Будут удалены уровень, опыт, история тренировок, достижения, монеты и все предметы. Это действие нельзя отменить.",
    resetConfirm = "СБРОСИТЬ",
    cancel = "Отмена",
    later = "Позже",
    downloadApk = "Скачать APK",
    openRelease = "Открыть релиз",
    updateDialogTitle = "Доступно обновление",
    updateDialogText = { "Версия v$it уже опубликована на GitHub. Открыть страницу релиза?" },
    workoutExitTitle = "Выйти из тренировки?",
    workoutExitText = "Введённые подходы не будут сохранены.",
    exit = "Выйти",
    continueAction = "Продолжить",
    finishWorkoutTitle = "Завершить тренировку?",
    finishWorkoutText = "Проверь количество повторений. После подтверждения результат будет сохранён.",
    finish = "Завершить",
    pullUps = "ПОДТЯГИВАНИЯ",
    dips = "БРУСЬЯ",
    pushUps = "ОТЖИМАНИЯ",
    saving = "СОХРАНЕНИЕ...",
    finishWorkout = "ЗАВЕРШИТЬ ТРЕНИРОВКУ",
    repetitionsPlaceholder = "Повторения",
    repsLabel = "Повторы",
    totalReps = "Всего повторений",
    addSet = "+ ДОБАВИТЬ ПОДХОД",
    historyEmptyTitle = "Тренировок пока нет",
    historyEmptyText = "Проведи первую тренировку,\nи она появится здесь.",
    progress = "ПРОГРЕСС",
    sevenDays = "7 дней",
    thirtyDays = "30 дней",
    allTime = "Всё время",
    workouts = "ТРЕНИРОВКИ",
    exerciseFilter = "УПРАЖНЕНИЕ",
    allExercises = "Все",
    progressSummary = "СВОДКА ПРОГРЕССА",
    workoutCount = "Тренировки",
    repetitions = "Повторения",
    average = "В среднем",
    record = "Рекорд",
    noWorkoutsPeriod = "За этот период тренировок нет. Выбери другой период или проведи тренировку.",
    repsByWorkout = "ПОВТОРЕНИЯ ПО ТРЕНИРОВКАМ",
    lastWorkoutsMax = { count, max -> "Последние $count тренировок · максимум $max повторений" },
    graphEmpty = "График появится после первой тренировки.",
    totalRepsLine = { "Всего повторений: $it" },
    pullUpsLine = { "Подтягивания: $it" },
    dipsLine = { "Брусья: $it" },
    pushUpsLine = { "Отжимания: $it" },
    inventory = "ИНВЕНТАРЬ",
    itemsCount = { "ПРЕДМЕТОВ: $it" },
    coinsCount = { "$it МОНЕТ" },
    emptyInventory = "Инвентарь пуст",
    emptyInventoryHint = "Завершай тренировки - после них могут выпасть предметы.",
    quantity = { "Количество: $it" },
    unequip = "СНЯТЬ С ПЕРСОНАЖА",
    equip = "НАДЕТЬ НА ПЕРСОНАЖА",
    sellFor = { "ПРОДАТЬ ЗА $it монет" },
    achievementsTitle = "ДОСТИЖЕНИЯ",
    unlockedCount = { unlocked, total -> "ОТКРЫТО $unlocked ИЗ $total" },
    setCollection = "КОЛЛЕКЦИЯ СЕТОВ",
    forestGuardian = { collected, total -> "Страж леса · $collected/$total" },
    forestSetHint = "Собери все 4 предмета, чтобы открыть достижение и получить 300 XP.",
    receivedReward = { "ПОЛУЧЕНО · +$it XP" },
    reward = { "НАГРАДА · +$it XP" },
    streakReminderTitle = "Не теряй серию!",
    streakReminderMessage = { "Твоя серия - $it дн. Сделай тренировку сегодня, чтобы продолжить прогресс." },
    workoutReminderTitle = "Время тренировки",
    workoutReminderMessage = "Пора сделать ещё один шаг к своей цели в IronQuest.",
    reminderChannelName = "Напоминания IronQuest",
    reminderChannelDescription = "Напоминания о тренировках и тренировочной серии",
    updateNotificationTitle = "Доступно обновление IronQuest",
    updateNotificationApkMessage = { "Версия $it уже доступна. Нажми, чтобы открыть релиз и скачать APK." },
    updateNotificationReleaseMessage = { "Версия $it уже доступна на GitHub. Нажми, чтобы посмотреть релиз." },
    updateChannelName = "Обновления IronQuest",
    updateChannelDescription = "Уведомления о новых версиях приложения"
)

private val enStrings = AppStrings(
    tagline = "YOUR PATH OF STRENGTH",
    streak = { "STREAK $it D" },
    lastWorkoutToday = "Last workout: today",
    lastWorkoutYesterday = "Last workout: yesterday",
    lastWorkoutDate = { "Last workout: $it" },
    lastWorkoutNone = "Last workout: none yet",
    history = "HISTORY",
    achievements = "REWARDS",
    rewardsCount = { unlocked, total -> "REWARDS $unlocked/$total" },
    inventoryWithCount = { "INVENTORY $it" },
    caseShop = "▣  CASE SHOP",
    startWorkout = "▶  START WORKOUT",
    workoutMotivation = "EVERY WORKOUT IS A STEP TOWARD A NEW LEVEL",
    yourHero = "YOUR HERO",
    fighter = "Fighter",
    level = { "LEVEL $it" },
    experience = "EXPERIENCE",
    totalEarned = { "Total earned: $it XP" },
    attributes = "ATTRIBUTES",
    stats = "STATS",
    arms = "ARMS",
    armsSubtitle = "Strength and endurance",
    shoulders = "SHOULDERS",
    shouldersSubtitle = "Stability and power",
    back = "BACK",
    backSubtitle = "Pull-ups",
    chest = "CHEST",
    chestSubtitle = "Push exercises",
    workoutComparison = "WORKOUT COMPARISON",
    comparisonEmpty = "Finish your first workout to start tracking progress",
    newStats = "NEW STATS",
    lastWorkout = "LAST WORKOUT",
    reps = { "$it reps" },
    previous = { "Previous: $it" },
    nextWorkoutDifference = "The next workout will show the difference",
    comparedToPrevious = "vs previous",
    workout = "WORKOUT",
    completed = "COMPLETE",
    greatWork = "Great work!",
    coinsForWorkout = { "+$it COINS FOR WORKOUT" },
    itemFound = "ITEM FOUND!",
    noItemDropped = "No item dropped this time. Better luck next workout!",
    backToMenu = "← BACK TO MENU",
    shop = "SHOP",
    shopSubtitle = "Open cases and build your equipment collection",
    openCase = { "OPEN · $it coins" },
    openingCase = "OPENING CASE",
    caseOpeningHint = "Rolling rarity...",
    itemReceived = "ITEM RECEIVED!",
    great = "GREAT",
    backButton = "BACK",
    settings = "SETTINGS",
    language = "LANGUAGE",
    chooseLanguage = "Choose app language",
    notifications = "NOTIFICATIONS",
    remindWorkout = "Workout reminder",
    remindWorkoutSub = "Daily reminder at the selected time",
    protectStreak = "Protect workout streak",
    protectStreakSub = "Reminds you if you have not trained today",
    reminderTime = "Reminder time",
    reminderTimeSub = "Every day when the workout is not complete yet",
    updates = "UPDATES",
    updateNotifications = "Notify about new versions",
    updateNotificationsSub = "Check GitHub Releases in the background about once per day",
    checkUpdates = "CHECK FOR UPDATES",
    checking = "CHECKING...",
    aboutApp = "ABOUT APP",
    appVersion = "App version",
    projectGithub = "Project GitHub",
    open = "Open",
    dangerZone = "DANGER ZONE",
    resetAllData = "RESET ALL DATA",
    settingsNote = "Settings are saved on this device. Android may limit exact notification delivery times.",
    updatePrompt = "Tap to check the latest version on GitHub",
    updateChecking = "Checking GitHub Releases...",
    updateLatest = "The latest available version is installed, or releases are not published yet",
    updateAvailable = { "Update available: v$it" },
    updateFailed = "Could not check updates. Check your internet connection and try again.",
    notificationGranted = "Notifications allowed",
    notificationDenied = "Allow notifications in Android settings to receive reminders",
    resetTitle = "Reset all progress?",
    resetText = "Level, XP, workout history, achievements, coins, and all items will be deleted. This cannot be undone.",
    resetConfirm = "RESET",
    cancel = "Cancel",
    later = "Later",
    downloadApk = "Download APK",
    openRelease = "Open release",
    updateDialogTitle = "Update available",
    updateDialogText = { "Version v$it is published on GitHub. Open the release page?" },
    workoutExitTitle = "Exit workout?",
    workoutExitText = "Entered sets will not be saved.",
    exit = "Exit",
    continueAction = "Continue",
    finishWorkoutTitle = "Finish workout?",
    finishWorkoutText = "Check your reps. The result will be saved after confirmation.",
    finish = "Finish",
    pullUps = "PULL-UPS",
    dips = "DIPS",
    pushUps = "PUSH-UPS",
    saving = "SAVING...",
    finishWorkout = "FINISH WORKOUT",
    repetitionsPlaceholder = "Repetitions",
    repsLabel = "Reps",
    totalReps = "Total reps",
    addSet = "+ ADD SET",
    historyEmptyTitle = "No workouts yet",
    historyEmptyText = "Complete your first workout,\nand it will appear here.",
    progress = "PROGRESS",
    sevenDays = "7 days",
    thirtyDays = "30 days",
    allTime = "All time",
    workouts = "WORKOUTS",
    exerciseFilter = "EXERCISE",
    allExercises = "All",
    progressSummary = "PROGRESS SUMMARY",
    workoutCount = "Workouts",
    repetitions = "Reps",
    average = "Average",
    record = "Record",
    noWorkoutsPeriod = "No workouts in this period. Choose another period or complete a workout.",
    repsByWorkout = "REPS BY WORKOUT",
    lastWorkoutsMax = { count, max -> "Last $count workouts · max $max reps" },
    graphEmpty = "The graph will appear after your first workout.",
    totalRepsLine = { "Total reps: $it" },
    pullUpsLine = { "Pull-ups: $it" },
    dipsLine = { "Dips: $it" },
    pushUpsLine = { "Push-ups: $it" },
    inventory = "INVENTORY",
    itemsCount = { "ITEMS: $it" },
    coinsCount = { "$it COINS" },
    emptyInventory = "Inventory is empty",
    emptyInventoryHint = "Finish workouts - items can drop after them.",
    quantity = { "Quantity: $it" },
    unequip = "UNEQUIP FROM HERO",
    equip = "EQUIP TO HERO",
    sellFor = { "SELL FOR $it coins" },
    achievementsTitle = "ACHIEVEMENTS",
    unlockedCount = { unlocked, total -> "UNLOCKED $unlocked OF $total" },
    setCollection = "SET COLLECTION",
    forestGuardian = { collected, total -> "Forest Guardian · $collected/$total" },
    forestSetHint = "Collect all 4 items to unlock the achievement and earn 300 XP.",
    receivedReward = { "RECEIVED · +$it XP" },
    reward = { "REWARD · +$it XP" },
    streakReminderTitle = "Keep your streak!",
    streakReminderMessage = { "Your streak is $it days. Train today to keep your progress going." },
    workoutReminderTitle = "Workout time",
    workoutReminderMessage = "Time to take another step toward your IronQuest goal.",
    reminderChannelName = "IronQuest reminders",
    reminderChannelDescription = "Workout and streak reminders",
    updateNotificationTitle = "IronQuest update available",
    updateNotificationApkMessage = { "Version $it is available. Tap to open the release and download the APK." },
    updateNotificationReleaseMessage = { "Version $it is available on GitHub. Tap to view the release." },
    updateChannelName = "IronQuest updates",
    updateChannelDescription = "New app version notifications"
)
