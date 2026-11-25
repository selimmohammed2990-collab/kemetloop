package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ConciergeOffer
import com.example.model.ConciergeRequest
import com.example.model.DiscountCoupon
import com.example.model.ListingCategory
import com.example.model.TranslatorLanguage
import com.example.model.UserProfile
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.viewmodel.TranslatorUiState
import com.example.viewmodel.UiState
import kotlinx.coroutines.flow.Flow

/**
 * Unified Travel Hub combining Concierge & QR Coupons & Offline Guide with the AI Smart Translator
 * under the 5-tab floating navigation bar ("السفر").
 */
@Composable
fun TravelHubScreen(
    profile: UserProfile?,
    isOnline: Boolean,
    initialSection: Int,
    conciergeState: UiState<List<ConciergeRequest>>,
    couponsState: UiState<List<DiscountCoupon>>,
    favoriteLandmarkIds: Set<String>,
    translatorState: TranslatorUiState,
    isActionLoading: Boolean,
    observeOffers: (requestId: String, requesterUid: String) -> Flow<List<ConciergeOffer>>,
    onCreateConciergeRequest: (ListingCategory, String, String, Long, String, () -> Unit) -> Unit,
    onSubmitConciergeOffer: (ConciergeRequest, Long, String, String, () -> Unit) -> Unit,
    onCreateCoupon: (String, String, Long, String, () -> Unit) -> Unit,
    onQrCouponScanned: (String) -> Unit,
    onToggleFavoriteLandmark: (String, String) -> Unit,
    onTranslate: (String, TranslatorLanguage, TranslatorLanguage) -> Unit,
    onTranslateImage: (Bitmap, TranslatorLanguage) -> Unit,
    onNavigateToVip: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    // 0: Concierge/Guide/Coupons (subtab tracked), 1: Smart AI Translator
    var mainTravelSection by rememberSaveable { mutableIntStateOf(if (initialSection == 3) 1 else 0) }
    var conciergeInitialSubTab by rememberSaveable { mutableIntStateOf(if (initialSection in 0..2) initialSection else 0) }

    LaunchedEffect(initialSection) {
        if (initialSection == 3) {
            mainTravelSection = 1
        } else if (initialSection in 0..2) {
            mainTravelSection = 0
            conciergeInitialSubTab = initialSection
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = mainTravelSection == 0,
                onClick = { mainTravelSection = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = RoyalGold,
                    activeContentColor = ObsidianDeep
                ),
                modifier = Modifier.testTag("travel_hub_concierge_tab")
            ) {
                Text("الرحلات، الكونسيرج والدليل", maxLines = 1, fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = mainTravelSection == 1,
                onClick = { mainTravelSection = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = RoyalGold,
                    activeContentColor = ObsidianDeep
                ),
                modifier = Modifier.testTag("travel_hub_translator_tab")
            ) {
                Text("المترجم الذكي والكاميرا", maxLines = 1, fontWeight = FontWeight.Bold)
            }
        }

        if (mainTravelSection == 0) {
            ConciergeCouponsScreen(
                profile = profile,
                isOnline = isOnline,
                conciergeState = conciergeState,
                couponsState = couponsState,
                favoriteLandmarkIds = favoriteLandmarkIds,
                initialTab = conciergeInitialSubTab,
                isActionLoading = isActionLoading,
                observeOffers = observeOffers,
                onCreateConciergeRequest = onCreateConciergeRequest,
                onSubmitConciergeOffer = onSubmitConciergeOffer,
                onCreateCoupon = onCreateCoupon,
                onQrCouponScanned = onQrCouponScanned,
                onToggleFavoriteLandmark = onToggleFavoriteLandmark,
                onNavigateToVip = onNavigateToVip,
                onShowMessage = onShowMessage
            )
        } else {
            TranslatorScreen(
                profile = profile,
                isOnline = isOnline,
                translatorState = translatorState,
                onTranslate = onTranslate,
                onTranslateImage = onTranslateImage,
                onNavigateToVip = onNavigateToVip,
                onShowMessage = onShowMessage
            )
        }
    }
}
