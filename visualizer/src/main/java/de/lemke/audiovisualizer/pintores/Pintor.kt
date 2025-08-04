package de.lemke.audiovisualizer.pintores

import android.graphics.Canvas
import android.graphics.Paint
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator.LINEAR
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator.SPLINE
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Interpolator.LOGARITHMIC
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima_Baixo
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Baixo
import de.lemke.audiovisualizer.utils.VisualizerHelper
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.cos
import kotlin.math.sin

abstract class Pintor {
    companion object {
        // Direções de desenho das visualizações
        enum class Direcao {
            Cima,   // Para cima/para fora
            Baixo,  // Para baixo/para dentro
            Cima_Baixo,      // Ambas as direções
        }

        // Tipos de interpolação para suavizar dados FFT
        enum class Interpolator {
            LINEAR,     // Interpolação linear
            SPLINE,     // Interpolação spline (mais suave)
            LOGARITHMIC // Interpolação logarítmica (enfatiza frequências baixas)
        }
    }

    // Interpoladores para processamento de dados FFT
    private val linear = LinearInterpolator()
    private val spline = AkimaSplineInterpolator()

    // Paint abstrato que cada painter deve implementar para definir estilo de desenho
    abstract var paint: Paint

    /**
     * Função abstrata que todos os painters devem implementar para fazer seus cálculos.
     * Aqui é onde os dados de áudio são processados e preparados para desenho.
     *
     * @param helper o helper do visualizador que contém dados de áudio
     */
    abstract fun calc(helper: VisualizerHelper)

    /**
     * Função abstrata que todos os painters devem implementar para desenhar na tela.
     * Aqui é onde a visualização é efetivamente renderizada no canvas.
     *
     * @param canvas o canvas onde desenhar a visualização
     * @param helper o helper do visualizador que contém dados de áudio
     */
    abstract fun draw(canvas: Canvas, helper: VisualizerHelper)

    /**
     * Interpola o espectro FFT para criar mais pontos de dados
     *
     * O Android não captura um espectro de alta resolução, e queremos que o número de bandas seja ajustável,
     * então fazemos uma interpolação aqui.
     *
     * (Por exemplo, para mostrar 64 bandas de frequências de 0Hz a 1200Hz, o Android retorna apenas ~10 valores FFT.
     * Então precisamos interpolar esses ~10 valores em 64 valores para encaixar em nossas bandas)
     *
     * @param gravityModels Array de modelos de gravidade (dados FFT processados)
     * @param sliceNum Número de fatias/bandas desejadas
     * @param interpolator Qual interpolador usar (LINEAR ou SPLINE)
     *
     * @return uma `PolynomialSplineFunction` (psf). Para obter o valor, use
     * `psf.value(x)`, onde `x` deve ser um valor Double de 0 a `num`
     */
    fun interpolateFft(
        gravityModels: Array<GravityModel>, sliceNum: Int, interpolator: Interpolator, logarithmicIntensity: Float = 1f,
    ): PolynomialSplineFunction {
        val nRaw = gravityModels.size
        val xRaw = DoubleArray(nRaw) { (it * sliceNum).toDouble() / (nRaw - 1) }
        val yRaw = DoubleArray(nRaw)
        gravityModels.forEachIndexed { index, bar -> yRaw[index] = bar.height.toDouble() }
        val psf: PolynomialSplineFunction = when (interpolator) {
            LINEAR -> linear.interpolate(xRaw, yRaw)
            SPLINE -> spline.interpolate(xRaw, yRaw)
            else -> linear.interpolate(xRaw, yRaw)
        }
        return psf
    }

    /**
     * Interpola o espectro FFT para visualizações circulares
     *
     * Similar ao `interpolateFft()`. Porém este é destinado para FFT de `getCircleFft()`
     * que conecta o início e fim para formar um círculo perfeito.
     *
     * @param gravityModels Array de modelos de gravidade
     * @param sliceNum Número de fatias/bandas
     * @param interpolador Qual interpolador usar (LINEAR ou SPLINE)
     *
     * @return uma `PolynomialSplineFunction` (psf). Para obter o valor, use
     * `psf.value(x)`, onde `x` deve ser um valor Double de 0 a `num`
     */
    fun interpoladorFftCircular(
        gravityModels: Array<GravityModel>, sliceNum: Int, interpolador: Interpolator, logarithmicIntensity: Float = 1f,
    ): PolynomialSplineFunction {
        val nRaw = gravityModels.size
        val xRaw = DoubleArray(nRaw) { ((it - 1) * sliceNum).toDouble() / (nRaw - 1 - 2) }
        val yRaw = DoubleArray(nRaw)
        gravityModels.forEachIndexed { index, bar -> yRaw[index] = bar.height.toDouble() }
        val psf: PolynomialSplineFunction = when (interpolador) {
            LINEAR -> linear.interpolate(xRaw, yRaw)
            SPLINE -> spline.interpolate(xRaw, yRaw)
            else -> linear.interpolate(xRaw, yRaw)
        }
        return psf
    }



    /**
     * Verifica se o áudio está silencioso o suficiente para pular o desenho
     * Otimização de performance - não desenha quando não há áudio significativo
     *
     * @param fft Dados FFT para verificar
     * @return true se estiver silencioso, false caso contrário
     */
    fun isQuiet(fft: DoubleArray): Boolean {
        val threshold = 5f  // Limite de silêncio
        fft.forEach { if (it > threshold) return false }
        return true
    }

    /**
     * Converte coordenadas polares para cartesianas
     * Útil para visualizações circulares onde trabalhamos com ângulos e raios
     *
     * @param radius Raio (distância do centro)
     * @param theta Ângulo em radianos
     * @return FloatArray com (x,y) em coordenadas cartesianas
     */
    fun toCartesian(radius: Float, theta: Float): FloatArray {
        val x = radius * cos(theta)
        val y = radius * sin(theta)
        return floatArrayOf(x, y)
    }

    /**
     * Modifica o FFT para que o início e fim se conectem perfeitamente em visualizações circulares.
     * Use com `interpolateFftCircle()` para criar círculos sem descontinuidades.
     *
     * Transformação: `[0, 1, ..., n] -> [n-1, 0, 1, ..., n-1, 0, 1]`
     *
     * @param fft Dados FFT originais
     * @return FFT modificado para uso circular
     */
    fun getCircleFft(fft: DoubleArray): DoubleArray {
        val patched = DoubleArray(fft.size + 2)
        fft.forEachIndexed { index, d -> patched[index + 1] = d }
        patched[0] = fft[fft.lastIndex - 1]
        patched[patched.lastIndex - 1] = fft[0]
        patched[patched.lastIndex] = fft[1]
        return patched
    }

    /**
     * Modifica o FFT para criar efeitos espelhados/simétricos
     * Útil para visualizações que precisam de simetria ou efeitos especiais
     *
     * @param fft Dados FFT originais
     * @param mode Modo de espelhamento:
     *             0 -> não faz nada
     *             1 -> `[0, 1, ..., n] -> [n, ..., 1, 0, 0, 1, ..., n]` (espelho invertido + original)
     *             2 -> `[0, 1, ..., n] -> [0, 1, ..., n, n, ..., 1, 0]` (original + espelho invertido)
     *             3 -> `[0, 1, ..., n] -> [n/2, ..., 1, 0, 0, 1, ..., n/2]` (metade espelhada)
     *             4 -> `[0, 1, ..., n] -> [0, 1, ..., n/2, n/2, ..., 1, 0]` (metade + metade espelhada)
     * @return FFT espelhado conforme o modo escolhido
     */
    fun getMirrorFft(fft: DoubleArray, mode: Int = 1): DoubleArray {
        return when (mode) {
            1 -> {
                fft.sliceArray(0..fft.lastIndex).reversedArray() + fft.sliceArray(0..fft.lastIndex)
            }

            2 -> {
                fft.sliceArray(0..fft.lastIndex) + fft.sliceArray(0..fft.lastIndex).reversedArray()
            }

            3 -> {
                fft.sliceArray(0..fft.lastIndex / 2).reversedArray() + fft.sliceArray(0..fft.lastIndex / 2)
            }

            4 -> {
                fft.sliceArray(0..fft.lastIndex / 2) + fft.sliceArray(0..fft.lastIndex / 2).reversedArray()
            }

            else -> fft
        }
    }

    /**
     * Amplifica valores altos enquanto suprime valores baixos, geralmente dá uma sensação mais poderosa
     * Aplica uma função quadrática que enfatiza picos de áudio
     *
     * @param fft Dados FFT originais
     * @param param Parâmetro de ajuste, modifique para seu gosto (valores maiores = menos amplificação)
     * @return FFT com efeito de potência aplicado
     */
    fun getPowerFft(fft: DoubleArray, param: Double = 100.0): DoubleArray {
        return fft.map { it * it / param }.toDoubleArray()
    }

    /**
     * Helper para rotacionar o canvas. Use o painter `Rotate` se quiser rotacionar painter(s) inteiros
     *
     * @param canvas Canvas para rotacionar
     * @param rotation Rotação em graus
     * @param xRotation Ponto de rotação X, 1f = largura total do canvas
     * @param yRotation Ponto de rotação Y, 1f = altura total do canvas
     * @param d Operação de desenho a ser executada com rotação aplicada
     */
    fun rotateHelper(canvas: Canvas, rotation: Float, xRotation: Float, yRotation: Float, d: () -> Unit) {
        canvas.save()  // Salva estado atual do canvas
        canvas.rotate(rotation, canvas.width * xRotation, canvas.height * yRotation)
        d()  // Executa o desenho rotacionado
        canvas.restore()  // Restaura estado original
    }

    /**
     * Helper para transladar o canvas e fornecer funcionalidade de direção
     * Permite desenhar visualizações em diferentes direções (cima, baixo, ambas)
     *
     * @param canvas Canvas para manipular
     * @param direcao Direção: UP_OUT (cima/fora), DOWN_IN (baixo/dentro), BOTH (ambas)
     * @param xRotation Ponto de referência X, 1f = largura total do canvas
     * @param yRotation Ponto de referência Y, 1f = altura total do canvas
     * @param draw Operação de desenho a ser executada
     */
    fun drawHelper(canvas: Canvas, direction: Direcao, xRotation: Float, yRotation: Float, draw: () -> Unit) {
        canvas.save()
        when (direction) {
            Cima -> {
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                draw()
            }

            Baixo -> {
                canvas.scale(1f, -1f, canvas.width / 2f, canvas.height / 2f)
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                draw()
            }

            Cima_Baixo -> {
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                draw()
                canvas.scale(1f, -1f)
                draw()
            }
        }
        canvas.restore()
    }

    /**
     * Helper para direção com operações de desenho separadas
     * Permite diferentes lógicas de desenho para direções simples vs bidirecionais
     *
     * @param canvas Canvas para manipular
     * @param direcao Direção da visualização
     * @param xRotation Ponto de referência X
     * @param yRotation Ponto de referência Y
     * @param upDownDraw Operação de desenho para UP_OUT e DOWN_IN
     * @param bothDraw Operação de desenho específica para BOTH
     */
    fun drawHelper(canvas: Canvas, direction: Direcao, xRotation: Float, yRotation: Float, upDownDraw: () -> Unit, bothDraw: () -> Unit) {
        canvas.save()
        when (direction) {
            Cima -> {
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                upDownDraw()
            }

            Baixo -> {
                canvas.scale(1f, -1f, canvas.width / 2f, canvas.height / 2f)
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                upDownDraw()
            }

            Cima_Baixo -> {
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                bothDraw()
            }
        }
        canvas.restore()
    }

    /**
     * Helper para direção com controle total sobre cada tipo de desenho
     * Máxima flexibilidade - permite lógica completamente diferente para cada direção
     *
     * @param canvas Canvas para manipular
     * @param direcao Direção da visualização
     * @param xRotation Ponto de referência X
     * @param yRotation Ponto de referência Y
     * @param upOutDraw Operação específica para direção UP_OUT
     * @param downInDraw Operação específica para direção DOWN_IN
     * @param bothDraw Operação específica para direção BOTH
     */
    fun drawHelper(
        canvas: Canvas, direction: Direcao, xRotation: Float, yRotation: Float, upOutDraw: () -> Unit, downInDraw: () -> Unit, bothDraw: () -> Unit,
    ) {
        canvas.save()
        when (direction) {
            Cima -> {
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                upOutDraw()
            }

            Baixo -> {
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                downInDraw()
            }

            Cima_Baixo -> {
                canvas.translate(canvas.width * xRotation, canvas.height * yRotation)
                bothDraw()
            }
        }
        canvas.restore()
    }


    /**
     * Modelo com gravidade para suavizar valores FFT brutos
     * Simula física real - valores sobem rapidamente mas caem gradualmente
     * Cria efeito visual mais natural e suave nas visualizações
     */
    class GravityModel(
        var height: Float = 0f,    // Altura atual da barra/elemento
        var dy: Float = 0f,        // Velocidade de queda (delta Y)
        var ay: Float = 2f,        // Aceleração da gravidade
    ) {
        /**
         * Atualiza o modelo com novo valor de áudio
         * @param h Novo valor de altura do áudio
         */
        fun update(h: Float) {
            if (h > height) {
                // Novo pico - sobe imediatamente
                height = h
                dy = 0f  // Reseta velocidade de queda
            }
            // Aplica gravidade - queda gradual
            height -= dy
            dy += ay  // Acelera a queda

            // Não deixa ir abaixo de zero
            if (height < 0) {
                height = 0f
                dy = 0f
            }
        }
    }
}