package io.github.jeffshee.visualizer.painters.fft

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import io.github.jeffshee.visualizer.painters.Painter
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class VisualizacaoPersonalizada : Painter() {
    override var paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.CYAN
        isAntiAlias = true
    }
    private val path = Path()
    // Usamos 40 pontos para uma forma circular simples
    private val numPoints = 40
    // Parâmetros ajustados para manter o círculo central pequeno:
    private var radialOffsets = FloatArray(numPoints) { 0f }
    private val baseRadiusFactor = 0.12f      // Aproximadamente 12% da menor dimensão da tela
    private val maxNoise = 3f                 // Reduzido para manter a variação local sutil
    private val smoothing = 0.1f
    private val noiseSensitivity = 0.2f
    private val beatSensitivity = 0.8f
    private var globalPulse = 0f
    // Fator de amplificação menor para evitar o círculo ocupar espaço demais
    private val amplitudeFactor = 8f

    override fun calc(helper: VisualizerHelper) {
        val generalFft = helper.getFftMagnitudeRange(0, 1000)
        val bassFft = helper.getFftMagnitudeRange(0, 300)
        val generalAmp = if (generalFft.isNotEmpty()) generalFft.average().toFloat() else 0f
        val bassAmp = if (bassFft.isNotEmpty()) bassFft.average().toFloat() else 0f
        
        val amplifiedGeneral = generalAmp * amplitudeFactor
        val amplifiedBass = bassAmp * amplitudeFactor
        
        globalPulse = globalPulse + 0.2f * (amplifiedBass * beatSensitivity - globalPulse)
        
        for (i in 0 until numPoints) {
            val target = Random.nextFloat() * maxNoise * amplifiedGeneral * noiseSensitivity
            radialOffsets[i] += smoothing * (target - radialOffsets[i])
        }
        // Modula a cor de forma sutil
        val blue = (150 + (amplifiedGeneral.coerceAtMost(50f))).toInt()
        paint.color = Color.rgb(0, 255 - blue / 2, blue)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        canvas.drawColor(Color.BLACK)
        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f
        val minDim = min(canvas.width, canvas.height)
        // Calcula o raio base contido, pulsado conforme os graves
        val baseRadius = minDim * baseRadiusFactor * (1f + globalPulse)
        
        val points = Array(numPoints) { i ->
            val angle = (2 * Math.PI * i / numPoints).toFloat()
            val r = baseRadius + radialOffsets[i]
            centerX + r * cos(angle) to centerY + r * sin(angle)
        }
        
        path.reset()
        val (firstX, firstY) = points[0]
        val (lastX, lastY) = points[numPoints - 1]
        path.moveTo((lastX + firstX) / 2f, (lastY + firstY) / 2f)
        for (i in 0 until numPoints) {
            val current = points[i]
            val next = points[(i + 1) % numPoints]
            val midX = (current.first + next.first) / 2f
            val midY = (current.second + next.second) / 2f
            path.quadTo(current.first, current.second, midX, midY)
        }
        path.close()
        
        canvas.drawPath(path, paint)
    }
}
