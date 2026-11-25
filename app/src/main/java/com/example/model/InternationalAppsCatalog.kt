package com.example.model

enum class InternationalAppCategory(val labelAr: String) {
    VIDEO_ENTERTAINMENT("الفيديو والترفيه"),
    GLOBAL_SOCIAL("التواصل الاجتماعي العالمي"),
    ASIAN_APPS("التطبيقات الصينية واليابانية والآسيوية"),
    AFRICAN_ARAB_APPS("التطبيقات الإفريقية والعربية"),
    TRAVEL_SERVICES("السفر والخدمات الدولية")
}

data class InternationalAppItem(
    val id: String,
    val name: String,
    val subtitleAr: String,
    val url: String,
    val category: InternationalAppCategory,
    val badgeText: String,
    val accentColorHex: Long
)

object InternationalAppsCatalog {
    val apps: List<InternationalAppItem> = listOf(
        // الفيديو والترفيه
        InternationalAppItem(
            id = "yt",
            name = "YouTube",
            subtitleAr = "فيديو وبث مباشر عالمي",
            url = "https://m.youtube.com",
            category = InternationalAppCategory.VIDEO_ENTERTAINMENT,
            badgeText = "YT",
            accentColorHex = 0xFFFF0000
        ),
        InternationalAppItem(
            id = "tiktok",
            name = "TikTok",
            subtitleAr = "مقاطع قصيرة وترندات",
            url = "https://www.tiktok.com",
            category = InternationalAppCategory.VIDEO_ENTERTAINMENT,
            badgeText = "TT",
            accentColorHex = 0xFF00F2EA
        ),
        InternationalAppItem(
            id = "twitch",
            name = "Twitch",
            subtitleAr = "بث ألعاب ومجتمعات مباشرة",
            url = "https://m.twitch.tv",
            category = InternationalAppCategory.VIDEO_ENTERTAINMENT,
            badgeText = "TW",
            accentColorHex = 0xFF9146FF
        ),

        // التواصل الاجتماعي العالمي
        InternationalAppItem(
            id = "whatsapp",
            name = "WhatsApp",
            subtitleAr = "مراسلات ومكالمات عالمية",
            url = "https://web.whatsapp.com",
            category = InternationalAppCategory.GLOBAL_SOCIAL,
            badgeText = "WA",
            accentColorHex = 0xFF25D366
        ),
        InternationalAppItem(
            id = "telegram",
            name = "Telegram",
            subtitleAr = "قنوات ومحادثات سريعة",
            url = "https://web.telegram.org",
            category = InternationalAppCategory.GLOBAL_SOCIAL,
            badgeText = "TG",
            accentColorHex = 0xFF229ED9
        ),
        InternationalAppItem(
            id = "instagram",
            name = "Instagram",
            subtitleAr = "صور وريلز السفر والتجارة",
            url = "https://www.instagram.com",
            category = InternationalAppCategory.GLOBAL_SOCIAL,
            badgeText = "IG",
            accentColorHex = 0xFFE1306C
        ),
        InternationalAppItem(
            id = "facebook",
            name = "Facebook",
            subtitleAr = "تواصل ومجموعات تجارية",
            url = "https://m.facebook.com",
            category = InternationalAppCategory.GLOBAL_SOCIAL,
            badgeText = "FB",
            accentColorHex = 0xFF1877F2
        ),
        InternationalAppItem(
            id = "x_twitter",
            name = "X",
            subtitleAr = "الأخبار العاجلة والتغريدات",
            url = "https://x.com",
            category = InternationalAppCategory.GLOBAL_SOCIAL,
            badgeText = "X",
            accentColorHex = 0xFFE2E8F0
        ),
        InternationalAppItem(
            id = "linkedin",
            name = "LinkedIn",
            subtitleAr = "شبكة الأعمال والشركات",
            url = "https://www.linkedin.com",
            category = InternationalAppCategory.GLOBAL_SOCIAL,
            badgeText = "IN",
            accentColorHex = 0xFF0A66C2
        ),

        // التطبيقات الصينية واليابانية والآسيوية
        InternationalAppItem(
            id = "wechat",
            name = "WeChat",
            subtitleAr = "التواصل والأعمال في الصين",
            url = "https://www.wechat.com",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "WC",
            accentColorHex = 0xFF07C160
        ),
        InternationalAppItem(
            id = "xiaohongshu",
            name = "Xiaohongshu",
            subtitleAr = "اكتشاف السفر والتسوق الصيني",
            url = "https://www.xiaohongshu.com",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "RED",
            accentColorHex = 0xFFFF2442
        ),
        InternationalAppItem(
            id = "aliexpress",
            name = "AliExpress",
            subtitleAr = "تسوق عالمي مباشر",
            url = "https://m.aliexpress.com",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "AE",
            accentColorHex = 0xFFFF4747
        ),
        InternationalAppItem(
            id = "alibaba",
            name = "Alibaba",
            subtitleAr = "تجارة الجملة والمصانع",
            url = "https://m.alibaba.com",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "AB",
            accentColorHex = 0xFFFF6A00
        ),
        InternationalAppItem(
            id = "trip_com",
            name = "Trip.com",
            subtitleAr = "حجوزات السفر الآسيوية والدولية",
            url = "https://www.trip.com",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "TR",
            accentColorHex = 0xFF287DFA
        ),
        InternationalAppItem(
            id = "line",
            name = "LINE",
            subtitleAr = "التواصل الأشهر في اليابان",
            url = "https://line.me",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "LN",
            accentColorHex = 0xFF06C755
        ),
        InternationalAppItem(
            id = "rakuten_travel",
            name = "Rakuten Travel",
            subtitleAr = "فنادق ومنتجعات اليابان",
            url = "https://travel.rakuten.com",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "RK",
            accentColorHex = 0xFFBF0000
        ),
        InternationalAppItem(
            id = "pixiv",
            name = "Pixiv",
            subtitleAr = "مجتمع الفن والتصميم الياباني",
            url = "https://www.pixiv.net",
            category = InternationalAppCategory.ASIAN_APPS,
            badgeText = "PX",
            accentColorHex = 0xFF0096FA
        ),

        // التطبيقات الإفريقية والعربية
        InternationalAppItem(
            id = "jumia",
            name = "Jumia",
            subtitleAr = "التسوق الرائد في مصر وإفريقيا",
            url = "https://www.jumia.com.eg",
            category = InternationalAppCategory.AFRICAN_ARAB_APPS,
            badgeText = "JM",
            accentColorHex = 0xFFF68B1E
        ),
        InternationalAppItem(
            id = "boomplay",
            name = "Boomplay",
            subtitleAr = "الموسيقى والترفيه الإفريقي",
            url = "https://www.boomplay.com",
            category = InternationalAppCategory.AFRICAN_ARAB_APPS,
            badgeText = "BP",
            accentColorHex = 0xFF00D6C6
        ),
        InternationalAppItem(
            id = "flutterwave",
            name = "Flutterwave",
            subtitleAr = "حلول الدفع الإفريقية",
            url = "https://flutterwave.com",
            category = InternationalAppCategory.AFRICAN_ARAB_APPS,
            badgeText = "FW",
            accentColorHex = 0xFFF5A623
        ),
        InternationalAppItem(
            id = "paystack",
            name = "Paystack",
            subtitleAr = "بوابة المدفوعات للتجار",
            url = "https://paystack.com",
            category = InternationalAppCategory.AFRICAN_ARAB_APPS,
            badgeText = "PS",
            accentColorHex = 0xFF00C3F7
        ),
        InternationalAppItem(
            id = "noon",
            name = "Noon",
            subtitleAr = "التجارة السريعة في الشرق الأوسط",
            url = "https://www.noon.com",
            category = InternationalAppCategory.AFRICAN_ARAB_APPS,
            badgeText = "NN",
            accentColorHex = 0xFFFEEE00
        ),

        // السفر والخدمات الدولية
        InternationalAppItem(
            id = "booking",
            name = "Booking.com",
            subtitleAr = "فنادق ومنتجعات حول العالم",
            url = "https://www.booking.com",
            category = InternationalAppCategory.TRAVEL_SERVICES,
            badgeText = "BK",
            accentColorHex = 0xFF003580
        ),
        InternationalAppItem(
            id = "skyscanner",
            name = "Skyscanner",
            subtitleAr = "مقارنة أسعار الطيران",
            url = "https://www.skyscanner.net",
            category = InternationalAppCategory.TRAVEL_SERVICES,
            badgeText = "SK",
            accentColorHex = 0xFF0770E3
        ),
        InternationalAppItem(
            id = "airbnb",
            name = "Airbnb",
            subtitleAr = "إقامات وتجارب محلية مميزة",
            url = "https://www.airbnb.com",
            category = InternationalAppCategory.TRAVEL_SERVICES,
            badgeText = "AB",
            accentColorHex = 0xFFFF5A5F
        ),
        InternationalAppItem(
            id = "google_maps",
            name = "Google Maps",
            subtitleAr = "الخرائط والملاحة العالمية",
            url = "https://maps.google.com",
            category = InternationalAppCategory.TRAVEL_SERVICES,
            badgeText = "GM",
            accentColorHex = 0xFF34A853
        ),
        InternationalAppItem(
            id = "currency_converter",
            name = "محول العملات",
            subtitleAr = "أسعار الصرف الفورية (XE)",
            url = "https://www.xe.com/currencyconverter/",
            category = InternationalAppCategory.TRAVEL_SERVICES,
            badgeText = "FX",
            accentColorHex = 0xFFD4AF37
        )
    )
}
