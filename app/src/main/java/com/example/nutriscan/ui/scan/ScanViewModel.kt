package com.example.nutriscan.ui.scan

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nutriscan.BuildConfig
import com.example.nutriscan.FirestoreClient
import com.example.nutriscan.GeminiClient
import com.example.nutriscan.NutritionLog
import com.example.nutriscan.domain.DietAnalyzer
import com.example.nutriscan.domain.FitResult
import com.example.nutriscan.domain.NutritionLabel
import com.example.nutriscan.domain.NutritionParser
import com.example.nutriscan.domain.UserGoals
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors

sealed interface ScanUiState {
    data object Idle       : ScanUiState
    data object Scanning   : ScanUiState
    data class  Error(val message: String) : ScanUiState
    data class  Results(
        val label:      NutritionLabel,
        val result:     FitResult,
        val aiOverview: String = "",
    ) : ScanUiState
}

class ScanViewModel(
    private val firestoreClient: FirestoreClient,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private var userAllergens: List<String> = emptyList()
    private var userGoalsList: List<String> = emptyList()
    private var userDiets: List<String> = emptyList()

    private val geminiClient by lazy { GeminiClient(BuildConfig.GEMINI_API_KEY) }

    private val placeholderGoals = UserGoals(
        dailyCalories = 2000,
        dailyProteinG = 150,
        dailyCarbsG   = 250,
        dailyFatG     = 65,
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            firestoreClient.getDietaryByUserId(userId).collect { dietary ->
                userAllergens = dietary?.allergens ?: emptyList()
                userGoalsList = dietary?.goals ?: emptyList()
                userDiets = dietary?.diets ?: emptyList()
            }
        }
    }

    fun bindCamera(
        provider:       ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        previewView:    PreviewView,
    ) {
        val preview = Preview.Builder().build()
            .also { it.surfaceProvider = previewView.surfaceProvider }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        runCatching {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
            )
        }
    }

    fun captureAndScan() {
        val capture = imageCapture ?: return
        _uiState.update { ScanUiState.Scanning }

        capture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                @androidx.camera.core.ExperimentalGetImage
                override fun onCaptureSuccess(proxy: ImageProxy) = processProxy(proxy)

                override fun onError(e: ImageCaptureException) {
                    _uiState.update { ScanUiState.Error("Capture failed: ${e.message}") }
                }
            }
        )
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processProxy(proxy: ImageProxy) {
        val mediaImage = proxy.image
        if (mediaImage == null) {
            proxy.close()
            _uiState.update { ScanUiState.Error("Could not read image.") }
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val label = NutritionParser.parse(visionText.text)
                if (label == null) {
                    _uiState.update {
                        ScanUiState.Error("No nutrition label detected. Try again with better lighting.")
                    }
                } else {
                    val fitResult = DietAnalyzer.analyze(label, placeholderGoals, userAllergens)
                    _uiState.update { ScanUiState.Results(label, fitResult) }
                    CoroutineScope(Dispatchers.IO).launch {
                        val overview = try {
                            val r = geminiClient.getNutritionOverview(label, userGoalsList, userAllergens, userDiets)
                            println("GEMINI_RESULT: $r")
                            r
                        } catch (e: Exception) {
                            println("GEMINI_ERROR: ${e.message}")
                            ""
                        }
                        _uiState.update { current ->
                            if (current is ScanUiState.Results) current.copy(aiOverview = overview) else current
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _uiState.update { ScanUiState.Error("Recognition failed: ${e.message}") }
            }
            .addOnCompleteListener { proxy.close() }
    }

    fun saveLog(label: com.example.nutriscan.domain.NutritionLabel, productName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            firestoreClient.insertNutritionLog(
                NutritionLog(
                    userId = userId,
                    productName = productName,
                    calories = label.calories,
                    proteinG = label.proteinG,
                    carbsG = label.carbsG,
                    fatG = label.fatG,
                    sugarG = label.sugarG,
                    waterMl = label.waterMl
                )
            ).collect {}
        }
    }

    fun resetToIdle() = _uiState.update { ScanUiState.Idle }

    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
        textRecognizer.close()
    }
}

class ScanViewModelFactory(
    private val firestoreClient: FirestoreClient,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ScanViewModel(firestoreClient, userId) as T
    }
}
