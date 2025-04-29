package br.com.fiap.desafioquod.screens.digitalbiometrics

import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.CustomButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.screens.documentcospy.ImagePreviewDocViewModel
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.GreenQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.RedError
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import br.com.fiap.desafioquod.utils.connectionErrorMessage
import br.com.fiap.desafioquod.utils.errorMessage
import br.com.fiap.desafioquod.utils.failureMessage
import br.com.fiap.desafioquod.utils.fraudDetectedMessage
import br.com.fiap.desafioquod.utils.serverErrorMessage
import br.com.fiap.desafioquod.utils.successMessage
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import java.net.URLDecoder


@Composable
fun DigitalBiometricsValidationScreen(
    navController: NavController,
    status: String?,
    analysisReport: String? = null,
    message: String? = null
) {
    var isProcessing by remember { mutableStateOf(true) }
    val dots = remember { mutableStateOf("") }
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Log para verificar os argumentos recebidos
    Log.d("DigitalBiometricsValidation", "Received - status: $status, analysisReport: $analysisReport, message: $message")

    // Log para inspecionar o conteúdo de message
    val decodedMessage = message?.let { URLDecoder.decode(it, "UTF-8").trim() } ?: ""
    Log.d("DigitalBiometricsValidation", "Decoded message content: '$decodedMessage'")

    LaunchedEffect(isProcessing) {
        while (isProcessing) {
            dots.value = ""
            delay(300)
            dots.value = "."
            delay(300)
            dots.value = ".."
            delay(300)
            dots.value = "..."
            delay(300)
        }
    }

    LaunchedEffect(Unit) {
        delay(1500)
        isProcessing = false
    }

    // Log para monitorar o estado do Dialog
    LaunchedEffect(showDetailsDialog) {
        Log.d("DigitalBiometricsValidation", "showDetailsDialog changed to: $showDetailsDialog")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteQuod)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, start = 16.dp, bottom = 20.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(
                iconResId = R.drawable.hbmenu,
                onMenuClick = { navController.navigate("home") },
                iconTint = GrayQuod
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

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        200f,
                                        context.resources.displayMetrics
                                    ).toInt(),
                                    TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        200f,
                                        context.resources.displayMetrics
                                    ).toInt()
                                )
                            }
                        },
                        update = { imageView ->
                            Glide.with(imageView.context)
                                .asGif()
                                .load(R.drawable.loadingscreens)
                                .into(imageView)
                        }
                    )
                } else {
                    val (imageRes, contentDescription) = when (status) {
                        "success" -> Pair(R.drawable.ok, "Autenticação bem-sucedida")
                        "failure", "fraud_detected" -> {
                            when {
                                decodedMessage.contains("Movimento suspeito", ignoreCase = true) -> Pair(
                                    R.drawable.running_fraud,
                                    "Movimento suspeito detectado"
                                )
                                decodedMessage.contains("Dispositivo inconsistente", ignoreCase = true) -> Pair(
                                    R.drawable.device_fraud,
                                    "Dispositivo não reconhecido"
                                )
                                decodedMessage.contains("Muitas tentativas falhas", ignoreCase = true) -> Pair(
                                    R.drawable.digital_fraud,
                                    "Muitas tentativas falhas"
                                )
                                else -> Pair(R.drawable.digital_fraud, fraudDetectedMessage)
                            }
                        }
                        "serverError" -> Pair(R.drawable.error, "Erro no servidor")
                        "connectionError" -> Pair(R.drawable.error, "Erro de conexão")
                        else -> Pair(R.drawable.error, "Erro desconhecido")
                    }
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = contentDescription,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val textToDisplay = if (isProcessing) {
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = GrayQuod)) {
                        append("Validando biometria${dots.value}")
                    }
                }
            } else {
                when (status) {
                    "success" -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = PurpleQuod)) {
                            append("Autenticação bem-sucedida")
                        }
                    }
                    "failure" -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Falha na Validação!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append("Falha na autenticação")
                        }
                    }
                    "fraud_detected" -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Possível Fraude Detectada!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            when {
                                decodedMessage.contains("Movimento suspeito", ignoreCase = true) -> {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Movimento Suspeito Detectado")
                                    }
                                    append(". \nSua localização mudou drasticamente.")
                                }
                                decodedMessage.contains("Dispositivo inconsistente", ignoreCase = true) -> {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Dispositivo não Reconhecido")
                                    }
                                    append(". \nAcesso bloqueado.")
                                }
                                decodedMessage.contains("Muitas tentativas falhas", ignoreCase = true) -> {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Digital não reconhecida!")
                                    }
                                    append(" \nMuitas tentativas falhas. \nAcesso temporariamente bloqueado.")
                                }
                                decodedMessage.contains("Geolocalização:") -> {
                                    // Evitar renderizar relatório na tela principal
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Possível irregularidade detectada")
                                    }
                                    append(". \nConsulte os detalhes para mais informações.")
                                }
                                else -> append(fraudDetectedMessage.split("\n\n").joinToString("\n"))
                            }
                        }
                    }
                    "serverError" -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Erro na Validação!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append("Erro no servidor")
                        }
                    }
                    "connectionError" -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Erro na Validação!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append("Erro de conexão")
                        }
                    }
                    else -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Erro na Validação!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append("Erro desconhecido")
                        }
                    }
                }
            }

            Text(
                text = textToDisplay,
                fontFamily = recursiveFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mostrar botão "Detalhes" apenas se analysisReport não for nulo ou vazio
            if (!isProcessing && analysisReport != null && analysisReport.isNotEmpty()) {
                Text(
                    text = "Detalhes",
                    fontFamily = recursiveFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleQuod,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showDetailsDialog = true }
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            if (!isProcessing) {
                CustomButton(
                    modifier = Modifier.width(130.dp),
                    color = Color.Transparent,
                    borderWith = 0.5.dp,
                    borderColor = GreenQuod,
                    onClick = { navController.navigate("digital") },
                    cornerRadius = 10.dp,
                    textStyle = TextStyle(
                        color = GreenQuod,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    buttonText = "↩ Voltar"
                )
            }
        }

        if (showDetailsDialog && analysisReport != null && analysisReport.isNotEmpty()) {
            Dialog(onDismissRequest = { showDetailsDialog = false }) {
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteQuod)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Detalhes da Análise",
                            fontFamily = recursiveFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleQuod,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val decodedReport = try {
                            URLDecoder.decode(analysisReport, "UTF-8")
                        } catch (e: Exception) {
                            Log.e("DigitalBiometricsValidation", "Error decoding analysisReport: ${e.message}")
                            analysisReport
                        }
                        Log.d("DigitalBiometricsValidation", "Rendering analysisReport: $decodedReport")
                        val parts = decodedReport.split(" | ")
                        Text(
                            text = buildAnnotatedString {
                                if (parts.size >= 3) {
                                    withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Bold)) {
                                        append("Geolocalização: ")
                                    }
                                    withStyle(style = SpanStyle(color = BlackQuod)) {
                                        append(parts[0].substringAfter("Geolocalização: ") + "\n")
                                    }
                                    withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Bold)) {
                                        append("Dispositivo: ")
                                    }
                                    withStyle(style = SpanStyle(color = BlackQuod)) {
                                        append(parts[1].substringAfter("Dispositivo: ") + "\n")
                                    }
                                    withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Bold)) {
                                        append("Tentativas falhas: ")
                                    }
                                    withStyle(style = SpanStyle(color = BlackQuod)) {
                                        append(parts[2].substringAfter("Tentativas falhas: "))
                                    }
                                } else {
                                    withStyle(style = SpanStyle(color = BlackQuod)) {
                                        append(decodedReport)
                                    }
                                }
                            },
                            fontFamily = recursiveFontFamily,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showDetailsDialog = false },
                            colors = ButtonDefaults.buttonColors(BlackQuod)
                        ) {
                            Text(
                                text = "Fechar",
                                fontFamily = recursiveFontFamily,
                                color = WhiteQuod
                            )
                        }
                    }
                }
            }
        }
    }
}