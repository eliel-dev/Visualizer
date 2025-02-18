package io.github.jeffshee.visualizer.desenhadores

import android.graphics.Canvas
import io.github.jeffshee.visualizer.utilitarios.VisualizerHelper
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction
import kotlin.math.cos
import kotlin.math.sin

abstract class Painter {

    private val li = LinearInterpolator()
    private val sp = AkimaSplineInterpolator()

    abstract fun calc(helper: VisualizerHelper)

    /**
     * Uma função abstrata que todos os pintores devem implementar e fazer seus desenhos lá.
     *
     * @param canvas o canvas do VisualizerView
     * @param helper o visualizerHelper do VisualizerView
     */
    abstract fun draw(canvas: Canvas, helper: VisualizerHelper)

    /**
     * Interpola o espectro FFT
     *
     * O Android não captura um espectro de alta resolução, e queremos que o número de bandas seja ajustável,
     * então fazemos uma interpolação aqui.
     *
     * (Por exemplo, para mostrar 64 bandas em frequências de 0Hz a 1200Hz, o Android retorna apenas ~10 valores FFT.
     * Então precisamos interpolar esses ~10 valores em 64 valores para encaixá-los em nossas bandas)
     *
     * @param gravityModels Array de gravityModel
     * @param sliceNum Número de Fatias
     * @param interpolator Qual interpolador usar, `li` para Linear, `sp` para Spline
     *
     * @return uma `PolynomialSplineFunction` (psf). Para obter o valor, use
     * `psf.value(x)`, onde `x` deve ser um valor Double de 0 a `num`
     */
    fun interpolateFft(
        gravityModels: Array<GravityModel>, sliceNum: Int, interpolator: String
    ): PolynomialSplineFunction {
        val nRaw = gravityModels.size
        val xRaw = DoubleArray(nRaw) { (it * sliceNum).toDouble() / (nRaw - 1) }
        val yRaw = DoubleArray(nRaw)
        gravityModels.forEachIndexed { index, bar -> yRaw[index] = bar.height.toDouble() }
        val psf: PolynomialSplineFunction
        psf = when (interpolator) {
            "li", "linear" -> li.interpolate(xRaw, yRaw)
            "sp", "spline" -> sp.interpolate(xRaw, yRaw)
            else -> li.interpolate(xRaw, yRaw)
        }
        return psf
    }

    /**
     * Interpola o espectro FFT (Círculo)
     *
     * Similar a `interpolateFft()`. No entanto, isso é destinado para Fft de `getCircleFft()`
     *
     * @param gravityModels Array de gravityModel
     * @param sliceNum Número de Fatias
     * @param interpolator Qual interpolador usar, `li` para Linear, `sp` para Spline
     *
     * @return uma `PolynomialSplineFunction` (psf). Para obter o valor, use
     * `psf.value(x)`, onde `x` deve ser um valor Double de 0 a `num`
     */
    fun interpolateFftCircle(
        gravityModels: Array<GravityModel>, sliceNum: Int, interpolator: String
    ): PolynomialSplineFunction {
        val nRaw = gravityModels.size
        val xRaw = DoubleArray(nRaw) { ((it - 1) * sliceNum).toDouble() / (nRaw - 1 - 2) }
        val yRaw = DoubleArray(nRaw)
        gravityModels.forEachIndexed { index, bar -> yRaw[index] = bar.height.toDouble() }
        val psf: PolynomialSplineFunction
        psf = when (interpolator) {
            "li", "linear" -> li.interpolate(xRaw, yRaw)
            "sp", "spline" -> sp.interpolate(xRaw, yRaw)
            else -> li.interpolate(xRaw, yRaw)
        }
        return psf
    }

    /**
     * Verifica se está quieto o suficiente para que possamos pular o desenho
     * @param fft Fft
     * @return true se estiver quieto, false caso contrário
     */
    fun isQuiet(fft: DoubleArray): Boolean {
        val threshold = 5f
        fft.forEach { if (it > threshold) return false }
        return true
    }

    /**
     * Converte Polar para Cartesiano
     * @param radius Raio
     * @param theta Theta
     * @return FloatArray de (x,y) do Cartesiano
     */
    fun toCartesian(radius: Float, theta: Float): FloatArray {
        val x = radius * cos(theta)
        val y = radius * sin(theta)
        return floatArrayOf(x, y)
    }

    /**
     * Corrige o Fft para que o início e o fim se conectem perfeitamente. Use isso com `interpolateFftCircle()`
     *
     * `[0, 1, ..., n] -> [n-1, 0, 1, ..., n-1, 0, 1]`
     *
     * @param fft Fft
     * @return CircleFft
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
     * Corrige o Fft para um MirrorFft
     *
     * @param fft Fft
     * @param mode quando 0 -> não faz nada
     *             quando 1 ->
     *              `[0, 1, ..., n] -> [n, ..., 1, 0, 0, 1, ..., n]`
     *             quando 2 ->
     *              `[0, 1, ..., n] -> [0, 1, ..., n, n, ..., 1, 0]`
     *             quando 3 ->
     *             `[0, 1, ..., n] -> [n/2, ..., 1, 0, 0, 1, ..., n/2]`
     *             quando 4 ->
     *             `[0, 1, ..., n] -> [0, 1, ..., n/2, n/2, ..., 1, 0]`
     * @return MirrorFft
     */
    fun getMirrorFft(fft: DoubleArray, mode: Int = 1): DoubleArray {
        return when (mode) {
            1 -> {
                // Se o modo for 1, invertemos o array fft e concatenamos com ele mesmo
                fft.sliceArray(0..fft.lastIndex).reversedArray() + fft.sliceArray(0..fft.lastIndex)
            }
            2 -> {
                // Se o modo for 2, concatenamos o array fft com ele mesmo e depois com a sua versão inversa
                fft.sliceArray(0..fft.lastIndex) + fft.sliceArray(0..fft.lastIndex).reversedArray()
            }
            3 -> {
                // Se o modo for 3, pegamos a metade do array fft, invertemos e depois concatenamos com a metade original
                fft.sliceArray(0..fft.lastIndex / 2).reversedArray() + fft.sliceArray(0..fft.lastIndex / 2)
            }
            4 -> {
                // Se o modo for 4, pegamos a metade do array fft e concatenamos com a sua versão inversa
                fft.sliceArray(0..fft.lastIndex / 2) + fft.sliceArray(0..fft.lastIndex / 2).reversedArray()
            }
            else -> fft
            // Se o modo não for nenhum dos anteriores (1-4), retorna o array fft original
        }
    }

    /**
     * Aumenta valores altos enquanto suprime valores baixos, geralmente dá uma sensação poderosa
     * @param fft Fft
     * @param param Parâmetro, ajuste conforme sua preferência
     * @return PowerFft
     */
    fun getPowerFft(fft: DoubleArray, param: Double = 100.0): DoubleArray {
        return fft.map { it * it / param }.toDoubleArray()
    }

    /**
     * Um auxiliar para Rotacionar o canvas, use o pintor `Rotate` em vez disso se quiser rotacionar o(s) pintor(es) inteiro(s)
     * @param canvas Canvas
     * @param rot Rotação em graus
     * @param xR Ponto de rotação X, 1f = `canvas.width`
     * @param yR Ponto de rotação Y, 1f = `canvas.height`
     * @param d Operação de desenho aqui
     */
    fun rotateHelper(canvas: Canvas, rot: Float, xR: Float, yR: Float, d: () -> Unit) {
        canvas.save()
        canvas.rotate(rot, canvas.width * xR, canvas.height * yR)
        d()
        canvas.restore()
    }

    /**
     * Um auxiliar para Transladar o canvas e fornecer funcionalidade de Lado
     * @param canvas Canvas
     * @param side `a` `b` `ab` geralmente significa Para Cima(ou Para Fora), Para Baixo(ou Para Dentro), Ambos
     * @param xR Ponto de rotação X, 1f = `canvas.width`
     * @param yR Ponto de rotação Y, 1f = `canvas.height`
     * @param d Operação de desenho para o lado a aqui
     */
    fun drawHelper(canvas: Canvas, side: String, xR: Float, yR: Float, d: () -> Unit) {
        canvas.save()
        when (side) {
            "a" -> {
                canvas.translate(canvas.width * xR, canvas.height * yR)
                d()
            }
            "b" -> {
                canvas.scale(1f, -1f, canvas.width / 2f, canvas.height / 2f)
                canvas.translate(canvas.width * xR, canvas.height * yR)
                d()
            }
            "ab" -> {
                canvas.translate(canvas.width * xR, canvas.height * yR)
                d()
                canvas.scale(1f, -1f)
                d()
            }
        }
        canvas.restore()
    }

    /**
     * Um auxiliar para Transladar o canvas e fornecer funcionalidade de Lado
     * @param canvas Canvas
     * @param side `a` `b` `ab` geralmente significa Para Cima(ou Para Fora), Para Baixo(ou Para Dentro), Ambos
     * @param xR Ponto de rotação X, 1f = `canvas.width`
     * @param yR Ponto de rotação Y, 1f = `canvas.height`
     * @param d Operação de desenho para o lado a aqui
     * @param dab Operação de desenho para o lado ab aqui
     */
    fun drawHelper(canvas: Canvas, side: String, xR: Float, yR: Float, d: () -> Unit, dab: () -> Unit) {
        canvas.save()
        when (side) {
            "a" -> {
                canvas.translate(canvas.width * xR, canvas.height * yR)
                d()
            }
            "b" -> {
                canvas.scale(1f, -1f, canvas.width / 2f, canvas.height / 2f)
                canvas.translate(canvas.width * xR, canvas.height * yR)
                d()
            }
            "ab" -> {
                canvas.translate(canvas.width * xR, canvas.height * yR)
                dab()
            }
        }
        canvas.restore()
    }

    /**
     * Um auxiliar para Transladar o canvas e fornecer funcionalidade de Lado
     * @param canvas Canvas
     * @param side `a` `b` `ab` geralmente significa Para Cima(ou Para Fora), Para Baixo(ou Para Dentro), Ambos
     * @param xR Ponto de rotação X, 1f = `canvas.width`
     * @param yR Ponto de rotação Y, 1f = `canvas.height`
     * @param da Operação de desenho para o lado a aqui
     * @param db Operação de desenho para o lado b aqui
     * @param dab Operação de desenho para o lado ab aqui
     */
    fun drawHelper(
        canvas: Canvas, side: String, xR: Float, yR: Float, da: () -> Unit, db: () -> Unit, dab: () -> Unit
    ) {
        canvas.save()
        when (side) {
            "a" -> {
                canvas.translate(canvas.width * xR, canvas.height * yR)
                da()
            }
            "b" -> {
                canvas.translate(canvas.width * xR, canvas.height * yR)
                db()
            }
            "ab" -> {
                canvas.translate(canvas.width * xR, canvas.height * yR)
                dab()
            }
        }
        canvas.restore()
    }

    /**
     * Um modelo com gravidade. Útil para suavizar valores Fft brutos.
     */
    class GravityModel(
        var height: Float = 0f,
        var dy: Float = 0f,
        var ay: Float = 2f
    ) {
        fun update(h: Float) {
            if (h > height) {
                height = h
                dy = 0f
            }
            height -= dy
            dy += ay
            if (height < 0) {
                height = 0f
                dy = 0f
            }
        }
    }
}