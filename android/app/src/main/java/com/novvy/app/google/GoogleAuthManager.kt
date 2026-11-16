package com.novvy.app.google

/** A signed-in Google account (scaffold). */
data class GoogleAccount(val email: String, val displayName: String)

/**
 * Scaffold for Google Sign-In (Credential Manager).
 *
 * Real implementation will use `com.google.android.gms:play-services-auth` /
 * Credential Manager + the web client ID in [GoogleCredentials]. Kept
 * dependency-free here so the app builds and runs today; the UI already has a
 * full connect/disconnect flow wired to [GoogleCredentials.isConfigured].
 */
class GoogleAuthManager {

    fun isConfigured(): Boolean = GoogleCredentials.isConfigured

    /** Returns the account once a real sign-in flow is wired in. */
    suspend fun signIn(email: String? = null, name: String? = null): Result<GoogleAccount> {
        if (!isConfigured()) {
            return Result.failure(
                IllegalStateException(
                    "Google sign-in isn't configured yet. Add GOOGLE_WEB_CLIENT_ID to " +
                        "android/local.properties (see google/GoogleCredentials.kt)."
                )
            )
        }
        return Result.success(GoogleAccount(email ?: "user@example.com", name ?: "User"))
    }

    suspend fun signOut() = Unit
}