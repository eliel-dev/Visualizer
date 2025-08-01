package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction

/**
 * Visualizador FFT em formato de barras verticais
 * Cada barra representa uma faixa de frequência do áudio
 */
class FftBarra(
    // Configuração de desenho (cor branca, contorno, espessura 2px)
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    var startHz: Int = 0,           // Frequência inicial (Hz)
    var endHz: Int = 2000,          // Frequência final (Hz)
    var num: Int = 128,             // Número de barras a desenhar
    var interpolator: Companion.Interpolator = Companion.Interpolator.LINEAR,  // Tipo de interpolação
    var direcao: Companion.Direcao = Companion.Direcao.Cima,        // Direção das barras
    var mirror: Boolean = false,    // Se deve espelhar os dados
    var power: Boolean = false,     // Se deve aplicar efeito de potência
    var gapX: Float = 0f,          // Espaçamento entre barras
    var ampR: Float = 1f,          // Multiplicador de amplitude
) : Pintor() {

    // Path para desenhar as barras como formas geométricas
    private val path = Path()
    // Array de modelos de gravidade para suavização
    private var points = Array(0) { GravityModel() }
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
        if (power) fft = getPowerFft(fft)    // Amplifica picos
        if (mirror) fft = getMirrorFft(fft)  // Cria simetria

        // Inicializa array de gravidade se necessário
        if (points.size != fft.size) points =
            Array(fft.size) { GravityModel(0f) }

        // Atualiza cada ponto com gravidade e amplitude
        points.forEachIndexed { index, bar -> bar.update(fft[index].toFloat() * ampR) }

        // Interpola dados para o número desejado de barras
        psf = interpolateFft(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Pula desenho se frame foi marcado como silencioso
        if (skipFrame) return

        val width = canvas.width.toFloat()
        // Calcula largura de cada barra considerando espaçamentos
        val barWidth = (width - (num + 1) * gapX) / num

        drawHelper(canvas, direcao, 0f, .5f, {
            // Desenho para direções UP_OUT e DOWN_IN (barras simples)
            for (i in 0 until num) {
                // Cria retângulo para cada barra
                path.moveTo(barWidth * i + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), 0f)
                path.lineTo(barWidth * i + gapX * (i + 1), 0f)
                path.close()
            }
            canvas.drawPath(path, paint)
        }, {
            // Desenho para direção BOTH (barras bidirecionais)
            for (i in 0 until num) {
                // Cria retângulo que se estende para cima e para baixo
                path.moveTo(barWidth * i + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), -psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * (i + 1) + gapX * (i + 1), psf.value(i.toDouble()).toFloat())
                path.lineTo(barWidth * i + gapX * (i + 1), psf.value(i.toDouble()).toFloat())
                path.close()
            }
            canvas.drawPath(path, paint)
        })
        // Limpa o path para próximo frame
        path.reset()
    }
}