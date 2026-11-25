package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Safely validates URLs and executes Android intents (Web, Dial, Maps, WhatsApp, Share)
 * without crashing if a target app is missing or a URI is malformed.
 */
object SafeIntentHelper {

    fun normalizeWebUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return null
        val lower = trimmed.lowercase()
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("file:")) {
            return null
        }
        return if (lower.startsWith("http://") || lower.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    fun openExternalBrowser(context: Context, rawUrl: String, onError: (String) -> Unit) {
        val url = normalizeWebUrl(rawUrl)
        if (url == null) {
            onError("الرابط غير صالح أو غير آمن للفتح.")
            return
        }
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            onError("لا يوجد متصفح متاح لفتح هذا الرابط حالياً.")
        }
    }

    fun openWhatsApp(context: Context, phoneOrUrl: String, onError: (String) -> Unit) {
        val trimmed = phoneOrUrl.trim()
        val url = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            val digits = trimmed.filter { it.isDigit() }
            if (digits.isBlank()) {
                onError("رقم الواتساب غير صالح.")
                return
            }
            "https://wa.me/$digits"
        }
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            onError("تعذر فتح تطبيق واتساب أو المتصفح.")
        }
    }

    fun dialPhone(context: Context, number: String, onError: (String) -> Unit) {
        val clean = number.filter { it.isDigit() || it == '+' }
        if (clean.isBlank()) {
            onError("رقم الهاتف غير صالح.")
            return
        }
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")))
        }.onFailure {
            onError("تعذر فتح تطبيق الاتصال على هذا الجهاز.")
        }
    }

    fun openNearbyMapSearch(
        context: Context,
        query: String,
        latitude: Double?,
        longitude: Double?,
        onError: (String) -> Unit
    ) {
        val encoded = Uri.encode(query)
        val geoUri = if (latitude != null && longitude != null) {
            Uri.parse("geo:$latitude,$longitude?q=$encoded")
        } else {
            Uri.parse("geo:0,0?q=$encoded+Egypt")
        }
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        val launched = runCatching { context.startActivity(intent) }.isSuccess
        if (!launched) {
            val webUrl = if (latitude != null && longitude != null) {
                "https://www.google.com/maps/search/$encoded/@$latitude,$longitude,14z"
            } else {
                "https://www.google.com/maps/search/$encoded+Egypt"
            }
            openExternalBrowser(context, webUrl, onError)
        }
    }

    fun shareInviteWithReferral(
        context: Context,
        referralCode: String,
        customHeadline: String? = null,
        onError: (String) -> Unit
    ) {
        val text = buildString {
            if (!customHeadline.isNullOrBlank()) {
                appendLine(customHeadline)
            }
            appendLine("اكتشف تطبيق KemetLoop للسياحة الذكية والتجارة والمترجم الفوري!")
            append("سجّل الآن بكود الدعوة ($referralCode) واحصل على 100 عملة ترحيبية مجاناً.")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "مشاركة دعوة KemetLoop"))
        }.onFailure {
            onError("تعذر فتح قائمة المشاركة على هذا الجهاز.")
        }
    }

    fun openVaultUriWithSystem(
        context: Context,
        uri: Uri,
        mimeType: String,
        onError: (String) -> Unit
    ) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType.ifBlank { "*/*" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "فتح الملف"))
        }.onFailure {
            onError("لا يوجد تطبيق مثبت على الجهاز يدعم فتح هذا النوع من الملفات.")
        }
    }

    fun shareVaultUri(
        context: Context,
        uri: Uri,
        mimeType: String,
        title: String,
        onError: (String) -> Unit
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType.ifBlank { "*/*" }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "مشاركة: $title"))
        }.onFailure {
            onError("تعذر فتح قائمة المشاركة لهذا الملف.")
        }
    }
}
