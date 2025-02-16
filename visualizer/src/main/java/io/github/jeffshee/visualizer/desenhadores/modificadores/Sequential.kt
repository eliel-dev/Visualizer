//package io.github.jeffshee.visualizer.desenhadores.modificadores
//
//import android.graphics.Canvas
//import io.github.jeffshee.visualizer.desenhadores.Painter
//import io.github.jeffshee.visualizer.utilitarios.VisualizerHelper
//
//class Sequential(var painters: List<Painter>) : Painter() {
//
//    override fun calc(helper: VisualizerHelper) {
//        painters.forEach { painter ->
//            painter.calc(helper)
//        }
//    }
//
//    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
//        painters.forEach { painter -> painter.draw(canvas, helper) }
//    }
//}