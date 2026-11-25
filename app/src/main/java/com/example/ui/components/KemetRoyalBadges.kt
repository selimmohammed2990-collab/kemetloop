package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.RewardsState
import com.example.data.UserPreferencesStore
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Royal Vector Coin engraved with a Pharaonic Ankh symbol.
 */
@Composable
fun KemetGoldCoinIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "عملة كيميت الذهبية" }
    ) {
        val w = this.size.width
        val h = this.size.height
        val radius = minOf(w, h) / 2f
        val center = Offset(w / 2f, h / 2f)

        // Outer metallic gold disc
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RoyalGoldLight, RoyalGold, ObsidianDeep),
                center = Offset(w * 0.35f, h * 0.35f),
                radius = radius * 1.4f
            ),
            radius = radius,
            center = center
        )

        // Engraved inner ring
        drawCircle(
            color = ObsidianDeep.copy(alpha = 0.72f),
            radius = radius * 0.80f,
            center = center,
            style = Stroke(width = radius * 0.11f)
        )

        // Engraved Pharaonic Ankh symbol inside the coin
        val strokeW = (radius * 0.16f).coerceAtLeast(1.6f)
        // Ankh top oval loop
        val loopW = radius * 0.44f
        val loopH = radius * 0.48f
        drawOval(
            color = ObsidianDeep,
            topLeft = Offset(center.x - loopW / 2f, center.y - radius * 0.54f),
            size = Size(loopW, loopH),
            style = Stroke(width = strokeW)
        )
        // Ankh vertical stem
        val neckY = center.y - radius * 0.06f
        drawLine(
            color = ObsidianDeep,
            start = Offset(center.x, neckY),
            end = Offset(center.x, center.y + radius * 0.56f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        // Ankh horizontal crossbar
        drawLine(
            color = ObsidianDeep,
            start = Offset(center.x - radius * 0.38f, neckY + radius * 0.08f),
            end = Offset(center.x + radius * 0.38f, neckY + radius * 0.08f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun KemetGoldCoinPill(
    coins: Long,
    modifier: Modifier = Modifier,
    showLabelSuffix: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = CircleShape,
        color = MidnightElevated,
        border = BorderStroke(1.2.dp, RoyalGold),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KemetGoldCoinIcon(size = 20.dp)
            Text(
                text = if (showLabelSuffix) "$coins عملة" else "$coins",
                style = MaterialTheme.typography.labelLarge,
                color = RoyalGoldLight,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

/**
 * Custom Royal Crown Vector Badge for KemetLoop Pro subscribers & active 3-day trial.
 */
@Composable
fun ProCrownBadge(
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
    labelText: String = "Pro",
    iconSize: Dp = 20.dp
) {
    val crownGraphic: @Composable () -> Unit = {
        Canvas(
            modifier = Modifier
                .size(iconSize)
                .semantics { contentDescription = "وسام التاج الملكي Pro" }
        ) {
            val w = this.size.width
            val h = this.size.height

            val crownPath = Path().apply {
                moveTo(w * 0.12f, h * 0.78f)
                lineTo(w * 0.08f, h * 0.32f)
                lineTo(w * 0.33f, h * 0.54f)
                lineTo(w * 0.50f, h * 0.18f)
                lineTo(w * 0.67f, h * 0.54f)
                lineTo(w * 0.92f, h * 0.32f)
                lineTo(w * 0.88f, h * 0.78f)
                close()
            }

            drawPath(
                path = crownPath,
                brush = Brush.verticalGradient(listOf(RoyalGoldLight, RoyalGold))
            )
            drawPath(
                path = crownPath,
                color = ObsidianDeep,
                style = Stroke(width = w * 0.055f, join = StrokeJoin.Round)
            )

            // Crown base band
            drawRoundRect(
                color = RoyalGoldLight,
                topLeft = Offset(w * 0.12f, h * 0.80f),
                size = Size(w * 0.76f, h * 0.12f)
            )

            // Royal sapphire jewel in center peak
            drawCircle(
                color = ElectricAzure,
                radius = w * 0.085f,
                center = Offset(w * 0.50f, h * 0.61f)
            )
            // Side pearl tips
            drawCircle(color = RoyalGoldLight, radius = w * 0.065f, center = Offset(w * 0.09f, h * 0.28f))
            drawCircle(color = RoyalGoldLight, radius = w * 0.075f, center = Offset(w * 0.50f, h * 0.15f))
            drawCircle(color = RoyalGoldLight, radius = w * 0.065f, center = Offset(w * 0.91f, h * 0.28f))
        }
    }

    if (showLabel) {
        Surface(
            shape = CircleShape,
            color = DeepSkyBlue.copy(alpha = 0.75f),
            border = BorderStroke(1.dp, RoyalGold),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                crownGraphic()
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall,
                    color = RoyalGoldLight,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            crownGraphic()
        }
    }
}

/**
 * Custom Golden Star Checkmark Verification Seal for Business VIP merchants & partners.
 */
@Composable
fun VerifiedGoldStarBadge(
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
    labelText: String = "توثيق ذهبي",
    iconSize: Dp = 20.dp
) {
    val sealGraphic: @Composable () -> Unit = {
        Canvas(
            modifier = Modifier
                .size(iconSize)
                .semantics { contentDescription = "شارة التوثيق الذهبية" }
        ) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val outerR = minOf(w, h) * 0.48f
            val innerR = outerR * 0.76f
            val points = 8
            val starPath = Path()

            for (i in 0 until points * 2) {
                val angle = (i * PI / points) - (PI / 2.0)
                val r = if (i % 2 == 0) outerR else innerR
                val px = cx + (r * cos(angle)).toFloat()
                val py = cy + (r * sin(angle)).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()

            drawPath(
                path = starPath,
                brush = Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold))
            )
            drawPath(
                path = starPath,
                color = ObsidianDeep.copy(alpha = 0.55f),
                style = Stroke(width = outerR * 0.08f, join = StrokeJoin.Round)
            )

            // Crisp checkmark inside the golden star
            val checkPath = Path().apply {
                moveTo(cx - outerR * 0.32f, cy + outerR * 0.02f)
                lineTo(cx - outerR * 0.06f, cy + outerR * 0.28f)
                lineTo(cx + outerR * 0.36f, cy - outerR * 0.24f)
            }
            drawPath(
                path = checkPath,
                color = ObsidianDeep,
                style = Stroke(
                    width = (outerR * 0.22f).coerceAtLeast(2f),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }

    if (showLabel) {
        Surface(
            shape = CircleShape,
            color = RoyalGold.copy(alpha = 0.20f),
            border = BorderStroke(1.dp, RoyalGold),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sealGraphic()
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall,
                    color = RoyalGoldLight,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            sealGraphic()
        }
    }
}

/**
 * Animated Pulsing Gift Box button for the Home top bar.
 */
@Composable
fun AnimatedGiftBoxButton(
    hasUnclaimedGift: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gift_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (hasUnclaimedGift) 0.94f else 1f,
        targetValue = if (hasUnclaimedGift) 1.09f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (hasUnclaimedGift) 0.35f else 0.18f,
        targetValue = if (hasUnclaimedGift) 0.85f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gift_glow"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("home_animated_gift_btn"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = RoyalGold.copy(alpha = if (hasUnclaimedGift) 0.24f else 0.14f),
            border = BorderStroke(1.5.dp, RoyalGoldLight.copy(alpha = glowAlpha)),
            modifier = Modifier
                .size(42.dp)
                .scale(pulseScale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = "هدية الترحيب الملكية",
                    tint = RoyalGoldLight,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        if (hasUnclaimedGift) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(EmeraldVerified)
            )
        }
    }
}

/**
 * Luxury Celebratory Welcome Gift Modal showing 100 Gold Coins + 3-Day Free Pro Trial.
 */
@Composable
fun WelcomeGiftCelebrationDialog(
    rewardsState: RewardsState,
    onClaimGift: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = RoyalGold.copy(alpha = 0.22f),
                    border = BorderStroke(2.dp, RoyalGoldLight),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            tint = RoyalGoldLight,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Text(
                    text = "هدية الترحيب الملكية",
                    style = MaterialTheme.typography.headlineSmall,
                    color = RoyalGoldLight,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianDeep.copy(alpha = 0.85f),
                    border = BorderStroke(1.5.dp, RoyalGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "هدية الترحيب: 100 عملة ذهبية + 3 أيام تجربة Pro مجاناً",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrispWhite,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KemetGoldCoinPill(coins = 100L, showLabelSuffix = true)
                            ProCrownBadge(showLabel = true, labelText = "3 أيام Pro")
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GiftBenefitRow("إزالة الإعلانات فوراً خلال فترة التجربة")
                    GiftBenefitRow("ترجمة صوتية وبالكاميرا مفتوحة بلا حدود")
                    GiftBenefitRow("تفعيل وسام التاج الملكي Pro بجانب اسمك")
                }

                if (rewardsState.welcomeGiftClaimed) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldVerified.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, EmeraldVerified),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (rewardsState.proTrialActive) {
                                "تم استلام هديتك بنجاح ✓ تجربة Pro نشطة (متبقي ${rewardsState.proTrialDaysRemaining} أيام)"
                            } else {
                                "تم استلام هدية الترحيب الخاصة بك بنجاح ✓"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldVerified,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!rewardsState.welcomeGiftClaimed) {
                Button(
                    onClick = onClaimGift,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("claim_welcome_gift_btn")
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استلم هديتك الآن", fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("استمتع بتجربة KemetLoop", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!rewardsState.welcomeGiftClaimed) {
                TextButton(onClick = onDismiss) {
                    Text("لاحقاً", color = CoolSilver)
                }
            }
        }
    )
}

@Composable
private fun GiftBenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = EmeraldVerified,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = CoolSilver)
    }
}

/**
 * 7-Day Daily Gift Streak Bar on Home displaying 7 numbered mini gift boxes.
 */
@Composable
fun DailyGiftStreakBar(
    rewardsState: RewardsState,
    onClaimDailyGift: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDay = rewardsState.nextClaimDayNumber
    val canClaim = rewardsState.canClaimTodayGift

    GlassLuxuryCard(
        borderColor = RoyalGold.copy(alpha = 0.65f),
        containerColor = MidnightGlass,
        contentPadding = PaddingValues(14.dp),
        modifier = modifier.testTag("daily_gift_streak_bar")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CardGiftcard,
                        contentDescription = null,
                        tint = RoyalGoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "مكافآت الدخول اليومي (7 أيام)",
                            style = MaterialTheme.typography.titleSmall,
                            color = CrispWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (canClaim) {
                                "هدية اليوم $activeDay جاهزة للاستلام بضغطة واحدة!"
                            } else {
                                "تم فتح هدية اليوم (${rewardsState.dailyStreakCount}/7) — عد غداً للمزيد!"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canClaim) RoyalGoldLight else CoolSilver
                        )
                    }
                }

                Button(
                    onClick = onClaimDailyGift,
                    enabled = canClaim,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep,
                        disabledContainerColor = MidnightGlassSoft,
                        disabledContentColor = CoolSilver
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("claim_daily_gift_btn")
                ) {
                    Text(
                        text = if (canClaim) "افتح هدية اليوم" else "تم الاستلام ✓",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val days = (1..7).toList()
                items(days, key = { "streak_day_$it" }) { day ->
                    val rewardCoins = UserPreferencesStore.DAILY_REWARD_COINS[day - 1]
                    val isClaimed = day <= rewardsState.dailyStreakCount &&
                        !(rewardsState.dailyStreakCount >= 7 && canClaim)
                    val isClaimableToday = canClaim && day == activeDay

                    val borderColor = when {
                        isClaimableToday -> RoyalGoldLight
                        isClaimed -> EmeraldVerified
                        else -> RoyalGold.copy(alpha = 0.28f)
                    }
                    val bgColor = when {
                        isClaimableToday -> RoyalGold.copy(alpha = 0.22f)
                        isClaimed -> EmeraldVerified.copy(alpha = 0.14f)
                        else -> ObsidianDeep.copy(alpha = 0.72f)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bgColor,
                        border = BorderStroke(if (isClaimableToday) 1.6.dp else 1.dp, borderColor),
                        modifier = Modifier
                            .width(74.dp)
                            .defaultMinSize(minHeight = 68.dp)
                            .then(
                                if (isClaimableToday) {
                                    Modifier.clickable(onClick = onClaimDailyGift)
                                } else {
                                    Modifier
                                }
                            )
                            .testTag("daily_gift_day_$day")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "اليوم $day",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isClaimableToday) RoyalGoldLight else CrispWhite,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isClaimed) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "مفتوح",
                                    tint = EmeraldVerified,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.CardGiftcard,
                                    contentDescription = "صندوق هدية اليوم $day",
                                    tint = if (isClaimableToday) RoyalGoldLight else CoolSilver,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                KemetGoldCoinIcon(size = 13.dp)
                                Text(
                                    text = "+$rewardCoins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isClaimed) EmeraldVerified else RoyalGoldLight,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
