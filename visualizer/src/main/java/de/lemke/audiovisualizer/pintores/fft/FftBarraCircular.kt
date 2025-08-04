package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator.LINEAR
import de.lemke.audiovisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.PI
import kotlin.math.min

/**
 * Visualizador FFT circular em formato de barras radiais
 * Cria barras que se estendem radialmente do centro, similar a um equalizador circular
 * Cada barra representa uma faixa de frequência
 */
class FftBarraCircular(
    // Configuração de desenho (cor branca, contorno, espessura 2px)
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    var startHz: Int = 0,           // Frequência inicial (Hz)
    var endHz: Int = 2000,          // Frequência final (Hz)
    var num: Int = 64,              // Número de barras radiais
    var interpolator: Interpolator = LINEAR,  // Tipo de interpolação
    var direcao: Direcao = Cima,        // Direção das barras
    var mirror: Boolean = false,    // Se deve espelhar os dados
    var power: Boolean = true,      // Se deve aplicar efeito de potência (padrão: sim)
    var radiusR: Float = .4f,       // Raio base do círculo (0.0 a 1.0)
    var gapX: Float = 0f,          // Espaçamento angular entre barras
    var ampR: Float = 1f,          // Multiplicador de amplitude
) : Pintor() {

    // Path para desenhar as barras como formas trapezoidais
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

        // Aplica efeito de potência se habilitado (padrão para visualizações circulares)
        if (power) fft = getPowerFft(fft)

        // Prepara dados para formato circular
        fft = if (mirror) getMirrorFft(fft)      // Espelha dados se solicitado
        else getCircleFft(fft)                   // Ou prepara para círculo contínuo

        // Inicializa array de gravidade se necessário
        if (points.size != fft.size) points = Array(fft.size) { GravityModel(0f) }

        // Atualiza cada ponto com gravidade e amplitude
        points.forEachIndexed { index, bar -> bar.update(fft[index].toFloat() * ampR) }

        // Interpola dados especificamente para visualizações circulares
        psf = interpoladorFftCircular(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Pula desenho se frame foi marcado como silencioso
        if (skipFrame) return

        val shortest = min(canvas.width, canvas.height)
        // Converte espaçamento linear em ângulo
        val gapTheta = gapX / (shortest / 2f * radiusR)
        // Ângulo de cada barra considerando espaçamentos
        val angle = 2 * PI.toFloat() / num - gapTheta

        drawHelper(canvas, direcao, .5f, .5f, {
            // UP_OUT: barras se estendem para fora do círculo base
            for (i in 0 until num) {
                // Calcula os 4 pontos de cada barra trapezoidal
                val start1 = toCartesian(shortest / 2f * radiusR, (angle + gapTheta) * i)
                val stop1 = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                )
                val start2 = toCartesian(shortest / 2f * radiusR, (angle) * (i + 1) + gapTheta * i)
                val stop2 = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                )

                // Desenha barra como trapézio
                path.moveTo(start1[0], start1[1])  // Ponto interno esquerdo
                path.lineTo(stop1[0], stop1[1])   // Ponto externo esquerdo
                path.lineTo(stop2[0], stop2[1])   // Ponto externo direito
                path.lineTo(start2[0], start2[1]) // Ponto interno direito
                path.close()
            }
            canvas.drawPath(path, paint)
        }, {
            // DOWN_IN: barras se estendem para dentro do círculo base
            for (i in 0 until num) {
                val start1 = toCartesian(shortest / 2f * radiusR, (angle + gapTheta) * i)
                val stop1 = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                )
                val start2 = toCartesian(shortest / 2f * radiusR, (angle) * (i + 1) + gapTheta * i)
                val stop2 = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                )

                path.moveTo(start1[0], start1[1])
                path.lineTo(stop1[0], stop1[1])
                path.lineTo(stop2[0], stop2[1])
                path.lineTo(start2[0], start2[1])
                path.close()
            }
            canvas.drawPath(path, paint)
        }, {
            // BOTH: barras se estendem para ambos os lados do círculo base
            for (i in 0 until num) {
                val start1 = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                )
                val stop1 = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), (angle + gapTheta) * i
                )
                val start2 = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                )
                val stop2 = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), (angle) * (i + 1) + gapTheta * i
                )

                path.moveTo(start1[0], start1[1])  // Ponto externo esquerdo
                path.lineTo(stop1[0], stop1[1])   // Ponto interno esquerdo
                path.lineTo(stop2[0], stop2[1])   // Ponto interno direito
                path.lineTo(start2[0], start2[1]) // Ponto externo direito
                path.close()
            }
            canvas.drawPath(path, paint)
        })
        // Limpa o path para próximo frame
        path.reset()
    }
}