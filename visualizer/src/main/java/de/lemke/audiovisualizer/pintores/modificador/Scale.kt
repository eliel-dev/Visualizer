package de.lemke.audiovisualizer.pintores.modificador

import android.graphics.Canvas
import android.graphics.Paint
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper

class Scale(
    vararg val painters: Pintor,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var pxR: Float = .5f,
    var pyR: Float = .5f,
) : Pintor() {

    override var paint = Paint()

    override fun calc(helper: VisualizerHelper) {
        painters.forEach { painter ->
            painter.calc(helper)
        }
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        canvas.save()
        canvas.scale(scaleX, scaleY, pxR * canvas.width, pyR * canvas.height)
        painters.forEach { painter ->
            painter.paint.apply { colorFilter = paint.colorFilter;xfermode = paint.xfermode }
            painter.draw(canvas, helper)
        }
        canvas.restore()
    }
}