package de.lemke.audiovisualizer.pintores.misc

import android.graphics.*
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper
import kotlin.math.min

class Gradiente(
    var preset: Int = LINEAR_HORIZONTAL,
    var color1: Int = Color.RED,
    var color2: Int = Color.YELLOW,
    var hsv: Boolean = false,
    override var paint: Paint = Paint()
) : Pintor() {

    companion object {
        const val NONE = 0
        const val LINEAR_HORIZONTAL = 1
        const val LINEAR_VERTICAL = 2
        const val LINEAR_VERTICAL_MIRROR = 3
        const val RADIAL = 4
        const val SWEEP = 5
        val HSV = IntArray(36) { i -> Color.HSVToColor(floatArrayOf(i * 10f, 1f, 1f)) }
    }

    override fun calc(helper: VisualizerHelper) {
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        val shortest = min(canvas.width, canvas.height)
        when (preset) {
            LINEAR_HORIZONTAL -> paint.shader = LinearGradient(
                0f, 0f, 0f, canvas.height.toFloat(),
                intArrayOf(color2, color1, color2), floatArrayOf(.45f, .5f, .55f),
                Shader.TileMode.CLAMP
            )

            LINEAR_VERTICAL -> paint.shader = LinearGradient(
                0f, 0f, canvas.width.toFloat(), 0f,
                if (hsv) HSV else intArrayOf(color2, color1), null,
                Shader.TileMode.CLAMP
            )

            LINEAR_VERTICAL_MIRROR -> paint.shader = LinearGradient(
                0f, 0f, canvas.width / 2f, 0f,
                if (hsv) HSV else intArrayOf(color2, color1), null,
                Shader.TileMode.MIRROR
            )

            RADIAL -> paint.shader = RadialGradient(
                canvas.width / 2f, canvas.height / 2f, shortest / 2f,
                intArrayOf(color2, color1, color2), floatArrayOf(.2f, .4f, .6f),
                Shader.TileMode.CLAMP
            )

            SWEEP -> paint.shader = SweepGradient(
                canvas.width / 2f, canvas.height / 2f,
                if (hsv) HSV else intArrayOf(color2, color1, color2), null
            )
        }
        canvas.drawPaint(paint)
    }
}
