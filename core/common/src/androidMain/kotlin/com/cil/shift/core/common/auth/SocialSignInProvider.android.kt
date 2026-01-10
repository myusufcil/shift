package com.cil.shift.core.common.auth

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

/**
 * Android implementation of SocialSignInProvider using legacy Google Sign-In API
 */
actual class SocialSignInProvider(
    private val context: Context,
    private val webClientId: String
) {
    private var activityRef: WeakReference<ComponentActivity>? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var signInLauncher: ActivityResultLauncher<Intent>? = null
    private var pendingCallback: ((SocialSignInResult) -> Unit)? = null

    /**
     * Set the current activity and register for activity result
     */
    fun setActivity(activity: ComponentActivity) {
        // Clean up previous launcher
        signInLauncher?.unregister()

        activityRef = WeakReference(activity)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)

        // Register launcher directly using activity result registry
        signInLauncher = activity.activityResultRegistry.register(
            "google_sign_in_${System.currentTimeMillis()}",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleSignInResult(result.data)
        }
    }

    private fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                pendingCallback?.invoke(SocialSignInResult.Success(idToken = idToken, accessToken = null))
            } else {
                pendingCallback?.invoke(SocialSignInResult.Error("Failed to get ID token"))
            }
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                12501 -> "Sign-in cancelled"
                12502 -> "Sign-in failed. Please try again."
                10 -> "Developer error: Check SHA-1 and Web Client ID configuration"
                else -> "Google Sign-In failed: ${e.statusCode} - ${e.message}"
            }
            if (e.statusCode == 12501) {
                pendingCallback?.invoke(SocialSignInResult.Cancelled)
            } else {
                pendingCallback?.invoke(SocialSignInResult.Error(message))
            }
        } catch (e: Exception) {
            pendingCallback?.invoke(SocialSignInResult.Error("Sign-in error: ${e.message}"))
        } finally {
            pendingCallback = null
        }
    }

    actual suspend fun signInWithGoogle(): SocialSignInResult {
        val launcher = signInLauncher
            ?: return SocialSignInResult.Error("Activity not initialized. Please restart the app.")

        val client = googleSignInClient
            ?: return SocialSignInResult.Error("Google Sign-In not configured.")

        return suspendCancellableCoroutine { continuation ->
            pendingCallback = { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }

            continuation.invokeOnCancellation {
                pendingCallback = null
            }

            // Sign out first to always show account picker
            client.signOut().addOnCompleteListener {
                launcher.launch(client.signInIntent)
            }
        }
    }

    actual suspend fun signInWithApple(): SocialSignInResult {
        return SocialSignInResult.Error("Apple Sign-In is only available on iOS devices")
    }
}
