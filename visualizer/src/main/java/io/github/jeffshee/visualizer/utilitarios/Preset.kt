//package io.github.jeffshee.visualizer.utilitarios
//
//import android.graphics.*
//import io.github.jeffshee.visualizer.desenhadores.*
//import io.github.jeffshee.visualizer.desenhadores.espectro.*
//import io.github.jeffshee.visualizer.desenhadores.diversos.*
//import io.github.jeffshee.visualizer.desenhadores.modificadores.*
//
//class Preset {
//    companion object {
//
//        /**
//         * Feel free to add your awesome preset here ;)
//         * Hint: You can use `Sequential` painter to group multiple painters together as a single painter
//         */
//        fun getPreset(name: String): Painter {
//            return when (name) {
//                "debug" -> FftBar()
//                else -> FftBar()
//            }
//        }
//
//        fun getPresetWithBitmap(name: String, bitmap: Bitmap): Painter {
//            return when (name) {
//                "cIcon" -> Sequential(listOf(Rotate(FftCircle()), IconeSimples(IconeSimples.getCircledBitmap(bitmap))))
//                "cWaveRgbIcon" -> Sequential(
//                    listOf(
//                        Rotate(FftCircleWaveRgb()),
//                        IconeSimples(IconeSimples.getCircledBitmap(bitmap))
//                    )
//                )
//                "liveBg" -> Scale(Shake(Fundo(bitmap)), 1.02f, 1.02f)
//                "debug" -> IconeSimples(bitmap)
//                else -> IconeSimples(bitmap)
//            }
//        }
//    }
//}