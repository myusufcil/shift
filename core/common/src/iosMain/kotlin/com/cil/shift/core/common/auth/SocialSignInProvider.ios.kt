package com.cil.shift.core.common.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.*
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS implementation of SocialSignInProvider
 */
actual class SocialSignInProvider {
    private var appleSignInDelegate: AppleSignInDelegate? = null

    actual suspend fun signInWithGoogle(): SocialSignInResult {
        // Google Sign-In on iOS requires GoogleSignIn SDK
        // For now, we'll return a not-implemented message
        // Full implementation requires adding GoogleSignIn pod and Swift interop
        return SocialSignInResult.Error("Google Sign-In is not yet configured for iOS. Please use email login or Apple Sign-In.")
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun signInWithApple(): SocialSignInResult = suspendCancellableCoroutine { continuation ->
        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
        }

        appleSignInDelegate = AppleSignInDelegate { result ->
            continuation.resume(result)
        }

        val controller = ASAuthorizationController(authorizationRequests = listOf(request))
        controller.delegate = appleSignInDelegate
        controller.presentationContextProvider = appleSignInDelegate
        controller.performRequests()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class AppleSignInDelegate(
    private val onComplete: (SocialSignInResult) -> Unit
) : NSObject(), ASAuthorizationControllerDelegateProtocol, ASAuthorizationControllerPresentationContextProvidingProtocol {

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        val credential = didCompleteWithAuthorization.credential
        if (credential is ASAuthorizationAppleIDCredential) {
            val identityToken = credential.identityToken
            if (identityToken != null) {
                val tokenString = NSString.create(data = identityToken, encoding = NSUTF8StringEncoding)?.toString()
                if (tokenString != null) {
                    onComplete(SocialSignInResult.Success(idToken = tokenString))
                    return
                }
            }
            onComplete(SocialSignInResult.Error("Failed to get identity token"))
        } else {
            onComplete(SocialSignInResult.Error("Invalid credential type"))
        }
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: NSError
    ) {
        when (didCompleteWithError.code) {
            ASAuthorizationErrorCanceled -> onComplete(SocialSignInResult.Cancelled)
            ASAuthorizationErrorFailed -> onComplete(SocialSignInResult.Error("Sign in failed"))
            ASAuthorizationErrorInvalidResponse -> onComplete(SocialSignInResult.Error("Invalid response"))
            ASAuthorizationErrorNotHandled -> onComplete(SocialSignInResult.Error("Request not handled"))
            ASAuthorizationErrorUnknown -> onComplete(SocialSignInResult.Error("Unknown error"))
            else -> onComplete(SocialSignInResult.Error(didCompleteWithError.localizedDescription))
        }
    }

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor {
        return UIApplication.sharedApplication.keyWindow ?: UIWindow()
    }
}
