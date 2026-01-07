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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.cil.shift.core.common.auth.AuthManager
import com.cil.shift.core.common.auth.AuthResult
import com.cil.shift.core.common.localization.Language
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.purchase.PurchaseManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authManager = koinInject<AuthManager>()
    val purchaseManager = koinInject<PurchaseManager>()
    val localizationManager = koinInject<LocalizationManager>()
    val currentLanguage by localizationManager.currentLanguage.collectAsState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Email validation - simple check
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") &&
               email.contains(".") &&
               email.indexOf("@") < email.lastIndexOf(".") &&
               email.length >= 5
    }

    // Password validation rules
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasNumber = password.any { it.isDigit() }
    val isPasswordValid = hasMinLength && hasUppercase && hasLowercase && hasNumber

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    val strings = remember(currentLanguage) { SignUpStrings.get(currentLanguage) }

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
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = strings.createAccount,
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

            Spacer(modifier = Modifier.height(32.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text(strings.name) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
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
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.6f)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
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

            // Password requirements - only show unmet requirements
            if (password.isNotEmpty() && !isPasswordValid) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!hasMinLength) {
                        PasswordRequirement(text = strings.minLength)
                    }
                    if (!hasUppercase) {
                        PasswordRequirement(text = strings.hasUppercase)
                    }
                    if (!hasLowercase) {
                        PasswordRequirement(text = strings.hasLowercase)
                    }
                    if (!hasNumber) {
                        PasswordRequirement(text = strings.hasNumber)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text(strings.confirmPassword) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.6f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.6f)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

            // Password match indicator
            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordRequirement(text = strings.passwordsDoNotMatch)
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

            // Sign up button
            Button(
                onClick = {
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            errorMessage = strings.fillAllFields
                            return@Button
                        }
                        !isValidEmail(email) -> {
                            errorMessage = strings.invalidEmail
                            return@Button
                        }
                        !isPasswordValid -> {
                            errorMessage = strings.passwordRequirements
                            return@Button
                        }
                        password != confirmPassword -> {
                            errorMessage = strings.passwordsDoNotMatch
                            return@Button
                        }
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        when (val result = authManager.signUpWithEmail(email, password)) {
                            is AuthResult.Success -> {
                                // Update display name
                                if (name.isNotBlank()) {
                                    authManager.updateDisplayName(name)
                                }
                                // Login to RevenueCat with Firebase UID for cross-platform sync
                                purchaseManager.login(result.user.uid)
                                onSignUpSuccess()
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
                        text = strings.signUp,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Already have account
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = strings.alreadyHaveAccount,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.signIn,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4E7CFF),
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }
        }
    }
}

@Composable
private fun PasswordRequirement(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = Color(0xFFFF6B6B),
                    shape = RoundedCornerShape(3.dp)
                )
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFFFF6B6B)
        )
    }
}

private data class SignUpStrings(
    val createAccount: String,
    val subtitle: String,
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val signUp: String,
    val alreadyHaveAccount: String,
    val signIn: String,
    val fillAllFields: String,
    val invalidEmail: String,
    val passwordRequirements: String,
    val passwordsDoNotMatch: String,
    val minLength: String,
    val hasUppercase: String,
    val hasLowercase: String,
    val hasNumber: String
) {
    companion object {
        fun get(language: Language): SignUpStrings {
            return when (language) {
                Language.TURKISH -> SignUpStrings(
                    createAccount = "Hesap Oluştur",
                    subtitle = "Alışkanlıklarınızı takip etmeye başlayın",
                    name = "Ad Soyad",
                    email = "E-posta",
                    password = "Şifre",
                    confirmPassword = "Şifre Tekrar",
                    signUp = "Kayıt Ol",
                    alreadyHaveAccount = "Zaten hesabınız var mı?",
                    signIn = "Giriş Yap",
                    fillAllFields = "Lütfen tüm alanları doldurun",
                    invalidEmail = "Geçerli bir e-posta adresi girin",
                    passwordRequirements = "Şifre tüm gereksinimleri karşılamalı",
                    passwordsDoNotMatch = "Şifreler eşleşmiyor",
                    minLength = "En az 8 karakter",
                    hasUppercase = "En az 1 büyük harf",
                    hasLowercase = "En az 1 küçük harf",
                    hasNumber = "En az 1 rakam"
                )
                Language.SPANISH -> SignUpStrings(
                    createAccount = "Crear Cuenta",
                    subtitle = "Comienza a rastrear tus hábitos",
                    name = "Nombre",
                    email = "Correo electrónico",
                    password = "Contraseña",
                    confirmPassword = "Confirmar Contraseña",
                    signUp = "Regístrate",
                    alreadyHaveAccount = "¿Ya tienes cuenta?",
                    signIn = "Iniciar Sesión",
                    fillAllFields = "Por favor completa todos los campos",
                    invalidEmail = "Ingresa un correo electrónico válido",
                    passwordRequirements = "La contraseña debe cumplir todos los requisitos",
                    passwordsDoNotMatch = "Las contraseñas no coinciden",
                    minLength = "Mínimo 8 caracteres",
                    hasUppercase = "Al menos 1 mayúscula",
                    hasLowercase = "Al menos 1 minúscula",
                    hasNumber = "Al menos 1 número"
                )
                Language.FRENCH -> SignUpStrings(
                    createAccount = "Créer un Compte",
                    subtitle = "Commencez à suivre vos habitudes",
                    name = "Nom Complet",
                    email = "E-mail",
                    password = "Mot de passe",
                    confirmPassword = "Confirmer le mot de passe",
                    signUp = "S'inscrire",
                    alreadyHaveAccount = "Vous avez déjà un compte?",
                    signIn = "Se Connecter",
                    fillAllFields = "Veuillez remplir tous les champs",
                    invalidEmail = "Veuillez entrer une adresse e-mail valide",
                    passwordRequirements = "Le mot de passe doit répondre à toutes les exigences",
                    passwordsDoNotMatch = "Les mots de passe ne correspondent pas",
                    minLength = "Au moins 8 caractères",
                    hasUppercase = "Au moins 1 majuscule",
                    hasLowercase = "Au moins 1 minuscule",
                    hasNumber = "Au moins 1 chiffre"
                )
                Language.GERMAN -> SignUpStrings(
                    createAccount = "Konto Erstellen",
                    subtitle = "Beginnen Sie Ihre Gewohnheiten zu verfolgen",
                    name = "Vollständiger Name",
                    email = "E-Mail",
                    password = "Passwort",
                    confirmPassword = "Passwort bestätigen",
                    signUp = "Registrieren",
                    alreadyHaveAccount = "Bereits ein Konto?",
                    signIn = "Anmelden",
                    fillAllFields = "Bitte alle Felder ausfüllen",
                    invalidEmail = "Bitte geben Sie eine gültige E-Mail-Adresse ein",
                    passwordRequirements = "Passwort muss alle Anforderungen erfüllen",
                    passwordsDoNotMatch = "Passwörter stimmen nicht überein",
                    minLength = "Mindestens 8 Zeichen",
                    hasUppercase = "Mindestens 1 Großbuchstabe",
                    hasLowercase = "Mindestens 1 Kleinbuchstabe",
                    hasNumber = "Mindestens 1 Zahl"
                )
                Language.PORTUGUESE -> SignUpStrings(
                    createAccount = "Criar Conta",
                    subtitle = "Comece a acompanhar seus hábitos",
                    name = "Nome Completo",
                    email = "E-mail",
                    password = "Senha",
                    confirmPassword = "Confirmar Senha",
                    signUp = "Cadastrar",
                    alreadyHaveAccount = "Já tem uma conta?",
                    signIn = "Entrar",
                    fillAllFields = "Por favor preencha todos os campos",
                    invalidEmail = "Por favor insira um e-mail válido",
                    passwordRequirements = "A senha deve atender a todos os requisitos",
                    passwordsDoNotMatch = "As senhas não coincidem",
                    minLength = "Pelo menos 8 caracteres",
                    hasUppercase = "Pelo menos 1 letra maiúscula",
                    hasLowercase = "Pelo menos 1 letra minúscula",
                    hasNumber = "Pelo menos 1 número"
                )
                Language.ARABIC -> SignUpStrings(
                    createAccount = "إنشاء حساب",
                    subtitle = "ابدأ بتتبع عاداتك",
                    name = "الاسم الكامل",
                    email = "البريد الإلكتروني",
                    password = "كلمة المرور",
                    confirmPassword = "تأكيد كلمة المرور",
                    signUp = "تسجيل",
                    alreadyHaveAccount = "لديك حساب بالفعل؟",
                    signIn = "تسجيل الدخول",
                    fillAllFields = "يرجى ملء جميع الحقول",
                    invalidEmail = "يرجى إدخال بريد إلكتروني صحيح",
                    passwordRequirements = "يجب أن تستوفي كلمة المرور جميع المتطلبات",
                    passwordsDoNotMatch = "كلمات المرور غير متطابقة",
                    minLength = "8 أحرف على الأقل",
                    hasUppercase = "حرف كبير واحد على الأقل",
                    hasLowercase = "حرف صغير واحد على الأقل",
                    hasNumber = "رقم واحد على الأقل"
                )
                Language.RUSSIAN -> SignUpStrings(
                    createAccount = "Создать Аккаунт",
                    subtitle = "Начните отслеживать свои привычки",
                    name = "Полное имя",
                    email = "Эл. почта",
                    password = "Пароль",
                    confirmPassword = "Подтвердить пароль",
                    signUp = "Регистрация",
                    alreadyHaveAccount = "Уже есть аккаунт?",
                    signIn = "Войти",
                    fillAllFields = "Пожалуйста, заполните все поля",
                    invalidEmail = "Введите действительный адрес эл. почты",
                    passwordRequirements = "Пароль должен соответствовать всем требованиям",
                    passwordsDoNotMatch = "Пароли не совпадают",
                    minLength = "Минимум 8 символов",
                    hasUppercase = "Минимум 1 заглавная буква",
                    hasLowercase = "Минимум 1 строчная буква",
                    hasNumber = "Минимум 1 цифра"
                )
                Language.HINDI -> SignUpStrings(
                    createAccount = "खाता बनाएं",
                    subtitle = "अपनी आदतों को ट्रैक करना शुरू करें",
                    name = "पूरा नाम",
                    email = "ईमेल",
                    password = "पासवर्ड",
                    confirmPassword = "पासवर्ड की पुष्टि करें",
                    signUp = "साइन अप करें",
                    alreadyHaveAccount = "पहले से खाता है?",
                    signIn = "साइन इन करें",
                    fillAllFields = "कृपया सभी फ़ील्ड भरें",
                    invalidEmail = "कृपया एक वैध ईमेल पता दर्ज करें",
                    passwordRequirements = "पासवर्ड को सभी आवश्यकताओं को पूरा करना होगा",
                    passwordsDoNotMatch = "पासवर्ड मेल नहीं खाते",
                    minLength = "कम से कम 8 अक्षर",
                    hasUppercase = "कम से कम 1 बड़ा अक्षर",
                    hasLowercase = "कम से कम 1 छोटा अक्षर",
                    hasNumber = "कम से कम 1 अंक"
                )
                Language.JAPANESE -> SignUpStrings(
                    createAccount = "アカウント作成",
                    subtitle = "習慣の追跡を始めましょう",
                    name = "フルネーム",
                    email = "メール",
                    password = "パスワード",
                    confirmPassword = "パスワード確認",
                    signUp = "サインアップ",
                    alreadyHaveAccount = "アカウントをお持ちですか？",
                    signIn = "サインイン",
                    fillAllFields = "すべての項目を入力してください",
                    invalidEmail = "有効なメールアドレスを入力してください",
                    passwordRequirements = "パスワードはすべての要件を満たす必要があります",
                    passwordsDoNotMatch = "パスワードが一致しません",
                    minLength = "8文字以上",
                    hasUppercase = "大文字1文字以上",
                    hasLowercase = "小文字1文字以上",
                    hasNumber = "数字1文字以上"
                )
                Language.CHINESE -> SignUpStrings(
                    createAccount = "创建账户",
                    subtitle = "开始追踪您的习惯",
                    name = "全名",
                    email = "电子邮件",
                    password = "密码",
                    confirmPassword = "确认密码",
                    signUp = "注册",
                    alreadyHaveAccount = "已有账户？",
                    signIn = "登录",
                    fillAllFields = "请填写所有字段",
                    invalidEmail = "请输入有效的电子邮件地址",
                    passwordRequirements = "密码必须满足所有要求",
                    passwordsDoNotMatch = "密码不匹配",
                    minLength = "至少8个字符",
                    hasUppercase = "至少1个大写字母",
                    hasLowercase = "至少1个小写字母",
                    hasNumber = "至少1个数字"
                )
                else -> SignUpStrings(
                    createAccount = "Create Account",
                    subtitle = "Start tracking your habits",
                    name = "Full Name",
                    email = "Email",
                    password = "Password",
                    confirmPassword = "Confirm Password",
                    signUp = "Sign Up",
                    alreadyHaveAccount = "Already have an account?",
                    signIn = "Sign In",
                    fillAllFields = "Please fill in all fields",
                    invalidEmail = "Please enter a valid email address",
                    passwordRequirements = "Password must meet all requirements",
                    passwordsDoNotMatch = "Passwords do not match",
                    minLength = "At least 8 characters",
                    hasUppercase = "At least 1 uppercase letter",
                    hasLowercase = "At least 1 lowercase letter",
                    hasNumber = "At least 1 number"
                )
            }
        }
    }
}
