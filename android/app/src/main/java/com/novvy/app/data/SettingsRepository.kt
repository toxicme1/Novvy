package com.novvy.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.novvy.app.domain.model.ProviderConfig
import com.novvy.app.domain.model.ProviderKind
import com.novvy.app.domain.provider.Providers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val onboardingDone: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val displayName: String = "Abel",
    val gmailConnected: Boolean = false,
    val driveConnected: Boolean = false,
    val providers: List<ProviderConfig> = Providers.defaultConfigs(),
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "novvy_settings")

class SettingsRepository(
    private val context: Context,
    private val secureKeyStore: SecureKeyStore,
) {
    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
        val THEME = stringPreferencesKey("theme_mode")
        val NAME = stringPreferencesKey("display_name")
        val GMAIL = booleanPreferencesKey("gmail_connected")
        val DRIVE = booleanPreferencesKey("drive_connected")
        val PROVIDERS = stringPreferencesKey("providers_json")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            onboardingDone = p[Keys.ONBOARDING] ?: false,
            themeMode = runCatching { ThemeMode.valueOf(p[Keys.THEME] ?: "SYSTEM") }.getOrDefault(ThemeMode.SYSTEM),
            displayName = p[Keys.NAME] ?: "Abel",
            gmailConnected = p[Keys.GMAIL] ?: false,
            driveConnected = p[Keys.DRIVE] ?: false,
            providers = decodeProviders(p[Keys.PROVIDERS]).let { mergeWithKeys(it) },
        )
    }

    suspend fun setOnboardingDone(done: Boolean) = context.dataStore.edit { it[Keys.ONBOARDING] = done }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }

    suspend fun setDisplayName(name: String) = context.dataStore.edit { it[Keys.NAME] = name }

    suspend fun setGmailConnected(connected: Boolean) = context.dataStore.edit { it[Keys.GMAIL] = connected }

    suspend fun setDriveConnected(connected: Boolean) = context.dataStore.edit { it[Keys.DRIVE] = connected }

    suspend fun saveProviders(list: List<ProviderConfig>) = context.dataStore.edit {
        it[Keys.PROVIDERS] = encodeProviders(list)
    }

    suspend fun saveProviderKey(providerId: String, key: String) {
        secureKeyStore.setKey(providerId, key)
    }

    // ---- helpers ------------------------------------------------------

    private fun mergeWithKeys(list: List<ProviderConfig>): List<ProviderConfig> =
        list.map { it.copy(hasKey = secureKeyStore.hasKey(it.kind.name)) }

    private fun encodeProviders(list: List<ProviderConfig>): String {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(JSONObject().apply {
                put("kind", c.kind.name)
                put("model", c.model)
                put("enabled", c.enabled)
                put("isDefault", c.isDefault)
                put("isFallback", c.isFallback)
                put("privateOnly", c.privateOnly)
            })
        }
        return arr.toString()
    }

    private fun decodeProviders(json: String?): List<ProviderConfig> {
        if (json.isNullOrBlank()) return Providers.defaultConfigs()
        return runCatching {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        ProviderConfig(
                            kind = runCatching { ProviderKind.valueOf(o.getString("kind")) }
                                .getOrDefault(ProviderKind.MOCK),
                            model = o.optString("model"),
                            enabled = o.optBoolean("enabled", true),
                            isDefault = o.optBoolean("isDefault", false),
                            isFallback = o.optBoolean("isFallback", false),
                            privateOnly = o.optBoolean("privateOnly", false),
                            hasKey = false,
                        )
                    )
                }
            }
        }.getOrDefault(Providers.defaultConfigs())
    }
}