package io.github.jeffshee.visualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.pow

class BarrasVerticaisNormal : Painter() {
    override var paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private lateinit var interpolatedFft: PolynomialSplineFunction
    private val amplificationFactor = 1.4f // Ajuste conforme necessário para conter as barras
    private val smoothingFactor = 0.2f // Fator de suavização exponencial

    private var smoothedFft: FloatArray? = null

    override fun calc(helper: VisualizerHelper) {
        val fft = helper.getFftMagnitudeRange(0, 2000) // Usar a mesma faixa de frequências do Waveform

        // Aplicar uma suavização exponencial aos dados da FFT
        smoothedFft = if (smoothedFft == null) {
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            smoothedFft!!.mapIndexed { index, oldValue ->
                smoothingFactor * fft[index].toFloat() + (1 - smoothingFactor) * oldValue
            }.toFloatArray()
        }

        val gravityModels = Array(smoothedFft!!.size) { GravityModel() }
        smoothedFft!!.forEachIndexed { index, value -> gravityModels[index].update(value) }
        interpolatedFft = interpolateFft(gravityModels, 25, "sp") // Usar interpolação com spline para suavizar os dados
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        val numberOfBars = 24 // Número de barras
        val barMargin = 50f // Aumentar a margem entre as barras para ampliar a visualização
        val barWidth = 10f // Aumentar a espessura das barras

        // Calcula o offset para centralizar as barras horizontalmente
        val totalBarWidth = numberOfBars * barWidth + (numberOfBars - 1) * barMargin
        val startX = (canvas.width - totalBarWidth) / 2f // Ajuste para centralizar as barras horizontalmente
        val centerY = canvas.height / 2f

        for (i in 0 until numberOfBars) {
            val rawHeight = interpolatedFft.value(i.toDouble()).toFloat()
            val transformedHeight = rawHeight.pow(amplificationFactor)
            val barHeight = (transformedHeight / 3) // Ajuste da amplificação e racionalização para contenção

            // Cálculo da cor no espectro do arco-íris baseada no índice da barra com cores vibrantes
            paint.color = getVivrantRainbowColor(i, numberOfBars)

            val left = startX + i * (barWidth + barMargin)
            val right = left + barWidth
            val top = centerY - barHeight
            val bottom = centerY + barHeight
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    // Função auxiliar para calcular a cor RGB vibrante no espectro do arco-íris com base no índice
    private fun getVivrantRainbowColor(index: Int, totalBars: Int): Int {
        val hue = 360f * index / totalBars // Calcula a matiz (de 0 a 360 graus)
        val hsvColor = floatArrayOf(
            hue, // Matiz (Hue)
            1f, // Saturação máxima (Saturation)
            1f  // Valor máximo para luminância vibrante (Value)
        )
        return Color.HSVToColor(hsvColor)
    }
}