package de.lemke.audiovisualizer.pintores.misc

import android.graphics.*
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima
import de.lemke.audiovisualizer.utils.VisualizerHelper
import kotlin.math.max

class Fundo(
    var bitmap: Bitmap,
    var scaleXY: Float = 1f,
    override var paint: Paint = Paint(),
) : Pintor() {
    private val matrix = Matrix()
    override fun calc(helper: VisualizerHelper) {}
    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        bitmap.apply bitmap@{
            matrix.apply {
                val scale = max(
                    canvas.width.toFloat() * scaleXY / this@bitmap.width,
                    canvas.height.toFloat() * scaleXY / this@bitmap.height
                )
                postScale(scale, scale)
                postTranslate(-scale * this@bitmap.width / 2f, -scale * this@bitmap.height.toFloat() / 2f)
            }
            drawHelper(canvas, Cima, .5f, .5f) {
                canvas.drawBitmap(this, matrix, paint)
            }
            matrix.reset()
        }
    }
}
