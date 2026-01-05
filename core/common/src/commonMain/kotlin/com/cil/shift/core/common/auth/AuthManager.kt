package com.cil.shift.core.common.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages Firebase Authentication
 */
class AuthManager {
    private val auth: FirebaseAuth = Firebase.auth
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: AuthUser?
        get() = auth.currentUser?.toAuthUser()

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    init {
        // Initialize auth state
        _authState.value = if (auth.currentUser != null) {
            AuthState.Authenticated(auth.currentUser!!.toAuthUser())
        } else {
            AuthState.NotAuthenticated
        }

        // Listen to auth state changes
        observeAuthState()
    }

    private fun observeAuthState() {
        scope.launch {
            auth.authStateChanged.collect { firebaseUser ->
                _authState.value = if (firebaseUser != null) {
                    AuthState.Authenticated(firebaseUser.toAuthUser())
                } else {
                    AuthState.NotAuthenticated
                }
            }
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password)
            result.user?.let { user ->
                _authState.value = AuthState.Authenticated(user.toAuthUser())
                AuthResult.Success(user.toAuthUser())
            } ?: AuthResult.Error("Sign in failed")
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Invalid email or password"
                e.message?.contains("USER_NOT_FOUND") == true -> "No account found with this email"
                e.message?.contains("WRONG_PASSWORD") == true -> "Incorrect password"
                e.message?.contains("TOO_MANY_REQUESTS") == true -> "Too many attempts. Please try again later"
                e.message?.contains("INVALID_EMAIL") == true -> "Invalid email address"
                else -> e.message ?: "Sign in failed"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Create a new account with email and password
     */
    suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            result.user?.let { user ->
                _authState.value = AuthState.Authenticated(user.toAuthUser())
                AuthResult.Success(user.toAuthUser())
            } ?: AuthResult.Error("Sign up failed")
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("EMAIL_EXISTS") == true -> "An account with this email already exists"
                e.message?.contains("WEAK_PASSWORD") == true -> "Password should be at least 6 characters"
                e.message?.contains("INVALID_EMAIL") == true -> "Invalid email address"
                else -> e.message ?: "Sign up failed"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email)
            AuthResult.Success(AuthUser(
                uid = "",
                email = email,
                displayName = null,
                photoUrl = null,
                isEmailVerified = false,
                isAnonymous = false
            ))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("USER_NOT_FOUND") == true -> "No account found with this email"
                e.message?.contains("INVALID_EMAIL") == true -> "Invalid email address"
                else -> e.message ?: "Failed to send reset email"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Sign in with Google credential
     */
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val result = auth.signInWithCredential(credential)
            result.user?.let { user ->
                _authState.value = AuthState.Authenticated(user.toAuthUser())
                AuthResult.Success(user.toAuthUser())
            } ?: AuthResult.Error("Google Sign-In failed")
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("INVALID_CREDENTIAL") == true -> "Invalid Google credential"
                e.message?.contains("ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") == true ->
                    "An account already exists with this email using a different sign-in method"
                else -> e.message ?: "Google Sign-In failed"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Sign in with Apple credential
     */
    suspend fun signInWithApple(idToken: String): AuthResult {
        return try {
            val credential = OAuthProvider.credential("apple.com", idToken, null)
            val result = auth.signInWithCredential(credential)
            result.user?.let { user ->
                _authState.value = AuthState.Authenticated(user.toAuthUser())
                AuthResult.Success(user.toAuthUser())
            } ?: AuthResult.Error("Apple Sign-In failed")
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("INVALID_CREDENTIAL") == true -> "Invalid Apple credential"
                e.message?.contains("ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") == true ->
                    "An account already exists with this email using a different sign-in method"
                else -> e.message ?: "Apple Sign-In failed"
            }
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Sign out
     */
    suspend fun signOut() {
        try {
            auth.signOut()
            _authState.value = AuthState.NotAuthenticated
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Update user display name
     */
    suspend fun updateDisplayName(displayName: String): AuthResult {
        return try {
            auth.currentUser?.updateProfile(displayName = displayName)
            auth.currentUser?.let { user ->
                val updatedUser = user.toAuthUser()
                _authState.value = AuthState.Authenticated(updatedUser)
                AuthResult.Success(updatedUser)
            } ?: AuthResult.Error("User not found")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update profile")
        }
    }

    /**
     * Delete the current user account
     */
    suspend fun deleteAccount(): AuthResult {
        return try {
            auth.currentUser?.delete()
            _authState.value = AuthState.NotAuthenticated
            AuthResult.Success(AuthUser(
                uid = "",
                email = null,
                displayName = null,
                photoUrl = null,
                isEmailVerified = false,
                isAnonymous = false
            ))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("REQUIRES_RECENT_LOGIN") == true -> "Please sign in again before deleting your account"
                else -> e.message ?: "Failed to delete account"
            }
            AuthResult.Error(errorMessage)
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoURL,
            isEmailVerified = isEmailVerified,
            isAnonymous = isAnonymous
        )
    }
}
