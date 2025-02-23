package io.github.jeffshee.visualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import io.github.jeffshee.visualizer.utils.MatrixConfig
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.pow

class BarraSimplesMatriz : Painter() {
    override var paint = Paint().apply {
        style = Paint.Style.FILL
    }

    private lateinit var interpolatedFft: PolynomialSplineFunction
    private val amplificationFactor = 1.4f  // Fator para enfatizar a altura das barras
    private val smoothingFactor = 0.2f      // Fator de suavização exponencial
    private var smoothedFft: FloatArray? = null

    private val ledColumns: Int get() = MatrixConfig.ledMatrixColumns
    private val ledRows: Int get() = MatrixConfig.ledMatrixRows

    override fun calc(helper: VisualizerHelper) {
        val fft = helper.getFftMagnitudeRange(0, 2000)
        if (fft.isEmpty()) return

        // Suavização exponencial dos dados da FFT
        smoothedFft = if (smoothedFft == null) {
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            smoothedFft!!.mapIndexed { index, oldValue ->
                smoothingFactor * fft[index].toFloat() + (1 - smoothingFactor) * oldValue
            }.toFloatArray()
        }

        // Aplica modelo com gravidade para suavizar valores dinamicamente
        val gravityModels = Array(smoothedFft!!.size) { GravityModel() }
        smoothedFft!!.forEachIndexed { index, value ->
            gravityModels[index].update(value)
        }

        // Interpolação via spline para reduzir o número de pontos (ledColumns pontos)
        interpolatedFft = interpolateFft(gravityModels, ledColumns, "sp")
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        val barMargin = 1f
        val barWidth = (canvas.width - (ledColumns - 1) * barMargin) / ledColumns

        for (i in 0 until ledColumns) {
            val rawHeight = interpolatedFft.value(i.toDouble()).toFloat()
            val transformedHeight = rawHeight.pow(amplificationFactor)
            val barHeight = (transformedHeight / 3).coerceAtMost(canvas.height.toFloat())

            // Define a cor vibrante baseada no índice (rainbow)
            paint.color = getVivrantRainbowColor(i, ledColumns)

            val left = i * (barWidth + barMargin)
            val right = left + barWidth
            val top = canvas.height - barHeight
            val bottom = canvas.height.toFloat()
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    // Função auxiliar para gerar cores vibrantes no espectro do arco-íris
    private fun getVivrantRainbowColor(index: Int, totalBars: Int): Int {
        val hue = 360f * index / totalBars
        val hsvColor = floatArrayOf(hue, 1f, 1f)
        return Color.HSVToColor(hsvColor)
    }
}