package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.onGoogleSignInClicked
import com.example.auth.signOutUser
import com.example.data.AdminSecurityStore
import com.example.data.AiTranslatorRepository
import com.example.data.FileVaultStore
import com.example.data.GuestLocalStore
import com.example.data.KemetCommunityRepository
import com.example.data.KemetRepository
import com.example.data.OfflineCacheStore
import com.example.data.SafeIntentHelper
import com.example.data.UserPreferencesStore
import com.example.firebase.configureAppCheck
import com.example.model.SubscriptionPlan
import com.example.ui.components.AddDreamLinkDialog
import com.example.ui.components.AiVoiceAssistantSheet
import com.example.ui.components.AmbientLuxuryBackground
import com.example.ui.components.FloatingEdgeDrawerHandle
import com.example.ui.components.InternationalAppsDrawerSheet
import com.example.ui.components.LiveRoomsDialog
import com.example.ui.components.RewardedAdModal
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DeveloperAdminDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SmartWebViewScreen
import com.example.ui.screens.SosEssentialsSheet
import com.example.ui.screens.TalentsEdutainmentScreen
import com.example.ui.screens.TravelHubScreen
import com.example.ui.screens.VipPlansPaymentScreen
import com.example.ui.theme.CoolSilver
import com.example.ui.theme.KemetLoopTheme
import com.example.ui.theme.MidnightGlass
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.viewmodel.KemetViewModel
import com.example.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

object KemetRoutes {
    const val HOME = "home"
    const val TRAVEL = "travel"
    const val MARKET = "market"
    const val TALENTS = "talents"
    const val PROFILE = "profile"
    const val VIP = "vip"
}

private data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            KemetLoopTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = ObsidianDeep) {
                    KemetAppRoot()
                }
            }
        }
    }
}

@Composable
fun KemetAppRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isGuestSession by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                isGuestSession = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null && !isGuestSession) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                isGuestSession = false
            },
            onContinueAsGuest = {
                isGuestSession = true
            }
        )
    } else {
        val sessionKey = user?.uid ?: GuestLocalStore.GUEST_UID
        key(sessionKey) {
            KemetAuthenticatedShell(
                isGuestMode = user == null,
                currentUserId = sessionKey,
                displayName = user?.displayName ?: "زائر KemetLoop",
                email = user?.email.orEmpty(),
                photoUrl = user?.photoUrl?.toString(),
                onExitGuestToAuth = { isGuestSession = false }
            )
        }
    }
}

@Composable
fun KemetAuthenticatedShell(
    isGuestMode: Boolean,
    currentUserId: String,
    displayName: String,
    email: String,
    photoUrl: String?,
    onExitGuestToAuth: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: KemetViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val db = if (isGuestMode) {
                    null
                } else {
                    val databaseId = app.getString(R.string.firestore_database_id)
                    FirebaseFirestore.getInstance(databaseId)
                }
                KemetViewModel(
                    repository = db?.let { KemetRepository(it) },
                    communityRepository = db?.let { KemetCommunityRepository(it) },
                    guestStore = GuestLocalStore(app),
                    offlineCacheStore = OfflineCacheStore(app),
                    adminSecurityStore = AdminSecurityStore(app),
                    userPreferencesStore = UserPreferencesStore(app),
                    fileVaultStore = FileVaultStore(app),
                    translatorRepository = AiTranslatorRepository(),
                    isGuestMode = isGuestMode,
                    currentUserId = currentUserId,
                    initialDisplayName = displayName,
                    initialEmail = email
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val listingsState by viewModel.listingsState.collectAsStateWithLifecycle()
    val talentsState by viewModel.talentsState.collectAsStateWithLifecycle()
    val savedLinksState by viewModel.savedLinksState.collectAsStateWithLifecycle()
    val submissionsState by viewModel.paymentSubmissionsState.collectAsStateWithLifecycle()
    val affiliateLogsState by viewModel.affiliateLogsState.collectAsStateWithLifecycle()
    val conciergeRequestsState by viewModel.conciergeRequestsState.collectAsStateWithLifecycle()
    val couponsState by viewModel.couponsState.collectAsStateWithLifecycle()
    val vipRequestsState by viewModel.vipRequestsState.collectAsStateWithLifecycle()
    val translatorState by viewModel.translatorState.collectAsStateWithLifecycle()
    val edutainmentState by viewModel.edutainmentState.collectAsStateWithLifecycle()
    val favoriteLandmarkIds by viewModel.favoriteLandmarkIds.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val vaultFiles by viewModel.vaultFiles.collectAsStateWithLifecycle()
    val vaultAutoCloudSync by viewModel.vaultAutoCloudSync.collectAsStateWithLifecycle()
    val rewardsState by viewModel.rewardsState.collectAsStateWithLifecycle()
    val voiceAssistantState by viewModel.voiceAssistantState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val adminPinConfigured by viewModel.adminPinConfigured.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val isActionLoading by viewModel.isActionLoading.collectAsStateWithLifecycle()

    val profile = (profileState as? UiState.Success)?.data
    val effectiveProfile = if (profile != null && rewardsState.proTrialActive && profile.subscriptionPlan == SubscriptionPlan.FREE_STARTER) {
        profile.copy(plan = SubscriptionPlan.PRO.name)
    } else {
        profile
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var showRewardedAdModal by remember { mutableStateOf(false) }
    var showSosSheet by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var showAddDreamLinkDialog by remember { mutableStateOf(false) }
    var showAiVoiceAssistantSheet by remember { mutableStateOf(false) }
    var showLiveRoomsDialog by remember { mutableStateOf(false) }
    var travelSubTabIndex by remember { mutableIntStateOf(0) }
    var activeWebView by remember { mutableStateOf<Pair<String, String>?>(null) } // title to url

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRouteString = navBackStackEntry?.destination?.route

    val tabs = remember {
        listOf(
            NavTabItem(
                route = KemetRoutes.HOME,
                label = "الرئيسية",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_home"
            ),
            NavTabItem(
                route = KemetRoutes.TRAVEL,
                label = "السفر",
                selectedIcon = Icons.Filled.FlightTakeoff,
                unselectedIcon = Icons.Outlined.FlightTakeoff,
                testTag = "nav_travel"
            ),
            NavTabItem(
                route = KemetRoutes.MARKET,
                label = "السوق",
                selectedIcon = Icons.Filled.Storefront,
                unselectedIcon = Icons.Outlined.Storefront,
                testTag = "nav_market"
            ),
            NavTabItem(
                route = KemetRoutes.TALENTS,
                label = "المواهب",
                selectedIcon = Icons.Filled.EmojiEvents,
                unselectedIcon = Icons.Outlined.EmojiEvents,
                testTag = "nav_talents"
            ),
            NavTabItem(
                route = KemetRoutes.PROFILE,
                label = "حسابي",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                testTag = "nav_profile"
            )
        )
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openSmartBrowser(title: String, rawUrl: String) {
        val safeUrl = SafeIntentHelper.normalizeWebUrl(rawUrl)
        if (safeUrl == null) {
            viewModel.showStatus("الرابط غير صالح أو غير آمن للفتح.")
        } else {
            activeWebView = title to safeUrl
        }
    }

    fun triggerGoogleLinkFromGuest() {
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                viewModel.showStatus("تم ربط حساب Google بنجاح!")
            },
            onAuthError = { msg ->
                viewModel.showStatus(msg)
            },
            scope = scope,
            onAuthCancelled = {
                onExitGuestToAuth()
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = activeWebView == null,
        drawerContent = {
            InternationalAppsDrawerSheet(
                onOpenAppInWebView = ::openSmartBrowser,
                onAddCustomLinkClick = { showAddDreamLinkDialog = true },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AmbientLuxuryBackground {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useNavigationRail = maxWidth >= 600.dp

                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.padding(bottom = 84.dp)
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                if (useNavigationRail) {
                                    NavigationRail(
                                        containerColor = MidnightGlass
                                    ) {
                                        tabs.forEach { tab ->
                                            val selected = currentRouteString == tab.route
                                            NavigationRailItem(
                                                selected = selected,
                                                onClick = { navigateToTab(tab.route) },
                                                icon = {
                                                    Icon(
                                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.label,
                                                        tint = if (selected) RoyalGoldLight else CoolSilver
                                                    )
                                                },
                                                label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                modifier = Modifier.testTag(tab.testTag)
                                            )
                                        }
                                    }
                                }

                                NavHost(
                                    navController = navController,
                                    startDestination = KemetRoutes.HOME,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    composable(KemetRoutes.HOME) {
                                        HomeScreen(
                                            profile = profile,
                                            photoUrl = photoUrl,
                                            isOnline = isOnline,
                                            rewardsState = rewardsState,
                                            listingsState = listingsState,
                                            favoriteLandmarkIds = favoriteLandmarkIds,
                                            onOpenInternationalDrawer = {
                                                scope.launch { drawerState.open() }
                                            },
                                            onOpenInAppBrowser = ::openSmartBrowser,
                                            onOpenAiVoiceAssistant = { showAiVoiceAssistantSheet = true },
                                            onOpenLiveRoomsDialog = { showLiveRoomsDialog = true },
                                            onOpenSosSheet = { showSosSheet = true },
                                            onClaimWelcomeGift = viewModel::claimWelcomeGift,
                                            onClaimDailyGift = viewModel::claimDailyGift,
                                            onNavigateToTravel = { subTab ->
                                                travelSubTabIndex = subTab
                                                navigateToTab(KemetRoutes.TRAVEL)
                                            },
                                            onNavigateToMarket = { navigateToTab(KemetRoutes.MARKET) },
                                            onNavigateToVip = { navigateToTab(KemetRoutes.VIP) },
                                            onNavigateToProfile = { navigateToTab(KemetRoutes.PROFILE) },
                                            onToggleFavoriteLandmark = viewModel::toggleFavoriteLandmark
                                        )
                                    }
                                    composable(KemetRoutes.TRAVEL) {
                                        TravelHubScreen(
                                            profile = effectiveProfile,
                                            isOnline = isOnline,
                                            initialSection = travelSubTabIndex,
                                            conciergeState = conciergeRequestsState,
                                            couponsState = couponsState,
                                            favoriteLandmarkIds = favoriteLandmarkIds,
                                            translatorState = translatorState,
                                            isActionLoading = isActionLoading,
                                            observeOffers = viewModel::observeOffersForRequest,
                                            onCreateConciergeRequest = viewModel::createConciergeRequest,
                                            onSubmitConciergeOffer = viewModel::submitConciergeOffer,
                                            onCreateCoupon = viewModel::createDiscountCoupon,
                                            onQrCouponScanned = viewModel::redeemScannedQrCoupon,
                                            onToggleFavoriteLandmark = viewModel::toggleFavoriteLandmark,
                                            onTranslate = viewModel::translateText,
                                            onTranslateImage = viewModel::translateImageMenuOrSign,
                                            onNavigateToVip = { navigateToTab(KemetRoutes.VIP) },
                                            onShowMessage = viewModel::showStatus
                                        )
                                    }
                                    composable(KemetRoutes.MARKET) {
                                        MarketScreen(
                                            profile = effectiveProfile,
                                            isGuestMode = isGuestMode,
                                            isOnline = isOnline,
                                            listingsState = listingsState,
                                            savedLinksState = savedLinksState,
                                            affiliateLogsState = affiliateLogsState,
                                            currentUserId = currentUserId,
                                            isActionLoading = isActionLoading,
                                            observeReviews = viewModel::observeReviewsForListing,
                                            onSubmitReview = viewModel::submitListingReview,
                                            onPublishListing = viewModel::publishListing,
                                            onDeleteListing = viewModel::deleteListing,
                                            onShareOfferAffiliate = viewModel::shareOfferAffiliate,
                                            onInviteAffiliate = viewModel::recordInviteReward,
                                            onAddSavedLink = viewModel::addSavedLink,
                                            onDeleteSavedLink = viewModel::deleteSavedLink,
                                            onOpenSosSheet = { showSosSheet = true },
                                            onOpenConcierge = {
                                                travelSubTabIndex = 0
                                                navigateToTab(KemetRoutes.TRAVEL)
                                            },
                                            onOpenInAppBrowser = ::openSmartBrowser,
                                            onNavigateToVip = { navigateToTab(KemetRoutes.VIP) },
                                            onLinkGoogleClick = ::triggerGoogleLinkFromGuest,
                                            onShowMessage = viewModel::showStatus
                                        )
                                    }
                                    composable(KemetRoutes.TALENTS) {
                                        TalentsEdutainmentScreen(
                                            profile = effectiveProfile,
                                            edutainmentState = edutainmentState,
                                            talentsState = talentsState,
                                            isActionLoading = isActionLoading,
                                            onSelectRiddleOption = viewModel::submitRiddleAnswer,
                                            onNextRiddle = viewModel::nextRiddle,
                                            onPublishTalent = viewModel::publishTalent,
                                            onOpenInAppBrowser = ::openSmartBrowser,
                                            onWatchRewardedAd = { showRewardedAdModal = true }
                                        )
                                    }
                                    composable(KemetRoutes.PROFILE) {
                                        ProfileScreen(
                                            profile = profile,
                                            photoUrl = photoUrl,
                                            currentUserId = currentUserId,
                                            isGuestMode = isGuestMode,
                                            savedLinksState = savedLinksState,
                                            couponsState = couponsState,
                                            listingsState = listingsState,
                                            submissionsState = submissionsState,
                                            favoriteLandmarkIds = favoriteLandmarkIds,
                                            appSettings = appSettings,
                                            vaultFiles = vaultFiles,
                                            vaultAutoCloudSync = vaultAutoCloudSync,
                                            isActionLoading = isActionLoading,
                                            rewardsState = rewardsState,
                                            onUpdateDisplayName = viewModel::updateProfileDisplayName,
                                            onAddDreamLinkClick = { showAddDreamLinkDialog = true },
                                            onDeleteSavedLink = viewModel::deleteSavedLink,
                                            onOpenInAppBrowser = ::openSmartBrowser,
                                            onToggleFavoriteLandmark = viewModel::toggleFavoriteLandmark,
                                            onNavigateToTravelGuide = {
                                                travelSubTabIndex = 2
                                                navigateToTab(KemetRoutes.TRAVEL)
                                            },
                                            onDeleteListing = viewModel::deleteListing,
                                            onAutoSpeakChange = viewModel::setAutoSpeakAssistant,
                                            onOfflineFirstChange = viewModel::setOfflineFirstMode,
                                            onInspectPickedVaultUri = viewModel::inspectPickedVaultUri,
                                            onResolveLocalVaultFile = viewModel::resolveLocalVaultFile,
                                            onGetShareableVaultUri = viewModel::getShareableVaultUri,
                                            onAddFileToVault = viewModel::addFileToVault,
                                            onDeleteVaultFile = viewModel::deleteVaultFile,
                                            onVaultAutoCloudSyncChange = viewModel::setVaultAutoCloudSync,
                                            onSyncVaultFilesNow = viewModel::syncVaultFilesNow,
                                            onSubmitPayment = viewModel::submitVodafoneCashPayment,
                                            onWatchRewardedAd = { showRewardedAdModal = true },
                                            onInviteAffiliate = viewModel::recordInviteReward,
                                            onOpenAdminPanel = { showAdminDialog = true },
                                            onLinkGoogleClick = ::triggerGoogleLinkFromGuest,
                                            onSignOut = {
                                                signOutUser(credentialManager, scope)
                                            },
                                            onShowMessage = viewModel::showStatus
                                        )
                                    }
                                    composable(KemetRoutes.VIP) {
                                        VipPlansPaymentScreen(
                                            profile = profile,
                                            isGuestMode = isGuestMode,
                                            submissionsState = submissionsState,
                                            isActionLoading = isActionLoading,
                                            rewardsState = rewardsState,
                                            onNavigateBack = {
                                                if (!navController.popBackStack()) {
                                                    navigateToTab(KemetRoutes.HOME)
                                                }
                                            },
                                            onSubmitPayment = viewModel::submitVodafoneCashPayment,
                                            onWatchRewardedAd = { showRewardedAdModal = true },
                                            onInviteAffiliate = viewModel::recordInviteReward,
                                            onOpenAdminPanel = { showAdminDialog = true },
                                            onLinkGoogleClick = ::triggerGoogleLinkFromGuest,
                                            onSignOut = {
                                                signOutUser(credentialManager, scope)
                                            },
                                            onShowMessage = viewModel::showStatus
                                        )
                                    }
                                }
                            }

                            // Floating Glass Navigation Bar on handheld screens
                            if (!useNavigationRail) {
                                FloatingGlassNavBar(
                                    tabs = tabs,
                                    currentRoute = currentRouteString,
                                    onSelectTab = ::navigateToTab,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .navigationBarsPadding()
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (activeWebView == null) {
                FloatingEdgeDrawerHandle(
                    isDrawerOpen = drawerState.isOpen,
                    onToggleDrawer = {
                        scope.launch {
                            if (drawerState.isOpen) drawerState.close() else drawerState.open()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            val currentWebView = activeWebView
            if (currentWebView != null) {
                SmartWebViewScreen(
                    initialTitle = currentWebView.first,
                    initialUrl = currentWebView.second,
                    onClose = { activeWebView = null },
                    onShowMessage = viewModel::showStatus
                )
            }
        }
    }

    if (showAddDreamLinkDialog) {
        AddDreamLinkDialog(
            isLoading = isActionLoading,
            onDismiss = { showAddDreamLinkDialog = false },
            onSaveLink = { title, url, category ->
                viewModel.addSavedLink(title, url, category) {
                    showAddDreamLinkDialog = false
                }
            }
        )
    }

    if (showAiVoiceAssistantSheet) {
        AiVoiceAssistantSheet(
            state = voiceAssistantState,
            autoSpeak = appSettings.autoSpeakAssistant,
            onAsk = viewModel::askAiVoiceAssistant,
            onDismiss = { showAiVoiceAssistantSheet = false },
            onShowMessage = viewModel::showStatus
        )
    }

    if (showLiveRoomsDialog) {
        LiveRoomsDialog(
            onOpenAiVoiceRoom = { showAiVoiceAssistantSheet = true },
            onOpenTranslatorRoom = {
                travelSubTabIndex = 3
                navigateToTab(KemetRoutes.TRAVEL)
            },
            onDismiss = { showLiveRoomsDialog = false },
            onShowMessage = viewModel::showStatus
        )
    }

    if (showSosSheet) {
        SosEssentialsSheet(
            onDismiss = { showSosSheet = false },
            onShowMessage = viewModel::showStatus
        )
    }

    if (showAdminDialog) {
        DeveloperAdminDialog(
            isPinConfigured = adminPinConfigured,
            isAdminUnlocked = isAdminUnlocked,
            isAdminEmail = viewModel.isAdminEmail,
            isGuestMode = isGuestMode,
            vipRequestsState = vipRequestsState,
            listingsState = listingsState,
            isActionLoading = isActionLoading,
            onSetupPin = viewModel::setupAdminPin,
            onUnlockWithPin = viewModel::unlockAdminPanel,
            onApproveRequest = viewModel::approveVipRequest,
            onRejectRequest = viewModel::rejectVipRequest,
            onToggleVerifyListing = viewModel::toggleVerifyListing,
            onDismiss = {
                viewModel.lockAdminPanel()
                showAdminDialog = false
            }
        )
    }

    if (showRewardedAdModal) {
        RewardedAdModal(
            onDismiss = { showRewardedAdModal = false },
            onRewardEarned = {
                showRewardedAdModal = false
                viewModel.claimRewardedAdCoins()
            }
        )
    }
}

@Composable
private fun FloatingGlassNavBar(
    tabs: List<NavTabItem>,
    currentRoute: String?,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp),
        shape = RoundedCornerShape(28.dp),
        color = MidnightGlass,
        border = BorderStroke(1.dp, RoyalGold.copy(alpha = 0.55f)),
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                Column(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selected) RoyalGold.copy(alpha = 0.18f) else Color.Transparent
                        )
                        .clickable { onSelectTab(tab.route) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = if (selected) RoyalGoldLight else CoolSilver,
                        modifier = Modifier.size(21.dp)
                    )
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) RoyalGoldLight else CoolSilver,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
