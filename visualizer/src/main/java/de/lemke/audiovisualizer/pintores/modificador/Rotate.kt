package de.lemke.audiovisualizer.pintores.modificador

import android.graphics.Canvas
import android.graphics.Paint
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper

class Rotate(
    vararg val painters: Pintor,
    var pxR: Float = .5f,
    var pyR: Float = .5f,
    var rpm: Float = 1f,
    var offset: Float = 0f,
) : Pintor() {

    override var paint = Paint()

    private var rot: Float = 0f

    override fun calc(helper: VisualizerHelper) {
        painters.forEach { painter ->
            painter.calc(helper)
        }
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        rotateHelper(canvas, rot + offset, pxR, pyR) {
            painters.forEach { painter ->
                painter.paint.apply { colorFilter = paint.colorFilter;xfermode = paint.xfermode }
                painter.draw(canvas, helper)
            }
        }
        if (rpm != 0f) {
            rot += rpm / 10f
            rot %= 360f
        }
    }
}