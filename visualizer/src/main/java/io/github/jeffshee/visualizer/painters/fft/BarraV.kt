package io.github.jeffshee.visualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import io.github.jeffshee.visualizer.utils.MatrixConfig
import kotlin.math.min

class BarraV : Painter() {
    override var paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val ledRows: Int get() = MatrixConfig.ledMatrixRows
    private var smoothedFft: FloatArray? = null
    private val smoothingFactor = 0.3f

    override fun calc(helper: VisualizerHelper) {
        val fft = helper.getFftMagnitudeRange(0, 2000)
        if (fft.isEmpty()) return

        smoothedFft = if (smoothedFft == null) {
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            smoothedFft!!.mapIndexed { index, oldValue ->
                smoothingFactor * fft[index].toFloat() + (1 - smoothingFactor) * oldValue
            }.toFloatArray()
        }
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        val centerX = canvas.width / 2f
        val ledWidth = canvas.width / 64f
        val ledHeight = canvas.height / (ledRows * 1.2f)

        val maxValue = 50f
        val amplificationFactor = 0.7f
        val averageMagnitude = ((smoothedFft?.average()?.toFloat() ?: 0f) * amplificationFactor) / maxValue
        val ledsOn = (averageMagnitude * ledRows).toInt().coerceIn(0, ledRows)

        for (i in 0 until ledRows) {
            paint.color = if (i < ledsOn) getLedColor(i) else Color.DKGRAY

            val left = centerX - ledWidth / 2
            val right = centerX + ledWidth / 2
            val top = (canvas.height - (i + 1) * ledHeight) - (i * ledHeight * 0.2f)
            val bottom = top + ledHeight

            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    private fun getLedColor(index: Int): Int {
        val hue = 120f - (240f * index / ledRows)
        val hsvColor = floatArrayOf(hue, 1f, 1f)
        return Color.HSVToColor(hsvColor)
    }
}
