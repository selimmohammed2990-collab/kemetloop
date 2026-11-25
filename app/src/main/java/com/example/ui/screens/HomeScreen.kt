package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.RewardsState
import com.example.model.InternationalAppsCatalog
import com.example.model.MarketplaceListing
import com.example.model.SubscriptionPlan
import com.example.model.UserProfile
import com.example.ui.components.AdMobBannerSlot
import com.example.ui.components.AnimatedGiftBoxButton
import com.example.ui.components.DailyGiftStreakBar
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.GoldTagPill
import com.example.ui.components.KemetGoldCoinPill
import com.example.ui.components.ProCrownBadge
import com.example.ui.components.VerifiedGoldStarBadge
import com.example.ui.components.WelcomeGiftCelebrationDialog
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TerracottaError
import com.example.viewmodel.UiState

@Composable
fun HomeScreen(
    profile: UserProfile?,
    photoUrl: String?,
    isOnline: Boolean,
    rewardsState: RewardsState,
    listingsState: UiState<List<MarketplaceListing>>,
    favoriteLandmarkIds: Set<String>,
    onOpenInternationalDrawer: () -> Unit,
    onOpenInAppBrowser: (title: String, url: String) -> Unit,
    onOpenAiVoiceAssistant: () -> Unit,
    onOpenLiveRoomsDialog: () -> Unit,
    onOpenSosSheet: () -> Unit,
    onClaimWelcomeGift: () -> Unit,
    onClaimDailyGift: () -> Unit,
    onNavigateToTravel: (subTab: Int) -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToVip: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onToggleFavoriteLandmark: (String, String) -> Unit
) {
    val basePlan = profile?.subscriptionPlan ?: SubscriptionPlan.FREE_STARTER
    val activePlan = if (basePlan == SubscriptionPlan.FREE_STARTER && rewardsState.proTrialActive) {
        SubscriptionPlan.PRO
    } else {
        basePlan
    }
    val listings = (listingsState as? UiState.Success)?.data.orEmpty()
    var showWelcomeGiftModal by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAiVoiceAssistant,
                containerColor = RoyalGold,
                contentColor = ObsidianDeep,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 86.dp)
                    .testTag("ai_voice_assistant_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.RecordVoiceOver,
                    contentDescription = "المتحدث الذكي AI"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "المتحدث الذكي",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 152.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Luxury Welcome Bar (Avatar, Royal Badges, Pharaonic Gold Coin, Animated Gift Box, SOS & Drawer)
            item {
                WelcomeLuxuryTopBar(
                    profile = profile,
                    activePlan = activePlan,
                    rewardsState = rewardsState,
                    photoUrl = photoUrl,
                    onProfileClick = onNavigateToProfile,
                    onGiftBoxClick = { showWelcomeGiftModal = true },
                    onOpenDrawerClick = onOpenInternationalDrawer,
                    onOpenSosClick = onOpenSosSheet
                )
            }

            // 1B. 7-Day Daily Gift Streak Bar (مكافآت الدخول اليومي)
            item {
                DailyGiftStreakBar(
                    rewardsState = rewardsState,
                    onClaimDailyGift = onClaimDailyGift
                )
            }

            // 1C. Direct Prominent VIP Plans & Pricing Button + 100 Pioneers Offer
            item {
                VipPricingHighlightBanner(
                    activePlan = activePlan,
                    onNavigateToVip = onNavigateToVip
                )
            }

            if (!isOnline) {
                item {
                    GlassLuxuryCard(
                        borderColor = RoyalGold,
                        containerColor = ObsidianDeep.copy(alpha = 0.85f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "وضع الأوفلاين: الدليل السياحي والمعالم المفضلة والقاموس وصندوق الأحلام جاهزون.",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoyalGoldLight
                        )
                    }
                }
            }

            // 2. Quick International Platforms Strip + Drawer Trigger
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بوابة التطبيقات الدولية",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        GoldTagPill(
                            modifier = Modifier
                                .clickable(onClick = onOpenInternationalDrawer)
                                .testTag("home_open_drawer_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Apps, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "عرض الكل (${InternationalAppsCatalog.apps.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoyalGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(InternationalAppsCatalog.apps.take(10), key = { "quick_${it.id}" }) { app ->
                            val accent = Color(app.accentColorHex)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MidnightGlass,
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                                modifier = Modifier.clickable { onOpenInAppBrowser(app.name, app.url) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(accent.copy(alpha = 0.22f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = app.badgeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accent,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Text(
                                        text = app.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CrispWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Modern Luxury Card Grid with HD images (Tourism, Yachts, Golden Landmarks, Elite Bazaar)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "استكشف السياحة واليخوت والمعالم الذهبية",
                        style = MaterialTheme.typography.titleLarge,
                        color = CrispWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LuxuryVisualGridCard(
                            imageRes = R.drawable.img_luxury_yacht,
                            tag = "يخوت VIP",
                            title = "يخوت البحر الأحمر والنيل",
                            subtitle = "رحلات بحرية خاصة",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_card_yachts"),
                            onClick = { onNavigateToTravel(0) }
                        )

                        val isGizaFav = "guide_giza" in favoriteLandmarkIds
                        LuxuryVisualGridCard(
                            imageRes = R.drawable.img_kemet_hero,
                            tag = "معلم ذهبي",
                            title = "أهرامات الجيزة والمتحف",
                            subtitle = "دليل ذكي أوفلاين",
                            isFavorite = isGizaFav,
                            onToggleFavorite = { onToggleFavoriteLandmark("guide_giza", "أهرامات الجيزة والمتحف المصري الكبير") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_card_pyramids"),
                            onClick = { onNavigateToTravel(2) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isLuxorFav = "guide_luxor" in favoriteLandmarkIds
                        LuxuryVisualGridCard(
                            imageRes = R.drawable.img_travel_luxor,
                            tag = "تراث خالد",
                            title = "معابد الأقصر والكرنك",
                            subtitle = "برامج سياحية وتذاكر",
                            isFavorite = isLuxorFav,
                            onToggleFavorite = { onToggleFavoriteLandmark("guide_luxor", "معبد الكرنك ووادي الملوك") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_card_luxor"),
                            onClick = { onNavigateToTravel(2) }
                        )

                        LuxuryVisualGridCard(
                            imageRes = R.drawable.img_bazaar_crafts,
                            tag = "سوق النخبة",
                            title = "بازار التحف والهدايا",
                            subtitle = "تجار موثقون وعمولة 10%",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_card_bazaar"),
                            onClick = onNavigateToMarket
                        )
                    }
                }
            }

            // 4. Prominent Live Chat & Video Rooms Card
            item {
                LiveChatAndVideoRoomsCard(
                    onOpenLiveRooms = onOpenLiveRoomsDialog,
                    onOpenAiVoice = onOpenAiVoiceAssistant
                )
            }

            // 5. Smart Quick Shortcuts (Translator, Concierge, Market, VIP)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickLuxuryPillCard(
                        icon = Icons.Filled.Translate,
                        title = "المترجم والكاميرا",
                        subtitle = "8 لغات + أوفلاين",
                        accent = ElectricAzure,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTravel(3) }
                    )
                    QuickLuxuryPillCard(
                        icon = Icons.Filled.Storefront,
                        title = "سوق KemetLoop",
                        subtitle = "${listings.size} عروض متاحة",
                        accent = RoyalGoldLight,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToMarket
                    )
                }
            }

            // Balanced AdMob Banner on Free Starter
            item {
                AdMobBannerSlot(
                    activePlan = activePlan,
                    onUpgradeClick = onNavigateToVip
                )
            }
        }
    }

    if (showWelcomeGiftModal) {
        WelcomeGiftCelebrationDialog(
            rewardsState = rewardsState,
            onClaimGift = onClaimWelcomeGift,
            onDismiss = { showWelcomeGiftModal = false }
        )
    }
}

@Composable
private fun WelcomeLuxuryTopBar(
    profile: UserProfile?,
    activePlan: SubscriptionPlan,
    rewardsState: RewardsState,
    photoUrl: String?,
    onProfileClick: () -> Unit,
    onGiftBoxClick: () -> Unit,
    onOpenDrawerClick: () -> Unit,
    onOpenSosClick: () -> Unit
) {
    val showProCrown = activePlan == SubscriptionPlan.PRO
    val showVerifiedStar = profile?.verifiedBusiness == true || activePlan == SubscriptionPlan.BUSINESS_VIP
    val planSubtitle = when {
        profile?.subscriptionPlan == SubscriptionPlan.FREE_STARTER && rewardsState.proTrialActive ->
            "تجربة Pro مجانية (متبقي ${rewardsState.proTrialDaysRemaining} أيام)"
        else -> activePlan.labelAr
    }

    GlassLuxuryCard(
        borderColor = RoyalGold.copy(alpha = 0.72f),
        containerColor = MidnightGlass,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User Avatar Photo
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(1.8.dp, RoyalGold),
                    color = ObsidianDeep,
                    modifier = Modifier
                        .size(52.dp)
                        .clickable(onClick = onProfileClick)
                        .testTag("home_user_avatar")
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "صورة المستخدم",
                            placeholder = painterResource(id = R.drawable.img_kemetloop_royal_emblem),
                            error = painterResource(id = R.drawable.img_kemetloop_royal_emblem),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_kemetloop_royal_emblem),
                            contentDescription = "شعار المستخدم",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // User Name + Royal Badges (Crown / Verified Star) + Plan
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onProfileClick),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "أهلاً، ${profile?.displayName ?: "مسافر"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (showProCrown) {
                            ProCrownBadge(iconSize = 20.dp)
                        }
                        if (showVerifiedStar) {
                            VerifiedGoldStarBadge(iconSize = 20.dp)
                        }
                    }
                    Text(
                        text = planSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGoldLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Animated Pulsing Gift Box Icon (Welcome Gift 100 Coins + 3 Days Pro)
                AnimatedGiftBoxButton(
                    hasUnclaimedGift = !rewardsState.welcomeGiftClaimed,
                    onClick = onGiftBoxClick
                )
            }

            // Row 2: Pharaonic Gold Coin Pill + Quick SOS + International Apps Drawer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KemetGoldCoinPill(
                    coins = profile?.coins ?: 100L,
                    showLabelSuffix = true,
                    onClick = onProfileClick,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 40.dp)
                        .testTag("home_coins_pill")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaError.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, TerracottaError.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenSosClick)
                            .testTag("home_sos_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sos,
                                contentDescription = "طوارئ SOS",
                                tint = CrispWhite,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "طوارئ",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = RoyalGold.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenDrawerClick)
                            .testTag("home_drawer_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Apps,
                                contentDescription = "التطبيقات الدولية",
                                tint = RoyalGoldLight,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "الدولية",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VipPricingHighlightBanner(
    activePlan: SubscriptionPlan,
    onNavigateToVip: () -> Unit
) {
    GlassLuxuryCard(
        borderColor = RoyalGold,
        containerColor = DeepSkyBlue.copy(alpha = 0.35f),
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier.testTag("home_vip_pricing_card")
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
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = RoyalGoldLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "باقات العضوية الملكية والتوثيق",
                            style = MaterialTheme.typography.titleSmall,
                            color = CrispWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "تبدأ من 0 ج.م • باقة Pro بـ 99 ج.م • Business VIP بـ 299 ج.م",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolSilver
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProCrownBadge(iconSize = 20.dp)
                    VerifiedGoldStarBadge(iconSize = 20.dp)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RoyalGold.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "عرض الإطلاق للرواد: شهر مجاني إضافي لأول 100 نشاط تجاري وسياحي ينضم للمنصة",
                    style = MaterialTheme.typography.labelMedium,
                    color = RoyalGoldLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }

            Button(
                onClick = onNavigateToVip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = ObsidianDeep
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("home_vip_pricing_btn")
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (activePlan == SubscriptionPlan.FREE_STARTER) {
                        "الباقات والأسعار (VIP)"
                    } else {
                        "الباقات والأسعار (VIP) • ${activePlan.labelAr}"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LuxuryVisualGridCard(
    imageRes: Int,
    tag: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isFavorite: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = ObsidianDeep)
    ) {
        Box(modifier = Modifier.height(176.dp)) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianDeep.copy(alpha = 0.15f),
                                ObsidianDeep.copy(alpha = 0.92f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldTagPill(backgroundColor = ObsidianDeep.copy(alpha = 0.8f)) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGoldLight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (isFavorite != null && onToggleFavorite != null) {
                    Surface(
                        shape = CircleShape,
                        color = ObsidianDeep.copy(alpha = 0.72f),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onToggleFavorite)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "تفضيل المعلم",
                                tint = if (isFavorite) RoyalGoldLight else CrispWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = CoolSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LiveChatAndVideoRoomsCard(
    onOpenLiveRooms: () -> Unit,
    onOpenAiVoice: () -> Unit
) {
    GlassLuxuryCard(
        borderColor = ElectricAzure.copy(alpha = 0.75f),
        containerColor = DeepSkyBlue.copy(alpha = 0.36f),
        modifier = Modifier
            .clickable(onClick = onOpenLiveRooms)
            .testTag("home_live_rooms_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoyalGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.VideoCall, contentDescription = null, tint = RoyalGoldLight)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "غرف المحادثة والفيديو المباشر",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "استشارات سياحية حية ومكالمات فيديو VIP",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolSilver
                        )
                    }
                }
                GoldTagPill(
                    backgroundColor = EmeraldVerified.copy(alpha = 0.22f),
                    borderColor = EmeraldVerified
                ) {
                    Text(
                        text = "مباشر ●",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldVerified,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenLiveRooms,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = ObsidianDeep
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("دخول الغرف المباشرة", fontWeight = FontWeight.Bold, maxLines = 1)
                }
                Button(
                    onClick = onOpenAiVoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MidnightGlassSoft,
                        contentColor = RoyalGoldLight
                    ),
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مساعد AI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuickLuxuryPillCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassLuxuryCard(
        modifier = modifier.clickable(onClick = onClick),
        contentPadding = PaddingValues(14.dp),
        borderColor = accent.copy(alpha = 0.45f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = CoolSilver, maxLines = 1)
            }
        }
    }
}
