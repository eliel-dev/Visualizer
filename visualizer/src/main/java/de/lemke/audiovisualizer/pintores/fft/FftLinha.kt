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

/**
 * Visualizador FFT em formato de linhas verticais
 * Similar ao FftBar mas desenha linhas simples ao invés de barras preenchidas
 */
class FftLinha(
    // Configuração de desenho (cor branca, linha, espessura 2px)
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE;style = Paint.Style.STROKE;strokeWidth = 2f
    },
    var startHz: Int = 0,           // Frequência inicial (Hz)
    var endHz: Int = 2000,          // Frequência final (Hz)
    var num: Int = 128,             // Número de linhas a desenhar
    var interpolator: Interpolator = LINEAR,  // Tipo de interpolação
    var direcao: Direcao = Cima,        // Direção das linhas
    var mirror: Boolean = false,    // Se deve espelhar os dados
    var power: Boolean = false,     // Se deve aplicar efeito de potência
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

        // Aplica efeitos opcionais nos dados
        if (power) fft = getPowerFft(fft)    // Amplifica picos
        if (mirror) fft = getMirrorFft(fft)  // Cria simetria

        // Inicializa array de gravidade se necessário
        if (points.size != fft.size) points =
            Array(fft.size) { GravityModel(0f) }
        
        // Atualiza cada ponto com gravidade e amplitude
        points.forEachIndexed { index, bar -> bar.update(fft[index].toFloat() * ampR) }

        // Interpola dados para o número desejado de linhas
        psf = interpolateFft(points, num, interpolator)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        // Pula desenho se frame foi marcado como silencioso
        if (skipFrame) return

        val width = canvas.width.toFloat()
        // Calcula espaçamento entre linhas
        val gapWidth = width / num

        // Array para armazenar pontos das linhas (4 valores por linha: x1,y1,x2,y2)
        val pts = FloatArray(4 * num)
        
        drawHelper(canvas, direcao, 0f, .5f, {
            // Desenho para direções UP_OUT e DOWN_IN (linhas simples)
            for (i in 0 until num) {
                pts[4 * i] = gapWidth * (i + .5f)      // x1 (centro da posição)
                pts[4 * i + 1] = -psf.value(i.toDouble()).toFloat()  // y1 (altura do áudio)
                pts[4 * i + 2] = gapWidth * (i + .5f)  // x2 (mesmo x)
                pts[4 * i + 3] = 0f                    // y2 (linha base)
            }
            canvas.drawLines(pts, paint)
        }, {
            // Desenho para direção BOTH (linhas bidirecionais)
            for (i in 0 until num) {
                pts[4 * i] = gapWidth * (i + .5f)      // x1 (centro da posição)
                pts[4 * i + 1] = -psf.value(i.toDouble()).toFloat()  // y1 (altura negativa)
                pts[4 * i + 2] = gapWidth * (i + .5f)  // x2 (mesmo x)
                pts[4 * i + 3] = psf.value(i.toDouble()).toFloat()   // y2 (altura positiva)
            }
            canvas.drawLines(pts, paint)
        })
    }
}