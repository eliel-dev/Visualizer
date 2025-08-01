package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

/**
 * Visualizador FFT em formato de onda contínua
 * Conecta os pontos FFT criando uma forma de onda suave
 */
class FftOnda(
    // Configuração de desenho (cor branca, preenchimento por padrão)
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    },
    var startHz: Int = 0,           // Frequência inicial (Hz)
    var endHz: Int = 2000,          // Frequência final (Hz)
    var num: Int = 128,             // Número de pontos na onda
    var interpolator: Companion.Interpolator = Companion.Interpolator.LINEAR,  // Tipo de interpolação
    var direcao: Companion.Direcao = Companion.Direcao.Cima,        // Direção da onda
    var mirror: Boolean = false,    // Se deve espelhar os dados
    var power: Boolean = false,     // Se deve aplicar efeito de potência
    var ampR: Float = 1f,          // Multiplicador de amplitude
) : Pintor() {

    // Path para desenhar a onda como forma contínua
    private val path = Path()
    // Array de modelos de gravidade para suavização
    private var points = Array(0) { GravityModel() }
    // Flag para pular frames quando silencioso
    private var skipFrame = false
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
        if (power) fft = getPowerFft(fft)    // Amplifica picos
        if (mirror) fft = getMirrorFft(fft)  // Cria simetria

        // Inicializa array de gravidade se necessário
        if (points.size != fft.size) points =
            Array(fft.size) { GravityModel(0f) }

        // Atualiza cada ponto com gravidade e amplitude
        points.forEachIndexed { index, bar -> bar.update(fft[index].toFloat() * ampR) }

        // Interpola dados para o número desejado de pontos na onda
        psf = interpolateFft(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Pula desenho se frame foi marcado como silencioso
        if (skipFrame) return

        val width = canvas.width.toFloat()
        // Calcula largura de cada segmento da onda
        val sliceWidth = width / num

        if (paint.style == Paint.Style.STROKE) {
            // Modo linha: desenha apenas o contorno da onda
            path.moveTo(0f, -psf.value(0.0).toFloat())
            for (i in 1..num) {
                path.lineTo(sliceWidth * i, -psf.value(i.toDouble()).toFloat())
            }
        } else {
            // Modo preenchimento: cria forma fechada para preencher
            path.moveTo(0f, 1f)  // Começa na linha base
            for (i in 0..num) {
                // Conecta pontos da onda
                path.lineTo(sliceWidth * i, -psf.value(i.toDouble()).toFloat())
            }
            path.lineTo(width, 1f)  // Volta para linha base
            path.close()  // Fecha a forma
        }

        // Desenha a onda usando o helper de direção
        drawHelper(canvas, direcao, 0f, .5f) { canvas.drawPath(path, paint) }

        // Limpa o path para próximo frame
        path.reset()
    }
}