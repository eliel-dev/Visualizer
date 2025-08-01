package de.lemke.audiovisualizer.pintores.modificador

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper

class Mistura(val src: Pintor, val dst: Pintor) : Pintor() {
    override var paint = Paint()

    override fun calc(helper: VisualizerHelper) {
        src.calc(helper)
        dst.calc(helper)
    }

    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        dst.apply { paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
        src.draw(canvas, helper)
        dst.draw(canvas, helper)
        canvas.restore()
    }
}