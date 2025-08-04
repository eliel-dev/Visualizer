package de.lemke.audiovisualizer.pintores.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.PI
import kotlin.math.min

/**
 * Visualizador FFT circular em formato de onda contínua
 * Cria uma forma circular onde o raio varia baseado na amplitude do áudio
 * Produz efeitos visuais orgânicos e fluidos
 */
class FftCOnda(
    // Configuração de desenho (cor branca, preenchimento por padrão)
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    },
    var startHz: Int = 0,           // Frequência inicial (Hz)
    var endHz: Int = 2000,          // Frequência final (Hz)
    var num: Int = 128,             // Número de pontos na onda circular
    var interpolator: Companion.Interpolator = Companion.Interpolator.SPLINE,  // Interpolação spline para suavidade
    var direcao: Companion.Direcao = Companion.Direcao.Cima,        // Direção da expansão
    var mirror: Boolean = false,    // Se deve espelhar os dados
    var power: Boolean = true,      // Se deve aplicar efeito de potência (padrão: sim)
    var radiusR: Float = .4f,       // Raio base do círculo (0.0 a 1.0)
    var ampR: Float = .6f,          // Multiplicador de amplitude (menor que outros para suavidade)
) : Pintor() {

    // Path para desenhar a onda circular como forma contínua
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

        // Otimização: pula frame se áudio estiver silencioso E for modo linha
        if (isQuiet(fft) && paint.style == Paint.Style.STROKE) {
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

        // Calcula ângulo entre cada ponto da onda circular
        val angle = 2 * PI.toFloat() / num
        // Usa a menor dimensão para manter círculo proporcional
        val shortest = min(canvas.width, canvas.height)

        drawHelper(canvas, direcao, .5f, .5f, {
            // UP_OUT: onda se expande para fora do raio base
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])  // Primeiro ponto
                else path.lineTo(point[0], point[1])         // Conecta pontos
            }
            path.close()  // Fecha a forma circular
            canvas.drawPath(path, paint)
            path.reset()
        }, {
            // DOWN_IN: cria anel - círculo externo menos círculo interno
            // Círculo externo (raio base)
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR, angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()

            // Círculo interno (raio base menos amplitude)
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()

            // Usa regra even-odd para criar "buraco" no meio
            path.fillType = Path.FillType.EVEN_ODD
            canvas.drawPath(path, paint)
            path.reset()
        }, {
            // BOTH: anel com variação em ambos os lados
            // Círculo externo (raio base + amplitude)
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR + psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()

            // Círculo interno (raio base - amplitude)
            for (i in 0..num) {
                val point = toCartesian(shortest / 2f * radiusR - psf.value(i.toDouble()).toFloat(), angle * i)
                if (i == 0) path.moveTo(point[0], point[1])
                else path.lineTo(point[0], point[1])
            }
            path.close()

            // Cria anel com espessura variável
            path.fillType = Path.FillType.EVEN_ODD
            canvas.drawPath(path, paint)
            path.reset()
        })
    }


}