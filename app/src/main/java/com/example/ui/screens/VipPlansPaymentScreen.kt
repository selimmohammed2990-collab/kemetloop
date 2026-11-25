package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.RewardsState
import com.example.data.SafeIntentHelper
import com.example.model.BillingCycle
import com.example.model.PaymentSubmission
import com.example.model.SenderWalletType
import com.example.model.SubscriptionPlan
import com.example.model.UserProfile
import com.example.ui.components.AdMobRewardedCallout
import com.example.ui.components.GlassLuxuryCard
import com.example.ui.components.KemetGoldCoinPill
import com.example.ui.components.ProCrownBadge
import com.example.ui.components.VerifiedGoldStarBadge
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepSkyBlue
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.EmeraldVerified
import com.example.ui.theme.MidnightElevated
import com.example.ui.theme.MidnightGlassSoft
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TerracottaError
import com.example.ui.theme.VodafoneRed
import com.example.viewmodel.UiState

@Composable
fun VipPlansPaymentScreen(
    profile: UserProfile?,
    isGuestMode: Boolean,
    submissionsState: UiState<List<PaymentSubmission>>,
    isActionLoading: Boolean,
    rewardsState: RewardsState? = null,
    onNavigateBack: (() -> Unit)? = null,
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
    var selectedUpgradePlan by remember { mutableStateOf(SubscriptionPlan.PRO) }
    var selectedBillingCycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var senderWalletType by remember { mutableStateOf(SenderWalletType.VODAFONE_CASH) }
    var txReference by remember { mutableStateOf("") }
    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }

    if (onNavigateBack != null) {
        BackHandler(onBack = onNavigateBack)
    }

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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Plan & Account Header
        item {
            GlassLuxuryCard(borderColor = RoyalGold) {
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
                            if (onNavigateBack != null) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MidnightGlassSoft)
                                        .testTag("vip_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "الرجوع للرئيسية",
                                        tint = RoyalGoldLight
                                    )
                                }
                            } else {
                                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = RoyalGoldLight)
                            }
                            Text(
                                text = "الباقات والأسعار (VIP)",
                                style = MaterialTheme.typography.headlineSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        if (!isGuestMode) {
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = "تسجيل الخروج", tint = CrispWhite)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (basePlan == SubscriptionPlan.FREE_STARTER && rewardsState?.proTrialActive == true) {
                                    "تجربة Pro مجانية (متبقي ${rewardsState.proTrialDaysRemaining} أيام)"
                                } else {
                                    "باقتك: ${activePlan.labelAr}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            if (activePlan == SubscriptionPlan.PRO) {
                                ProCrownBadge(iconSize = 20.dp)
                            }
                            if (profile?.verifiedBusiness == true || activePlan == SubscriptionPlan.BUSINESS_VIP) {
                                VerifiedGoldStarBadge(iconSize = 20.dp)
                            }
                        }

                        KemetGoldCoinPill(coins = profile?.coins ?: 100L, showLabelSuffix = true)
                    }

                    val referralCode = profile?.referralCode ?: "KEMET100"
                    OutlinedButton(
                        onClick = {
                            SafeIntentHelper.shareInviteWithReferral(
                                context = context,
                                referralCode = referralCode,
                                onError = onShowMessage
                            )
                            onInviteAffiliate(referralCode)
                        },
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vip_share_referral_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = RoyalGoldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشاركة التطبيق بكود الدعوة ($referralCode) (+50 عملة)",
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
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
                                text = "ربط حساب Google لحفظ نقاطك واشتراكاتك بشكل دائم",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

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
}

fun LazyListScope.vipCheckoutAndDeveloperItems(
    activePlan: SubscriptionPlan,
    selectedUpgradePlan: SubscriptionPlan,
    onSelectUpgradePlan: (SubscriptionPlan) -> Unit,
    selectedBillingCycle: BillingCycle,
    onSelectBillingCycle: (BillingCycle) -> Unit,
    senderWalletType: SenderWalletType,
    onSelectSenderWalletType: (SenderWalletType) -> Unit,
    txReference: String,
    onTxReferenceChange: (String) -> Unit,
    selectedReceiptUri: Uri?,
    onPickReceiptClick: () -> Unit,
    submissionsState: UiState<List<PaymentSubmission>>,
    isActionLoading: Boolean,
    walletLocal: String,
    walletIntl: String,
    developerName: String,
    developerEmail: String,
    developerWhatsappUrl: String,
    context: android.content.Context,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    onSubmitPayment: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onShowMessage: (String) -> Unit
) {
        // Billing Cycle Switch
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedBillingCycle == BillingCycle.MONTHLY,
                    onClick = { onSelectBillingCycle(BillingCycle.MONTHLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = RoyalGold,
                        activeContentColor = ObsidianDeep
                    )
                ) {
                    Text("اشتراك شهري", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = selectedBillingCycle == BillingCycle.YEARLY,
                    onClick = { onSelectBillingCycle(BillingCycle.YEARLY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = RoyalGold,
                        activeContentColor = ObsidianDeep
                    )
                ) {
                    Text("اشتراك سنوي (وفر أكثر)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3 High-Contrast Pricing Cards
        item {
            HighContrastPricingCard(
                title = "الباقة المجانية (Free Starter)",
                primaryPriceText = "0 ج.م / مجاناً للأبد",
                secondaryPriceText = "تشمل 100 عملة ترحيبية مجاناً",
                yearlyPriceText = null,
                isActive = activePlan == SubscriptionPlan.FREE_STARTER,
                isSelectedForCheckout = false,
                accentColor = EmeraldVerified,
                highContrastGoldSurface = false,
                topBannerText = null,
                badgeContent = {
                    KemetGoldCoinPill(coins = 100L, showLabelSuffix = true)
                },
                features = listOf(
                    "تشمل 100 عملة ترحيبية فور الانضمام",
                    "استخدام منصة السفر والسوق والمترجم الذكي",
                    "حافظة الملفات الآمنة وصندوق الأحلام والروابط"
                ),
                buttonLabel = if (activePlan == SubscriptionPlan.FREE_STARTER) "باقتك الحالية ✓" else "الباقة الأساسية",
                testTagName = "pricing_card_free",
                onClick = null
            )
        }

        item {
            HighContrastPricingCard(
                title = "باقة الأفراد (KemetLoop Pro)",
                primaryPriceText = "99 ج.م / شهرياً",
                secondaryPriceText = "أو 4.99 دولار شهرياً",
                yearlyPriceText = "899 ج.م / سنوياً",
                isActive = activePlan == SubscriptionPlan.PRO,
                isSelectedForCheckout = selectedUpgradePlan == SubscriptionPlan.PRO,
                accentColor = ElectricAzure,
                highContrastGoldSurface = false,
                topBannerText = null,
                badgeContent = {
                    ProCrownBadge(showLabel = true, labelText = "وسام Pro")
                },
                features = listOf(
                    "إزالة الإعلانات تماماً من جميع الشاشات",
                    "500 عملة شهرية + وسام التاج الملكي Pro",
                    "ترجمة صوتية وبالكاميرا مفتوحة بلا حدود",
                    "أولوية في الدعم وعروض السفر الخاصة"
                ),
                buttonLabel = if (selectedUpgradePlan == SubscriptionPlan.PRO) "محددة للدفع أدناه ✓" else "اختر باقة Pro (99 ج.م)",
                testTagName = "pricing_card_pro",
                onClick = { onSelectUpgradePlan(SubscriptionPlan.PRO) }
            )
        }

        item {
            HighContrastPricingCard(
                title = "باقة التجار والشركات (KemetLoop Business VIP)",
                primaryPriceText = "299 ج.م / شهرياً",
                secondaryPriceText = "أو 14.99 دولار شهرياً",
                yearlyPriceText = "2,499 ج.م / سنوياً",
                isActive = activePlan == SubscriptionPlan.BUSINESS_VIP,
                isSelectedForCheckout = selectedUpgradePlan == SubscriptionPlan.BUSINESS_VIP,
                accentColor = RoyalGold,
                highContrastGoldSurface = true,
                topBannerText = "عرض الإطلاق للرواد: شهر مجاني إضافي لأول 100 نشاط تجاري وسياحي ينضم للمنصة",
                badgeContent = {
                    VerifiedGoldStarBadge(showLabel = true, labelText = "توثيق ذهبي")
                },
                features = listOf(
                    "شارة التوثيق الذهبية بجانب اسم النشاط التجاري",
                    "تثبيت العروض والمنتجات في الأعلى (Featured Ads)",
                    "إصدار كوبونات QR للخصم + ربط مباشر بواتساب",
                    "1,000 عملة شهرية + الرد على طلبات الكونسيرج"
                ),
                buttonLabel = if (selectedUpgradePlan == SubscriptionPlan.BUSINESS_VIP) "محددة للدفع أدناه ✓" else "اختر Business VIP (299 ج.م)",
                testTagName = "pricing_card_business_vip",
                onClick = { onSelectUpgradePlan(SubscriptionPlan.BUSINESS_VIP) }
            )
        }

        // Universal Payment Gateway UI (Local Egyptian Wallets + InstaPay + International Fiat/Banking)
        item {
            UniversalPaymentGatewayCard(
                selectedUpgradePlan = selectedUpgradePlan,
                selectedBillingCycle = selectedBillingCycle,
                senderWalletType = senderWalletType,
                onSelectSenderWalletType = onSelectSenderWalletType,
                txReference = txReference,
                onTxReferenceChange = onTxReferenceChange,
                selectedReceiptUri = selectedReceiptUri,
                onPickReceiptClick = onPickReceiptClick,
                isActionLoading = isActionLoading,
                walletLocal = walletLocal,
                walletIntl = walletIntl,
                developerWhatsappUrl = developerWhatsappUrl,
                context = context,
                clipboard = clipboard,
                onSubmitPayment = onSubmitPayment,
                onShowMessage = onShowMessage
            )
        }

        // Technical Support, Video Contact & About / Copyright Card (Selim Mohamed Selim)
        item {
            GlassLuxuryCard(
                borderColor = RoyalGold,
                containerColor = DeepSkyBlue.copy(alpha = 0.25f),
                modifier = Modifier.testTag("about_and_support_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = RoyalGoldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الدعم الفني والتواصل المرئي",
                            style = MaterialTheme.typography.titleLarge,
                            color = CrispWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "تواصل مباشرة مع المطور ($developerName) للحصول على الدعم الفني أو استفسارات الباقات والتجارة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CoolSilver
                    )

                    // Clickable Developer Email + 1-Tap Copy
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MidnightGlassSoft,
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$developerEmail")
                                    putExtra(Intent.EXTRA_SUBJECT, "KemetLoop Support")
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(mailIntent, "مراسلة المطور"))
                                }
                            }
                            .testTag("developer_email_clickable")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = "البريد الإلكتروني الرسمي",
                                    tint = RoyalGoldLight
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "البريد الإلكتروني الرسمي للمطور",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CoolSilver
                                    )
                                    Text(
                                        text = developerEmail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CrispWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(developerEmail))
                                    onShowMessage("تم نسخ البريد الإلكتروني: $developerEmail")
                                },
                                modifier = Modifier.testTag("copy_developer_email_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "نسخ البريد الإلكتروني",
                                    tint = RoyalGoldLight
                                )
                            }
                        }
                    }

                    // Prominent WhatsApp Direct Chat & Video Call Button
                    Button(
                        onClick = {
                            SafeIntentHelper.openWhatsApp(context, developerWhatsappUrl, onShowMessage)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldVerified,
                            contentColor = CrispWhite
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("whatsapp_video_support_button")
                    ) {
                        Icon(Icons.Filled.VideoCall, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تواصل مباشر ومكالمات فيديو عبر واتساب ($walletIntl)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Secret Developer Admin Panel Entry
                    OutlinedButton(
                        onClick = onOpenAdminPanel,
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_admin_panel_button")
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = RoyalGoldLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "لوحة تحكم المطور السرية (إدارة VIP وتوثيق التجار)",
                            color = RoyalGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // About & Copyright Section
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianDeep.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = RoyalGoldLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حول التطبيق وحقوق الملكية (About & Copyright)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Copyright, contentDescription = null, tint = CoolSilver, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "جميع الحقوق محفوظة © 2026 للمطور $developerName - تطبيق KemetLoop",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CoolSilver
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rewarded Ad option on Free plan
        item {
            AdMobRewardedCallout(
                activePlan = activePlan,
                onWatchRewardedAd = onWatchRewardedAd
            )
        }

        val submissions = (submissionsState as? UiState.Success)?.data.orEmpty()
        if (submissions.isNotEmpty()) {
            item {
                Text(
                    text = "سجل تأكيدات الدفع السابقة",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            items(submissions, key = { it.id }) { sub ->
                val statusLabel: String
                val statusColor: Color
                when (sub.status.uppercase()) {
                    "APPROVED", "CONFIRMED" -> {
                        statusLabel = "مفعّل ✓"
                        statusColor = EmeraldVerified
                    }
                    "REJECTED" -> {
                        statusLabel = "مرفوض"
                        statusColor = TerracottaError
                    }
                    else -> {
                        statusLabel = "قيد المراجعة ⏳"
                        statusColor = RoyalGoldLight
                    }
                }
                GlassLuxuryCard(contentPadding = PaddingValues(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = statusColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${SubscriptionPlan.fromRaw(sub.targetPlan).labelAr} • ${sub.amountEgp} ج.م",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CrispWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${SenderWalletType.fromRaw(sub.senderWalletType).labelAr} • مرجع: ${sub.txReference.ifBlank { "إيصال مرفق" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoolSilver
                                )
                            }
                        }
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
}

@Composable
private fun HighContrastPricingCard(
    title: String,
    primaryPriceText: String,
    secondaryPriceText: String,
    yearlyPriceText: String?,
    isActive: Boolean,
    isSelectedForCheckout: Boolean,
    accentColor: Color,
    highContrastGoldSurface: Boolean,
    topBannerText: String?,
    badgeContent: @Composable () -> Unit,
    features: List<String>,
    buttonLabel: String,
    testTagName: String,
    onClick: (() -> Unit)?
) {
    val cardBorderColor = when {
        isSelectedForCheckout || highContrastGoldSurface -> RoyalGoldLight
        isActive -> EmeraldVerified
        else -> accentColor
    }
    val cardBackground = if (highContrastGoldSurface) {
        MidnightElevated
    } else {
        ObsidianDeep.copy(alpha = 0.94f)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(if (isSelectedForCheckout || highContrastGoldSurface) 2.dp else 1.5.dp, cardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagName)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Prominent Golden Launch Offer Banner (100 Pioneers) at the very top of Business VIP card
            if (!topBannerText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RoyalGold,
                    border = BorderStroke(1.dp, RoyalGoldLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pioneers_100_offer_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = ObsidianDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = topBannerText,
                            style = MaterialTheme.typography.labelLarge,
                            color = ObsidianDeep,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Title + Custom Royal Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = CrispWhite,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                badgeContent()
            }

            // High-Contrast Bold Pricing Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (highContrastGoldSurface) RoyalGold.copy(alpha = 0.16f) else MidnightGlassSoft,
                border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = primaryPriceText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = RoyalGoldLight,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = secondaryPriceText,
                            style = MaterialTheme.typography.labelLarge,
                            color = CrispWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!yearlyPriceText.isNullOrBlank()) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldVerified.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, EmeraldVerified)
                            ) {
                                Text(
                                    text = yearlyPriceText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EmeraldVerified,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Crisp Benefits List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                features.forEach { feat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldVerified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feat,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrispWhite
                        )
                    }
                }
            }

            if (onClick != null) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelectedForCheckout || highContrastGoldSurface) RoyalGold else DeepSkyBlue,
                        contentColor = if (isSelectedForCheckout || highContrastGoldSurface) ObsidianDeep else CrispWhite
                    ),
                    border = if (!isSelectedForCheckout && !highContrastGoldSurface) {
                        BorderStroke(1.dp, ElectricAzure)
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(buttonLabel, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldVerified.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, EmeraldVerified),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = buttonLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldVerified,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UniversalPaymentGatewayCard(
    selectedUpgradePlan: SubscriptionPlan,
    selectedBillingCycle: BillingCycle,
    senderWalletType: SenderWalletType,
    onSelectSenderWalletType: (SenderWalletType) -> Unit,
    txReference: String,
    onTxReferenceChange: (String) -> Unit,
    selectedReceiptUri: Uri?,
    onPickReceiptClick: () -> Unit,
    isActionLoading: Boolean,
    walletLocal: String,
    walletIntl: String,
    developerWhatsappUrl: String,
    context: android.content.Context,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    onSubmitPayment: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val amountEgp = if (selectedBillingCycle == BillingCycle.YEARLY) {
        selectedUpgradePlan.yearlyEgp
    } else {
        selectedUpgradePlan.monthlyEgp
    }
    val amountUsd = if (selectedBillingCycle == BillingCycle.YEARLY) {
        selectedUpgradePlan.yearlyUsd
    } else {
        selectedUpgradePlan.monthlyUsd
    }
    val localOptions = remember { SenderWalletType.entries.filter { !it.isInternational } }
    val intlOptions = remember { SenderWalletType.entries.filter { it.isInternational } }

    var gatewayTab by remember(senderWalletType) {
        mutableIntStateOf(if (senderWalletType.isInternational) 1 else 0)
    }
    var showIntlFastPayDialog by remember { mutableStateOf(false) }

    GlassLuxuryCard(
        borderColor = RoyalGold,
        modifier = Modifier.testTag("vodafone_cash_payment_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Gateway Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    tint = RoyalGoldLight
                )
                Column {
                    Text(
                        text = "بوابة الدفع الموحدة (المحلية والدولية)",
                        style = MaterialTheme.typography.titleLarge,
                        color = CrispWhite,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "جميع المحافظ المصرية • إنستا باي • البطاقات البنكية • PayPal • Payoneer",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoolSilver
                    )
                }
            }

            // 1. Unified Egypt Local Receipt Number Box (01006256085) + All Egyptian Wallets & InstaPay Icons
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = VodafoneRed.copy(alpha = 0.12f),
                border = BorderStroke(1.2.dp, VodafoneRed.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "رقم الاستلام الموحد في مصر (جميع المحافظ وإنستا باي):",
                                style = MaterialTheme.typography.labelMedium,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = walletLocal,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF6B6B)
                            )
                            Text(
                                text = "دولياً: $walletIntl",
                                style = MaterialTheme.typography.labelSmall,
                                color = CoolSilver
                            )
                        }
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(walletLocal))
                                onShowMessage("تم نسخ رقم الاستلام الموحد: $walletLocal")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VodafoneRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("copy_vodafone_cash_button")
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نسخ الرقم", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Icons Strip of All Local Egyptian Wallets & InstaPay & Bank Apps
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(localOptions, key = { "badge_${it.name}" }) { wType ->
                            PaymentMethodMiniBadge(
                                shortTag = wType.shortTag,
                                label = wType.labelAr.substringBefore(" ("),
                                accent = Color(wType.badgeColorHex),
                                isSelected = senderWalletType == wType,
                                onClick = {
                                    gatewayTab = 0
                                    onSelectSenderWalletType(wType)
                                }
                            )
                        }
                    }

                    Text(
                        text = "أرسل من فودافون كاش، أورنج كاش، اتصالات كاش (e& Money)، وي باي (WE Pay)، إنستا باي (InstaPay)، أو أي تطبيق بنكي مصري باختيار «التحويل لمحفظة إلكترونية».",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoolSilver
                    )
                }
            }

            // 2. International Banking & Fiat Options Section (Visa / Mastercard / PayPal / Payoneer / SWIFT)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DeepSkyBlue.copy(alpha = 0.28f),
                border = BorderStroke(1.2.dp, ElectricAzure.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("international_payment_box")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                            Icon(Icons.Filled.Public, contentDescription = null, tint = ElectricAzure)
                            Text(
                                text = "الدفع الدولي للسياح وحول العالم",
                                style = MaterialTheme.typography.titleMedium,
                                color = CrispWhite,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = RoyalGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, RoyalGold)
                        ) {
                            Text(
                                text = "$$amountUsd USD",
                                style = MaterialTheme.typography.labelMedium,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(intlOptions, key = { "badge_${it.name}" }) { wType ->
                            PaymentMethodMiniBadge(
                                shortTag = wType.shortTag,
                                label = wType.labelAr.substringBefore(" ("),
                                accent = Color(wType.badgeColorHex),
                                isSelected = senderWalletType == wType,
                                onClick = {
                                    gatewayTab = 1
                                    onSelectSenderWalletType(wType)
                                }
                            )
                        }
                    }

                    Button(
                        onClick = { showIntlFastPayDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricAzure,
                            contentColor = CrispWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("intl_fast_pay_button")
                    ) {
                        Icon(Icons.Filled.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الدفع الدولي السريع (Visa / Mastercard / PayPal / Payoneer)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            HorizontalDivider(color = RoyalGold.copy(alpha = 0.3f))

            // 3. Universal Payment Confirmation Form (نموذج تأكيد الدفع الموحد)
            Text(
                text = "نموذج تأكيد الدفع الموحد",
                style = MaterialTheme.typography.titleMedium,
                color = RoyalGoldLight,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "المبلغ المطلوب لباقة ${selectedUpgradePlan.labelAr} (${selectedBillingCycle.labelAr}): $amountEgp ج.م (ما يعادل $$amountUsd دولار)",
                style = MaterialTheme.typography.bodyMedium,
                color = CrispWhite,
                fontWeight = FontWeight.Bold
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = gatewayTab == 0,
                    onClick = {
                        gatewayTab = 0
                        if (senderWalletType.isInternational) {
                            onSelectSenderWalletType(SenderWalletType.VODAFONE_CASH)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = RoyalGold,
                        activeContentColor = ObsidianDeep
                    )
                ) {
                    Text("محلي بمصر", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = gatewayTab == 1,
                    onClick = {
                        gatewayTab = 1
                        if (!senderWalletType.isInternational) {
                            onSelectSenderWalletType(SenderWalletType.VISA_MASTERCARD)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = ElectricAzure,
                        activeContentColor = CrispWhite
                    )
                ) {
                    Text("دولي وبنكي", fontWeight = FontWeight.Bold)
                }
            }

            val visibleMethods = if (gatewayTab == 0) localOptions else intlOptions
            Text("اختر وسيلة الدفع المستخدمة:", style = MaterialTheme.typography.labelLarge, color = CrispWhite)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibleMethods, key = { "chip_${it.name}" }) { wType ->
                    FilterChip(
                        selected = senderWalletType == wType,
                        onClick = { onSelectSenderWalletType(wType) },
                        label = { Text(wType.labelAr) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(wType.badgeColorHex),
                            selectedLabelColor = CrispWhite
                        )
                    )
                }
            }

            OutlinedTextField(
                value = txReference,
                onValueChange = onTxReferenceChange,
                label = {
                    Text(
                        if (senderWalletType.isInternational) {
                            "رقم المرجع البنكي / معرف عملية PayPal أو Payoneer"
                        } else {
                            "رقم عملية التحويل (أو آخر 4-10 أرقام للعملية)"
                        }
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_reference_input")
            )

            OutlinedButton(
                onClick = onPickReceiptClick,
                border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("upload_receipt_button")
            ) {
                Icon(Icons.Filled.UploadFile, contentDescription = null, tint = RoyalGoldLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedReceiptUri == null) "رفع صورة إيصال الدفع أو التحويل (اختياري)" else "تغيير صورة الإيصال المرفقة",
                    color = CrispWhite
                )
            }

            if (selectedReceiptUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedReceiptUri),
                        contentDescription = "صورة إيصال التحويل",
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "تم إرفاق صورة الإيصال بنجاح ✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldVerified
                    )
                }
            }

            Button(
                onClick = onSubmitPayment,
                enabled = !isActionLoading && (txReference.trim().length >= 4 || selectedReceiptUri != null),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = ObsidianDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_vodafone_payment_button")
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isActionLoading) "جارٍ التأكيد..." else "تأكيد الدفع الموحد وإرسال الطلب للتفعيل",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    if (showIntlFastPayDialog) {
        InternationalFastPayDialog(
            selectedPlan = selectedUpgradePlan,
            selectedCycle = selectedBillingCycle,
            amountEgp = amountEgp,
            amountUsd = amountUsd,
            developerWhatsappUrl = developerWhatsappUrl,
            onOpenWhatsApp = {
                val msg = Uri.encode(
                    "مرحباً، أرغب في تفعيل باقة ${selectedUpgradePlan.labelAr} (${selectedBillingCycle.labelAr}) بقيمة $$amountUsd عبر الدفع الدولي السريع (Visa/Mastercard/PayPal/Payoneer). يرجى إرسال رابط الدفع المباشر لبلدي."
                )
                val targetUrl = "$developerWhatsappUrl?text=$msg"
                SafeIntentHelper.openWhatsApp(context, targetUrl, onShowMessage)
                showIntlFastPayDialog = false
            },
            onDismiss = { showIntlFastPayDialog = false }
        )
    }
}

@Composable
private fun PaymentMethodMiniBadge(
    shortTag: String,
    label: String,
    accent: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accent.copy(alpha = 0.28f) else MidnightElevated,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, accent.copy(alpha = 0.8f)),
        modifier = Modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shortTag,
                    style = MaterialTheme.typography.labelSmall,
                    color = CrispWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CrispWhite,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InternationalFastPayDialog(
    selectedPlan: SubscriptionPlan,
    selectedCycle: BillingCycle,
    amountEgp: Long,
    amountUsd: String,
    developerWhatsappUrl: String,
    onOpenWhatsApp: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        containerColor = ObsidianDeep,
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Public, contentDescription = null, tint = ElectricAzure)
                Text(
                    text = "الدفع الدولي السريع",
                    style = MaterialTheme.typography.titleLarge,
                    color = CrispWhite,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MidnightElevated,
                    border = BorderStroke(1.dp, RoyalGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "الباقة المختارة: ${selectedPlan.labelAr} (${selectedCycle.labelAr})",
                            style = MaterialTheme.typography.titleSmall,
                            color = RoyalGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "القيمة المطلوبة: $$amountUsd دولار (أو $amountEgp ج.م)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrispWhite,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "الوسائل البنكية والدولية المعتمدة:",
                    style = MaterialTheme.typography.labelLarge,
                    color = RoyalGoldLight,
                    fontWeight = FontWeight.Bold
                )
                IntlStepLine(Icons.Filled.CreditCard, "بطاقات Visa / Mastercard العالمية عبر رابط الدفع الآمن")
                IntlStepLine(Icons.Filled.AccountBalanceWallet, "PayPal و Payoneer بالعملات الأجنبية المعتمدة")
                IntlStepLine(Icons.Filled.AccountBalance, "التحويلات البنكية الدولية (SWIFT / IBAN)")

                Text(
                    text = "اضغط على الزر بالأسفل للتواصل الفوري عبر واتساب ($developerWhatsappUrl) لإرسال رابط الدفع المباشر المناسب لبلدك وتفعيل باقتك فوراً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoolSilver
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenWhatsApp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldVerified,
                    contentColor = CrispWhite
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("intl_whatsapp_direct_pay_btn")
            ) {
                Icon(Icons.Filled.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("طلب رابط الدفع المباشر عبر واتساب", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = CoolSilver)
            }
        }
    )
}

@Composable
private fun IntlStepLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = ElectricAzure, modifier = Modifier.size(18.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = CrispWhite)
    }
}
