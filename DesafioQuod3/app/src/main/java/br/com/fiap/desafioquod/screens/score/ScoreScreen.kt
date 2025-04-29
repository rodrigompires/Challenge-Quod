package br.com.fiap.desafioquod.screens.score

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.CustomButton
import br.com.fiap.desafioquod.components.DraggableButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily

fun formatCPF(cpf: String): String {
    // Remove caracteres não numéricos
    val numbers = cpf.replace("\\D".toRegex(), "")

    // Formata o CPF
    return when {
        numbers.length <= 3 -> numbers
        numbers.length <= 6 -> numbers.substring(0, 3) + "." + numbers.substring(3)
        numbers.length <= 9 -> numbers.substring(0, 3) + "." + numbers.substring(3, 6) + "." + numbers.substring(6)
        numbers.length <= 11 -> numbers.substring(0, 3) + "." + numbers.substring(3, 6) + "." + numbers.substring(6, 9) + "-" + numbers.substring(9)
        else -> numbers.substring(0, 11)
    }
}


@Composable
fun ScoreScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var cpfText by remember { mutableStateOf(TextFieldValue("")) }
    val isButtonEnabled = cpfText.text.length == 14
    var classificacao by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Função de formatação do CPF
    fun formatCPF(cpf: String): String {
        val numbers = cpf.replace("\\D".toRegex(), "")
        return when {
            numbers.length <= 3 -> numbers
            numbers.length <= 6 -> "${numbers.substring(0, 3)}.${numbers.substring(3)}"
            numbers.length <= 9 -> "${numbers.substring(0, 3)}.${
                numbers.substring(
                    3,
                    6
                )
            }.${numbers.substring(6)}"

            numbers.length <= 11 -> "${numbers.substring(0, 3)}.${
                numbers.substring(
                    3,
                    6
                )
            }.${numbers.substring(6, 9)}-${numbers.substring(9)}"

            else -> numbers.substring(0, 11)
        }
    }

    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(WhiteQuod)
    ) {

        DraggableButton(
            bgColor = PurpleQuod,
            onClick = {
                showDialog = true
            },
            //modifier = Modifier.align(Alignment.CenterEnd)
        )

        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                //.padding(top = 30.dp, start = 16.dp, bottom = 20.dp, end = 16.dp)
                .background(WhiteQuod),
            //.background(Color(0Xffededed)),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            //verticalArrangement = Arrangement.Center
        ) {
//            Componente Header
            Header(
                iconResId = R.drawable.hbmenu,
                onMenuClick = {
                    navController.navigate("home")
                },
                iconTint = GrayQuod
            )
//            Fim Componente Header

            Spacer(modifier = Modifier.Companion.height(50.dp))

            Text(
                modifier = Modifier.Companion.fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append("_")
                    }
                    withStyle(style = SpanStyle(color = BlackQuod)) {
                        append("Cálculo de Score")
                    }
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append(".")
                    }
                },
                fontFamily = recursiveFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Companion.Normal,
                textAlign = TextAlign.Companion.Center,
                color = GrayQuod,
                style = TextStyle(
                    lineHeight = 38.sp
                )
            )

            Spacer(modifier = Modifier.Companion.height(100.dp))


            OutlinedTextField(
                value = cpfText,
                onValueChange = { newText ->
                    val formattedCpf = formatCPF(newText.text)
                    cpfText =
                        TextFieldValue(formattedCpf, selection = TextRange(formattedCpf.length))
                    if (formattedCpf.length == 14) { // Considerando que o CPF formatado tem 14 caracteres ("xxx.xxx.xxx-xx")
                        keyboardController?.hide() // Fecha o teclado
                    }
                },
                label = { Text("CPF", fontFamily = recursiveFontFamily, color = PurpleQuod) },
                placeholder = {
                    Text("Didite o seu CPF", fontFamily = recursiveFontFamily, color = PurpleQuod)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BlackQuod,
                    focusedBorderColor = PurpleQuod,
                    focusedTextColor = BlackQuod,
                    unfocusedTextColor = BlackQuod,
                    unfocusedContainerColor = WhiteQuod,
                    focusedContainerColor = WhiteQuod,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
                textStyle = TextStyle(
                    fontFamily = recursiveFontFamily,
                    fontSize = 16.sp,
                    color = BlackQuod,
                    fontWeight = FontWeight.Companion.Bold
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.Companion.height(10.dp))
            var score by remember { mutableStateOf(0) }

            CustomButton(
                modifier = Modifier.Companion
                    .padding(top = 16.dp)
                    .width(130.dp),
                color = PurpleQuod,
                borderWith = 0.5.dp,
                borderColor = Color.Companion.Transparent,
                onClick = {
                    val (newScore, newClassificacao) = calcularScoreSimplesCPF(cpfText.text)
                    score = newScore
                    classificacao = newClassificacao
                },
                cornerRadius = 10.dp,
                textStyle = TextStyle(
                    color = WhiteQuod,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Companion.Medium
                ),
                buttonText = "Calcular",
                enabled = isButtonEnabled
            )

            Spacer(modifier = Modifier.Companion.height(10.dp))

            // Estado para armazenar o score animado
            //var animatedScore by remember { mutableStateOf(score) }

            // Animação do valor do score, para que ele mude suavemente
            val displayedScore by animateIntAsState(
                targetValue = score,
                animationSpec = tween(
                    durationMillis = 1000, // Duração da animação em milissegundos
                    easing = FastOutSlowInEasing // Usando um easing para suavizar a animação
                )
            )

            Card(
                modifier = Modifier.Companion
                    //.width(200.dp)
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteQuod),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(width = 0.2.dp, color = PurpleQuod)
            ) {
                Box( // Ensures content fills the card
                    modifier = Modifier.Companion.fillMaxSize().padding(20.dp),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Column(
                        modifier = Modifier.Companion.fillMaxSize()
                    ) {

                        Row(
                            modifier = Modifier.Companion.fillMaxWidth()
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            color = BlackQuod,
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Companion.Bold
                                        )
                                    ) {
                                        append("$displayedScore")
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color = BlackQuod,
                                            fontSize = 24.sp
                                        )
                                    ) {
                                        append(" de 1000")
                                    }
                                },
                                //"$displayedScore de 1000",
                                fontFamily = recursiveFontFamily,
                                fontWeight = FontWeight.Companion.Normal,

                                //color = BlackQuod
                            )
                        }
                        Spacer(modifier = Modifier.Companion.height(10.dp))
                        ProgressBar(score = score)
                        //ScorePieChart(score, classificacao)
                        Spacer(modifier = Modifier.Companion.height(10.dp))

                        Row(
                            modifier = Modifier.Companion.fillMaxWidth()
                        ) {
                            Text(
                                "$classificacao",
                                fontFamily = recursiveFontFamily,
                                fontWeight = FontWeight.Companion.Normal,
                                fontSize = 20.sp,
                                color = BlackQuod
                            )
                        }

                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth(),
                            //.border(width = 1.dp, PurpleQuod),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.score_table),
                                contentDescription = null,
                                modifier = Modifier.Companion.size(500.dp)
                            )
                        }
                    }
                }
            }
        }

//        CircularProgressIndicator()
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Card(
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Companion.White)
                ) {
                    Column(
                        modifier = Modifier.Companion.padding(16.dp),
                        horizontalAlignment = Alignment.Companion.CenterHorizontally
                    ) {
                        Text(
                            text = "O cálculo do score de CPF é uma pontuação que reflete a sua saúde financeira, considerando seu " +
                                    "histórico de pagamentos, dívidas e outros fatores. Essa pontuação é usada " +
                                    "por bancos e empresas para avaliar o risco de conceder crédito.",
                            fontWeight = FontWeight.Companion.Bold,
                            fontFamily = recursiveFontFamily,
                            textAlign = TextAlign.Companion.Center,
                            color = Color.Companion.Black
                        )
                        Spacer(modifier = Modifier.Companion.height(8.dp))
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


fun calcularScoreSimplesCPF(cpf: String): Pair<Int, String> {
    // Remove caracteres não numéricos
    val numbers = cpf.replace("\\D".toRegex(), "")

    // Verifica se o CPF tem 11 dígitos
    if (numbers.length != 11) {
        return Pair(0, "Inválido") // Retorna 0 e "Inválido" se não tiver 11 dígitos
    }

    // Calcula a soma dos dígitos
    val somaDosDigitos = numbers.sumOf { it.digitToInt() }

    // Normaliza o score para uma faixa de 0 a 1.000
    val score = (somaDosDigitos * 1000) / 99 // 90 é a soma máxima dos dígitos (9*10)

    // Classificação baseada no score
    val classificacao = when (score) {
        in 0..300 -> "Seu score está muito baixo"
        in 301..500 -> "Seu score está baixo"
        in 501..700 -> "Seu score está bom"
        in 701..1000 -> "Seu score está excelente"
        else -> "inválido"
    }

    return Pair(score, classificacao)
}


@Composable
fun ProgressBar(score: Int) {
    val maxScore = 1000
    //val animatedScore = animateFloatAsState(targetValue = score / maxScore.toFloat())

    val animatedScore = animateFloatAsState(
        targetValue = score / maxScore.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow,
            //viscosity = 1000f
        )
    )

    val color = when {
        score in 0..300 -> Color(0xFFd99d9f) // Score muito baixo
        score in 301..500 -> Color(0xFFdcb229) // Score baixo
        score in 501..700 -> Color(0xFFdcb229) // Score bom
        score in 701..1000 -> Color(0xFF53bc5f) // Score excelente
        else -> Color(0xFFcccccc) // Cor padrão para valores fora do intervalo
    }

    Box(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(18.dp)
            .background(
                Color(0x88ECE9E9),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth(animatedScore.value)
                .height(18.dp)
                .background(
                    color,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .animateContentSize()
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScoreScreenPreview() {
    val navController = rememberNavController()
    ScoreScreen(navController)

}











//@Composable
//fun ScorePieChart(score: Int, classificacao: String) {
//    // Define as cores para as seções do gráfico
//    val colors = listOf(Color(0xFFfc6247), Color(0xFFf8c323), Color(0xFF81dd66), Color(0xFF52c176))
//    val separatorColor = Color.White
//    val separatorWidth = 5f // Largura do separador
//    val arcStrokeWidth = 20f // A espessura da borda do arco
//
//
//
//    // Define as faixas dos scores
//    val thresholds = listOf(300, 500, 700, 1000)
//    val scoreSections = mutableListOf<Float>()
//    var lastThreshold = 0
//
//    for ((index, threshold) in thresholds.withIndex()) {
//        val sectionScore = when {
//            score <= lastThreshold -> 0f
//            score in (lastThreshold + 1)..threshold -> (score - lastThreshold).toFloat()
//            else -> (threshold - lastThreshold).toFloat()
//        }
//        scoreSections.add(sectionScore)
//        lastThreshold = threshold
//    }
//
//    if (score > lastThreshold) {
//        scoreSections.add((score - lastThreshold).toFloat())
//    } else {
//        scoreSections.add(0f)
//    }
//
//    val animatedScoreSections = scoreSections.mapIndexed { index, section ->
//        animateFloatAsState(
//            targetValue = section,
//            animationSpec = spring(
//                dampingRatio = Spring.DampingRatioMediumBouncy,
//                stiffness = Spring.StiffnessLow
//            )
//        )
//    }
//
//    // Composição do gráfico
//    Canvas(modifier = Modifier.size(300.dp)) {
//        val total = scoreSections.fold(0f) { acc, value -> acc + value }
//        var startAngle = 180f // Inicia o arco em 180 graus
//        var sweepAngle: Float
//
//        val adjustedTotalAngle = 180f - (separatorWidth * 2) // Ajuste para não contar o separador de duas seções
//
//        scoreSections.forEachIndexed { index, section ->
//            //sweepAngle = (section / total) * adjustedTotalAngle
//            sweepAngle = (animatedScoreSections[index].value / total) * adjustedTotalAngle
//
//            // Limita o ângulo para evitar que uma seção ocupe mais espaço do que deveria
//            val clampedSweepAngle = sweepAngle.coerceAtMost(adjustedTotalAngle)
//
//            // Desenha o arco colorido
//            drawArc(
//                color = colors.getOrElse(index) { Color.Gray },
//                startAngle = startAngle,
//                sweepAngle = clampedSweepAngle,
//                useCenter = false,
//                style = Stroke(width = arcStrokeWidth)
//            )
//
//            // Desenha o separador apenas para as seções amarela, verde e entre vermelho e amarelo
//            if (index == 1 || index == 2 || (index == 0 && section > 0 && scoreSections[index + 1] > 0)) {
//                drawArc(
//                    color = separatorColor,
//                    startAngle = startAngle + sweepAngle,
//                    sweepAngle = separatorWidth,
//                    useCenter = false,
//                    style = Stroke(width = separatorWidth)
//                )
//            }
//
//            // Atualiza o ângulo inicial para a próxima seção
//            startAngle += clampedSweepAngle + (if (index == 1 || index == 2 || (index == 0 && section > 0 && scoreSections[index + 1] > 0)) separatorWidth else 0f) // Considera o separador quando necessário
//        }
//        // Desenho da classificação do score
//        val classificacaoText = classificacao
//        val classificacaoPaint = android.graphics.Paint().apply {
//            color = android.graphics.Color.BLACK // Cor do texto
//            textSize = 20f // Tamanho da fonte
//            textAlign = android.graphics.Paint.Align.CENTER // Centraliza o texto
//            //typeface = android.graphics.Typeface.create("recursiveFontFamily", android.graphics.Typeface.NORMAL) // Defina sua fonte se necessário
//            typeface = Typeface.create("recursiveFontFamily", Typeface.NORMAL)
//        }
//        val classificacaoX = size.width / 2
//        val classificacaoY = size.height / 2 - 40 // Posicionar acima do score
//
//        // Desenha o texto da classificação
//        drawContext.canvas.nativeCanvas.drawText(classificacaoText, classificacaoX, classificacaoY, classificacaoPaint)
//
//        // Desenho do valor do score no centro
//        val scoreText = score.toString()
//        val scorePaint = android.graphics.Paint().apply {
//            color = android.graphics.Color.BLACK // Cor do texto
//            textSize = 40f // Tamanho da fonte
//            textAlign = android.graphics.Paint.Align.CENTER // Centraliza o texto
//            //typeface = android.graphics.Typeface.create("recursiveFontFamily", android.graphics.Typeface.NORMAL) // Defina sua fonte se necessário
//            typeface = Typeface.create("recursiveFontFamily", Typeface.NORMAL)
//        }
//
//        // Calcular a posição para centralizar o texto do score
//        val scoreX = size.width / 2
//        val scoreY = size.height / 2 - (scorePaint.descent() + scorePaint.ascent()) / 2  // Ajuste para centralizar verticalmente e mover um pouco para cima
//
//        // Desenha o texto do score
//        drawContext.canvas.nativeCanvas.drawText(scoreText, scoreX, scoreY, scorePaint)
//
//        // Desenho do texto "/ 1000" abaixo do score
//        val belowText = "/ 1000"
//        val belowTextPaint = android.graphics.Paint().apply {
//            color = android.graphics.Color.BLACK // Cor do texto
//            textSize = 20f // Tamanho da fonte
//            textAlign = android.graphics.Paint.Align.CENTER // Centraliza o texto
//            //typeface =  android.graphics.Typeface.create("recursiveFontFamily", android.graphics.Typeface.NORMAL) // Defina sua fonte se necessário
//            typeface = Typeface.create("recursiveFontFamily", Typeface.NORMAL)
//        }
//
//        // Calcular a posição para centralizar o texto "/ 1000"
//        val belowTextX = size.width / 2
//        val belowTextY = scoreY + 20 // Ajuste para abaixo do score
//
//        // Desenha o texto "/ 1000"
//        drawContext.canvas.nativeCanvas.drawText(belowText, belowTextX, belowTextY, belowTextPaint)
//    }
//}
