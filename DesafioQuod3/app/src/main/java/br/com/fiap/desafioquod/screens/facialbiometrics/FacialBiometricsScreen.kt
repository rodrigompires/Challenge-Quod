package br.com.fiap.desafioquod.screens.facialbiometrics

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import br.com.fiap.desafioquod.utils.explanatoryText_1
import br.com.fiap.desafioquod.utils.explanatoryText_2
import br.com.fiap.desafioquod.utils.explanatoryText_3
import br.com.fiap.desafioquod.utils.explanatoryText_4
import br.com.fiap.desafioquod.utils.textDidYouKnow
import br.com.fiap.desafioquod.utils.textLineHeight

@Composable
fun FacialBiometricsScreen(navController: NavController) {

    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var isPermissionGranted by remember { mutableStateOf(false) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isPermissionGranted = isGranted
            if (isGranted) {
                navController.navigate("facialCapture")
            }
        }
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, "Permissão de localização concedida!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permissão de localização negada!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Exibe a permissão quando necessário
//    if (showPermissionDialog) {
//        // Exibindo a caixa de dialogo de permissão
//        PermissionDialog(permissionLauncher = permissionLauncher)
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteQuod)
    ) {
        DraggableButton(
            bgColor = PurpleQuod,
            onClick = {
                showDialog = true
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteQuod),
            horizontalAlignment = Alignment
                .CenterHorizontally
        ) {
            Header(
                iconResId = R.drawable.hbmenu,
                onMenuClick = { navController.navigate("home") },
                iconTint = GrayQuod
            )

            Spacer(
                modifier = Modifier
                    .height(50.dp)
            )

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

            Spacer(
                modifier = Modifier
                    .height(41.dp)
            )

            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = explanatoryText_1,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Justify,
                    fontSize = 15.sp,
                    color = BlackQuod,
                    fontFamily = recursiveFontFamily,
                    style = textLineHeight
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = explanatoryText_2,
//                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    fontSize = 15.sp,
                    color = BlackQuod,
                    fontFamily = recursiveFontFamily,
                    style = textLineHeight
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = explanatoryText_3,
//                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    fontSize = 15.sp,
                    color = BlackQuod,
                    fontFamily = recursiveFontFamily,
                    style = textLineHeight
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )


                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    colors = ButtonDefaults.buttonColors(PurpleQuod)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = "Ícone de Chave",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )
                    Text("Conceder Permissões", fontFamily = recursiveFontFamily)
                }
            }
            }

            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Card(
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
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
                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )
                            Text(
                                text = explanatoryText_4,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = BlackQuod,
                                fontFamily = recursiveFontFamily,
                                style = textLineHeight
                            )
                            Spacer(
                                modifier = Modifier.height(20.dp)
                            )
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


//@Composable
//fun PermissionDialog(permissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
//    Dialog(onDismissRequest = { /* No action needed for dismissing */ }) {
//        Card(
//            elevation = CardDefaults.cardElevation(2.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Vamos utilizar uma ferramenta de reconhecimento facial. Garanta que seu rosto esteja bem iluminado, " +
//                            "retire óculos escuros ou máscaras.",
//                    fontWeight = FontWeight.Normal,
//                    textAlign = TextAlign.Center,
//                    color = BlackQuod,
//                    fontFamily = recursiveFontFamily
//                )
//                Spacer(modifier = Modifier.height(16.dp)) // Espaço de 1 linha
//
//                Text(
//                    text = "Este aplicativo necessita de permissão para acessar a câmera para realizar a biometria facial. " +
//                            "Por favor, conceda a permissão.",
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center,
//                    color = BlackQuod,
//                    fontFamily = recursiveFontFamily
//                )
//
//                Spacer(modifier = Modifier.height(16.dp)) // Espaço de 1 linha
//
//                Button(
//                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
//                    colors = ButtonDefaults.buttonColors(BlackQuod)
//                ) {
//                    Text("Conceder Permissão", fontFamily = recursiveFontFamily)
//                }
//            }
//        }
//    }
//}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FacialBiometricsScreenPreview() {
    val navController = rememberNavController()
    FacialBiometricsScreen(navController)
}