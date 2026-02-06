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
import com.cil.shift.core.common.isIOS
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.AuthResult
import com.cil.shift.core.common.auth.SocialSignInProvider
import com.cil.shift.core.common.auth.SocialSignInResult
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
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isAppleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val socialSignInProvider = koinInject<SocialSignInProvider>()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4E7CFF),
                    unfocusedBorderColor = textColor.copy(alpha = 0.2f),
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = Color(0xFF4E7CFF),
                    focusedLabelColor = Color(0xFF4E7CFF),
                    unfocusedLabelColor = textColor.copy(alpha = 0.6f)
                ),
                enabled = !isLoading
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

            // Google Sign-In Button (Android only - not implemented on iOS)
            if (!isIOS) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isGoogleLoading = true
                            errorMessage = null

                            try {
                                when (val result = socialSignInProvider.signInWithGoogle()) {
                                    is SocialSignInResult.Success -> {
                                        when (val authResult = authManager.signInWithGoogle(result.idToken)) {
                                            is AuthResult.Success -> {
                                                purchaseManager.login(authResult.user.uid)
                                                onLoginSuccess()
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = authResult.message
                                            }
                                        }
                                    }
                                    is SocialSignInResult.Error -> {
                                        errorMessage = result.message
                                    }
                                    SocialSignInResult.Cancelled -> {
                                        // User cancelled, do nothing
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Unexpected error: ${e.message}"
                            } finally {
                                isGoogleLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = !isGoogleLoading),
                    enabled = !isGoogleLoading && !isAppleLoading && !isLoading
                ) {
                    // Google logo with multicolor G
                    GoogleLogo(size = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.continueWithGoogle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
            }

            // Apple Sign-In Button (iOS only)
            if (isIOS) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isAppleLoading = true
                            errorMessage = null

                            when (val result = socialSignInProvider.signInWithApple()) {
                                is SocialSignInResult.Success -> {
                                    when (val authResult = authManager.signInWithApple(result.idToken, result.rawNonce)) {
                                        is AuthResult.Success -> {
                                            purchaseManager.login(authResult.user.uid)
                                            onLoginSuccess()
                                        }
                                        is AuthResult.Error -> {
                                            errorMessage = authResult.message
                                        }
                                    }
                                }
                                is SocialSignInResult.Error -> {
                                    errorMessage = result.message
                                }
                                SocialSignInResult.Cancelled -> {
                                    // User cancelled, do nothing
                                }
                            }

                            isAppleLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textColor,
                        contentColor = backgroundColor
                    ),
                    enabled = !isAppleLoading && !isGoogleLoading && !isLoading
                ) {
                    AppleLogo(size = 18.dp, color = backgroundColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.continueWithApple,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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

            // Full screen loading overlay
            if (isLoading || isGoogleLoading || isAppleLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF4E7CFF),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun AppleLogo(
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier.size(size)
    ) {
        val s = size.toPx() / 24f
        val path = androidx.compose.ui.graphics.Path().apply {
            // Apple body
            moveTo(18.71f * s, 19.5f * s)
            cubicTo(17.88f * s, 20.74f * s, 17f * s, 21.95f * s, 15.66f * s, 21.97f * s)
            cubicTo(14.32f * s, 21.99f * s, 13.89f * s, 21.18f * s, 12.37f * s, 21.18f * s)
            cubicTo(10.84f * s, 21.18f * s, 10.37f * s, 21.95f * s, 9.1f * s, 21.99f * s)
            cubicTo(7.79f * s, 22.03f * s, 6.8f * s, 20.68f * s, 5.96f * s, 19.47f * s)
            cubicTo(4.25f * s, 16.56f * s, 2.93f * s, 11.3f * s, 4.7f * s, 7.72f * s)
            cubicTo(5.57f * s, 5.94f * s, 7.36f * s, 4.86f * s, 9.28f * s, 4.84f * s)
            cubicTo(10.56f * s, 4.82f * s, 11.78f * s, 5.71f * s, 12.58f * s, 5.71f * s)
            cubicTo(13.38f * s, 5.71f * s, 14.88f * s, 4.63f * s, 16.42f * s, 4.8f * s)
            cubicTo(17.07f * s, 4.83f * s, 18.9f * s, 5.06f * s, 20.06f * s, 6.79f * s)
            cubicTo(19.96f * s, 6.85f * s, 17.62f * s, 8.22f * s, 17.65f * s, 11.04f * s)
            cubicTo(17.68f * s, 14.37f * s, 20.58f * s, 15.48f * s, 20.61f * s, 15.49f * s)
            cubicTo(20.58f * s, 15.56f * s, 20.15f * s, 17.05f * s, 19.09f * s, 18.57f * s)
            lineTo(18.71f * s, 19.5f * s)
            close()
            // Leaf
            moveTo(13f * s, 3.5f * s)
            cubicTo(13.73f * s, 2.67f * s, 14.94f * s, 2.04f * s, 15.94f * s, 2f * s)
            cubicTo(16.07f * s, 3.17f * s, 15.6f * s, 4.35f * s, 14.9f * s, 5.19f * s)
            cubicTo(14.21f * s, 6.04f * s, 13.07f * s, 6.7f * s, 11.95f * s, 6.61f * s)
            cubicTo(11.8f * s, 5.46f * s, 12.36f * s, 4.26f * s, 13f * s, 3.5f * s)
            close()
        }
        drawPath(path, color = color)
    }
}

@Composable
private fun GoogleLogo(
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    // Google brand colors
    val blue = Color(0xFF4285F4)
    val red = Color(0xFFEA4335)
    val yellow = Color(0xFFFBBC05)
    val green = Color(0xFF34A853)

    androidx.compose.foundation.Canvas(
        modifier = modifier.size(size)
    ) {
        val strokeWidth = size.toPx() * 0.18f
        val radius = (size.toPx() - strokeWidth) / 2
        val center = androidx.compose.ui.geometry.Offset(size.toPx() / 2, size.toPx() / 2)

        // Draw colored arcs to form the G
        // Blue arc (right side, top)
        drawArc(
            color = blue,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
        )

        // Green arc (bottom right)
        drawArc(
            color = green,
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
        )

        // Yellow arc (bottom left)
        drawArc(
            color = yellow,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
        )

        // Red arc (top left)
        drawArc(
            color = red,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
        )

        // Draw the horizontal bar of the G (blue)
        val barY = size.toPx() / 2
        val barStartX = size.toPx() / 2
        val barEndX = size.toPx() - strokeWidth / 2
        drawLine(
            color = blue,
            start = androidx.compose.ui.geometry.Offset(barStartX, barY),
            end = androidx.compose.ui.geometry.Offset(barEndX, barY),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Butt
        )
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
    val fillAllFields: String,
    val continueWithGoogle: String,
    val continueWithApple: String
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
                    fillAllFields = "Lütfen tüm alanları doldurun",
                    continueWithGoogle = "Google ile devam et",
                    continueWithApple = "Apple ile devam et"
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
                    fillAllFields = "Por favor completa todos los campos",
                    continueWithGoogle = "Continuar con Google",
                    continueWithApple = "Continuar con Apple"
                )
                Language.FRENCH -> LoginStrings(
                    welcomeBack = "Bon retour!",
                    email = "E-mail",
                    password = "Mot de passe",
                    forgotPassword = "Mot de passe oublié?",
                    signIn = "Se Connecter",
                    or = "ou",
                    noAccount = "Pas de compte?",
                    signUp = "S'inscrire",
                    fillAllFields = "Veuillez remplir tous les champs",
                    continueWithGoogle = "Continuer avec Google",
                    continueWithApple = "Continuer avec Apple"
                )
                Language.GERMAN -> LoginStrings(
                    welcomeBack = "Willkommen zurück!",
                    email = "E-Mail",
                    password = "Passwort",
                    forgotPassword = "Passwort vergessen?",
                    signIn = "Anmelden",
                    or = "oder",
                    noAccount = "Kein Konto?",
                    signUp = "Registrieren",
                    fillAllFields = "Bitte alle Felder ausfüllen",
                    continueWithGoogle = "Weiter mit Google",
                    continueWithApple = "Weiter mit Apple"
                )
                Language.PORTUGUESE -> LoginStrings(
                    welcomeBack = "Bem-vindo de volta!",
                    email = "E-mail",
                    password = "Senha",
                    forgotPassword = "Esqueceu a senha?",
                    signIn = "Entrar",
                    or = "ou",
                    noAccount = "Não tem conta?",
                    signUp = "Cadastrar",
                    fillAllFields = "Por favor preencha todos os campos",
                    continueWithGoogle = "Continuar com Google",
                    continueWithApple = "Continuar com Apple"
                )
                Language.ARABIC -> LoginStrings(
                    welcomeBack = "!مرحباً بعودتك",
                    email = "البريد الإلكتروني",
                    password = "كلمة المرور",
                    forgotPassword = "نسيت كلمة المرور؟",
                    signIn = "تسجيل الدخول",
                    or = "أو",
                    noAccount = "ليس لديك حساب؟",
                    signUp = "إنشاء حساب",
                    fillAllFields = "يرجى ملء جميع الحقول",
                    continueWithGoogle = "المتابعة مع Google",
                    continueWithApple = "المتابعة مع Apple"
                )
                Language.RUSSIAN -> LoginStrings(
                    welcomeBack = "С возвращением!",
                    email = "Эл. почта",
                    password = "Пароль",
                    forgotPassword = "Забыли пароль?",
                    signIn = "Войти",
                    or = "или",
                    noAccount = "Нет аккаунта?",
                    signUp = "Регистрация",
                    fillAllFields = "Пожалуйста, заполните все поля",
                    continueWithGoogle = "Продолжить с Google",
                    continueWithApple = "Продолжить с Apple"
                )
                Language.HINDI -> LoginStrings(
                    welcomeBack = "वापसी पर स्वागत है!",
                    email = "ईमेल",
                    password = "पासवर्ड",
                    forgotPassword = "पासवर्ड भूल गए?",
                    signIn = "साइन इन करें",
                    or = "या",
                    noAccount = "खाता नहीं है?",
                    signUp = "साइन अप करें",
                    fillAllFields = "कृपया सभी फ़ील्ड भरें",
                    continueWithGoogle = "Google से जारी रखें",
                    continueWithApple = "Apple से जारी रखें"
                )
                Language.JAPANESE -> LoginStrings(
                    welcomeBack = "おかえりなさい！",
                    email = "メール",
                    password = "パスワード",
                    forgotPassword = "パスワードをお忘れですか？",
                    signIn = "サインイン",
                    or = "または",
                    noAccount = "アカウントをお持ちでないですか？",
                    signUp = "サインアップ",
                    fillAllFields = "すべての項目を入力してください",
                    continueWithGoogle = "Googleで続ける",
                    continueWithApple = "Appleで続ける"
                )
                Language.CHINESE -> LoginStrings(
                    welcomeBack = "欢迎回来！",
                    email = "电子邮件",
                    password = "密码",
                    forgotPassword = "忘记密码？",
                    signIn = "登录",
                    or = "或",
                    noAccount = "没有账户？",
                    signUp = "注册",
                    fillAllFields = "请填写所有字段",
                    continueWithGoogle = "使用 Google 继续",
                    continueWithApple = "使用 Apple 继续"
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
                    fillAllFields = "Please fill in all fields",
                    continueWithGoogle = "Continue with Google",
                    continueWithApple = "Continue with Apple"
                )
            }
        }
    }
}
