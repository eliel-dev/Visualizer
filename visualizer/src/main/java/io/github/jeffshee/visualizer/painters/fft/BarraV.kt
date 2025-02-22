package io.github.jeffshee.visualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper

class BarraV : Painter() {
    override var paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val numberOfLeds = 100 // Número de LEDs
    private var smoothedFft: FloatArray? = null
    private val smoothingFactor = 0.3f // Fator de suavização reduzido para maior reatividade

    // Método para calcular os valores da FFT
    override fun calc(helper: VisualizerHelper) {
        val fft = helper.getFftMagnitudeRange(0, 2000)

        // Verificar se há dados suficientes de FFT
        if (fft.isEmpty()) return

        // Aplicar uma suavização exponencial aos dados da FFT
        smoothedFft = if (smoothedFft == null) {
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            smoothedFft!!.mapIndexed { index, oldValue ->
                smoothingFactor * fft[index].toFloat() + (1 - smoothingFactor) * oldValue
            }.toFloatArray()
        }
    }

    // Método para desenhar os LEDs
    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        val centerX = canvas.width / 2f
        val ledWidth = 90f
        val ledHeight = canvas.height / (numberOfLeds * 1.2f)

        // Ajustar para mais sensibilidade e amplificação
        val maxValue = 50f // Reduzir valor máximo para aumentar sensibilidade
        val amplificationFactor = 0.7f // Aumentar o fator de amplificação para mais movimento
        val averageMagnitude = ((smoothedFft?.average()?.toFloat() ?: 0f) * amplificationFactor) / maxValue
        val ledsOn = (averageMagnitude * numberOfLeds).toInt().coerceIn(0, numberOfLeds)

        for (i in 0 until numberOfLeds) {
            // Definir cor usando um gradiente de vermelho para verde
            paint.color = if (i < ledsOn) getLedColor(i) else Color.DKGRAY

            // Calcular as coordenadas de cada LED
            val left = centerX - ledWidth / 2
            val right = centerX + ledWidth / 2
            val top = (canvas.height - (i + 1) * ledHeight) - (i * ledHeight * 0.2f)
            val bottom = top + ledHeight

            // Desenhar o LED
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    // Gradiente de vermelho para verde baseado no índice do LED
    private fun getLedColor(index: Int): Int {
        val hue = 120f - (240f * index / numberOfLeds) // Transição de vermelho a verde
        val hsvColor = floatArrayOf(hue, 1f, 1f)
        return Color.HSVToColor(hsvColor)
    }
}
