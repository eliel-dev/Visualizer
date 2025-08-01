package de.lemke.audiovisualizerdemo

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.BitmapFactory
import android.graphics.Paint.Cap.ROUND
import android.graphics.Paint.Style.FILL
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.lemke.audiovisualizer.pintores.Pintor
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Cima_Baixo
import de.lemke.audiovisualizer.pintores.Pintor.Companion.Direcao.Baixo
import de.lemke.audiovisualizer.pintores.fft.*
import de.lemke.audiovisualizer.pintores.fft.BarrasHRGB
import de.lemke.audiovisualizer.pintores.fft.FftBarra
import de.lemke.audiovisualizer.pintores.fft.FftCOnda
import de.lemke.audiovisualizer.pintores.fft.FftOnda
import de.lemke.audiovisualizer.pintores.fft.FftOndaRgb
import de.lemke.audiovisualizer.pintores.misc.Gradiente
import de.lemke.audiovisualizer.pintores.misc.Gradiente.Companion.LINEAR_HORIZONTAL
import de.lemke.audiovisualizer.pintores.misc.Gradiente.Companion.LINEAR_VERTICAL
import de.lemke.audiovisualizer.pintores.misc.Gradiente.Companion.LINEAR_VERTICAL_MIRROR
import de.lemke.audiovisualizer.pintores.misc.Gradiente.Companion.RADIAL
import de.lemke.audiovisualizer.pintores.misc.Gradiente.Companion.SWEEP
import de.lemke.audiovisualizer.pintores.misc.Icone
import de.lemke.audiovisualizer.pintores.modificador.*
import de.lemke.audiovisualizer.pintores.waveform.WfmAnalog
import de.lemke.audiovisualizer.utils.Preset
import de.lemke.audiovisualizer.utils.Preset.Companion.PresetType.ICON
import de.lemke.audiovisualizer.utils.Preset.Companion.PresetType.LIVE_BG
import de.lemke.audiovisualizer.utils.Preset.Companion.PresetType.WAVE_RGB_ICON
import de.lemke.audiovisualizer.utils.VisualizerHelper
import de.lemke.audiovisualizerdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "VisualizerDemo"
    }

    private lateinit var binding: ActivityMainBinding
    private val microphonePermissionCallback: ActivityResultCallback<Boolean> =
        ActivityResultCallback { granted -> if (granted) permissionGranted() else permissionNotGranted() }
    private val activityResultLauncher = registerForActivityResult(RequestPermission(), microphonePermissionCallback)
    private lateinit var helper: VisualizerHelper
    private var atual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestMicrophonePermission()
    }

    fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PERMISSION_GRANTED) microphonePermissionCallback.onActivityResult(true)
        else activityResultLauncher.launch(RECORD_AUDIO)
    }

    private fun permissionGranted() {
        Log.d(TAG, "Microphone permission granted")
        initVisualizer()
    }

    private fun permissionNotGranted() {
        Log.d(TAG, "Microphone permission not granted")
    }

    private fun initVisualizer() {
        val circleBitmap = Icone.getCircledBitmap(BitmapFactory.decodeResource(resources, R.drawable.chino512))
        helper = VisualizerHelper(0)
        val list = listOf(
            // Basic components
            BarrasHRGB(),
            Compose(
                Move(WfmAnalog(), yRelative = -.3f),
                Move(FftBarra(), yRelative = -.1f),
                Move(FftLinha(), yRelative = .1f),
                Move(FftOnda(), yRelative = .3f),
                Move(FftOndaRgb(), yRelative = .5f)
            ),
            Compose(
                //Move(FftBarra(direcao = DOWN_IN), yRelative = -.3f),
                //Move(FftLinha(direcao = DOWN_IN), yRelative = -.1f),
                Move(FftOnda(direcao = Baixo), yRelative = .1f),
                Move(FftOnda(direcao = Pintor.Companion.Direcao.Cima), yRelative = .3f),
                //Move(FftOndaRgb(direcao = DOWN_IN), yRelative = .3f)
            ),
            Compose(
                Move(FftBarra(direcao = Cima_Baixo), yRelative = -.3f),
                Move(FftLinha(direcao = Cima_Baixo), yRelative = -.1f),
                Move(FftOnda(direcao = Cima_Baixo), yRelative = .1f),
                Move(FftOndaRgb(direcao = Cima_Baixo), yRelative = .3f)
            ),
            // Basic components (Circle)
            Compose(
                Move(FftLinhaCircular(), xRelative = -.3f),
                FftCOnda(),
                Move(FftOndaCircularRgb(), xRelative = .3f)
            ),
            Compose(
                Move(FftLinhaCircular(direcao = Baixo), xRelative = -.3f),
                FftCOnda(direcao = Baixo),
                Move(FftOndaCircularRgb(direcao = Baixo), xRelative = .3f)
            ),
            Compose(
                Move(FftLinhaCircular(direcao = Cima_Baixo), xRelative = -.3f),
                FftCOnda(direcao = Cima_Baixo),
                Move(FftOndaCircularRgb(direcao = Cima_Baixo), xRelative = .3f)
            ),
            //Blend
            Mistura(FftLinha().apply { paint.strokeWidth = 8f; paint.strokeCap = ROUND }, Gradiente(LINEAR_HORIZONTAL)),
            Mistura(FftLinha().apply { paint.strokeWidth = 8f; paint.strokeCap = ROUND }, Gradiente(LINEAR_VERTICAL, hsv = true)),
            Mistura(FftLinha().apply { paint.strokeWidth = 8f; paint.strokeCap = ROUND }, Gradiente(LINEAR_VERTICAL_MIRROR, hsv = true)),
            Mistura(FftLinhaCircular().apply { paint.strokeWidth = 8f; paint.strokeCap = ROUND }, Gradiente(RADIAL)),
            Mistura(FftBarraCircular(direcao = Cima_Baixo, gapX = 8f).apply { paint.style = FILL }, Gradiente(SWEEP, hsv = true)),
            Mistura(FftBarraCircular(direcao = Cima_Baixo, gapX = 8f).apply { paint.style = FILL }, Gradiente(SWEEP, hsv = true)),
            // Composition
            Glitch(Batida(Preset.getPresetWithBitmap(ICON, circleBitmap))),
            Compose(
                WfmAnalog().apply { paint.alpha = 150 },
                Shake(Preset.getPresetWithBitmap(WAVE_RGB_ICON, circleBitmap)).apply { animX.duration = 1000; animY.duration = 2000 }),
            Compose(
                Preset.getPresetWithBitmap(LIVE_BG, BitmapFactory.decodeResource(resources, R.drawable.background)),
                FftLinhaCircular().apply { paint.strokeWidth = 8f;paint.strokeCap = ROUND }
            )
        )
        binding.visualizerView.setup(helper, list[atual])
        binding.next.setOnClickListener {
            Log.d(TAG, "Next")
            atual = (atual + 1) % list.size
            binding.visualizerView.setup(helper, list[atual])
        }
        binding.previous.setOnClickListener {
            Log.d(TAG, "Previous")
            atual = (atual - 1 + list.size) % list.size
            binding.visualizerView.setup(helper, list[atual])
        }
    }

    override fun onDestroy() {
        helper.release()
        super.onDestroy()
    }
}