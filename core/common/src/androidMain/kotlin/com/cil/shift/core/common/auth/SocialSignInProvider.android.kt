package com.cil.shift.core.common.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.lang.ref.WeakReference

/**
 * Android implementation of SocialSignInProvider
 */
actual class SocialSignInProvider(
    private val context: Context,
    private val webClientId: String
) {
    private val credentialManager = CredentialManager.create(context)
    private var activityRef: WeakReference<Activity>? = null

    /**
     * Set the current activity for credential manager operations
     * This must be called before signInWithGoogle
     */
    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    actual suspend fun signInWithGoogle(): SocialSignInResult {
        val activity = activityRef?.get()
            ?: return SocialSignInResult.Error("Activity not available. Please try again.")

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = activity
            )

            handleGoogleSignInResult(result)
        } catch (e: GetCredentialCancellationException) {
            SocialSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            SocialSignInResult.Error("No Google account found. Please add a Google account to your device.")
        } catch (e: GetCredentialException) {
            SocialSignInResult.Error(e.message ?: "Google Sign-In failed")
        } catch (e: Exception) {
            SocialSignInResult.Error(e.message ?: "Unknown error during Google Sign-In")
        }
    }

    private fun handleGoogleSignInResult(result: GetCredentialResponse): SocialSignInResult {
        return when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        SocialSignInResult.Success(
                            idToken = googleIdTokenCredential.idToken,
                            accessToken = null
                        )
                    } catch (e: Exception) {
                        SocialSignInResult.Error("Failed to parse Google credential: ${e.message}")
                    }
                } else {
                    SocialSignInResult.Error("Unexpected credential type")
                }
            }
            else -> SocialSignInResult.Error("Unexpected credential type")
        }
    }

    actual suspend fun signInWithApple(): SocialSignInResult {
        // Apple Sign-In on Android requires using Firebase's native OAuth provider
        // This is handled differently - typically through Firebase console OAuth setup
        return SocialSignInResult.Error("Apple Sign-In is only available on iOS devices")
    }
}
