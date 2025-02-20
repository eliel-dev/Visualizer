package io.github.jeffshee.visualizer.painters.fft

import android.graphics.*
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import kotlin.math.sin

/**
 * EmotiveCWaveRgb:
 *
 * Essa versão visa transmitir a "alma" da música com movimentos fluidos e orgânicos.
 * - A rotação oscila dinamicamente conforme a amplitude do áudio, criando uma sensação de dança.
 * - Vários layers rotacionados geram profundidade e um efeito mais vivo.
 * - A suavização da FFT e o uso de variação temporal resultam em transições mais naturais.
 */
class EmotiveVisualizer(
    flags: Int = Paint.ANTI_ALIAS_FLAG,
    var baseColors: List<Int> = listOf(Color.RED, Color.GREEN, Color.BLUE),
    //
    startHz: Int = 0,
    endHz: Int = 2000,
    //
    num: Int = 128,
    interpolator: String = "sp",
    //
    side: String = "a",
    mirror: Boolean = false,
    power: Boolean = true,
    //
    radiusR: Float = .4f,
    ampR: Float = .6f,
    var baseRot: Float = 10f
) : Painter() {

    override var paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private var smoothedFft: FloatArray? = null
    private val wave = FftCWave(Paint(flags).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }, startHz, endHz, num, interpolator, side, mirror, power, radiusR, ampR)

    // Variáveis para controle dinâmico
    private var avgAmplitude = 0f
    private var timeCounter = 0f  // Incremento para variação temporal

    override fun calc(helper: VisualizerHelper) {
        wave.calc(helper)
        val fft = helper.getFftMagnitudeRange(0, 2000)
        if (fft.isEmpty()) return

        // Suavização da FFT para transições mais fluidas (fator de 0.25)
        smoothedFft = if (smoothedFft == null) {
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            smoothedFft!!.mapIndexed { index, oldValue ->
                0.25f * fft[index].toFloat() + (1 - 0.25f) * oldValue
            }.toFloatArray()
        }

        // Calcula a amplitude média para modular rotação e cores
        avgAmplitude = smoothedFft?.average()?.toFloat() ?: 0f

        // Atualiza o contador temporal para criar uma oscilação suave
        timeCounter += 0.05f
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Cria um offset dinâmico para a rotação: quanto maior a amplitude, maior a oscilação
        val dynamicRotOffset = (avgAmplitude / 100f) * 20f * sin(timeCounter)

        // Primeira camada: rotação base mais oscilatória
        wave.paint.color = baseColors[0]
        rotateHelper(canvas, baseRot + dynamicRotOffset, .5f, .5f) {
            wave.draw(canvas, helper)
        }

        // Segunda camada: rotação um pouco maior
        rotateHelper(canvas, (baseRot * 2) + dynamicRotOffset, .5f, .5f) {
            wave.paint.color = baseColors[1]
            wave.draw(canvas, helper)
        }

        // Terceira camada: rotação ainda maior para profundidade
        rotateHelper(canvas, (baseRot * 3) + dynamicRotOffset, .5f, .5f) {
            wave.paint.color = baseColors[2]
            wave.draw(canvas, helper)
        }

        // Círculo central preto para cobrir áreas indesejadas e focar a visualização
        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f
        val innerRadius = minOf(centerX, centerY) / 2.6f
        val blackPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, innerRadius, blackPaint)
    }

    // Exemplo de função para gerar cor dinâmica baseada em índice e amplitude
    private fun getDynamicColor(index: Int, amplitude: Float): Int {
        val totalPoints = smoothedFft?.size ?: 1
        val hue = ((index.toFloat() / totalPoints) * 360f) % 360
        val saturation = (0.7f + (amplitude / 255f) * 0.3f).coerceIn(0f, 1f)
        val brightness = (0.6f + (amplitude / 255f) * 0.4f).coerceIn(0f, 1f)
        return Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }
}
