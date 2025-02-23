package io.github.jeffshee.visualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper

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

    private val numberOfBars = 24 // Número total de barras
    private val ledsPerBar = 800 // Número de LEDs por barra

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
        val totalBarWidth = numberOfBars * barWidth + (numberOfBars - 1) * barMargin
        val startX = (canvas.width - totalBarWidth) / 2f
        val centerY = canvas.height / 2f

        for (i in 0 until numberOfBars) {
            val rawHeight = interpolatedFft.value(i.toDouble()).toFloat()
            val transformedHeight = rawHeight.pow(amplificationFactor)
            val maxBarHeight = canvas.height / 2f
            val barHeight = (transformedHeight / 3).coerceAtMost(maxBarHeight) // Ajuste da altura da barra

            // Desenhar cada LED dentro da barra
            val ledHeight = barHeight / ledsPerBar
            for (j in 0 until ledsPerBar) {
                val ledTop = centerY - barHeight + j * ledHeight
                val ledBottom = ledTop + ledHeight
                paint.color = getVivrantRainbowColor(i, numberOfBars)

                val left = startX + i * (barWidth + barMargin)
                val right = left + barWidth
                canvas.drawRect(left, ledTop, right, ledBottom, paint)
            }
        }
    }

    private fun getVivrantRainbowColor(index: Int, totalBars: Int): Int {
        val hue = 360f * index / totalBars
        val hsvColor = floatArrayOf(hue, 1f, 1f)
        return Color.HSVToColor(hsvColor)
    }
}