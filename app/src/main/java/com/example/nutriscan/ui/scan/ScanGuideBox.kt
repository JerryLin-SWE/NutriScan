package com.example.nutriscan.ui.scan

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ScanGuideBox(
    cornerColor:  Color,
    modifier:     Modifier = Modifier,
    boxWidthFrac: Float = 0.72f,   // fraction of canvas width
    boxHeightFrac: Float = 0.28f,  // fraction of canvas height
    cornerLength: Dp = 24.dp,
    strokeWidth:  Dp = 2.5.dp,
) {
    Canvas(modifier = modifier) {
        val cw = size.width
        val ch = size.height

        val boxW = cw * boxWidthFrac
        val boxH = ch * boxHeightFrac
        val left   = (cw - boxW) / 2f
        val top    = (ch - boxH) / 2f
        val right  = left + boxW
        val bottom = top  + boxH

        val cl = cornerLength.toPx()
        val sw = strokeWidth.toPx()

        drawCorner(left,  top,    cl,  cl,  sw, cornerColor)  // top-left
        drawCorner(right, top,   -cl,  cl,  sw, cornerColor)  // top-right
        drawCorner(left,  bottom, cl, -cl,  sw, cornerColor)  // bottom-left
        drawCorner(right, bottom,-cl, -cl,  sw, cornerColor)  // bottom-right
    }
}

private fun DrawScope.drawCorner(
    x: Float, y: Float,
    dx: Float, dy: Float,
    strokeWidth: Float,
    color: Color,
) {
    drawLine(color, Offset(x, y), Offset(x + dx, y), strokeWidth, StrokeCap.Round)
    drawLine(color, Offset(x, y), Offset(x, y + dy), strokeWidth, StrokeCap.Round)
}
