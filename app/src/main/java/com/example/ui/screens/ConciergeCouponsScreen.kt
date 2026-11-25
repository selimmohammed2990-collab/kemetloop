package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SafeIntentHelper
import com.example.model.ConciergeOffer
import com.example.model.ConciergeRequest
import com.example.model.DiscountCoupon
import com.example.model.KemetCatalogDefaults
import com.example.model.ListingCategory
import com.example.model.SubscriptionPlan
import com.example.model.UserProfile
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.GoldTagPill
import com.example.ui.components.VerifiedGoldStarBadge
import com.example.ui.components.QrCameraScannerDialog
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
import com.example.viewmodel.UiState
import kotlinx.coroutines.flow.Flow

@Composable
fun ConciergeCouponsScreen(
    profile: UserProfile?,
    isOnline: Boolean,
    conciergeState: UiState<List<ConciergeRequest>>,
    couponsState: UiState<List<DiscountCoupon>>,
    favoriteLandmarkIds: Set<String> = emptySet(),
    initialTab: Int = 0,
    isActionLoading: Boolean,
    observeOffers: (requestId: String, requesterUid: String) -> Flow<List<ConciergeOffer>>,
    onCreateConciergeRequest: (ListingCategory, String, String, Long, String, () -> Unit) -> Unit,
    onSubmitConciergeOffer: (ConciergeRequest, Long, String, String, () -> Unit) -> Unit,
    onCreateCoupon: (String, String, Long, String, () -> Unit) -> Unit,
    onQrCouponScanned: (String) -> Unit,
    onToggleFavoriteLandmark: (String, String) -> Unit = { _, _ -> },
    onNavigateToVip: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val isBusinessVip = profile?.subscriptionPlan == SubscriptionPlan.BUSINESS_VIP
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0: Concierge, 1: QR Coupons, 2: Offline Guide

    var showNewRequestDialog by remember { mutableStateOf(false) }
    var offerTargetRequest by remember { mutableStateOf<ConciergeRequest?>(null) }
    var showCreateCouponDialog by remember { mutableStateOf(false) }
    var showQrScannerDialog by remember { mutableStateOf(false) }
    var previewCoupon by remember { mutableStateOf<DiscountCoupon?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassLuxuryCard(borderColor = RoyalGold.copy(alpha = 0.65f)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RoomService, contentDescription = null, tint = RoyalGoldLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الكونسيرج، كوبونات QR والدليل",
                                style = MaterialTheme.typography.titleLarge,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isOnline) {
                            GoldTagPill(backgroundColor = RoyalGold.copy(alpha = 0.25f)) {
                                Text(
                                    text = "بدون إنترنت",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoyalGoldLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "اطلب رحلة أو منتجاً مخصصاً لتجار VIP، واستفد من كوبونات QR والدليل السياحي المحلي.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoolSilver
                    )
                }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = RoyalGold, activeContentColor = ObsidianDeep),
                    modifier = Modifier.testTag("concierge_tab_requests")
                ) {
                    Text("الكونسيرج", maxLines = 1, fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = RoyalGold, activeContentColor = ObsidianDeep),
                    modifier = Modifier.testTag("concierge_tab_coupons")
                ) {
                    Text("كوبونات QR", maxLines = 1, fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = RoyalGold, activeContentColor = ObsidianDeep),
                    modifier = Modifier.testTag("concierge_tab_offline_guide")
                ) {
                    Text("الدليل السياحي", maxLines = 1, fontWeight = FontWeight.Bold)
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item {
                    Button(
                        onClick = { showNewRequestDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("new_concierge_request_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("كتابة طلب خاص لتجار Business VIP", fontWeight = FontWeight.Bold)
                    }
                }

                when (conciergeState) {
                    is UiState.Loading -> item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = RoyalGold)
                        }
                    }
                    is UiState.Error -> item {
                        GlassLuxuryCard(borderColor = MaterialTheme.colorScheme.error) {
                            Text(conciergeState.message, color = CrispWhite)
                        }
                    }
                    is UiState.Success -> {
                        if (conciergeState.data.isEmpty()) {
                            item {
                                GlassLuxuryCard {
                                    Text("لا توجد طلبات خاصة مفتوحة حالياً. أضف طلبك الأول!", color = CoolSilver)
                                }
                            }
                        } else {
                            items(conciergeState.data, key = { it.id }) { req ->
                                ConciergeRequestCard(
                                    request = req,
                                    isBusinessVip = isBusinessVip,
                                    offersFlow = observeOffers(req.id, req.userId),
                                    onSubmitOfferClick = { offerTargetRequest = req },
                                    onUpgradeVipClick = onNavigateToVip,
                                    onOpenWhatsApp = { phone ->
                                        SafeIntentHelper.openWhatsApp(context, phone, onShowMessage)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showQrScannerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("scan_qr_coupon_button")
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مسح كوبون QR", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showCreateCouponDialog = true },
                            border = BorderStroke(1.dp, RoyalGold),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("create_qr_coupon_button")
                        ) {
                            Icon(Icons.Filled.QrCode2, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("توليد كوبون QR", color = CrispWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                when (couponsState) {
                    is UiState.Loading -> item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = RoyalGold)
                        }
                    }
                    is UiState.Error -> item {
                        GlassLuxuryCard { Text(couponsState.message, color = CrispWhite) }
                    }
                    is UiState.Success -> {
                        items(couponsState.data, key = { it.id }) { coupon ->
                            CouponCard(
                                coupon = coupon,
                                onShowQr = { previewCoupon = coupon }
                            )
                        }
                    }
                }
            }

            2 -> {
                item {
                    GlassLuxuryCard(
                        borderColor = EmeraldVerified.copy(alpha = 0.6f),
                        containerColor = DeepSkyBlue.copy(alpha = 0.25f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.OfflinePin, contentDescription = null, tint = EmeraldVerified)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الدليل السياحي الذكي يعمل 100% بدون إنترنت",
                                style = MaterialTheme.typography.titleSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                items(KemetCatalogDefaults.offlineTravelGuide, key = { it.id }) { place ->
                    val isFav = place.id in favoriteLandmarkIds
                    GlassLuxuryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = place.titleAr,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GoldTagPill {
                                        Text(
                                            place.cityAr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RoyalGoldLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onToggleFavoriteLandmark(place.id, place.titleAr) },
                                        modifier = Modifier.testTag("fav_landmark_${place.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = if (isFav) "إزالة من المفضلة" else "إضافة إلى المعالم المفضلة",
                                            tint = if (isFav) RoyalGoldLight else CoolSilver
                                        )
                                    }
                                }
                            }
                            Text(place.summaryAr, style = MaterialTheme.typography.bodyMedium, color = CoolSilver)
                            Text("أبرز المعالم: ${place.highlightsAr}", style = MaterialTheme.typography.labelMedium, color = RoyalGoldLight)
                            Text("نصيحة التنقل: ${place.transportTipAr}", style = MaterialTheme.typography.labelSmall, color = CoolSilver)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("أقرب مستشفى: ${place.nearbyHospitalAr}", style = MaterialTheme.typography.labelSmall, color = EmeraldVerified)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewRequestDialog) {
        CreateConciergeRequestDialog(
            isLoading = isActionLoading,
            onDismiss = { showNewRequestDialog = false },
            onSubmit = { cat, title, details, budget, city ->
                onCreateConciergeRequest(cat, title, details, budget, city) {
                    showNewRequestDialog = false
                }
            }
        )
    }

    offerTargetRequest?.let { req ->
        SubmitVipOfferDialog(
            request = req,
            isLoading = isActionLoading,
            onDismiss = { offerTargetRequest = null },
            onSubmit = { price, proposal, wa ->
                onSubmitConciergeOffer(req, price, proposal, wa) {
                    offerTargetRequest = null
                }
            }
        )
    }

    if (showCreateCouponDialog) {
        CreateCouponDialog(
            isLoading = isActionLoading,
            onDismiss = { showCreateCouponDialog = false },
            onSubmit = { code, title, discount, city ->
                onCreateCoupon(code, title, discount, city) {
                    showCreateCouponDialog = false
                }
            }
        )
    }

    if (showQrScannerDialog) {
        QrCameraScannerDialog(
            onScanned = { qrText ->
                showQrScannerDialog = false
                onQrCouponScanned(qrText)
            },
            onDismiss = { showQrScannerDialog = false },
            onError = onShowMessage
        )
    }

    previewCoupon?.let { coupon ->
        QrCouponViewerDialog(
            coupon = coupon,
            onDismiss = { previewCoupon = null }
        )
    }
}

@Composable
private fun ConciergeRequestCard(
    request: ConciergeRequest,
    isBusinessVip: Boolean,
    offersFlow: Flow<List<ConciergeOffer>>,
    onSubmitOfferClick: () -> Unit,
    onUpgradeVipClick: () -> Unit,
    onOpenWhatsApp: (String) -> Unit
) {
    val offers by offersFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    GlassLuxuryCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldTagPill {
                    Text(
                        text = "${request.listingCategory.labelAr} · ${request.city}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RoyalGoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "الميزانية: ${request.budgetEgp} ج.م",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldVerified,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(request.title, style = MaterialTheme.typography.titleMedium, color = CrispWhite, fontWeight = FontWeight.Bold)
            Text(request.details, style = MaterialTheme.typography.bodyMedium, color = CoolSilver)
            Text("بواسطة: ${request.requesterName}", style = MaterialTheme.typography.labelSmall, color = CoolSilver)

            if (offers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("عروض تجار Business VIP (${offers.size}):", style = MaterialTheme.typography.labelLarge, color = RoyalGoldLight)
                    offers.forEach { offer ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MidnightGlassSoft,
                            border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(offer.merchantName, style = MaterialTheme.typography.labelLarge, color = CrispWhite, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        VerifiedGoldStarBadge(iconSize = 16.dp)
                                    }
                                    Text("${offer.priceEgp} ج.م", color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                                }
                                Text(offer.proposal, style = MaterialTheme.typography.bodySmall, color = CoolSilver)
                                if (offer.whatsappPhone.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = { onOpenWhatsApp(offer.whatsappPhone) },
                                        border = BorderStroke(1.dp, EmeraldVerified.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Outlined.Chat, contentDescription = null, tint = EmeraldVerified, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تواصل واتساب مع التاجر", color = CrispWhite, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isBusinessVip) {
                Button(
                    onClick = onSubmitOfferClick,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تقديم عرض Business VIP لهذا الطلب", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onUpgradeVipClick,
                    border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تقديم العروض متاح لتجار Business VIP (ترقية)", color = RoyalGoldLight, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CouponCard(
    coupon: DiscountCoupon,
    onShowQr: () -> Unit
) {
    GlassLuxuryCard(borderColor = RoyalGold.copy(alpha = 0.55f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ConfirmationNumber, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(coupon.code, style = MaterialTheme.typography.titleMedium, color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                }
                Text(coupon.title, style = MaterialTheme.typography.titleSmall, color = CrispWhite, fontWeight = FontWeight.Bold)
                Text("${coupon.merchantName} · ${coupon.city}", style = MaterialTheme.typography.labelSmall, color = CoolSilver)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GoldTagPill(backgroundColor = RoyalGold, borderColor = RoyalGoldLight) {
                    Text(
                        text = "خصم ${coupon.discountPercent}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = ObsidianDeep,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                OutlinedButton(
                    onClick = onShowQr,
                    border = BorderStroke(1.dp, ElectricAzure.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.QrCode2, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض QR", color = CrispWhite, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun QrCouponViewerDialog(
    coupon: DiscountCoupon,
    onDismiss: () -> Unit
) {
    val qrPayload = "KEMETCOUPON:${coupon.code}|${coupon.discountPercent}%|${coupon.merchantName}"
    val qrBitmap = remember(qrPayload) { generateQrImageBitmap(qrPayload, 512) }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
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
                            .size(220.dp)
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
                Text("الكود: ${coupon.code} · خصم ${coupon.discountPercent}%", color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                Text("أبرز هذا الرمز للتاجر (${coupon.merchantName}) للحصول على الخصم الفوري.", style = MaterialTheme.typography.bodySmall, color = CoolSilver)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep)
            ) {
                Text("تم", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun CreateConciergeRequestDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (ListingCategory, String, String, Long, String) -> Unit
) {
    var category by remember { mutableStateOf(ListingCategory.TRIP) }
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("القاهرة") }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = { Text("طلب خاص لصندوق الكونسيرج", color = CrispWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ListingCategory.entries) { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat.labelAr) })
                    }
                }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان الطلب الخاص") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("التفاصيل والمواصفات") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it.filter { c -> c.isDigit() } },
                        label = { Text("الميزانية (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("المدينة") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(category, title, details, budgetText.toLongOrNull() ?: 0L, city) },
                enabled = !isLoading && title.trim().length >= 2 && details.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep)
            ) {
                Text(if (isLoading) "جارٍ الإرسال..." else "إرسال لتجار VIP", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء", color = CrispWhite) } }
    )
}

@Composable
private fun SubmitVipOfferDialog(
    request: ConciergeRequest,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Long, String, String) -> Unit
) {
    var priceText by remember { mutableStateOf(request.budgetEgp.toString()) }
    var proposal by remember { mutableStateOf("") }
    var whatsappPhone by remember { mutableStateOf("+201006256085") }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = { Text("تقديم عرض Business VIP", color = CrispWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("الطلب: ${request.title}", color = RoyalGoldLight, style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("السعر المقترح (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = proposal, onValueChange = { proposal = it }, label = { Text("تفاصيل عرضك ومميزاته") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = whatsappPhone, onValueChange = { whatsappPhone = it }, label = { Text("رقم واتساب للتواصل") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(priceText.toLongOrNull() ?: 0L, proposal, whatsappPhone) },
                enabled = !isLoading && proposal.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep)
            ) {
                Text(if (isLoading) "جارٍ الإرسال..." else "إرسال العرض", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء", color = CrispWhite) } }
    )
}

@Composable
private fun CreateCouponDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Long, String) -> Unit
) {
    var code by remember { mutableStateOf("KEMET25") }
    var title by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("25") }
    var city by remember { mutableStateOf("القاهرة") }

    AlertDialog(
        containerColor = MidnightGlass,
        onDismissRequest = onDismiss,
        title = { Text("توليد كوبون خصم QR Code", color = CrispWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("كود الكوبون (مثل KEMET25)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("وصف الخصم (مثل خصم 25% على التحف)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it.filter { c -> c.isDigit() } },
                        label = { Text("نسبة الخصم %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("المدينة") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(code, title, discountText.toLongOrNull() ?: 10L, city) },
                enabled = !isLoading && code.trim().length >= 3 && title.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = ObsidianDeep)
            ) {
                Text(if (isLoading) "جارٍ التوليد..." else "توليد رمز QR", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء", color = CrispWhite) } }
    )
}
