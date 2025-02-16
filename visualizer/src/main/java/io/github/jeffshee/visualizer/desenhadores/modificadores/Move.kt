//package io.github.jeffshee.visualizer.desenhadores.modificadores
//
//import android.graphics.Canvas
//import io.github.jeffshee.visualizer.desenhadores.Painter
//import io.github.jeffshee.visualizer.utilitarios.VisualizerHelper
//
//class Move : Painter {
//    var painters: List<Painter>
//    //
//    var xR: Float
//    var yR: Float
//
//    constructor(painters: List<Painter>, xR: Float = 0f, yR: Float = 0f) {
//        this.painters = painters
//        this.xR = xR
//        this.yR = yR
//    }
//
//    constructor(painter: Painter, xR: Float = 0f, yR: Float = 0f) : this(
//        listOf(painter), xR, yR
//    )
//
//    override fun calc(helper: VisualizerHelper) {
//        painters.forEach { painter ->
//            painter.calc(helper)
//        }
//    }
//
//    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
//        canvas.save()
//        canvas.translate(canvas.width * xR, canvas.height * yR)
//        painters.forEach { painter ->
//            painter.draw(canvas, helper)
//        }
//        canvas.restore()
//    }
//}