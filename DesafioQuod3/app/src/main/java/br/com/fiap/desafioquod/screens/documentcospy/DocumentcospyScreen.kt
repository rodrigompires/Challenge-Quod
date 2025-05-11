package br.com.fiap.desafioquod.screens.documentcospy

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CardMembership
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.sharp.Badge
import androidx.compose.material.icons.sharp.CardMembership
import androidx.compose.material.icons.sharp.DirectionsCar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import br.com.fiap.desafioquod.R
import br.com.fiap.desafioquod.components.CustomButton
import br.com.fiap.desafioquod.components.DraggableButton
import br.com.fiap.desafioquod.components.Header
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.GreenQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import br.com.fiap.desafioquod.utils.dialogTextDocumetcospy
import br.com.fiap.desafioquod.utils.explanatoryTextDoc_1
import br.com.fiap.desafioquod.utils.explanatoryTextDoc_2
import br.com.fiap.desafioquod.utils.getLocation
import br.com.fiap.desafioquod.utils.textDidYouKnow
import br.com.fiap.desafioquod.utils.textLineHeight
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun DocumentcospyScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var scannerTriggered by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf("Selecione o tipo de documento.") }
    var scannedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedDocumentType by remember { mutableStateOf<String?>(null) }
    var shouldNavigate by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val locationStateFront = remember { mutableStateOf<Location?>(null) }
    val locationStateBack = remember { mutableStateOf<Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d("DocumentcospyScreen", "Permissão de localização concedida")
        } else {
            Log.w("DocumentcospyScreen", "Permissão de localização negada")
            feedbackText = "Permissão de localização necessária para continuar."
        }
    }


    val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setPageLimit(2)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
        .build()

    val scanner = GmsDocumentScanning.getClient(scannerOptions)
    val scannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        Log.d("DocumentcospyScreen", "Scanner result code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pages?.let { pages ->
                scannedImages = pages.map { it.imageUri }
                feedbackText = if (scannedImages.size == 2) {
                    "Frente e verso capturados com sucesso."
                } else {
                    "Erro: Número de imagens capturadas inválido. Esperado: 2, Obtido: ${scannedImages.size}"
                }
                Log.d("DocumentcospyScreen", "Imagens capturadas: $scannedImages")

                if (scannedImages.size == 2) {
                    // Capturar localização do verso
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getLocation(context, fusedLocationClient) { location ->
                            if (locationStateBack.value == null) {
                                locationStateBack.value = location
                                Log.d("DocumentcospyScreen", "Localização do verso capturada após captura da frente: Lat=${location?.latitude}, Lon=${location?.longitude}")
                            }
                        }
                    }
                    shouldNavigate = true
                }
            }
        } else {
            feedbackText = "Falha ao capturar as imagens."
            Log.e("DocumentcospyScreen", "Falha ao escanear, result code: ${result.resultCode}")
        }
        scannerTriggered = false
    }

    // Lógica de navegação
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate && selectedDocumentType != null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Aguardar localização do verso (timeout de 2 segundos)
                repeat(20) {
                    if (locationStateBack.value != null) return@repeat
                    delay(100)
                }

                val latitudeFront = locationStateFront.value?.latitude?.toString() ?: "0.0"
                val longitudeFront = locationStateFront.value?.longitude?.toString() ?: "0.0"
                val latitudeBack = locationStateBack.value?.latitude?.toString() ?: "0.0"
                val longitudeBack = locationStateBack.value?.longitude?.toString() ?: "0.0"

                if (locationStateFront.value == null) {
                    Log.w("DocumentcospyScreen", "Localização da frente não disponível")
                    feedbackText = "Localização da frente não disponível. Verifique o GPS."
                    shouldNavigate = false
                    return@LaunchedEffect
                }
                if (locationStateBack.value == null) {
                    Log.w("DocumentcospyScreen", "Localização do verso não disponível")
                    feedbackText = "Localização do verso não disponível. Verifique o GPS."
                    shouldNavigate = false
                    return@LaunchedEffect
                }

                val encodedFrontUri = URLEncoder.encode(scannedImages[0].toString(), StandardCharsets.UTF_8.toString())
                val encodedBackUri = URLEncoder.encode(scannedImages[1].toString(), StandardCharsets.UTF_8.toString())

                val route = "imagepreviewdocscreen/$selectedDocumentType/$encodedFrontUri/$encodedBackUri/$latitudeFront/$longitudeFront/$latitudeBack/$longitudeBack"
                Log.d("DocumentcospyScreen", "Navegando para: $route")
                try {
                    navController.navigate(route)
                    Log.d("DocumentcospyScreen", "Navegação concluída.")
                } catch (e: Exception) {
                    Log.e("DocumentcospyScreen", "Erro ao navegar: ${e.message}", e)
                    feedbackText = "Erro ao navegar para a próxima tela."
                }
                shouldNavigate = false
            } else {
                Log.w("DocumentcospyScreen", "Permissão de localização não concedida")
                feedbackText = "Permissão de localização necessária."
                shouldNavigate = false
            }
        }
    }


    LaunchedEffect(scannerTriggered) {
        if (scannerTriggered) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                getLocation(context, fusedLocationClient) { location ->
                    if (locationStateFront.value == null) {
                        locationStateFront.value = location
                        Log.d("DocumentcospyScreen", "Localização da frente capturada ao acionar o scanner: Lat=${location?.latitude}, Lon=${location?.longitude}")
                    }
                }

                repeat(10) {
                    if (locationStateFront.value != null) return@repeat
                    delay(100)
                }
                if (locationStateFront.value == null) {
                    feedbackText = "Não foi possível obter a localização da frente. Verifique o GPS."
                    scannerTriggered = false
                    return@LaunchedEffect
                }

                // Iniciar o scanner
                Log.d("DocumentcospyScreen", "Iniciando scanner para $selectedDocumentType...")
                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
                        scannerLauncher.launch(intentSenderRequest)
                        Log.d("DocumentcospyScreen", "Scanner iniciado.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DocumentcospyScreen", "Falha ao iniciar scanner", e)
                        feedbackText = "Erro ao iniciar o scanner."
                        scannerTriggered = false
                    }
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                scannerTriggered = false
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

            Spacer(modifier = Modifier.height(50.dp))

            Spacer(modifier = Modifier.height(41.dp))

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = explanatoryTextDoc_1,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Justify,
                    fontSize = 15.sp,
                    color = BlackQuod,
                    fontFamily = recursiveFontFamily,
                    style = textLineHeight
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = explanatoryTextDoc_2,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    fontSize = 15.sp,
                    color = BlackQuod,
                    fontFamily = recursiveFontFamily,
                    style = textLineHeight
                )

                Spacer(modifier = Modifier.height(5.dp))

                CustomButton(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    color = if (selectedDocumentType == "CNH") BlackQuod else Color.Transparent,
                    borderWith = 0.5.dp,
                    borderColor = PurpleQuod,
                    onClick = { selectedDocumentType = "CNH" },
                    cornerRadius = 10.dp,
                    textStyle = TextStyle(
                        color = if (selectedDocumentType == "CNH") WhiteQuod else BlackQuod,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = recursiveFontFamily
                    ),
                    buttonText = "Carteira Nacional de Habilitação (CNH)",
                    icon = Icons.Rounded.DirectionsCar
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomButton(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    color = if (selectedDocumentType == "CPF") BlackQuod else Color.Transparent,
                    borderWith = 0.5.dp,
                    borderColor = PurpleQuod,
                    onClick = { selectedDocumentType = "CPF" },
                    cornerRadius = 10.dp,
                    textStyle = TextStyle(
                        color = if (selectedDocumentType == "CPF") WhiteQuod else BlackQuod,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = recursiveFontFamily
                    ),
                    buttonText = "CPF",
                    icon = Icons.Rounded.Badge
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomButton(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    color = if (selectedDocumentType == "RG") BlackQuod else Color.Transparent,
                    borderWith = 0.5.dp,
                    borderColor = PurpleQuod,
                    onClick = { selectedDocumentType = "RG" },
                    cornerRadius = 10.dp,
                    textStyle = TextStyle(
                        color = if (selectedDocumentType == "RG") WhiteQuod else BlackQuod,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = recursiveFontFamily
                    ),
                    buttonText = "Registro Geral (RG)",
                    icon = Icons.Rounded.CardMembership
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            CustomButton(
                modifier = Modifier.width(200.dp).height(50.dp),
                color = PurpleQuod,
                borderWith = 0.5.dp,
                borderColor = PurpleQuod,
                onClick = {
                    feedbackText = "Capturar Frente e Verso do $selectedDocumentType"
                    scannerTriggered = true
                },
                cornerRadius = 10.dp,
                textStyle = TextStyle(
                    color = WhiteQuod,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = recursiveFontFamily
                ),
                buttonText = "Scanner",
                enabled = selectedDocumentType != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = feedbackText,
                fontSize = 16.sp,
                color = if (feedbackText.contains("sucesso")) GreenQuod else BlackQuod,
                fontFamily = recursiveFontFamily,
                textAlign = TextAlign.Center
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
                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )
                        Text(
                            text = dialogTextDocumetcospy,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = BlackQuod,
                            fontFamily = recursiveFontFamily,
                            style = textLineHeight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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