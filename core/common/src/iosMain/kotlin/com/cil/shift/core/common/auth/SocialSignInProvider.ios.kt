package com.cil.shift.core.common.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.*
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.random.Random

/**
 * iOS implementation of SocialSignInProvider
 */
actual class SocialSignInProvider {
    private var appleSignInDelegate: AppleSignInDelegate? = null

    actual suspend fun signInWithGoogle(): SocialSignInResult {
        return SocialSignInResult.Error("Google Sign-In is not yet configured for iOS. Please use email login or Apple Sign-In.")
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun signInWithApple(): SocialSignInResult = suspendCancellableCoroutine { continuation ->
        val rawNonce = randomNonceString()
        val hashedNonce = sha256(rawNonce)

        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            nonce = hashedNonce
        }

        appleSignInDelegate = AppleSignInDelegate(rawNonce) { result ->
            continuation.resume(result)
        }

        val controller = ASAuthorizationController(authorizationRequests = listOf(request))
        controller.delegate = appleSignInDelegate
        controller.presentationContextProvider = appleSignInDelegate
        controller.performRequests()
    }
}

private fun randomNonceString(length: Int = 32): String {
    val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"
    return (1..length).map { charset[Random.nextInt(charset.length)] }.joinToString("")
}

/**
 * SHA-256 using iOS native CommonCrypto (CC_SHA256)
 */
@OptIn(ExperimentalForeignApi::class)
private fun sha256(input: String): String {
    val data = input.encodeToByteArray()
    val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
    data.usePinned { pinnedData ->
        digest.usePinned { pinnedDigest ->
            CC_SHA256(pinnedData.addressOf(0), data.size.toUInt(), pinnedDigest.addressOf(0))
        }
    }
    return digest.joinToString("") { it.toString(16).padStart(2, '0') }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class AppleSignInDelegate(
    private val rawNonce: String,
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
                    onComplete(SocialSignInResult.Success(idToken = tokenString, rawNonce = rawNonce))
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
        for (scene in UIApplication.sharedApplication.connectedScenes) {
            if (scene is UIWindowScene) {
                for (window in scene.windows) {
                    if (window is UIWindow && window.isKeyWindow()) {
                        return window
                    }
                }
            }
        }
        @Suppress("DEPRECATION")
        return UIApplication.sharedApplication.keyWindow ?: UIWindow()
    }
}
