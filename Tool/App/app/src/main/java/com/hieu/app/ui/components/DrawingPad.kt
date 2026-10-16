package com.hieu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hieu.app.ui.theme.BorderSoft
import com.hieu.app.ui.theme.InkBlue
import com.hieu.app.ui.theme.PadBg


@Composable
fun DrawingPad(
    strokes: List<List<Offset>>,
    currentStroke: List<Offset>,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onStrokeEnd: () -> Unit,
    onSizeChanged: (IntSize) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PadBg)
            .border(0.75.dp, BorderSoft, RoundedCornerShape(16.dp))
            .onSizeChanged(onSizeChanged)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onStrokeStart(offset) },
                    onDrag = { change, _ -> onStrokeMove(change.position) },
                    onDragEnd = { onStrokeEnd() },
                    onDragCancel = { onStrokeEnd() }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val allStrokes = if (currentStroke.size > 1) strokes + listOf(currentStroke) else strokes
            for (stroke in allStrokes) {
                if (stroke.size < 2) continue
                val path = Path()
                path.moveTo(stroke[0].x, stroke[0].y)
                for (p in stroke.drop(1)) path.lineTo(p.x, p.y)
                drawPath(
                    path = path,
                    color = InkBlue,
                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}