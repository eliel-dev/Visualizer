
package io.github.jeffshee.visualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import io.github.jeffshee.visualizer.utils.MatrixConfig
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.pow

class BarraHSimples : Painter() {
    override var paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private lateinit var interpolatedFft: PolynomialSplineFunction
    private val amplificationFactor = 1.4f
    private val smoothingFactor = 0.2f
    private var smoothedFft: FloatArray? = null

    private val ledColumns: Int get() = MatrixConfig.ledMatrixColumns
    private val ledRows: Int get() = MatrixConfig.ledMatrixRows

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

        val gravityModels = Array(smoothedFft!!.size) { GravityModel() }
        smoothedFft!!.forEachIndexed { index, value -> gravityModels[index].update(value) }
        interpolatedFft = interpolateFft(gravityModels, 25, "sp")
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        val barMargin = 50f
        val barWidth = 10f
        val totalBarWidth = ledColumns * barWidth + (ledColumns - 1) * barMargin
        val startX = (canvas.width - totalBarWidth) / 2f
        val centerY = canvas.height / 2f

        for (i in 0 until ledColumns) {
            val rawHeight = interpolatedFft.value(i.toDouble()).toFloat()
            val transformedHeight = rawHeight.pow(amplificationFactor)
            val maxBarHeight = canvas.height / 2f
            val barHeight = (transformedHeight / 3).coerceAtMost(maxBarHeight)

            val ledHeight = barHeight / ledRows
            for (j in 0 until ledRows) {
                val ledTop = centerY - barHeight + j * ledHeight
                val ledBottom = ledTop + ledHeight
                paint.color = getVivrantRainbowColor(i)

                val left = startX + i * (barWidth + barMargin)
                val right = left + barWidth
                canvas.drawRect(left, ledTop, right, ledBottom, paint)
            }
        }
    }

    private fun getVivrantRainbowColor(index: Int): Int {
        val hue = 360f * index / ledColumns
        val hsvColor = floatArrayOf(hue, 1f, 1f)
        return Color.HSVToColor(hsvColor)
    }
}
