package com.cil.shift

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.cil.shift.core.common.auth.SocialSignInProvider
import com.cil.shift.core.common.haptic.initHapticFeedback
import com.cil.shift.di.initKoin
import com.cil.shift.update.InAppUpdateManager
import com.cil.shift.update.UpdateState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {
    private val socialSignInProvider: SocialSignInProvider by inject()
    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate() to disable launcher animation
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        initKoin {
            androidContext(this@MainActivity)
        }

        // Initialize in-app update manager
        inAppUpdateManager = InAppUpdateManager(this)

        // Initialize haptic feedback
        initHapticFeedback(this)

        // Set activity for social sign-in (Google Sign-In)
        socialSignInProvider.setActivity(this)

        setContent {
            val updateState by inAppUpdateManager.updateState.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                App()

                // Show update banner when update is downloaded
                if (updateState is UpdateState.Downloaded) {
                    UpdateBanner(
                        onInstall = { inAppUpdateManager.completeUpdate() },
                        onDismiss = { inAppUpdateManager.dismissUpdate() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Check for updates after initialization
        inAppUpdateManager.checkForUpdate(this)
    }

    override fun onResume() {
        super.onResume()
        // Check for pending updates when app resumes
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.checkPendingUpdate(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.cleanup()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == InAppUpdateManager.REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                // Update flow failed or was cancelled
                android.util.Log.w("MainActivity", "Update flow failed or cancelled: $resultCode")
            }
        }
    }
}

@Composable
private fun UpdateBanner(
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 80.dp), // Account for bottom nav
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E88E5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SystemUpdate,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Update Ready",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "A new version has been downloaded",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }

            TextButton(
                onClick = onInstall,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "INSTALL",
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
