package com.cil.shift.feature.settings.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.AuthResult
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authManager = koinInject<AuthManager>()
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    val strings = remember(currentLanguage) { ForgotPasswordStrings.get(currentLanguage) }

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

            // Title
            Text(
                text = strings.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.subtitle,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                    successMessage = null
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

            // Success message
            if (successMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = successMessage!!,
                    fontSize = 13.sp,
                    color = Color(0xFF4ECDC4),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reset button
            Button(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = strings.enterEmail
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        successMessage = null

                        when (val result = authManager.sendPasswordResetEmail(email)) {
                            is AuthResult.Success -> {
                                successMessage = strings.emailSent
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
                        text = strings.sendResetLink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to login
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = strings.backToLogin,
                    fontSize = 14.sp,
                    color = Color(0xFF4E7CFF)
                )
            }
        }
    }
}

private data class ForgotPasswordStrings(
    val title: String,
    val subtitle: String,
    val email: String,
    val sendResetLink: String,
    val backToLogin: String,
    val enterEmail: String,
    val emailSent: String
) {
    companion object {
        fun get(language: Language): ForgotPasswordStrings {
            return when (language) {
                Language.TURKISH -> ForgotPasswordStrings(
                    title = "Şifremi Unuttum",
                    subtitle = "E-posta adresinizi girin, size şifre sıfırlama bağlantısı göndereceğiz",
                    email = "E-posta",
                    sendResetLink = "Sıfırlama Bağlantısı Gönder",
                    backToLogin = "Giriş sayfasına dön",
                    enterEmail = "Lütfen e-posta adresinizi girin",
                    emailSent = "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi"
                )
                Language.SPANISH -> ForgotPasswordStrings(
                    title = "Olvidé mi Contraseña",
                    subtitle = "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña",
                    email = "Correo electrónico",
                    sendResetLink = "Enviar Enlace",
                    backToLogin = "Volver al inicio de sesión",
                    enterEmail = "Por favor ingresa tu correo",
                    emailSent = "Se ha enviado un enlace a tu correo"
                )
                Language.FRENCH -> ForgotPasswordStrings(
                    title = "Mot de passe oublié",
                    subtitle = "Entrez votre e-mail et nous vous enverrons un lien pour réinitialiser votre mot de passe",
                    email = "E-mail",
                    sendResetLink = "Envoyer le lien",
                    backToLogin = "Retour à la connexion",
                    enterEmail = "Veuillez entrer votre e-mail",
                    emailSent = "Un lien de réinitialisation a été envoyé à votre e-mail"
                )
                Language.GERMAN -> ForgotPasswordStrings(
                    title = "Passwort vergessen",
                    subtitle = "Geben Sie Ihre E-Mail ein und wir senden Ihnen einen Link zum Zurücksetzen",
                    email = "E-Mail",
                    sendResetLink = "Link senden",
                    backToLogin = "Zurück zur Anmeldung",
                    enterEmail = "Bitte geben Sie Ihre E-Mail ein",
                    emailSent = "Ein Link wurde an Ihre E-Mail gesendet"
                )
                Language.PORTUGUESE -> ForgotPasswordStrings(
                    title = "Esqueci a Senha",
                    subtitle = "Digite seu e-mail e enviaremos um link para redefinir sua senha",
                    email = "E-mail",
                    sendResetLink = "Enviar Link",
                    backToLogin = "Voltar ao login",
                    enterEmail = "Por favor insira seu e-mail",
                    emailSent = "Link de redefinição enviado para seu e-mail"
                )
                Language.ARABIC -> ForgotPasswordStrings(
                    title = "نسيت كلمة المرور",
                    subtitle = "أدخل بريدك الإلكتروني وسنرسل لك رابطًا لإعادة تعيين كلمة المرور",
                    email = "البريد الإلكتروني",
                    sendResetLink = "إرسال الرابط",
                    backToLogin = "العودة لتسجيل الدخول",
                    enterEmail = "يرجى إدخال بريدك الإلكتروني",
                    emailSent = "تم إرسال رابط إعادة التعيين إلى بريدك الإلكتروني"
                )
                Language.RUSSIAN -> ForgotPasswordStrings(
                    title = "Забыли пароль",
                    subtitle = "Введите вашу эл. почту и мы отправим ссылку для сброса пароля",
                    email = "Эл. почта",
                    sendResetLink = "Отправить ссылку",
                    backToLogin = "Вернуться к входу",
                    enterEmail = "Пожалуйста, введите вашу эл. почту",
                    emailSent = "Ссылка для сброса отправлена на вашу эл. почту"
                )
                Language.HINDI -> ForgotPasswordStrings(
                    title = "पासवर्ड भूल गए",
                    subtitle = "अपना ईमेल दर्ज करें और हम आपको पासवर्ड रीसेट लिंक भेजेंगे",
                    email = "ईमेल",
                    sendResetLink = "लिंक भेजें",
                    backToLogin = "लॉगिन पर वापस जाएं",
                    enterEmail = "कृपया अपना ईमेल दर्ज करें",
                    emailSent = "पासवर्ड रीसेट लिंक आपके ईमेल पर भेजा गया"
                )
                Language.JAPANESE -> ForgotPasswordStrings(
                    title = "パスワードをお忘れですか",
                    subtitle = "メールアドレスを入力すると、パスワードリセットのリンクをお送りします",
                    email = "メール",
                    sendResetLink = "リンクを送信",
                    backToLogin = "ログインに戻る",
                    enterEmail = "メールアドレスを入力してください",
                    emailSent = "パスワードリセットリンクがメールに送信されました"
                )
                Language.CHINESE -> ForgotPasswordStrings(
                    title = "忘记密码",
                    subtitle = "输入您的电子邮件，我们将向您发送重置密码的链接",
                    email = "电子邮件",
                    sendResetLink = "发送链接",
                    backToLogin = "返回登录",
                    enterEmail = "请输入您的电子邮件",
                    emailSent = "密码重置链接已发送到您的电子邮件"
                )
                else -> ForgotPasswordStrings(
                    title = "Forgot Password",
                    subtitle = "Enter your email and we'll send you a link to reset your password",
                    email = "Email",
                    sendResetLink = "Send Reset Link",
                    backToLogin = "Back to login",
                    enterEmail = "Please enter your email",
                    emailSent = "Password reset link has been sent to your email"
                )
            }
        }
    }
}
