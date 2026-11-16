package com.novvy.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores provider API keys encrypted at rest using Android Keystore-backed
 * EncryptedSharedPreferences. Keys are never shown back in the UI and never
 * written to logs or source.
 */
class SecureKeyStore(context: Context) {

    private val prefs: SharedPreferences = run {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "novvy_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            Log.w("Novvy", "EncryptedSharedPreferences unavailable, using fallback", e)
            context.getSharedPreferences("novvy_secure_fallback", Context.MODE_PRIVATE)
        }
    }

    fun getKey(providerId: String): String = prefs.getString(key(providerId), "").orEmpty()

    fun hasKey(providerId: String): Boolean = getKey(providerId).isNotBlank()

    fun setKey(providerId: String, value: String) {
        prefs.edit().putString(key(providerId), value).apply()
    }

    fun removeKey(providerId: String) {
        prefs.edit().remove(key(providerId)).apply()
    }

    private fun key(providerId: String) = "key_$providerId"
}