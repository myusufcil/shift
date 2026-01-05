package com.cil.shift.feature.settings.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.AuthResult
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.purchase.PurchaseManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authManager = koinInject<AuthManager>()
    val purchaseManager = koinInject<PurchaseManager>()
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    val strings = remember(currentLanguage) { LoginStrings.get(currentLanguage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo/Title
            Text(
                text = "Shift",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E7CFF)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.welcomeBack,
                fontSize = 16.sp,
                color = textColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text(strings.email) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4E7CFF),
                    unfocusedBorderColor = textColor.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text(strings.password) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = textColor.copy(alpha = 0.6f)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4E7CFF),
                    unfocusedBorderColor = textColor.copy(alpha = 0.2f)
                )
            )

            // Forgot password
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = strings.forgotPassword,
                    fontSize = 13.sp,
                    color = Color(0xFF4E7CFF),
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    fontSize = 13.sp,
                    color = Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign in button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = strings.fillAllFields
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        when (val result = authManager.signInWithEmail(email, password)) {
                            is AuthResult.Success -> {
                                // Login to RevenueCat with Firebase UID for cross-platform sync
                                purchaseManager.login(result.user.uid)
                                onLoginSuccess()
                            }
                            is AuthResult.Error -> {
                                errorMessage = result.message
                            }
                        }

                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4E7CFF)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = strings.signIn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with "or"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = textColor.copy(alpha = 0.2f)
                )
                Text(
                    text = strings.or,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = textColor.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign up prompt
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = strings.noAccount,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.signUp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4E7CFF),
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}

private data class LoginStrings(
    val welcomeBack: String,
    val email: String,
    val password: String,
    val forgotPassword: String,
    val signIn: String,
    val or: String,
    val noAccount: String,
    val signUp: String,
    val fillAllFields: String
) {
    companion object {
        fun get(language: Language): LoginStrings {
            return when (language) {
                Language.TURKISH -> LoginStrings(
                    welcomeBack = "Tekrar hoş geldiniz!",
                    email = "E-posta",
                    password = "Şifre",
                    forgotPassword = "Şifremi unuttum",
                    signIn = "Giriş Yap",
                    or = "veya",
                    noAccount = "Hesabınız yok mu?",
                    signUp = "Kayıt Ol",
                    fillAllFields = "Lütfen tüm alanları doldurun"
                )
                Language.SPANISH -> LoginStrings(
                    welcomeBack = "¡Bienvenido de nuevo!",
                    email = "Correo electrónico",
                    password = "Contraseña",
                    forgotPassword = "Olvidé mi contraseña",
                    signIn = "Iniciar Sesión",
                    or = "o",
                    noAccount = "¿No tienes cuenta?",
                    signUp = "Regístrate",
                    fillAllFields = "Por favor completa todos los campos"
                )
                else -> LoginStrings(
                    welcomeBack = "Welcome back!",
                    email = "Email",
                    password = "Password",
                    forgotPassword = "Forgot password?",
                    signIn = "Sign In",
                    or = "or",
                    noAccount = "Don't have an account?",
                    signUp = "Sign Up",
                    fillAllFields = "Please fill in all fields"
                )
            }
        }
    }
}
