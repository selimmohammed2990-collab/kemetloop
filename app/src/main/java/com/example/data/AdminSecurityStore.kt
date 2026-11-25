package com.example.data

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Manages the Developer Admin Panel secret passcode securely on-device using a salted SHA-256 hash.
 * Supports first-run passcode setup (no hardcoded secrets in source code).
 */
class AdminSecurityStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("kemetloop_admin_sec", Context.MODE_PRIVATE)

    fun hasPinConfigured(): Boolean {
        return !prefs.getString(KEY_PIN_HASH, null).isNullOrBlank() &&
            !prefs.getString(KEY_PIN_SALT, null).isNullOrBlank()
    }

    fun setupPin(pin: String): Result<Unit> {
        val clean = pin.trim()
        if (clean.length < 4 || clean.length > 12) {
            return Result.failure(IllegalArgumentException("الرمز السري يجب أن يتكون من 4 إلى 12 خانة."))
        }
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hashBase64 = hashPin(clean, saltBytes)
        prefs.edit()
            .putString(KEY_PIN_SALT, saltBase64)
            .putString(KEY_PIN_HASH, hashBase64)
            .apply()
        return Result.success(Unit)
    }

    fun verifyPin(pin: String): Boolean {
        val saltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val expectedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val saltBytes = runCatching { Base64.decode(saltBase64, Base64.NO_WRAP) }.getOrNull() ?: return false
        val candidateHash = hashPin(pin.trim(), saltBytes)
        return MessageDigest.isEqual(
            candidateHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun resetPin() {
        prefs.edit().remove(KEY_PIN_SALT).remove(KEY_PIN_HASH).apply()
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val digest = md.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private companion object {
        const val KEY_PIN_SALT = "admin_pin_salt"
        const val KEY_PIN_HASH = "admin_pin_hash"
    }
}
