//package io.github.jeffshee.visualizer.desenhadores.diversos
//
//import android.graphics.*
//import io.github.jeffshee.visualizer.desenhadores.Painter
//import io.github.jeffshee.visualizer.utilitarios.VisualizerHelper
//import kotlin.math.min
//
//class IconeSimples(
//    var bitmap: Bitmap,
//    //
//    var radiusR: Float = .3f
//) : Painter() {
//
//    private val matrix = Matrix()
//
//    companion object {
//        // Função para criar um bitmap circular a partir de um bitmap quadrado
//        fun getCircledBitmap(bitmap: Bitmap): Bitmap {
//            val tmpBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//            val tmpCanvas = Canvas(tmpBitmap)
//            val tmpPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//            val tmpRect = Rect(0, 0, bitmap.width, bitmap.height)
//            tmpCanvas.drawARGB(0, 0, 0, 0)
//            tmpCanvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, bitmap.width / 2f, tmpPaint)
//            tmpPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//            tmpCanvas.drawBitmap(bitmap, tmpRect, tmpRect, tmpPaint)
//            return tmpBitmap
//        }
//    }
//
//    // Método de cálculo (vazio neste caso)
//    override fun calc(helper: VisualizerHelper) {
//    }
//
//    // Método para desenhar o ícone
//    override fun draw(canvas: Canvas, helper: VisualizerHelper) {
//        val shortest = min(canvas.width, canvas.height)
//        bitmap.apply bitmap@{
//            val radius = shortest * radiusR
//            matrix.apply {
//                // Escala o bitmap para o tamanho desejado
//                postScale(radius / this@bitmap.width, radius / this@bitmap.width)
//                // Centraliza o bitmap
//                postTranslate(-radius / 2f, -radius / 2f)
//            }
//            // Desenha o bitmap no centro do canvas
//            drawHelper(canvas, "a", .5f, .5f) {
//                canvas.drawBitmap(this, matrix, null)
//            }
//            matrix.reset()
//        }
//    }
//}
