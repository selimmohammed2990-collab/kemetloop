package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class SubscriptionPlan(
    val labelAr: String,
    val monthlyEgp: Long,
    val yearlyEgp: Long,
    val monthlyCoins: Long,
    val monthlyUsd: String,
    val yearlyUsd: String
) {
    FREE_STARTER("الباقة المجانية", 0L, 0L, 100L, "0", "0"),
    PRO("KemetLoop Pro", 99L, 899L, 500L, "4.99", "44.99"),
    BUSINESS_VIP("KemetLoop Business VIP", 299L, 2499L, 1000L, "14.99", "124.99");

    companion object {
        fun fromRaw(raw: String?): SubscriptionPlan =
            entries.firstOrNull { it.name == raw } ?: FREE_STARTER
    }
}

enum class BillingCycle(val labelAr: String) {
    NONE("مجاني"),
    MONTHLY("شهري"),
    YEARLY("سنوي");

    companion object {
        fun fromRaw(raw: String?): BillingCycle =
            entries.firstOrNull { it.name == raw } ?: NONE
    }
}

enum class ListingCategory(val labelAr: String) {
    PRODUCT("منتجات"),
    TRIP("رحلات"),
    SERVICE("خدمات");

    companion object {
        fun fromRaw(raw: String?): ListingCategory =
            entries.firstOrNull { it.name == raw } ?: PRODUCT
    }
}

enum class LinkCategory(val labelAr: String) {
    TRAVEL("سفر"),
    SHOP("تسوق"),
    SOCIAL("تواصل"),
    OTHER("أخرى");

    companion object {
        fun fromRaw(raw: String?): LinkCategory =
            entries.firstOrNull { it.name == raw } ?: OTHER
    }
}

enum class VaultCategory(val labelAr: String) {
    TRAVEL_TICKETS("سفر وتذاكر"),
    ID_DOCS("هوية ووثائق"),
    INVOICES_RECEIPTS("فواتير وإيصالات"),
    MEDIA_FILES("وسائط وملفات");

    companion object {
        fun fromRaw(raw: String?): VaultCategory =
            entries.firstOrNull { it.name == raw } ?: TRAVEL_TICKETS
    }
}

enum class TalentCategory(val labelAr: String) {
    MUSIC("موسيقى وعزف"),
    ART("فنون ورسم"),
    CRAFT("حرف يدوية"),
    STORY("سرد وتاريخ"),
    TECH("تقنية وإبداع");

    companion object {
        fun fromRaw(raw: String?): TalentCategory =
            entries.firstOrNull { it.name == raw } ?: ART
    }
}

enum class SenderWalletType(
    val labelAr: String,
    val shortTag: String,
    val badgeColorHex: Long,
    val isInternational: Boolean
) {
    VODAFONE_CASH("فودافون كاش (Vodafone Cash)", "VF", 0xFFE60000L, false),
    ORANGE_CASH("أورنج كاش (Orange Cash)", "OC", 0xFFFF7900L, false),
    ETISALAT_CASH("اتصالات كاش (e& Money)", "e&", 0xFF84BD00L, false),
    WE_PAY("وي باي (WE Pay)", "WE", 0xFF7B2CBFL, false),
    INSTAPAY("إنستا باي (InstaPay)", "IP", 0xFF4F46E5L, false),
    EGYPT_BANK_APP("تطبيق بنكي مصري (تحويل لمحفظة)", "BNK", 0xFF0EA5E9L, false),
    VISA_MASTERCARD("بطاقة Visa / Mastercard", "VISA", 0xFF2563EBL, true),
    PAYPAL("باي بال (PayPal)", "PP", 0xFF0070BAL, true),
    PAYONEER("بايونير (Payoneer)", "PY", 0xFFFF4800L, true),
    INTL_BANK_TRANSFER("تحويل بنكي دولي", "BANK", 0xFFD4AF37L, true);

    companion object {
        fun fromRaw(raw: String?): SenderWalletType =
            entries.firstOrNull { it.name == raw } ?: VODAFONE_CASH
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val plan: String = SubscriptionPlan.FREE_STARTER.name,
    val billingCycle: String = BillingCycle.NONE.name,
    val coins: Long = 100L,
    val referralCode: String = "",
    val referredBy: String = "",
    val referralCount: Long = 0L,
    val affiliateEarningsCoins: Long = 0L,
    val verifiedBusiness: Boolean = false,
    val dailyTranslationsUsed: Long = 0L,
    val quizWins: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val subscriptionPlan: SubscriptionPlan get() = SubscriptionPlan.fromRaw(plan)
    val activeBillingCycle: BillingCycle get() = BillingCycle.fromRaw(billingCycle)
}

@Keep
@IgnoreExtraProperties
data class SavedLink(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val url: String = "",
    val category: String = LinkCategory.OTHER.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class VaultFileItem(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val fileName: String = "",
    val mimeType: String = "application/octet-stream",
    val category: String = VaultCategory.TRAVEL_TICKETS.name,
    val sizeBytes: Long = 0L,
    val localRelativePath: String = "",
    val note: String = "",
    val cloudSynced: Boolean = false,
    val hasCloudInlineBackup: Boolean = false,
    val createdAtMillis: Long = 0L
) {
    val vaultCategory: VaultCategory get() = VaultCategory.fromRaw(category)
    val isImage: Boolean get() = mimeType.startsWith("image/", ignoreCase = true)
    val isPdf: Boolean get() = mimeType.equals("application/pdf", ignoreCase = true) ||
        fileName.endsWith(".pdf", ignoreCase = true)
}

@Keep
@IgnoreExtraProperties
data class CloudVaultFileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val fileName: String = "",
    val mimeType: String = "application/octet-stream",
    val category: String = VaultCategory.TRAVEL_TICKETS.name,
    val sizeBytes: Long = 0L,
    val note: String = "",
    val inlineDataBase64: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PaymentSubmission(
    @DocumentId val id: String = "",
    val userId: String = "",
    val targetPlan: String = SubscriptionPlan.PRO.name,
    val billingCycle: String = BillingCycle.MONTHLY.name,
    val amountEgp: Long = 99L,
    val walletNumber: String = "01006256085",
    val senderWalletType: String = SenderWalletType.VODAFONE_CASH.name,
    val txReference: String = "",
    val receiptSelected: Boolean = false,
    val receiptUriNote: String = "",
    val status: String = "PENDING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class VipPaymentRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val targetPlan: String = SubscriptionPlan.PRO.name,
    val billingCycle: String = BillingCycle.MONTHLY.name,
    val amountEgp: Long = 99L,
    val walletNumber: String = "01006256085",
    val senderWalletType: String = SenderWalletType.VODAFONE_CASH.name,
    val txReference: String = "",
    val receiptSelected: Boolean = false,
    val receiptUriNote: String = "",
    val status: String = "PENDING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AffiliateLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actionType: String = "SHARE_OFFER",
    val referenceTitle: String = "",
    val offerPriceEgp: Long = 0L,
    val platformCommissionEgp: Long = 0L,
    val earnedCoins: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MarketplaceListing(
    @DocumentId val id: String = "",
    val userId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = ListingCategory.PRODUCT.name,
    val priceEgp: Long = 0L,
    val location: String = "",
    val featured: Boolean = false,
    val verifiedSeller: Boolean = false,
    val whatsappPhone: String = "",
    val socialUrl: String = "",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val listingCategory: ListingCategory get() = ListingCategory.fromRaw(category)
    val platformCommissionEgp: Long get() = (priceEgp * 10L) / 100L
    val affiliateRewardCoins: Long get() = (platformCommissionEgp / 10L).coerceIn(15L, 250L)
}

@Keep
@IgnoreExtraProperties
data class ListingReview(
    @DocumentId val id: String = "",
    val listingId: String = "",
    val userId: String = "",
    val reviewerName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ConciergeRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val requesterName: String = "",
    val category: String = ListingCategory.TRIP.name,
    val title: String = "",
    val details: String = "",
    val budgetEgp: Long = 0L,
    val city: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val listingCategory: ListingCategory get() = ListingCategory.fromRaw(category)
}

@Keep
@IgnoreExtraProperties
data class ConciergeOffer(
    @DocumentId val id: String = "",
    val requestId: String = "",
    val requesterUid: String = "",
    val merchantUserId: String = "",
    val merchantName: String = "",
    val priceEgp: Long = 0L,
    val proposal: String = "",
    val whatsappPhone: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DiscountCoupon(
    @DocumentId val id: String = "",
    val userId: String = "",
    val merchantName: String = "",
    val code: String = "",
    val title: String = "",
    val discountPercent: Long = 10L,
    val city: String = "",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TalentShowcase(
    @DocumentId val id: String = "",
    val userId: String = "",
    val creatorName: String = "",
    val talentTitle: String = "",
    val category: String = TalentCategory.ART.name,
    val bio: String = "",
    val youtubeUrl: String = "",
    val socialUrl: String = "",
    val status: String = "PUBLISHED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val talentCategory: TalentCategory get() = TalentCategory.fromRaw(category)
}

data class TranslatorLanguage(
    val code: String,
    val nameAr: String,
    val bcp47: String
)

data class TranslationResult(
    val translatedText: String,
    val pronunciationGuide: String,
    val culturalTip: String,
    val extractedSourceText: String = "",
    val fromOfflineCache: Boolean = false
)

data class ImageTranslationResult(
    val extractedText: String,
    val translatedText: String,
    val pronunciationGuide: String,
    val culturalTip: String
)

data class EdutainmentRiddle(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val rewardCoins: Long,
    val fact: String,
    val audienceTag: String
)

data class EmergencyHotline(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val phoneNumber: String
)

data class OfflineGuidePlace(
    val id: String,
    val cityAr: String,
    val titleAr: String,
    val summaryAr: String,
    val highlightsAr: String,
    val transportTipAr: String,
    val nearbyHospitalAr: String
)

data class OfflinePhraseEntry(
    val id: String,
    val categoryAr: String,
    val arabic: String,
    val english: String,
    val french: String,
    val german: String,
    val spanish: String,
    val italian: String,
    val pronunciationHint: String
) {
    fun textForCode(code: String): String = when (code) {
        "ar" -> arabic
        "en" -> english
        "fr" -> french
        "de" -> german
        "es" -> spanish
        "it" -> italian
        else -> english
    }
}

object KemetCatalogDefaults {
    val adminEmails = setOf(
        "selimmmohammed@gmail.com",
        "selimmohammed2990@gmail.com"
    )

    val languages = listOf(
        TranslatorLanguage("ar", "العربية", "ar-EG"),
        TranslatorLanguage("en", "الإنجليزية", "en-US"),
        TranslatorLanguage("fr", "الفرنسية", "fr-FR"),
        TranslatorLanguage("de", "الألمانية", "de-DE"),
        TranslatorLanguage("es", "الإسبانية", "es-ES"),
        TranslatorLanguage("it", "الإيطالية", "it-IT"),
        TranslatorLanguage("tr", "التركية", "tr-TR"),
        TranslatorLanguage("zh", "الصينية", "zh-CN")
    )

    val quickPhrases = listOf(
        "أهلاً بكم، كم سعر هذه التحفة؟",
        "هل يوجد خصم عند شراء أكثر من قطعة؟",
        "أين أقرب فندق أو محطة قطار؟",
        "نرحب بكم في رحلتنا النيلية غداً الساعة 9 صباحاً",
        "الدفع متاح نقداً أو عبر المحفظة الإلكترونية"
    )

    val emergencyHotlines = listOf(
        EmergencyHotline("sos_tourist", "شرطة السياحة والآثار", "حماية ومساعدة السائحين على مدار 24 ساعة", "126"),
        EmergencyHotline("sos_amb", "هيئة الإسعاف المصرية", "طوارئ طبية ونقل إسعافي سريع", "123"),
        EmergencyHotline("sos_police", "شرطة النجدة", "البلاغات الأمنية والطوارئ العاجلة", "122"),
        EmergencyHotline("sos_fire", "الحماية المدنية والإطفاء", "طوارئ الحرائق والإنقاذ", "180"),
        EmergencyHotline("sos_ministry", "خط وزارة السياحة الساخن", "الشكاوى والاستفسارات السياحية", "19654")
    )

    val offlineTravelGuide = listOf(
        OfflineGuidePlace(
            id = "guide_giza",
            cityAr = "الجيزة والقاهرة الكبرى",
            titleAr = "أهرامات الجيزة والمتحف المصري الكبير",
            summaryAr = "أعظم معالم الحضارة المصرية القديمة مع مسارات زيارة منظمة وتذاكر إلكترونية.",
            highlightsAr = "الهرم الأكبر · أبو الهول · المتحف الكبير · خان الخليلي",
            transportTipAr = "استخدم مترو الأنفاق أو سيارات الأجرة المرخصة، وابدأ الزيارة 8 صباحاً.",
            nearbyHospitalAr = "مستشفى دار الفؤاد / مستشفى السلام الدولي"
        ),
        OfflineGuidePlace(
            id = "guide_luxor",
            cityAr = "الأقصر",
            titleAr = "معبد الكرنك ووادي الملوك",
            summaryAr = "أكبر متحف مفتوح في العالم يضم معابد البر الشرقي ومقابر البر الغربي.",
            highlightsAr = "الكرنك · معبد الأقصر · وادي الملوك · معبد حتشبسوت",
            transportTipAr = "العبارة النيلية تربط البر الشرقي بالغربي بتكلفة رمزية.",
            nearbyHospitalAr = "مستشفى الأقصر الدولي"
        ),
        OfflineGuidePlace(
            id = "guide_aswan",
            cityAr = "أسوان",
            titleAr = "معبد فيلة والقرية النوبية",
            summaryAr = "هدوء النيل الساحر والبيوت النوبية الملونة والحرف اليدوية التراثية.",
            highlightsAr = "معبد فيلة · السد العالي · القرية النوبية · جزيرة النباتات",
            transportTipAr = "الفلوكة والمراكب الشراعية هي الوسيلة الأجمل للتنقل عند الغروب.",
            nearbyHospitalAr = "مستشفى أسوان التخصصي"
        ),
        OfflineGuidePlace(
            id = "guide_redsea",
            cityAr = "شرم الشيخ والغردقة",
            titleAr = "الشعاب المرجانية والرحلات البحرية",
            summaryAr = "وجهة الغوص العالمية ومحميات رأس محمد والجفتون والأنشطة البحرية.",
            highlightsAr = "محمية رأس محمد · خليج نعمة · جزيرة الجفتون · الجونة",
            transportTipAr = "احرص على حجز رحلات الغوص عبر مراكز معتمدة وموثقة.",
            nearbyHospitalAr = "مستشفى شرم الشيخ الدولي / مستشفى الغردقة العام"
        ),
        OfflineGuidePlace(
            id = "guide_alex",
            cityAr = "الإسكندرية",
            titleAr = "مكتبة الإسكندرية وقلعة قايتباي",
            summaryAr = "عروس البحر المتوسط بتاريخها الهلنستي وكورنيشها العريق.",
            highlightsAr = "قلعة قايتباي · مكتبة الإسكندرية · حدائق المنتزه",
            transportTipAr = "الترام الأزرق التاريخي وسيلة اقتصادية وممتعة لاستكشاف وسط المدينة.",
            nearbyHospitalAr = "المستشفى الأميري الجامعي"
        )
    )

    val offlineTopPhrases = listOf(
        OfflinePhraseEntry(
            id = "ph_1",
            categoryAr = "تعارف وترحيب",
            arabic = "أهلاً وسهلاً بك في مصر، كيف يمكنني مساعدتك؟",
            english = "Welcome to Egypt, how can I help you?",
            french = "Bienvenue en Égypte, comment puis-je vous aider ?",
            german = "Willkommen in Ägypten, wie kann ich Ihnen helfen?",
            spanish = "Bienvenido a Egipto, ¿cómo puedo ayudarte?",
            italian = "Benvenuto in Egitto, come posso aiutarti?",
            pronunciationHint = "Ahlan wa sahlan / Welcome to Egypt"
        ),
        OfflinePhraseEntry(
            id = "ph_2",
            categoryAr = "التسوق والأسعار",
            arabic = "كم سعر هذا المنتج؟ وهل يوجد خصم؟",
            english = "How much is this item? Is there a discount?",
            french = "Combien coûte cet article ? Y a-t-il une réduction ?",
            german = "Wie viel kostet dieser Artikel? Gibt es einen Rabatt?",
            spanish = "¿Cuánto cuesta este artículo? ¿Hay descuento?",
            italian = "Quanto costa questo articolo? C'è uno sconto?",
            pronunciationHint = "Bikam dah? / How much is this item?"
        ),
        OfflinePhraseEntry(
            id = "ph_3",
            categoryAr = "المطاعم والطعام",
            arabic = "من فضلك أريد قائمة الطعام، هل هذا الطعام حلال؟",
            english = "Please bring the menu. Is this food Halal?",
            french = "Le menu s'il vous plaît. Cette nourriture est-elle Halal ?",
            german = "Bitte die Speisekarte. Ist dieses Essen Halal?",
            spanish = "Por favor, el menú. ¿Esta comida es Halal?",
            italian = "Per favore il menu. Questo cibo è Halal?",
            pronunciationHint = "Min fadlak el-menu / Please bring the menu"
        ),
        OfflinePhraseEntry(
            id = "ph_4",
            categoryAr = "المواصلات والاتجاهات",
            arabic = "أين أقرب محطة أو صيدلية أو ماكينة صراف آلي؟",
            english = "Where is the nearest station, pharmacy, or ATM?",
            french = "Où est la station, pharmacie ou distributeur le plus proche ?",
            german = "Wo ist die nächste Station, Apotheke oder der nächste Geldautomat?",
            spanish = "¿Dónde está la estación, farmacia o cajero más cercano?",
            italian = "Dov'è la stazione, farmacia o bancomat più vicino?",
            pronunciationHint = "Fayn aqrab saydaliya aw ATM?"
        ),
        OfflinePhraseEntry(
            id = "ph_5",
            categoryAr = "الطوارئ والمساعدة",
            arabic = "أحتاج إلى مساعدة طبية عاجلة أو الاتصال بشرطة السياحة (126).",
            english = "I need urgent medical help or tourist police (126).",
            french = "J'ai besoin d'une aide médicale urgente ou de la police touristique (126).",
            german = "Ich brauche dringend medizinische Hilfe oder die Touristenpolizei (126).",
            spanish = "Necesito ayuda médica urgente o a la policía turística (126).",
            italian = "Ho bisogno di aiuto medico urgente o della polizia turistica (126).",
            pronunciationHint = "Urgent medical help / Police 126"
        )
    )

    val riddles = listOf(
        EdutainmentRiddle(
            id = "r1",
            question = "ما هي المدينة المصرية التي تضم ثلث آثار العالم القديم وتُعرف بمدينة المائة باب؟",
            options = listOf("الأقصر", "الإسكندرية", "بورسعيد", "المنصورة"),
            correctIndex = 0,
            rewardCoins = 25L,
            fact = "الأقصر (طيبة القديمة) كانت عاصمة مصر الفرعونية في عصر الدولة الحديثة وتضم معبد الكرنك ووادي الملوك.",
            audienceTag = "تاريخ وجغرافيا"
        ),
        EdutainmentRiddle(
            id = "r2",
            question = "لغز ذكي: شيء كلما أخذت منه كبر، وكلما وضعت فيه صغر. ما هو؟",
            options = listOf("النيل", "الحفرة", "الهرم", "الميزان"),
            correctIndex = 1,
            rewardCoins = 20L,
            fact = "الألغاز التفاعلية تنمي التفكير الإبداعي للصغار والكبار وتمنحك عملات KemetLoop!",
            audienceTag = "ألغاز للجميع"
        ),
        EdutainmentRiddle(
            id = "r3",
            question = "في أي حي قاهري تاريخي يشتهر التجار بصناعة الفوانيس النحاسية والمشغولات اليدوية؟",
            options = listOf("خان الخليلي", "الزمالك", "المعادي", "مدينة نصر"),
            correctIndex = 0,
            rewardCoins = 25L,
            fact = "خان الخليلي يعود تاريخه للقرن الرابع عشر ويُعد أشهر سوق تراثي للحرفيين في مصر.",
            audienceTag = "تراث وتجارة"
        ),
        EdutainmentRiddle(
            id = "r4",
            question = "أين يقع معبد فيلة الشهير الذي نُقل لحمايته من مياه النيل ويستقبل السائحين بالفلوكة؟",
            options = listOf("أسوان", "مرسى مطروح", "الفيوم", "السويس"),
            correctIndex = 0,
            rewardCoins = 30L,
            fact = "معبد فيلة في أسوان يُعد جوهرة النيل ونُقل بالتعاون الدولي إلى جزيرة أجيليكا.",
            audienceTag = "سياحة مصرية"
        ),
        EdutainmentRiddle(
            id = "r5",
            question = "لغز حسابي للتجار: إذا كان سعر رحلة 1000 ج.م ونسبة عمولة المنصة 10%، فكم تبلغ عمولة المنصة؟",
            options = listOf("50 ج.م", "100 ج.م", "150 ج.م", "200 ج.م"),
            correctIndex = 1,
            rewardCoins = 20L,
            fact = "الشفافية في حساب نسبة الـ 10% تساعد التجار والمسوقين بالعمولة على تعظيم أرباحهم.",
            audienceTag = "تسويق وذكاء"
        )
    )
}
