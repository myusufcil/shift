package com.cil.shift.core.common.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.*
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
 * Pure Kotlin SHA-256 implementation
 */
private fun sha256(input: String): String {
    val bytes = input.encodeToByteArray()

    val k = intArrayOf(
        0x428a2f98, 0x71374491, -0x4a3f0431, -0x164a245b,
        0x3956c25b, 0x59f111f1, -0x6dc07d5c, -0x54e3a12b,
        -0x27f85568, 0x12835b01, 0x243185be, 0x550c7dc3,
        0x72be5d74, -0x7f214e02, -0x6423f959, -0x3e640e8c,
        -0x1b64963f, -0x1041b87a, 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        -0x67c1aeae, -0x57ce3993, -0x4ffcd838, -0x40a68039,
        -0x391ff40d, -0x2a586eb9, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, -0x7e3d36d2, -0x6d8dd37b,
        -0x5d40175f, -0x57e599b5, -0x3db47490, -0x3893ae5d,
        -0x2e6d17e7, -0x2966f9dc, -0x0bf1ca7b, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
        0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, -0x7b3787ec, -0x7338fdf8,
        -0x6f410006, -0x5baf9315, -0x41065c09, -0x398e870e
    )

    var h0 = 0x6a09e667
    var h1 = -0x44a93365
    var h2 = 0x3c6ef372
    var h3 = -0x5ab00ac6
    var h4 = 0x510e527f
    var h5 = -0x64fa9774
    var h6 = 0x1f83d9ab
    var h7 = 0x5be0cd19

    val originalLength = bytes.size
    val bitLength = originalLength.toLong() * 8

    var paddedLength = originalLength + 1
    while (paddedLength % 64 != 56) paddedLength++
    paddedLength += 8

    val padded = ByteArray(paddedLength)
    bytes.copyInto(padded)
    padded[originalLength] = 0x80.toByte()

    for (i in 0..7) {
        padded[paddedLength - 8 + i] = (bitLength shr (56 - i * 8)).toByte()
    }

    for (chunkStart in 0 until paddedLength step 64) {
        val w = IntArray(64)
        for (i in 0..15) {
            w[i] = ((padded[chunkStart + i * 4].toInt() and 0xFF) shl 24) or
                    ((padded[chunkStart + i * 4 + 1].toInt() and 0xFF) shl 16) or
                    ((padded[chunkStart + i * 4 + 2].toInt() and 0xFF) shl 8) or
                    (padded[chunkStart + i * 4 + 3].toInt() and 0xFF)
        }
        for (i in 16..63) {
            val s0 = ror(w[i - 15], 7) xor ror(w[i - 15], 18) xor (w[i - 15] ushr 3)
            val s1 = ror(w[i - 2], 17) xor ror(w[i - 2], 19) xor (w[i - 2] ushr 10)
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }

        var a = h0; var b = h1; var c = h2; var d = h3
        var e = h4; var f = h5; var g = h6; var h = h7

        for (i in 0..63) {
            val s1 = ror(e, 6) xor ror(e, 11) xor ror(e, 25)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = h + s1 + ch + k[i] + w[i]
            val s0 = ror(a, 2) xor ror(a, 13) xor ror(a, 22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = s0 + maj

            h = g; g = f; f = e; e = d + temp1
            d = c; c = b; b = a; a = temp1 + temp2
        }

        h0 += a; h1 += b; h2 += c; h3 += d
        h4 += e; h5 += f; h6 += g; h7 += h
    }

    return intArrayOf(h0, h1, h2, h3, h4, h5, h6, h7)
        .joinToString("") {
            (it.toLong() and 0xFFFFFFFFL).toString(16).padStart(8, '0')
        }
}

private fun ror(value: Int, n: Int): Int = (value ushr n) or (value shl (32 - n))

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
