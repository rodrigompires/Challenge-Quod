package br.com.fiap.desafioquod.screens.documentcospy

import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.CustomButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.GreenQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import com.bumptech.glide.Glide
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.Dialog
import br.com.fiap.desafioquod.screens.facialbiometrics.ProgressBar
import br.com.fiap.desafioquod.ui.theme.RedError
import br.com.fiap.desafioquod.ui.theme.backBar
import br.com.fiap.desafioquod.ui.theme.backBoxBar
import br.com.fiap.desafioquod.ui.theme.highColor
import br.com.fiap.desafioquod.ui.theme.lowColor
import br.com.fiap.desafioquod.ui.theme.mediumHighColor
import br.com.fiap.desafioquod.ui.theme.mediumLowColor
import br.com.fiap.desafioquod.utils.coordinatesFraudMessage
import br.com.fiap.desafioquod.utils.docCoordinatesFraudMessage
import br.com.fiap.desafioquod.utils.docErrorFraudMessage
import br.com.fiap.desafioquod.utils.docErrorMessage
import br.com.fiap.desafioquod.utils.docFraudMessage
import br.com.fiap.desafioquod.utils.failedDocMessage
import br.com.fiap.desafioquod.utils.successMessageDoc
import kotlinx.coroutines.delay


@Composable
fun DocumentcospyValidationScreen(navController: NavController) {
    val backStackEntry = try {
        navController.getBackStackEntry("imagepreviewdocscreen/{selectedDocumentType}/{frontImageUri}/{backImageUri}/{latitudeFront}/{longitudeFront}/{latitudeBack}/{longitudeBack}")
    } catch (e: IllegalArgumentException) {
        Log.e("ValidationScreen", "Rota anterior não encontrada: ${e.message}")
        navController.navigate("home")
        return
    }
    val viewModel: ImagePreviewDocViewModel = viewModel(viewModelStoreOwner = backStackEntry)
    val isProcessing by viewModel.isProcessing
    val validationResult by viewModel.validationResult
    val docScore = viewModel.docScore
    var showDetailsDialog by remember { mutableStateOf(false) }
    var animationId by remember { mutableStateOf(0) }

    val progress = remember(animationId) { Animatable(0f) }
    LaunchedEffect(docScore, showDetailsDialog, animationId) {
        if (docScore != null && showDetailsDialog) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = (docScore).toFloat().coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
            )
        }
    }
    val animatedDocPercentage = (progress.value * 100).toInt()

    val processingSteps = listOf(
        "Enviando documentos...",
        "Analisando texto do documento...",
        "Verificando regiões geográficas...",
        "Validando núcleos e padrões visíveis...",
        "Concluindo análise"
    )
    var currentStep by remember { mutableStateOf(0) }
    var dotsCount by remember { mutableStateOf(0) }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            while (isProcessing) {
                Log.d("ValidationScreen", "currentStep=$currentStep, dotsCount=$dotsCount")
                if (currentStep < processingSteps.size - 1) {
                    delay(1400)
                    currentStep += 1
                    delay(300)
                } else {
                    dotsCount = (dotsCount % 4) + 1
                    delay(500)
                }
            }
        } else {
            currentStep = 0
            dotsCount = 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteQuod)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp, start = 16.dp, bottom = 20.dp, end = 16.dp)
                .background(WhiteQuod),
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
                    withStyle(style = SpanStyle(color = BlackQuod)) { append("Documentoscopia") }
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
            Card(
                colors = CardDefaults.cardColors(containerColor = WhiteQuod),
                modifier = Modifier
                    .size(220.dp)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isProcessing) {
                        Log.d("ValidationScreen", "Exibindo GIF de carregamento")
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
                        val message = validationResult ?: "Erro: Resultado não disponível"
                        val (imageRes, contentDescription) = when {
                            message.contains("Sem Fraude", ignoreCase = true) -> Pair(
                                R.drawable.ok,
                                "Validação bem-sucedida"
                            )
                            message.contains("Coordenadas Discrepantes", ignoreCase = true) -> Pair(
                                R.drawable.coordinates_fraud,
                                "Validação falha: Coordenadas discrepantes"
                            )
                            message.contains("Documento inválido - Não é um", ignoreCase = true) -> Pair(
                                R.drawable.doc_fraud,
                                "Validação falha: Documento inválido"
                            )
                            message.contains("Coordenadas e Documento discrepantes", ignoreCase = true) -> Pair(
                                R.drawable.doc_fraud,
                                "Validação falha: Coordenadas e documento discrepantes"
                            )
                            message.contains("Documento incorreto - Esperado", ignoreCase = true) -> Pair(
                                R.drawable.doc_fraud,
                                "Validação falha: Documento incorreto"
                            )
                            message.contains("Coordenadas Inválidas", ignoreCase = true) -> Pair(
                                R.drawable.coordinates_fraud,
                                "Validação falha: Coordenadas inválidas"
                            )
                            else -> Pair(
                                R.drawable.error,
                                "Validação falha: Erro desconhecido - $message"
                            )
                        }
                        Log.d("ValidationScreen", "Exibindo resultado: imageRes=$imageRes, contentDescription=$contentDescription")
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = contentDescription,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    val displayText = if (currentStep == processingSteps.size - 1) {
                        "Concluindo análise" + ".".repeat(dotsCount)
                    } else {
                        processingSteps[currentStep]
                    }
                    Text(
                        text = displayText,
                        fontFamily = recursiveFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = GreenQuod,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                    )
                } else {
                    val message = validationResult ?: "Erro: Resultado não disponível"
                    Log.d("ValidationScreen", "Gerando texto para exibição: $message")
                    val textToDisplay = when {
                        message.contains("Sem Fraude", ignoreCase = true) -> buildAnnotatedString {
                            withStyle(style = SpanStyle(color = PurpleQuod, fontWeight = FontWeight.Bold)) {
                                append(successMessageDoc)
                            }
                        }
                        message.contains("Coordenadas Discrepantes", ignoreCase = true) -> buildAnnotatedString {
                            withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                append("Possível Fraude Detectada!\n\n")
                            }
                            withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                append("Coordenadas discrepantes")
                            }
                        }
                        message.contains("Documento inválido - Não é um", ignoreCase = true) -> {
                            val docType = message.substringAfter("Não é um ").trim().replace("\"", "")
                            Log.d("ValidationScreen", "docType: $docType")
                            val annotatedString = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                    append("Possível Fraude Detectada!\n\n")
                                }
                                withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                    append("Documento inválido: Não é um ")
                                }
                                withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                    append(docType)
                                }
                                withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                    append(".")
                                }
                            }
                            Log.d("ValidationScreen", "AnnotatedString for Documento inválido: ${annotatedString.text}")
                            annotatedString
                        }
                        message.contains("Coordenadas e Documento discrepantes", ignoreCase = true) -> {
                            val docType = message.substringAfter("Não é um ").trim().replace("\"", "")
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                    append("Possível Fraude Detectada!\n\n")
                                }
                                withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                    append("Coordenadas e documentos discrepantes: Não é um ")
                                }
                                withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                    append(docType)
                                }
                                withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                    append(".")
                                }
                            }
                        }
                        message.contains("Documento incorreto - Esperado", ignoreCase = true) -> {
                            val normalizedMessage = message.replace("\\s+".toRegex(), " ").trim()
                            val regex = Regex("Documento incorreto - Esperado \"?(\\w+)\"?, identificado como (\\w+)")
                            val matchResult = regex.find(normalizedMessage)
                            val expectedType = matchResult?.groups?.get(1)?.value?.replace("\"", "") ?: "Desconhecido"
                            val identifiedType = matchResult?.groups?.get(2)?.value?.replace("\"", "") ?: "Desconhecido"
                            Log.d("ValidationScreen", "expectedType: $expectedType, identifiedType: $identifiedType")
                            val annotatedString = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                    append("Possível Fraude Detectada!\n\n")
                                }
                                withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Bold)) {
                                    append("Falha na validação dos documentos: ")
                                }
                                withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                    append("Esperado: ")
                                }
                                withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                    append(expectedType)
                                }
                                withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                    append(", identificado como ")
                                }
                                withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                    append(identifiedType)
                                }
                            }
                            Log.d("ValidationScreen", "AnnotatedString for Documento incorreto: ${annotatedString.text}")
                            annotatedString
                        }
                        message.contains("Coordenadas Inválidas", ignoreCase = true) -> buildAnnotatedString {
                            withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                                append("Possível Fraude Detectada!\n\n")
                            }
                            withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                append("Coordenadas inválidas exibidas")
                            }
                        }
                        else -> buildAnnotatedString {
                            withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Medium)) {
                                append("Erro desconhecido: $message")
                            }
                        }
                    }
                    Text(
                        text = textToDisplay,
                        fontFamily = recursiveFontFamily,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (!isProcessing) {
                Text(
                    text = "Detalhes",
                    fontFamily = recursiveFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = PurpleQuod,
                    modifier = Modifier
                        .clickable {
                            if (!showDetailsDialog) {
                                animationId++
                            }
                            showDetailsDialog = true
                        }
                        .padding(8.dp),
                    textDecoration = TextDecoration.Underline
                )
                Spacer(modifier = Modifier.height(30.dp))
                CustomButton(
                    modifier = Modifier.width(130.dp),
                    color = Color.Transparent,
                    borderWith = 0.5.dp,
                    borderColor = GreenQuod,
                    onClick = { navController.navigate("documentcospy") },
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
        if (showDetailsDialog && !isProcessing) {
            Dialog(onDismissRequest = { showDetailsDialog = false }) {
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteQuod)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Detalhes da Análise",
                            fontFamily = recursiveFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlackQuod,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Mensagem: ")
                                }
                                append(validationResult ?: "Nenhuma mensagem disponível")
                            },
                            fontFamily = recursiveFontFamily,
                            fontSize = 14.sp,
                            color = BlackQuod,
                            textAlign = TextAlign.Left
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Resultado: ")
                                }
                                append(if (validationResult?.contains("Sem Fraude", ignoreCase = true) == true) "Válido" else "Inválido")
                            },
                            fontFamily = recursiveFontFamily,
                            fontSize = 14.sp,
                            color = BlackQuod,
                            textAlign = TextAlign.Left
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        docScore?.let { score ->
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Pontuação de Confiança: ")
                                    }
                                    append("$animatedDocPercentage%")
                                },
                                fontFamily = recursiveFontFamily,
                                fontSize = 14.sp,
                                color = PurpleQuod,
                                textAlign = TextAlign.Left
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CustomProgressBar(animatedProgress = progress.value)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showDetailsDialog = false },
                            colors = ButtonDefaults.buttonColors(BlackQuod),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
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

@Composable
fun CustomProgressBar(animatedProgress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .background(
                backBoxBar,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(backBar)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            when {
                                animatedProgress <= 0.3f -> animatedProgress / 0.3f
                                else -> 1f
                            }
                        )
                        .background(lowColor)
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(backBar)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            when {
                                animatedProgress <= 0.3f -> 0f
                                animatedProgress <= 0.5f -> (animatedProgress - 0.3f) / 0.2f
                                else -> 1f
                            }
                        )
                        .background(mediumLowColor)
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(backBar)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            when {
                                animatedProgress <= 0.5f -> 0f
                                animatedProgress <= 0.7f -> (animatedProgress - 0.5f) / 0.2f
                                else -> 1f
                            }
                        )
                        .background(mediumHighColor)
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(backBar)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            when {
                                animatedProgress <= 0.7f -> 0f
                                else -> (animatedProgress - 0.7f) / 0.3f
                            }
                        )
                        .background(highColor)
                )
            }
        }
    }
}