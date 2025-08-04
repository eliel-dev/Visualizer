package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.pow

/**
 * Visualizador FFT com barras horizontais coloridas em RGB
 * Cria barras centralizadas com cores vibrantes do arco-íris
 */
class BarrasHRGBLogaritimico(
    override var paint: Paint = Paint().apply { style = Paint.Style.FILL },
    var startHz: Int = 0,                    // Frequência inicial
    var endHz: Int = 2000,                   // Frequência final
    var numberOfBars: Int = 24,              // Número de barras
    var barWidth: Float = 10f,               // Largura de cada barra
    var barMargin: Float = 50f,              // Espaçamento entre barras
    var amplificationFactor: Float = 1f,   // Fator de amplificação da altura
    var smoothingFactor: Float = 0.2f,       // Fator de suavização exponencial
    var interpolationIntensity: Float = 1f,  // Parâmetro para controlar a intensidade do efeito de interpolação
    var interpolator: Companion.Interpolator = Companion.Interpolator.LOGARITHMIC, // Tipo de interpolação
) : Pintor() {

    // Função de interpolação para suavizar dados FFT
    private lateinit var interpolatedFft: PolynomialSplineFunction
    // Array para suavização exponencial dos dados
    private var smoothedFft: FloatArray? = null
    // Array de modelos de gravidade para cada barra
    private var gravityModels = Array(0) { GravityModel() }
    // Flag para otimização quando áudio está silencioso
    private var skipFrame = true

    override fun calc(helper: VisualizerHelper) {
        // Obtém dados FFT na faixa de frequência especificada
        val fft = helper.getFftMagnitudeRange(startHz, endHz)

        // Otimização: pula frame se não há dados ou áudio muito baixo
        if (fft.isEmpty() || isQuiet(fft)) {
            skipFrame = true
            return
        } else skipFrame = false

        // Suavização exponencial dos dados FFT
        smoothedFft = if (smoothedFft == null) {
            // Primeira execução: usa dados FFT diretamente
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            // Suavização: mistura dados novos com anteriores
            smoothedFft!!.mapIndexed { index, oldValue ->
                smoothingFactor * fft[index].toFloat() + (1 - smoothingFactor) * oldValue
            }.toFloatArray()
        }

        // Inicializa modelos de gravidade se necessário
        if (gravityModels.size != smoothedFft!!.size) {
            gravityModels = Array(smoothedFft!!.size) { GravityModel() }
        }

        // Aplica modelo de gravidade para suavizar valores dinamicamente
        smoothedFft!!.forEachIndexed { index, value ->
            gravityModels[index].update(value)
        }

        // Interpolação via spline para reduzir/aumentar número de pontos
        interpolatedFft = interpolateFft(gravityModels, numberOfBars, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Pula desenho se frame foi marcado como silencioso
        if (skipFrame) return

        // Calcula dimensões e posicionamento
        val totalBarWidth = numberOfBars * barWidth + (numberOfBars - 1) * barMargin
        val startX = (canvas.width - totalBarWidth) / 2f  // Centraliza horizontalmente
        val centerY = canvas.height / 2f                  // Centro vertical

        // Desenha cada barra
        for (i in 0 until numberOfBars) {
            // Obtém altura da barra e aplica amplificação
            val rawHeight = interpolatedFft.value(i.toDouble()).toFloat()
            val transformedHeight = rawHeight.pow(amplificationFactor)
            val barHeight = transformedHeight / 3f  // Normaliza a altura

            // Define cor vibrante baseada no índice (arco-íris)
            paint.color = getVibrantRainbowColor(i, numberOfBars)

            // Calcula posição da barra
            val left = startX + i * (barWidth + barMargin)
            val right = left + barWidth
            val top = centerY - barHeight      // Parte superior
            val bottom = centerY + barHeight   // Parte inferior (simétrica)

            // Desenha a barra como retângulo
            canvas.drawRect(left, top, right, bottom, paint)
        }
    }

    /**
     * Gera cores vibrantes no espectro do arco-íris
     * @param index Índice da barra atual
     * @param totalBars Número total de barras
     * @return Cor RGB vibrante
     */
    private fun getVibrantRainbowColor(index: Int, totalBars: Int): Int {
        // Calcula matiz (hue) distribuído uniformemente no espectro
        val hue = 360f * index / totalBars
        // Usa saturação e valor máximos para cores vibrantes
        val hsvColor = floatArrayOf(hue, 1f, 1f)
        return Color.HSVToColor(hsvColor)
    }
}