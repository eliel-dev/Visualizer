package de.lemke.audiovisualizer.utils

import android.graphics.*
import de.lemke.audiovisualizer.pintores.*
import de.lemke.audiovisualizer.pintores.fft.*
import de.lemke.audiovisualizer.pintores.fft.FftBarra
import de.lemke.audiovisualizer.pintores.misc.*
import de.lemke.audiovisualizer.pintores.modificador.*
import de.lemke.audiovisualizer.utils.Preset.Companion.PresetType.DEBUG
import de.lemke.audiovisualizer.utils.Preset.Companion.PresetType.ICON
import de.lemke.audiovisualizer.utils.Preset.Companion.PresetType.LIVE_BG
import de.lemke.audiovisualizer.utils.Preset.Companion.PresetType.WAVE_RGB_ICON

class Preset {
    companion object {
        enum class PresetType {
            ICON, WAVE_RGB_ICON, LIVE_BG, DEBUG
        }

        fun getPreset(type: PresetType): Pintor {
            return when (type) {
                DEBUG -> FftBarra()
                else -> FftBarra()
            }
        }

        fun getPresetWithBitmap(type: PresetType, bitmap: Bitmap): Pintor {
            return when (type) {
                ICON -> Compose(Rotate(FftLinhaCircular()), Icone(Icone.getCircledBitmap(bitmap)))
                WAVE_RGB_ICON -> Compose(Rotate(FftOndaCircularRgb()), Icone(Icone.getCircledBitmap(bitmap)))
                LIVE_BG -> Scale(Shake(Fundo(bitmap)), scaleX = 1.02f, scaleY = 1.02f)
                DEBUG -> Icone(bitmap)
                else -> Icone(bitmap)
            }
        }
    }
}