package com.cil.shift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cil.shift.core.common.auth.SocialSignInProvider
import com.cil.shift.core.common.haptic.initHapticFeedback
import com.cil.shift.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {
    private val socialSignInProvider: SocialSignInProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate() to disable launcher animation
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        initKoin {
            androidContext(this@MainActivity)
        }

        // Initialize haptic feedback
        initHapticFeedback(this)

        // Set activity for social sign-in (Google Sign-In)
        socialSignInProvider.setActivity(this)

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        // Update activity reference when resumed (in case it was recreated)
        socialSignInProvider.setActivity(this)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
