package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.model.KemetCatalogDefaults
import com.example.model.LinkCategory
import com.example.model.SavedLink
import com.example.model.TranslationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local offline cache + live ConnectivityManager monitor so Dream Box links,
 * top translated phrases, and the Egypt Travel Guide remain instantly usable offline.
 */
class OfflineCacheStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("kemetloop_offline_cache", Context.MODE_PRIVATE)
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(checkCurrentlyOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _cachedLinks = MutableStateFlow(loadCachedLinks())
    val cachedLinks: StateFlow<List<SavedLink>> = _cachedLinks.asStateFlow()

    private val _cachedTranslations = MutableStateFlow(loadCachedTranslations())
    val cachedTranslations: StateFlow<Map<String, TranslationResult>> = _cachedTranslations.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun checkCurrentlyOnline(): Boolean {
        val cm = connectivityManager ?: return true
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkCallback() {
        val cm = connectivityManager ?: return
        runCatching {
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkCurrentlyOnline()
                }
            })
        }
    }

    fun saveLinksSnapshot(userId: String, links: List<SavedLink>) {
        if (links.isEmpty()) return
        val arr = JSONArray()
        links.forEach { link ->
            arr.put(
                JSONObject().apply {
                    put("id", link.id)
                    put("userId", userId)
                    put("title", link.title)
                    put("url", link.url)
                    put("category", link.category)
                }
            )
        }
        prefs.edit().putString(KEY_DREAM_LINKS, arr.toString()).apply()
        _cachedLinks.value = links
    }

    private fun loadCachedLinks(): List<SavedLink> {
        val raw = prefs.getString(KEY_DREAM_LINKS, null)
        if (raw.isNullOrBlank()) {
            return listOf(
                SavedLink(
                    id = "offline_link_1",
                    userId = "offline",
                    title = "صندوق الأحلام · دليل رحلات النيل",
                    url = "https://kemetloop.app/nile-trips",
                    category = LinkCategory.TRAVEL.name
                ),
                SavedLink(
                    id = "offline_link_2",
                    userId = "offline",
                    title = "صندوق الأحلام · بازار خان الخليلي",
                    url = "https://kemetloop.app/khan-el-khalili",
                    category = LinkCategory.SHOP.name
                )
            )
        }
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SavedLink(
                    id = o.optString("id", "offline_$i"),
                    userId = o.optString("userId", "offline"),
                    title = o.optString("title", ""),
                    url = o.optString("url", ""),
                    category = o.optString("category", LinkCategory.OTHER.name)
                )
            }
        }.getOrElse { emptyList() }
    }

    fun cacheTranslation(
        sourceText: String,
        sourceCode: String,
        targetCode: String,
        result: TranslationResult
    ) {
        val key = buildCacheKey(sourceText, sourceCode, targetCode)
        val updated = _cachedTranslations.value.toMutableMap()
        updated[key] = result.copy(fromOfflineCache = true)
        // Cap cache at 60 recent entries
        val trimmed = if (updated.size > 60) {
            updated.entries.toList().takeLast(60).associate { it.key to it.value }
        } else {
            updated
        }
        _cachedTranslations.value = trimmed
        persistTranslations(trimmed)
    }

    fun lookupOfflineTranslation(
        inputText: String,
        sourceCode: String,
        targetCode: String
    ): TranslationResult? {
        val key = buildCacheKey(inputText, sourceCode, targetCode)
        _cachedTranslations.value[key]?.let { return it }

        val clean = inputText.trim()
        val matchedPhrase = KemetCatalogDefaults.offlineTopPhrases.firstOrNull { entry ->
            entry.arabic.equals(clean, ignoreCase = true) ||
                entry.english.equals(clean, ignoreCase = true) ||
                entry.arabic.contains(clean, ignoreCase = true) ||
                clean.contains(entry.arabic, ignoreCase = true)
        } ?: return null

        return TranslationResult(
            translatedText = matchedPhrase.textForCode(targetCode),
            pronunciationGuide = matchedPhrase.pronunciationHint,
            culturalTip = "تمت الترجمة من القاموس المحلي المحفوظ بدون إنترنت.",
            fromOfflineCache = true
        )
    }

    private fun buildCacheKey(text: String, src: String, tgt: String): String =
        "${src}_${tgt}_${text.trim().lowercase()}"

    private fun persistTranslations(map: Map<String, TranslationResult>) {
        val arr = JSONArray()
        map.forEach { (k, v) ->
            arr.put(
                JSONObject().apply {
                    put("key", k)
                    put("translatedText", v.translatedText)
                    put("pronunciationGuide", v.pronunciationGuide)
                    put("culturalTip", v.culturalTip)
                }
            )
        }
        prefs.edit().putString(KEY_TRANSLATIONS, arr.toString()).apply()
    }

    private fun loadCachedTranslations(): Map<String, TranslationResult> {
        val raw = prefs.getString(KEY_TRANSLATIONS, null) ?: return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            val map = mutableMapOf<String, TranslationResult>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val key = o.optString("key")
                if (key.isNotBlank()) {
                    map[key] = TranslationResult(
                        translatedText = o.optString("translatedText"),
                        pronunciationGuide = o.optString("pronunciationGuide"),
                        culturalTip = o.optString("culturalTip"),
                        fromOfflineCache = true
                    )
                }
            }
            map
        }.getOrElse { emptyMap() }
    }

    companion object {
        private const val KEY_DREAM_LINKS = "dream_links_json"
        private const val KEY_TRANSLATIONS = "cached_translations_json"
    }
}
