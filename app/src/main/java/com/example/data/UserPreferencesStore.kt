package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.ceil

data class UserAppSettings(
    val autoSpeakAssistant: Boolean = true,
    val offlineFirstMode: Boolean = false
)

data class RewardsState(
    val welcomeGiftClaimed: Boolean = false,
    val proTrialExpiresAtMillis: Long = 0L,
    val dailyStreakCount: Int = 0,
    val lastDailyClaimEpochDay: Long = -1L
) {
    val proTrialActive: Boolean
        get() = proTrialExpiresAtMillis > System.currentTimeMillis()

    val proTrialDaysRemaining: Int
        get() {
            val remaining = proTrialExpiresAtMillis - System.currentTimeMillis()
            if (remaining <= 0L) return 0
            return ceil(remaining / 86_400_000.0).toInt().coerceIn(1, 3)
        }

    val currentEpochDay: Long
        get() = System.currentTimeMillis() / 86_400_000L

    val canClaimTodayGift: Boolean
        get() = lastDailyClaimEpochDay < currentEpochDay

    val nextClaimDayNumber: Int
        get() = if (dailyStreakCount >= 7) 1 else dailyStreakCount + 1
}

/**
 * Per-user local preferences for favorite landmarks, app settings, welcome gift + 3-day Pro trial,
 * and 7-day daily reward streak, shared smoothly across Guest Mode and Google-authenticated accounts.
 */
class UserPreferencesStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("kemetloop_user_prefs", Context.MODE_PRIVATE)

    private val _favoriteLandmarkIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteLandmarkIds: StateFlow<Set<String>> = _favoriteLandmarkIds.asStateFlow()

    private val _appSettings = MutableStateFlow(loadSettings())
    val appSettings: StateFlow<UserAppSettings> = _appSettings.asStateFlow()

    private val _rewardsState = MutableStateFlow(RewardsState())
    val rewardsState: StateFlow<RewardsState> = _rewardsState.asStateFlow()

    private var activeUserId: String = "local_guest"

    fun bindUser(userId: String) {
        activeUserId = userId.ifBlank { "local_guest" }
        val saved = prefs.getStringSet(favKey(activeUserId), null)
        if (saved == null) {
            val defaults = setOf("guide_giza", "guide_luxor")
            prefs.edit().putStringSet(favKey(activeUserId), defaults).apply()
            _favoriteLandmarkIds.value = defaults
        } else {
            _favoriteLandmarkIds.value = saved.toSet()
        }
        _appSettings.value = loadSettings()
        _rewardsState.value = loadRewards(activeUserId)
    }

    fun toggleFavoriteLandmark(landmarkId: String): Boolean {
        val current = _favoriteLandmarkIds.value.toMutableSet()
        val added = if (landmarkId in current) {
            current.remove(landmarkId)
            false
        } else {
            current.add(landmarkId)
            true
        }
        prefs.edit().putStringSet(favKey(activeUserId), current).apply()
        _favoriteLandmarkIds.value = current
        return added
    }

    fun setAutoSpeakAssistant(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
        _appSettings.value = _appSettings.value.copy(autoSpeakAssistant = enabled)
    }

    fun setOfflineFirstMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OFFLINE_FIRST, enabled).apply()
        _appSettings.value = _appSettings.value.copy(offlineFirstMode = enabled)
    }

    fun claimWelcomeGift(): Boolean {
        val current = loadRewards(activeUserId)
        if (current.welcomeGiftClaimed) return false
        val trialExpiry = System.currentTimeMillis() + (3L * 86_400_000L)
        prefs.edit()
            .putBoolean(welcomeGiftKey(activeUserId), true)
            .putLong(proTrialKey(activeUserId), trialExpiry)
            .apply()
        _rewardsState.value = current.copy(
            welcomeGiftClaimed = true,
            proTrialExpiresAtMillis = trialExpiry
        )
        return true
    }

    /**
     * Advances the 7-day daily gift streak and returns the claimed day (1..7) and coin reward,
     * or null if today's gift was already claimed.
     */
    fun claimDailyGift(): Pair<Int, Long>? {
        val current = loadRewards(activeUserId)
        if (!current.canClaimTodayGift) return null
        val today = current.currentEpochDay
        val consecutive = current.lastDailyClaimEpochDay == today - 1L || current.lastDailyClaimEpochDay < 0L
        val nextDay = when {
            !consecutive -> 1
            current.dailyStreakCount >= 7 -> 1
            else -> current.dailyStreakCount + 1
        }
        val rewardCoins = DAILY_REWARD_COINS[(nextDay - 1).coerceIn(0, DAILY_REWARD_COINS.lastIndex)]
        prefs.edit()
            .putInt(streakCountKey(activeUserId), nextDay)
            .putLong(streakLastDayKey(activeUserId), today)
            .apply()
        _rewardsState.value = current.copy(
            dailyStreakCount = nextDay,
            lastDailyClaimEpochDay = today
        )
        return nextDay to rewardCoins
    }

    private fun loadSettings(): UserAppSettings = UserAppSettings(
        autoSpeakAssistant = prefs.getBoolean(KEY_AUTO_SPEAK, true),
        offlineFirstMode = prefs.getBoolean(KEY_OFFLINE_FIRST, false)
    )

    private fun loadRewards(userId: String): RewardsState = RewardsState(
        welcomeGiftClaimed = prefs.getBoolean(welcomeGiftKey(userId), false),
        proTrialExpiresAtMillis = prefs.getLong(proTrialKey(userId), 0L),
        dailyStreakCount = prefs.getInt(streakCountKey(userId), 0),
        lastDailyClaimEpochDay = prefs.getLong(streakLastDayKey(userId), -1L)
    )

    private fun favKey(userId: String) = "fav_landmarks_$userId"
    private fun welcomeGiftKey(userId: String) = "welcome_gift_claimed_$userId"
    private fun proTrialKey(userId: String) = "pro_trial_expiry_$userId"
    private fun streakCountKey(userId: String) = "daily_streak_count_$userId"
    private fun streakLastDayKey(userId: String) = "daily_streak_last_day_$userId"

    companion object {
        val DAILY_REWARD_COINS = listOf(15L, 20L, 25L, 30L, 40L, 50L, 100L)
        private const val KEY_AUTO_SPEAK = "pref_auto_speak_assistant"
        private const val KEY_OFFLINE_FIRST = "pref_offline_first"
    }
}
