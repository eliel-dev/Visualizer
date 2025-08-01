package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator.LINEAR
import de.lemke.audiovisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

/**
 * Visualizador FFT em formato analógico/vintage
 * Cria uma onda que alterna entre valores positivos e negativos
 * Simula o visual de osciloscópios antigos ou equipamentos analógicos
 */
class FftAnalogico(
    // Configuração de desenho (cor branca, linha, espessura 2px)
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    var startHz: Int = 0,           // Frequência inicial (Hz)
    var endHz: Int = 2000,          // Frequência final (Hz)
    var num: Int = 128,             // Número de pontos na onda
    var interpolator: Interpolator = LINEAR,  // Tipo de interpolação
    var mirror: Boolean = false,    // Se deve espelhar os dados
    var power: Boolean = false,     // Se deve aplicar efeito de potência (padrão: não)
    var ampR: Float = 1f,          // Multiplicador de amplitude
) : Pintor() {

    // Array de modelos de gravidade para suavização
    private var points = Array(0) { GravityModel() }
    // Path para desenhar a onda analógica
    private val path = Path()
    // Flag para pular frames quando silencioso
    private var skipFrame = true
    // Dados FFT processados
    lateinit var fft: DoubleArray
    // Função de interpolação para suavizar dados
    lateinit var psf: PolynomialSplineFunction

    override fun calc(helper: VisualizerHelper) {
        // Obtém dados FFT na faixa de frequência especificada
        fft = helper.getFftMagnitudeRange(startHz, endHz)

        // Otimização: pula frame se áudio estiver muito baixo
        if (isQuiet(fft)) {
            skipFrame = true
            return
        } else skipFrame = false

        // Aplica efeitos opcionais nos dados
        if (power) fft = getPowerFft(fft)    // Amplifica picos (raramente usado aqui)
        if (mirror) fft = getMirrorFft(fft)  // Cria simetria

        // Inicializa array de gravidade se necessário
        if (points.size != fft.size) points =
            Array(fft.size) { GravityModel(0f) }
        
        // Atualiza cada ponto com gravidade e amplitude
        points.forEachIndexed { index, bar -> bar.update(fft[index].toFloat() * ampR) }

        // Interpola dados para o número desejado de pontos
        psf = interpolateFft(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Pula desenho se frame foi marcado como silencioso
        if (skipFrame) return

        val width = canvas.width.toFloat()
        // Calcula espaçamento entre pontos
        val gapWidth = width / num

        // Sempre desenha na direção UP_OUT (efeito analógico específico)
        drawHelper(canvas, Cima, 0f, .5f) {
            for (i in 0 until num) {
                if (i % 2 == 0) {
                    // Pontos pares: valores negativos (para baixo)
                    if (i == 0)
                        path.moveTo(0f, -psf.value(0.toDouble()).toFloat())
                    else
                        path.lineTo(gapWidth * i, -psf.value(i.toDouble()).toFloat())
                } else {
                    // Pontos ímpares: valores positivos (para cima)
                    // Cria o efeito de alternância característico do visual analógico
                    path.lineTo(gapWidth * i, psf.value(i.toDouble()).toFloat())
                }
            }
            canvas.drawPath(path, paint)
        }
        // Limpa o path para próximo frame
        path.reset()
    }
}