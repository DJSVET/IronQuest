package com.example.ironquest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/* ===================================== ТЕМЫ ===================================== */

@Immutable
data class GamePalette(
    val bg: Color, val bgPattern: Color,
    val panel: Color, val panelHi: Color, val panelLo: Color, val inset: Color, val border: Color,
    val accent: Color, val onAccent: Color, val accent2: Color,
    val text: Color, val muted: Color,
    val gold: Color, val danger: Color, val good: Color,
    val skyTop: Color, val skyBottom: Color, val star: Color, val moon: Color,
    val mountainFar: Color, val mountainNear: Color,
    val ground: Color, val groundTop: Color, val groundDark: Color, val torchPole: Color,
)

private fun c(v: Long) = Color(v)

enum class AppTheme(val title: LText, val price: Int, val palette: GamePalette) {
    DUNGEON(LText("Подземелье", "Dungeon"), 0, GamePalette(
        bg = c(0xFF0B0F1E), bgPattern = c(0xFF111834),
        panel = c(0xFF18213A), panelHi = c(0xFF2C3B62), panelLo = c(0xFF0D1324), inset = c(0xFF0F1629), border = c(0xFF04060C),
        accent = c(0xFFB8FF5C), onAccent = c(0xFF10200A), accent2 = c(0xFF5CE1FF),
        text = c(0xFFEAF2FF), muted = c(0xFF8494B4),
        gold = c(0xFFFFD75A), danger = c(0xFFFF5C6C), good = c(0xFF6FE38A),
        skyTop = c(0xFF0A0E24), skyBottom = c(0xFF2A2F6A), star = c(0xFFE6ECFF), moon = c(0xFFF4F0C8),
        mountainFar = c(0xFF1C2350), mountainNear = c(0xFF141938),
        ground = c(0xFF2A2440), groundTop = c(0xFF4A3F6E), groundDark = c(0xFF1C182E), torchPole = c(0xFF5E3A1F),
    )),
    FOREST(LText("Лесная поляна", "Forest Glade"), 150, GamePalette(
        bg = c(0xFF0C1A12), bgPattern = c(0xFF12241A),
        panel = c(0xFF1A3324), panelHi = c(0xFF2F5A3A), panelLo = c(0xFF0B160F), inset = c(0xFF10231A), border = c(0xFF040A06),
        accent = c(0xFFA6E86A), onAccent = c(0xFF10200A), accent2 = c(0xFFE6C35A),
        text = c(0xFFEEF7E6), muted = c(0xFF8FAE92),
        gold = c(0xFFFFD75A), danger = c(0xFFFF6A5C), good = c(0xFF8FE86A),
        skyTop = c(0xFF0E2A2A), skyBottom = c(0xFF3F7A5A), star = c(0xFFF2FFE6), moon = c(0xFFFFF6C8),
        mountainFar = c(0xFF1F4A33), mountainNear = c(0xFF163826),
        ground = c(0xFF3A2A18), groundTop = c(0xFF5FA03A), groundDark = c(0xFF2A1E10), torchPole = c(0xFF5E3A1F),
    )),
    LAVA(LText("Вулкан", "Volcano"), 200, GamePalette(
        bg = c(0xFF180A0A), bgPattern = c(0xFF221010),
        panel = c(0xFF351612), panelHi = c(0xFF63291C), panelLo = c(0xFF140606), inset = c(0xFF220C0A), border = c(0xFF080202),
        accent = c(0xFFFF8A3C), onAccent = c(0xFF2A0C02), accent2 = c(0xFFFFD75A),
        text = c(0xFFFFF0E6), muted = c(0xFFC79A88),
        gold = c(0xFFFFD75A), danger = c(0xFFFF4A5A), good = c(0xFF9BE56A),
        skyTop = c(0xFF1A0606), skyBottom = c(0xFF7A2A14), star = c(0xFFFFC8A0), moon = c(0xFFFF9A5A),
        mountainFar = c(0xFF3A1210), mountainNear = c(0xFF240A08),
        ground = c(0xFF2A1410), groundTop = c(0xFFFF6A2A), groundDark = c(0xFF1A0A08), torchPole = c(0xFF3A2A28),
    )),
    FROST(LText("Ледяной пик", "Frost Peak"), 200, GamePalette(
        bg = c(0xFF08131F), bgPattern = c(0xFF0E1D2E),
        panel = c(0xFF15304A), panelHi = c(0xFF2C5C82), panelLo = c(0xFF071019), inset = c(0xFF0D2236), border = c(0xFF02060A),
        accent = c(0xFF8FE8FF), onAccent = c(0xFF06202A), accent2 = c(0xFFFFFFFF),
        text = c(0xFFEAF8FF), muted = c(0xFF8FB3C9),
        gold = c(0xFFFFE27A), danger = c(0xFFFF6A7A), good = c(0xFF8FF0C8),
        skyTop = c(0xFF0A1A2E), skyBottom = c(0xFF4A8AB8), star = c(0xFFFFFFFF), moon = c(0xFFE8FBFF),
        mountainFar = c(0xFF2A5070), mountainNear = c(0xFFB8E0F0),
        ground = c(0xFFDCEFF8), groundTop = c(0xFFFFFFFF), groundDark = c(0xFFA8CCE0), torchPole = c(0xFF5A6170),
    )),
    ROYAL(LText("Тронный зал", "Throne Hall"), 300, GamePalette(
        bg = c(0xFF120C22), bgPattern = c(0xFF1A1230),
        panel = c(0xFF291C46), panelHi = c(0xFF4D3880), panelLo = c(0xFF0C0718), inset = c(0xFF1C1234), border = c(0xFF05030A),
        accent = c(0xFFFFD75A), onAccent = c(0xFF2A1A02), accent2 = c(0xFFC49BFF),
        text = c(0xFFF6F0FF), muted = c(0xFFA898C8),
        gold = c(0xFFFFD75A), danger = c(0xFFFF5C7A), good = c(0xFF8FE86A),
        skyTop = c(0xFF120A2A), skyBottom = c(0xFF4A2A80), star = c(0xFFFFF1A6), moon = c(0xFFFFE27A),
        mountainFar = c(0xFF2A1A50), mountainNear = c(0xFF1C1238),
        ground = c(0xFF5A1A2A), groundTop = c(0xFFB8343A), groundDark = c(0xFF3A1018), torchPole = c(0xFFB8862A),
    )),
    GAMEBOY(LText("Ретро-консоль", "Retro Console"), 250, GamePalette(
        bg = c(0xFF0F380F), bgPattern = c(0xFF15421A),
        panel = c(0xFF306230), panelHi = c(0xFF5A8A28), panelLo = c(0xFF173F17), inset = c(0xFF1F4F1F), border = c(0xFF081C08),
        accent = c(0xFF9BBC0F), onAccent = c(0xFF0F380F), accent2 = c(0xFF8BAC0F),
        text = c(0xFFC8E08A), muted = c(0xFF8BAC0F),
        gold = c(0xFFD8F07A), danger = c(0xFFE8F0A0), good = c(0xFF9BBC0F),
        skyTop = c(0xFF306230), skyBottom = c(0xFF8BAC0F), star = c(0xFF9BBC0F), moon = c(0xFF9BBC0F),
        mountainFar = c(0xFF306230), mountainNear = c(0xFF0F380F),
        ground = c(0xFF306230), groundTop = c(0xFF9BBC0F), groundDark = c(0xFF0F380F), torchPole = c(0xFF0F380F),
    )),
    VOID(LText("Бездна", "The Abyss"), 400, GamePalette(
        bg = c(0xFF07040D), bgPattern = c(0xFF0F0818),
        panel = c(0xFF1A0F2A), panelHi = c(0xFF3B2160), panelLo = c(0xFF050208), inset = c(0xFF120A1E), border = c(0xFF000000),
        accent = c(0xFFFF4FD8), onAccent = c(0xFF2A0420), accent2 = c(0xFF9F6AFF),
        text = c(0xFFF5E6FF), muted = c(0xFF9C88B8),
        gold = c(0xFFFFD75A), danger = c(0xFFFF5C6C), good = c(0xFF7BFFC8),
        skyTop = c(0xFF05020A), skyBottom = c(0xFF3A0F5A), star = c(0xFFFF9AF0), moon = c(0xFFE0B8FF),
        mountainFar = c(0xFF1F0A35), mountainNear = c(0xFF120620),
        ground = c(0xFF1A0A28), groundTop = c(0xFF7B3FB8), groundDark = c(0xFF0A0412), torchPole = c(0xFF2A1838),
    ));

    companion object {
        fun fromName(name: String?): AppTheme = values().firstOrNull { it.name == name } ?: DUNGEON
    }
}

val LocalPalette = staticCompositionLocalOf { AppTheme.DUNGEON.palette }
val LocalHaptics = staticCompositionLocalOf { true }

/* ===================================== ШРИФТЫ ===================================== */

/** Press Start 2P — заголовки и цифры. */
val TitleFont = FontFamily(Font(R.font.pixel_title))

/** Tiny5 — основной текст (полная кириллица). */
val BodyFont = FontFamily(
    Font(R.font.pixel_body, FontWeight.Normal),
    Font(R.font.pixel_body_bold, FontWeight.Bold),
)

@Composable
fun PText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalPalette.current.text,
    size: TextUnit = 15.sp,
    bold: Boolean = false,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = BodyFont,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontSize = size,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        lineHeight = if (lineHeight == TextUnit.Unspecified) size * 1.25f else lineHeight,
    )
}

/** Пиксельный заголовок с жёсткой тенью. */
@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalPalette.current.text,
    size: TextUnit = 12.sp,
    align: TextAlign? = null,
    maxLines: Int = 2,
    shadow: Boolean = true,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontFamily = TitleFont,
        fontSize = size,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        lineHeight = size * 1.45f,
        style = TextStyle(shadow = if (shadow) Shadow(Color.Black.copy(alpha = 0.75f), Offset(3f, 3f), 0f) else null),
    )
}

/* ================================= ПИКСЕЛЬНЫЕ РАМКИ ================================= */

/** Прямоугольник со «срезанными» углами в 1 пиксель — классика пиксельных окон. */
fun DrawScope.stepRect(color: Color, x: Float, y: Float, w: Float, h: Float, p: Float) {
    if (w <= 0 || h <= 0) return
    drawRect(color, Offset(x + p, y), Size(w - 2 * p, h))
    drawRect(color, Offset(x, y + p), Size(w, h - 2 * p))
}

/** Окно RPG: тёмный контур, заливка, светлая фаска сверху-слева и тень снизу-справа. */
fun Modifier.pixelFrame(
    fill: Color,
    hi: Color,
    lo: Color,
    border: Color,
    px: Dp = 3.dp,
): Modifier = drawBehind {
    val p = px.toPx()
    val w = size.width
    val h = size.height
    stepRect(border, 0f, 0f, w, h, p)
    drawRect(fill, Offset(p, p), Size(w - 2 * p, h - 2 * p))
    drawRect(hi, Offset(p, p), Size(w - 2 * p, p))
    drawRect(hi, Offset(p, p), Size(p, h - 2 * p))
    drawRect(lo, Offset(p, h - 2 * p), Size(w - 2 * p, p))
    drawRect(lo, Offset(w - 2 * p, p), Size(p, h - 2 * p))
}

@Composable
fun Modifier.panelFrame(fill: Color? = null, highlight: Color? = null): Modifier {
    val pal = LocalPalette.current
    val f = fill ?: pal.panel
    return pixelFrame(f, highlight ?: lerp(f, Color.White, 0.14f), lerp(f, Color.Black, 0.35f), pal.border)
}

@Composable
fun Modifier.insetFrame(fill: Color? = null): Modifier {
    val pal = LocalPalette.current
    val f = fill ?: pal.inset
    return pixelFrame(f, lerp(f, Color.Black, 0.35f), lerp(f, Color.White, 0.08f), pal.border, 2.dp)
}

@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    fill: Color? = null,
    padding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.panelFrame(fill).padding(padding), content = content)
}

/* ===================================== КНОПКИ ===================================== */

@Composable
fun rememberHaptic(): (HapticFeedbackType) -> Unit {
    val haptic = LocalHapticFeedback.current
    val enabled = LocalHaptics.current
    return remember(haptic, enabled) { { type -> if (enabled) haptic.performHapticFeedback(type) } }
}

/** Объёмная пиксельная кнопка, которая «вдавливается» при нажатии. */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalPalette.current.accent,
    textColor: Color = LocalPalette.current.onAccent,
    enabled: Boolean = true,
    icon: String? = null,
    height: Dp = 54.dp,
    fontSize: TextUnit = 11.sp,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val pal = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth = 5.dp
    val press by animateDpAsState(if (pressed && enabled) depth else 0.dp, tween(50), label = "press")
    val haptic = rememberHaptic()
    val fill = if (enabled) color else lerp(pal.panelLo, pal.panel, 0.5f)
    val fg = if (enabled) textColor else pal.muted
    Box(
        modifier
            .heightIn(min = height)
            .semantics { contentDescription = text }
            .clickable(interaction, null, enabled = enabled, role = Role.Button) {
                haptic(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .drawBehind {
                val d = depth.toPx()
                val p = 3.dp.toPx()
                val pr = press.toPx()
                val w = size.width
                stepRect(pal.border, 0f, pr, w, size.height - pr, p)
                drawRect(lerp(fill, Color.Black, 0.45f), Offset(p, pr + p), Size(w - 2 * p, size.height - pr - 2 * p))
                drawRect(fill, Offset(p, pr + p), Size(w - 2 * p, size.height - d - 2 * p))
                drawRect(lerp(fill, Color.White, 0.35f), Offset(p, pr + p), Size(w - 2 * p, p))
                drawRect(lerp(fill, Color.White, 0.18f), Offset(p, pr + p), Size(p, size.height - d - 2 * p))
            }
            .padding(top = press, bottom = depth - press)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (content != null) {
                content()
            } else {
                if (icon != null) {
                    PixelIcon(icon, 22.dp, disabled = !enabled)
                    Spacer(Modifier.width(8.dp))
                }
                TitleText(text, color = fg, size = fontSize, shadow = false, maxLines = 1, align = TextAlign.Center)
            }
        }
    }
}

/** Квадратная кнопка-иконка 48dp. */
@Composable
fun PixelIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    contentDescription: String? = null,
    fill: Color? = null,
    badge: String? = null,
) {
    val pal = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "ib")
    val haptic = rememberHaptic()
    Box(
        modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
            .clickable(interaction, null, role = Role.Button) { haptic(HapticFeedbackType.TextHandleMove); onClick() }
            .panelFrame(fill ?: pal.panel),
        contentAlignment = Alignment.Center,
    ) {
        PixelIcon(icon, iconSize)
        if (badge != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .background(pal.danger)
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) { PText(badge, size = 10.sp, bold = true, color = Color.White) }
        }
    }
}

@Composable
fun PixelChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    color: Color = LocalPalette.current.accent,
) {
    val pal = LocalPalette.current
    val haptic = rememberHaptic()
    val fill = if (selected) color else pal.panel
    Row(
        modifier
            .heightIn(min = 40.dp)
            .clickable(role = Role.Tab) { haptic(HapticFeedbackType.TextHandleMove); onClick() }
            .pixelFrame(fill, lerp(fill, Color.White, 0.25f), lerp(fill, Color.Black, 0.35f), pal.border, 2.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            PixelIcon(icon, 18.dp)
            Spacer(Modifier.width(6.dp))
        }
        PText(text, color = if (selected) pal.onAccent else pal.text, size = 14.sp, bold = true, maxLines = 1)
    }
}

/* =================================== ПОЛОСЫ =================================== */

/** Сегментированная полоса прогресса с анимацией заполнения. */
@Composable
fun PixelBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = LocalPalette.current.accent,
    height: Dp = 14.dp,
    segments: Int = 10,
    track: Color = LocalPalette.current.inset,
) {
    val pal = LocalPalette.current
    val anim by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "bar")
    Box(
        modifier
            .height(height)
            .drawBehind {
                val p = 2.dp.toPx()
                stepRect(pal.border, 0f, 0f, size.width, size.height, p)
                drawRect(track, Offset(p, p), Size(size.width - 2 * p, size.height - 2 * p))
                val fw = (size.width - 2 * p) * anim
                if (fw > 0f) {
                    drawRect(color, Offset(p, p), Size(fw, size.height - 2 * p))
                    drawRect(lerp(color, Color.White, 0.45f), Offset(p, p), Size(fw, p))
                    drawRect(lerp(color, Color.Black, 0.3f), Offset(p, size.height - 2 * p), Size(fw, p))
                }
                if (segments > 1) {
                    val step = (size.width - 2 * p) / segments
                    for (i in 1 until segments) {
                        drawRect(pal.border.copy(alpha = 0.55f), Offset(p + i * step - p / 2, p), Size(p / 2, size.height - 2 * p))
                    }
                }
            }
    )
}

/* ================================== ВЕРХНЯЯ ПАНЕЛЬ ================================== */

@Composable
fun PixelTopBar(
    title: String,
    icon: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            PixelIconButton("back", onBack, contentDescription = tr("Назад", "Back"))
            Spacer(Modifier.width(12.dp))
        }
        if (icon != null) {
            PixelIcon(icon, 28.dp)
            Spacer(Modifier.width(8.dp))
        }
        TitleText(title, Modifier.weight(1f), size = 12.sp, maxLines = 1)
        trailing()
    }
}

/** Счётчик монет с анимацией изменения. */
@Composable
fun CoinCounter(coins: Int, modifier: Modifier = Modifier) {
    val pal = LocalPalette.current
    val shown by animateIntAsState(coins, tween(600), label = "coins")
    Row(
        modifier
            .heightIn(min = 40.dp)
            .insetFrame()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PixelIcon("coin", 20.dp)
        Spacer(Modifier.width(6.dp))
        TitleText("$shown", color = pal.gold, size = 11.sp, shadow = false, maxLines = 1)
    }
}

@Composable
fun SectionHeader(title: String, icon: String? = null, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            PixelIcon(icon, 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        TitleText(title, Modifier.weight(1f), size = 10.sp, color = LocalPalette.current.accent, maxLines = 1)
        trailing()
    }
}

/* ===================================== ДИАЛОГ ===================================== */

@Composable
fun PixelDialog(
    onDismiss: () -> Unit,
    title: String? = null,
    icon: String? = null,
    dismissible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible),
    ) {
        val anim = remember { Animatable(0.8f) }
        LaunchedEffect(Unit) { anim.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 500f)) }
        Column(
            Modifier
                .padding(20.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .graphicsLayer { scaleX = anim.value; scaleY = anim.value; alpha = ((anim.value - 0.8f) / 0.2f).coerceIn(0f, 1f) }
                .panelFrame()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        PixelIcon(icon, 28.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    TitleText(title, size = 12.sp, color = LocalPalette.current.accent)
                }
                Spacer(Modifier.height(14.dp))
            }
            content()
        }
    }
}

/** Диалог подтверждения в пиксельном стиле. */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirm: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: String? = null,
    danger: Boolean = false,
    cancel: String = tr("Отмена", "Cancel"),
) {
    val pal = LocalPalette.current
    PixelDialog(onDismiss, title, icon) {
        PText(text, color = pal.muted, size = 16.sp)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton(cancel, onDismiss, Modifier.weight(1f), color = pal.panelHi, textColor = pal.text, height = 48.dp, fontSize = 9.sp)
            PixelButton(confirm, onConfirm, Modifier.weight(1f), color = if (danger) pal.danger else pal.accent,
                textColor = if (danger) Color.White else pal.onAccent, height = 48.dp, fontSize = 9.sp)
        }
    }
}

/* ===================================== ТОСТЫ ===================================== */

data class GameToast(val id: Long, val icon: String, val title: String, val subtitle: String, val color: Color)

/** Всплывающие уведомления сверху (достижения, покупки). */
@Composable
fun BoxScope.ToastHost(toasts: SnapshotStateList<GameToast>) {
    val current = toasts.firstOrNull()
    LaunchedEffect(current?.id) {
        if (current != null) {
            delay(2600)
            toasts.remove(current)
        }
    }
    AnimatedVisibility(
        visible = current != null,
        modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp),
        enter = slideInVertically { -it * 2 } + fadeIn(),
        exit = slideOutVertically { -it * 2 } + fadeOut(),
    ) {
        val t = current ?: return@AnimatedVisibility
        val pal = LocalPalette.current
        Row(
            Modifier
                .sizeIn(maxWidth = 420.dp)
                .fillMaxWidth()
                .clickable { toasts.remove(t) }
                .pixelFrame(pal.panel, t.color, lerp(pal.panel, Color.Black, 0.4f), pal.border)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PixelIcon(t.icon, 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                TitleText(t.title, color = t.color, size = 9.sp, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                PText(t.subtitle, color = pal.text, size = 14.sp, maxLines = 2)
            }
        }
    }
}

/** Фон экрана: цвет темы + разреженный пиксельный узор. */
@Composable
fun Modifier.gameBackground(): Modifier {
    val pal = LocalPalette.current
    return drawBehind {
        drawRect(pal.bg)
        val step = 28.dp.toPx()
        val p = 3.dp.toPx()
        var row = 0
        var y = 0f
        while (y < size.height) {
            var x = if (row % 2 == 0) 0f else step / 2
            while (x < size.width) {
                drawRect(pal.bgPattern, Offset(x, y), Size(p, p))
                x += step
            }
            y += step / 2
            row++
        }
    }
}

@Composable
fun ProvideGameTheme(theme: AppTheme, language: AppLanguage, haptics: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalPalette provides theme.palette,
        LocalLanguage provides language,
        LocalHaptics provides haptics,
        content = content,
    )
}

/** Метка-плашка (например «НАДЕТО», «×2»). */
@Composable
fun Tag(text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = LocalPalette.current.bg) {
    Box(
        modifier
            .pixelFrame(color, lerp(color, Color.White, 0.3f), lerp(color, Color.Black, 0.3f), LocalPalette.current.border, 2.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) { PText(text, color = textColor, size = 12.sp, bold = true, maxLines = 1) }
}

@Composable
fun StatPill(icon: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier.insetFrame().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        PixelIcon(icon, 16.dp)
        Spacer(Modifier.width(4.dp))
        PText(value, color = color, size = 14.sp, bold = true, maxLines = 1)
    }
}

/** Пустое состояние с иконкой. */
@Composable
fun EmptyState(icon: String, title: String, text: String, modifier: Modifier = Modifier) {
    val pal = LocalPalette.current
    PixelPanel(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            PixelIcon(icon, 56.dp)
            Spacer(Modifier.height(12.dp))
            TitleText(title, size = 11.sp, align = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            PText(text, color = pal.muted, align = TextAlign.Center)
        }
    }
}
