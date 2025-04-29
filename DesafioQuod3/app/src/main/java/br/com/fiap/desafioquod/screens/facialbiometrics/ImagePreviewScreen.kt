package br.com.fiap.desafioquod.screens.facialbiometrics

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import br.com.fiap.desafioquod.apiservice.RetrofitClient
import br.com.fiap.desafioquod.apiservice.UploadResponse
import br.com.fiap.desafioquod.ui.theme.BlackQuod
import br.com.fiap.desafioquod.ui.theme.GrayQuod
import br.com.fiap.desafioquod.ui.theme.PurpleQuod
import br.com.fiap.desafioquod.ui.theme.WhiteQuod
import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
import br.com.fiap.desafioquod.utils.explanatoryTextDoc_3
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ImagePreviewScreen(
    navController: NavController,
    firstUri: String?,
    secondUri: String?,
    firstLat: String?,
    firstLon: String?,
    secondLat: String?,
    secondLon: String?
) {
    val context = LocalContext.current
    val backStackEntry = remember(navController, firstUri, secondUri, firstLat, firstLon, secondLat, secondLon) {
        navController.getBackStackEntry("imagePreview/{firstUri}/{secondUri}/{firstLat}/{firstLon}/{secondLat}/{secondLon}")
    }
    val viewModel: ImagePreviewViewModel = viewModel(viewModelStoreOwner = backStackEntry)
    val androidVersion = Build.VERSION.RELEASE
    val apiLevel = Build.VERSION.SDK_INT

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
                modifier = Modifier.fillMaxWidth(),
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PurpleQuod)) { append("_") }
                    withStyle(style = SpanStyle(color = BlackQuod)) { append("Biometria Facial") }
                    withStyle(style = SpanStyle(color = PurpleQuod)) { append(".") }
                },
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = GrayQuod,
                style = TextStyle(lineHeight = 38.sp),
                fontFamily = recursiveFontFamily
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                explanatoryTextDoc_3,
                fontFamily = recursiveFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (firstUri != null && secondUri != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context).data(Uri.decode(firstUri).toUri()).build()
                        ),
                        contentDescription = "Primeira imagem capturada",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context).data(Uri.decode(secondUri).toUri())
                                .build()
                        ),
                        contentDescription = "Segunda imagem capturada",
                        modifier = Modifier.size(200.dp)
                    )
                }
            } else {
                Text("Imagens não disponíveis")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (firstUri != null && secondUri != null) {
                            viewModel.sendImagesToApi(
                                context,
                                Uri.decode(firstUri).toUri(),
                                Uri.decode(secondUri).toUri(),
                                firstLat?.toDouble(),
                                firstLon?.toDouble(),
                                secondLat?.toDouble(),
                                secondLon?.toDouble(),
                                androidVersion,
                                apiLevel,
                                navController
                            )
                        }
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
                    onClick = {
                        navController.navigate("facialCapture") {
                            popUpTo("imagePreview/{firstUri}/{secondUri}") {
                                inclusive = true
                            }
                        }
                    },
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
}

class ImagePreviewViewModel : ViewModel() {
    var uploadStatus by mutableStateOf<String?>(null)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    var validationResult by mutableStateOf<UploadResponse?>(null)
        private set

    fun sendImagesToApi(
        context: Context,
        firstUri: Uri,
        secondUri: Uri,
        latitude1: Double?,
        longitude1: Double?,
        latitude2: Double?,
        longitude2: Double?,
        androidVersion: String,
        apiLevel: Int,
        navController: NavController
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            isProcessing = true
            Log.d("ViewModel", "Iniciando envio das imagens")

            navController.navigate("facialvalidation")

            withContext(Dispatchers.IO) {
                try {
                    val firstImagePath = getPathFromUri(context, firstUri)
                    val secondImagePath = getPathFromUri(context, secondUri)

                    if (firstImagePath != null && secondImagePath != null) {
                        val firstImageFile = File(firstImagePath)
                        val secondImageFile = File(secondImagePath)

                        val requestFile1 = firstImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val requestFile2 = secondImageFile.asRequestBody("image/*".toMediaTypeOrNull())

                        val partImage1 = MultipartBody.Part.createFormData("image1", firstImageFile.name, requestFile1)
                        val partImage2 = MultipartBody.Part.createFormData("image2", secondImageFile.name, requestFile2)

                        val metadata = getCaptureMetadata(context)

                        val latitude1Body = latitude1?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            ?: "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                        val longitude1Body = longitude1?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            ?: "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                        val latitude2Body = latitude2?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            ?: "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                        val longitude2Body = longitude2?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                            ?: "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                        val androidVersionBody = androidVersion.toRequestBody("text/plain".toMediaTypeOrNull())
                        val apiLevelBody = apiLevel.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val manufacturerBody = metadata["manufacturer"]?.toRequestBody("text/plain".toMediaTypeOrNull())
                        val modelBody = metadata["model"]?.toRequestBody("text/plain".toMediaTypeOrNull())
                        val captureDateBody = metadata["captureDate"]?.toRequestBody("text/plain".toMediaTypeOrNull())
                        val testModeBody = "false".toRequestBody("text/plain".toMediaTypeOrNull())

                        val response = RetrofitClient.apiService.uploadImages(
                            image1 = partImage1,
                            image2 = partImage2,
                            latitude1 = latitude1Body,
                            longitude1 = longitude1Body,
                            latitude2 = latitude2Body,
                            longitude2 = longitude2Body,
                            androidVersion = androidVersionBody,
                            apiLevel = apiLevelBody,
                            manufacturer = manufacturerBody,
                            model = modelBody,
                            captureDate = captureDateBody,
                            testMode = testModeBody
                        )

                        Log.d("ViewModel", "Resposta bruta: ${response.raw()}")
                        if (response.isSuccessful) {
                            val uploadResponse = response.body()
                            Log.d("ViewModel", "Corpo da resposta: $uploadResponse")
                            uploadStatus = "Success: ${uploadResponse?.transacaoId ?: "ID não encontrado"}"
                            validationResult = uploadResponse
                            Log.d("ViewModel", "Sucesso: $uploadStatus, validationResult=$validationResult")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Upload bem-sucedido: ${uploadResponse?.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            uploadStatus = "Error: ${response.code()}"
                            validationResult = UploadResponse(
                                transacaoId = null,
                                documentType = null,
                                message = "Erro: ${response.code()} - $errorBody",
                                fraudType = null,
                                similarityScore = 0.0,
                                areCoordinatesEqual = null,
                                euclideanDistance = 0.0,
                                deviceInfo = null,
                                analysisReport = null
                            )
                            Log.e("ViewModel", "Erro na resposta: $uploadStatus, errorBody=$errorBody")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Erro no upload: ${response.code()}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        throw IllegalStateException("Caminho da imagem inválido")
                    }
                } catch (e: Exception) {
                    Log.e("ViewModel", "Exceção ao enviar imagens: ${e.message}", e)
                    uploadStatus = "Error: ${e.message}"
                    validationResult = UploadResponse(
                        transacaoId = null,
                        documentType = null,
                        message = "Erro: ${e.message}",
                        fraudType = null,
                        similarityScore = 0.0,
                        areCoordinatesEqual = null,
                        euclideanDistance = 0.0,
                        deviceInfo = null,
                        analysisReport = null
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return file.absolutePath
        } catch (e: Exception) {
            Log.e("ViewModel", "Erro ao obter caminho do URI: ${e.message}", e)
            return null
        }
    }

    private fun getCaptureMetadata(context: Context): Map<String, String> {
        val captureDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        return mapOf(
            "manufacturer" to (Build.MANUFACTURER ?: "Desconhecido"),
            "model" to (Build.MODEL ?: "Desconhecido"),
            "captureDate" to captureDate
        )
    }
}
