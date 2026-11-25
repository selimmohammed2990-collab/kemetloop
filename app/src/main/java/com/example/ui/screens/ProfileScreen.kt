package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.PickedUriMeta
import com.example.data.RewardsState
import com.example.data.SafeIntentHelper
import com.example.data.UserAppSettings
import com.example.model.BillingCycle
import com.example.model.DiscountCoupon
import com.example.model.KemetCatalogDefaults
import com.example.model.MarketplaceListing
import com.example.model.PaymentSubmission
import com.example.model.SavedLink
import com.example.model.SenderWalletType
import com.example.model.SubscriptionPlan
import com.example.model.UserProfile
import com.example.model.VaultCategory
import com.example.model.VaultFileItem
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.GoldTagPill
import com.example.ui.components.KemetGoldCoinIcon
import com.example.ui.components.KemetGoldCoinPill
import com.example.ui.components.ProCrownBadge
import com.example.ui.components.UserFileVaultSection
import com.example.ui.components.VerifiedGoldStarBadge
import com.example.ui.components.generateQrImageBitmap
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
import java.io.File

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    photoUrl: String?,
    currentUserId: String,
    isGuestMode: Boolean,
    savedLinksState: UiState<List<SavedLink>>,
    couponsState: UiState<List<DiscountCoupon>>,
    listingsState: UiState<List<MarketplaceListing>>,
    submissionsState: UiState<List<PaymentSubmission>>,
    favoriteLandmarkIds: Set<String>,
    appSettings: UserAppSettings,
    vaultFiles: List<VaultFileItem>,
    vaultAutoCloudSync: Boolean,
    isActionLoading: Boolean,
    rewardsState: RewardsState? = null,
    onUpdateDisplayName: (String, () -> Unit) -> Unit,
    onAddDreamLinkClick: () -> Unit,
    onDeleteSavedLink: (String) -> Unit,
    onOpenInAppBrowser: (title: String, url: String) -> Unit,
    onToggleFavoriteLandmark: (String, String) -> Unit,
    onNavigateToTravelGuide: () -> Unit,
    onDeleteListing: (String) -> Unit,
    onAutoSpeakChange: (Boolean) -> Unit,
    onOfflineFirstChange: (Boolean) -> Unit,
    onInspectPickedVaultUri: (Uri) -> PickedUriMeta,
    onResolveLocalVaultFile: (VaultFileItem) -> File?,
    onGetShareableVaultUri: (VaultFileItem) -> Uri?,
    onAddFileToVault: (uri: Uri, title: String, category: VaultCategory, note: String, onDone: () -> Unit) -> Unit,
    onDeleteVaultFile: (VaultFileItem) -> Unit,
    onVaultAutoCloudSyncChange: (Boolean) -> Unit,
    onSyncVaultFilesNow: () -> Unit,
    onSubmitPayment: (
        targetPlan: SubscriptionPlan,
        billingCycle: BillingCycle,
        senderWalletType: SenderWalletType,
        txReference: String,
        receiptSelected: Boolean,
        receiptUriNote: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onWatchRewardedAd: () -> Unit,
    onInviteAffiliate: (String) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLinkGoogleClick: () -> Unit,
    onSignOut: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val walletLocal = stringResource(R.string.vodafone_cash_local)
    val walletIntl = stringResource(R.string.vodafone_cash_intl)
    val developerName = stringResource(R.string.developer_name)
    val developerEmail = stringResource(R.string.developer_email)
    val developerWhatsappUrl = stringResource(R.string.developer_whatsapp_url)

    val basePlan = profile?.subscriptionPlan ?: SubscriptionPlan.FREE_STARTER
    val activePlan = if (basePlan == SubscriptionPlan.FREE_STARTER && rewardsState?.proTrialActive == true) {
        SubscriptionPlan.PRO
    } else {
        basePlan
    }
    val referralCode = profile?.referralCode?.ifBlank { "KEMET100" } ?: "KEMET100"
    val savedLinks = (savedLinksState as? UiState.Success)?.data.orEmpty()
    val coupons = (couponsState as? UiState.Success)?.data.orEmpty()
    val myListings = remember(listingsState, currentUserId) {
        (listingsState as? UiState.Success)?.data.orEmpty().filter { it.userId == currentUserId }
    }
    val favoriteLandmarks = remember(favoriteLandmarkIds) {
        KemetCatalogDefaults.offlineTravelGuide.filter { it.id in favoriteLandmarkIds }
    }

    var mySpaceTab by remember { mutableIntStateOf(0) } // 0: Dream Box, 1: Landmarks, 2: QR Coupons, 3: My Listings
    var showEditNameDialog by remember { mutableStateOf(false) }
    var previewCoupon by remember { mutableStateOf<DiscountCoupon?>(null) }

    // VIP Upgrade state
    var selectedUpgradePlan by remember { mutableStateOf(SubscriptionPlan.PRO) }
    var selectedBillingCycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var senderWalletType by remember { mutableStateOf(SenderWalletType.VODAFONE_CASH) }
    var txReference by remember { mutableStateOf("") }
    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedReceiptUri = uri
            if (uri != null) {
                onShowMessage("تم إرفاق صورة الإيصال بنجاح.")
            }
        }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Editable Personal Identity Card + Google Link / Sign Out
        item {
            GlassLuxuryCard(
                borderColor = RoyalGold,
                modifier = Modifier.testTag("profile_identity_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(2.dp, RoyalGold),
                            color = ObsidianDeep,
                            modifier = Modifier.size(66.dp)
                        ) {
                            if (!photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "الصورة الشخصية",
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
                                    contentDescription = "شعار الهوية",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = profile?.displayName ?: "مسافر KemetLoop",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (activePlan == SubscriptionPlan.PRO) {
                                    ProCrownBadge(iconSize = 20.dp)
                                }
                                if (profile?.verifiedBusiness == true || activePlan == SubscriptionPlan.BUSINESS_VIP) {
                                    VerifiedGoldStarBadge(iconSize = 20.dp)
                                }
                            }
                            Text(
                                text = profile?.email.orEmpty(),
                                style = MaterialTheme.typography.labelMedium,
                                color = CoolSilver,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            GoldTagPill {
                                Text(
                                    text = if (basePlan == SubscriptionPlan.FREE_STARTER && rewardsState?.proTrialActive == true) {
                                        "تجربة Pro مجانية (متبقي ${rewardsState.proTrialDaysRemaining} أيام)"
                                    } else {
                                        activePlan.labelAr
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoyalGoldLight,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(
                                onClick = { showEditNameDialog = true },
                                modifier = Modifier.testTag("profile_edit_name_btn")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "تعديل الاسم", tint = RoyalGoldLight)
                            }
                            if (!isGuestMode) {
                                IconButton(
                                    onClick = onSignOut,
                                    modifier = Modifier.testTag("sign_out_button")
                                ) {
                                    Icon(Icons.Filled.Logout, contentDescription = "تسجيل الخروج", tint = TerracottaError)
                                }
                            }
                        }
                    }

                    if (isGuestMode) {
                        Button(
                            onClick = onLinkGoogleClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalGold,
                                contentColor = ObsidianDeep
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("account_link_google_button")
                        ) {
                            Icon(Icons.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ربط حساب Google لحفظ ملفك ونقاطك سحابياً",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Coins Wallet & Activity Statistics
        item {
            GlassLuxuryCard(
                borderColor = RoyalGold.copy(alpha = 0.6f),
                containerColor = DeepSkyBlue.copy(alpha = 0.25f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KemetGoldCoinIcon(size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "محفظة عملات كيميت وإحصائيات النشاط",
                                style = MaterialTheme.typography.titleMedium,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        KemetGoldCoinPill(coins = profile?.coins ?: 100L, showLabelSuffix = true)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileStatTile(
                            icon = Icons.Filled.EmojiEvents,
                            label = "الألغاز",
                            value = "${profile?.quizWins ?: 0}",
                            accent = RoyalGoldLight,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            icon = Icons.Filled.Translate,
                            label = "الترجمات",
                            value = "${profile?.dailyTranslationsUsed ?: 0}",
                            accent = ElectricAzure,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            icon = Icons.Filled.TrendingUp,
                            label = "أرباح العمولة",
                            value = "${profile?.affiliateEarningsCoins ?: 0}",
                            accent = EmeraldVerified,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Referral Code & Share Card
        item {
            GlassLuxuryCard(borderColor = RoyalGold.copy(alpha = 0.5f)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "كود الدعوة الخاص بك",
                                style = MaterialTheme.typography.labelMedium,
                                color = CoolSilver
                            )
                            Text(
                                text = referralCode,
                                style = MaterialTheme.typography.headlineSmall,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(referralCode))
                                onShowMessage("تم نسخ كود الدعوة: $referralCode")
                            },
                            modifier = Modifier.testTag("copy_referral_code_btn")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ كود الدعوة", tint = RoyalGoldLight)
                        }
                    }

                    Button(
                        onClick = {
                            SafeIntentHelper.shareInviteWithReferral(
                                context = context,
                                referralCode = referralCode,
                                onError = onShowMessage
                            )
                            onInviteAffiliate(referralCode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vip_share_referral_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشاركة التطبيق بكود الدعوة (+50 عملة)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. My Personal Space (مساحتي الخاصة)
        item {
            GlassLuxuryCard(
                borderColor = RoyalGold.copy(alpha = 0.55f),
                modifier = Modifier.testTag("profile_my_space_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = RoyalGoldLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مساحتي الخاصة",
                                style = MaterialTheme.typography.titleLarge,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = onAddDreamLinkClick) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة رابط", color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val spaceTabs = listOf(
                            0 to "صندوق الأحلام (${savedLinks.size})",
                            1 to "المعالم المفضلة (${favoriteLandmarks.size})",
                            2 to "كوبونات QR (${coupons.size})",
                            3 to "إعلاناتي (${myListings.size})"
                        )
                        items(spaceTabs) { (idx, label) ->
                            FilterChip(
                                selected = mySpaceTab == idx,
                                onClick = { mySpaceTab = idx },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalGold,
                                    selectedLabelColor = ObsidianDeep
                                ),
                                modifier = Modifier.testTag("my_space_tab_$idx")
                            )
                        }
                    }

                    when (mySpaceTab) {
                        0 -> {
                            if (savedLinks.isEmpty()) {
                                Text("لا توجد روابط محفوظة بعد. اضغط \"إضافة رابط\" لحفظ روابطك المفضلة.", color = CoolSilver)
                            } else {
                                savedLinks.take(6).forEach { link ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MidnightGlassSoft,
                                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenInAppBrowser(link.title, link.url) }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Link, contentDescription = null, tint = ElectricAzure, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(link.title, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(link.url, style = MaterialTheme.typography.labelSmall, color = CoolSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            IconButton(onClick = { onOpenInAppBrowser(link.title, link.url) }) {
                                                Icon(Icons.Filled.OpenInNew, contentDescription = "فتح الرابط", tint = RoyalGoldLight, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = { onDeleteSavedLink(link.id) }) {
                                                Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف الرابط", tint = TerracottaError, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            if (favoriteLandmarks.isEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("لم تقم بتفضيل أي معالم بعد.", color = CoolSilver)
                                    OutlinedButton(onClick = onNavigateToTravelGuide, border = BorderStroke(1.dp, RoyalGold)) {
                                        Text("استكشف الدليل السياحي", color = RoyalGoldLight)
                                    }
                                }
                            } else {
                                favoriteLandmarks.forEach { place ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MidnightGlassSoft,
                                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(onClick = onNavigateToTravelGuide)
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(place.titleAr, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold)
                                                Text("${place.cityAr} • ${place.highlightsAr}", style = MaterialTheme.typography.labelSmall, color = CoolSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            IconButton(onClick = { onToggleFavoriteLandmark(place.id, place.titleAr) }) {
                                                Icon(Icons.Filled.DeleteOutline, contentDescription = "إزالة من المفضلة", tint = TerracottaError, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            if (coupons.isEmpty()) {
                                Text("لا توجد كوبونات متاحة حالياً.", color = CoolSilver)
                            } else {
                                coupons.take(6).forEach { coupon ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MidnightGlassSoft,
                                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { previewCoupon = coupon }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.ConfirmationNumber, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text("${coupon.code} • خصم ${coupon.discountPercent}%", style = MaterialTheme.typography.titleSmall, color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                                                    Text(coupon.title, style = MaterialTheme.typography.labelSmall, color = CoolSilver, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                            Icon(Icons.Filled.QrCode2, contentDescription = "عرض QR", tint = CrispWhite)
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            if (myListings.isEmpty()) {
                                Text("لم تنشر أي إعلانات في السوق بعد. أضف إعلانك الأول من تبويب السوق!", color = CoolSilver)
                            } else {
                                myListings.forEach { listing ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MidnightGlassSoft,
                                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Storefront, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(listing.title, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("${listing.priceEgp} ج.م • ${listing.location}", style = MaterialTheme.typography.labelSmall, color = CoolSilver)
                                            }
                                            IconButton(onClick = { onDeleteListing(listing.id) }) {
                                                Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف الإعلان", tint = TerracottaError, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4B. User File Vault & Document Wallet (حافظة ملفاتي ووثائقي)
        item {
            UserFileVaultSection(
                vaultFiles = vaultFiles,
                autoCloudSync = vaultAutoCloudSync,
                isGuestMode = isGuestMode,
                isActionLoading = isActionLoading,
                onInspectPickedUri = onInspectPickedVaultUri,
                onResolveLocalFile = onResolveLocalVaultFile,
                onGetShareableUri = onGetShareableVaultUri,
                onSavePickedFile = onAddFileToVault,
                onDeleteVaultFile = onDeleteVaultFile,
                onAutoCloudSyncChange = onVaultAutoCloudSyncChange,
                onSyncNowClick = onSyncVaultFilesNow,
                onLinkGoogleClick = onLinkGoogleClick,
                onShowMessage = onShowMessage
            )
        }

        // 5. Settings Card
        item {
            GlassLuxuryCard(borderColor = RoyalGold.copy(alpha = 0.45f)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = RoyalGoldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إعدادات الحساب والتطبيق",
                            style = MaterialTheme.typography.titleMedium,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = ElectricAzure, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("النطق الصوتي التلقائي للمساعد", style = MaterialTheme.typography.bodyMedium, color = CrispWhite, fontWeight = FontWeight.SemiBold)
                                Text("قراءة ردود المتحدث الذكي تلقائياً", style = MaterialTheme.typography.labelSmall, color = CoolSilver)
                            }
                        }
                        Switch(
                            checked = appSettings.autoSpeakAssistant,
                            onCheckedChange = onAutoSpeakChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WifiOff, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("الأولوية للدليل المحفوظ والأوفلاين", style = MaterialTheme.typography.bodyMedium, color = CrispWhite, fontWeight = FontWeight.SemiBold)
                                Text("توفير البيانات والعمل السريع في السفر", style = MaterialTheme.typography.labelSmall, color = CoolSilver)
                            }
                        }
                        Switch(
                            checked = appSettings.offlineFirstMode,
                            onCheckedChange = onOfflineFirstChange
                        )
                    }
                }
            }
        }

        // 6. Universal Payment Gateway & VIP Upgrade Section Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = RoyalGoldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بوابة الدفع الموحدة وترقية VIP ($walletLocal)",
                    style = MaterialTheme.typography.titleLarge,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 7. Full VIP Plans, Vodafone Cash Checkout, Secret Admin Panel, Developer Info & Payment History
        vipCheckoutAndDeveloperItems(
            activePlan = activePlan,
            selectedUpgradePlan = selectedUpgradePlan,
            onSelectUpgradePlan = { selectedUpgradePlan = it },
            selectedBillingCycle = selectedBillingCycle,
            onSelectBillingCycle = { selectedBillingCycle = it },
            senderWalletType = senderWalletType,
            onSelectSenderWalletType = { senderWalletType = it },
            txReference = txReference,
            onTxReferenceChange = { txReference = it },
            selectedReceiptUri = selectedReceiptUri,
            onPickReceiptClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            submissionsState = submissionsState,
            isActionLoading = isActionLoading,
            walletLocal = walletLocal,
            walletIntl = walletIntl,
            developerName = developerName,
            developerEmail = developerEmail,
            developerWhatsappUrl = developerWhatsappUrl,
            context = context,
            clipboard = clipboard,
            onSubmitPayment = {
                onSubmitPayment(
                    selectedUpgradePlan,
                    selectedBillingCycle,
                    senderWalletType,
                    txReference,
                    selectedReceiptUri != null,
                    selectedReceiptUri?.lastPathSegment.orEmpty()
                ) {
                    txReference = ""
                    selectedReceiptUri = null
                }
            },
            onWatchRewardedAd = onWatchRewardedAd,
            onOpenAdminPanel = onOpenAdminPanel,
            onShowMessage = onShowMessage
        )
    }

    if (showEditNameDialog) {
        EditDisplayNameDialog(
            currentName = profile?.displayName ?: "",
            isLoading = isActionLoading,
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                onUpdateDisplayName(newName) {
                    showEditNameDialog = false
                }
            }
        )
    }

    previewCoupon?.let { coupon ->
        val qrPayload = "KEMETCOUPON:${coupon.code}|${coupon.discountPercent}%|${coupon.merchantName}"
        val qrBitmap = remember(qrPayload) { generateQrImageBitmap(qrPayload, 512) }
        AlertDialog(
            containerColor = MidnightGlass,
            onDismissRequest = { previewCoupon = null },
            title = { Text("كوبون الخصم المباشر (QR)", color = CrispWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (qrBitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = qrBitmap,
                                contentDescription = "رمز QR للكوبون ${coupon.code}",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Text(coupon.title, style = MaterialTheme.typography.titleMedium, color = CrispWhite, fontWeight = FontWeight.Bold)
                    Text("الكود: ${coupon.code} • خصم ${coupon.discountPercent}%", color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { previewCoupon = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MidnightGlassSoft,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = CrispWhite, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = CoolSilver, maxLines = 1)
        }
    }
}

@Composable
private fun EditDisplayNameDialog(
    currentName: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameInput by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = { Text("تعديل الهوية الشخصية", color = CrispWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "اختر الاسم الذي يظهر في بطاقتك الشخصية وعروضك في السوق.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolSilver
                )
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("الاسم الشخصي") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nameInput) },
                enabled = !isLoading && nameInput.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                modifier = Modifier.testTag("save_profile_name_btn")
            ) {
                Text(if (isLoading) "جارٍ الحفظ..." else "حفظ التعديل", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = CrispWhite)
            }
        }
    )
}
