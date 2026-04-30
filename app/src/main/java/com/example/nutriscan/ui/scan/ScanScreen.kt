package com.example.nutriscan.ui.scan

import android.Manifest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriscan.domain.FitResult
import com.example.nutriscan.domain.NutritionLabel
import com.example.nutriscan.ui.theme.FitNoBg
import com.example.nutriscan.ui.theme.FitNoText
import com.example.nutriscan.ui.theme.FitYesBg
import com.example.nutriscan.ui.theme.FitYesText
import com.example.nutriscan.ui.theme.MacroCardBg
import com.example.nutriscan.ui.theme.MacroValueText
import com.example.nutriscan.ui.theme.OverlayDark
import com.example.nutriscan.ui.theme.ScanCornerIdle
import com.example.nutriscan.ui.theme.ScanCornerSuccess
import com.example.nutriscan.ui.theme.TealPrimary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    firestoreClient: com.example.nutriscan.FirestoreClient,
    userId: String,
    viewModel: ScanViewModel = viewModel(factory = ScanViewModelFactory(firestoreClient, userId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
    ) {
        when {
            cameraPermission.status.isGranted -> {
                CameraPreview(viewModel = viewModel)
                ScanGuideOverlay(uiState = uiState)
                if (uiState is ScanUiState.Scanning) ProcessingOverlay()
            }
            cameraPermission.status.shouldShowRationale -> {
                PermissionRationale(onRequest = { cameraPermission.launchPermissionRequest() })
            }
            else -> {
                PermissionDenied()
            }
        }

        TopBar(onBack = onNavigateBack)
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            AnimatedVisibility(
                visible = uiState is ScanUiState.Results,
                enter = slideInVertically(tween(300)) { it },
                exit  = slideOutVertically(tween(300)) { it },
            ) {
                val results = uiState as? ScanUiState.Results
                if (results != null) {
                    ResultsSheet(
                        label      = results.label,
                        result     = results.result,
                        aiOverview = results.aiOverview,
                        onReset    = viewModel::resetToIdle,
                        onSaveWithName = { name -> viewModel.saveLog(results.label, name) }
                    )
                }
            }
            AnimatedVisibility(
                visible = uiState !is ScanUiState.Results,
                enter = slideInVertically(tween(300)) { it },
                exit  = slideOutVertically(tween(300)) { it },
            ) {
                IdleSheet(
                    isScanning = uiState is ScanUiState.Scanning,
                    errorMessage = (uiState as? ScanUiState.Error)?.message,
                    onScan     = viewModel::captureAndScan,
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(viewModel: ScanViewModel) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView    = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            viewModel.bindCamera(providerFuture.get(), lifecycleOwner, previewView)
        }, ContextCompat.getMainExecutor(context))
    }

    AndroidView(
        factory  = { previewView },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text  = "Scan label",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ScanGuideOverlay(uiState: ScanUiState) {
    val cornerColor = if (uiState is ScanUiState.Results) ScanCornerSuccess else ScanCornerIdle
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ScanGuideBox(cornerColor = cornerColor)
        if (uiState is ScanUiState.Idle || uiState is ScanUiState.Error) {
            Text(
                text = "Point at nutrition label",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 220.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayDark),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = TealPrimary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text("Reading label…", color = Color.White, fontSize = 15.sp)
        }
    }
}

@Composable
private fun IdleSheet(
    isScanning:   Boolean,
    errorMessage: String?,
    onScan:       () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
        ) {
            SheetHandle()
            Text("Ready to scan", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                errorMessage ?: "Align the nutrition label within the guide",
                style = MaterialTheme.typography.bodySmall,
                color = if (errorMessage != null) FitNoText else Color(0xFF757575),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = onScan,
                enabled  = !isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            ) {
                Text(
                    if (isScanning) "Please wait…" else "Scan now",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResultsSheet(
    label:      NutritionLabel,
    result:     FitResult,
    aiOverview: String = "",
    onReset:    () -> Unit,
    onSaveWithName: (String) -> Unit = {},
) {
    var productName by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
        ) {
            SheetHandle()
            FitBanner(result = result)
            if (result.allergenWarnings.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                AllergenWarningBanner(allergens = result.allergenWarnings)
            }
            if (aiOverview.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                AiOverviewCard(text = aiOverview)
            }
            Spacer(Modifier.height(12.dp))
            MacroRow(label = label)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("What is this product?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onSaveWithName(productName); onReset() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            ) {
                Text("Save to Pantry", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
            ) {
                Text("Scan another", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AiOverviewCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("🤖", fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF2E7D32),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun AllergenWarningBanner(allergens: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFEBEE))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⚠️", fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "Allergen Warning!",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFB71C1C)
            )
            Text(
                text = "Contains: ${allergens.joinToString(", ")}",
                fontSize = 11.sp,
                color = Color(0xFFB71C1C)
            )
        }
    }
}

@Composable
private fun FitBanner(result: FitResult) {
    val bg        = if (result.fitsInDiet) FitYesBg   else FitNoBg
    val textColor = if (result.fitsInDiet) FitYesText  else FitNoText
    val icon      = if (result.fitsInDiet) Icons.Default.Check else Icons.Default.Warning
    val title     = if (result.fitsInDiet) "Fits your daily goals" else "May exceed your daily goals"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
            val sub = if (result.fitsInDiet)
                "${result.remainingCalories} kcal remaining today"
            else result.message
            Text(sub, fontSize = 11.sp, color = Color(0xFF555555))
        }
    }
}

@Composable
private fun MacroRow(label: NutritionLabel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MacroCard(value = "${label.calories}", unit = "kcal", name = "Calories", modifier = Modifier.weight(1f))
        MacroCard(value = "${label.proteinG}g", unit = "", name = "Protein",  modifier = Modifier.weight(1f))
        MacroCard(value = "${label.carbsG}g",   unit = "", name = "Carbs",    modifier = Modifier.weight(1f))
        MacroCard(value = "${label.fatG}g",     unit = "", name = "Fat",      modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MacroCard(value: String, unit: String, name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MacroCardBg)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MacroValueText)
        Spacer(Modifier.height(2.dp))
        Text(name, fontSize = 11.sp, color = Color(0xFF888888))
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFE0E0E0))
        )
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Camera access needed", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("NutriScan needs your camera to scan nutrition labels.", color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)) {
            Text("Grant permission")
        }
    }
}

@Composable
private fun PermissionDenied() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Camera permission denied", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Please enable camera access in your device settings.", color = Color.White.copy(0.7f), textAlign = TextAlign.Center)
    }
}
