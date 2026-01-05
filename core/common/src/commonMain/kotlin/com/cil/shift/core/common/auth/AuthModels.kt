package com.cil.shift.core.common.auth

/**
 * Represents an authenticated user
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val isAnonymous: Boolean
)

/**
 * Authentication state
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Result of authentication operations
 */
sealed class AuthResult {
    data class Success(val user: AuthUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
