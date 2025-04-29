package br.com.fiap.desafioquod.screens.digitalbiometrics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.DraggableButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import br.com.fiap.desafioquod.apiservice.RetrofitClient
import br.com.fiap.desafioquod.ui.theme.RedError
import br.com.fiap.desafioquod.utils.explanatoryTextBiom_1
import br.com.fiap.desafioquod.utils.explanatoryText_4
import br.com.fiap.desafioquod.utils.getLocation
import br.com.fiap.desafioquod.utils.message_1
import br.com.fiap.desafioquod.utils.textDidYouKnow
import br.com.fiap.desafioquod.utils.textInitial
import br.com.fiap.desafioquod.utils.textLineHeight
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.UUID

// Constantes
private const val MAX_FAILED_ATTEMPTS = 3

@Composable
fun DigitalBiometricsScreen(navController: NavController) {
    var showNoFingerprintDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val sharedPreferences = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
    var isBiometricRegistered by remember {
        mutableStateOf(sharedPreferences.getBoolean("biometric_registered", false))
    }
    var failedAttempts by remember { mutableStateOf(0) }
    var isFingerprintEnrolled by remember { mutableStateOf(false) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Chave única para forçar a atualização da localização em cada entrada
    val screenKey = remember { mutableStateOf(UUID.randomUUID().toString()) }

    // Capturar localização sempre que a tela for carregada
    LaunchedEffect(screenKey.value) {
        getLocation(context, fusedLocationClient) { location ->
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
            } ?: run {
                Toast.makeText(context, "Localização não disponível", Toast.LENGTH_SHORT).show()
                latitude = 0.0
                longitude = 0.0
            }
        }
    }

    LaunchedEffect(Unit) {
        checkBiometricAvailability(context) { isAvailable ->
            if (!isAvailable) {
                Toast.makeText(context, "Biometria não disponível no dispositivo", Toast.LENGTH_SHORT).show()
            }
        }
        checkFingerprintEnrolled(context) { isEnrolled ->
            isFingerprintEnrolled = isEnrolled
            if (!isEnrolled) {
                showNoFingerprintDialog = true
            }
        }
    }

    val biometricPrompt = remember { mutableStateOf<BiometricPrompt?>(null) }

    LaunchedEffect(failedAttempts) {
        biometricPrompt.value = createBiometricPrompt(
            context,
            failedAttempts
        ) { authenticated, updatedFailedAttempts, isCancelled ->
            failedAttempts = updatedFailedAttempts

            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown_device"
            val captureDate = java.time.LocalDateTime.now().toString()

            getLocation(context, fusedLocationClient) { location ->
                location?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                    Log.d("Location", "Updated Latitude: $latitude, Longitude: $longitude")
                } ?: run {
                    latitude = 0.0
                    longitude = 0.0
                }

                val authenticatedBody = authenticated.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val failedAttemptsBody = failedAttempts.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val deviceIdBody = deviceId.toRequestBody("text/plain".toMediaTypeOrNull())
                val latitudeBody = (latitude ?: 0.0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val longitudeBody = (longitude ?: 0.0).toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val androidVersionBody = Build.VERSION.RELEASE.toRequestBody("text/plain".toMediaTypeOrNull())
                val apiLevelBody = Build.VERSION.SDK_INT.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val manufacturerBody = Build.MANUFACTURER.toRequestBody("text/plain".toMediaTypeOrNull())
                val modelBody = Build.MODEL.toRequestBody("text/plain".toMediaTypeOrNull())
                val captureDateBody = captureDate.toRequestBody("text/plain".toMediaTypeOrNull())

                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.apiService.validateBiometric(
                            authenticatedBody, failedAttemptsBody, deviceIdBody,
                            latitudeBody, longitudeBody, androidVersionBody, apiLevelBody,
                            manufacturerBody, modelBody, captureDateBody
                        )
                        if (response.isSuccessful) {
                            val biometricResponse = response.body()
                            biometricResponse?.let {
                                val analysisReportEncoded = it.analysisReport?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
                                val messageEncoded = it.message?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
                                // Adicionar logs para depurar
                                Log.d("DigitalBiometricsScreen", "Response - status: ${it.status}, message: ${it.message}")
                                Log.d("DigitalBiometricsScreen", "Navigating - analysisReport: $analysisReportEncoded, message: $messageEncoded")
                                when (it.status) {
                                    "success" -> {
                                        if (!isBiometricRegistered && it.registered) {
                                            sharedPreferences.edit().putBoolean("biometric_registered", true).apply()
                                            isBiometricRegistered = true
                                        }
                                        failedAttempts = 0
                                        navController.navigate("digitalvalidation/success?analysisReport=$analysisReportEncoded")
                                    }
                                    "failure" -> {
                                        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                                            biometricPrompt.value?.cancelAuthentication()
                                            biometricPrompt.value = null
                                            navController.navigate("digitalvalidation/fraud_detected?analysisReport=$analysisReportEncoded&message=$messageEncoded")
                                        } else {
                                            Toast.makeText(context, "Tentativa falhou. Tente novamente ($failedAttempts/$MAX_FAILED_ATTEMPTS).", Toast.LENGTH_SHORT).show()
                                            // Não navega, apenas atualiza o estado para permitir nova tentativa
                                        }
                                    }
                                    "fraud_detected" -> {
                                        failedAttempts = 0
                                        biometricPrompt.value?.cancelAuthentication()
                                        biometricPrompt.value = null
                                        navController.navigate("digitalvalidation/fraud_detected?analysisReport=$analysisReportEncoded&message=$messageEncoded")
                                    }
                                    else -> {
                                        navController.navigate("digitalvalidation/unknown?analysisReport=$analysisReportEncoded&message=$messageEncoded")
                                    }
                                }
                            } ?: Log.e("DigitalBiometricsScreen", "Response body is null")
                        } else {
                            Log.e("DigitalBiometricsScreen", "API call failed with code: ${response.code()}")
                            navController.navigate("digitalvalidation/serverError")
                        }
                    } catch (e: Exception) {
                        Log.e("DigitalBiometricsScreen", "Error during API call: ${e.message}")
                        navController.navigate("digitalvalidation/connectionError")
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteQuod)
    ) {
        DraggableButton(
            bgColor = PurpleQuod,
            onClick = { showDialog = true }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteQuod),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(
                iconResId = R.drawable.hbmenu,
                onMenuClick = { navController.navigate("home") },
                iconTint = BlackQuod
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PurpleQuod)) { append("_") }
                    withStyle(style = SpanStyle(color = BlackQuod)) { append("Biometria Digital") }
                    withStyle(style = SpanStyle(color = PurpleQuod)) { append(".") }
                },
                fontFamily = recursiveFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = GrayQuod,
                style = TextStyle(lineHeight = 38.sp)
            )

            Spacer(modifier = Modifier.height(100.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)) {
                Text(
                    text = textInitial,
                    color = BlackQuod,
                    fontFamily = recursiveFontFamily,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Justify
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = {
                    if (isBiometricAvailable(context)) {
                        biometricPrompt.value?.authenticate(createPromptInfo())
                    } else {
                        Toast.makeText(context, "Biometria não disponível", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(PurpleQuod),
                modifier = Modifier
                    .padding(16.dp)
                    .height(50.dp),
                enabled = isFingerprintEnrolled
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = "Ícone de Impressão Digital",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Iniciar Leitura Biométrica",
                    fontFamily = recursiveFontFamily,
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
                )
            }

            if (showNoFingerprintDialog) {
                CustomDialog(
                    message = message_1,
                    onDismiss = {
                        showNoFingerprintDialog = false
                        checkFingerprintEnrolled(context) { isEnrolled ->
                            isFingerprintEnrolled = isEnrolled
                        }
                    },
                    icon = Icons.Rounded.Fingerprint,
                    iconColor = RedError
                )
            }

            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Card(
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                textDidYouKnow,
                                fontFamily = recursiveFontFamily,
                                color = PurpleQuod,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = explanatoryTextBiom_1,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = BlackQuod,
                                fontFamily = recursiveFontFamily,
                                style = textLineHeight
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(BlackQuod)
                            ) {
                                Text("Fechar", fontFamily = recursiveFontFamily)
                            }
                        }
                    }
                }
            }
        }
    }

    // Atualizar a chave ao voltar para a tela
    LaunchedEffect(navController.currentBackStackEntryAsState().value) {
        screenKey.value = UUID.randomUUID().toString()
    }
}

@Composable
fun CustomDialog(
    message: String,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    iconColor: Color = BlackQuod
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = iconColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = message,
                    fontWeight = FontWeight.Bold,
                    fontFamily = recursiveFontFamily,
                    textAlign = TextAlign.Center,
                    color = BlackQuod
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(BlackQuod)) {
                    Text("Fechar", fontFamily = recursiveFontFamily)
                }
            }
        }
    }
}

private fun checkFingerprintEnrolled(context: Context, onResult: (Boolean) -> Unit) {
    val biometricManager = BiometricManager.from(context)
    val result =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            else -> false
        }
    onResult(result)
}

private fun createBiometricPrompt(
    context: Context,
    failedAttempts: Int,
    onAuthenticationResult: (Boolean, Int, Boolean) -> Unit
): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(context)
    val activity = context as? FragmentActivity
        ?: throw IllegalArgumentException("Context is not a FragmentActivity")
    var currentFailedAttempts = failedAttempts
    return BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_CANCELED) {
                onAuthenticationResult(false, currentFailedAttempts, true)
            } else {
                onAuthenticationResult(false, currentFailedAttempts, false)
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthenticationResult(true, currentFailedAttempts, false)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            currentFailedAttempts++
            onAuthenticationResult(false, currentFailedAttempts, false)
        }
    })
}

private fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
}

private fun checkBiometricAvailability(context: Context, onBiometricChecked: (Boolean) -> Unit) {
    val biometricManager = BiometricManager.from(context)
    onBiometricChecked(biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS)
}

private fun createPromptInfo(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
        .setTitle("Quod")
        .setSubtitle("Usar proteção para acessar")
        .setNegativeButtonText("Cancelar")
        .build()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DigitalBiometricsScreenPreview() {
    val navController = rememberNavController()
    DigitalBiometricsScreen(navController)
}