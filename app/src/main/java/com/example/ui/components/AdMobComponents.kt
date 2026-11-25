package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.SubscriptionPlan
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import kotlinx.coroutines.delay

@Composable
fun AdMobBannerSlot(
    activePlan: SubscriptionPlan,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlan != SubscriptionPlan.FREE_STARTER) return

    val bannerUnitId = stringResource(R.string.admob_banner_id_test)

    GlassLuxuryCard(
        modifier = modifier.testTag("admob_banner_slot"),
        cornerRadius = 18.dp,
        borderColor = RoyalGold.copy(alpha = 0.3f),
        containerColor = MidnightGlassSoft,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoyalGold.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "AdMob",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGoldLight,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "مساحة إعلانية متوازنة للباقة المجانية",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CrispWhite
                    )
                    Text(
                        text = "Unit: $bannerUnitId",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoolSilver,
                        maxLines = 1
                    )
                }
            }
            TextButton(onClick = onUpgradeClick) {
                Text("إزالة الإعلانات", style = MaterialTheme.typography.labelMedium, color = RoyalGoldLight)
            }
        }
    }
}

@Composable
fun AdMobRewardedCallout(
    activePlan: SubscriptionPlan,
    onWatchRewardedAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activePlan != SubscriptionPlan.FREE_STARTER) return

    GlassLuxuryCard(
        modifier = modifier.testTag("admob_rewarded_card"),
        cornerRadius = 20.dp,
        borderColor = RoyalGold.copy(alpha = 0.55f),
        containerColor = MidnightGlass,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RoyalGold.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "إعلان بمكافأة",
                        tint = RoyalGoldLight,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "شاهد إعلان مكافأة (AdMob)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrispWhite
                    )
                    Text(
                        text = "اربح +25 عملة مجانية لدعم رحلاتك وترجماتك",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoolSilver
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onWatchRewardedAd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = ObsidianDeep
                ),
                modifier = Modifier.testTag("watch_rewarded_ad_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("+25", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RewardedAdModal(
    onDismiss: () -> Unit,
    onRewardEarned: () -> Unit
) {
    val rewardedUnitId = stringResource(R.string.admob_rewarded_id_test)
    var secondsLeft by remember { mutableIntStateOf(4) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = { if (secondsLeft == 0) onDismiss() },
        icon = {
            Icon(
                imageVector = if (secondsLeft == 0) Icons.Filled.CheckCircle else Icons.Filled.AdsClick,
                contentDescription = "إعلان AdMob",
                tint = if (secondsLeft == 0) EmeraldVerified else RoyalGold,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (secondsLeft == 0) "اكتمل الإعلان بنجاح!" else "جارٍ عرض إعلان المكافأة...",
                style = MaterialTheme.typography.titleLarge,
                color = CrispWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (secondsLeft == 0) {
                        "أصبح بإمكانك الآن استلام +25 عملة KemetLoop في محفظتك."
                    } else {
                        "يتبقى $secondsLeft ثوانٍ لاستلام المكافأة."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoolSilver
                )
                LinearProgressIndicator(
                    progress = { (4 - secondsLeft) / 4f },
                    color = RoyalGold,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = CoolSilver
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AdMob Rewarded Unit: $rewardedUnitId",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoolSilver
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onRewardEarned,
                enabled = secondsLeft == 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = ObsidianDeep
                ),
                modifier = Modifier.testTag("claim_rewarded_coins_button")
            ) {
                Text("استلام +25 عملة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إغلاق", color = CrispWhite)
            }
        }
    )
}
