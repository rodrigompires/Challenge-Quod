package br.com.fiap.desafioquod.screens.registrationauthentication

import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import kotlinx.coroutines.delay

@Composable
fun RegistrationAuthenticationValidationScreen(navController: NavController, selectedOption: String?) {

    val selectedOption = navController.currentBackStackEntry?.arguments?.getString("selectedOption")
        ?: "Default"
    var imageResource by remember { mutableStateOf<Int?>(null) }
    var textToDisplay by remember { mutableStateOf<String>("Estamos analisando os dados digitados...") }

    LaunchedEffect(key1 = selectedOption) {
        if (selectedOption == "OK") {
            delay(5000)
            imageResource = R.drawable.ok
            textToDisplay = "Informações cadastrais validadas com sucesso."
        } else {
            delay(5000)
            imageResource = R.drawable.error
            textToDisplay =
                "Falha na validação. Por favor, verifique seus dados e tente novamente "
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
                .background(
                    WhiteQuod
                ),
                //.background(Color(0Xffededed)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Componente Header
            Header(
                iconResId = R.drawable.hbmenu,
                onMenuClick = { navController.navigate("menu") },
                iconTint = GrayQuod
            )
//            Fim Componente Header

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append("_")
                    }
                    withStyle(style = SpanStyle(color = BlackQuod)) {
                        append("Autenticação Cadastral")
                    }
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append(".")
                    }
                },
                fontFamily = recursiveFontFamily,
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = GrayQuod,
                style = TextStyle(
                    lineHeight = 50.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

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

                    AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                val widthInDp = 500.dp
                                val heightInDp = 500.dp
                                layoutParams = ViewGroup.LayoutParams(
                                    TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        widthInDp.value,
                                        context.resources.displayMetrics
                                    ).toInt(),
                                    TypedValue.applyDimension(
                                        TypedValue.COMPLEX_UNIT_DIP,
                                        heightInDp.value,
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
                        },
                    )
                    imageResource?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(WhiteQuod),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = it),
                                contentDescription = "Image",
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }
            Text(
                textToDisplay,
                fontFamily = recursiveFontFamily,
                fontWeight = FontWeight.Medium,
                color = when (imageResource) {
                    R.drawable.ok -> PurpleQuod
                    R.drawable.error -> BlackQuod
                    else -> GrayQuod
                },
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            CustomButton(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .width(130.dp),
                color = Color.Transparent,
                borderWith = 0.5.dp,
                borderColor = GreenQuod,
                onClick = {
                    navController.navigate("registration")
                },
                cornerRadius = 10.dp,
                textStyle = TextStyle(
                    color = GreenQuod,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                buttonText = "↩ Voltar",
            )

        }
    }
}