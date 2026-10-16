package com.hieu.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hieu.app.ui.components.GraphErrorList
import com.hieu.app.ui.components.VIEW_HALF_RANGE
import com.hieu.app.ui.components.drawGraphScene
import com.hieu.app.ui.components.rememberGraphData
import com.hieu.app.ui.i18n.AppStrings
import com.hieu.app.ui.theme.BorderSoft
import com.hieu.app.ui.theme.BorderStrong
import com.hieu.app.ui.theme.CardBg
import com.hieu.app.ui.theme.ChipActiveBg
import com.hieu.app.ui.theme.CurveOrange
import com.hieu.app.ui.theme.InkBlue
import com.hieu.app.ui.theme.PadBg
import com.hieu.app.ui.theme.TextMuted
import com.hieu.app.ui.theme.TextSecondary
import com.hieu.app.ui.theme.TitleDark
import kotlin.math.hypot



enum class GraphTool { PEN, HIGHLIGHTER, ERASER, MOVE }


data class AnnotationStroke(
    val points: List<Offset>,
    val color: Color,
    val width: Float,
    val highlighter: Boolean = false
)

private val PenColors = listOf(
    Color(0xFF1E1E1E),
    CurveOrange,
    InkBlue,
    Color(0xFF2E9E5B),
    Color(0xFF8E44AD),
    Color(0xFFDDA92E)
)

private const val MIN_ZOOM = 0.25f
private const val MAX_ZOOM = 8f


@Composable
fun GraphToolsScreen(
    formulas: List<String>,
    strings: AppStrings,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val graphData = rememberGraphData(formulas)

    var tool by remember { mutableStateOf(GraphTool.PEN) }
    var penColor by remember { mutableStateOf(PenColors[0]) }
    var penWidth by remember { mutableStateOf(5f) }

    var strokes by remember { mutableStateOf(listOf<AnnotationStroke>()) }
    var currentStroke by remember { mutableStateOf(listOf<Offset>()) }

    var pan by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    fun pxPerUnit(): Float =
        (canvasSize.width / (VIEW_HALF_RANGE.toFloat() * 2f)) * zoom

    fun toWorld(p: Offset): Offset {
        val scale = pxPerUnit()
        if (scale <= 0f) return Offset.Zero
        val ox = canvasSize.width / 2f + pan.x
        val oy = canvasSize.height / 2f + pan.y
        return Offset((p.x - ox) / scale, (oy - p.y) / scale)
    }

    fun eraseAt(p: Offset) {
        val scale = pxPerUnit()
        if (scale <= 0f) return
        val world = toWorld(p)
        val radius = (18f + penWidth) / scale
        strokes = strokes.filterNot { stroke ->
            stroke.points.any { pt -> hypot(pt.x - world.x, pt.y - world.y) <= radius }
        }
    }

    fun zoomBy(factor: Float) {
        val newZoom = (zoom * factor).coerceIn(MIN_ZOOM, MAX_ZOOM)

        pan *= (newZoom / zoom)
        zoom = newZoom
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEFEDE8))
    ) {
        Card(
            modifier = Modifier
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

                //  Thanh tieu de
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.backButtonDescription,
                            tint = TitleDark
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        strings.graphToolsScreenTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { zoomBy(1 / 1.3f) }) {
                        Icon(Icons.Filled.ZoomOut, contentDescription = strings.zoomOut, tint = TextSecondary)
                    }
                    IconButton(onClick = { zoomBy(1.3f) }) {
                        Icon(Icons.Filled.ZoomIn, contentDescription = strings.zoomIn, tint = TextSecondary)
                    }
                    IconButton(onClick = { pan = Offset.Zero; zoom = 1f }) {
                        Icon(
                            Icons.Filled.CenterFocusStrong,
                            contentDescription = strings.resetView,
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                //  Vung do thi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PadBg)
                        .border(0.75.dp, BorderSoft, RoundedCornerShape(16.dp))
                ) {
                  Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(tool) {
                            if (tool == GraphTool.MOVE) {
                                detectTransformGestures { _, panChange, zoomChange, _ ->
                                    val newZoom = (zoom * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                    pan = pan * (newZoom / zoom) + panChange
                                    zoom = newZoom
                                }
                            } else {
                                detectDragGestures(
                                    onDragStart = { p ->
                                        if (tool == GraphTool.ERASER) eraseAt(p)
                                        else currentStroke = listOf(toWorld(p))
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        if (tool == GraphTool.ERASER) eraseAt(change.position)
                                        else currentStroke = currentStroke + toWorld(change.position)
                                    },
                                    onDragEnd = {
                                        if (currentStroke.size > 1) {
                                            strokes = strokes + AnnotationStroke(
                                                points = currentStroke,
                                                color = penColor,
                                                width = penWidth,
                                                highlighter = tool == GraphTool.HIGHLIGHTER
                                            )
                                        }
                                        currentStroke = emptyList()
                                    },
                                    onDragCancel = { currentStroke = emptyList() }
                                )
                            }
                        }
                  ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawGraphScene(graphData.curves, pan, zoom)
                    }

                    // Lop net ve tay nam tren do thi
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scale = (size.width / (VIEW_HALF_RANGE.toFloat() * 2f)) * zoom
                        val ox = size.width / 2f + pan.x
                        val oy = size.height / 2f + pan.y

                        fun screenOf(p: Offset) = Offset(ox + p.x * scale, oy - p.y * scale)

                        val live = if (currentStroke.size > 1) {
                            listOf(
                                AnnotationStroke(
                                    currentStroke, penColor, penWidth,
                                    tool == GraphTool.HIGHLIGHTER
                                )
                            )
                        } else emptyList()

                        (strokes + live).forEach { stroke ->
                            if (stroke.points.size < 2) return@forEach
                            val path = Path()
                            val first = screenOf(stroke.points[0])
                            path.moveTo(first.x, first.y)
                            stroke.points.drop(1).forEach { p ->
                                val s = screenOf(p)
                                path.lineTo(s.x, s.y)
                            }
                            drawPath(
                                path = path,
                                color = if (stroke.highlighter) stroke.color.copy(alpha = 0.32f) else stroke.color,
                                style = Stroke(
                                    width = stroke.width * zoom * (if (stroke.highlighter) 3.5f else 1f),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                  }

                    if (graphData.curves.isEmpty()) {
                        Text(
                            strings.noFormulaToPlot,
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    Text(
                        "${strings.zoomLabel}: ${"%.1f".format(zoom)}x",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    )
                }

                GraphErrorList(graphData.errors)

                Spacer(Modifier.height(10.dp))

                //  Hop cong cu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ToolButton(
                        icon = Icons.Filled.Edit,
                        label = strings.toolPen,
                        selected = tool == GraphTool.PEN,
                        modifier = Modifier.weight(1f)
                    ) { tool = GraphTool.PEN }
                    ToolButton(
                        icon = Icons.Filled.Brush,
                        label = strings.toolHighlighter,
                        selected = tool == GraphTool.HIGHLIGHTER,
                        modifier = Modifier.weight(1f)
                    ) { tool = GraphTool.HIGHLIGHTER }
                    ToolButton(
                        icon = Icons.Filled.Backspace,
                        label = strings.toolEraser,
                        selected = tool == GraphTool.ERASER,
                        modifier = Modifier.weight(1f)
                    ) { tool = GraphTool.ERASER }
                    ToolButton(
                        icon = Icons.Filled.OpenWith,
                        label = strings.toolMove,
                        selected = tool == GraphTool.MOVE,
                        modifier = Modifier.weight(1f)
                    ) { tool = GraphTool.MOVE }
                }

                Spacer(Modifier.height(10.dp))

                //  Bang mau
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        strings.colorLabel,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.width(64.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PenColors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (penColor == c) 2.5.dp else 0.75.dp,
                                        color = if (penColor == c) TitleDark else BorderStrong,
                                        shape = CircleShape
                                    )
                                    .clickable { penColor = c }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Do day net
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        strings.thicknessLabel,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.width(64.dp)
                    )
                    Slider(
                        value = penWidth,
                        onValueChange = { penWidth = it },
                        valueRange = 2f..20f,
                        colors = SliderDefaults.colors(
                            thumbColor = TitleDark,
                            activeTrackColor = TitleDark,
                            inactiveTrackColor = BorderStrong
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${penWidth.toInt()}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.width(26.dp)
                    )
                }

                //  Hoan tac / Xoa het
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.hieu.app.ui.components.OutlinedActionButton(
                        text = strings.undo,
                        icon = Icons.AutoMirrored.Filled.Undo,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (strokes.isNotEmpty()) strokes = strokes.dropLast(1)
                    }
                    com.hieu.app.ui.components.OutlinedActionButton(
                        text = strings.clearAnnotations,
                        icon = Icons.Filled.DeleteOutline,
                        modifier = Modifier.weight(1f)
                    ) {
                        strokes = emptyList()
                        currentStroke = emptyList()
                    }
                }
            }
        }
    }
}


@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) ChipActiveBg else Color.Transparent)
            .border(
                BorderStroke(if (selected) 1.dp else 0.75.dp, if (selected) TitleDark else BorderStrong),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) TitleDark else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = if (selected) TitleDark else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
