package com.example.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdminSecurityStore
import com.example.data.AiTranslatorRepository
import com.example.data.FileVaultStore
import com.example.data.GuestLocalStore
import com.example.data.KemetCommunityRepository
import com.example.data.KemetRepository
import com.example.data.OfflineCacheStore
import com.example.data.PickedUriMeta
import com.example.data.RewardsState
import com.example.data.UserAppSettings
import com.example.data.UserPreferencesStore
import com.example.model.AffiliateLog
import com.example.model.BillingCycle
import com.example.model.ConciergeOffer
import com.example.model.ConciergeRequest
import com.example.model.DiscountCoupon
import com.example.model.EdutainmentRiddle
import com.example.model.KemetCatalogDefaults
import com.example.model.LinkCategory
import com.example.model.ListingCategory
import com.example.model.ListingReview
import com.example.model.MarketplaceListing
import com.example.model.PaymentSubmission
import com.example.model.SavedLink
import com.example.model.SenderWalletType
import com.example.model.SubscriptionPlan
import com.example.model.TalentCategory
import com.example.model.TalentShowcase
import com.example.model.TranslationResult
import com.example.model.TranslatorLanguage
import com.example.model.UserProfile
import com.example.model.VaultCategory
import com.example.model.VaultFileItem
import com.example.model.VipPaymentRequest
import java.io.File
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface TranslatorUiState {
    data object Idle : TranslatorUiState
    data object Loading : TranslatorUiState
    data class Success(val result: TranslationResult) : TranslatorUiState
    data class Error(val message: String) : TranslatorUiState
}

sealed interface VoiceAssistantUiState {
    data object Idle : VoiceAssistantUiState
    data object Loading : VoiceAssistantUiState
    data class Success(val query: String, val reply: String) : VoiceAssistantUiState
    data class Error(val message: String) : VoiceAssistantUiState
}

data class EdutainmentState(
    val riddles: List<EdutainmentRiddle> = KemetCatalogDefaults.riddles,
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isCorrect: Boolean? = null,
    val streak: Int = 0
) {
    val currentRiddle: EdutainmentRiddle get() = riddles[currentIndex % riddles.size]
}

class KemetViewModel(
    private val repository: KemetRepository?,
    private val communityRepository: KemetCommunityRepository?,
    private val guestStore: GuestLocalStore,
    private val offlineCacheStore: OfflineCacheStore,
    private val adminSecurityStore: AdminSecurityStore,
    private val userPreferencesStore: UserPreferencesStore,
    private val fileVaultStore: FileVaultStore,
    private val translatorRepository: AiTranslatorRepository,
    val isGuestMode: Boolean,
    private val currentUserId: String,
    private val initialDisplayName: String,
    private val initialEmail: String
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = offlineCacheStore.isOnline
    val isAdminEmail: Boolean = initialEmail.trim().lowercase() in KemetCatalogDefaults.adminEmails
    val favoriteLandmarkIds: StateFlow<Set<String>> = userPreferencesStore.favoriteLandmarkIds
    val appSettings: StateFlow<UserAppSettings> = userPreferencesStore.appSettings
    val vaultFiles: StateFlow<List<VaultFileItem>> = fileVaultStore.vaultFiles
    val vaultAutoCloudSync: StateFlow<Boolean> = fileVaultStore.autoCloudSync
    val rewardsState: StateFlow<RewardsState> = userPreferencesStore.rewardsState

    private val _adminPinConfigured = MutableStateFlow(adminSecurityStore.hasPinConfigured())
    val adminPinConfigured: StateFlow<Boolean> = _adminPinConfigured.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private fun <T> Flow<T>.asUiState(tagLabel: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $tagLabel", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "تعذر تحميل $tagLabel"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val profileState: StateFlow<UiState<UserProfile?>> = if (isGuestMode || repository == null) {
        guestStore.guestProfile.asUiState("ملف الزائر")
    } else {
        repository.observeUserProfile(currentUserId).asUiState("الملف الشخصي")
    }

    val listingsState: StateFlow<UiState<List<MarketplaceListing>>> = if (isGuestMode || repository == null) {
        guestStore.guestListings.asUiState("السوق")
    } else {
        repository.observeListings().asUiState("السوق")
    }

    val talentsState: StateFlow<UiState<List<TalentShowcase>>> = if (isGuestMode || repository == null) {
        guestStore.guestTalents.asUiState("المواهب")
    } else {
        repository.observeTalents().asUiState("المواهب")
    }

    // Dream / Link Box automatically caches locally and falls back cleanly when offline
    val savedLinksState: StateFlow<UiState<List<SavedLink>>> = if (isGuestMode || repository == null) {
        guestStore.guestLinks.asUiState("صندوق الروابط")
    } else {
        combine(
            repository.observeSavedLinks(currentUserId)
                .onEach { links -> offlineCacheStore.saveLinksSnapshot(currentUserId, links) }
                .catch { emit(offlineCacheStore.cachedLinks.value) },
            offlineCacheStore.isOnline,
            offlineCacheStore.cachedLinks
        ) { remoteLinks, online, cached ->
            if (!online && remoteLinks.isEmpty()) cached else remoteLinks
        }.asUiState("صندوق الروابط")
    }

    val paymentSubmissionsState: StateFlow<UiState<List<PaymentSubmission>>> = if (isGuestMode || repository == null) {
        guestStore.guestPaymentSubmissions.asUiState("المدفوعات")
    } else {
        repository.observePaymentSubmissions(currentUserId).asUiState("المدفوعات")
    }

    val affiliateLogsState: StateFlow<UiState<List<AffiliateLog>>> = if (isGuestMode || repository == null) {
        flowOf(emptyList<AffiliateLog>()).asUiState("التسويق بالعمولة")
    } else {
        repository.observeAffiliateLogs(currentUserId).asUiState("التسويق بالعمولة")
    }

    val conciergeRequestsState: StateFlow<UiState<List<ConciergeRequest>>> =
        if (isGuestMode || communityRepository == null) {
            guestStore.guestConciergeRequests.asUiState("الطلبات الخاصة")
        } else {
            communityRepository.observeConciergeRequests().asUiState("الطلبات الخاصة")
        }

    val couponsState: StateFlow<UiState<List<DiscountCoupon>>> =
        if (isGuestMode || communityRepository == null) {
            guestStore.guestCoupons.asUiState("كوبونات الخصم")
        } else {
            communityRepository.observeCoupons().asUiState("كوبونات الخصم")
        }

    val vipRequestsState: StateFlow<UiState<List<VipPaymentRequest>>> =
        if (isGuestMode || communityRepository == null) {
            guestStore.guestVipRequests.asUiState("طلبات VIP")
        } else {
            communityRepository.observeVipRequests(isAdminEmail).asUiState("طلبات VIP")
        }

    private val _translatorState = MutableStateFlow<TranslatorUiState>(TranslatorUiState.Idle)
    val translatorState: StateFlow<TranslatorUiState> = _translatorState.asStateFlow()

    private val _voiceAssistantState = MutableStateFlow<VoiceAssistantUiState>(VoiceAssistantUiState.Idle)
    val voiceAssistantState: StateFlow<VoiceAssistantUiState> = _voiceAssistantState.asStateFlow()

    private val _edutainmentState = MutableStateFlow(EdutainmentState())
    val edutainmentState: StateFlow<EdutainmentState> = _edutainmentState.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    init {
        userPreferencesStore.bindUser(currentUserId)
        viewModelScope.launch {
            fileVaultStore.bindUser(currentUserId)
            if (!isGuestMode && repository != null) {
                repository.ensureUserProfileAndStarterContent(initialDisplayName, initialEmail)
                repository.seedStarterMarketplaceIfEmpty(initialDisplayName)

                // Migrate any guest vault files & sync to Firestore automatically upon Google Sign-In
                val pending = fileVaultStore.migrateGuestVaultToUser(currentUserId)
                if (pending.isNotEmpty() && fileVaultStore.autoCloudSync.value && offlineCacheStore.isOnline.value) {
                    val syncedCount = syncPendingVaultFilesInternal(pending)
                    if (syncedCount > 0) {
                        _statusBanner.value = "تمت مزامنة $syncedCount ملفات من حافظة الزائر إلى السحابة تلقائياً ☁️"
                    }
                }
            }
        }
        if (!isGuestMode && repository != null) {
            viewModelScope.launch {
                repository.observeVaultFiles(currentUserId)
                    .catch { err ->
                        if (err is CancellationException) throw err
                        Log.w(TAG, "Vault cloud observe skipped", err)
                    }
                    .collect { cloudDocs ->
                        fileVaultStore.mergeCloudDocs(currentUserId, cloudDocs)
                    }
            }
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showStatus(message: String) {
        _statusBanner.value = message
    }

    fun effectivePlanFor(profile: UserProfile?): SubscriptionPlan {
        val base = profile?.subscriptionPlan ?: SubscriptionPlan.FREE_STARTER
        return if (base == SubscriptionPlan.FREE_STARTER && rewardsState.value.proTrialActive) {
            SubscriptionPlan.PRO
        } else {
            base
        }
    }

    fun claimWelcomeGift() {
        val granted = userPreferencesStore.claimWelcomeGift()
        if (!granted) {
            _statusBanner.value = "لقد استلمت هدية الترحيب الخاصة بك بالفعل ✓"
            return
        }
        val bonusCoins = 100L
        if (isGuestMode || repository == null) {
            guestStore.addCoins(bonusCoins)
            _statusBanner.value = "تهانينا! تم تفعيل +100 عملة ذهبية + 3 أيام تجربة Pro مجاناً 🎁"
        } else {
            viewModelScope.launch {
                repository.awardCoinsForActivity(bonusCoins, isQuizWin = false)
                    .onSuccess {
                        _statusBanner.value = "تهانينا! تم تفعيل +100 عملة ذهبية + 3 أيام تجربة Pro مجاناً 🎁"
                    }
                    .onFailure { err ->
                        _statusBanner.value = "تم تفعيل 3 أيام Pro مجاناً! (${err.localizedMessage ?: "تعذر إضافة العملات سحابياً"})"
                    }
            }
        }
    }

    fun claimDailyGift() {
        val claimed = userPreferencesStore.claimDailyGift()
        if (claimed == null) {
            _statusBanner.value = "تم استلام هدية اليوم بالفعل! عد غداً لصندوق جديد."
            return
        }
        val (day, coins) = claimed
        if (isGuestMode || repository == null) {
            guestStore.addCoins(coins)
            _statusBanner.value = "تم فتح هدية اليوم $day: ربحت +$coins عملة كيميت الذهبية!"
        } else {
            viewModelScope.launch {
                repository.awardCoinsForActivity(coins, isQuizWin = false)
                    .onSuccess {
                        _statusBanner.value = "تم فتح هدية اليوم $day: ربحت +$coins عملة كيميت الذهبية!"
                    }
                    .onFailure { err ->
                        _statusBanner.value = err.localizedMessage ?: "تعذر إضافة مكافأة اليوم."
                    }
            }
        }
    }

    // --- Profile Identity, Favorite Landmarks & Settings ---
    fun updateProfileDisplayName(newName: String, onDone: () -> Unit) {
        val clean = newName.trim()
        if (clean.length < 2) {
            _statusBanner.value = "يرجى إدخال اسم لا يقل عن حرفين."
            return
        }
        if (isGuestMode || repository == null) {
            guestStore.updateDisplayName(clean)
            _statusBanner.value = "تم تحديث اسمك الشخصي بنجاح ✓"
            onDone()
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = repository.updateDisplayName(clean)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم تحديث بطاقة الهوية الشخصية بنجاح ✓"
                onDone()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "تعذر تحديث الاسم."
            }
        }
    }

    fun toggleFavoriteLandmark(landmarkId: String, landmarkTitle: String) {
        val added = userPreferencesStore.toggleFavoriteLandmark(landmarkId)
        _statusBanner.value = if (added) {
            "تمت إضافة \"$landmarkTitle\" إلى المعالم المفضلة ★"
        } else {
            "تمت إزالة \"$landmarkTitle\" من المعالم المفضلة."
        }
    }

    fun setAutoSpeakAssistant(enabled: Boolean) {
        userPreferencesStore.setAutoSpeakAssistant(enabled)
    }

    fun setOfflineFirstMode(enabled: Boolean) {
        userPreferencesStore.setOfflineFirstMode(enabled)
        _statusBanner.value = if (enabled) "تم تفعيل وضع الأولوية للبيانات المحلية والأوفلاين." else "تم تفعيل الوضع السحابي القياسي."
    }

    // --- User File Vault & Document Wallet (Internal Storage + DataStore + Cloud Sync) ---
    fun inspectPickedVaultUri(uri: Uri): PickedUriMeta = fileVaultStore.inspectUri(uri)

    fun resolveLocalVaultFile(item: VaultFileItem): File? = fileVaultStore.resolveLocalFile(item)

    fun getShareableVaultUri(item: VaultFileItem): Uri? = fileVaultStore.createShareableUri(item)

    fun addFileToVault(
        uri: Uri,
        title: String,
        category: VaultCategory,
        note: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            _isActionLoading.value = true
            val localResult = runCatching {
                fileVaultStore.importUriToVault(currentUserId, uri, title, category, note)
            }
            localResult.onSuccess { item ->
                var synced = false
                if (!isGuestMode && repository != null && fileVaultStore.autoCloudSync.value && offlineCacheStore.isOnline.value) {
                    val base64 = fileVaultStore.readInlineBase64IfSmall(item)
                    val cloudRes = repository.upsertVaultFile(item, base64)
                    if (cloudRes.isSuccess) {
                        fileVaultStore.markItemsCloudSynced(currentUserId, setOf(item.id))
                        synced = true
                    }
                }
                _isActionLoading.value = false
                _statusBanner.value = if (synced) {
                    "تم حفظ \"${item.title}\" في الحافظة ومزامنته سحابياً ✓"
                } else {
                    "تم حفظ \"${item.title}\" في حافظة الهاتف الآمنة (يعمل بدون إنترنت) ✓"
                }
                onDone()
            }.onFailure { err ->
                _isActionLoading.value = false
                _statusBanner.value = err.localizedMessage ?: "تعذر حفظ الملف في الحافظة."
            }
        }
    }

    fun deleteVaultFile(item: VaultFileItem) {
        viewModelScope.launch {
            _isActionLoading.value = true
            runCatching { fileVaultStore.deleteVaultItem(currentUserId, item.id) }
            if (!isGuestMode && repository != null && offlineCacheStore.isOnline.value) {
                repository.deleteVaultFile(item.id)
            }
            _isActionLoading.value = false
            _statusBanner.value = "تم حذف \"${item.title}\" من الحافظة."
        }
    }

    fun setVaultAutoCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            fileVaultStore.setAutoCloudSync(enabled)
            if (enabled && !isGuestMode && repository != null && offlineCacheStore.isOnline.value) {
                val synced = syncPendingVaultFilesInternal(fileVaultStore.getUnsyncedItems(currentUserId))
                _statusBanner.value = if (synced > 0) {
                    "تم تفعيل المزامنة التلقائية ورفع $synced ملفات إلى السحابة ☁️"
                } else {
                    "تم تفعيل المزامنة السحابية التلقائية للملفات."
                }
            } else {
                _statusBanner.value = if (enabled) {
                    "ستتم مزامنة ملفاتك تلقائياً فور ربط حساب Google وتوفر الإنترنت."
                } else {
                    "تم الاكتفاء بالتخزين المحلي الآمن على الجهاز فقط."
                }
            }
        }
    }

    fun syncVaultFilesNow() {
        if (isGuestMode || repository == null) {
            _statusBanner.value = "اربط حساب Google أولاً لمزامنة ملفات الحافظة سحابياً."
            return
        }
        if (!offlineCacheStore.isOnline.value) {
            _statusBanner.value = "لا يوجد اتصال بالإنترنت حالياً؛ ملفاتك محفوظة بأمان على الجهاز."
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val pending = fileVaultStore.getUnsyncedItems(currentUserId)
            val synced = syncPendingVaultFilesInternal(pending)
            _isActionLoading.value = false
            _statusBanner.value = if (synced > 0) {
                "تمت مزامنة $synced ملفات بنجاح إلى السحابة ☁️"
            } else {
                "جميع ملفات الحافظة متزامنة ومحدّثة بالفعل ✓"
            }
        }
    }

    private suspend fun syncPendingVaultFilesInternal(pending: List<VaultFileItem>): Int {
        val repo = repository ?: return 0
        if (pending.isEmpty()) return 0
        val syncedIds = mutableSetOf<String>()
        pending.forEach { item ->
            val inline = fileVaultStore.readInlineBase64IfSmall(item)
            if (repo.upsertVaultFile(item, inline).isSuccess) {
                syncedIds.add(item.id)
            }
        }
        if (syncedIds.isNotEmpty()) {
            fileVaultStore.markItemsCloudSynced(currentUserId, syncedIds)
        }
        return syncedIds.size
    }

    // --- AI Voice Assistant (Smart Concierge) ---
    fun askAiVoiceAssistant(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        val planLabel = (profileState.value as? UiState.Success)?.data?.subscriptionPlan?.labelAr
            ?: SubscriptionPlan.FREE_STARTER.labelAr

        if (!offlineCacheStore.isOnline.value) {
            val offlineReply = "أنت في وضع عدم الاتصال حالياً. يمكنك تصفح الدليل السياحي المحفوظ لأهرامات الجيزة والأقصر وأسوان وشرم الشيخ، أو الاتصال بطوارئ السياحة 126 والإسعاف 123 في أي وقت."
            _voiceAssistantState.value = VoiceAssistantUiState.Success(clean, offlineReply)
            return
        }

        viewModelScope.launch {
            _voiceAssistantState.value = VoiceAssistantUiState.Loading
            val res = translatorRepository.askVoiceAssistant(clean, planLabel)
            res.onSuccess { reply ->
                _voiceAssistantState.value = VoiceAssistantUiState.Success(clean, reply)
            }.onFailure { err ->
                _voiceAssistantState.value = VoiceAssistantUiState.Error(
                    err.localizedMessage ?: "تعذر الاتصال بالمتحدث الذكي حالياً."
                )
            }
        }
    }

    fun clearVoiceAssistant() {
        _voiceAssistantState.value = VoiceAssistantUiState.Idle
    }

    // --- Smart Translator + Camera OCR + Offline Phrase Fallback ---
    fun translateText(
        inputText: String,
        sourceLanguage: TranslatorLanguage,
        targetLanguage: TranslatorLanguage
    ) {
        val profile = (profileState.value as? UiState.Success)?.data
        val isFreePlan = effectivePlanFor(profile) == SubscriptionPlan.FREE_STARTER
        val usedToday = profile?.dailyTranslationsUsed ?: 0L
        if (isFreePlan && usedToday >= FREE_DAILY_TRANSLATION_LIMIT) {
            _translatorState.value = TranslatorUiState.Error(
                "وصلت للحد اليومي المجاني (5 ترجمات صوتية ذكية). قم بالترقية إلى KemetLoop Pro للترجمة غير المحدودة."
            )
            return
        }

        // Instant Offline Dictionary / Cache fallback if offline
        if (!offlineCacheStore.isOnline.value) {
            val cached = offlineCacheStore.lookupOfflineTranslation(inputText, sourceLanguage.code, targetLanguage.code)
            if (cached != null) {
                _translatorState.value = TranslatorUiState.Success(cached)
                return
            }
        }

        viewModelScope.launch {
            _translatorState.value = TranslatorUiState.Loading
            val result = translatorRepository.translate(inputText, sourceLanguage, targetLanguage)
            result.onSuccess { translation ->
                offlineCacheStore.cacheTranslation(inputText, sourceLanguage.code, targetLanguage.code, translation)
                _translatorState.value = TranslatorUiState.Success(translation)
                if (isGuestMode || repository == null) {
                    guestStore.incrementTranslations()
                } else {
                    repository.incrementDailyTranslations()
                }
            }.onFailure { err ->
                val cached = offlineCacheStore.lookupOfflineTranslation(inputText, sourceLanguage.code, targetLanguage.code)
                if (cached != null) {
                    _translatorState.value = TranslatorUiState.Success(cached)
                } else {
                    _translatorState.value = TranslatorUiState.Error(
                        err.localizedMessage ?: "تعذر إتمام الترجمة الذكية حالياً."
                    )
                }
            }
        }
    }

    fun translateImageMenuOrSign(bitmap: Bitmap, targetLanguage: TranslatorLanguage) {
        if (!offlineCacheStore.isOnline.value) {
            _translatorState.value = TranslatorUiState.Error(
                "ترجمة الكاميرا واللافتات تتطلب اتصالاً بالإنترنت. يمكنك استخدام القاموس المحفوظ بالأسفل بدون إنترنت."
            )
            return
        }
        viewModelScope.launch {
            _translatorState.value = TranslatorUiState.Loading
            val res = translatorRepository.translateFromImage(bitmap, targetLanguage)
            res.onSuccess { imgRes ->
                val mapped = TranslationResult(
                    translatedText = imgRes.translatedText,
                    pronunciationGuide = imgRes.pronunciationGuide,
                    culturalTip = imgRes.culturalTip,
                    extractedSourceText = imgRes.extractedText
                )
                _translatorState.value = TranslatorUiState.Success(mapped)
                if (isGuestMode || repository == null) {
                    guestStore.incrementTranslations()
                } else {
                    repository.incrementDailyTranslations()
                }
            }.onFailure { err ->
                _translatorState.value = TranslatorUiState.Error(
                    err.localizedMessage ?: "تعذر تحليل الصورة. حاول التقاط صورة أوضح."
                )
            }
        }
    }

    // --- Marketplace & Reviews ---
    fun observeReviewsForListing(listingId: String): Flow<List<ListingReview>> {
        return if (isGuestMode || communityRepository == null) {
            guestStore.guestReviews.map { it[listingId].orEmpty() }
        } else {
            communityRepository.observeListingReviews(listingId).catch { emit(emptyList()) }
        }
    }

    fun submitListingReview(listingId: String, rating: Int, comment: String, onDone: () -> Unit) {
        if (isGuestMode || communityRepository == null) {
            guestStore.addGuestReview(listingId, rating, comment)
            _statusBanner.value = "شكراً لتقييمك ($rating نجوم)!"
            onDone()
            return
        }
        val reviewer = (profileState.value as? UiState.Success)?.data?.displayName ?: initialDisplayName
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = communityRepository.submitListingReview(listingId, reviewer, rating, comment)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم تسجيل تقييمك ($rating نجوم) بنجاح!"
                onDone()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "تعذر إرسال التقييم."
            }
        }
    }

    fun publishListing(
        title: String,
        description: String,
        category: ListingCategory,
        priceEgp: Long,
        location: String,
        featured: Boolean,
        whatsappPhone: String,
        socialUrl: String,
        onComplete: () -> Unit
    ) {
        if (isGuestMode || repository == null) {
            guestStore.addGuestListing(title, description, category, priceEgp, location)
            _statusBanner.value = "تمت إضافة العرض في وضع الزائر! اربط حساب Google لحفظ عروضك سحابياً."
            onComplete()
            return
        }
        val profile = (profileState.value as? UiState.Success)?.data
        val isBusinessVip = profile?.subscriptionPlan == SubscriptionPlan.BUSINESS_VIP
        val sellerName = profile?.displayName ?: "تاجر KemetLoop"

        viewModelScope.launch {
            _isActionLoading.value = true
            val res = repository.publishListing(
                sellerName = sellerName,
                title = title,
                description = description,
                category = category,
                priceEgp = priceEgp,
                location = location,
                featured = featured && isBusinessVip,
                verifiedSeller = isBusinessVip,
                whatsappPhone = if (isBusinessVip) whatsappPhone else "",
                socialUrl = if (isBusinessVip) socialUrl else ""
            )
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم نشر العرض بنجاح في سوق KemetLoop!"
                onComplete()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "تعذر نشر الإعلان."
            }
        }
    }

    fun deleteListing(listingId: String) {
        if (isGuestMode || repository == null) {
            guestStore.deleteGuestListing(listingId)
            _statusBanner.value = "تم حذف العرض."
            return
        }
        viewModelScope.launch {
            repository.deleteListing(listingId)
                .onSuccess { _statusBanner.value = "تم حذف الإعلان." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "تعذر حذف الإعلان." }
        }
    }

    // --- Concierge Requests & Business VIP Offers ---
    fun observeOffersForRequest(requestId: String, requesterUid: String): Flow<List<ConciergeOffer>> {
        return if (isGuestMode || communityRepository == null) {
            guestStore.guestConciergeOffers.map { it[requestId].orEmpty() }
        } else {
            communityRepository.observeOffersForRequest(requestId, requesterUid).catch { emit(emptyList()) }
        }
    }

    fun createConciergeRequest(
        category: ListingCategory,
        title: String,
        details: String,
        budgetEgp: Long,
        city: String,
        onDone: () -> Unit
    ) {
        if (isGuestMode || communityRepository == null) {
            guestStore.addGuestConciergeRequest(category, title, details, budgetEgp, city)
            _statusBanner.value = "تم إرسال طلبك الخاص لصندوق الكونسيرج!"
            onDone()
            return
        }
        val requesterName = (profileState.value as? UiState.Success)?.data?.displayName ?: initialDisplayName
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = communityRepository.createConciergeRequest(requesterName, category, title, details, budgetEgp, city)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم إرسال طلبك الخاص لتجار Business VIP لتقديم عروضهم!"
                onDone()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "تعذر إرسال الطلب."
            }
        }
    }

    fun submitConciergeOffer(
        request: ConciergeRequest,
        priceEgp: Long,
        proposal: String,
        whatsappPhone: String,
        onDone: () -> Unit
    ) {
        val profile = (profileState.value as? UiState.Success)?.data
        if (profile?.subscriptionPlan != SubscriptionPlan.BUSINESS_VIP) {
            _statusBanner.value = "تقديم العروض على الطلبات الخاصة متاح لمشتركي باقة Business VIP فقط."
            return
        }
        if (isGuestMode || communityRepository == null) {
            guestStore.addGuestConciergeOffer(request.id, priceEgp, proposal, whatsappPhone)
            _statusBanner.value = "تم إرسال عرض Business VIP بنجاح!"
            onDone()
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = communityRepository.submitConciergeOffer(
                requestId = request.id,
                requesterUid = request.userId,
                merchantName = profile.displayName,
                priceEgp = priceEgp,
                proposal = proposal,
                whatsappPhone = whatsappPhone
            )
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم إرسال عرضك للعميل بنجاح!"
                onDone()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "تعذر إرسال العرض."
            }
        }
    }

    // --- QR Discount Coupons ---
    fun createDiscountCoupon(
        code: String,
        title: String,
        discountPercent: Long,
        city: String,
        onDone: () -> Unit
    ) {
        val merchantName = (profileState.value as? UiState.Success)?.data?.displayName ?: initialDisplayName
        if (isGuestMode || communityRepository == null) {
            guestStore.addGuestCoupon(merchantName, code, title, discountPercent, city)
            _statusBanner.value = "تم إنشاء كوبون الخصم وتوليد رمز QR!"
            onDone()
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = communityRepository.createCoupon(merchantName, code, title, discountPercent, city)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم إنشاء الكوبون وتوليد رمز QR بنجاح!"
                onDone()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "تعذر إنشاء الكوبون."
            }
        }
    }

    fun redeemScannedQrCoupon(rawQrText: String) {
        val clean = rawQrText.trim()
        val extractedCode = clean.removePrefix("KEMETCOUPON:").substringBefore("|").trim().uppercase()
        val coupons = (couponsState.value as? UiState.Success)?.data.orEmpty()
        val matched = coupons.firstOrNull { it.code.equals(extractedCode, ignoreCase = true) }
        if (matched != null) {
            _statusBanner.value = "تم تفعيل كوبون ${matched.code}: خصم ${matched.discountPercent}% لدى ${matched.merchantName}!"
        } else {
            _statusBanner.value = "تم مسح رمز الكوبون ($extractedCode) بنجاح!"
        }
    }

    // --- Affiliate & Dream/Link Box ---
    fun shareOfferAffiliate(listing: MarketplaceListing) {
        if (isGuestMode || repository == null) {
            guestStore.addCoins(listing.affiliateRewardCoins, isAffiliate = true)
            _statusBanner.value = "ربحت +${listing.affiliateRewardCoins} عملة! (عمولة المنصة 10%: ${listing.platformCommissionEgp} ج.م)."
            return
        }
        viewModelScope.launch {
            repository.recordAffiliateAction("SHARE_OFFER", listing.title, listing.priceEgp, listing.affiliateRewardCoins)
                .onSuccess {
                    _statusBanner.value = "ربحت +${listing.affiliateRewardCoins} عملة (عمولة المنصة 10%: ${listing.platformCommissionEgp} ج.م)."
                }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "تعذر تسجيل المكافأة." }
        }
    }

    fun recordInviteReward(referralCode: String) {
        if (isGuestMode || repository == null) {
            guestStore.addCoins(50L, isAffiliate = true, isInvite = true)
            _statusBanner.value = "تمت مشاركة الدعوة وحصلت على +50 عملة في وضع الزائر!"
            return
        }
        viewModelScope.launch {
            repository.recordAffiliateAction("INVITE_FRIEND", "دعوة صديق عبر الكود $referralCode", 500L, 50L)
                .onSuccess { _statusBanner.value = "تمت مشاركة رابط الدعوة! حصلت على +50 عملة مكافأة." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "تعذر تسجيل الدعوة." }
        }
    }

    fun addSavedLink(title: String, url: String, category: LinkCategory, onComplete: () -> Unit) {
        if (isGuestMode || repository == null) {
            guestStore.addSavedLink(title, url, category)
            _statusBanner.value = "تم حفظ الرابط في صندوق الأحلام والروابط (محلياً)."
            onComplete()
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = repository.addSavedLink(title, url, category)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم حفظ الرابط في صندوق الأحلام والروابط."
                onComplete()
            }.onFailure { _statusBanner.value = it.localizedMessage ?: "تعذر حفظ الرابط." }
        }
    }

    fun deleteSavedLink(linkId: String) {
        if (isGuestMode || repository == null) {
            guestStore.deleteSavedLink(linkId)
            return
        }
        viewModelScope.launch { repository.deleteSavedLink(linkId) }
    }

    // --- Talents & Edutainment ---
    fun publishTalent(
        creatorName: String,
        talentTitle: String,
        category: TalentCategory,
        bio: String,
        youtubeUrl: String,
        socialUrl: String,
        onComplete: () -> Unit
    ) {
        if (isGuestMode || repository == null) {
            guestStore.addGuestTalent(creatorName, talentTitle, category, bio, youtubeUrl, socialUrl)
            _statusBanner.value = "تمت إضافة الموهبة في وضع الزائر!"
            onComplete()
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = repository.publishTalent(creatorName, talentTitle, category, bio, youtubeUrl, socialUrl)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تمت إضافة موهبتك إلى منصة المواهب!"
                onComplete()
            }.onFailure { _statusBanner.value = it.localizedMessage ?: "تعذر نشر الموهبة." }
        }
    }

    fun submitRiddleAnswer(optionIndex: Int) {
        val current = _edutainmentState.value
        if (current.selectedOptionIndex != null) return
        val riddle = current.currentRiddle
        val correct = optionIndex == riddle.correctIndex
        _edutainmentState.value = current.copy(
            selectedOptionIndex = optionIndex,
            isCorrect = correct,
            streak = if (correct) current.streak + 1 else 0
        )
        if (correct) {
            if (isGuestMode || repository == null) {
                guestStore.addCoins(riddle.rewardCoins, isQuizWin = true)
                _statusBanner.value = "إجابة صحيحة! أُضيف +${riddle.rewardCoins} عملة إلى رصيدك."
            } else {
                viewModelScope.launch {
                    repository.awardCoinsForActivity(riddle.rewardCoins, isQuizWin = true)
                        .onSuccess { _statusBanner.value = "إجابة صحيحة! أُضيف +${riddle.rewardCoins} عملة إلى رصيدك." }
                }
            }
        }
    }

    fun nextRiddle() {
        val current = _edutainmentState.value
        _edutainmentState.value = current.copy(
            currentIndex = (current.currentIndex + 1) % current.riddles.size,
            selectedOptionIndex = null,
            isCorrect = null
        )
    }

    fun claimRewardedAdCoins() {
        if (isGuestMode || repository == null) {
            guestStore.addCoins(25L)
            _statusBanner.value = "شكراً لمشاهدة الإعلان! تمت إضافة +25 عملة مجانية."
            return
        }
        viewModelScope.launch {
            repository.awardCoinsForActivity(25L, isQuizWin = false)
                .onSuccess { _statusBanner.value = "شكراً لمشاهدة الإعلان! تمت إضافة +25 عملة مجانية." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "تعذر إضافة المكافأة." }
        }
    }

    // --- Vodafone Cash Payment & Developer Admin Panel ---
    fun submitVodafoneCashPayment(
        targetPlan: SubscriptionPlan,
        billingCycle: BillingCycle,
        senderWalletType: SenderWalletType,
        txReference: String,
        receiptSelected: Boolean,
        receiptUriNote: String,
        onSuccess: () -> Unit
    ) {
        if (isGuestMode || repository == null) {
            guestStore.submitGuestVipRequest(targetPlan, billingCycle, senderWalletType, txReference, receiptSelected, receiptUriNote)
            _statusBanner.value = "تم تسجيل طلب التحويل (قيد المراجعة) ويمكن مراجعته وتفعيله من لوحة تحكم المطور."
            onSuccess()
            return
        }
        val profile = (profileState.value as? UiState.Success)?.data
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = repository.submitVodafoneCashPayment(
                userName = profile?.displayName ?: initialDisplayName,
                userEmail = profile?.email ?: initialEmail,
                targetPlan = targetPlan,
                billingCycle = billingCycle,
                senderWalletType = senderWalletType,
                txReference = txReference,
                receiptSelected = receiptSelected,
                receiptUriNote = receiptUriNote
            )
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم إرسال إيصال التحويل بنجاح! سيقوم المطور بمراجعته وتفعيل باقة ${targetPlan.labelAr}."
                onSuccess()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "يرجى التأكد من رقم التحويل أو صورة الإيصال."
            }
        }
    }

    fun setupAdminPin(pin: String): Boolean {
        val res = adminSecurityStore.setupPin(pin)
        return res.onSuccess {
            _adminPinConfigured.value = true
            _isAdminUnlocked.value = true
            _statusBanner.value = "تم تعيين الرمز السري وفتح لوحة تحكم المطور."
        }.onFailure {
            _statusBanner.value = it.localizedMessage ?: "الرمز غير صالح."
        }.isSuccess
    }

    fun unlockAdminPanel(pin: String): Boolean {
        val ok = adminSecurityStore.verifyPin(pin)
        if (ok) {
            _isAdminUnlocked.value = true
        } else {
            _statusBanner.value = "الرمز السري غير صحيح."
        }
        return ok
    }

    fun lockAdminPanel() {
        _isAdminUnlocked.value = false
    }

    fun approveVipRequest(request: VipPaymentRequest) {
        if (isGuestMode || communityRepository == null) {
            guestStore.approveGuestVipRequest(request.id)
            _statusBanner.value = "تم تفعيل باقة ${SubscriptionPlan.fromRaw(request.targetPlan).labelAr} للمستخدم بنجاح ✓"
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = communityRepository.approveVipRequestAsAdmin(request)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = "تم اعتماد الدفع وتفعيل باقة ${SubscriptionPlan.fromRaw(request.targetPlan).labelAr}!"
            }.onFailure {
                _statusBanner.value = "تأكد من تسجيل الدخول ببريد المطور الرسمي لتفعيل الطلبات السحابية: ${it.localizedMessage}"
            }
        }
    }

    fun rejectVipRequest(request: VipPaymentRequest) {
        if (isGuestMode || communityRepository == null) {
            guestStore.rejectGuestVipRequest(request.id)
            _statusBanner.value = "تم رفض الطلب."
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = communityRepository.rejectVipRequestAsAdmin(request)
            _isActionLoading.value = false
            res.onSuccess { _statusBanner.value = "تم تحديث حالة الطلب إلى مرفوض." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "تعذر رفض الطلب." }
        }
    }

    fun toggleVerifyListing(listing: MarketplaceListing) {
        val nextVerified = !listing.verifiedSeller
        val nextFeatured = nextVerified
        if (isGuestMode || communityRepository == null) {
            guestStore.toggleVerifyGuestListing(listing.id, nextVerified, nextFeatured)
            _statusBanner.value = if (nextVerified) "تم توثيق النشاط التجاري بالشارة الذهبية ✓" else "تم إلغاء توثيق النشاط."
            return
        }
        viewModelScope.launch {
            _isActionLoading.value = true
            val res = communityRepository.toggleVerifyListingAsAdmin(listing.id, nextVerified, nextFeatured)
            _isActionLoading.value = false
            res.onSuccess {
                _statusBanner.value = if (nextVerified) "تم توثيق النشاط التجاري وتمييزه ✓" else "تم إلغاء التوثيق."
            }.onFailure {
                _statusBanner.value = "توثيق الإعلانات الأخرى يتطلب بريد المطور الرسمي: ${it.localizedMessage}"
            }
        }
    }

    companion object {
        private const val TAG = "KemetViewModel"
        const val FREE_DAILY_TRANSLATION_LIMIT = 5L
    }
}
