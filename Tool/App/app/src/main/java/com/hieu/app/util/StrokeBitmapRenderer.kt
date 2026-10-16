package com.hieu.app.util


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset


fun renderStrokesToBitmap(
    strokes: List<List<Offset>>,
    widthPx: Int,
    heightPx: Int
): Bitmap {
    val bitmap = Bitmap.createBitmap(
        widthPx.coerceAtLeast(1),
        heightPx.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)

    val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    for (stroke in strokes) {
        if (stroke.size < 2) continue
        val path = Path()
        path.moveTo(stroke[0].x, stroke[0].y)
        for (p in stroke.drop(1)) path.lineTo(p.x, p.y)
        canvas.drawPath(path, paint)
    }

    return bitmap
}
