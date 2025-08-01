package de.lemke.audiovisualizer.pintores.misc

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.utils.VisualizerHelper

class SimpleText(
    override var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE },
    var text: String = "",
    var x: Float = 50f,
    var y: Float = 50f
) : Pintor() {
    override fun calc(helper: VisualizerHelper) {}
    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
        canvas.drawText(text, x, y, paint)
    }
}