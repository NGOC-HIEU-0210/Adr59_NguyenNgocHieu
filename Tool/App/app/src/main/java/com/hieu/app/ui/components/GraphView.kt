package com.hieu.app.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.domain.math.Expr
import com.hieu.app.domain.math.FormulaParseException
import com.hieu.app.domain.math.parseFormula
import com.hieu.app.ui.i18n.LocalAppStrings
import com.hieu.app.ui.theme.AxisLine
import com.hieu.app.ui.theme.BorderSoft
import com.hieu.app.ui.theme.CurvePalette
import com.hieu.app.ui.theme.DangerRed
import com.hieu.app.ui.theme.GridLine
import com.hieu.app.ui.theme.InkBlue
import com.hieu.app.ui.theme.IntersectionDot
import com.hieu.app.ui.theme.PadBg
import com.hieu.app.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

const val VIEW_HALF_RANGE = 7.5


data class PlottedCurve(
    val formula: String,
    val color: Color,
    val expr: Expr
)

private data class MarkedPoint(
    val x: Double,
    val y: Double,
    val color: Color,
    val label: String
)


data class GraphData(
    val curves: List<PlottedCurve>,
    val errors: List<Pair<String, Throwable>>
)


@Composable
fun rememberGraphData(formulas: List<String>): GraphData {
    val cleaned = remember(formulas) { formulas.map { it.trim() }.filter { it.isNotEmpty() } }

    val parsedResults = remember(cleaned) {
        cleaned.map { f -> f to runCatching { parseFormula(f) } }
    }

    return remember(parsedResults) {
        GraphData(
            curves = parsedResults.mapIndexedNotNull { i, pair ->
                val (formula, result) = pair
                result.getOrNull()?.let { expr ->
                    PlottedCurve(formula, CurvePalette[i % CurvePalette.size], expr)
                }
            },
            errors = parsedResults.mapNotNull { (formula, result) ->
                result.exceptionOrNull()?.let { formula to it }
            }
        )
    }
}



fun DrawScope.drawGraphScene(
    curves: List<PlottedCurve>,
    pan: Offset,
    zoom: Float = 1f,
    showCurveLabels: Boolean = true,
    showMarkedPoints: Boolean = true,
    labelScale: Float = 1f
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    val ox = w / 2f + pan.x
    val oy = h / 2f + pan.y
    val scale = (w / (VIEW_HALF_RANGE.toFloat() * 2f)) * zoom
    if (scale <= 0.0001f) return

    val worldMinX = ((0f - ox) / scale).toDouble()
    val worldMaxX = ((w - ox) / scale).toDouble()
    val worldMinY = ((oy - h) / scale).toDouble()
    val worldMaxY = (oy / scale).toDouble()

    val axisLabelPaint = Paint().apply {
        color = AxisLine.toArgb()
        textSize = 21f * labelScale
        isAntiAlias = true
    }
    val pointLabelPaint = Paint().apply {
        color = TextSecondary.toArgb()
        textSize = 20f * labelScale
        isAntiAlias = true
    }

    fun inBox(sx: Float, sy: Float) = sx in 0f..w && sy in 0f..h

    // Buoc chia luoi tu dieu chinh theo muc zoom de khong ve qua day.
    val step = niceStep(scale)

    var u = floor(worldMinX / step).toInt()
    val maxU = ceil(worldMaxX / step).toInt()
    while (u <= maxU) {
        val value = u * step
        val gx = ox + (value * scale).toFloat()
        if (gx in 0f..w) {
            drawLine(GridLine, Offset(gx, 0f), Offset(gx, h), 1f)
            if (u != 0) {
                val tickY = oy.coerceIn(0f, h)
                drawLine(AxisLine, Offset(gx, tickY - 4f), Offset(gx, tickY + 4f), 1.5f)
                drawContext.canvas.nativeCanvas.drawText(
                    fmtNum(value), gx + 3f, tickY + 18f * labelScale, axisLabelPaint
                )
            }
        }
        u++
    }

    var v = floor(worldMinY / step).toInt()
    val maxV = ceil(worldMaxY / step).toInt()
    while (v <= maxV) {
        val value = v * step
        val gy = oy - (value * scale).toFloat()
        if (gy in 0f..h) {
            drawLine(GridLine, Offset(0f, gy), Offset(w, gy), 1f)
            if (v != 0) {
                val tickX = ox.coerceIn(0f, w)
                drawLine(AxisLine, Offset(tickX - 4f, gy), Offset(tickX + 4f, gy), 1.5f)
                drawContext.canvas.nativeCanvas.drawText(
                    fmtNum(value), tickX + 6f, gy - 3f, axisLabelPaint
                )
            }
        }
        v++
    }

    if (oy in 0f..h) drawLine(AxisLine, Offset(0f, oy), Offset(w, oy), 2f)
    if (ox in 0f..w) drawLine(AxisLine, Offset(ox, 0f), Offset(ox, h), 2f)
    if (ox in 0f..w && oy in 0f..h) {
        drawContext.canvas.nativeCanvas.drawText("0", ox + 6f, oy + 18f * labelScale, axisLabelPaint)
    }

    curves.forEach { curve ->
        val path = Path()
        var started = false
        var px = 0f
        val visiblePts = mutableListOf<Offset>()
        while (px <= w) {
            val mx = ((px - ox) / scale).toDouble()
            val my = curve.expr.eval(mx)
            if (my.isFinite()) {
                val py = oy - (my * scale).toFloat()
                if (py in -30f..(h + 30f)) {
                    if (!started) { path.moveTo(px, py); started = true } else path.lineTo(px, py)
                    if (py in 0f..h) visiblePts += Offset(px, py)
                } else started = false
            } else started = false
            px += 1f
        }
        drawPath(path, curve.color, style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        if (showCurveLabels && visiblePts.isNotEmpty()) {
            val anchor = visiblePts[((visiblePts.size - 1) * 0.62f).toInt().coerceIn(0, visiblePts.size - 1)]
            val label = curveLabel(curve.formula)
            val labelPaint = Paint().apply {
                color = curve.color.toArgb()
                textSize = 22f * labelScale
                isFakeBoldText = true
                isAntiAlias = true
            }
            val textWidth = labelPaint.measureText(label)
            var lx = anchor.x + 8f
            if (lx + textWidth > w) lx = (anchor.x - textWidth - 8f).coerceAtLeast(2f)
            val ly = (anchor.y - 10f).coerceIn(18f * labelScale, h - 6f)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.8f),
                topLeft = Offset(lx - 3f, ly - 17f * labelScale),
                size = Size(textWidth + 6f, 22f * labelScale),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawContext.canvas.nativeCanvas.drawText(label, lx, ly, labelPaint)
        }
    }

    if (!showMarkedPoints) return

    val searchLo = worldMinX - 1.0
    val searchHi = worldMaxX + 1.0
    val markedPoints = mutableListOf<MarkedPoint>()
    curves.forEach { curve ->
        val y0 = curve.expr.eval(0.0)
        if (y0.isFinite()) {
            markedPoints += MarkedPoint(0.0, y0, curve.color, "(0, ${fmtNum(y0)})")
        }
        findRoots({ x -> curve.expr.eval(x) }, searchLo, searchHi).forEach { rx ->
            markedPoints += MarkedPoint(rx, 0.0, curve.color, "(${fmtNum(rx)}, 0)")
        }
    }

    for (i in curves.indices) {
        for (j in i + 1 until curves.size) {
            val a = curves[i].expr
            val b = curves[j].expr
            findRoots({ x -> a.eval(x) - b.eval(x) }, searchLo, searchHi).forEach { ix ->
                val iy = a.eval(ix)
                if (iy.isFinite()) {
                    markedPoints += MarkedPoint(ix, iy, IntersectionDot, "(${fmtNum(ix)}, ${fmtNum(iy)})")
                }
            }
        }
    }

    val deduped = dedupePoints(markedPoints)
    deduped.forEachIndexed { idx, pt ->
        val sx = ox + (pt.x * scale).toFloat()
        val sy = oy - (pt.y * scale).toFloat()
        if (inBox(sx, sy)) {
            drawCircle(Color.White, radius = 5.5f, center = Offset(sx, sy))
            drawCircle(pt.color, radius = 4f, center = Offset(sx, sy))
            val labelY = if (idx % 2 == 0) sy - 8f else sy + 20f
            drawContext.canvas.nativeCanvas.drawText(pt.label, sx + 6f, labelY, pointLabelPaint)
        }
    }
}


@Composable
fun GraphLegend(curves: List<PlottedCurve>, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    if (curves.isEmpty()) return
    Column(modifier = modifier) {
        curves.forEach { curve ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(curve.color)
                )
                Text(
                    "  ${curve.formula}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        if (curves.size > 1) {
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(IntersectionDot)
                )
                Text(
                    "  ${strings.intersectionLegend}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}


@Composable
fun GraphErrorList(errors: List<Pair<String, Throwable>>, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    Column(modifier = modifier) {
        errors.forEach { (formula, e) ->
            Text(
                "\u201C$formula\u201D: ${(e as? FormulaParseException)?.message ?: strings.unreadableFormula}",
                fontSize = 12.sp,
                color = DangerRed,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }
    }
}


@Composable
fun GraphView(formulas: List<String>) {
    val strings = LocalAppStrings.current
    val graphData = rememberGraphData(formulas)

    var panPx by remember(formulas) { mutableStateOf(Offset.Zero) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PadBg)
                .border(0.75.dp, BorderSoft, RoundedCornerShape(16.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            panPx += dragAmount
                        }
                    }
            ) {
                drawGraphScene(graphData.curves, panPx)
            }

            if (panPx != Offset.Zero) {
                TextButton(
                    onClick = { panPx = Offset.Zero },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Text(strings.recenterGraph, fontSize = 11.sp, color = InkBlue)
                }
            }
        }

        GraphLegend(graphData.curves, modifier = Modifier.padding(top = 4.dp))
        GraphErrorList(graphData.errors)
    }
}


private fun niceStep(scale: Float): Double {
    // Giu khoang cach giua 2 vach chia o muc de doc
    val target = 70.0 / scale
    val candidates = listOf(
        0.01, 0.02, 0.05, 0.1, 0.2, 0.25, 0.5, 1.0, 2.0, 2.5, 5.0,
        10.0, 20.0, 25.0, 50.0, 100.0, 200.0, 500.0, 1000.0
    )
    return candidates.firstOrNull { it >= target } ?: candidates.last()
}


private fun findRoots(fn: (Double) -> Double, lo: Double, hi: Double, steps: Int = 800): List<Double> {
    if (lo >= hi) return emptyList()
    val roots = mutableListOf<Double>()
    val stepSize = (hi - lo) / steps
    var xPrev = lo
    var yPrev = fn(xPrev)
    for (i in 1..steps) {
        val xCur = lo + stepSize * i
        val yCur = fn(xCur)
        if (yPrev.isFinite() && yCur.isFinite()) {
            when {
                yPrev == 0.0 -> roots += xPrev
                yPrev * yCur < 0 -> {
                    var a = xPrev
                    var b = xCur
                    var fa = yPrev
                    repeat(50) {
                        val m = (a + b) / 2.0
                        val fm = fn(m)
                        if (fa * fm <= 0) b = m else { a = m; fa = fm }
                    }
                    roots += (a + b) / 2.0
                }
            }
        }
        xPrev = xCur
        yPrev = yCur
    }
    return dedupeDoubles(roots)
}

private fun dedupeDoubles(values: List<Double>, tolerance: Double = 0.05): List<Double> {
    val sorted = values.sorted()
    val out = mutableListOf<Double>()
    for (v in sorted) {
        if (out.isEmpty() || abs(v - out.last()) > tolerance) out += v
    }
    return out
}

private fun dedupePoints(points: List<MarkedPoint>, tolerance: Double = 0.12): List<MarkedPoint> {
    val out = mutableListOf<MarkedPoint>()
    for (p in points) {
        val alreadyThere = out.any { abs(it.x - p.x) < tolerance && abs(it.y - p.y) < tolerance }
        if (!alreadyThere) out += p
    }
    return out
}


private fun curveLabel(formula: String): String =
    if (formula.contains("=")) formula else "y = $formula"

private fun fmtNum(v: Double): String {
    val rounded = round(v * 100.0) / 100.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
