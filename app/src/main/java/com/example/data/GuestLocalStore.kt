package com.example.data

import android.content.Context
import com.example.model.BillingCycle
import com.example.model.ConciergeOffer
import com.example.model.ConciergeRequest
import com.example.model.DiscountCoupon
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
import com.example.model.UserProfile
import com.example.model.VipPaymentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Local persistence for Instant Guest Mode & Offline testing so guests receive 100 welcome coins
 * and can test marketplace, Link Box, Concierge, 5-star reviews, QR coupons, translator, and games.
 */
class GuestLocalStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("kemetloop_guest_store", Context.MODE_PRIVATE)

    private val _guestProfile = MutableStateFlow(loadProfile())
    val guestProfile: StateFlow<UserProfile> = _guestProfile.asStateFlow()

    private val _guestLinks = MutableStateFlow(loadLinks())
    val guestLinks: StateFlow<List<SavedLink>> = _guestLinks.asStateFlow()

    private val _guestListings = MutableStateFlow(loadStarterListings())
    val guestListings: StateFlow<List<MarketplaceListing>> = _guestListings.asStateFlow()

    private val _guestTalents = MutableStateFlow(loadStarterTalents())
    val guestTalents: StateFlow<List<TalentShowcase>> = _guestTalents.asStateFlow()

    private val _guestConciergeRequests = MutableStateFlow(loadStarterConciergeRequests())
    val guestConciergeRequests: StateFlow<List<ConciergeRequest>> = _guestConciergeRequests.asStateFlow()

    private val _guestConciergeOffers = MutableStateFlow(loadStarterConciergeOffers())
    val guestConciergeOffers: StateFlow<Map<String, List<ConciergeOffer>>> = _guestConciergeOffers.asStateFlow()

    private val _guestReviews = MutableStateFlow(loadStarterReviews())
    val guestReviews: StateFlow<Map<String, List<ListingReview>>> = _guestReviews.asStateFlow()

    private val _guestCoupons = MutableStateFlow(loadStarterCoupons())
    val guestCoupons: StateFlow<List<DiscountCoupon>> = _guestCoupons.asStateFlow()

    private val _guestPaymentSubmissions = MutableStateFlow<List<PaymentSubmission>>(emptyList())
    val guestPaymentSubmissions: StateFlow<List<PaymentSubmission>> = _guestPaymentSubmissions.asStateFlow()

    private val _guestVipRequests = MutableStateFlow(loadStarterVipRequests())
    val guestVipRequests: StateFlow<List<VipPaymentRequest>> = _guestVipRequests.asStateFlow()

    private fun loadProfile(): UserProfile {
        val initialized = prefs.getBoolean(KEY_INITIALIZED, false)
        if (!initialized) {
            prefs.edit()
                .putBoolean(KEY_INITIALIZED, true)
                .putLong(KEY_COINS, 100L)
                .putLong(KEY_QUIZ_WINS, 0L)
                .putLong(KEY_TRANSLATIONS, 0L)
                .putLong(KEY_AFFILIATE_COINS, 0L)
                .putLong(KEY_REFERRAL_COUNT, 0L)
                .putString(KEY_PLAN, SubscriptionPlan.FREE_STARTER.name)
                .putString(KEY_BILLING, BillingCycle.NONE.name)
                .putBoolean(KEY_VERIFIED_BIZ, false)
                .apply()
        }
        return UserProfile(
            id = GUEST_UID,
            userId = GUEST_UID,
            displayName = prefs.getString(KEY_DISPLAY_NAME, "زائر KemetLoop") ?: "زائر KemetLoop",
            email = "وضع التصفح الفوري (غير مربوط بحساب Google)",
            plan = prefs.getString(KEY_PLAN, SubscriptionPlan.FREE_STARTER.name) ?: SubscriptionPlan.FREE_STARTER.name,
            billingCycle = prefs.getString(KEY_BILLING, BillingCycle.NONE.name) ?: BillingCycle.NONE.name,
            coins = prefs.getLong(KEY_COINS, 100L),
            referralCode = "KEMETGUEST",
            referralCount = prefs.getLong(KEY_REFERRAL_COUNT, 0L),
            affiliateEarningsCoins = prefs.getLong(KEY_AFFILIATE_COINS, 0L),
            verifiedBusiness = prefs.getBoolean(KEY_VERIFIED_BIZ, false),
            dailyTranslationsUsed = prefs.getLong(KEY_TRANSLATIONS, 0L),
            quizWins = prefs.getLong(KEY_QUIZ_WINS, 0L)
        )
    }

    private fun loadLinks(): List<SavedLink> {
        val raw = prefs.getString(KEY_LINKS_JSON, null)
        if (raw.isNullOrBlank()) {
            val starter = listOf(
                SavedLink(
                    id = "guest_link_1",
                    userId = GUEST_UID,
                    title = "رحلات النيل الذهبية - KemetLoop",
                    url = "https://kemetloop.app/nile-trips",
                    category = LinkCategory.TRAVEL.name
                ),
                SavedLink(
                    id = "guest_link_2",
                    userId = GUEST_UID,
                    title = "دليل بازار خان الخليلي",
                    url = "https://kemetloop.app/khan-el-khalili",
                    category = LinkCategory.SHOP.name
                )
            )
            saveLinks(starter)
            return starter
        }
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { idx ->
                val obj = arr.getJSONObject(idx)
                SavedLink(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    userId = GUEST_UID,
                    title = obj.optString("title", ""),
                    url = obj.optString("url", ""),
                    category = obj.optString("category", LinkCategory.OTHER.name)
                )
            }
        }.getOrElse { emptyList() }
    }

    private fun saveLinks(links: List<SavedLink>) {
        val arr = JSONArray()
        links.forEach { link ->
            arr.put(
                JSONObject().apply {
                    put("id", link.id)
                    put("title", link.title)
                    put("url", link.url)
                    put("category", link.category)
                }
            )
        }
        prefs.edit().putString(KEY_LINKS_JSON, arr.toString()).apply()
    }

    fun addCoins(delta: Long, isQuizWin: Boolean = false, isAffiliate: Boolean = false, isInvite: Boolean = false) {
        val current = _guestProfile.value
        val newCoins = (current.coins + delta).coerceAtLeast(0L)
        val newQuizWins = if (isQuizWin) current.quizWins + 1 else current.quizWins
        val newAffiliate = if (isAffiliate) current.affiliateEarningsCoins + delta else current.affiliateEarningsCoins
        val newReferrals = if (isInvite) current.referralCount + 1 else current.referralCount
        prefs.edit()
            .putLong(KEY_COINS, newCoins)
            .putLong(KEY_QUIZ_WINS, newQuizWins)
            .putLong(KEY_AFFILIATE_COINS, newAffiliate)
            .putLong(KEY_REFERRAL_COUNT, newReferrals)
            .apply()
        _guestProfile.value = current.copy(
            coins = newCoins,
            quizWins = newQuizWins,
            affiliateEarningsCoins = newAffiliate,
            referralCount = newReferrals
        )
    }

    fun incrementTranslations() {
        val current = _guestProfile.value
        val next = current.dailyTranslationsUsed + 1
        prefs.edit().putLong(KEY_TRANSLATIONS, next).apply()
        _guestProfile.value = current.copy(dailyTranslationsUsed = next)
    }

    fun updateDisplayName(newName: String) {
        val clean = newName.trim().take(80)
        if (clean.length < 2) return
        prefs.edit().putString(KEY_DISPLAY_NAME, clean).apply()
        _guestProfile.value = _guestProfile.value.copy(displayName = clean)
    }

    fun addSavedLink(title: String, url: String, category: LinkCategory) {
        val normalizedUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val item = SavedLink(
            id = "guest_link_${System.currentTimeMillis()}",
            userId = GUEST_UID,
            title = title.trim(),
            url = normalizedUrl.trim(),
            category = category.name
        )
        val updated = listOf(item) + _guestLinks.value
        saveLinks(updated)
        _guestLinks.value = updated
    }

    fun deleteSavedLink(linkId: String) {
        val updated = _guestLinks.value.filterNot { it.id == linkId }
        saveLinks(updated)
        _guestLinks.value = updated
    }

    fun addGuestListing(
        title: String,
        description: String,
        category: ListingCategory,
        priceEgp: Long,
        location: String
    ) {
        val isVip = _guestProfile.value.subscriptionPlan == SubscriptionPlan.BUSINESS_VIP
        val item = MarketplaceListing(
            id = "guest_listing_${System.currentTimeMillis()}",
            userId = GUEST_UID,
            sellerName = "زائر KemetLoop",
            title = title.trim(),
            description = description.trim(),
            category = category.name,
            priceEgp = priceEgp,
            location = location.trim(),
            featured = isVip,
            verifiedSeller = isVip,
            status = "ACTIVE"
        )
        _guestListings.value = listOf(item) + _guestListings.value
    }

    fun deleteGuestListing(listingId: String) {
        _guestListings.value = _guestListings.value.filterNot { it.id == listingId }
    }

    fun toggleVerifyGuestListing(listingId: String, verified: Boolean, featured: Boolean) {
        _guestListings.value = _guestListings.value.map { item ->
            if (item.id == listingId) item.copy(verifiedSeller = verified, featured = featured) else item
        }
    }

    fun addGuestReview(listingId: String, rating: Int, comment: String) {
        val review = ListingReview(
            id = GUEST_UID,
            listingId = listingId,
            userId = GUEST_UID,
            reviewerName = _guestProfile.value.displayName,
            rating = rating.toLong().coerceIn(1L, 5L),
            comment = comment.trim()
        )
        val map = _guestReviews.value.toMutableMap()
        val existing = map[listingId].orEmpty().filterNot { it.userId == GUEST_UID }
        map[listingId] = listOf(review) + existing
        _guestReviews.value = map
    }

    fun addGuestConciergeRequest(
        category: ListingCategory,
        title: String,
        details: String,
        budgetEgp: Long,
        city: String
    ) {
        val req = ConciergeRequest(
            id = "creq_guest_${System.currentTimeMillis()}",
            userId = GUEST_UID,
            requesterName = _guestProfile.value.displayName,
            category = category.name,
            title = title.trim(),
            details = details.trim(),
            budgetEgp = budgetEgp,
            city = city.trim(),
            status = "OPEN"
        )
        _guestConciergeRequests.value = listOf(req) + _guestConciergeRequests.value
    }

    fun addGuestConciergeOffer(
        requestId: String,
        priceEgp: Long,
        proposal: String,
        whatsappPhone: String
    ) {
        val offer = ConciergeOffer(
            id = "offer_guest_${System.currentTimeMillis()}",
            requestId = requestId,
            requesterUid = GUEST_UID,
            merchantUserId = GUEST_UID,
            merchantName = _guestProfile.value.displayName,
            priceEgp = priceEgp,
            proposal = proposal.trim(),
            whatsappPhone = whatsappPhone.trim()
        )
        val map = _guestConciergeOffers.value.toMutableMap()
        map[requestId] = listOf(offer) + map[requestId].orEmpty()
        _guestConciergeOffers.value = map
    }

    fun addGuestCoupon(
        merchantName: String,
        code: String,
        title: String,
        discountPercent: Long,
        city: String
    ) {
        val coupon = DiscountCoupon(
            id = "cpn_guest_${System.currentTimeMillis()}",
            userId = GUEST_UID,
            merchantName = merchantName.ifBlank { "متجر KemetLoop" },
            code = code.trim().uppercase(),
            title = title.trim(),
            discountPercent = discountPercent.coerceIn(1L, 90L),
            city = city.trim().ifBlank { "القاهرة" },
            status = "ACTIVE"
        )
        _guestCoupons.value = listOf(coupon) + _guestCoupons.value
    }

    fun submitGuestVipRequest(
        targetPlan: SubscriptionPlan,
        billingCycle: BillingCycle,
        senderWalletType: SenderWalletType,
        txReference: String,
        receiptSelected: Boolean,
        receiptUriNote: String
    ) {
        val amount = if (billingCycle == BillingCycle.YEARLY) targetPlan.yearlyEgp else targetPlan.monthlyEgp
        val id = "vip_guest_${System.currentTimeMillis()}"
        val submission = PaymentSubmission(
            id = id,
            userId = GUEST_UID,
            targetPlan = targetPlan.name,
            billingCycle = billingCycle.name,
            amountEgp = amount,
            walletNumber = "01006256085",
            senderWalletType = senderWalletType.name,
            txReference = txReference.trim(),
            receiptSelected = receiptSelected,
            receiptUriNote = receiptUriNote.trim(),
            status = "PENDING"
        )
        val adminReq = VipPaymentRequest(
            id = id,
            userId = GUEST_UID,
            userName = _guestProfile.value.displayName,
            userEmail = _guestProfile.value.email,
            targetPlan = targetPlan.name,
            billingCycle = billingCycle.name,
            amountEgp = amount,
            walletNumber = "01006256085",
            senderWalletType = senderWalletType.name,
            txReference = txReference.trim(),
            receiptSelected = receiptSelected,
            receiptUriNote = receiptUriNote.trim(),
            status = "PENDING"
        )
        _guestPaymentSubmissions.value = listOf(submission) + _guestPaymentSubmissions.value
        _guestVipRequests.value = listOf(adminReq) + _guestVipRequests.value
    }

    fun approveGuestVipRequest(reqId: String) {
        val req = _guestVipRequests.value.firstOrNull { it.id == reqId } ?: return
        val plan = SubscriptionPlan.fromRaw(req.targetPlan)
        _guestVipRequests.value = _guestVipRequests.value.map {
            if (it.id == reqId) it.copy(status = "APPROVED") else it
        }
        _guestPaymentSubmissions.value = _guestPaymentSubmissions.value.map {
            if (it.id == reqId) it.copy(status = "APPROVED") else it
        }
        if (req.userId == GUEST_UID) {
            val cur = _guestProfile.value
            val updatedCoins = cur.coins + plan.monthlyCoins
            val verified = plan == SubscriptionPlan.BUSINESS_VIP
            prefs.edit()
                .putString(KEY_PLAN, plan.name)
                .putString(KEY_BILLING, req.billingCycle)
                .putBoolean(KEY_VERIFIED_BIZ, verified)
                .putLong(KEY_COINS, updatedCoins)
                .apply()
            _guestProfile.value = cur.copy(
                plan = plan.name,
                billingCycle = req.billingCycle,
                verifiedBusiness = verified,
                coins = updatedCoins
            )
        }
    }

    fun rejectGuestVipRequest(reqId: String) {
        _guestVipRequests.value = _guestVipRequests.value.map {
            if (it.id == reqId) it.copy(status = "REJECTED") else it
        }
        _guestPaymentSubmissions.value = _guestPaymentSubmissions.value.map {
            if (it.id == reqId) it.copy(status = "REJECTED") else it
        }
    }

    fun addGuestTalent(
        creatorName: String,
        talentTitle: String,
        category: TalentCategory,
        bio: String,
        youtubeUrl: String,
        socialUrl: String
    ) {
        val item = TalentShowcase(
            id = "guest_talent_${System.currentTimeMillis()}",
            userId = GUEST_UID,
            creatorName = creatorName.ifBlank { "مبدع زائر" },
            talentTitle = talentTitle.trim(),
            category = category.name,
            bio = bio.trim(),
            youtubeUrl = if (youtubeUrl.startsWith("http")) youtubeUrl else "https://$youtubeUrl",
            socialUrl = socialUrl.trim(),
            status = "PUBLISHED"
        )
        _guestTalents.value = listOf(item) + _guestTalents.value
    }

    private fun loadStarterListings(): List<MarketplaceListing> = listOf(
        MarketplaceListing(
            id = "demo_1",
            userId = "kemet_official",
            sellerName = "بازار النيل الذهبي",
            title = "رحلة فلوكة أسوان وغروب النيل الملكية",
            description = "جولة VIP خاصة في النيل لمدة 3 ساعات مع مرشد سياحي وضيافة مصرية فاخرة.",
            category = ListingCategory.TRIP.name,
            priceEgp = 1450L,
            location = "أسوان - كورنيش النيل",
            featured = true,
            verifiedSeller = true,
            whatsappPhone = "+201006256085",
            socialUrl = "https://www.youtube.com/@EgyptTravel"
        ),
        MarketplaceListing(
            id = "demo_2",
            userId = "kemet_official",
            sellerName = "حرف خان الخليلي",
            title = "فانوس نحاس يدوي مطعّم بالذهب والزجاج",
            description = "تحفة فنية مصرية مصنوعة يدوياً بأعلى جودة تناسب القصور والهدايا الفاخرة.",
            category = ListingCategory.PRODUCT.name,
            priceEgp = 680L,
            location = "القاهرة - الحسين",
            featured = false,
            verifiedSeller = true,
            whatsappPhone = "+201006256085"
        ),
        MarketplaceListing(
            id = "demo_3",
            userId = "kemet_official",
            sellerName = "استقبال وتجارة",
            title = "مرافق تجاري ومترجم ذكي للمعارض والأسواق",
            description = "خدمة مرافقة للتجار والوفود الدولية في القاهرة والإسكندرية مع دعم الترجمة الفورية.",
            category = ListingCategory.SERVICE.name,
            priceEgp = 900L,
            location = "القاهرة والجيزة",
            featured = false,
            verifiedSeller = false
        )
    )

    private fun loadStarterTalents(): List<TalentShowcase> = listOf(
        TalentShowcase(
            id = "talent_demo_1",
            userId = "kemet_official",
            creatorName = "كريم النوبي",
            talentTitle = "عزف عود نُوبي ومقطوعات تراثية",
            category = TalentCategory.MUSIC.name,
            bio = "مؤلف موسيقي شاب يمزج ألحان النوبة الأصيلة بالموسيقى العالمية.",
            youtubeUrl = "https://www.youtube.com/results?search_query=arabic+oud+music",
            socialUrl = "https://www.instagram.com/"
        ),
        TalentShowcase(
            id = "talent_demo_2",
            userId = "kemet_official",
            creatorName = "سلمى الخطاطة",
            talentTitle = "الخط العربي الذهبي على ورق البردي",
            category = TalentCategory.ART.name,
            bio = "تصميم لوحات فنية بالخط الديواني والثلث مستوحاة من الحضارة المصرية.",
            youtubeUrl = "https://www.youtube.com/results?search_query=arabic+calligraphy+art",
            socialUrl = "https://www.tiktok.com/"
        )
    )

    private fun loadStarterConciergeRequests(): List<ConciergeRequest> = listOf(
        ConciergeRequest(
            id = "creq_demo_1",
            userId = GUEST_UID,
            requesterName = "ضيف KemetLoop",
            category = ListingCategory.TRIP.name,
            title = "برنامج سياحي خاص في الأقصر وأسوان لمدة 4 أيام",
            details = "أبحث عن باقة عائلية شاملة الإقامة النيلية ومرشد يتحدث العربية والإنجليزية.",
            budgetEgp = 12000L,
            city = "الأقصر وأسوان",
            status = "OPEN"
        )
    )

    private fun loadStarterConciergeOffers(): Map<String, List<ConciergeOffer>> = mapOf(
        "creq_demo_1" to listOf(
            ConciergeOffer(
                id = "offer_demo_1",
                requestId = "creq_demo_1",
                requesterUid = GUEST_UID,
                merchantUserId = "vip_nile_tours",
                merchantName = "شركة نايل جولد للسياحة VIP",
                priceEgp = 10900L,
                proposal = "باقة 4 أيام 5 نجوم شاملة الانتقالات وجولات الكرنك وفيلة مع خصم VIP.",
                whatsappPhone = "+201006256085"
            )
        )
    )

    private fun loadStarterReviews(): Map<String, List<ListingReview>> = mapOf(
        "demo_1" to listOf(
            ListingReview(
                id = "rev_1",
                listingId = "demo_1",
                userId = "sample_user",
                reviewerName = "أحمد منصور",
                rating = 5L,
                comment = "رحلة نيلية لا تُنسى وتنظيم راقٍ جداً وقت الغروب."
            )
        ),
        "demo_2" to listOf(
            ListingReview(
                id = "rev_2",
                listingId = "demo_2",
                userId = "sample_user",
                reviewerName = "سارة خالد",
                rating = 5L,
                comment = "تغليف فاخر وخامة نحاسية ممتازة."
            )
        )
    )

    private fun loadStarterCoupons(): List<DiscountCoupon> = listOf(
        DiscountCoupon(
            id = "cpn_demo_1",
            userId = "kemet_official",
            merchantName = "بازار النيل الذهبي",
            code = "KEMET20",
            title = "خصم 20% على رحلات الفلوكة الخاصة",
            discountPercent = 20L,
            city = "أسوان",
            status = "ACTIVE"
        ),
        DiscountCoupon(
            id = "cpn_demo_2",
            userId = "kemet_official",
            merchantName = "حرف خان الخليلي",
            code = "KHAN15",
            title = "خصم 15% على الفوانيس والمشغولات النحاسية",
            discountPercent = 15L,
            city = "القاهرة",
            status = "ACTIVE"
        )
    )

    private fun loadStarterVipRequests(): List<VipPaymentRequest> = listOf(
        VipPaymentRequest(
            id = "vip_demo_sample",
            userId = "sample_merchant_1",
            userName = "بازار الحسين الملكي",
            userEmail = "merchant@example.com",
            targetPlan = SubscriptionPlan.BUSINESS_VIP.name,
            billingCycle = BillingCycle.MONTHLY.name,
            amountEgp = 299L,
            walletNumber = "01006256085",
            senderWalletType = SenderWalletType.VODAFONE_CASH.name,
            txReference = "VF8842109",
            receiptSelected = true,
            status = "PENDING"
        )
    )

    companion object {
        const val GUEST_UID = "local_guest"
        private const val KEY_INITIALIZED = "guest_initialized"
        private const val KEY_COINS = "guest_coins"
        private const val KEY_QUIZ_WINS = "guest_quiz_wins"
        private const val KEY_TRANSLATIONS = "guest_translations"
        private const val KEY_AFFILIATE_COINS = "guest_affiliate_coins"
        private const val KEY_REFERRAL_COUNT = "guest_referral_count"
        private const val KEY_LINKS_JSON = "guest_links_json"
        private const val KEY_PLAN = "guest_plan"
        private const val KEY_BILLING = "guest_billing"
        private const val KEY_VERIFIED_BIZ = "guest_verified_biz"
        private const val KEY_DISPLAY_NAME = "guest_display_name"
    }
}
