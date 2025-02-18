//package io.github.jeffshee.visualizer.desenhadores.diversos
//
//import android.graphics.*
//import io.github.jeffshee.visualizer.desenhadores.Painter
//import io.github.jeffshee.visualizer.utilitarios.VisualizerHelper
//import kotlin.math.max
//
//// Classe Fundo que herda de Painter, responsável por desenhar um fundo usando um bitmap
//class Fundo(
//    var bitmap: Bitmap, // Bitmap que será usado como fundo
//    var scaleXY: Float = 1f // Fator de escala para o bitmap, padrão é 1f (sem escala)
//) : Painter() {
//
//    private val matrix = Matrix() // Matrix para aplicar transformações no bitmap
//
//    // Método de cálculo (vazio neste caso)
//    override fun calc(helper: VisualizerHelper) {
//    }
//
//    // Método para desenhar o fundo no canvas
//    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
//        bitmap.apply bitmap@{
//            matrix.apply {
//                // Calcula a escala necessária para preencher o canvas mantendo a proporção do bitmap
//                val scale = max(
//                    canvas.width.toFloat() * scaleXY / this@bitmap.width,
//                    canvas.height.toFloat() * scaleXY / this@bitmap.height
//                )
//                // Aplica a escala à matrix
//                postScale(scale, scale)
//                // Centraliza o bitmap no canvas
//                postTranslate(-scale * this@bitmap.width / 2f, -scale * this@bitmap.height.toFloat() / 2f)
//            }
//            // Desenha o bitmap usando um helper, posicionando-o no centro do canvas
//            drawHelper(canvas, "a", .5f, .5f) {
//                canvas.drawBitmap(this, matrix, null)
//            }
//            // Reseta a matrix para uso futuro
//            matrix.reset()
//        }
//    }
//}
