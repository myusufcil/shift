package com.cil.shift.core.common.auth

/**
 * Result of social sign-in operation
 */
sealed class SocialSignInResult {
    data class Success(val idToken: String, val accessToken: String? = null, val rawNonce: String? = null) : SocialSignInResult()
    data class Error(val message: String) : SocialSignInResult()
    data object Cancelled : SocialSignInResult()
}

/**
 * Platform-specific social sign-in provider
 */
expect class SocialSignInProvider {
    /**
     * Sign in with Google
     * Returns the ID token and access token needed for Firebase authentication
     */
    suspend fun signInWithGoogle(): SocialSignInResult

    /**
     * Sign in with Apple
     * Returns the ID token needed for Firebase authentication
     */
    suspend fun signInWithApple(): SocialSignInResult
}
