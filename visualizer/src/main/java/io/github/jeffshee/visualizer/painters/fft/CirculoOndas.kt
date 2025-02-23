package io.github.jeffshee.visualizer.painters.fft

import android.graphics.*
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.painters.modificadores.CirculoWave
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
class CirculoOndas(
    flags: Int = Paint.ANTI_ALIAS_FLAG,
    var baseColors: List<Int> = listOf(Color.RED, Color.GREEN, Color.BLUE),
    startHz: Int = 0,
    endHz: Int = 2000,
    num: Int = 128,
    interpolator: String = "sp",
    side: String = "a",
    mirror: Boolean = false,
    power: Boolean = true,
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
    private val wave = CirculoWave(Paint(flags).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }, startHz, endHz, num, interpolator, side, mirror, power, radiusR, ampR)

    private var avgAmplitude = 0f
    private var timeCounter = 0f

    override fun calc(helper: VisualizerHelper) {
        wave.calc(helper)
        val fft = helper.getFftMagnitudeRange(0, 2000)
        if (fft.isEmpty()) return

        smoothedFft = if (smoothedFft == null) {
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            smoothedFft!!.mapIndexed { index, oldValue ->
                0.25f * fft[index].toFloat() + (1 - 0.25f) * oldValue
            }.toFloatArray()
        }

        avgAmplitude = smoothedFft?.average()?.toFloat() ?: 0f
        timeCounter += 0.05f
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        val dynamicRotOffset = (avgAmplitude / 100f) * 20f * sin(timeCounter)

        wave.paint.color = baseColors[0]
        rotateHelper(canvas, baseRot + dynamicRotOffset, .5f, .5f) {
            wave.draw(canvas, helper)
        }

        rotateHelper(canvas, (baseRot * 2) + dynamicRotOffset, .5f, .5f) {
            wave.paint.color = baseColors[1]
            wave.draw(canvas, helper)
        }

        rotateHelper(canvas, (baseRot * 3) + dynamicRotOffset, .5f, .5f) {
            wave.paint.color = baseColors[2]
            wave.draw(canvas, helper)
        }

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

    private fun getDynamicColor(index: Int, amplitude: Float): Int {
        val totalPoints = smoothedFft?.size ?: 1
        val hue = ((index.toFloat() / totalPoints) * 360f) % 360
        val saturation = (0.7f + (amplitude / 255f) * 0.3f).coerceIn(0f, 1f)
        val brightness = (0.6f + (amplitude / 255f) * 0.4f).coerceIn(0f, 1f)
        return Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }
}
