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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.SafeIntentHelper
import com.example.model.AffiliateLog
import com.example.model.LinkCategory
import com.example.model.ListingCategory
import com.example.model.ListingReview
import com.example.model.MarketplaceListing
import com.example.model.SavedLink
import com.example.model.SubscriptionPlan
import com.example.model.UserProfile
import com.example.ui.components.AdMobBannerSlot
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.GoldTagPill
import com.example.ui.components.KemetGoldCoinPill
import com.example.ui.components.ProCrownBadge
import com.example.ui.components.VerifiedGoldStarBadge
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TerracottaError
import com.example.viewmodel.UiState
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    profile: UserProfile?,
    isGuestMode: Boolean,
    isOnline: Boolean,
    listingsState: UiState<List<MarketplaceListing>>,
    savedLinksState: UiState<List<SavedLink>>,
    affiliateLogsState: UiState<List<AffiliateLog>>,
    currentUserId: String,
    isActionLoading: Boolean,
    observeReviews: (String) -> Flow<List<ListingReview>>,
    onSubmitReview: (String, Int, String, () -> Unit) -> Unit,
    onPublishListing: (
        title: String,
        description: String,
        category: ListingCategory,
        priceEgp: Long,
        location: String,
        featured: Boolean,
        whatsappPhone: String,
        socialUrl: String,
        onComplete: () -> Unit
    ) -> Unit,
    onDeleteListing: (String) -> Unit,
    onShareOfferAffiliate: (MarketplaceListing) -> Unit,
    onInviteAffiliate: (String) -> Unit,
    onAddSavedLink: (String, String, LinkCategory, () -> Unit) -> Unit,
    onDeleteSavedLink: (String) -> Unit,
    onOpenSosSheet: () -> Unit,
    onOpenConcierge: () -> Unit,
    onOpenInAppBrowser: (title: String, url: String) -> Unit,
    onNavigateToVip: () -> Unit,
    onLinkGoogleClick: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val activePlan = profile?.subscriptionPlan ?: SubscriptionPlan.FREE_STARTER
    val isBusinessVip = activePlan == SubscriptionPlan.BUSINESS_VIP

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ListingCategory?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showLinkBoxSheet by remember { mutableStateOf(false) }
    var reviewTargetListing by remember { mutableStateOf<MarketplaceListing?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = RoyalGold,
                contentColor = ObsidianDeep,
                modifier = Modifier
                    .padding(bottom = 84.dp)
                    .testTag("add_listing_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "نشر إعلان أو رحلة")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!isOnline) {
                item {
                    GlassLuxuryCard(
                        borderColor = RoyalGold,
                        containerColor = ObsidianDeep.copy(alpha = 0.85f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "وضع عدم الاتصال: صندوق الأحلام والروابط والدليل والقاموس جاهزون للعمل بدون إنترنت.",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoyalGoldLight
                        )
                    }
                }
            }

            // Guest Banner if browsing in Guest Mode
            if (isGuestMode) {
                item {
                    GlassLuxuryCard(
                        borderColor = RoyalGold,
                        containerColor = DeepSkyBlue.copy(alpha = 0.38f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "أنت تتصفح كزائر (هدية 100 عملة)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "اربط حساب Google لحفظ نقاطك واشتراكاتك للأبد",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CoolSilver
                                )
                            }
                            Button(
                                onClick = onLinkGoogleClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalGold,
                                    contentColor = ObsidianDeep
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("guest_link_google_banner_btn")
                            ) {
                                Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ربط Google", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Panoramic Luxury Tourism & Tech Hero Card with Quick SOS Badge
            item {
                MarketLuxuryHeroCard(
                    profile = profile,
                    onOpenSosSheet = onOpenSosSheet,
                    onNavigateToVip = onNavigateToVip
                )
            }

            // Modern Bento Grid (6 interactive glass tiles: SOS, Dream Box, Share & Invite, VIP, Concierge/QR, Publish)
            item {
                val code = profile?.referralCode?.ifBlank { "KEMET100" } ?: "KEMET100"
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoActionTile(
                            icon = Icons.Filled.Sos,
                            title = "مساعد الطوارئ SOS",
                            subtitle = "مستشفى · صيدلية · 126",
                            accent = TerracottaError,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sos_bento_button"),
                            onClick = onOpenSosSheet
                        )
                        BentoActionTile(
                            icon = Icons.Filled.Link,
                            title = "صندوق الأحلام والروابط",
                            subtitle = "محفوظ ويعمل أوفلاين",
                            accent = ElectricAzure,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_link_box_button"),
                            onClick = { showLinkBoxSheet = true }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoActionTile(
                            icon = Icons.Filled.TrendingUp,
                            title = "شارك التطبيق (+50 عملة)",
                            subtitle = "كود الدعوة: $code",
                            accent = RoyalGold,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("invite_friend_button"),
                            onClick = {
                                onInviteAffiliate(code)
                                clipboard.setText(AnnotatedString(code))
                                SafeIntentHelper.shareInviteWithReferral(context, code, null, onShowMessage)
                            }
                        )
                        BentoActionTile(
                            icon = Icons.Filled.WorkspacePremium,
                            title = "الكونسيرج وكوبونات QR",
                            subtitle = "طلبات VIP وخصومات",
                            accent = RoyalGoldLight,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("concierge_bento_button"),
                            onClick = onOpenConcierge
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BentoActionTile(
                            icon = Icons.Filled.Storefront,
                            title = "نشر عرض جديد",
                            subtitle = if (isBusinessVip) "تثبيت VIP متاح" else "منتج · رحلة · خدمة",
                            accent = EmeraldVerified,
                            modifier = Modifier.weight(1f),
                            onClick = { showCreateDialog = true }
                        )
                        BentoActionTile(
                            icon = Icons.Filled.WorkspacePremium,
                            title = activePlan.labelAr,
                            subtitle = "ترقية & فودافون كاش",
                            accent = RoyalGold,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToVip
                        )
                    }
                }
            }

            // Recent Affiliate activity strip if present
            val recentLogs = (affiliateLogsState as? UiState.Success)?.data.orEmpty()
            if (recentLogs.isNotEmpty()) {
                item {
                    val latest = recentLogs.first()
                    GlassLuxuryCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "آخر عمولة: ${latest.referenceTitle}",
                                style = MaterialTheme.typography.labelLarge,
                                color = CrispWhite
                            )
                            Text(
                                text = "+${latest.earnedCoins} عملة",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldVerified,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Horizontal Carousel of Featured VIP / Highlighted Offers
            if (listingsState is UiState.Success && listingsState.data.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "عروض النخبة المميزة",
                                style = MaterialTheme.typography.titleLarge,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${listingsState.data.size} عروض",
                                style = MaterialTheme.typography.labelMedium,
                                color = RoyalGoldLight
                            )
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(listingsState.data.take(6), key = { "carousel_${it.id}" }) { item ->
                                CarouselHighlightCard(
                                    listing = item,
                                    onShare = { onShareOfferAffiliate(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Search & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث عن منتج، رحلة، أو خدمة...", color = CoolSilver) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalGoldLight) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_search_field")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("الكل") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGold,
                                    selectedLabelColor = ObsidianDeep
                                )
                            )
                        }
                        items(ListingCategory.entries) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text(cat.labelAr) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGold,
                                    selectedLabelColor = ObsidianDeep
                                )
                            )
                        }
                    }
                }
            }

            // Marketplace List
            when (listingsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RoyalGold)
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        GlassLuxuryCard(borderColor = MaterialTheme.colorScheme.error) {
                            Text(text = listingsState.message, color = CrispWhite)
                        }
                    }
                }

                is UiState.Success -> {
                    val filtered = listingsState.data.filter { item ->
                        val catMatch = selectedCategory == null || item.listingCategory == selectedCategory
                        val q = searchQuery.trim()
                        val queryMatch = q.isEmpty() ||
                            item.title.contains(q, ignoreCase = true) ||
                            item.description.contains(q, ignoreCase = true) ||
                            item.location.contains(q, ignoreCase = true)
                        catMatch && queryMatch
                    }

                    if (filtered.isEmpty()) {
                        item {
                            GlassLuxuryCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "لا توجد عروض مطابقة حالياً",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CrispWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "اضغط على (+) لنشر عرض جديد",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CoolSilver
                                    )
                                }
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { listing ->
                            MarketplaceListingCard(
                                listing = listing,
                                isOwner = listing.userId == currentUserId,
                                onShareAffiliate = {
                                    onShareOfferAffiliate(listing)
                                    val code = profile?.referralCode ?: "KEMET"
                                    SafeIntentHelper.shareInviteWithReferral(
                                        context = context,
                                        referralCode = code,
                                        customHeadline = "${listing.title} - ${listing.priceEgp} ج.م في ${listing.location}",
                                        onError = onShowMessage
                                    )
                                },
                                onOpenReviews = { reviewTargetListing = listing },
                                onOpenWhatsApp = { phone ->
                                    SafeIntentHelper.openWhatsApp(context, phone, onShowMessage)
                                },
                                onOpenSocial = { url ->
                                    onOpenInAppBrowser(listing.sellerName, url)
                                },
                                onDelete = { onDeleteListing(listing.id) }
                            )
                        }
                    }
                }
            }

            // Balanced AdMob Banner for Free Starter
            item {
                AdMobBannerSlot(
                    activePlan = activePlan,
                    onUpgradeClick = onNavigateToVip
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateListingDialog(
            isBusinessVip = isBusinessVip,
            isLoading = isActionLoading,
            onDismiss = { showCreateDialog = false },
            onSubmit = { title, desc, cat, price, loc, featured, wa, social ->
                onPublishListing(title, desc, cat, price, loc, featured, wa, social) {
                    showCreateDialog = false
                }
            },
            onUpgradeClick = {
                showCreateDialog = false
                onNavigateToVip()
            }
        )
    }

    if (showLinkBoxSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLinkBoxSheet = false },
            containerColor = MidnightGlass
        ) {
            LinkBoxSheetContent(
                linksState = savedLinksState,
                isActionLoading = isActionLoading,
                onAddLink = { title, url, cat ->
                    onAddSavedLink(title, url, cat) {}
                },
                onDeleteLink = onDeleteSavedLink,
                onOpenUrl = { title, url ->
                    showLinkBoxSheet = false
                    onOpenInAppBrowser(title, url)
                }
            )
        }
    }

    reviewTargetListing?.let { listing ->
        ListingReviewsDialog(
            listing = listing,
            reviewsFlow = observeReviews(listing.id),
            isActionLoading = isActionLoading,
            onSubmitReview = { stars, comment ->
                onSubmitReview(listing.id, stars, comment) {}
            },
            onDismiss = { reviewTargetListing = null }
        )
    }
}

@Composable
private fun MarketLuxuryHeroCard(
    profile: UserProfile?,
    onOpenSosSheet: () -> Unit,
    onNavigateToVip: () -> Unit
) {
    val plan = profile?.subscriptionPlan ?: SubscriptionPlan.FREE_STARTER
    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = ObsidianDeep),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.img_luxury_tourism_tech),
                contentDescription = "بانوراما السياحة والتكنولوجيا",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianDeep.copy(alpha = 0.25f),
                                ObsidianDeep.copy(alpha = 0.88f)
                            )
                        )
                    )
            )
            GoldTagPill(
                backgroundColor = TerracottaError.copy(alpha = 0.9f),
                borderColor = RoyalGoldLight,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clickable(onClick = onOpenSosSheet)
                    .testTag("hero_sos_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Sos, contentDescription = "الطوارئ SOS", tint = CrispWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طوارئ GPS", style = MaterialTheme.typography.labelSmall, color = CrispWhite, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "مرحباً، ${profile?.displayName ?: "مسافر"}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                        if (plan == SubscriptionPlan.PRO) {
                            ProCrownBadge(iconSize = 20.dp)
                        }
                        if (profile?.verifiedBusiness == true || plan == SubscriptionPlan.BUSINESS_VIP) {
                            VerifiedGoldStarBadge(iconSize = 20.dp)
                        }
                    }

                    KemetGoldCoinPill(coins = profile?.coins ?: 100L, showLabelSuffix = true)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عالم السياحة الذكية والتجارة الفاخرة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoolSilver
                    )
                    GoldTagPill(
                        modifier = Modifier.clickable(onClick = onNavigateToVip)
                    ) {
                        Text(
                            text = plan.labelAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = RoyalGoldLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightGlass),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(accent.copy(alpha = 0.16f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = CrispWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = CoolSilver,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CarouselHighlightCard(
    listing: MarketplaceListing,
    onShare: () -> Unit
) {
    val thumbRes = when (listing.listingCategory) {
        ListingCategory.TRIP -> R.drawable.img_travel_luxor
        ListingCategory.PRODUCT -> R.drawable.img_bazaar_crafts
        ListingCategory.SERVICE -> R.drawable.img_luxury_tourism_tech
    }
    Card(
        modifier = Modifier.width(235.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightGlass),
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f))
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(id = thumbRes),
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp),
                    contentScale = ContentScale.Crop
                )
                if (listing.featured) {
                    GoldTagPill(
                        backgroundColor = RoyalGold,
                        borderColor = RoyalGoldLight,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "VIP مميز",
                            style = MaterialTheme.typography.labelSmall,
                            color = ObsidianDeep,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${listing.priceEgp} ج.م",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoyalGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${listing.affiliateRewardCoins} عمولة",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldVerified,
                        modifier = Modifier.clickable(onClick = onShare)
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplaceListingCard(
    listing: MarketplaceListing,
    isOwner: Boolean,
    onShareAffiliate: () -> Unit,
    onOpenReviews: () -> Unit,
    onOpenWhatsApp: (String) -> Unit,
    onOpenSocial: (String) -> Unit,
    onDelete: () -> Unit
) {
    val thumbRes = when (listing.listingCategory) {
        ListingCategory.TRIP -> R.drawable.img_travel_luxor
        ListingCategory.PRODUCT -> R.drawable.img_bazaar_crafts
        ListingCategory.SERVICE -> R.drawable.img_luxury_tourism_tech
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("listing_card_${listing.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightGlass),
        border = BorderStroke(
            width = 1.dp,
            color = if (listing.featured) RoyalGold else RoyalGold.copy(alpha = 0.4f)
        )
    ) {
        Column {
            Box {
                Image(
                    painter = painterResource(id = thumbRes),
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GoldTagPill(backgroundColor = ObsidianDeep.copy(alpha = 0.8f)) {
                        Text(
                            text = listing.listingCategory.labelAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = CrispWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (listing.featured) {
                        GoldTagPill(backgroundColor = RoyalGold, borderColor = RoyalGoldLight) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "إعلان مميز",
                                    tint = ObsidianDeep,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "إعلان مميز VIP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ObsidianDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = listing.sellerName,
                            style = MaterialTheme.typography.labelMedium,
                            color = CoolSilver
                        )
                        if (listing.verifiedSeller) {
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedGoldStarBadge(iconSize = 18.dp)
                        }
                    }
                    Text(
                        text = "${listing.priceEgp} ج.م",
                        style = MaterialTheme.typography.titleMedium,
                        color = RoyalGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoolSilver
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = ElectricAzure,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${listing.location} • عمولة 10%",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoolSilver
                        )
                    }
                    GoldTagPill(
                        modifier = Modifier
                            .clickable(onClick = onOpenReviews)
                            .testTag("listing_reviews_btn_${listing.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "تقييم 5 نجوم",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onShareAffiliate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalGold,
                            contentColor = ObsidianDeep
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "شارك واربح +${listing.affiliateRewardCoins} عملة",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (listing.whatsappPhone.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onOpenWhatsApp(listing.whatsappPhone) },
                            border = BorderStroke(1.dp, EmeraldVerified.copy(alpha = 0.7f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Outlined.Chat, contentDescription = "واتساب", modifier = Modifier.size(16.dp), tint = EmeraldVerified)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واتساب", style = MaterialTheme.typography.labelMedium, color = CrispWhite)
                        }
                    }

                    if (listing.socialUrl.isNotBlank()) {
                        IconButton(onClick = { onOpenSocial(listing.socialUrl) }) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = "فتح في المتصفح الذكي", tint = RoyalGoldLight)
                        }
                    }

                    if (isOwner) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "حذف الإعلان",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
