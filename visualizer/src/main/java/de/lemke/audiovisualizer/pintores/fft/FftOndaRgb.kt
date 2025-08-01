package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima


/**
 * Visualizador FFT em formato de onda com efeito RGB
 * Cria três ondas sobrepostas (vermelha, verde, azul) com modo de mistura aditiva
 * Produz efeitos coloridos vibrantes quando as ondas se sobrepõem
 */
class FftOndaRgb(
    flags: Int = Paint.ANTI_ALIAS_FLAG,
    // Cores das três ondas (padrão: vermelho, verde, azul)
    var color: List<Int> = listOf(Color.RED, Color.GREEN, Color.BLUE),
    startHz: Int = 0,           // Frequência inicial (Hz)
    endHz: Int = 2000,          // Frequência final (Hz)
    num: Int = 128,             // Número de pontos na onda
    interpolator: Companion.Interpolator = Companion.Interpolator.SPLINE,  // Interpolação spline para suavidade
    direcao: Companion.Direcao = Companion.Direcao.Cima,        // Direção da onda
    mirror: Boolean = false,    // Se deve espelhar os dados
    power: Boolean = false,     // Se deve aplicar efeito de potência
    ampR: Float = 1f,          // Multiplicador de amplitude
) : Pintor() {

    override var paint: Paint = Paint()
    private val wave = FftOnda(Paint(flags).apply {
        style = Paint.Style.FILL;xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }, startHz, endHz, num, interpolator, direcao, mirror, power, ampR)

    override fun calc(helper: VisualizerHelper) {
        wave.calc(helper)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        canvas.save()
        canvas.scale(1.2f, 1f, canvas.width / 2f, canvas.height.toFloat())
        drawHelper(canvas, Cima, -.03f, 0f) {
            wave.paint.color = color[0]
            wave.draw(canvas, helper)
        }
        drawHelper(canvas, Cima, 0f, 0f) {
            wave.paint.color = color[1]
            wave.draw(canvas, helper)
        }
        drawHelper(canvas, Cima, .03f, 0f) {
            wave.paint.color = color[2]
            wave.draw(canvas, helper)
        }
        canvas.restore()
    }
}
