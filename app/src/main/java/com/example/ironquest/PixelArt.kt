package com.example.ironquest

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/* ================================ ДЕКОДЕР СПРАЙТОВ ================================ */

object PixelCache {
    private val bitmaps = HashMap<String, ImageBitmap>()
    private val parsed = HashMap<String, List<String>>()

    fun rows(data: String): List<String> = parsed.getOrPut(data) { data.split('|') }

    fun bitmap(key: String, build: () -> ImageBitmap): ImageBitmap = synchronized(bitmaps) {
        bitmaps.getOrPut(key, build)
    }
}

private fun toBitmap(w: Int, h: Int, pixels: IntArray): ImageBitmap =
    Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888).asImageBitmap()

private fun spriteBitmap(data: String, color: (Char) -> Int?): ImageBitmap {
    val rows = PixelCache.rows(data)
    val h = rows.size
    val w = rows.maxOf { it.length }
    val px = IntArray(w * h)
    rows.forEachIndexed { y, row ->
        row.forEachIndexed { x, ch -> if (ch != '.') px[y * w + x] = color(ch) ?: 0 }
    }
    return toBitmap(w, h, px)
}

private val iconPalette: Map<Char, Int> by lazy { PixelArtData.ICON_PALETTE.mapValues { it.value.toInt() } }

/**
 * Ключи иконок:
 *  "trophy" или "ui:trophy" — интерфейс,
 *  "ex:pullup" — упражнения,
 *  "mu:arms" — мышцы,
 *  "item:head_crown:gold" — форма предмета + материал.
 */
fun iconBitmap(key: String): ImageBitmap? {
    val parts = key.split(':')
    return when {
        parts.size == 1 -> PixelArtData.UI[key]?.let { d -> PixelCache.bitmap("ui:$key") { spriteBitmap(d) { iconPalette[it] } } }
        parts[0] == "ui" -> iconBitmap(parts[1])
        parts[0] == "ex" -> (PixelArtData.EXERCISES[parts[1]] ?: PixelArtData.UI[parts[1]])?.let { d ->
            PixelCache.bitmap(key) { spriteBitmap(d) { iconPalette[it] } }
        }
        parts[0] == "chest" && parts.size >= 2 -> PixelArtData.UI["chest"]?.let { d ->
            val mat = PixelArtData.MATERIALS[parts[1]] ?: PixelArtData.MATERIALS.getValue("leather")
            PixelCache.bitmap(key) {
                spriteBitmap(d) { ch ->
                    when (ch) {
                        'n' -> mat[0].toInt(); 'N' -> mat[1].toInt(); 'm' -> mat[2].toInt()
                        'y' -> mat[3].toInt()
                        else -> iconPalette[ch]
                    }
                }
            }
        }
        parts[0] == "mu" -> PixelArtData.MUSCLES[parts[1]]?.let { d -> PixelCache.bitmap(key) { spriteBitmap(d) { iconPalette[it] } } }
        parts[0] == "item" && parts.size >= 3 -> PixelArtData.ITEMS[parts[1]]?.let { d ->
            val mat = PixelArtData.MATERIALS[parts[2]] ?: PixelArtData.MATERIALS.getValue("iron")
            PixelCache.bitmap(key) {
                spriteBitmap(d) { ch ->
                    when (ch) {
                        'a' -> mat[0].toInt(); 'b' -> mat[1].toInt(); 'c' -> mat[2].toInt(); 'd' -> mat[3].toInt()
                        else -> iconPalette[ch]
                    }
                }
            }
        }
        else -> null
    }
}

fun ItemDefinition.iconKey(): String = "item:$shape:$material"

private val grayscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun PixelIcon(
    key: String,
    size: Dp,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    alpha: Float = 1f,
    contentDescription: String? = null,
) {
    val bmp = remember(key) { iconBitmap(key) }
    if (bmp == null) {
        Box(modifier.size(size))
        return
    }
    Image(
        bitmap = bmp,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        filterQuality = FilterQuality.None,
        alpha = if (disabled) alpha * 0.45f else alpha,
        colorFilter = if (disabled) grayscale else null,
    )
}

@Composable
fun ItemIcon(item: ItemDefinition, size: Dp, modifier: Modifier = Modifier, disabled: Boolean = false) =
    PixelIcon(item.iconKey(), size, modifier, disabled)

/* ===================================== ГЕРОЙ ===================================== */

val HAIR_COLORS: List<Pair<Long, Long>> = listOf(
    0xFF6B3E26 to 0xFF4A2A18, // каштан
    0xFF2A2438 to 0xFF14101C, // чёрный
    0xFFE8C25A to 0xFFB8862A, // блонд
    0xFFC0502A to 0xFF7A2A16, // рыжий
    0xFFE0E4EC to 0xFF9AA3AD, // седой
    0xFF5C8CFF to 0xFF2F4FA8, // синий
    0xFF6FD15A to 0xFF2F7A35, // зелёный
    0xFFFF6BB0 to 0xFFB03A78, // розовый
)

val SKIN_COLORS: List<Pair<Long, Long>> = listOf(
    0xFFF2C29B to 0xFFD39A77,
    0xFFFFDDC2 to 0xFFE8B898,
    0xFFC98E62 to 0xFFA06A44,
    0xFF8A5A3A to 0xFF603C24,
)

private const val OUTLINE = 0xFF14101C.toInt()

/** Собирает спрайт героя 32x32 из базовых слоёв и экипировки. */
fun heroBitmap(equipped: Map<ItemSlot, ItemDefinition>, look: HeroLook, frame: Int, blink: Boolean): ImageBitmap {
    val sig = buildString {
        ItemSlot.values().forEach { append(equipped[it]?.id ?: "-").append(',') }
        append(look.hair).append(look.skin).append(frame).append(blink)
    }
    return PixelCache.bitmap("hero:$sig") {
        val w = 32
        val h = 32
        val px = IntArray(w * h)
        val base = PixelArtData.HERO_PALETTE.mapValues { it.value.toInt() }.toMutableMap()
        HAIR_COLORS.getOrNull(look.hair)?.let { (a, b) -> base['h'] = a.toInt(); base['H'] = b.toInt() }
        SKIN_COLORS.getOrNull(look.skin)?.let { (a, b) -> base['k'] = a.toInt(); base['K'] = b.toInt() }

        fun stamp(layer: String, dy: Int, item: ItemDefinition? = null) {
            val data = PixelArtData.HERO[layer] ?: return
            val mat = item?.let { PixelArtData.MATERIALS[it.material] }
            PixelCache.rows(data).forEachIndexed { y, row ->
                val ty = y + dy
                if (ty !in 0 until h) return@forEachIndexed
                row.forEachIndexed { x, ch ->
                    if (ch == '.') return@forEachIndexed
                    val c = when {
                        mat != null && ch == 'a' -> mat[0].toInt()
                        mat != null && ch == 'b' -> mat[1].toInt()
                        mat != null && ch == 'c' -> mat[2].toInt()
                        mat != null && ch == 'd' -> mat[3].toInt()
                        else -> base[ch]
                    } ?: return@forEachIndexed
                    px[ty * w + x] = c
                }
            }
        }

        val up = if (frame % 2 == 1) 1 else 0
        equipped[ItemSlot.BACK]?.let { stamp("back_cape${frame % 2}", up, it) }
        stamp("base_legs", 0)
        val feet = equipped[ItemSlot.FEET]
        if (feet != null) stamp(feet.shape, 0, feet) else stamp("base_shoes", 0)
        stamp("base_torso", up)
        stamp("base_arms", up)
        equipped[ItemSlot.BODY]?.let { stamp(it.shape, up, it) }
        equipped[ItemSlot.HANDS]?.let { stamp(it.shape, up, it) }
        equipped[ItemSlot.TRINKET]?.let { stamp("trinket_amulet", up, it) }
        stamp("base_head", up)
        if (blink) stamp("base_blink", up)
        val head = equipped[ItemSlot.HEAD]
        if (head?.shape != "head_hood" && head?.shape != "head_wizard") stamp("base_hair", up)
        head?.let { stamp(it.shape, up, it) }
        equipped[ItemSlot.WEAPON]?.let { stamp(it.shape, up, it) }

        // внешний контур
        val out = px.copyOf()
        for (y in 0 until h) for (x in 0 until w) {
            if (px[y * w + x] != 0) continue
            val n = (x > 0 && px[y * w + x - 1] != 0) || (x < w - 1 && px[y * w + x + 1] != 0) ||
                (y > 0 && px[(y - 1) * w + x] != 0) || (y < h - 1 && px[(y + 1) * w + x] != 0)
            if (n) out[y * w + x] = OUTLINE
        }
        toBitmap(w, h, out)
    }
}

private data class Spark(val id: Long, val x: Float, val y: Float, val color: Color)

/**
 * Анимированный пиксельный герой: дыхание, моргание, развевающийся плащ, прыжок по тапу.
 * Обновляется несколько раз в секунду (не 60 fps) — экономит батарею.
 */
@Composable
fun HeroView(
    equipped: Map<ItemSlot, ItemDefinition>,
    look: HeroLook,
    modifier: Modifier = Modifier,
    pixel: Dp = 6.dp,
    celebrate: Boolean = false,
    onTap: (() -> Unit)? = null,
) {
    var frame by remember { mutableIntStateOf(0) }
    var blink by remember { mutableStateOf(false) }
    val jump = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val sparks = remember { mutableStateListOf<Spark>() }
    val sparkProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(if (celebrate) 300 else 600)
            frame = (frame + 1) % 2
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2200, 4600))
            blink = true
            delay(130)
            blink = false
        }
    }
    LaunchedEffect(celebrate) {
        if (celebrate) {
            while (true) {
                jump.animateTo(-1f, tween(220, easing = FastOutSlowInEasing))
                jump.animateTo(0f, tween(220, easing = LinearEasing))
                delay(260)
            }
        }
    }

    fun burst() {
        sparks.clear()
        val colors = listOf(Color(0xFFFFD75A), Color(0xFFB8FF5C), Color(0xFF5CE1FF), Color(0xFFFF8AB0))
        repeat(10) { i ->
            sparks.add(Spark(System.nanoTime() + i, Random.nextFloat() * 2f - 1f, -Random.nextFloat() * 0.8f - 0.3f, colors.random()))
        }
        scope.launch {
            sparkProgress.snapTo(0f)
            sparkProgress.animateTo(1f, tween(650))
            sparks.clear()
        }
    }

    val spriteSize = pixel * 32
    val bmp = heroBitmap(equipped, look, frame, blink)
    Box(
        modifier
            .size(spriteSize)
            .then(
                if (onTap != null) Modifier.clickable(remember { MutableInteractionSource() }, null) {
                    scope.launch {
                        jump.animateTo(-1f, tween(160, easing = FastOutSlowInEasing))
                        jump.animateTo(0f, tween(200))
                    }
                    burst()
                    onTap()
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        // тень
        Canvas(Modifier.fillMaxSize()) {
            val p = pixel.toPx()
            val lift = -jump.value
            val shrink = (lift * 3).toInt()
            val shadow = Color.Black.copy(alpha = 0.35f - lift * 0.15f)
            val cx = spriteSize.toPx() / 2f
            val y = 28.5f * p
            val half = (8 - shrink).coerceAtLeast(4)
            drawRect(shadow, Offset(cx - half * p, y), Size(half * 2 * p, p))
            drawRect(shadow, Offset(cx - (half - 2) * p, y + p), Size((half - 2) * 2 * p, p))
        }
        Image(
            bitmap = bmp,
            contentDescription = null,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .size(spriteSize)
                .graphicsLayer { translationY = jump.value * pixel.toPx() * 5f },
        )
        if (sparks.isNotEmpty()) {
            Canvas(Modifier.fillMaxSize()) {
                val t = sparkProgress.value
                val p = pixel.toPx()
                sparks.forEach { s ->
                    val x = size.width / 2 + s.x * size.width * 0.45f * t
                    val y = size.height * 0.45f + s.y * size.height * 0.5f * t + t * t * p * 6
                    drawRect(s.color.copy(alpha = 1f - t), Offset(x, y), Size(p, p))
                }
            }
        }
    }
}

/* =================================== СЦЕНА ГЕРОЯ =================================== */

/** Пиксельный задник: небо с градиентом-ступеньками, звёзды, горы, земля и факелы. */
@Composable
fun HeroScenery(palette: GamePalette, modifier: Modifier = Modifier, cell: Dp = 4.dp) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(220)
            tick++
        }
    }
    val stars = remember { List(28) { Triple(Random.nextFloat(), Random.nextFloat() * 0.55f, Random.nextInt(3)) } }
    Canvas(modifier) {
        val c = cell.toPx()
        val cols = (size.width / c).toInt() + 1
        val rows = (size.height / c).toInt() + 1
        val groundRow = (rows * 0.74f).toInt()
        // небо ступенчатыми полосами
        val bands = 6
        for (b in 0 until bands) {
            val y0 = (groundRow * b / bands)
            val y1 = (groundRow * (b + 1) / bands)
            drawRect(lerp(palette.skyTop, palette.skyBottom, b / (bands - 1f)), Offset(0f, y0 * c), Size(size.width, (y1 - y0) * c + 1))
        }
        // звёзды
        stars.forEachIndexed { i, (sx, sy, kind) ->
            val on = (tick + i) % 7 != 0
            val alpha = if (on) 0.9f else 0.25f
            val x = (sx * cols).toInt() * c
            val y = (sy * groundRow).toInt() * c
            drawRect(palette.star.copy(alpha = alpha), Offset(x, y), Size(c, c))
            if (kind == 0 && on && (tick / 3 + i) % 4 == 0) {
                drawRect(palette.star.copy(alpha = 0.5f), Offset(x - c, y), Size(c, c))
                drawRect(palette.star.copy(alpha = 0.5f), Offset(x + c, y), Size(c, c))
                drawRect(palette.star.copy(alpha = 0.5f), Offset(x, y - c), Size(c, c))
                drawRect(palette.star.copy(alpha = 0.5f), Offset(x, y + c), Size(c, c))
            }
        }
        // луна
        val mx = cols - 9
        val my = 3
        for (dy in -3..3) for (dx in -3..3) {
            if (dx * dx + dy * dy <= 10) drawRect(palette.moon, Offset((mx + dx) * c, (my + dy) * c), Size(c, c))
        }
        for (dy in -3..3) for (dx in -3..3) {
            if ((dx - 2) * (dx - 2) + (dy + 1) * (dy + 1) <= 6 && dx * dx + dy * dy <= 10) drawRect(lerp(palette.moon, palette.skyTop, 0.75f), Offset((mx + dx) * c, (my + dy) * c), Size(c, c))
        }
        // горы (две гряды)
        for (layer in 0..1) {
            val color = if (layer == 0) palette.mountainFar else palette.mountainNear
            val amp = if (layer == 0) 7 else 5
            val base = groundRow - if (layer == 0) 3 else 0
            for (x in 0 until cols) {
                val v = kotlin.math.sin(x * (0.21 + layer * 0.13) + layer * 2.0) * 0.6 + kotlin.math.sin(x * 0.07 + layer) * 0.4
                val hgt = ((v + 1) / 2 * amp).toInt() + 2
                drawRect(color, Offset(x * c, (base - hgt) * c), Size(c + 0.5f, (hgt + 1) * c))
            }
        }
        // земля
        drawRect(palette.ground, Offset(0f, groundRow * c), Size(size.width, size.height - groundRow * c))
        drawRect(palette.groundTop, Offset(0f, groundRow * c), Size(size.width, c))
        for (r in groundRow + 2 until rows step 3) {
            val shift = if ((r / 3) % 2 == 0) 0 else 3
            for (x in -shift until cols step 6) {
                drawRect(palette.groundDark, Offset((x + shift) * c, r * c), Size(c * 5, c * 0.5f))
            }
        }
        // факелы
        listOf(3, cols - 5).forEach { tx ->
            val ty = groundRow - 8
            drawRect(palette.torchPole, Offset(tx * c, ty * c), Size(c, 8 * c))
            drawRect(palette.torchPole, Offset((tx - 1) * c, ty * c), Size(3 * c, c))
            val flick = (tick + tx) % 3
            val flame = listOf(Color(0xFFFF6A2A), Color(0xFFFFB13A), Color(0xFFFFE27A))
            drawRect(flame[0], Offset((tx - 1) * c, (ty - 2) * c), Size(3 * c, 2 * c))
            drawRect(flame[1], Offset(tx * c, (ty - 3 - (if (flick == 0) 1 else 0)) * c), Size(c, (2 + (if (flick == 0) 1 else 0)) * c))
            drawRect(flame[2], Offset(tx * c, (ty - 1) * c), Size(c, c))
            if (flick == 2) drawRect(flame[1].copy(alpha = 0.7f), Offset((tx + if (tick % 2 == 0) 1 else -1) * c, (ty - 4) * c), Size(c, c))
            // свечение
            drawRect(Color(0xFFFFB13A).copy(alpha = 0.06f + flick * 0.02f), Offset((tx - 4) * c, (ty - 5) * c), Size(9 * c, 9 * c))
        }
    }
}

/** Цвет-шаблон: слегка темнее/светлее для бликов UI. */
fun Color.darker(f: Float = 0.35f) = lerp(this, Color.Black, f)
fun Color.lighter(f: Float = 0.3f) = lerp(this, Color.White, f)
fun Color.argb(): Int = toArgb()
