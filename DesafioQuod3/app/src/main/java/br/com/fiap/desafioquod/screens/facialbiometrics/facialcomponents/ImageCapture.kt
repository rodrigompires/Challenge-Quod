    package br.com.fiap.desafioquod.screens.facialbiometrics.facialcomponents

    import android.content.ContentValues
    import android.content.Context
    import android.content.pm.PackageManager
    import android.graphics.Color
    import android.graphics.RectF
    import android.location.Location
    import android.media.Image
    import android.net.Uri
    import android.os.Build
    import android.os.Handler
    import android.os.Looper
    import android.provider.MediaStore
    import android.util.Log
    import android.widget.Toast
    import androidx.annotation.OptIn
    import androidx.camera.core.CameraSelector
    import androidx.camera.core.ExperimentalGetImage
    import androidx.camera.core.ImageAnalysis
    import androidx.camera.core.ImageCapture
    import androidx.camera.core.ImageCaptureException
    import androidx.camera.core.ImageProxy
    import androidx.camera.core.Preview
    import androidx.camera.lifecycle.ProcessCameraProvider
    import androidx.camera.view.PreviewView
    import androidx.compose.animation.core.Animatable
    import androidx.compose.animation.core.FastOutSlowInEasing
    import androidx.compose.animation.core.LinearEasing
    import androidx.compose.animation.core.RepeatMode
    import androidx.compose.animation.core.animateFloatAsState
    import androidx.compose.animation.core.infiniteRepeatable
    import androidx.compose.animation.core.tween
    import androidx.compose.foundation.Canvas
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.offset
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.shape.GenericShape
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.MutableState
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.geometry.Rect
    import androidx.compose.ui.geometry.Size
    import androidx.compose.ui.graphics.Path
    import androidx.compose.ui.graphics.drawscope.Stroke
    import androidx.compose.ui.graphics.graphicsLayer
    import androidx.compose.ui.graphics.toComposeRect
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.compose.ui.viewinterop.AndroidView
    import androidx.core.content.ContextCompat
    import androidx.lifecycle.compose.LocalLifecycleOwner
    import androidx.navigation.NavController
    import androidx.navigation.compose.rememberNavController
    import br.com.fiap.desafioquod.ui.theme.GrayQuod
    import br.com.fiap.desafioquod.ui.theme.GreenQuod
    import br.com.fiap.desafioquod.ui.theme.PurpleQuod
    import br.com.fiap.desafioquod.ui.theme.WhiteQuod
    import br.com.fiap.desafioquod.ui.theme.recursiveFontFamily
    import br.com.fiap.desafioquod.utils.CAPTURE_DELAY
    import br.com.fiap.desafioquod.utils.FEEDBACK_APROXIME_SEU_ROSTO
    import br.com.fiap.desafioquod.utils.FEEDBACK_FACE_PROXIMA
    import br.com.fiap.desafioquod.utils.FEEDBACK_MANTENHA_POSICAO
    import br.com.fiap.desafioquod.utils.FEEDBACK_POSICIONE_SEU_ROSTO
    import com.google.android.gms.location.FusedLocationProviderClient
    import com.google.android.gms.location.LocationServices
    import com.google.mlkit.vision.common.InputImage
    import com.google.mlkit.vision.face.FaceDetection
    import com.google.mlkit.vision.face.FaceDetectorOptions
    import kotlinx.coroutines.coroutineScope
    import kotlinx.coroutines.delay
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.suspendCancellableCoroutine
    import java.text.SimpleDateFormat
    import java.util.Date
    import java.util.TimeZone
    import java.util.concurrent.Executors
    import kotlin.coroutines.resume
    import kotlin.coroutines.suspendCoroutine



    class LivenessTracker {
        private val samples = mutableListOf<FaceSample>()
        private var lastLeftEyeOpen: Float? = null
        private var lastRightEyeOpen: Float? = null
        private val maxSamples = 10

        private var microMovementsDetected = 0
        private var naturalBlinkDetected = false
        private var breathingMovementDetected = false
        private var textureVariationDetected = false

        data class FaceSample(
            val leftEyeOpenProbability: Float,
            val rightEyeOpenProbability: Float,
            val headEulerAngleY: Float,
            val headEulerAngleX: Float,
            val faceDistance: Float,
            val textureLiveness: Float
        )

        fun addSample(
            leftEyeOpenProbability: Float,
            rightEyeOpenProbability: Float,
            headEulerAngleY: Float,
            headEulerAngleX: Float,
            faceDistance: Float,
            textureLiveness: Float
        ) {
            samples.add(FaceSample(
                leftEyeOpenProbability,
                rightEyeOpenProbability,
                headEulerAngleY,
                headEulerAngleX,
                faceDistance,
                textureLiveness
            ))

            if (samples.size > maxSamples) {
                samples.removeAt(0)
            }

            if (lastLeftEyeOpen != null && lastRightEyeOpen != null) {
                val leftEyeClosed = lastLeftEyeOpen!! > 0.5 && leftEyeOpenProbability <= 0.3
                val rightEyeClosed = lastRightEyeOpen!! > 0.5 && rightEyeOpenProbability <= 0.3

                if (leftEyeClosed || rightEyeClosed) {
                    naturalBlinkDetected = true
                }
            }

            lastLeftEyeOpen = leftEyeOpenProbability
            lastRightEyeOpen = rightEyeOpenProbability

            if (samples.size >= 5) {
                detectPassiveSignals()
            }

            checkAndLogLivenessIfConfirmed()
        }

        private fun detectPassiveSignals() {

            val recentSamples = samples.takeLast(5)
            val microMovementsY = calculateMicroMovements(recentSamples.map { it.headEulerAngleY })
            val microMovementsX = calculateMicroMovements(recentSamples.map { it.headEulerAngleX })

            if (microMovementsY > 0.2f || microMovementsX > 0.2f) {
                microMovementsDetected++
            }

            val distanceVariations = calculateBreathingPattern(recentSamples.map { it.faceDistance })
            if (distanceVariations) {
                breathingMovementDetected = true
            }

            val avgTextureLiveness = recentSamples.map { it.textureLiveness }.average().toFloat()
            if (avgTextureLiveness > 0.7f) {
                textureVariationDetected = true
            }
        }

        private fun calculateMicroMovements(values: List<Float>): Float {
            var totalVariation = 0f
            for (i in 1 until values.size) {
                totalVariation += Math.abs(values[i] - values[i-1])
            }
            return totalVariation / (values.size - 1)
        }

        private fun calculateBreathingPattern(distances: List<Float>): Boolean {
            val variations = mutableListOf<Float>()
            for (i in 1 until distances.size) {
                variations.add(distances[i] - distances[i-1])
            }

            var directionChanges = 0
            for (i in 1 until variations.size) {
                if ((variations[i] > 0 && variations[i-1] < 0) ||
                    (variations[i] < 0 && variations[i-1] > 0)) {
                    directionChanges++
                }
            }

            return directionChanges >= 1
        }

        private fun checkAndLogLivenessIfConfirmed() {
            if (samples.size >= 5) {
                val isLive = isLivenessConfirmed()

                if (isLive) {
                    val headAnglesY = samples.map { it.headEulerAngleY }
                    val headVariationY = headAnglesY.maxOrNull()?.minus(headAnglesY.minOrNull() ?: 0f) ?: 0f

                    val headAnglesX = samples.map { it.headEulerAngleX }
                    val headVariationX = headAnglesX.maxOrNull()?.minus(headAnglesX.minOrNull() ?: 0f) ?: 0f

                    val textureScore = samples.map { it.textureLiveness }.average().toFloat()

//                    Log.d("LivenessTracker", "LIVENESS ATINGIDO: score=true, " +
//                            "naturalBlinkDetected=$naturalBlinkDetected, " +
//                            "microMovementsDetected=$microMovementsDetected, " +
//                            "breathingDetected=$breathingMovementDetected, " +
//                            "textureVariation=$textureVariationDetected (score=$textureScore), " +
//                            "headVariationY=$headVariationY, headVariationX=$headVariationX, " +
//                            "piscarValores=[leftEye=$lastLeftEyeOpen, rightEye=$lastRightEyeOpen], " +
//                            "amostrasColetadas=${samples.size}, " +
//                            "variaçãoDeMovimento=${headAnglesY.joinToString()}")
                }
            }
        }

        fun isLivenessConfirmed(): Boolean {
            if (samples.size < 5) {
                return false
            }

            var signalsDetected = 0

            if (naturalBlinkDetected) signalsDetected++
            if (microMovementsDetected >= 3) signalsDetected++
            if (breathingMovementDetected) signalsDetected++
            if (textureVariationDetected) signalsDetected++

            return signalsDetected >= 3
        }

        fun reset() {
            samples.clear()
            lastLeftEyeOpen = null
            lastRightEyeOpen = null
            microMovementsDetected = 0
            naturalBlinkDetected = false
            breathingMovementDetected = false
            textureVariationDetected = false
//            Log.d("LivenessTracker", "LivenessTracker redefinido")
        }
    }

    @OptIn(ExperimentalGetImage::class)
    @Composable
    fun ImageCapture(navController: NavController) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val previewView: PreviewView = remember {
            PreviewView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        val preview = Preview.Builder().build()
        val imageCapture = remember { ImageCapture.Builder().build() }
        val faceDetected = remember { mutableStateOf(false) }
        val feedback = remember { mutableStateOf(FEEDBACK_POSICIONE_SEU_ROSTO) }
        val autoCapture = remember { mutableStateOf(false) }
        val isCapturing = remember { mutableStateOf(false) }
        val handler = remember { Handler(Looper.getMainLooper()) }
        val runnable = remember { mutableStateOf<Runnable?>(null) }
        val isTimerRunning = remember { mutableStateOf(false) }
        val progress = remember { Animatable(0f) }
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationState = remember { mutableStateOf<Location?>(null) }
        val photoCounter = remember { mutableStateOf(0) }
        val firstPhotoUri = remember { mutableStateOf<Uri?>(null) }
        val secondPhotoUri = remember { mutableStateOf<Uri?>(null) }
        val firstPhotoLocation = remember { mutableStateOf<Location?>(null) }
        val secondPhotoLocation = remember { mutableStateOf<Location?>(null) }
        val livenessTracker = remember { LivenessTracker() }
        val livenessConfirmed = remember { mutableStateOf(false) }
        val stableFrames = remember { mutableStateOf(0) }
        val unstableFrames = remember { mutableStateOf(0) }

        // Novas variáveis para a animação de captura
        val captureAnimationState = remember { mutableStateOf(false) }
        val captureScale = remember { Animatable(1f) }
        val captureOpacity = remember { Animatable(0f) }
        val navigateToPreview = remember { mutableStateOf(false) }
        // Nova variável para controlar a visibilidade do feedback
        val showFeedback = remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            getLocation(context, fusedLocationClient, locationState)
        }

        LaunchedEffect(isTimerRunning.value) {
            if (isTimerRunning.value) {
//                Log.d("ImageCapture", "Temporizador iniciado: progress animando para 360")
                progress.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(CAPTURE_DELAY.toInt(), easing = LinearEasing)
                )
            } else {
//                Log.d("ImageCapture", "Temporizador cancelado: progress resetado para 0")
                progress.snapTo(0f)
            }
        }


        LaunchedEffect(captureAnimationState.value) {
            if (captureAnimationState.value) {
//                Log.d("ImageCapture", "Iniciando animação de captura")

                showFeedback.value = false

                coroutineScope {

                    launch {
                        captureOpacity.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(800, easing = LinearEasing)
                        )
                    }

                    launch {
                        captureScale.snapTo(0.95f)
                        delay(150)

                        captureScale.animateTo(
                            targetValue = 6f,
                            animationSpec = tween(1000, easing = FastOutSlowInEasing)
                        )

                        if (navigateToPreview.value) {
                            val encodedFirstUri = Uri.encode(firstPhotoUri.value.toString())
                            val encodedSecondUri = Uri.encode(secondPhotoUri.value.toString())
                            val firstLat = firstPhotoLocation.value?.latitude
                            val firstLon = firstPhotoLocation.value?.longitude
                            val secondLat = secondPhotoLocation.value?.latitude
                            val secondLon = secondPhotoLocation.value?.longitude

                            if (firstLat != null && firstLon != null && secondLat != null && secondLon != null) {
//                                Log.d("ImageCapture", "Navegando para imagePreview após animação")
                                navController.navigate(
                                    "imagePreview/$encodedFirstUri/$encodedSecondUri/$firstLat/$firstLon/$secondLat/$secondLon"
                                ) {
                                    popUpTo("facialCapture") { inclusive = true }
                                }
                            }
                        }
                    }
                }
            } else {

                captureOpacity.snapTo(0f)
                captureScale.snapTo(1f)
            }
        }

        LaunchedEffect(feedback.value) {
//            Log.d("ImageCapture", "Renderizando feedback: ${feedback.value}")
        }

        val analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    val canvasWidthPx = previewView.width
                    processImageProxy(
                        imageProxy = imageProxy,
                        faceDetected = faceDetected,
                        feedback = feedback,
                        autoCapture = autoCapture,
                        isCapturing = isCapturing,
                        context = context,
                        handler = handler,
                        runnable = runnable,
                        isTimerRunning = isTimerRunning,
                        canvasWidthPx = canvasWidthPx,
                        photoCounter = photoCounter,
                        firstPhotoUri = firstPhotoUri,
                        secondPhotoUri = secondPhotoUri,
                        livenessTracker = livenessTracker,
                        livenessConfirmed = livenessConfirmed,
                        stableFrames = stableFrames,
                        unstableFrames = unstableFrames
                    )
                }
            }

        LaunchedEffect(Unit) {
            try {
                val cameraProvider = context.getCameraProvider()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner = lifecycleOwner,
                    cameraSelector = cameraSelector,
                    preview,
                    imageCapture,
                    analysisUseCase
                )
                preview.setSurfaceProvider(previewView.surfaceProvider)
//                Log.d("ImageCapture", "Camera binded successfully")
            } catch (e: Exception) {
                Log.e("ImageCapture", "Erro ao bindar câmera: ${e.message}", e)
            }
        }

        LaunchedEffect(autoCapture.value) {
//            Log.d("ImageCapture", "LaunchedEffect acionado: autoCapture=${autoCapture.value}, photoCounter=${photoCounter.value}")
            if (autoCapture.value) {
//                Log.d("ImageCapture", "Iniciando captura: photoCounter=${photoCounter.value}")
                try {
                    val currentLocation = locationState.value
//                    Log.d("ImageCapture", "Localização atual: $currentLocation")
                    val captureResult = capturePhoto(imageCapture, context)
//                    Log.d("ImageCapture", "Resultado da captura: $captureResult")

                    if (captureResult != null) {
                        val imageUri = captureResult.first
                        val metadata = captureResult.second
//                        Log.d("ImageCapture", "Foto capturada: uri=$imageUri, metadata=$metadata")

                        if (imageUri != null) {
                            photoCounter.value++
//                            Log.d("ImageCapture", "photoCounter incrementado: ${photoCounter.value}")
                            if (photoCounter.value == 1) {
                                firstPhotoUri.value = imageUri
                                firstPhotoLocation.value = currentLocation
                                livenessTracker.reset()
//                                Log.d("ImageCapture", "Primeira foto capturada: uri=$imageUri, location=$currentLocation")
                                autoCapture.value = false
                            } else if (photoCounter.value == 2) {
                                secondPhotoUri.value = imageUri
                                secondPhotoLocation.value = currentLocation
//                                Log.d("ImageCapture", "Segunda foto capturada: uri=$imageUri, location=$currentLocation")

                                // Iniciar animação após capturar a segunda foto
                                captureAnimationState.value = true
                                navigateToPreview.value = true
                                autoCapture.value = false
                            }
                        } else {
//                            Log.e("ImageCapture", "URI da imagem é nula")
                            autoCapture.value = false
                        }
                    } else {
//                        Log.e("ImageCapture", "Captura falhou: resultado nulo")
                        autoCapture.value = false
                    }
                } catch (e: Exception) {
//                    Log.e("ImageCapture", "Erro ao capturar a foto: ${e.message}", e)
                    autoCapture.value = false
                }
            }
        }


        Box(
            modifier = Modifier
                .size(width = 250.dp, height = 350.dp)
                .graphicsLayer {
                    scaleX = captureScale.value
                    scaleY = captureScale.value
                },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        shape = GenericShape { size, _ ->
                            addOval(Rect(0f, 0f, size.width.toFloat(), size.height.toFloat()))
                        }
                        clip = true
                    }
            )

            // Canvas para o círculo de progresso e animação de captura
            Canvas(modifier = Modifier.matchParentSize()) {

                drawArc(
                    color = if (feedback.value == FEEDBACK_MANTENHA_POSICAO) PurpleQuod else androidx.compose.ui.graphics.Color.Transparent,
                    startAngle = -90f,
                    sweepAngle = progress.value,
                    useCenter = false,
                    style = Stroke(width = 7.dp.toPx())
                )

                drawOval(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = captureOpacity.value),
                    size = Size(size.width, size.height)
                )
            }
        }

        val fontSize by animateFloatAsState(
            targetValue = if (faceDetected.value) 16f else 14f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        if (showFeedback.value) {
            Box(
                modifier = Modifier
                    .offset(y = -340.dp)
                    .background(WhiteQuod)
                    .border(1.dp, GrayQuod)
                    .width(260.dp).height(55.dp)
                    .graphicsLayer {
                        alpha = 1f - captureOpacity.value
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (faceDetected.value) feedback.value else FEEDBACK_POSICIONE_SEU_ROSTO,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    color = if (feedback.value == FEEDBACK_MANTENHA_POSICAO) PurpleQuod else GreenQuod,
                    fontWeight = FontWeight.Bold,
                    fontFamily = recursiveFontFamily,
                    style = TextStyle(
                        letterSpacing = 1.2.sp,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        imageProxy: ImageProxy,
        faceDetected: MutableState<Boolean>,
        feedback: MutableState<String>,
        autoCapture: MutableState<Boolean>,
        isCapturing: MutableState<Boolean>,
        context: Context,
        handler: Handler,
        runnable: MutableState<Runnable?>,
        isTimerRunning: MutableState<Boolean>,
        canvasWidthPx: Int,
        photoCounter: MutableState<Int>,
        firstPhotoUri: MutableState<Uri?>,
        secondPhotoUri: MutableState<Uri?>,
        livenessTracker: LivenessTracker,
        livenessConfirmed: MutableState<Boolean>,
        stableFrames: MutableState<Int>,
        unstableFrames: MutableState<Int>
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val options = FaceDetectorOptions.Builder()
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
            val detector = FaceDetection.getClient(options)

//            Log.d("ImageProxy", "Processando frame: width=${image.width}, height=${image.height}, rotation=${imageProxy.imageInfo.rotationDegrees}")

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        faceDetected.value = true
                        val face = faces.first()
                        val headEulerAngleY = face.headEulerAngleY ?: 0f
                        val headEulerAngleX = face.headEulerAngleX ?: 0f // Adicionado
                        val leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f
                        val rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f
                        val faceWidth = face.boundingBox.width()
                        val smileProbability = face.smilingProbability ?: 0f

                        val faceDistance = (faceWidth.toFloat() / canvasWidthPx) * 100f

                        val textureLiveness = 0.7f

//                        Log.d("ImageProxy", "Rosto detectado: headY=$headEulerAngleY, headX=$headEulerAngleX, leftEye=$leftEyeOpenProbability, rightEye=$rightEyeOpenProbability, faceWidth=$faceWidth, smile=$smileProbability, canvasWidthPx=$canvasWidthPx, faceDistance=$faceDistance")

                        livenessTracker.addSample(
                            leftEyeOpenProbability = leftEyeOpenProbability,
                            rightEyeOpenProbability = rightEyeOpenProbability,
                            headEulerAngleY = headEulerAngleY,
                            headEulerAngleX = headEulerAngleX,
                            faceDistance = faceDistance,
                            textureLiveness = textureLiveness
                        )
                        livenessConfirmed.value = livenessTracker.isLivenessConfirmed()


                        val isWellPositioned = headEulerAngleY in -7f..7f &&
                                leftEyeOpenProbability > 0.4 &&
                                rightEyeOpenProbability > 0.4 &&
                                faceWidth in 330..620 &&
                                canvasWidthPx in 510..710

//                        Log.d("ImageProxy", "Verificação isWellPositioned: headY=$headEulerAngleY in -7..7=${headEulerAngleY in -7f..7f}, " +
//                                "leftEye=$leftEyeOpenProbability > 0.4=${leftEyeOpenProbability > 0.4}, " +
//                                "rightEye=$rightEyeOpenProbability > 0.4=${rightEyeOpenProbability > 0.4}, " +
//                                "faceWidth=$faceWidth in 330..620=${faceWidth in 330..620}, " +
//                                "canvasWidthPx=$canvasWidthPx in 510..710=${canvasWidthPx in 510..710}, " +
//                                "isWellPositioned=$isWellPositioned")

                        if (photoCounter.value == 0) {

                            if (smileProbability <= 0.5) {

                                val brightness = calculateBrightness(mediaImage)
//                                Log.d("ImageProxy", "Brilho: $brightness")
                                if (brightness < 60) {
                                    feedback.value = "Imagem muito escura. Ajuste a iluminação."
//                                    Log.d("ImageProxy", "Captura cancelada: brilho insuficiente ($brightness)")
                                    autoCapture.value = false
                                    isCapturing.value = false
                                    runnable.value?.let { handler.removeCallbacks(it) }
                                    runnable.value = null
                                    isTimerRunning.value = false
                                    stableFrames.value = 0
                                    unstableFrames.value = 0
                                } else {
                                    if (isWellPositioned) {
                                        stableFrames.value++
                                        unstableFrames.value = 0
//                                        Log.d("ImageProxy", "Frame estável: stableFrames=${stableFrames.value}")
                                        if (stableFrames.value >= 5 && !isTimerRunning.value && livenessConfirmed.value) {
                                            feedback.value = FEEDBACK_MANTENHA_POSICAO
//                                            Log.d("ImageProxy", "Condições atendidas: isWellPositioned=true, livenessConfirmed=${livenessConfirmed.value}, smileProbability=$smileProbability")
//                                            Log.d("ImageProxy", "Iniciando temporizador para captura")
                                            runnable.value = Runnable {
                                                if (smileProbability <= 0.5) {
                                                    autoCapture.value = true
                                                    isCapturing.value = true
//                                                    Log.d("ImageProxy", "Captura iniciada: autoCapture=${autoCapture.value}, isCapturing=${isCapturing.value}, smileProbability=$smileProbability")
                                                } else {
                                                    feedback.value = "Sorriso detectado! Não sorria na primeira foto."
//                                                    Log.d("ImageProxy", "Captura cancelada: sorriso detectado ($smileProbability)")
                                                    autoCapture.value = false
                                                    isCapturing.value = false
                                                    isTimerRunning.value = false
                                                    stableFrames.value = 0
                                                    unstableFrames.value = 0
                                                }
                                            }
                                            runnable.value?.let { handler.postDelayed(it, CAPTURE_DELAY) }
                                            isTimerRunning.value = true
                                        } else if (isTimerRunning.value) {
                                            feedback.value = FEEDBACK_MANTENHA_POSICAO
//                                            Log.d("ImageProxy", "Temporizador já em execução")
                                        } else {
                                            feedback.value = if (livenessConfirmed.value) "Aguarde, estabilizando posição..." else "Aguarde, verificando vivacidade..."
//                                            Log.d("ImageProxy", "Feedback: ${feedback.value}, stableFrames=${stableFrames.value}, livenessConfirmed=${livenessConfirmed.value}")
                                        }
                                    } else {
                                        feedback.value = FEEDBACK_APROXIME_SEU_ROSTO
                                        unstableFrames.value++
                                        stableFrames.value = 0
//                                        Log.d("ImageProxy", "Frame instável: unstableFrames=${unstableFrames.value}")
                                        if (unstableFrames.value >= 5 && isTimerRunning.value) {
                                            feedback.value = FEEDBACK_APROXIME_SEU_ROSTO
//                                            Log.d("ImageProxy", "Captura cancelada: condições não atendidas (headY=$headEulerAngleY, leftEye=$leftEyeOpenProbability, rightEye=$rightEyeOpenProbability, faceWidth=$faceWidth, canvasWidthPx=$canvasWidthPx, livenessConfirmed=${livenessConfirmed.value})")
                                            autoCapture.value = false
                                            isCapturing.value = false
                                            runnable.value?.let { handler.removeCallbacks(it) }
                                            runnable.value = null
                                            isTimerRunning.value = false
                                        }
                                    }
                                }
                            } else {
                                feedback.value = "Sorriso detectado! Não sorria na primeira foto."
//                                Log.d("ImageProxy", "Captura cancelada: sorriso detectado na primeira foto ($smileProbability)")
                                autoCapture.value = false
                                isCapturing.value = false
                                runnable.value?.let { handler.removeCallbacks(it) }
                                runnable.value = null
                                isTimerRunning.value = false
                                stableFrames.value = 0
                                unstableFrames.value = 0
                            }
                        } else if (photoCounter.value == 1) {

                            if (smileProbability > 0.5) {


                                val brightness = calculateBrightness(mediaImage)
//                                Log.d("ImageProxy", "Brilho: $brightness")
                                if (brightness < 60) {
                                    feedback.value = "Imagem muito escura. Ajuste a iluminação."
//                                    Log.d("ImageProxy", "Captura cancelada: brilho insuficiente ($brightness)")
                                    autoCapture.value = false
                                    isCapturing.value = false
                                    runnable.value?.let { handler.removeCallbacks(it) }
                                    runnable.value = null
                                    isTimerRunning.value = false
                                    stableFrames.value = 0
                                    unstableFrames.value = 0
                                } else {
                                    if (isWellPositioned) {
                                        stableFrames.value++
                                        unstableFrames.value = 0
//                                        Log.d("ImageProxy", "Frame estável: stableFrames=${stableFrames.value}")
                                        if (stableFrames.value >= 5 && !isTimerRunning.value) {
                                            feedback.value = FEEDBACK_MANTENHA_POSICAO
//                                            Log.d("ImageProxy", "Condições atendidas: isWellPositioned=true, smileProbability=$smileProbability")
//                                            Log.d("ImageProxy", "Iniciando temporizador para captura")
                                            runnable.value = Runnable {
                                                if (smileProbability > 0.5) {
                                                    autoCapture.value = true
                                                    isCapturing.value = true
//                                                    Log.d("ImageProxy", "Captura iniciada: autoCapture=${autoCapture.value}, isCapturing=${isCapturing.value}, smileProbability=$smileProbability")
                                                } else {
                                                    feedback.value = "Por favor, sorria para a segunda foto."
//                                                    Log.d("ImageProxy", "Captura cancelada: sorriso não detectado ($smileProbability)")
                                                    autoCapture.value = false
                                                    isCapturing.value = false
                                                    isTimerRunning.value = false
                                                    stableFrames.value = 0
                                                    unstableFrames.value = 0
                                                }
                                            }
                                            runnable.value?.let { handler.postDelayed(it, CAPTURE_DELAY) }
                                            isTimerRunning.value = true
                                        } else if (isTimerRunning.value) {
                                            feedback.value = FEEDBACK_MANTENHA_POSICAO
//                                            Log.d("ImageProxy", "Temporizador já em execução")
                                        } else {
                                            feedback.value = "Aguarde, estabilizando posição..."
//                                            Log.d("ImageProxy", "Feedback: ${feedback.value}, stableFrames=${stableFrames.value}, smileProbability=$smileProbability")
                                        }
                                    } else {
                                        feedback.value = FEEDBACK_APROXIME_SEU_ROSTO
                                        unstableFrames.value++
                                        stableFrames.value = 0
//                                        Log.d("ImageProxy", "Frame instável: unstableFrames=${unstableFrames.value}")
                                        if (unstableFrames.value >= 5 && isTimerRunning.value) {
                                            feedback.value = FEEDBACK_APROXIME_SEU_ROSTO
//                                            Log.d("ImageProxy", "Captura cancelada: condições não atendidas (headY=$headEulerAngleY, leftEye=$leftEyeOpenProbability, rightEye=$rightEyeOpenProbability, faceWidth=$faceWidth, canvasWidthPx=$canvasWidthPx)")
                                            autoCapture.value = false
                                            isCapturing.value = false
                                            runnable.value?.let { handler.removeCallbacks(it) }
                                            runnable.value = null
                                            isTimerRunning.value = false
                                        }
                                    }
                                }
                            } else {
                                feedback.value = "Por favor, sorria para a segunda foto."
//                                Log.d("ImageProxy", "Captura cancelada: sorriso não detectado na segunda foto ($smileProbability)")
                                autoCapture.value = false
                                isCapturing.value = false
                                runnable.value?.let { handler.removeCallbacks(it) }
                                runnable.value = null
                                isTimerRunning.value = false
                                stableFrames.value = 0
                                unstableFrames.value = 0
                            }
                        } else {
//                            Log.d("ImageProxy", "photoCounter=${photoCounter.value}, nenhuma ação realizada")
                        }
                    } else {
                        faceDetected.value = false
                        feedback.value = FEEDBACK_POSICIONE_SEU_ROSTO
//                        Log.d("ImageProxy", "Nenhum rosto detectado")
                        autoCapture.value = false
                        isCapturing.value = false
                        runnable.value?.let { handler.removeCallbacks(it) }
                        runnable.value = null
                        isTimerRunning.value = false
                        stableFrames.value = 0
                        unstableFrames.value = 0
                    }
                    imageProxy.close()
                }
                .addOnFailureListener {
//                    Log.e("ImageProxy", "Erro ao processar imagem: ${it.message}", it)
                    feedback.value = "Erro na detecção. Tente novamente."
                    imageProxy.close()
                }
        } else {
//            Log.e("ImageProxy", "Imagem nula")
            feedback.value = "Erro na captura. Tente novamente."
            imageProxy.close()
        }
    }

    private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener(
                { continuation.resume(cameraProviderFuture.get()) },
                ContextCompat.getMainExecutor(this)
            )
        }

    private suspend fun capturePhoto(imageCapture: ImageCapture, context: Context): Pair<Uri?, Map<String, String>>? =
        suspendCancellableCoroutine { continuation ->
            val name = "QuodAntifraude_${System.currentTimeMillis()}"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                }
            }
//            Log.d("CapturePhoto", "Configurando outputOptions: name=$name")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(
                context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            ).build()

            imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val imageUri = outputFileResults.savedUri
                        if (imageUri != null) {
//                            Log.d("CapturePhoto", "Imagem salva: uri=$imageUri")
                            Toast.makeText(context, "Foto capturada com sucesso!", Toast.LENGTH_SHORT).show()
                            val metadata = getCaptureMetadata(context)
//                            Log.d("CapturePhoto", "Metadados da captura: $metadata")
                            continuation.resume(Pair(imageUri, metadata))
                        } else {
//                            Log.e("CapturePhoto", "Erro: URI nula")
                            Toast.makeText(context, "Erro ao capturar foto: URI nula", Toast.LENGTH_SHORT).show()
                            continuation.resume(null)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
//                        Log.e("CapturePhoto", "Erro ao salvar imagem: ${exception.message}", exception)
                        Toast.makeText(context, "Erro ao capturar foto: ${exception.message}", Toast.LENGTH_SHORT).show()
                        continuation.resume(null)
                    }
                }
            )
        }

    private fun calculateBrightness(image: Image): Int {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val width = image.width
        val height = image.height

        var sum = 0
        var count = 0

        buffer.rewind()
        for (row in 0 until height) {
            for (col in 0 until width) {
                val pixel = buffer.get(row * rowStride + col * pixelStride).toInt() and 0xFF
                sum += pixel
                count++
            }
        }

        return if (count > 0) sum / count else 0
    }

    private fun getLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        locationState: MutableState<Location?>
    ) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    locationState.value = it
                }
            }
        }
    }

    private fun getCaptureMetadata(context: Context): Map<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val captureDate = dateFormat.format(Date())

        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "captureDate" to captureDate
        )
    }


    @androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun ImageCapturePreview() {
        val navController = rememberNavController()
        ImageCapture(navController)

    }