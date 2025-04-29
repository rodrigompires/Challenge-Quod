package br.com.fiap.desafioquod.screens.facialbiometrics

import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.CustomButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.GreenQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.RedError
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.backBar
import br.com.fiap.desafioquod.ui.theme.backBoxBar
import br.com.fiap.desafioquod.ui.theme.highColor
import br.com.fiap.desafioquod.ui.theme.lowColor
import br.com.fiap.desafioquod.ui.theme.mediumHighColor
import br.com.fiap.desafioquod.ui.theme.mediumLowColor
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import br.com.fiap.desafioquod.utils.coordinatesFacialFraudMessage
import br.com.fiap.desafioquod.utils.facialCoordinatesFacialFraudMessage
import br.com.fiap.desafioquod.utils.facialFraudMessage
import br.com.fiap.desafioquod.utils.successMessageFacial
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun FacialBiometricsValidationScreen(navController: NavController, imageName: String? = null) {
    val backStackEntry = navController.getBackStackEntry("imagePreview/{firstUri}/{secondUri}/{firstLat}/{firstLon}/{secondLat}/{secondLon}")
    val viewModel: ImagePreviewViewModel = viewModel(viewModelStoreOwner = backStackEntry)

    val args = backStackEntry.arguments
    val firstUri = Uri.encode(args?.getString("firstUri") ?: "")
    val secondUri = Uri.encode(args?.getString("secondUri") ?: "")
    val firstLat = Uri.encode(args?.getString("firstLat") ?: "")
    val firstLon = Uri.encode(args?.getString("firstLon") ?: "")
    val secondLat = Uri.encode(args?.getString("secondLat") ?: "")
    val secondLon = Uri.encode(args?.getString("secondLon") ?: "")

    // Lista de passos de processamento
    val processingSteps = listOf(
        "Enviando imagens...",
        "Analisando características faciais...",
        "Verificando coordenadas geográficas...",
        "Comparando similaridade facial...",
        "Concluindo análise"
    )

    var currentStep by remember { mutableStateOf(0) }

    val dots = remember { mutableStateOf("") }

    LaunchedEffect(viewModel.isProcessing) {
        if (viewModel.isProcessing) {
            currentStep = 0

            while (currentStep < processingSteps.size - 1) {
                delay(2000)
                currentStep++
            }
        } else {
            currentStep = 0
        }
    }

    LaunchedEffect(viewModel.isProcessing, currentStep) {
        if (viewModel.isProcessing && currentStep == processingSteps.size - 1) {
            while (true) {
                dots.value = ""
                delay(300)
                dots.value = "."
                delay(300)
                dots.value = ".."
                delay(300)
                dots.value = "..."
                delay(300)
            }
        } else {
            dots.value = ""
        }
    }

    var showDetailsDialog by remember { mutableStateOf(false) }

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
                    withStyle(style = SpanStyle(color = BlackQuod)) { append("Biometria Facial") }
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
                    if (viewModel.isProcessing) {
                        Log.d("ValidationScreen", "Exibindo GIF de carregamento")
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, context.resources.displayMetrics).toInt(),
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, context.resources.displayMetrics).toInt()
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
                        val result = viewModel.validationResult
                        val fraudType = result?.fraudType ?: ""
                        val (imageRes, contentDescription) = when {
                            result?.message?.contains("Similares", ignoreCase = true) == true -> Pair(
                                R.drawable.ok,
                                "Validação bem-sucedida"
                            )
                            fraudType == "Faces Diferentes" -> Pair(
                                R.drawable.facial_fraud,
                                "Validação falhou: Imagens faciais não similares"
                            )
                            fraudType == "Coordenadas Diferentes" -> Pair(
                                R.drawable.coordinates_fraud,
                                "Validação falhou: Coordenadas discrepantes"
                            )
                            result?.message?.contains("Diferentes", ignoreCase = true) == true -> Pair(
                                R.drawable.facial_coordinates_fraud,
                                "Validação falhou: Faces ou coordenadas discrepantes"
                            )
                            result?.message?.contains("Erro", ignoreCase = true) == true -> Pair(
                                R.drawable.error,
                                "Validação falhou: Erro no processamento"
                            )
                            else -> Pair(
                                R.drawable.error,
                                "Validação falhou: Resultado desconhecido"
                            )
                        }
                        Log.d("ValidationScreen", "Exibindo resultado: imageRes=$imageRes, fraudType=$fraudType, similarityScore=${result?.similarityScore}")
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = contentDescription,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (viewModel.isProcessing) {
                Text(
                    text = processingSteps[currentStep] + if (currentStep == processingSteps.size - 1) dots.value else "",
                    fontFamily = recursiveFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = GreenQuod,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        .padding(horizontal = 16.dp)
                )
            } else {
                val result = viewModel.validationResult
                val fraudType = result?.fraudType ?: ""
                val textToDisplay: AnnotatedString = when {
                    result?.message?.contains("Similares", ignoreCase = true) == true -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = PurpleQuod)) {
                            append(successMessageFacial)
                        }
                    }
                    fraudType == "Faces Diferentes" -> buildAnnotatedString {
                        val parts = facialFraudMessage.split("Falha na validação das imagens: \n\n")
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Possível Fraude Detectada!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Bold)) {
                            append("Falha na validação das imagens: \n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append(parts.getOrNull(1) ?: "Faces diferentes detectadas.")
                        }
                    }
                    fraudType == "Coordenadas Diferentes" -> buildAnnotatedString {
                        val parts = coordinatesFacialFraudMessage.split("Falha na validação das imagens: \n")
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Possível Fraude Detectada!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Bold)) {
                            append("Falha na validação das imagens: \n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append(parts.getOrNull(1) ?: "Coordenadas discrepantes detectadas.")
                        }
                    }
                    result?.message?.contains("Diferentes", ignoreCase = true) == true -> buildAnnotatedString {
                        val parts = facialCoordinatesFacialFraudMessage.split("Possível Fraude Detectada! \n\n")
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Possível Fraude Detectada!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod, fontWeight = FontWeight.Bold)) {
                            append("Falha na validação das imagens: \n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append(parts.getOrNull(1) ?: "Faces ou coordenadas discrepantes.")
                        }
                    }
                    result?.message?.contains("Erro", ignoreCase = true) == true -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = RedError, fontWeight = FontWeight.Bold)) {
                            append("Erro na Validação!\n\n")
                        }
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append(result.message ?: "Erro desconhecido.")
                        }
                    }
                    else -> buildAnnotatedString {
                        withStyle(style = SpanStyle(color = BlackQuod)) {
                            append("Falha na validação das imagens: Resultado desconhecido.")
                        }
                    }
                }

                Text(
                    text = textToDisplay,
                    fontFamily = recursiveFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!viewModel.isProcessing) {
                Text(
                    text = "Detalhes",
                    fontFamily = recursiveFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = PurpleQuod,
                    modifier = Modifier
                        .clickable { showDetailsDialog = true }
                        .padding(8.dp),
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (!viewModel.isProcessing) {
                CustomButton(
                    modifier = Modifier.width(130.dp),
                    color = Color.Transparent,
                    borderWith = 0.5.dp,
                    borderColor = GreenQuod,
                    onClick = {
                        Log.d("ValidationScreen", "Botão Voltar clicado")
                        val route = "imagePreview/$firstUri/$secondUri/$firstLat/$firstLon/$secondLat/$secondLon"
                        navController.navigate(route) {
                            popUpTo("facialvalidation") { inclusive = true }
                        }
                    },
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

        LaunchedEffect(viewModel.isProcessing, viewModel.validationResult) {
            Log.d("ValidationScreen", "Estado alterado: isProcessing=${viewModel.isProcessing}, validationResult=${viewModel.validationResult}")
        }

        if (showDetailsDialog && !viewModel.isProcessing) {
            val result = viewModel.validationResult
            Dialog(onDismissRequest = { showDetailsDialog = false }) {
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteQuod)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.Start // Alinhamento padrão à esquerda para textos do backend
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
                                    append("Status de Fraude: ")
                                }
                                append(result?.fraudType ?: "Desconhecido")
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
                                    append("Confirmação de Coordenadas: ")
                                }
                                append(if (result?.areCoordinatesEqual == true) "Iguais" else "Diferentes")
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
                                    append("Distância Euclidiana: ")
                                }
                                append(result?.euclideanDistance?.let { String.format("%.4f", it) } ?: "N/A")
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
                                    append("Detalhes do Dispositivo: ")
                                }
                                append(result?.deviceInfo ?: "Desconhecido")
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
                                    append("Mensagem: ")
                                }
                                append(result?.message ?: "Nenhuma mensagem disponível")
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
                                append(if (result?.message?.contains("Similares", ignoreCase = true) == true) "Similares" else "Diferentes")
                            },
                            fontFamily = recursiveFontFamily,
                            fontSize = 14.sp,
                            color = BlackQuod,
                            textAlign = TextAlign.Left
                        )
                        Spacer(modifier = Modifier.height(50.dp))
                        val similarityScore = result?.similarityScore ?: 0.0
                        var percentageTarget by remember { mutableStateOf(0) }
                        val animatedPercentage by animateIntAsState(
                            targetValue = percentageTarget,
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = FastOutSlowInEasing
                            ),
                            label = "PercentageAnimation"
                        )
                        LaunchedEffect(showDetailsDialog, similarityScore) {
                            Log.d("ValidationScreen", "Animations started")
                            percentageTarget = (similarityScore * 100).toInt()
                        }
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Similaridade Facial: ")
                                }
                                append(if (similarityScore > 0) "$animatedPercentage%" else "Não disponível")
                            },
                            fontFamily = recursiveFontFamily,
                            fontSize = 18.sp,
                            color = PurpleQuod,
                            textAlign = TextAlign.Left
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProgressBar(score = (similarityScore * 100).toInt())
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showDetailsDialog = false },
                            colors = ButtonDefaults.buttonColors(BlackQuod),
                            modifier = Modifier.align(Alignment.CenterHorizontally) // Centraliza o botão
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
fun ProgressBar(score: Int) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        progress.animateTo(
            targetValue = score / 100f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    val animatedProgress = progress.value
//    val animatedScore = (animatedProgress * 100).toInt()

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
            modifier = Modifier
                .fillMaxSize(),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FacialBiometricsValidationScreenPreview() {
    val navController = rememberNavController()
    FacialBiometricsValidationScreen(navController)
}
