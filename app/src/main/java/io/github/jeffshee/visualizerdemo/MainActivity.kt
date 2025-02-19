package io.github.jeffshee.visualizerdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_main.*
import io.github.jeffshee.visualizer.painters.fft.BarraDeLedVertical
import io.github.jeffshee.visualizer.painters.fft.BarrasVerticais
import io.github.jeffshee.visualizer.painters.fft.BarrasVerticaisLED
import io.github.jeffshee.visualizer.painters.fft.FftCWaveRgb
import io.github.jeffshee.visualizer.utils.VisualizerHelper
import io.github.jeffshee.visualizer.painters.modifier.Beat

class MainActivity : AppCompatActivity() {

    private lateinit var helper: VisualizerHelper
    private lateinit var background: Bitmap
    private lateinit var bitmap: Bitmap
    private lateinit var circleBitmap: Bitmap
    private var current = 0
    private lateinit var visualizationTitle: TextView
    private lateinit var switchVisualizationButton: Button

    private val visualizations = listOf(
        "Barra de LED Vertical" to "Visualização de barras verticais com LEDs",
        "Barras Verticais" to "Visualização de barras verticais",
        "Barras Verticais LED" to "Visualização de barras verticais com LEDs coloridos",
        "Visualização Personalizada" to "Visualização customizada com círculo animado",
        "Visualização Personalizada2" to "Visualização customizada com círculo animado2"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        } else init()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0 && grantResults[0] == 0) init()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }

    private fun init() {
        //background = BitmapFactory.decodeResource(resources, R.drawable.background)
        //bitmap = BitmapFactory.decodeResource(resources, R.drawable.chino512)

        helper = VisualizerHelper(0)
        // Atualize a lista de painters para incluir todas as visualizações:
        val painterLists = listOf(
            listOf(BarraDeLedVertical()),  // Nova visualização: Barra de LED Vertical
            listOf(BarrasVerticais()),      // Visualização: Barras Verticais
            listOf(BarrasVerticaisLED()),     // Visualização: Barras Verticais LED
            listOf(Beat(FftCWaveRgb(), startHz = 60, endHz = 800, pxR = 0.5f, pyR = 0.5f, radiusR = 1f, beatAmpR = 0.7f, peak = 200f))
        )
        
        visual.setPainterList(
            helper, painterLists[current]
        )
        // Chame o método para esconder os FPS, se disponível
        // visual.setShowFps(false)
        
        visual.setOnLongClickListener {
            if (current < painterLists.lastIndex) current++ else current = 0
            visual.setPainterList(helper, painterLists[current])
            updateVisualizationInfo()
            true
        }

        visualizationTitle = findViewById(R.id.visualizationTitle)
        switchVisualizationButton = findViewById(R.id.switchVisualizationButton)

        updateVisualizationInfo()

        switchVisualizationButton.setOnClickListener {
            if (current < painterLists.lastIndex) current++ else current = 0
            visual.setPainterList(helper, painterLists[current])
            updateVisualizationInfo()
        }

        // Remova ou comente a linha abaixo caso ela ative o FPS
         // visual.showFps(false)
    }

    private fun updateVisualizationInfo() {
        visualizationTitle.text = visualizations[current].first
    }

    override fun onDestroy() {
        helper.release()
        super.onDestroy()
    }
}