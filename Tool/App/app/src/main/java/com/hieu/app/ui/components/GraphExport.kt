package com.hieu.app.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.hieu.app.domain.math.MathFmt
import com.hieu.app.ui.screen.AnnotationStroke
import com.hieu.app.ui.screen.drawAnnotations
import com.hieu.app.ui.theme.BorderSoft
import com.hieu.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

private const val TARGET_EXPORT_WIDTH = 1600f
private const val LEGEND_ROW_HEIGHT = 22f
private const val LEGEND_PADDING = 20f


fun renderGraphBitmap(
    curves: List<PlottedCurve>,
    strokes: List<AnnotationStroke>,
    pan: Offset,
    zoom: Float,
    widthPx: Int,
    heightPx: Int,
    density: Density
): Bitmap {
    val exportScale = (TARGET_EXPORT_WIDTH / widthPx).coerceIn(1f, 2f)
    val w = widthPx.toFloat()
    val h = heightPx.toFloat()
    val legendHeight = if (curves.isEmpty()) 0f else curves.size * LEGEND_ROW_HEIGHT + LEGEND_PADDING
    val totalH = h + legendHeight

    val image = ImageBitmap(
        (w * exportScale).roundToInt(),
        (totalH * exportScale).roundToInt(),
        ImageBitmapConfig.Argb8888
    )

    val labelPaint = Paint().apply {
        color = TextSecondary.toArgb()
        textSize = 15f
        isAntiAlias = true
    }

    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image), Size(w, h)) {
        scale(exportScale, exportScale, pivot = Offset.Zero) {
            drawRect(Color.White, topLeft = Offset.Zero, size = Size(w, totalH))

            // Cắt theo khung đồ thị để đường cong không tràn xuống phần chú thích
            clipRect(0f, 0f, w, h) {
                drawGraphScene(curves, pan, zoom)
                drawAnnotations(strokes, pan, zoom)
            }

            if (curves.isNotEmpty()) {
                drawLine(BorderSoft, Offset(0f, h), Offset(w, h), 1f)
                curves.forEachIndexed { i, curve ->
                    val top = h + LEGEND_PADDING / 2f + i * LEGEND_ROW_HEIGHT
                    drawRoundRect(
                        color = curve.color,
                        topLeft = Offset(14f, top + 3f),
                        size = Size(12f, 12f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    val label = if (curve.formula.contains("=")) curve.formula else "y = ${curve.formula}"
                    drawContext.canvas.nativeCanvas.drawText(
                        MathFmt.pretty(label), 34f, top + 15f, labelPaint
                    )
                }
            }
        }
    }
    return image.asAndroidBitmap()
}
