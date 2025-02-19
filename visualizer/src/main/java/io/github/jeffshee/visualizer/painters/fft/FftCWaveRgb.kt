package io.github.jeffshee.visualizer.painters.fft

import android.graphics.*
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper

class FftCWaveRgb(
    flags: Int = Paint.ANTI_ALIAS_FLAG,
    var color: List<Int> = listOf(Color.RED, Color.GREEN, Color.BLUE),
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
    var rot: Float = 10f
) : Painter() {

    override var paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true // Habilita anti-aliasing para suavizar a onda
    }
    private var smoothedFft: FloatArray? = null
    private val wave = FftCWave(Paint(flags).apply {
        style = Paint.Style.FILL;xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }, startHz, endHz, num, interpolator, side, mirror, power, radiusR, ampR)

    override fun calc(helper: VisualizerHelper) {
        wave.calc(helper)
        val fft = helper.getFftMagnitudeRange(0, 2000)
        if (fft.isEmpty()) return
        smoothedFft = if (smoothedFft == null) {
            fft.map { it.toFloat() }.toFloatArray()
        } else {
            smoothedFft!!.mapIndexed { index, oldValue ->
                0.3f * fft[index].toFloat() + (1 - 0.3f) * oldValue
            }.toFloatArray()
        }
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Desenha as ondas circulares
        wave.paint.color = color[0]
        wave.draw(canvas, helper)
        rotateHelper(canvas, rot, .5f, .5f) {
            wave.paint.color = color[1]
            wave.draw(canvas, helper)
        }
        rotateHelper(canvas, rot * 2, .5f, .5f) {
            wave.paint.color = color[2]
            wave.draw(canvas, helper)
        }
        // Desenha o círculo central preto para cobrir totalmente a área branca
        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f
        val innerRadius = Math.min(centerX, centerY) / 2.6f // Aumentado para cobrir a área branca
        val blackPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(centerX, centerY, innerRadius, blackPaint)
    }

    // Função para gerar uma cor dinâmica baseada no índice e amplitude
    private fun getDynamicColor(index: Int, amplitude: Float): Int {
        val totalPoints = smoothedFft?.size ?: 1
        val hue = ((index.toFloat() / totalPoints) * 360f) % 360
        // Modula saturação e brilho conforme a amplitude
        val saturation = (0.7f + (amplitude / 255f) * 0.3f).coerceIn(0f, 1f)
        val brightness = (0.6f + (amplitude / 255f) * 0.4f).coerceIn(0f, 1f)
        return Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    }
}