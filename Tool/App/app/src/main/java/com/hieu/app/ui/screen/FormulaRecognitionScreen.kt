package com.hieu.app.ui.screen

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hieu.app.ui.FormulaViewModel
import com.hieu.app.ui.InputMode
import com.hieu.app.ui.RecognitionError
import com.hieu.app.ui.components.DrawingPad
import com.hieu.app.ui.components.GraphView
import com.hieu.app.ui.components.OutlinedActionButton
import com.hieu.app.ui.components.RecognitionResultPanel
import com.hieu.app.ui.components.SegmentButton
import com.hieu.app.ui.components.UploadDropZone
import com.hieu.app.ui.i18n.LocalAppStrings
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.CardBg
import com.hieu.app.ui.theme.ChipActiveBg
import com.hieu.app.ui.theme.DangerRed
import com.hieu.app.ui.theme.TextMuted
import com.hieu.app.ui.theme.TextSecondary
import com.hieu.app.ui.theme.TitleDark


@Composable
fun FormulaRecognitionScreen(
    viewModel: FormulaViewModel,
    onOpenSettings: () -> Unit,
    onOpenGraphTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val context = LocalContext.current


    var drawingPadSize by remember { mutableStateOf(IntSize.Zero) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            viewModel.setImage(null, null)
        } else {
            val bitmap = runCatching {
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
            viewModel.setImage(bitmap, uri.lastPathSegment ?: strings.selectedImageFallbackName)
        }
    }


    val errorText: String? = when (val e = state.error) {
        null -> null
        RecognitionError.NoFormula -> strings.noFormulaFound
        is RecognitionError.Server -> strings.serverCallFailedPrefix + (e.detail ?: strings.unknownError)
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(0.75.dp, BorderStrong),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize()
        ) {
            //  Thanh tiêu đề
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(strings.screenTitle, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TitleDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenGraphTools) {
                        Icon(
                            Icons.Filled.ShowChart,
                            contentDescription = strings.openGraphTools,
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = strings.settingsButtonDescription,
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            //  Chọn chế độ nhập
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SegmentButton(
                    label = strings.handwriteTab, icon = Icons.Filled.Edit,
                    selected = state.mode == InputMode.DRAW, modifier = Modifier.weight(1f)
                ) { viewModel.setMode(InputMode.DRAW) }
                SegmentButton(
                    label = strings.uploadTab, icon = Icons.Filled.Upload,
                    selected = state.mode == InputMode.UPLOAD, modifier = Modifier.weight(1f)
                ) { viewModel.setMode(InputMode.UPLOAD) }
            }

            Spacer(Modifier.height(10.dp))

            // Vùng nhập
            if (state.mode == InputMode.DRAW) {
                Text(
                    strings.handwriteHint,
                    fontSize = 12.sp, color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                DrawingPad(
                    strokes = state.strokes,
                    currentStroke = state.currentStroke,
                    onStrokeStart = viewModel::startStroke,
                    onStrokeMove = viewModel::addPoint,
                    onStrokeEnd = viewModel::endStroke,
                    onSizeChanged = { drawingPadSize = it }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedActionButton(
                        text = strings.undo, icon = Icons.AutoMirrored.Filled.Undo, modifier = Modifier.weight(1f),
                        onClick = viewModel::undoStroke
                    )
                    OutlinedActionButton(
                        text = strings.clear, icon = Icons.Filled.DeleteOutline, modifier = Modifier.weight(1f),
                        onClick = viewModel::clearStrokes
                    )
                }
            } else {
                Text(
                    strings.uploadHint,
                    fontSize = 12.sp, color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                UploadDropZone(bitmap = state.imageBitmap) { pickImageLauncher.launch("image/*") }
                state.imageName?.let {
                    Text(it, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 6.dp, start = 4.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Nút nhận diện
            Button(
                onClick = { viewModel.recognize(drawingPadSize.width, drawingPadSize.height) },
                enabled = state.canRecognize,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChipActiveBg,
                    contentColor = TitleDark,
                    disabledContainerColor = ChipActiveBg.copy(alpha = 0.5f),
                    disabledContentColor = TextMuted
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (state.isRecognizing) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(16.dp)
                            .width(16.dp),
                        strokeWidth = 2.dp,
                        color = TitleDark
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(strings.recognizing, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                } else {
                    Icon(Icons.Filled.DocumentScanner, contentDescription = null, modifier = Modifier.height(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.recognize, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            errorText?.let {
                Text(it, fontSize = 12.sp, color = DangerRed, modifier = Modifier.padding(top = 6.dp, start = 2.dp))
            }

            // Kết quả (sửa được)
            AnimatedVisibility(
                visible = state.formulas.isNotEmpty(),
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(100))
            ) {
                RecognitionResultPanel(
                    formulas = state.formulas,
                    onFormulasChange = viewModel::updateFormulas,
                    onPlotClick = viewModel::plotGraph
                )
            }

            //  Đồ thị xem nhanh
            AnimatedVisibility(
                visible = state.showGraph,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(100))
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    GraphView(formulas = state.formulas)

                    OutlinedActionButton(
                        text = strings.openGraphTools,
                        icon = Icons.Filled.OpenInFull,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        onClick = onOpenGraphTools
                    )
                }
            }
        }
    }
}
