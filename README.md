<div align="center">

# ⚔️ IronQuest

### Your workouts are the quest. Your body is the hero.

**A pixel-art RPG fitness tracker for Android** — every workout is a battle, every rep is XP,<br>
and every streak brings you closer to legendary loot.

<br>

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Min SDK](https://img.shields.io/badge/min%20SDK-24%20(Android%207.0)-B8FF5C?style=for-the-badge)
![Version](https://img.shields.io/badge/version-1.0-FFD75A?style=for-the-badge)
![Offline](https://img.shields.io/badge/100%25-offline-5CE1FF?style=for-the-badge)

<br>

[**⬇️ Download the latest release**](https://github.com/DJSVET/IronQuest/releases/latest)

</div>

---

## 🌟 At a glance

<table>
<tr>
<td align="center" width="25%"><h3>🧙</h3><b>Living pixel hero</b><br><sub>Breathes, blinks, wears every piece of gear you equip</sub></td>
<td align="center" width="25%"><h3>🏋️</h3><b>15 + custom exercises</b><br><sub>Reps, seconds or minutes — each one trains its own stats</sub></td>
<td align="center" width="25%"><h3>🎰</h3><b>Chest roulette</b><br><sub>CS-style spinning reel with haptic ticks and rarity rays</sub></td>
<td align="center" width="25%"><h3>🌙</h3><b>Rest days</b><br><sub>Your streak survives your days off — up to 5 per week</sub></td>
</tr>
<tr>
<td align="center"><h3>🎒</h3><b>75 items</b><br><sub>6 rarities, stat bonuses and passive perks</sub></td>
<td align="center"><h3>🛡</h3><b>9 gear sets</b><br><sub>Full-set bonuses from Rookie to the mythic Void</sub></td>
<td align="center"><h3>🏆</h3><b>75 achievements</b><br><sub>Every one with a progress bar and XP + coin rewards</sub></td>
<td align="center"><h3>🎨</h3><b>7 themes</b><br><sub>Recolor the whole app and your hero's world</sub></td>
</tr>
</table>

---

## 📑 Table of contents

1. [The Hero](#-the-hero)
2. [Workouts](#-workouts)
3. [Exercises](#-exercises)
4. [Attributes & Muscles](#-attributes--muscles)
5. [XP, Levels & Ranks](#-xp-levels--ranks)
6. [Streaks, Rest Days & Freezes](#-streaks-rest-days--freezes)
7. [Loot & Inventory](#-loot--inventory)
8. [Gear Sets](#-gear-sets)
9. [The Shop](#-the-shop)
10. [Achievements](#-achievements)
11. [Chronicle (Workout History)](#-chronicle-workout-history)
12. [Settings](#%EF%B8%8F-settings)
13. [Design & Feel](#-design--feel)
14. [Getting Started](#-getting-started)
15. [Tech Stack](#-tech-stack)
16. [Project Structure](#-project-structure)
17. [Credits & Licenses](#-credits--licenses)

---

## 🧙 The Hero

Your home screen is a **living pixel-art stage**: a starry night sky with twinkling stars, a glowing moon, layered mountains, a stone floor and two flickering torches. In the middle stands **your hero**.

- 🫁 **Animated idle** — the hero breathes, blinks and their cape sways in the wind.
- 👆 **Interactive** — tap the hero to make them jump, burst into sparkles and shout a motivational line (*"One more set!"*, *"Loot won't farm itself!"*…).
- 🛡 **Gear is visible on the character** — helmets, armor, gloves, boots, capes, weapons and amulets are drawn directly on the sprite in their material colors.
- 🔲 **7 equipment slots** around the hero: **Head · Body · Hands · Feet · Cape · Weapon · Trinket**. Tap any slot to jump straight to matching items in your bag.
- ✏️ **Customization** — name your hero and pick from **8 hair colors** and **4 skin tones** (free).
- 🎖 **Ranks** — your title grows with your level, from *Recruit* to *Titan*.
- ⚡ **Power score** — one number that sums up your hero: attributes + gear bonuses + level.
- ⭐ **Favorite achievements** — up to 3 badges displayed above your hero.

The HUD shows your **level badge, rank, streak 🔥, coins and XP bar**. Below the stage you'll find:

- 📅 **Hero Week** — a Monday-to-Sunday strip marking each day as *trained ✓*, *rest day 🌙*, *frozen ❄*, *missed ✕* or *today*.
- 🕒 **Last workout** — date and time (*"today, 18:42"*, *"yesterday"*, *"12.10.2026 (4 days ago)"*), plus current and best streak.
- 🧪 **Active boosts** — potions, clovers, magnets and gear perks currently in effect.
- ⚔️ **Last Battle** — your latest workout compared to the previous one (volume up / down) with every set listed.

Navigation lives in a pixel bottom bar: **Hero · Bag · ⚔ FIGHT · Shop · Awards**. The big center **FIGHT** button pulses until you've trained today.

---

## 🗡 Workouts

A workout screen built for sweaty thumbs: big targets, one-tap logging, zero friction.

| Feature | Description |
|---|---|
| ➕ **One-tap logging** | Stepper `[−] value [+]` with quick adds (**+1 / +5 / +10** reps, **+10s / +30s / +60s** for timed exercises) and a reset button. Tap the number to type it in. |
| 🧠 **Smart defaults** | The input is pre-filled with your last result, and a hint shows *"Last time: 12 · 10 · 8"*. |
| 🏷 **Set chips** | Logged sets appear as bouncy chips — tap one to edit or delete it. |
| ⏳ **Rest timer** | Starts automatically after each set (off / 30 / 60 / 90 / 120 s). Vibrates when rest is over; add +15 s or skip. |
| ⏱ **Stopwatch** | For timed exercises like the plank — start, hold, stop, and the time is filled in for you. |
| 🔀 **Add on the fly** | Add any exercise in the middle of a workout from a pixel picker. |
| ⭐ **Live XP preview** | The total XP (including potions and gear perks) updates as you log. |
| 🕐 **Workout timer** | Elapsed time is shown in the top bar and saved with the workout. |
| 🛑 **Safe exits** | Confirmations before leaving or finishing, so you never lose logged sets. |

### 🏆 Victory screen

Finishing a workout triggers a full victory sequence:

- A bouncing **"VICTORY!"** banner and your hero **celebrating** in front of rotating light rays.
- **Animated XP and coin counters**, with every applied bonus listed (potion ×2, gear +%, magnet ×3, clover).
- **Level bar** with a flashing **LEVEL UP!** (+25 coins per level).
- **Attribute gains** for Strength, Agility, Endurance and Vitality.
- A **shaking loot chest** — tap it to open and watch your item pop out in its rarity color.
- Any **new achievements** unlocked by the workout.

---

## 📖 Exercises

### Built-in exercises

| | Exercise | Unit | Trains |
|---|---|---|---|
| 🧗 | Pull-ups | reps | STR ×3 · END ×1 |
| 🤸 | Dips | reps | STR ×3 · END ×1 |
| 💪 | Push-ups | reps | STR ×2 · END ×2 |
| 🦵 | Squats | reps | END ×2 · STR ×1 · AGI ×1 |
| 🚶 | Lunges | reps | AGI ×2 · STR ×1 · END ×1 |
| 🧱 | Plank | seconds | VIT ×3 · END ×1 |
| 🌀 | Crunches | reps | VIT ×3 |
| 🦿 | Leg Raises | reps | VIT ×3 · STR ×1 |
| 💥 | Burpees | reps | AGI ×2 · END ×2 · STR ×1 |
| ⭐ | Jumping Jacks | reps | AGI ×2 · END ×1 |
| 🪢 | Jump Rope | reps | AGI ×2 · END ×2 |
| 🏃 | Running | minutes | END ×3 · AGI ×1 |
| 🏋️ | Biceps Curls | reps | STR ×3 |
| 🔔 | Kettlebell Swings | reps | STR ×2 · END ×1 · AGI ×1 |
| 🌉 | Glute Bridge | reps | VIT ×2 · STR ×1 |

Every exercise has its own **pixel pictogram**, XP rate and list of targeted muscles. Pick which ones appear in your workouts in the **Exercises** screen.

### ✏️ Custom exercises

Create your own exercise in seconds:

- **Name** and **icon** (19 pixel icons to choose from)
- **Unit** — reps, seconds or minutes
- **Difficulty** — Easy / Normal / Hard (affects XP and stat points)
- **Main attribute** and optional **secondary attribute**
- **Targeted muscles**
- A **live preview** shows exactly how much XP and how many stat points 10 reps (or 60 s / 10 min) will give you.

Deleting a custom exercise only archives it, so your history and stats stay intact.

---

## 💪 Attributes & Muscles

Four classic RPG attributes, each with **its own level and progress bar**:

| | Attribute | Grown by |
|---|---|---|
| ✊ | **Strength (STR)** | pulling and pushing — pull-ups, dips, curls |
| 🪶 | **Agility (AGI)** | jumps, lunges and explosive moves |
| ❤️ | **Endurance (END)** | high reps and cardio |
| 🛡 | **Vitality (VIT)** | core work, holds and balance |

- Points come from your training volume, weighted by each exercise's profile.
- **Gear and set bonuses** are shown separately as *+N*.
- Tap an attribute to see a hint and the best exercises for it.
- **Muscle mastery** — Arms, Shoulders, Chest, Back, Core and Legs each level up independently, displayed with a pixel body map.
- Stats are calculated from your entire history, so workouts logged before an update still count.

---

## ⭐ XP, Levels & Ranks

- XP is earned per rep / second / minute depending on the exercise and its difficulty.
- Level 1 → 2 costs **100 XP**, and every next level costs **10 XP more**.
- Every level up grants **+25 coins**.
- Gear perks, set bonuses and potions multiply your XP.

| Level | Rank |
|---|---|
| 1 | Recruit |
| 5 | Fighter |
| 10 | Warrior |
| 15 | Veteran |
| 20 | Knight |
| 30 | Champion |
| 40 | Legend |
| 50 | Titan |

---

## 🔥 Streaks, Rest Days & Freezes

Consistency is the real boss fight — and IronQuest is fair about it.

### 🔥 Streak
Counts consecutive training days. Today never breaks your streak until the day is over.

### 🌙 Rest days
- On a rest day **your streak won't break** even if you don't train — and training on a rest day still counts.
- You start with **1 rest day per week** (Sunday by default).
- Unlock up to **5 rest days per week** in the shop: **150 → 250 → 400 → 600** coins.
- Choose the exact weekdays yourself — from the home screen, the shop or settings.
- Reminders stay quiet on your rest days.

### ❄️ Streak freezes
- A consumable from the shop (hold up to 3).
- If you miss training days, freezes are **used automatically** on your next launch and your streak survives.
- Frozen days are marked with a snowflake in the week strip.

---

## 🎒 Loot & Inventory

### 🎁 Loot drops
After each workout there's a chance to find an item. **Bigger workouts mean better odds** — from 35% for a short session up to 90% for 100+ reps — and better rarities. Luck perks and the Luck Clover boost it further.

### 💎 Rarities

| Rarity | Sell price |
|---|---|
| ⚪ Common | 5 |
| 🟢 Uncommon | 12 |
| 🔵 Rare | 25 |
| 🟣 Epic | 60 |
| 🟡 Legendary | 150 |
| 🔴 Mythic | 350 |

### 🧰 Item types
- **Equipment** — helmets, hoods, crowns, wizard hats · tunics, plate armor, robes · gloves, gauntlets, bracers · shoes, boots, greaves · capes · swords, axes, hammers, staffs, dumbbells · amulets and rings.
- **Collectibles** — tokens, crystals, gems, scrolls, feathers, cores and more.

Equipment grants **attribute bonuses** and passive **perks**: **+% XP, +% coins, +% luck**.

### 🎒 The Bag
- Pixel grid with rarity-colored frames, item counts, **E** marks for equipped gear and lock icons.
- Filters: **All · Gear · Loot** or by equipment slot; sorting by **rarity, name or count**.
- Item card with a big icon, stats, perks, description and set progress.
- Actions: **Equip / Unequip · Lock · Sell one · Sell all copies**.
- **Sell all duplicates** with one tap — locked items and your last equipped copy are always kept.

---

## 🛡 Gear Sets

Wear a full set to unlock its bonus:

| Set | Pieces | Full-set bonus |
|---|---|---|
| 🎽 **Rookie** | Rookie Headband · Rookie Tunic · Fighter Gloves · Rookie Sneakers · Wooden Sword | +1 END · +1 VIT · +5% XP |
| ⚙️ **Iron Legion** | Legionnaire Helm · Vanguard Armor · Iron Gauntlets · Iron Greaves · Legion Hammer | +3 VIT · +1 STR |
| 🌲 **Forest Guardian** | Iron Helmet · Forest Guardian Armor · Steel Bracer · Traveler Boots · Forest Guardian Cloak | +2 VIT · +2 END · +5% luck |
| 🏅 **Athlete** | Athlete Headband · Athlete Tank · Spartan Gloves · Athlete Sneakers · Golden Dumbbell | +2 AGI · +2 END · +10% coins |
| ❄️ **Frost Wanderer** | Frost Hood · Rime Robe · Blizzard Bracer · Chill Boots · Snowstorm Staff | +3 END · +8% luck |
| 🌙 **Night Champion** | Champion Crown · Night Armor · Ember Gloves · Swift Boots · Midnight Blade | +3 STR · +10% XP |
| 🔥 **Phoenix Flame** | Horned Flame Helm · Flame Plate · Heat Bracers · Cinder Boots · Phoenix Wings · Phoenix Axe | +2 STR · +2 AGI · +15% coins |
| 👑 **Royal** | King's Circlet · Royal Armor · Royal Gauntlets · Royal Greaves · Royal Mantle · King's Sword | +2 to all · +10% XP · +10% coins |
| 🌌 **The Void** *(mythic)* | Void Hat · Void Robe · Void Cloak · Void Staff | +3 to all · +15% XP · +10% luck |

The **Sets** tab shows which pieces you're missing (as dark silhouettes), and a completed set can be **equipped with one tap**.

<details>
<summary><b>🧾 Standalone gear & collectibles</b></summary>

<br>

**Standalone gear:** Guardian Helm · Ranger Hood · Bone Helm · Storm Boots · Crimson Cape · Storm Cloak · Iron Dumbbell · Steel Axe · Storm Hammer · Apprentice Robe

**Trinkets:** Lucky Charm (+3% luck) · Champion Charm (+8% XP) · Phoenix Amulet (+15% coins) · Jade Ring (+5% luck) · Gold Ring (+12% coins) · Abyss Ring (+10% XP, +10% coins, +5% luck)

**Collectibles:** Iron Token · Energy Drink · Sage Scroll · Lucky Feather · Sun Crystal · Ruby of Might · Crystal Core · Champion Badge · Amethyst of Patience · Golden Token · Dragon Scroll · Ancient Crystal · Void Core

</details>

---

## 🛒 The Shop

Four tabs: **Chests · Goods · Upgrades · Themes**.

### 🧰 Chests

| Chest | Price | Odds (Common → Mythic) |
|---|---|---|
| 🎁 **Daily Gift** | **Free**, once a day | Rookie Chest odds |
| 🟫 Rookie Chest | 30 | 55 · 27 · 13 · 4 · 1 · 0 % |
| 🔷 Seeker Chest | 75 | 20 · 30 · 28 · 17 · 5 · 0 % |
| 🟪 Epic Chest | 150 | 0 · 10 · 25 · 40 · 22 · 3 % |
| 🌌 Void Chest | 400 | 0 · 0 · 10 · 35 · 40 · 15 % |

Odds are shown in-app as a color bar for every chest.

#### 🎰 The opening roulette
Opening a chest launches a **CS-style reel**: dozens of items race past a golden marker, slow down with a **haptic click for every item**, and stop on your prize. The item then **pops out in rays of its rarity color**, with its stats, perks and set info. **Skip** and **Open again** buttons are always at hand. The item is saved *before* the animation starts, so closing the screen never loses loot.

### 🧪 Goods

| Item | Price | Effect |
|---|---|---|
| 🏷 **Deal of the Day** | −40% | A random rare-to-legendary gear piece, new every day |
| 🧪 **XP Potion** | 60 | ×2 XP on your next workout |
| 🍀 **Luck Clover** | 70 | Guaranteed loot with better rarity next workout |
| 🧲 **Coin Magnet** | 40 | ×3 coins on your next workout |
| ❄️ **Streak Freeze** | 90 | Auto-saves your streak if you miss a day |

### ⬆️ Upgrades
- Extra **rest days** — up to 5 per week.

### 🎨 Themes
Each theme recolors **every screen and your hero's scene** — with a live preview in the shop.

| Theme | Price |
|---|---|
| 🏰 Dungeon | Free |
| 🌲 Forest Glade | 150 |
| 🌋 Volcano | 200 |
| 🏔 Frost Peak | 200 |
| 🕹 Retro Console *(4-color handheld style)* | 250 |
| 👑 Throne Hall | 300 |
| 🌌 The Abyss | 400 |

---

## 🏆 Achievements

**75 achievements** across 8 categories, each with a **progress bar** (e.g. *37/100*) and a reward in **XP and coins**, granted automatically and only once.

| Category | Examples |
|---|---|
| ⚔️ **Workouts** | First Step · Unbending (10) · Marathoner (25) · Hundred Battles (100) · Living Legend (250) |
| 🔥 **Streaks** | Warm-Up (3 days) · Iron Will (7) · Month of Might (30) · Eternal Fire (100) |
| 💪 **Volume** | One Hundred · Titan (1,000 reps) · Ten Thousand · Boss Raid (300+ reps in one workout) |
| 🏋️ **Exercises** | Pull-Up Master · Bar Overlord (1,000 pull-ups) · The Rock (1 hour of planks) · All-Rounder · Inventor |
| ⭐ **Growth** | Level 5 → Titan (50) · 20,000 XP · Specialist · Harmony (all attributes 10+) |
| 🎒 **Collection** | Collector · Treasure Keeper · Legendary Find · Mythic Find · Chest Hunter · Fully Geared |
| 🛡 **Sets** | Complete each of the 9 gear sets |
| ✨ **Special** | Early Bird · Night Owl · Weekend Warrior · Ice Armor · Designer · Right to Rest |

- Pop-up toasts when you unlock something, plus a summary on the victory screen.
- Filters: **In progress · Unlocked · All · Sets**, and by category.
- Star up to **3 favorites** to show them next to your hero.

---

## 📜 Chronicle (Workout History)

- Periods: **7 days · 30 days · All time**.
- Filter by **any exercise** you've ever logged.
- Summary: battles, volume, XP, average, record, minutes trained.
- Pixel **bar chart** of your last 10 workouts (latest one highlighted in gold).
- Detailed cards: date, weekday, duration, XP, coins and every set of every exercise.

---

## ⚙️ Settings

- 🧙 **Hero** — name, look, rest days
- 🌍 **Language** — Русский / English
- 🔔 **Notifications** — daily workout reminder, streak protection (silent on rest days), reminder time
- 📳 **Haptics** — button feedback, roulette ticks, rest-timer alerts
- ⬆️ **Updates** — background checks of GitHub Releases with update notifications, manual check, direct APK download
- 💀 **Danger zone** — full progress reset

---

## 🎨 Design & Feel

- **Pure pixel-art UI** — beveled RPG windows, 3D buttons that physically press down, segmented progress bars, stepped corners.
- **Pixel fonts** — *Press Start 2P* for titles and *Tiny5* for text, both with full Cyrillic support.
- **Hand-drawn art in code** — the hero, gear layers, 50+ UI icons, item icons in 16 materials and 15 exercise pictograms are stored as pixel data and rendered without blur on any screen.
- **Animations everywhere** — screen transitions, bouncing counters, chest shakes, rarity rays, pulsing buttons, achievement toasts.
- **Battery-friendly** — scene and hero animations tick a few times per second instead of redrawing at 60 fps.
- **Accessible touch targets** — 44–58 dp buttons, primary actions in the thumb zone.
- **Edge-to-edge** layout with a custom pixel app icon (adaptive + monochrome).

---

## 🚀 Getting Started

### 📲 Install the APK
1. Open [**Releases**](https://github.com/DJSVET/IronQuest/releases/latest) and download the APK from **Assets**.
2. Allow installation from unknown sources if Android asks.
3. Installing over an older version keeps all your progress.

**Requires Android 7.0 (API 24) or newer.**

### 🛠 Build from source

**Requirements:** Android Studio (recent), JDK 11+, Android SDK 36.

```bash
git clone https://github.com/DJSVET/IronQuest.git
cd IronQuest
./gradlew assembleDebug
```

The APK will be in `app/build/outputs/apk/debug/`.

---

## 🧩 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose · Material3 · custom pixel components |
| Storage | DataStore Preferences (fully offline, on-device) |
| Background | WorkManager (update checks) · AlarmManager (reminders) |
| Graphics | Pixel sprites stored as data and rendered to bitmaps with nearest-neighbor scaling |
| Localization | Russian & English |

All data stays on your device. Saves from previous versions are migrated automatically.

---

## 🗂 Project Structure

```
app/src/main/java/com/example/ironquest/
├── MainActivity.kt         Navigation, bottom bar, achievement rewards
├── HomeScreen.kt           Hero stage, week strip, attributes, last battle
├── WorkoutScreen.kt        Workout logging and the victory screen
├── ShopScreen.kt           Shop, chest roulette, themes
├── InventoryScreen.kt      Bag and item details
├── AchievementsScreen.kt   Achievements and gear sets
├── WorkoutHistory.kt       Chronicle (history & analytics)
├── ExercisesScreen.kt      Exercise catalog and custom exercise editor
├── SettingsScreen.kt       Settings
├── PlayerData.kt           Player progress persistence (DataStore)
├── WorkoutData.kt          Workout history storage & migration
├── ProgressSystem.kt       Levels, attributes, streaks, rest days, achievements
├── ItemSystem.kt           Items, rarities, sets, chests, consumables
├── Exercises.kt            Exercise definitions
├── PixelUi.kt              Pixel UI kit and themes
├── PixelArt.kt             Sprite renderer, animated hero, scenery
├── PixelArtData.kt         Pixel art data (icons, hero layers, materials)
├── Localization.kt         Languages and app preferences
├── ReminderSystem.kt       Workout & streak reminders
└── ReleaseUpdate*.kt       GitHub release update checker
```

---

## 📄 Credits & Licenses

- **Fonts:** [Press Start 2P](https://fonts.google.com/specimen/Press+Start+2P) and [Tiny5](https://fonts.google.com/specimen/Tiny5) — SIL Open Font License 1.1.
- **Pixel art:** the hero, gear, icons and app icon were drawn specifically for IronQuest.

<div align="center">

<br>

**Every rep counts. Every streak matters. Every hero starts as a Recruit.**

⚔️ *Start your quest today.* ⚔️

</div>
