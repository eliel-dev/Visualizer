package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
 * Visualizador FFT circular em formato de linhas radiais
 * Desenha linhas que se estendem do centro para fora baseadas na amplitude do áudio
 * Cria um efeito visual similar a um equalizador circular
 */
class FftLinhaCircular(
    // Configuração de desenho (cor branca, linha, espessura 2px)
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    var startHz: Int = 0,           // Frequência inicial (Hz)
    var endHz: Int = 2000,          // Frequência final (Hz)
    var num: Int = 64,              // Número de linhas radiais
    var interpolator: Interpolator = LINEAR,  // Tipo de interpolação
    var direcao: Direcao = Cima,        // Direção das linhas
    var mirror: Boolean = false,    // Se deve espelhar os dados
    var power: Boolean = true,      // Se deve aplicar efeito de potência (padrão: sim)
    var radiusR: Float = .4f,       // Raio base do círculo (0.0 a 1.0)
    var ampR: Float = 1f,          // Multiplicador de amplitude
) : Pintor() {

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
        psf = interpolateFftCircle(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Pula desenho se frame foi marcado como silencioso
        if (skipFrame) return

        // Calcula ângulo entre cada linha radial
        val angle = 2 * PI.toFloat() / num
        // Array para armazenar pontos das linhas (4 valores por linha: x1,y1,x2,y2)
        val pts = FloatArray(4 * num)
        // Usa a menor dimensão para manter círculo proporcional
        val shortest = min(canvas.width, canvas.height)

        drawHelper(canvas, direcao, .5f, .5f, {
            // UP_OUT: linhas se estendem para fora do círculo base
            for (i in 0 until num) {
                val start = toCartesian(shortest / 2f * radiusR, angle * i)
                val stop = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i
                )
                pts[4 * i] = start[0]; pts[4 * i + 1] = start[1]      // Ponto inicial
                pts[4 * i + 2] = stop[0]; pts[4 * i + 3] = stop[1]    // Ponto final
            }
            canvas.drawLines(pts, paint)
        }, {
            // DOWN_IN: linhas se estendem para dentro do círculo base
            for (i in 0 until num) {
                val start = toCartesian(shortest / 2f * radiusR, angle * i)
                val stop = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i
                )
                pts[4 * i] = start[0]; pts[4 * i + 1] = start[1]      // Ponto inicial
                pts[4 * i + 2] = stop[0]; pts[4 * i + 3] = stop[1]    // Ponto final
            }
            canvas.drawLines(pts, paint)
        }, {
            // BOTH: linhas se estendem para ambos os lados do círculo base
            for (i in 0 until num) {
                val start = toCartesian(
                    shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i
                )
                val stop = toCartesian(
                    shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i
                )
                pts[4 * i] = start[0]; pts[4 * i + 1] = start[1]      // Ponto externo
                pts[4 * i + 2] = stop[0]; pts[4 * i + 3] = stop[1]    // Ponto interno
            }
            canvas.drawLines(pts, paint)
        })
    }
}