package com.example.xrvirtualkeyboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.xrvirtualkeyboard.geometry.Point2D

/**
 * Renders a stylized 3D virtual right hand relative to the active selection/cursor position
 * (where index tip is positioned at [cursorPosition]).
 *
 * Smoothly animates the thumb into a pinch gesture when [isEngaged] is true.
 */
@Composable
fun VirtualRightHand(
    cursorPosition: Point2D,
    isEngaged: Boolean,
    modifier: Modifier = Modifier,
) {
    val pinchProgress by animateFloatAsState(
        targetValue = if (isEngaged) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "pinchAnimation",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Canvas coordinate origin (0,0) is top-left of surface.
        // Convert surface center-relative Point2D (in dp) to Canvas pixel coordinates.
        val indexTipPx = Offset(
            x = (size.width / 2f) + cursorPosition.x.dp.toPx(),
            y = (size.height / 2f) + cursorPosition.y.dp.toPx(),
        )

        draw3DRightHand(indexTipPx = indexTipPx, pinchProgress = pinchProgress)
    }
}

private fun DrawScope.draw3DRightHand(
    indexTipPx: Offset,
    pinchProgress: Float,
) {
    val scale = density // 1 dp = density px

    // Helper to offset relative to index tip in DP units
    fun rel(dxDp: Float, dyDp: Float): Offset {
        return Offset(
            x = indexTipPx.x + (dxDp * scale),
            y = indexTipPx.y + (dyDp * scale),
        )
    }

    // --- Key Joint Positions (Relative to Index Tip) ---
    val indexTip = indexTipPx
    val indexDip = rel(-2f, 18f)
    val indexPip = rel(-3f, 36f)
    val indexMcp = rel(-5f, 58f)

    // Thumb joints (interpolate toward index tip on pinch)
    val thumbTipIdle = rel(-28f, 30f)
    val thumbTipPinching = rel(-2f, 4f)
    val thumbTip = lerpOffset(thumbTipIdle, thumbTipPinching, pinchProgress)

    val thumbIpIdle = rel(-34f, 46f)
    val thumbIpPinching = rel(-14f, 24f)
    val thumbIp = lerpOffset(thumbIpIdle, thumbIpPinching, pinchProgress)

    val thumbMcpIdle = rel(-32f, 68f)
    val thumbMcpPinching = rel(-24f, 50f)
    val thumbMcp = lerpOffset(thumbMcpIdle, thumbMcpPinching, pinchProgress)

    val thumbCmc = rel(-24f, 88f)

    // Middle finger
    val middleTip = rel(18f, -4f)
    val middleDip = rel(16f, 14f)
    val middlePip = rel(14f, 34f)
    val middleMcp = rel(12f, 58f)

    // Ring finger
    val ringTip = rel(36f, 4f)
    val ringDip = rel(33f, 20f)
    val ringPip = rel(30f, 38f)
    val ringMcp = rel(26f, 60f)

    // Pinky finger
    val pinkyTip = rel(50f, 16f)
    val pinkyDip = rel(46f, 30f)
    val pinkyPip = rel(42f, 46f)
    val pinkyMcp = rel(38f, 62f)

    // Wrist & Forearm
    val wristLeft = rel(-12f, 104f)
    val wristRight = rel(32f, 104f)
    val forearmLeft = rel(-16f, 135f)
    val forearmRight = rel(36f, 135f)

    val shadowOffset = Offset(6f * scale, 10f * scale)

    // --- 1. DROP SHADOW FOR 3D ELEVATION ---
    val shadowPath = Path().apply {
        moveTo(wristLeft.x + shadowOffset.x, wristLeft.y + shadowOffset.y)
        lineTo(thumbCmc.x + shadowOffset.x, thumbCmc.y + shadowOffset.y)
        lineTo(thumbMcp.x + shadowOffset.x, thumbMcp.y + shadowOffset.y)
        lineTo(indexMcp.x + shadowOffset.x, indexMcp.y + shadowOffset.y)
        lineTo(middleMcp.x + shadowOffset.x, middleMcp.y + shadowOffset.y)
        lineTo(ringMcp.x + shadowOffset.x, ringMcp.y + shadowOffset.y)
        lineTo(pinkyMcp.x + shadowOffset.x, pinkyMcp.y + shadowOffset.y)
        lineTo(wristRight.x + shadowOffset.x, wristRight.y + shadowOffset.y)
        close()
    }
    drawPath(path = shadowPath, color = Color.Black.copy(alpha = 0.35f))

    // --- 2. PALM & FOREARM MESH FILL ---
    val palmPath = Path().apply {
        moveTo(forearmLeft.x, forearmLeft.y)
        lineTo(wristLeft.x, wristLeft.y)
        lineTo(thumbCmc.x, thumbCmc.y)
        lineTo(thumbMcp.x, thumbMcp.y)
        lineTo(indexMcp.x, indexMcp.y)
        lineTo(middleMcp.x, middleMcp.y)
        lineTo(ringMcp.x, ringMcp.y)
        lineTo(pinkyMcp.x, pinkyMcp.y)
        lineTo(wristRight.x, wristRight.y)
        lineTo(forearmRight.x, forearmRight.y)
        close()
    }

    val palmGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xCC00E5FF), // glowing cyan near knuckles
            Color(0x990091EA), // deep blue
            Color(0xBB1A237E), // dark translucent blue base
        ),
        startY = indexTipPx.y,
        endY = forearmLeft.y,
    )
    drawPath(path = palmPath, brush = palmGradient)
    drawPath(
        path = palmPath,
        color = Color(0xFF80D8FF).copy(alpha = 0.6f),
        style = Stroke(width = 2f * scale, join = StrokeJoin.Round),
    )

    // --- 3. FINGER BONES (SKELETON) ---
    val fingers = listOf(
        listOf(thumbCmc, thumbMcp, thumbIp, thumbTip),
        listOf(indexMcp, indexPip, indexDip, indexTip),
        listOf(middleMcp, middlePip, middleDip, middleTip),
        listOf(ringMcp, ringPip, ringDip, ringTip),
        listOf(pinkyMcp, pinkyPip, pinkyDip, pinkyTip),
    )

    fingers.forEach { joints ->
        for (i in 0 until (joints.size - 1)) {
            val p1 = joints[i]
            val p2 = joints[i + 1]

            // Outer bone segment shadow/glow
            drawLine(
                color = Color(0xAA00B0FF),
                start = p1,
                end = p2,
                strokeWidth = 7f * scale,
                cap = StrokeCap.Round,
            )
            // Inner bone core
            drawLine(
                color = Color(0xFFE0F7FA),
                start = p1,
                end = p2,
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round,
            )
        }
    }

    // --- 4. JOINT SPHERES ---
    val allJoints = listOf(
        indexMcp, indexPip, indexDip,
        thumbCmc, thumbMcp, thumbIp,
        middleMcp, middlePip, middleDip, middleTip,
        ringMcp, ringPip, ringDip, ringTip,
        pinkyMcp, pinkyPip, pinkyDip, pinkyTip,
        wristLeft, wristRight,
    )

    allJoints.forEach { joint ->
        drawCircle(
            color = Color(0xAA00E5FF),
            radius = 5.5f * scale,
            center = joint,
        )
        drawCircle(
            color = Color.White,
            radius = 2.5f * scale,
            center = joint,
        )
    }

    // --- 5. PINCH ENGAGEMENT CONTACT HIGHLIGHT ---
    if (pinchProgress > 0.05f) {
        val contactCenter = lerpOffset(indexTip, thumbTip, 0.5f)
        val glowRadius = (8f + (10f * pinchProgress)) * scale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF69F0AE), // bright green press feedback
                    Color(0x8000E5FF),
                    Color.Transparent,
                ),
                center = contactCenter,
                radius = glowRadius,
            ),
            radius = glowRadius,
            center = contactCenter,
        )
    }

    // --- 6. INDEX TIP TARGETING CURSOR ---
    drawCircle(
        color = Color(0x66FF1744),
        radius = 12f * scale,
        center = indexTip,
    )
    drawCircle(
        color = if (pinchProgress > 0.5f) Color(0xFF00E676) else Color(0xFFFF1744),
        radius = 6f * scale,
        center = indexTip,
    )
    drawCircle(
        color = Color.White,
        radius = 2.5f * scale,
        center = indexTip,
    )
}

private fun lerpOffset(start: Offset, stop: Offset, fraction: Float): Offset {
    return Offset(
        x = start.x + ((stop.x - start.x) * fraction),
        y = start.y + ((stop.y - start.y) * fraction),
    )
}
