package br.com.fiap.desafioquod.screens.documentcospy

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import br.com.fiap.desafioquod.apiservice.RetrofitClient
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import br.com.fiap.desafioquod.utils.explanatoryTextDoc_3
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImagePreviewDocScreen(
    navController: NavController,
    selectedDocumentType: String?,
    frontImageUri: String?,
    backImageUri: String?,
    latitudeFront: String?,
    longitudeFront: String?,
    latitudeBack: String?,
    longitudeBack: String?
) {
    val context = LocalContext.current

    if (selectedDocumentType == null || frontImageUri == null || backImageUri == null) {
        Log.e(
            "ImagePreviewDocScreen",
            "Argumentos inválidos: selectedDocumentType=$selectedDocumentType, frontImageUri=$frontImageUri, backImageUri=$backImageUri"
        )
        Text(
            text = "Erro: Dados inválidos para exibir a tela.",
            color = BlackQuod,
            fontFamily = recursiveFontFamily,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
        return
    }


    val backStackEntry: NavBackStackEntry? =
        remember(navController, selectedDocumentType, frontImageUri, backImageUri) {
            try {
                navController.getBackStackEntry("imagepreviewdocscreen/{selectedDocumentType}/{frontImageUri}/{backImageUri}/{latitudeFront}/{longitudeFront}/{latitudeBack}/{longitudeBack}")
            } catch (e: IllegalArgumentException) {
                Log.e("ImagePreviewDocScreen", "Rota não encontrada no back stack: ${e.message}", e)
                null
            }
        }


    if (backStackEntry == null) {
        Log.e("ImagePreviewDocScreen", "Não foi possível obter o backStackEntry.")
        Text(
            text = "Erro: Não foi possível carregar a tela de visualização.",
            color = BlackQuod,
            fontFamily = recursiveFontFamily,
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val viewModel: ImagePreviewDocViewModel = viewModel(viewModelStoreOwner = backStackEntry)
    val decodedFrontUri =
        frontImageUri.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    val decodedBackUri =
        backImageUri.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    val androidVersion = Build.VERSION.RELEASE
    val apiLevel = Build.VERSION.SDK_INT

    Log.d(
        "ImagePreviewDocScreen",
        "Iniciando composição com: $selectedDocumentType, $decodedFrontUri, $decodedBackUri"
    )

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
//            Header(
//                iconResId = R.drawable.hbmenu,
//                onMenuClick = { navController.navigate("home") },
//                iconTint = GrayQuod
//            )

            Spacer(modifier = Modifier.height(200.dp))

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

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Tipo de documento: ${selectedDocumentType.uppercase()}",
                fontFamily = recursiveFontFamily,
                fontWeight = FontWeight.Medium,
                color = BlackQuod,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                explanatoryTextDoc_3,
                fontFamily = recursiveFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AsyncImage(
                    model = decodedFrontUri,
                    contentDescription = "Frente do documento",
                    modifier = Modifier
                        .width(150.dp)
                        .height(100.dp)
                )
                AsyncImage(
                    model = decodedBackUri,
                    contentDescription = "Verso do documento",
                    modifier = Modifier
                        .width(150.dp)
                        .height(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
//                    Log.d("ImagePreviewScreen", "Button clicked: Sending images to API")
//                    Log.d("ImagePreviewScreen", "Front URI: $decodedFrontUri")
//                    Log.d("ImagePreviewScreen", "Back URI: $decodedBackUri")
//                    Log.d("ImagePreviewScreen", "Document Type: $selectedDocumentType")
//                    Log.d(
//                        "ImagePreviewScreen",
//                        "Latitude Front: $latitudeFront, Longitude Front: $longitudeFront"
//                    )
//                    Log.d(
//                        "ImagePreviewScreen",
//                        "Latitude Back: $latitudeBack, Longitude Back: $longitudeBack"
//                    )
//                    Log.d(
//                        "ImagePreviewScreen",
//                        "Android Version: $androidVersion, API Level: $apiLevel"
//                    )
                    viewModel.sendImagesToApi(
                        context = context,
                        frontUri = decodedFrontUri.toUri(),
                        backUri = decodedBackUri.toUri(),
                        documentType = selectedDocumentType,
                        latitudeFront = latitudeFront?.toDoubleOrNull(),
                        longitudeFront = longitudeFront?.toDoubleOrNull(),
                        latitudeBack = latitudeBack?.toDoubleOrNull(),
                        longitudeBack = longitudeBack?.toDoubleOrNull(),
                        androidVersion = androidVersion,
                        apiLevel = apiLevel,
                        navController = navController
                    )
                },
                colors = ButtonDefaults.buttonColors(PurpleQuod),
                modifier = Modifier
                    .width(200.dp)
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Ícone de Enviar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar", fontFamily = recursiveFontFamily)
            }

            Button(
                onClick = { navController.navigate("documentcospy") },
                colors = ButtonDefaults.buttonColors(BlackQuod),
                modifier = Modifier
                    .width(200.dp)
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Undo,
                    contentDescription = "Ícone de Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voltar", fontFamily = recursiveFontFamily)
            }
        }
    }
}

class ImagePreviewDocViewModel : ViewModel() {

    var uploadStatus by mutableStateOf<String?>(null)
        private set

    private val _isProcessing = mutableStateOf(true)
    val isProcessing: State<Boolean> = _isProcessing

    private val _validationResult = mutableStateOf<String?>(null)
    val validationResult: State<String?> = _validationResult

    var similarityScore by mutableStateOf<Double?>(null)
        private set

    var docScore by mutableStateOf<Double?>(null)
        private set

    fun sendImagesToApi(
        context: Context,
        frontUri: Uri,
        backUri: Uri,
        documentType: String,
        latitudeFront: Double?,
        longitudeFront: Double?,
        latitudeBack: Double?,
        longitudeBack: Double?,
        androidVersion: String,
        apiLevel: Int,
        navController: NavController
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            _isProcessing.value = true
            Log.d("ImagePreviewDocViewModel", "Iniciando sendImagesToApi")
            navController.navigate("documentValidation")

            withContext(Dispatchers.IO) {
                try {
                    val frontImagePath = getPathFromUri(context, frontUri)
                    val backImagePath = getPathFromUri(context, backUri)
                    if (frontImagePath != null && backImagePath != null) {
                        val frontImageFile = File(frontImagePath)
                        val backImageFile = File(backImagePath)
                        val requestFileFront = frontImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val requestFileBack = backImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val partImageFront = MultipartBody.Part.createFormData("frontImage", frontImageFile.name, requestFileFront)
                        val partImageBack = MultipartBody.Part.createFormData("backImage", backImageFile.name, requestFileBack)

                        val metadata = getCaptureMetadata(context)
                        val response = RetrofitClient.apiService.uploadDocumentImages(
                            documentType = documentType,
                            frontImage = partImageFront,
                            backImage = partImageBack,
                            latitudeFront = latitudeFront,
                            longitudeFront = longitudeFront,
                            latitudeBack = latitudeBack,
                            longitudeBack = longitudeBack,
                            androidVersion = androidVersion,
                            apiLevel = apiLevel,
                            manufacturer = metadata["manufacturer"],
                            model = metadata["model"],
                            captureDate = metadata["captureDate"]
                        )

                        if (response.isSuccessful) {
                            val uploadResponse = response.body()
                            uploadStatus = "Success: ${uploadResponse?.transacaoId ?: "ID não encontrado"}"
                            _validationResult.value = uploadResponse?.message ?: "Resultado não encontrado"
                            Log.d("ImagePreviewDocViewModel", "Success: $uploadStatus, validationResult=${_validationResult.value}")
                            Log.d("ImagePreviewDocViewModel", "Resposta da API: transacaoId=${uploadResponse?.transacaoId}, message=${uploadResponse?.message}")

                            docScore = uploadResponse?.docScore
                        } else {
                            _validationResult.value = "Erro: ${response.code()}"
                        }
                    }
                } catch (e: Exception) {
                    _validationResult.value = "Erro: ${e.message}"
                    Log.e("ImagePreviewDocViewModel", "Exception: ${e.message}", e)
                } finally {
                    delay(100)
                    _isProcessing.value = false
                    Log.d("ImagePreviewDocViewModel", "Processamento concluído: isProcessing=${_isProcessing.value}, validationResult=${_validationResult.value}")
                }
            }
        }
    }
}

private fun getPathFromUri(context: Context, uri: Uri): String? {
    return try {
        Log.d("ImagePreviewDocViewModel", "Processing URI: $uri, scheme=${uri.scheme}")
        if (uri.scheme == "file") {
            val path = uri.path
            Log.d("ImagePreviewDocViewModel", "Extracted path from file URI: $path")
            if (path != null && File(path).exists()) {
                return path
            } else {
                Log.w(
                    "ImagePreviewDocViewModel",
                    "File from URI does not exist or path is null: $path"
                )

                val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(
                    "ImagePreviewDocViewModel",
                    "Created temp file from file URI: ${file.absolutePath}"
                )
                return file.absolutePath
            }
        }

        Log.d("ImagePreviewDocViewModel", "Processing non-file URI")
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Log.d("ImagePreviewDocViewModel", "Created temp file: ${file.absolutePath}")
        file.absolutePath
    } catch (e: Exception) {
        Log.e(
            "ImagePreviewDocViewModel",
            "Error getting path from URI: ${e.message}, stacktrace=${e.stackTraceToString()}"
        )
        null
    }
}

private fun getCaptureMetadata(context: Context): Map<String, String?> {
    val captureDate =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
    return mapOf(
        "manufacturer" to android.os.Build.MANUFACTURER,
        "model" to android.os.Build.MODEL,
        "captureDate" to captureDate
    )
}
