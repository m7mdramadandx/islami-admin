package com.islami.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A faint tiling of eight-pointed stars (two overlapping squares), a classic Islamic geometric motif. */
@Composable
fun StarPattern(color: Color, spacing: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val step = spacing.toPx()
        val radius = step * 0.34f
        val stroke = Stroke(width = 1.dp.toPx())
        var row = 0
        var y = 0f
        while (y < size.height + step) {
            // Offset every other row so the stars sit in a staggered lattice.
            var x = if (row % 2 == 0) 0f else step / 2
            while (x < size.width + step) {
                drawStar(Offset(x, y), radius, color, stroke)
                x += step
            }
            y += step * 0.87f
            row++
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color, stroke: Stroke) {
    for (rotation in listOf(0.0, PI / 4)) {
        val square = Path().apply {
            for (corner in 0..3) {
                val angle = rotation + corner * PI / 2
                val point = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat())
                if (corner == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
            close()
        }
        drawPath(square, color, style = stroke)
    }
    drawCircle(color, radius = radius * 0.28f, center = center, style = stroke)
}
