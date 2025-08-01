package de.lemke.audiovisualizer.pintores.fft

import android.graphics.*
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator.SPLINE
import de.lemke.audiovisualizer.utils.VisualizerHelper

/**
 * Visualizador FFT circular em formato de onda com efeito RGB rotativo
 * Combina três ondas circulares coloridas com rotações diferentes
 * Cria padrões caleidoscópicos e efeitos psicodélicos
 */
class FftOndaCircularRgb(
    flags: Int = Paint.ANTI_ALIAS_FLAG,
    // Cores das três ondas rotativas (padrão: vermelho, verde, azul)
    var color: List<Int> = listOf(Color.RED, Color.GREEN, Color.BLUE),
    startHz: Int = 0,           // Frequência inicial (Hz)
    endHz: Int = 2000,          // Frequência final (Hz)
    num: Int = 128,             // Número de pontos na onda circular
    interpolator: Interpolator = SPLINE,  // Interpolação spline para suavidade
    direcao: Direcao = Cima,        // Direção da expansão
    mirror: Boolean = false,    // Se deve espelhar os dados
    power: Boolean = true,      // Se deve aplicar efeito de potência
    radiusR: Float = .4f,       // Raio base do círculo
    ampR: Float = .5f,          // Multiplicador de amplitude
    var rot: Float = 10f,       // Ângulo de rotação entre as ondas (graus)
) : Pintor() {

    override var paint: Paint = Paint()
    
    // Onda circular base com modo de mistura aditiva para efeitos RGB
    private val wave = FftCOnda(Paint(flags).apply {
        style = Paint.Style.FILL  // Preenchimento para permitir sobreposição
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)  // Modo aditivo para cores vibrantes
    }, startHz, endHz, num, interpolator, direcao, mirror, power, radiusR, ampR)

    override fun calc(helper: VisualizerHelper) {
        // Delega o cálculo para a onda circular base
        wave.calc(helper)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Primeira onda (sem rotação) - cor base
        wave.paint.color = color[0]
        wave.draw(canvas, helper)
        
        // Segunda onda rotacionada - cria sobreposição colorida
        rotateHelper(canvas, rot, .5f, .5f) {
            wave.paint.color = color[1]
            wave.draw(canvas, helper)
        }
        
        // Terceira onda com rotação dupla - completa o efeito RGB
        rotateHelper(canvas, rot * 2, .5f, .5f) {
            wave.paint.color = color[2]
            wave.draw(canvas, helper)
        }
    }
}