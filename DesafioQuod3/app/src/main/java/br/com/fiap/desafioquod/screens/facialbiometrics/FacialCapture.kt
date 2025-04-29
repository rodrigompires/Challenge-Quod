package br.com.fiap.desafioquod.screens.facialbiometrics

import android.Manifest
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.DraggableButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.screens.facialbiometrics.facialcomponents.ImageCapture
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily

@Composable
fun FacialCapture(navController: NavController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteQuod)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteQuod),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(200.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append("_")
                    }
                    withStyle(style = SpanStyle(color = BlackQuod)) {
                        append("Biometria Facial")
                    }
                    withStyle(style = SpanStyle(color = PurpleQuod)) {
                        append(".")
                    }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = GrayQuod,
                style = TextStyle(lineHeight = 38.sp),
                fontFamily = recursiveFontFamily
            )
            Spacer(modifier = Modifier.height(40.dp))
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Fechar",
                    modifier = Modifier.size(34.dp),
                    tint = PurpleQuod
                )
            }

//            Spacer(modifier = Modifier.height(5.dp)) // Ajuste o espaçamento do topo
            ImageCapture(navController)
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FacialCapturePreview() {
    val navController = rememberNavController()
    FacialCapture(navController)
}