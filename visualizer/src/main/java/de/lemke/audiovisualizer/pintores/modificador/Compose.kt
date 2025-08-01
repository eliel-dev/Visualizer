package de.lemke.audiovisualizer.pintores.modificador

import android.graphics.Canvas
import android.graphics.Paint
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper

class Compose(vararg val painters: Pintor) : Pintor() {
    override var paint = Paint()

    override fun calc(helper: VisualizerHelper) {
        painters.forEach { painter ->
            painter.calc(helper)
        }
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        painters.forEach { painter ->
            painter.paint.apply { colorFilter = paint.colorFilter;xfermode = paint.xfermode }
            painter.draw(canvas, helper)
        }
    }
}